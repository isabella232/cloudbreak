package com.sequenceiq.cloudbreak.reactor.handler.cluster;

import java.time.Duration;
import java.time.Instant;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.cluster.service.ClusterClientInitException;
import com.sequenceiq.cloudbreak.common.event.Selectable;
import com.sequenceiq.cloudbreak.core.cluster.ClusterBuilderService;
import com.sequenceiq.flow.event.EventSelectorUtil;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.InstallClusterFailed;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.InstallClusterRequest;
import com.sequenceiq.cloudbreak.reactor.api.event.cluster.InstallClusterSuccess;
import com.sequenceiq.flow.reactor.api.handler.EventHandler;
import com.sequenceiq.cloudbreak.service.CloudbreakException;

import reactor.bus.Event;
import reactor.bus.EventBus;

@Component
public class InstallClusterHandler implements EventHandler<InstallClusterRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstallClusterHandler.class);

    @Inject
    private EventBus eventBus;

    @Inject
    private ClusterBuilderService clusterBuilderService;

    @Override
    public String selector() {
        return EventSelectorUtil.selector(InstallClusterRequest.class);
    }

    @Override
    public void accept(Event<InstallClusterRequest> event) {
        LOGGER.debug("InstallClusterHandler for {}", event.getData().getResourceId());
        Instant start = Instant.now();
        Long stackId = event.getData().getResourceId();
        Selectable response;
        try {
            clusterBuilderService.buildCluster(stackId);
            response = new InstallClusterSuccess(stackId);
        } catch (RuntimeException | ClusterClientInitException | CloudbreakException e) {
            LOGGER.error("Build cluster failed", e);
            response = new InstallClusterFailed(stackId, e);
        } finally {
            LOGGER.debug("InstallClusterHandler for {} finished in {}ms", event.getData().getResourceId(), Duration.between(start, Instant.now()).toMillis());
        }
        eventBus.notify(response.selector(), new Event<>(event.getHeaders(), response));
    }
}
