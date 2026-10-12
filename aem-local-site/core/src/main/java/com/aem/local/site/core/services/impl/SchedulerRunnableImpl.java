package com.aem.local.site.core.services.impl;

import org.apache.sling.commons.scheduler.ScheduleOptions;
import org.apache.sling.commons.scheduler.Scheduler;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(service = Runnable.class, immediate = true)
@Designate(ocd = SchedulerRunnableImpl.Config.class)
public class SchedulerRunnableImpl implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchedulerRunnableImpl.class);

    @Reference
    private Scheduler scheduler;

    private int schedulerId;

    @ObjectClassDefinition(name = "AEM Local Site - Example Cron Job Configuration", 
                           description = "Scheduled cron job example using Sling Scheduler")
    public @interface Config {
       /*@AttributeDefinition(name = "Cron Expression", 
                             description = "Cron expression (e.g., '0 0/5 * * * ?' for every 5 minutes)")
        String cron_expression() default "0 0/2 * * * ?"; // Default: Every 2 minutes for testing*/
        @AttributeDefinition(
            name = "Cron Expression", 
            description = "Quartz Cron expression. Default: Every Sunday at midnight ('0 0 0 ? * SUN')"
        )
        String cron_expression() default "0 0 0 ? * SUN";
        @AttributeDefinition(name = "Enable Job", 
                             description = "Check to enable this scheduled job")
        boolean enabled() default true;
    }

    @Activate
    public void activate(Config config) {
        schedulerId = "SchedulerRunnable".hashCode();
        
        if (config.enabled()) {
            // Create schedule options using the cron expression
            ScheduleOptions options = scheduler.EXPR(config.cron_expression());
            options.name(String.valueOf(schedulerId));
            
            // Can-run concurrently = false ensures the job won't run again if the previous execution is still ongoing
            options.canRunConcurrently(false);

            // Schedule the job
            boolean scheduled = scheduler.schedule(this, options);
            if (scheduled) {
                LOGGER.info("=== Scheduler Runnable successfully scheduled with expression: {} ===", config.cron_expression());
            } else {
                LOGGER.error("=== Failed to schedule Scheduler Runnable ===");
            }
        } else {
            LOGGER.info("=== Scheduler Runnable is disabled via configuration ===");
        }
    }

    @Deactivate
    public void deactivate() {
        // Unschedule the job when the bundle is stopped or component is modified
        scheduler.unschedule(String.valueOf(schedulerId));
        LOGGER.info("=== Scheduler Runnable unscheduled ===");
    }

    @Override
    public void run() {
        LOGGER.info("=== Scheduler Runnable Execution Started ===");
        
        try {
            // Place your scheduled logic here 
            // (e.g., clearing caches, syncing users, cleaning up old JCR nodes, calling external reports)
            LOGGER.info("Performing scheduled background tasks...");
            
            // Simulate task work
            Thread.sleep(1000);

            LOGGER.info("=== Scheduler Runnable Execution Completed Successfully ===");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.error("Scheduler Runnable interrupted", e);
        } catch (Exception e) {
            LOGGER.error("Error occurred during Scheduler Runnable execution", e);
        }
    }
}