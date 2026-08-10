package com.aem.local.site.core.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.ChildResource;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)

public class SmallTeaser {

    @ValueMapValue
    private String pretitle;

    @ChildResource
    private Resource teaseritems;

    public Resource getTeaseritems() {
        return teaseritems;
    }

    public String getPretitle() {
        return pretitle;
    }


}
