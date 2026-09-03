package com.aem.local.site.core.services.impl;

import java.io.IOException;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.Designate;

import com.aem.local.site.core.config.DestinationRepoConfig;
import com.aem.local.site.core.services.DestinationRepoService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Modified;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;

@Component(service = DestinationRepoService.class, immediate = true)
@Designate(ocd = DestinationRepoConfig.DestinationRepoConfiguration.class)
public class DestinationRepoServiceImpl implements DestinationRepoService {


    private static final Logger log = LoggerFactory.getLogger(DestinationRepoServiceImpl.class);

    private HttpClient httpClient;
    private String assetUrl;
    private String authHeaderValue;
    private int timeoutSeconds;

    @Activate
    @Modified
    protected void activate(DestinationRepoConfig.DestinationRepoConfiguration config) {
        this.assetUrl = config.remote_asset_url();
        this.timeoutSeconds = config.timeout_seconds();

        // Generate Basic Auth token (admin:admin -> Basic YWRtaW46YWRtaW4=) [baeldung.com]
        String credentials = config.remote_username() + ":" + config.remote_password();
        this.authHeaderValue = "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes());

        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(timeoutSeconds))
                .build();
    }

    @Override
    public String fetchDestinationRepoJson() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(assetUrl))
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .header("Authorization", authHeaderValue) // Authorize credentials via HTTP header [baeldung.com]
                .header("Accept", "application/json")
                .GET()
                .build();

        log.debug("Calling remote AEM DAM JSON at: {}", assetUrl);
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            return response.body();
        } else {
            log.error("Failed to fetch remote asset. HTTP Status Code: {}", response.statusCode());
            throw new IOException("Remote instance returned status code: " + response.statusCode());
        }
    }
    
}