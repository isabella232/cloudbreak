package com.sequenceiq.distrox.api.v1.distrox.model.instancegroup;

import java.util.Set;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.glassfish.jersey.internal.guava.Sets;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.sequenceiq.authorization.resource.AuthNameStringCollection;
import com.sequenceiq.authorization.resource.AuthorizationResourceAction;
import com.sequenceiq.cloudbreak.doc.ModelDescriptions.HostGroupModelDescription;
import com.sequenceiq.cloudbreak.doc.ModelDescriptions.InstanceGroupModelDescription;
import com.sequenceiq.distrox.api.v1.distrox.model.instancegroup.template.InstanceTemplateV1Request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(Include.NON_NULL)
public class InstanceGroupV1Request extends InstanceGroupV1Base {

    @Valid
    @NotNull
    @ApiModelProperty(InstanceGroupModelDescription.TEMPLATE)
    private InstanceTemplateV1Request template;

    @ApiModelProperty(HostGroupModelDescription.RECIPE_NAMES)
    private AuthNameStringCollection recipeNames =
            new AuthNameStringCollection(Sets.newHashSet(), AuthorizationResourceAction.DESCRIBE_RECIPE);

    public InstanceTemplateV1Request getTemplate() {
        return template;
    }

    public void setTemplate(InstanceTemplateV1Request template) {
        this.template = template;
    }

    public Set<String> getRecipeNames() {
        return (Set<String>) recipeNames.getValue();
    }

    public void setRecipeNames(Set<String> recipeNames) {
        this.recipeNames = new AuthNameStringCollection(recipeNames, AuthorizationResourceAction.DESCRIBE_RECIPE);
    }
}
