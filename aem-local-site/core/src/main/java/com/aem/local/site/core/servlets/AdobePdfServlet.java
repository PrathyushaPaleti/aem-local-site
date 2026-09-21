package com.aem.local.site.core.servlets;

import com.aem.local.site.core.services.AdobePdfService;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.servlet.Servlet;
import java.io.IOException;

@Component(
        service = Servlet.class,
        property = {
            "sling.servlet.paths=/bin/mfs/generate-pdf",
            "sling.servlet.methods=" + HttpConstants.METHOD_GET,
            "sling.servlet.methods=" + HttpConstants.METHOD_POST
        }
)
public class AdobePdfServlet extends SlingAllMethodsServlet {

    @Reference
    private AdobePdfService adobePdfService;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {
        response.setStatus(SlingHttpServletResponse.SC_OK);
        response.setContentType("text/plain");
        response.setHeader("Cache-Control", "no-store");
        response.getWriter().write("PDF generation endpoint is available. Send a POST request to generate a PDF.");
    }

    @Override
    protected void doPost(SlingHttpServletRequest request,SlingHttpServletResponse response)throws IOException {

        try {

            String fileName = request.getParameter("fileName");

            if (fileName == null || fileName.trim().isEmpty()) {
                fileName = "generated-page.pdf";
            }

            if (!fileName.endsWith(".pdf")) {
                fileName += ".pdf";
            }

            String pdfPath = adobePdfService.convertHtmlZipToPdf(fileName);

            response.setStatus(SlingHttpServletResponse.SC_OK);

            response.setContentType("text/plain");

            response.getWriter().write("PDF generated successfully:\n"+ pdfPath);

        } catch (Exception e) {

            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);

            response.setContentType("text/plain");

            response.getWriter().write("PDF generation failed: " + e.getMessage());
        }
    }
}