{%- from 'metadata/settings.sls' import metadata with context %}

{% set configure_remote_db = salt['pillar.get']('postgres:configure_remote_db', 'None') %}
{% set postgres_data_directory = salt['pillar.get']('postgres:postgres_data_directory') %}
{% set postgres_data_on_attached_disk = salt['pillar.get']('postgres:postgres_data_on_attached_disk', 'None') %}

{% if 'None' != configure_remote_db %}

/opt/salt/scripts/init_db_remote.sh:
  file.managed:
    - makedirs: True
    - user: root
    - group: postgres
    - mode: 750
    - source: salt://postgresql/scripts/init_db_remote.sh
    - template: jinja

init-services-db-remote:
  cmd.run:
    - name: runuser -l postgres -c '/opt/salt/scripts/init_db_remote.sh' && echo $(date +%Y-%m-%d:%H:%M:%S) >> /var/log/init-services-db-remote-executed
    - unless: test -f /var/log/init-services-db-remote-executed
    - require:
      - file: /opt/salt/scripts/init_db_remote.sh

{%- else %}

init-db-with-utf8:
  cmd.run:
    - name: rm -rf {{ postgres_data_directory }} && runuser -l postgres sh -c 'initdb --locale=en_US.UTF-8 {{ postgres_data_directory }} > /var/lib/pgsql/initdb.log' && rm /var/log/pgsql_listen_address_configured
    - unless: grep -q UTF-8 /var/lib/pgsql/initdb.log

{%- if postgres_data_on_attached_disk %}

change-db-location:
  file.replace:
    - name: /usr/lib/systemd/system/postgresql-10.service
    - pattern: "Environment=PGDATA=.*"
    - repl: Environment=PGDATA=/hadoopfs/fs1/pgsql/data
    - unless: grep "Environment=PGDATA={{ postgres_data_directory }}" /usr/lib/systemd/system/postgresql-10.service

{%- endif %}

start-postgresql:
  service.running:
    - enable: True
    - require:
      - cmd: init-db-with-utf8
{%- if postgres_data_on_attached_disk %}
    - watch:
      - file: /usr/lib/systemd/system/postgresql-10.service
{%- endif %}
    - name: postgresql

/opt/salt/scripts/conf_pgsql_listen_address.sh:
  file.managed:
    - makedirs: True
    - user: root
    - group: postgres
    - mode: 750
    - source: salt://postgresql/scripts/conf_pgsql_listen_address.sh

configure-listen-address:
  cmd.run:
    - name: runuser -l postgres -c '/opt/salt/scripts/conf_pgsql_listen_address.sh' && echo $(date +%Y-%m-%d:%H:%M:%S) >> /var/log/pgsql_listen_address_configured
    - require:
      - file: /opt/salt/scripts/conf_pgsql_listen_address.sh
      - service: start-postgresql
    - unless: test -f /var/log/pgsql_listen_address_configured

/opt/salt/scripts/conf_pgsql_max_connections.sh:
  file.managed:
    - makedirs: True
    - user: root
    - group: postgres
    - mode: 750
    - source: salt://postgresql/scripts/conf_pgsql_max_connections.sh

configure-max-connections:
  cmd.run:
    - name: runuser -l postgres -c '/opt/salt/scripts/conf_pgsql_max_connections.sh' && echo $(date +%Y-%m-%d:%H:%M:%S) >> /var/log/pgsql_max_connections_configured
    - require:
      - file: /opt/salt/scripts/conf_pgsql_max_connections.sh
      - service: start-postgresql
    - unless: test -f /var/log/pgsql_max_connections_configured

/opt/salt/scripts/init_db.sh:
  file.managed:
    - makedirs: True
    - require:
      - cmd: configure-listen-address
      - cmd: configure-max-connections
    - mode: 750
    - user: root
    - group: postgres
    - source: salt://postgresql/scripts/init_db.sh
    - template: jinja

init-services-db:
  cmd.run:
    - name: runuser -l postgres -c '/opt/salt/scripts/init_db.sh' && echo $(date +%Y-%m-%d:%H:%M:%S) >> /var/log/init-services-db-executed
    - unless: test -f /var/log/init-services-db-executed
    - require:
      - file: /opt/salt/scripts/init_db.sh
      - cmd: configure-listen-address
      - cmd: configure-max-connections

restart-pgsql-if-reconfigured:
  service.running:
    - name: postgresql
    - watch:
      - cmd: configure-listen-address
      - cmd: configure-max-connections
      - cmd: init-services-db

{% endif %}
