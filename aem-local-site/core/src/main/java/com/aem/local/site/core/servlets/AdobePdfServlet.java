package com.aem.local.site.core.servlets;

import com.aem.local.site.core.services.AdobePdfService;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Servlet;
import java.io.IOException;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/mfs/generate-pdf",
                "sling.servlet.methods=GET",
                "sling.servlet.methods=POST"
        }
)
public class AdobePdfServlet extends SlingAllMethodsServlet {

     private static final Logger LOG = LoggerFactory.getLogger(AdobePdfServlet.class);

    @Reference
    private AdobePdfService adobePdfService;

    @Override
    protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws IOException {

        try {
            String htmlUrl = request.getParameter("url");

            LOG.info("HTML URL received: {}", htmlUrl);

            if (htmlUrl == null || htmlUrl.trim().isEmpty()) {
                response.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("text/plain");
                response.getWriter().write("Missing required parameter: url");
                return;
            }
            /*
             * Get PDF file name.
             */
            String fileName = request.getParameter("fileName");

            if (fileName == null || fileName.trim().isEmpty()) {
                response.setStatus(SlingHttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("text/plain");
                response.getWriter().write("Missing required parameter: fileName");
                return;
            }

            if (!fileName.toLowerCase().endsWith(".pdf")) {
                fileName += ".pdf";
            }

            /*
             * Generate PDF and save to DAM.
             */
            String pdfPath = adobePdfService.convertHtmlUrlToPdf(htmlUrl, fileName);

            response.setStatus(SlingHttpServletResponse.SC_OK);
            response.setContentType("text/plain");
            response.getWriter().write("PDF generated successfully:\n" + pdfPath);

        } catch (Exception e) {
            response.setStatus(SlingHttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/plain");
            response.getWriter().write("PDF generation failed: " + e.getMessage());
        }
    }
}