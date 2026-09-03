package com.aem.local.site.core.servlets;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.aem.local.site.core.services.DestinationRepoService;


@Component(service = { Servlet.class })
@SlingServletResourceTypes(
    resourceTypes = "aem-local-site/components/page",
    methods = HttpConstants.METHOD_GET,  // Limit to GET requests
    extensions = "json",  // Responds to '.json' file extensions
    selectors = "destinationRoutes"  // Optional selector: triggers on '.route.json' 
)
public class DestinationRepoServlet extends SlingSafeMethodsServlet {

    @Reference
    private transient DestinationRepoService destinationRepoService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

         // request.getResource() points to the jcr:content node of the requested page
        Resource pageContentResource = request.getResource();
        
        // Internally navigate to the destinationcomponent node path based on your container structure
        Resource destinationComponent = pageContentResource.getChild("root/container/container/destinationcomponent");

        // Verify that the node exists AND matches your required resourceType
        if (destinationComponent == null || !destinationComponent.isResourceType("aem-local-site/components/destinationcomponent")) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            response.getWriter().write("{\"error\": \"Destination component not found or resource type does not match.\"}");
            return;
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