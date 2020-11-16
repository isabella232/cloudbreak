package com.sequenceiq.authorization.service;

import java.util.EnumSet;
import java.util.Optional;

import com.sequenceiq.cloudbreak.auth.altus.Crn;

public interface ResourceNameProvider {
    Optional<String> getNameByCrn(String crn);

    EnumSet<Crn.ResourceType> getCrnType();
}
