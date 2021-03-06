package com.sequenceiq.authorization.service;

import static com.sequenceiq.authorization.utils.AuthorizationMessageUtils.INSUFFICIENT_RIGHTS;
import static com.sequenceiq.authorization.utils.AuthorizationMessageUtils.INSUFFICIENT_RIGHTS_TEMPLATE;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.security.access.AccessDeniedException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sequenceiq.authorization.resource.AuthorizationResourceAction;
import com.sequenceiq.cloudbreak.auth.ThreadBasedUserCrnProvider;
import com.sequenceiq.cloudbreak.auth.altus.Crn;
import com.sequenceiq.cloudbreak.auth.altus.EntitlementService;
import com.sequenceiq.cloudbreak.auth.altus.GrpcUmsClient;

@RunWith(MockitoJUnitRunner.class)
public class UmsResourceAuthorizationServiceTest {

    private static final String USER_ID = "userId";

    private static final String USER_CRN = "crn:cdp:iam:us-west-1:1234:user:" + USER_ID;

    private static final String RESOURCE_CRN = "crn:cdp:datalake:us-west-1:1234:environment:1";

    private static final String RESOURCE_CRN2 = "crn:cdp:datalake:us-west-1:1234:environment:2";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Mock
    private GrpcUmsClient umsClient;

    @Mock
    private UmsRightProvider umsRightProvider;

    @Mock
    private EntitlementService entitlementService;

    @InjectMocks
    private UmsResourceAuthorizationService underTest;

    @Before
    public void init() {
        when(umsRightProvider.getRight(any())).thenAnswer(invocation -> {
            AuthorizationResourceAction action = invocation.getArgument(0);
            return action.getRight();
        });
        when(entitlementService.isAuthorizationEntitlementRegistered(anyString(), anyString())).thenReturn(TRUE);
    }

    @Test
    public void testCheckRightOnResource() {
        when(umsClient.checkRight(anyString(), anyString(), anyString(), anyString(), any())).thenReturn(false);

        thrown.expect(AccessDeniedException.class);
        thrown.expectMessage(INSUFFICIENT_RIGHTS);
        thrown.expectMessage(formatTemplate("environments/describeEnvironment", RESOURCE_CRN));

        ThreadBasedUserCrnProvider.doAs(USER_CRN, () -> underTest.
                checkRightOfUserOnResource(USER_CRN, AuthorizationResourceAction.DESCRIBE_ENVIRONMENT, RESOURCE_CRN));
    }

    @Test
    public void testCheckRightOnResourcesFailure() {
        when(umsClient.hasRights(anyString(), anyString(), anyList(), anyString(), any())).thenReturn(hasRightsResultMap());

        thrown.expect(AccessDeniedException.class);
        thrown.expectMessage(INSUFFICIENT_RIGHTS);
        thrown.expectMessage(formatTemplate("environments/describeEnvironment", RESOURCE_CRN));
        thrown.expectMessage(formatTemplate("environments/describeEnvironment", RESOURCE_CRN2));

        ThreadBasedUserCrnProvider.doAs(USER_CRN, () -> underTest.
                checkRightOfUserOnResources(USER_CRN, AuthorizationResourceAction.DESCRIBE_ENVIRONMENT, Lists.newArrayList(RESOURCE_CRN, RESOURCE_CRN2)));
    }

    @Test
    public void testCheckRightOnResources() {
        Map<String, Boolean> resultMap = hasRightsResultMap();
        resultMap.put(RESOURCE_CRN2, TRUE);
        when(umsClient.hasRights(anyString(), anyString(), anyList(), anyString(), any())).thenReturn(resultMap);

        ThreadBasedUserCrnProvider.doAs(USER_CRN, () -> underTest.
                checkRightOfUserOnResources(USER_CRN, AuthorizationResourceAction.DESCRIBE_ENVIRONMENT, Lists.newArrayList(RESOURCE_CRN, RESOURCE_CRN2)));
    }

    private Map<String, Boolean> hasRightsResultMap() {
        Map<String, Boolean> result = Maps.newHashMap();
        result.put(RESOURCE_CRN, TRUE);
        result.put(RESOURCE_CRN2, FALSE);
        return result;
    }

    @Test
    public void testCheckIfUserHasAtLeastOneRigthFailure() {
        when(umsClient.hasRights(anyString(), anyString(), anyList(), any())).thenReturn(List.of(FALSE, FALSE));

        thrown.expect(AccessDeniedException.class);
        thrown.expectMessage(INSUFFICIENT_RIGHTS);
        thrown.expectMessage(formatTemplate("environments/describeEnvironment", RESOURCE_CRN));
        thrown.expectMessage(formatTemplate("environments/accessEnvironment", RESOURCE_CRN2));

        ThreadBasedUserCrnProvider.doAs(USER_CRN, () -> underTest.
                checkIfUserHasAtLeastOneRight(USER_CRN, Map.of(RESOURCE_CRN, AuthorizationResourceAction.DESCRIBE_ENVIRONMENT,
                        RESOURCE_CRN2, AuthorizationResourceAction.ACCESS_ENVIRONMENT)));
    }

    private String formatTemplate(String right, String resourceCrn) {
        return String.format(INSUFFICIENT_RIGHTS_TEMPLATE, right, Crn.fromString(resourceCrn).getResourceType().getName(), resourceCrn);
    }
}
