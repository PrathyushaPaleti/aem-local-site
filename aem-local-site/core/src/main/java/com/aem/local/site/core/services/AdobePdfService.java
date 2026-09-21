package com.aem.local.site.core.services;

import com.adobe.pdfservices.operation.PDFServices;
import com.adobe.pdfservices.operation.PDFServicesMediaType;
import com.adobe.pdfservices.operation.PDFServicesResponse;
import com.adobe.pdfservices.operation.auth.Credentials;
import com.adobe.pdfservices.operation.auth.ServicePrincipalCredentials;
import com.adobe.pdfservices.operation.exception.SDKException;
import com.adobe.pdfservices.operation.exception.ServiceApiException;
import com.adobe.pdfservices.operation.exception.ServiceUsageException;
import com.adobe.pdfservices.operation.io.Asset;
import com.adobe.pdfservices.operation.io.StreamAsset;
import com.adobe.pdfservices.operation.pdfjobs.jobs.HTMLToPDFJob;
import com.adobe.pdfservices.operation.pdfjobs.params.htmltopdf.HTMLToPDFParams;
import com.adobe.pdfservices.operation.pdfjobs.params.htmltopdf.PageLayout;
import com.adobe.pdfservices.operation.pdfjobs.result.HTMLToPDFResult;
import com.day.cq.dam.api.AssetManager;

import org.apache.commons.io.IOUtils;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

@Component(service = AdobePdfService.class, immediate = true)
@Designate(ocd = AdobePdfService.Config.class)
public class AdobePdfService {

    private static final Logger LOG =
            LoggerFactory.getLogger(AdobePdfService.class);

    /**
     * ZIP file must be placed here:
     *
     * src/main/resources/createHtmlToPdfInput.zip
     */
    private static final String HTML_ZIP_RESOURCE = "/createHtmlToPdfInput.zip";

    /**
     * DAM destination.
     */
    private static final String DAM_FOLDER_PATH = "/content/dam/pdf-folder";

    /**
     * Service user subservice name.
     */
    private static final String SERVICE_USER = "pdf-service";

    @Reference
    private ResourceResolverFactory resourceResolverFactory;

    private String clientId;
    private String clientSecret;

    @ObjectClassDefinition(
            name = "Adobe PDF Services Configuration",
            description = "Adobe PDF Services credentials"
    )
    public @interface Config {

        @AttributeDefinition(
                name = "Client ID",
                description = "Adobe PDF Services Client ID"
        )
        String clientId() default "";

        @AttributeDefinition(
                name = "Client Secret",
                description = "Adobe PDF Services Client Secret",
                type = AttributeType.PASSWORD
        )
        String clientSecret() default "";
    }

    @Activate
    protected void activate(Config config) {

        this.clientId = config.clientId();
        this.clientSecret = config.clientSecret();

        LOG.info("Adobe PDF Service initialized");
    }

    /**
     * Reads the HTML ZIP from src/main/resources,
     * converts it to PDF using Adobe PDF Services,
     * and saves the generated PDF to DAM.
     *
     * Example:
     *
     * convertHtmlZipToPdf("my-page.pdf");
     *
     * Result:
     *
     * /content/dam/product-dam/my-page.pdf
     */
    public String convertHtmlZipToPdf(String pdfFileName)
            throws Exception {

        LOG.info("Starting HTML to PDF conversion. Resource={}, PDF={}",HTML_ZIP_RESOURCE,pdfFileName);

        /*
         * 1. Get ZIP from src/main/resources
         */
        byte[] pdfBytes;

        try (InputStream htmlZipInputStream = getClass().getResourceAsStream(HTML_ZIP_RESOURCE)) {

            if (htmlZipInputStream == null) {
                throw new IllegalStateException("HTML ZIP not found on classpath: "+ HTML_ZIP_RESOURCE);
            }

            /*
             * 2. Send ZIP to Adobe and get PDF bytes
             */
            pdfBytes = generatePdf(htmlZipInputStream);
        }

        /*
         * 3. Save PDF to DAM
         */
        return savePdfToDam(pdfFileName, pdfBytes);
    }

    /**
     * Converts HTML ZIP to PDF using Adobe PDF Services.
     */
    private byte[] generatePdf(InputStream htmlZipInputStream) throws Exception {

        try {

            /*
             * Create Adobe credentials
             */
            Credentials credentials = new ServicePrincipalCredentials(clientId, clientSecret);

            /*
             * Create Adobe PDF Services client
             */
            PDFServices pdfServices =new PDFServices(credentials);

            /*
             * Upload ZIP to Adobe.
             *
             * The ZIP must contain index.html.
             */
            Asset inputAsset = pdfServices.upload(htmlZipInputStream, PDFServicesMediaType.ZIP.getMediaType());

            /*
             * HTML -> PDF parameters
             */
            HTMLToPDFParams params = getHtmlToPdfParams();

            /*
             * Create HTML -> PDF job
             */
            HTMLToPDFJob job = new HTMLToPDFJob(inputAsset).setParams(params);

            /*
             * Submit job
             */
            String location = pdfServices.submit(job);

            LOG.info("Adobe PDF job submitted. Location={}",location);

            /*
             * Get result
             */
            PDFServicesResponse<HTMLToPDFResult> response = pdfServices.getJobResult(location, HTMLToPDFResult.class);

            /*
             * Get generated PDF asset
             */
            Asset resultAsset = response.getResult().getAsset();

            /*
             * Download generated PDF
             */
            StreamAsset resultStreamAsset = pdfServices.getContent(resultAsset);

            /*
             * Convert stream to byte[]
             */
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            try (InputStream pdfInputStream = resultStreamAsset.getInputStream()) {

                IOUtils.copy(pdfInputStream,output);
            }

            LOG.info( "PDF generated successfully. Size={} bytes", output.size());

            return output.toByteArray();

        } catch (ServiceApiException | ServiceUsageException | SDKException e) {

            LOG.error("Adobe PDF generation failed", e);

            throw new Exception( "Adobe PDF generation failed", e);
        }
    }

    /**
     * Adobe HTML-to-PDF configuration.
     */
    private HTMLToPDFParams getHtmlToPdfParams() {

        PageLayout pageLayout = new PageLayout();

        /*
         * Width = 8 inches
         * Height = 11.5 inches
         */
        pageLayout.setPageSize(8, 11.5);

        return new HTMLToPDFParams.Builder().includeHeaderFooter(false).withPageLayout(pageLayout).build();
    }

    /**
     * Saves generated PDF to AEM DAM.
     */
    private String savePdfToDam(String pdfFileName, byte[] pdfBytes) throws LoginException {

        if (!pdfFileName.endsWith(".pdf")) {
            pdfFileName += ".pdf";
        }

        String assetPath =
                DAM_FOLDER_PATH + "/" + pdfFileName;

        Map<String, Object> authInfo = Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, SERVICE_USER);

        try (ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(authInfo)) {

            AssetManager assetManager = resourceResolver.adaptTo(AssetManager.class);

            if (assetManager == null) {
                throw new IllegalStateException(
                        "Unable to obtain AssetManager"
                );
            }

            /*
             * Create/update PDF asset.
             */
            assetManager.createAsset(assetPath, new ByteArrayInputStream(pdfBytes),"application/pdf",true);

            LOG.info("PDF successfully saved to DAM: {}", assetPath);
            return assetPath;
        }
    }
}