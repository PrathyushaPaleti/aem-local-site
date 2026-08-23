package com.aem.local.site.core.servlets;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletPaths;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.aem.local.site.core.services.PostService;

import javax.servlet.Servlet;
import java.io.IOException;

@Component(service = { Servlet.class })
@SlingServletPaths(
    value = "/bin/api/posts"
)
public class PostServlet extends SlingSafeMethodsServlet {

    @Reference
    private transient PostService postService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Grab the optional "id" query parameter from the request URL (e.g., /bin/api/posts?id=3)
        String postId = request.getParameter("id");

        try {
            // Fetch posts dynamically based on parameters
            String postsJson = postService.getPosts(postId);
            
            // Write JSON response directly back to the client
            response.getWriter().write(postsJson);
            
        } catch (IOException | InterruptedException e) {
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"status\": \"error\", \"message\": \"" + e.getMessage() + "\"}");
        }
    }
}