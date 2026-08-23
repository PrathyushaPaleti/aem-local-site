package com.aem.local.site.core.config;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

@ObjectClassDefinition(
    name = "JSONPlaceholder Posts API Configuration",
    description = "Externalized configurations for pulling posts from JSONPlaceholder"
)
public @interface PostConfig {

    @AttributeDefinition(
        name = "Posts API Base URL",
        description = "Base URL of the third-party posts endpoint"
    )
    String posts_api_url() default "https://jsonplaceholder.typicode.com/posts";

    @AttributeDefinition(
        name = "Request Timeout (Seconds)",
        description = "Timeout limit for making the HTTP connection and retrieving posts"
    )
    int request_timeout() default 30;
}