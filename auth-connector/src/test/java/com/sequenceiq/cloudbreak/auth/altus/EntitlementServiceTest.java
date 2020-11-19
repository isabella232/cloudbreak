package com.sequenceiq.cloudbreak.auth.altus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.Account;
import com.cloudera.thunderhead.service.usermanagement.UserManagementProto.Entitlement;

@ExtendWith(MockitoExtension.class)
class EntitlementServiceTest {

    private static final String ACCOUNT_ID = UUID.randomUUID().toString();

    private static final String ACTOR_CRN = "crn:cdp:iam:us-west-1:" + ACCOUNT_ID + ":user:" + UUID.randomUUID();

    private static final Account ACCOUNT_NO_ENTITLEMENTS = Account.newBuilder().build();

    private static final String ENTITLEMENT_FOO = "FOO";

    private static final String ENTITLEMENT_BAR = "BAR";

    private static final Account ACCOUNT_ENTITLEMENTS_FOO_BAR = createAccountForEntitlements(ENTITLEMENT_FOO, ENTITLEMENT_BAR);

    @Mock
    private GrpcUmsClient umsClient;

    @InjectMocks
    private EntitlementService underTest;

    static Object[][] entitlementCheckDataProvider() {
        return new Object[][]{

                // entitlementName, function, enabled
                {"CDP_AZURE", (EntitlementCheckFunction) EntitlementService::azureEnabled, false},
                {"CDP_AZURE", (EntitlementCheckFunction) EntitlementService::azureEnabled, true},

                {"CDP_GCP", (EntitlementCheckFunction) EntitlementService::gcpEnabled, false},
                {"CDP_GCP", (EntitlementCheckFunction) EntitlementService::gcpEnabled, true},

                {"CDP_BASE_IMAGE", (EntitlementCheckFunction) EntitlementService::baseImageEnabled, false},
                {"CDP_BASE_IMAGE", (EntitlementCheckFunction) EntitlementService::baseImageEnabled, true},

                {"CDP_AUTOMATIC_USERSYNC_POLLER", (EntitlementCheckFunction) EntitlementService::automaticUsersyncPollerEnabled, false},
                {"CDP_AUTOMATIC_USERSYNC_POLLER", (EntitlementCheckFunction) EntitlementService::automaticUsersyncPollerEnabled, true},

                {"CDP_FREEIPA_HA", (EntitlementCheckFunction) EntitlementService::freeIpaHaEnabled, false},
                {"CDP_FREEIPA_HA", (EntitlementCheckFunction) EntitlementService::freeIpaHaEnabled, true},

                {"CDP_FREEIPA_HA_REPAIR", (EntitlementCheckFunction) EntitlementService::freeIpaHaRepairEnabled, false},
                {"CDP_FREEIPA_HA_REPAIR", (EntitlementCheckFunction) EntitlementService::freeIpaHaRepairEnabled, true},

                {"CDP_FREEIPA_HEALTH_CHECK", (EntitlementCheckFunction) EntitlementService::freeIpaHealthCheckEnabled, false},
                {"CDP_FREEIPA_HEALTH_CHECK", (EntitlementCheckFunction) EntitlementService::freeIpaHealthCheckEnabled, true},

                {"CLOUDERA_INTERNAL_ACCOUNT", (EntitlementCheckFunction) EntitlementService::internalTenant, false},
                {"CLOUDERA_INTERNAL_ACCOUNT", (EntitlementCheckFunction) EntitlementService::internalTenant, true},

                {"CDP_FMS_CLUSTER_PROXY", (EntitlementCheckFunction) EntitlementService::fmsClusterProxyEnabled, false},
                {"CDP_FMS_CLUSTER_PROXY", (EntitlementCheckFunction) EntitlementService::fmsClusterProxyEnabled, true},

                {"CDP_CLOUD_STORAGE_VALIDATION", (EntitlementCheckFunction) EntitlementService::cloudStorageValidationEnabled, false},
                {"CDP_CLOUD_STORAGE_VALIDATION", (EntitlementCheckFunction) EntitlementService::cloudStorageValidationEnabled, true},

                {"CDP_RAZ", (EntitlementCheckFunction) EntitlementService::razEnabled, false},
                {"CDP_RAZ", (EntitlementCheckFunction) EntitlementService::razEnabled, true},

                {"CDP_RUNTIME_UPGRADE", (EntitlementCheckFunction) EntitlementService::runtimeUpgradeEnabled, false},
                {"CDP_RUNTIME_UPGRADE", (EntitlementCheckFunction) EntitlementService::runtimeUpgradeEnabled, true},

                {"CDP_FREEIPA_DL_EBS_ENCRYPTION", (EntitlementCheckFunction) EntitlementService::freeIpaDlEbsEncryptionEnabled, false},
                {"CDP_FREEIPA_DL_EBS_ENCRYPTION", (EntitlementCheckFunction) EntitlementService::freeIpaDlEbsEncryptionEnabled, true},

                {"LOCAL_DEV", (EntitlementCheckFunction) EntitlementService::localDevelopment, false},
                {"LOCAL_DEV", (EntitlementCheckFunction) EntitlementService::localDevelopment, true},

                {"CDP_AZURE_SINGLE_RESOURCE_GROUP", (EntitlementCheckFunction) EntitlementService::azureSingleResourceGroupDeploymentEnabled, false},
                {"CDP_AZURE_SINGLE_RESOURCE_GROUP", (EntitlementCheckFunction) EntitlementService::azureSingleResourceGroupDeploymentEnabled, true},

                {"CDP_CB_FAST_EBS_ENCRYPTION", (EntitlementCheckFunction) EntitlementService::fastEbsEncryptionEnabled, false},
                {"CDP_CB_FAST_EBS_ENCRYPTION", (EntitlementCheckFunction) EntitlementService::fastEbsEncryptionEnabled, true},

                {"CDP_CLOUD_IDENTITY_MAPPING", (EntitlementCheckFunction) EntitlementService::cloudIdentityMappingEnabled, false},
                {"CDP_CLOUD_IDENTITY_MAPPING", (EntitlementCheckFunction) EntitlementService::cloudIdentityMappingEnabled, true},

                {"CDP_ALLOW_INTERNAL_REPOSITORY_FOR_UPGRADE", (EntitlementCheckFunction) EntitlementService::isInternalRepositoryForUpgradeAllowed, true},
                {"CDP_ALLOW_INTERNAL_REPOSITORY_FOR_UPGRADE", (EntitlementCheckFunction) EntitlementService::isInternalRepositoryForUpgradeAllowed, false},

                {"CDP_UMS_USER_SYNC_MODEL_GENERATION", (EntitlementCheckFunction) EntitlementService::umsUserSyncModelGenerationEnabled, false},
                {"CDP_UMS_USER_SYNC_MODEL_GENERATION", (EntitlementCheckFunction) EntitlementService::umsUserSyncModelGenerationEnabled, true},

                {"CDP_SDX_HBASE_CLOUD_STORAGE", (EntitlementCheckFunction) EntitlementService::sdxHbaseCloudStorageEnabled, false},
                {"CDP_SDX_HBASE_CLOUD_STORAGE", (EntitlementCheckFunction) EntitlementService::sdxHbaseCloudStorageEnabled, true},

                {"CDP_ALLOW_DIFFERENT_DATAHUB_VERSION_THAN_DATALAKE",
                        (EntitlementCheckFunction) EntitlementService::isDifferentDataHubAndDataLakeVersionAllowed, false},
                {"CDP_ALLOW_DIFFERENT_DATAHUB_VERSION_THAN_DATALAKE",
                        (EntitlementCheckFunction) EntitlementService::isDifferentDataHubAndDataLakeVersionAllowed, true},

                {"CDP_AZURE_SINGLE_RESOURCE_GROUP_DEDICATED_STORAGE_ACCOUNT",
                        (EntitlementCheckFunction) EntitlementService::azureSingleResourceGroupDedicatedStorageAccountEnabled, false},
                {"CDP_AZURE_SINGLE_RESOURCE_GROUP_DEDICATED_STORAGE_ACCOUNT",
                        (EntitlementCheckFunction) EntitlementService::azureSingleResourceGroupDedicatedStorageAccountEnabled, true},

                {"DATAHUB_AWS_AUTOSCALING", (EntitlementCheckFunction) EntitlementService::awsAutoScalingEnabled, false},
                {"DATAHUB_AWS_AUTOSCALING", (EntitlementCheckFunction) EntitlementService::awsAutoScalingEnabled, true},

                {"DATAHUB_AZURE_AUTOSCALING", (EntitlementCheckFunction) EntitlementService::azureAutoScalingEnabled, false},
                {"DATAHUB_AZURE_AUTOSCALING", (EntitlementCheckFunction) EntitlementService::azureAutoScalingEnabled, true},

                {"CDP_CB_DATABASE_WIRE_ENCRYPTION", (EntitlementCheckFunction) EntitlementService::databaseWireEncryptionEnabled, false},
                {"CDP_CB_DATABASE_WIRE_ENCRYPTION", (EntitlementCheckFunction) EntitlementService::databaseWireEncryptionEnabled, true},

                {"CDP_DL_LOAD_BALANCER", (EntitlementCheckFunction) EntitlementService::datalakeLoadBalancerEnabled, false},
                {"CDP_DL_LOAD_BALANCER", (EntitlementCheckFunction) EntitlementService::datalakeLoadBalancerEnabled, true}
        };
    }

