{% set filecollector = {} %}

{% set s3_bucket = salt['pillar.get']('filecollector:s3_bucket') %}
{% set s3_location = salt['pillar.get']('filecollector:s3_location') %}
{% set s3_region = salt['pillar.get']('filecollector:s3_region') %}
{% set adlsv2_storage_account = salt['pillar.get']('filecollector:adlsv2_storage_account') %}
{% set adlsv2_storage_container = salt['pillar.get']('filecollector:adlsv2_storage_container') %}
{% set adlsv2_storage_location = salt['pillar.get']('filecollector:adlsv2_storage_location') %}
{% set account_id = salt['pillar.get']('filecollector:accountId') %}
{% set destination = salt['pillar.get']('filecollector:destination') %}
{% set issue = salt['pillar.get']('filecollector:issue') %}
{% set description = salt['pillar.get']('filecollector:description') %}
{% set start_time = salt['pillar.get']('filecollector:startTime') %}
{% set end_time = salt['pillar.get']('filecollector:endTime') %}
{% set label_filter = salt['pillar.get']('filecollector:labelFilter') %}
{% set include_salt_logs = salt['pillar.get']('filecollector:includeSaltLogs') %}
{% set update_package = salt['pillar.get']('filecollector:updatePackage') %}
{% set skip_test_cloud_storage = salt['pillar.get']('filecollector:skipTestCloudStorage') %}
{% set additional_logs = salt['pillar.get']('filecollector:additionalLogs') %}
{% set mode = salt['pillar.get']('filecollector:mode') %}
{% set uuid = salt['pillar.get']('filecollector:uuid') %}

{% if s3_location and not s3_region %}
  {%- set instanceDetails = salt.cmd.run('curl -s http://169.254.169.254/latest/dynamic/instance-identity/document') | load_json %}
  {%- set s3_region = instanceDetails['region'] %}
{% endif %}

{% if mode == 'CLOUDERA_MANAGER' %}
  {% set compressed_file_pattern = '/var/lib/filecollector/*.zip' %}
{% else %}
  {% set compressed_file_pattern = '/var/lib/filecollector/*.gz' %}
{% endif %}

{% set cloud_storage_upload_params = None %}
{% set test_cloud_storage_upload_params = None %}
{% if s3_location %}
  {% set cloud_storage_upload_params = "s3 upload -e -p '" + compressed_file_pattern + "' --location " + s3_location + " --bucket " + s3_bucket +  " --region " + s3_region %}
  {% set test_cloud_storage_upload_params = "s3 upload -e -p /tmp/.test_cloud_storage_upload.txt --location " + s3_location + " --bucket " + s3_bucket +  " --region " + s3_region %}
{% elif adlsv2_storage_location %}
  {% set cloud_storage_upload_params = "abfs upload -p '" + compressed_file_pattern + "' --location " + adlsv2_storage_location + " --account " + adlsv2_storage_account + " --container " + adlsv2_storage_container%}
  {% set test_cloud_storage_upload_params = "abfs upload -p /tmp/.test_cloud_storage_upload.txt --location " + adlsv2_storage_location + " --account " + adlsv2_storage_account + " --container " + adlsv2_storage_container%}
{% endif %}

{% set skip_validation = False %}
{% if salt['pillar.get']('filecollector:skipValidation') %}
    {% set skip_validation = True %}
{% endif %}

{% set proxy_full_url = None %}
{% set proxy_protocol = None %}
{% if salt['pillar.get']('proxy:host') %}
  {% set proxy_host = salt['pillar.get']('proxy:host') %}
  {% set proxy_port = salt['pillar.get']('proxy:port')|string %}
  {% set proxy_protocol = salt['pillar.get']('proxy:protocol') %}
  {% set proxy_url = proxy_protocol + "://" + proxy_host + ":" + proxy_port %}
  {% if salt['pillar.get']('proxy:user') and salt['pillar.get']('proxy:password') %}
    {% set proxy_user = salt['pillar.get']('proxy:user') %}
    {% set proxy_password = salt['pillar.get']('proxy:password') %}
    {% set proxy_full_url =  proxy_protocol + "://" + proxy_user + ":"+ proxy_password + "@" + proxy_host + ":" + proxy_port %}
  {% else %}
    {% set proxy_full_url = proxy_url %}
  {% endif %}
{% endif %}

{% if salt['pillar.get']('tags:Cloudera-Resource-Name') %}
   {% set resource_crn = salt['pillar.get']('tags:Cloudera-Resource-Name') %}
{% endif %}
{% if salt['pillar.get']('tags:Cloudera-Creator-Resource-Name') %}
   {% set creator_crn = salt['pillar.get']('tags:Cloudera-Creator-Resource-Name') %}
{% endif %}
{% if salt['pillar.get']('tags:Cloudera-Environment-Resource-Name') %}
   {% set environment_crn = salt['pillar.get']('tags:Cloudera-Environment-Resource-Name') %}
{% endif %}

{% if  salt['pillar.get']('filecollector:clusterType') %}
   {% set cluster_type = salt['pillar.get']('filecollector:clusterType') %}
{% else %}
   {% set cluster_type = '' %}
{% endif %}
{% if  salt['pillar.get']('filecollector:clusterVersion') %}
   {% set cluster_version = salt['pillar.get']('filecollector:clusterVersion') %}
{% else %}
   {% set cluster_version = '' %}
{% endif %}

{% set version_data = namespace(entities=[]) %}
{% for role in grains.get('roles', []) %}
{% if role.startswith("cdp_telemetry_prewarmed") %}
  {% set version_data.entities = version_data.entities + [role.split("cdp_telemetry_prewarmed_v")[1]]%}
{% endif %}
{% endfor %}
{% if version_data.entities|length > 0 %}
{% set cdp_telemetry_version = version_data.entities[0] | int %}
{% else %}
{% set cdp_telemetry_version = 0 %}
{% endif %}

{% do filecollector.update({
    "destination": destination,
    "cloudStorageUploadParams": cloud_storage_upload_params,
    "testCloudStorageUploadParams": test_cloud_storage_upload_params,
    "startTime": start_time,
    "endTime": end_time,
    "issue": issue,
    "description": description,
    "labelFilter": label_filter,
    "additionalLogs": additional_logs,
    "includeSaltLogs": include_salt_logs,
    "updatePackage": update_package,
    "skipValidation": skip_validation,
    "proxyUrl": proxy_full_url,
    "proxyProtocol": proxy_protocol,
    "mode": mode,
    "resourceCrn": resource_crn,
    "creatorCrn": creator_crn,
    "environmentCrn": environment_crn,
    "clusterType": cluster_type,
    "clusterVersion": cluster_version,
    "uuid": uuid,
    "accountId": account_id,
    "cdpTelemetryVersion": cdp_telemetry_version
}) %}