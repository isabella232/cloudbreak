package com.sequenceiq.cloudbreak.cloud.event.loadbalancer;

import java.util.List;

import com.sequenceiq.cloudbreak.cloud.context.CloudContext;
import com.sequenceiq.cloudbreak.cloud.event.CloudPlatformRequest;
import com.sequenceiq.cloudbreak.cloud.model.CloudCredential;

public class CollectLoadBalancerMetadataRequest extends CloudPlatformRequest<CollectLoadBalancerMetadataResult> {

    private final List<String> gatewayGroupNames;

    public CollectLoadBalancerMetadataRequest(CloudContext cloudContext, CloudCredential cloudCredential, List<String> gatewayGroupNames) {
        super(cloudContext, cloudCredential);
        this.gatewayGroupNames = gatewayGroupNames;
    }

    public List<String> getGatewayGroupNames() {
        return gatewayGroupNames;
    }
}
