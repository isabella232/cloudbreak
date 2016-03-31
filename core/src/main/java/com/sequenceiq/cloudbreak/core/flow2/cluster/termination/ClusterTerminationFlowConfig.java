package com.sequenceiq.cloudbreak.core.flow2.cluster.termination;


import static com.sequenceiq.cloudbreak.core.flow2.cluster.termination.ClusterTerminationEvent.CLUSTER_TERMINATION_FAIL_HANDLED_EVENT;
import static com.sequenceiq.cloudbreak.core.flow2.cluster.termination.ClusterTerminationEvent.TERMINATION_EVENT;
import static com.sequenceiq.cloudbreak.core.flow2.cluster.termination.ClusterTerminationEvent.TERMINATION_FINALIZED_EVENT;
import static com.sequenceiq.cloudbreak.core.flow2.cluster.termination.ClusterTerminationEvent.TERMINATION_FINISHED_EVENT;
import static com.sequenceiq.cloudbreak.core.flow2.cluster.termination.ClusterTerminationState.FINAL_STATE;
import static com.sequenceiq.cloudbreak.core.flow2.cluster.termination.ClusterTerminationState.INIT_STATE;
import static com.sequenceiq.cloudbreak.core.flow2.cluster.termination.ClusterTerminationState.TERMINATION_FAILED_STATE;
import static com.sequenceiq.cloudbreak.core.flow2.cluster.termination.ClusterTerminationState.TERMINATION_FINISHED_STATE;
import static com.sequenceiq.cloudbreak.core.flow2.cluster.termination.ClusterTerminationState.TERMINATION_STATE;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.core.flow2.EventConverter;
import com.sequenceiq.cloudbreak.core.flow2.config.AbstractFlowConfiguration;

@Component
public class ClusterTerminationFlowConfig extends AbstractFlowConfiguration<ClusterTerminationState, ClusterTerminationEvent> {

    private static final List<Transition<ClusterTerminationState, ClusterTerminationEvent>> TRANSITIONS = Arrays.asList(
            new Transition<>(INIT_STATE, TERMINATION_STATE, TERMINATION_EVENT),
            new Transition<>(TERMINATION_STATE, TERMINATION_FINISHED_STATE, TERMINATION_FINISHED_EVENT)
    );
    private static final FlowEdgeConfig<ClusterTerminationState, ClusterTerminationEvent> EDGE_CONFIG =
            new FlowEdgeConfig<>(INIT_STATE, FINAL_STATE, TERMINATION_FINISHED_STATE, TERMINATION_FINALIZED_EVENT, TERMINATION_FAILED_STATE,
                    CLUSTER_TERMINATION_FAIL_HANDLED_EVENT);

    @Override
    protected EventConverter<ClusterTerminationEvent> getEventConverter() {
        return new ClusterTerminationEventConverter();
    }

    @Override
    protected List<Transition<ClusterTerminationState, ClusterTerminationEvent>> getTransitions() {
        return TRANSITIONS;
    }

    @Override
    protected FlowEdgeConfig<ClusterTerminationState, ClusterTerminationEvent> getEdgeConfig() {
        return EDGE_CONFIG;
    }

    @Override
    public List<ClusterTerminationEvent> getFlowTriggerEvents() {
        return Collections.singletonList(ClusterTerminationEvent.TERMINATION_EVENT);
    }

    @Override
    public ClusterTerminationEvent[] getEvents() {
        return ClusterTerminationEvent.values();
    }

}
