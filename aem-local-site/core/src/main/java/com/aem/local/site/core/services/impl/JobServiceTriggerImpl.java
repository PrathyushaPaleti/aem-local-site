package com.aem.local.site.core.services.impl;

import com.aem.local.site.core.constants.JobTopics;

import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.JobManager;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.Activate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

@Component(service = JobServiceTriggerImpl.class, immediate = true)
public class JobServiceTriggerImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobServiceTriggerImpl.class);

    @Reference
    private JobManager jobManager;

    @Activate
    public void activate() {
        LOGGER.info("Triggering sample Sling Job on bundle startup...");

        Map<String, Object> jobProperties = new HashMap<>();
        jobProperties.put("message", "Hello from AEM Local Instance!");
        jobProperties.put("userId", "admin");

        // Add the job to the Sling Job queue
       Job job = jobManager.addJob(JobTopics.JOB_TOPIC, jobProperties);

        if (job != null) {
            LOGGER.info("Sling Job successfully added with ID: {}", job.getId());
        } else {
            LOGGER.warn("Failed to add Sling Job (possibly duplicate or queue full).");
        }
    }
}