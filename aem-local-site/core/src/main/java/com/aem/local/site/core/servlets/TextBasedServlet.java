/*package com.aem.local.site.core.servlets;

import org.apache.sling.api.servlets.HttpServlet;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.api.servlets.SlingHttpServletRequest;
import org.apache.sling.api.servlets.SlingHttpServletResponse;
import org.osgi.service.component.annotations.Component;
import javax.servlet.Servlet;
import java.io.IOException;

@Component(
    service = Servlet.class,
    property = {
        "sling.servlet.paths=/bin/helloworld",
        "sling.servlet.methods=GET"
    }
)
public class TextBasedServlet extends SlingAllMethodsServlet {
    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        response.setContentType("text/plain");
        response.getWriter().write("Hello, World!");
    }
}*/