    @ParameterizedTest(name = "{0} == {2}")
    @MethodSource("entitlementCheckDataProvider")
    void entitlementEnabledTestWhenNoOtherEntitlementsAreGranted(String entitlementName, EntitlementCheckFunction function, boolean enabled) {
        setUpUmsClient(entitlementName, enabled);
        assertThat(function.entitlementEnabled(underTest, ACTOR_CRN, ACCOUNT_ID)).isEqualTo(enabled);
    }

    @Test
    void getEntitlementsTest() {
        when(umsClient.getAccountDetails(eq(ACTOR_CRN), eq(ACCOUNT_ID), any(Optional.class))).thenReturn(ACCOUNT_ENTITLEMENTS_FOO_BAR);
        assertThat(underTest.getEntitlements(ACTOR_CRN, ACCOUNT_ID)).containsExactly(ENTITLEMENT_FOO, ENTITLEMENT_BAR);
    }

    @SuppressWarnings("unchecked")
    private void setUpUmsClient(String entitlement, boolean entitled) {
        when(umsClient.isEntitled(eq(ACTOR_CRN), eq(ACCOUNT_ID), eq(com.sequenceiq.cloudbreak.auth.altus.model.Entitlement.valueOf(entitlement))))
                .thenReturn(entitled);
    }

    private static Account createAccountForEntitlements(String... entitlementNames) {
        // Protobuf wrappers are all finals, so cannot be mocked
        Account.Builder builder = Account.newBuilder();
        Arrays.stream(entitlementNames).forEach(entitlementName -> builder.addEntitlements(createEntitlement(entitlementName)));
        return builder.build();
    }

    private static Entitlement createEntitlement(String entitlementName) {
        return Entitlement.newBuilder().setEntitlementName(entitlementName).build();
    }

    @FunctionalInterface
    private interface EntitlementCheckFunction {
        boolean entitlementEnabled(EntitlementService service, String actorCrn, String accountId);
    }

}