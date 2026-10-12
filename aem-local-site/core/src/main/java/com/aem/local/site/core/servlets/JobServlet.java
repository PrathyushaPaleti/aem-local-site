package com.aem.local.site.core.servlets;

import com.aem.local.site.core.constants.JobTopics;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component(
    service = Servlet.class,
    property = {
        "sling.servlet.paths=/bin/trigger/slingjob",
        "sling.servlet.methods=GET"
    }
)
public class JobServlet extends SlingSafeMethodsServlet {

    @Reference
    private JobManager jobManager;

    @Override
    protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) 
            throws ServletException, IOException {
        
        response.setContentType("text/plain");

        // Pass custom payload/properties to the job
        Map<String, Object> jobProperties = new HashMap<>();
        jobProperties.put("message", "Triggered manually via HTTP request!");
        jobProperties.put("userId", request.getRemoteUser() != null ? request.getRemoteUser() : "anonymous");

        // Add the job to the queue
        Job job = jobManager.addJob(JobTopics.JOB_TOPIC, jobProperties);

        if (job != null) {
            response.getWriter().write("Sling Job successfully queued! Job ID: " + job.getId());
        } else {
            response.getWriter().write("Failed to queue Sling Job.");
        }
    }
}