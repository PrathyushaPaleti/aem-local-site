package com.aem.local.site.core.config;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

public class DestinationRepoConfig {
    
    @ObjectClassDefinition(
        name = "AEM DAM Remote Fetcher Configuration",
        description = "Configuration properties to fetch raw assets from remote AEM instances"
    )
    public @interface DestinationRepoConfiguration {

        @AttributeDefinition(name = "Target Asset URL")
        String remote_asset_url() default "http://localhost:4504/content/dam/aem-local-site/product-json/destinationRepo.json";

        @AttributeDefinition(name = "AEM Username")
        String remote_username() default "";

        @AttributeDefinition(name = "AEM Password", type = AttributeType.PASSWORD)
        String remote_password() default "";

        @AttributeDefinition(name = "Connection Timeout (Seconds)")
        int timeout_seconds() default 10;
    }
}