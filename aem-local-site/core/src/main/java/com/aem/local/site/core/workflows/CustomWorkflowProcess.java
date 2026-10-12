package com.aem.local.site.core.workflows;

import com.adobe.granite.workflow.WorkflowSession;
import com.adobe.granite.workflow.exec.WorkItem;
import com.adobe.granite.workflow.exec.WorkflowProcess;
import com.adobe.granite.workflow.metadata.MetaDataMap;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(
    service = WorkflowProcess.class,
    property = {
        "process.label=AEM Local Site - Custom Workflow Process Step",
        "service.description=Custom workflow step example that logs payload and arguments."
    }
)
public class CustomWorkflowProcess implements WorkflowProcess {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomWorkflowProcess.class);

    @Override
    public void execute(WorkItem workItem, WorkflowSession workflowSession, MetaDataMap args) {
        LOGGER.info("=== Custom Workflow Process Step Started ===");

        try {
            // 1. Get the payload path (e.g., path of the page, asset, or node being processed)
            String payloadType = workItem.getWorkflowData().getPayloadType();
            String payloadPath = (String) workItem.getWorkflowData().getPayload();
            
            LOGGER.info("Workflow Payload Type: {}", payloadType);
            LOGGER.info("Workflow Payload Path: {}", payloadPath);

            // 2. Read custom arguments passed from the Workflow Model editor (Process Arguments)
            String processArgs = args.get("PROCESS_ARGS", "Default Argument");
            LOGGER.info("Workflow Process Arguments: {}", processArgs);

            // 3. Implement your business logic here
            // (e.g., modifying JCR properties, adding tags, sending notifications, calling external services)
            if ("JCR_PATH".equals(payloadType) && payloadPath != null) {
                LOGGER.info("Performing custom operation on JCR node: {}", payloadPath);
                // Example: ResourceResolver resolver = workflowSession.adaptTo(ResourceResolver.class);
            }

            LOGGER.info("=== Custom Workflow Process Step Completed Successfully ===");

        } catch (Exception e) {
            LOGGER.error("Error occurred during Custom Workflow Process execution", e);
        }
    }
}