package com.aem.local.site.core.services.impl;

import com.aem.local.site.core.constants.JobTopics;
import org.apache.sling.event.jobs.Job;
import org.apache.sling.event.jobs.consumer.JobConsumer;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
    service = JobConsumer.class,
    property = {
        JobConsumer.PROPERTY_TOPICS + "=" + JobTopics.JOB_TOPIC
    }
)
public class JobConsumerImpl implements JobConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobConsumerImpl.class);

    @Override
    public JobResult process(Job job) {
        LOGGER.info("=== Sling Job Started ===");

        // Retrieve properties passed along with the job
        String message = job.getProperty("message", String.class);
        String userId = job.getProperty("userId", String.class);

        LOGGER.info("Job Topic: {}", job.getTopic());
        LOGGER.info("Received Message: {}", message);
        LOGGER.info("Received User ID: {}", userId);

        try {
            // Simulate background task (e.g., calling an external API, heavy JCR processing, etc.)
            Thread.sleep(2000);
            LOGGER.info("=== Sling Job Completed Successfully ===");
            
            // Return OK if processed successfully
            return JobResult.OK;

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Job interrupted", e);
            return JobResult.FAILED;
        } catch (Exception e) {
            LOGGER.error("Job failed with error", e);
            // Return FAILED or CANCEL depending on whether you want a retry
            return JobResult.FAILED;
        }
    }
}