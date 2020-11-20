package com.sequenceiq.cloudbreak.core.bootstrap.service.container.postgres;

import static com.sequenceiq.cloudbreak.auth.ThreadBasedUserCrnProvider.INTERNAL_ACTOR_CRN;
import static java.util.Collections.singletonMap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.auth.ThreadBasedUserCrnProvider;
import com.sequenceiq.cloudbreak.auth.altus.EntitlementService;
import com.sequenceiq.cloudbreak.domain.RDSConfig;
import com.sequenceiq.cloudbreak.domain.Template;
import com.sequenceiq.cloudbreak.domain.stack.Stack;
import com.sequenceiq.cloudbreak.domain.stack.cluster.Cluster;
import com.sequenceiq.cloudbreak.orchestrator.model.SaltPillarProperties;
import com.sequenceiq.cloudbreak.service.rdsconfig.RdsConfigProviderFactory;
import com.sequenceiq.cloudbreak.service.rdsconfig.RedbeamsDbCertificateProvider;
import com.sequenceiq.cloudbreak.service.rdsconfig.RedbeamsDbServerConfigurer;
import com.sequenceiq.cloudbreak.template.VolumeUtils;

@Service
public class PostgresConfigService {

    @Inject
    private RdsConfigProviderFactory rdsConfigProviderFactory;

    @Inject
    private RedbeamsDbServerConfigurer dbServerConfigurer;

    @Inject
    private RedbeamsDbCertificateProvider dbCertificateProvider;

    @Inject
    private EntitlementService entitlementService;

    public void decorateServicePillarWithPostgresIfNeeded(Map<String, SaltPillarProperties> servicePillar, Stack stack, Cluster cluster) {
        Map<String, Object> postgresConfig = initPostgresConfig(stack, cluster);

        Set<String> rootCerts = dbCertificateProvider.getRelatedSslCerts(stack, cluster);
        if (CollectionUtils.isNotEmpty(rootCerts)) {
            Map<String, String> rootSslCertsMap = Map.of("ssl_certs", String.join("\n", rootCerts));
            servicePillar.put("postgres-common", new SaltPillarProperties("/postgresql/root-certs.sls",
                    singletonMap("postgres_root_certs", rootSslCertsMap)));
        }

        if (!postgresConfig.isEmpty()) {
            servicePillar.put("postgresql-server", new SaltPillarProperties("/postgresql/postgre.sls", singletonMap("postgres", postgresConfig)));
        }
    }

    public Set<RDSConfig> createRdsConfigIfNeeded(Stack stack, Cluster cluster) {
        return rdsConfigProviderFactory.getAllSupportedRdsConfigProviders().stream().map(provider ->
                provider.createPostgresRdsConfigIfNeeded(stack, cluster)).reduce((first, second) -> second).orElse(Collections.emptySet());
    }

    private Map<String, Object> initPostgresConfig(Stack stack, Cluster cluster) {
        Map<String, Object> postgresConfig = new HashMap<>();
        if (dbServerConfigurer.isRemoteDatabaseNeeded(cluster)) {
            postgresConfig.put("configure_remote_db", "true");
        } else {
            collectEmbeddedDatabaseConfigs(stack, postgresConfig);
        }
        rdsConfigProviderFactory.getAllSupportedRdsConfigProviders().forEach(provider ->
                postgresConfig.putAll(provider.createServicePillarConfigMapIfNeeded(stack, cluster)));
        return postgresConfig;
    }

    private void collectEmbeddedDatabaseConfigs(Stack stack, Map<String, Object> postgresConfig) {
        String databaseDirectory = "/var/lib/pgsql/data";
        boolean databaseDirectoryOnAttachedDisk = false;
        if (entitlementService.embeddedDatabaseOnAttachedDiskEnabled(INTERNAL_ACTOR_CRN, ThreadBasedUserCrnProvider.getAccountId())) {
            Template template = stack.getPrimaryGatewayInstance().getInstanceGroup().getTemplate();
            int volumeCount = template == null ? 1 : template.getVolumeTemplates().stream()
                    .mapToInt(volume -> volume.getVolumeCount()).sum();
            databaseDirectory = VolumeUtils.buildSingleVolumePath(volumeCount, "pgsql/data");
            databaseDirectoryOnAttachedDisk = true;
        }
        postgresConfig.put("postgres_data_directory", databaseDirectory);
        postgresConfig.put("postgres_data_on_attached_disk", databaseDirectoryOnAttachedDisk);
    }
}
