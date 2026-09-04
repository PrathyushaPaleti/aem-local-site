package com.aem.local.site.core.servlets;

import java.io.IOException;

import javax.servlet.Servlet;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.aem.local.site.core.services.DestinationRepoService;

/*http://localhost:4504/content/aem-local-site/us/en/products/product-page/_jcr_content.destinationRoutes.json */
@Component(service = { Servlet.class })
@SlingServletResourceTypes(
    resourceTypes = "aem-local-site/components/page",  // Target resource type for this servlet
    methods = HttpConstants.METHOD_GET,  // Limit to GET requests
    extensions = "json",  // Responds to '.json' file extensions
    selectors = "destinationRoutes"  // Optional selector: triggers on '.route.json' 
)
public class DestinationRepoServlet extends SlingSafeMethodsServlet {

    private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(DestinationRepoServlet.class);

    private static final String COMPONENT_RELATIVE_PATH = "jcr:content/root/container/container/destinationcomponent";

    @Reference
    private transient DestinationRepoService destinationRepoService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Resource pageResource = request.getResource();
        if (pageResource == null) {
            response.setStatus(SlingHttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"error\": \"Page resource not found.\"}");
            return;
        }

        // 3. Directly target the hardcoded component path relative to the page resource
        Resource destinationCompResource = pageResource.getChild(COMPONENT_RELATIVE_PATH);

        if (destinationCompResource != null) {
            LOGGER.info("Successfully resolved destination component at: {}", destinationCompResource.getPath());
        } else {
            LOGGER.warn("Expected component node not found at relative path: '{}'", COMPONENT_RELATIVE_PATH);
        }
        
        try {
            // Fetch the JSON payload from the remote DAM on port 4504
            String remoteJsonData = destinationRepoService.fetchDestinationRepoJson();
            
            // Output raw JSON data directly to the client browser/caller
            response.getWriter().write(remoteJsonData);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // Restore interrupted status
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Fetching thread was interrupted.\"}");
        } catch (IOException e) {
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"error\": \"Could not retrieve remote asset: " + e.getMessage() + "\"}");
        }
    }
    
}