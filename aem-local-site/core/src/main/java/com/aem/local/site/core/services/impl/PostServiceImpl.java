package com.aem.local.site.core.services.impl;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aem.local.site.core.config.PostConfig;
import com.aem.local.site.core.services.PostService;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

@Component(service = PostService.class, immediate = true)
@Designate(ocd = PostConfig.class)
public class PostServiceImpl implements PostService {

    private static final Logger log = LoggerFactory.getLogger(PostServiceImpl.class);

    private HttpClient httpClient;
    private String postsApiUrl;
    private int timeoutSeconds;

    @Activate
    protected void activate(PostConfig config) {
        this.postsApiUrl = config.posts_api_url();
        this.timeoutSeconds = config.request_timeout();

        HttpClient.Builder clientBuilder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds));

        this.httpClient = clientBuilder.build();
    }

    @Override
    public String getPosts(String postId) throws IOException, InterruptedException {
        // Construct the target URL. If postId is present, query that specific post (e.g. /posts/5)
        String targetUrl = this.postsApiUrl;
        if (postId != null && !postId.trim().isEmpty()) {
            targetUrl = targetUrl + "/" + postId.trim();
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(targetUrl))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .header("Accept", "application/json")
                .header("User-Agent", "AEM-Posts-Proxy-Service")
                .GET()
                .build();

        log.debug("Calling external Posts API: {}", targetUrl);
        HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            log.error("Failed to fetch posts from {}. HTTP Status: {}", targetUrl, response.statusCode());
            throw new IOException("JSONPlaceholder returned status code: " + response.statusCode());
        }
    }
}