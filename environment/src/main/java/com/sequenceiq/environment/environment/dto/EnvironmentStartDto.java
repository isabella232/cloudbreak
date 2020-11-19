package com.sequenceiq.environment.environment.dto;

import com.sequenceiq.cloudbreak.common.event.Payload;
import com.sequenceiq.common.api.telemetry.model.DataHubStartAction;

public class EnvironmentStartDto implements Payload {

    private Long id;

    private EnvironmentDto environmentDto;

    private DataHubStartAction dataHubStartAction;

    @Override
    public Long getResourceId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EnvironmentDto getEnvironmentDto() {
        return environmentDto;
    }

    public void setEnvironmentDto(EnvironmentDto environmentDto) {
        this.environmentDto = environmentDto;
    }

    public DataHubStartAction getDataHubStart() {
        return dataHubStartAction;
    }

    public void setDataHubStart(DataHubStartAction dataHubStart) {
        this.dataHubStartAction = dataHubStartAction;
    }

    @Override
    public String toString() {
        return "EnvironmentDto{"
                + "environmentDto='" + environmentDto + '\''
                + ", dataHubStartAction='" + dataHubStartAction + '\''
                + '}';
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private Long id;

        private EnvironmentDto environmentDto;

        private DataHubStartAction dataHubStartAction;

        private Builder() {
        }

        public Builder withId(Long id) {
            this.id = id;
            return this;
        }

        public Builder withEnvironmentDto(EnvironmentDto environmentDto) {
            this.environmentDto = environmentDto;
            return this;
        }

        public Builder withDataHubStart(DataHubStartAction dataHubStartAction) {
            this.dataHubStartAction = dataHubStartAction;
            return this;
        }

        public EnvironmentStartDto build() {
            EnvironmentStartDto environmentDeletionDto = new EnvironmentStartDto();
            environmentDeletionDto.setEnvironmentDto(environmentDto);
            environmentDeletionDto.setId(id);
            environmentDeletionDto.setDataHubStart(dataHubStartAction);
            return environmentDeletionDto;
        }
    }
}
