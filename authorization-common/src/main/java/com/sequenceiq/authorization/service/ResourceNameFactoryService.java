package com.sequenceiq.authorization.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.sequenceiq.cloudbreak.auth.altus.Crn;

@Service
public class ResourceNameFactoryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResourceNameFactoryService.class);

    @Inject
    private Optional<List<ResourceNameProvider>> resourceNameProviderList;

    public Optional<String> getName(String resourceCrn) {
        Crn crn = Crn.fromString(resourceCrn);
        return resourceNameProviderList.orElse((List<ResourceNameProvider>) Collections.EMPTY_LIST).stream()
                .filter(provider -> provider.getCrnType().contains(crn.getResourceType()))
                .findFirst()
                .flatMap(provider -> provider.getNameByCrn(resourceCrn));
    }
}
