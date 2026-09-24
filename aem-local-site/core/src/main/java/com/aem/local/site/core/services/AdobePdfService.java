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
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;

@Component(service = AdobePdfService.class, immediate = true)
@Designate(ocd = AdobePdfService.Config.class)
public class AdobePdfService {

    private static final Logger LOG = LoggerFactory.getLogger(AdobePdfService.class);

    private static final String MFS_COOKIES = "pst_user_lang_loc_role=en-us|investment-professional";
    /**
     * DAM destination folder.
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

    @ObjectClassDefinition(name = "Adobe PDF Services Configuration", description = "Adobe PDF Services credentials")
    public @interface Config {

        @AttributeDefinition(name = "Client ID", description = "Adobe PDF Services Client ID")
        String clientId() default "";

        @AttributeDefinition(name = "Client Secret", description = "Adobe PDF Services Client Secret",
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
     * Converts an HTML URL to PDF using Adobe PDF Services
     * and saves the generated PDF to AEM DAM.
     *
     * @param htmlUrl HTML page URL
     * @param pdfFileName PDF file name
     * @return DAM path of the generated PDF
     */
    public String convertHtmlUrlToPdf(String htmlUrl, String pdfFileName) throws Exception {

        String html = fetchHtmlWithCookies(htmlUrl, MFS_COOKIES); // Fetch HTML with cookies

        LOG.info("Starting HTML URL to PDF conversion. URL={}, PDF={}",htmlUrl, html.length());
        /*
         * 1. Generate PDF from HTML URL using Adobe PDF Services
         */
        //byte[] pdfBytes = generatePdfFromUrl(html); // option 2
        byte[] pdfBytes = generatePdfFromUrl(htmlUrl);
        return savePdfToDam(pdfFileName, pdfBytes);
    }

    /**
     * Converts HTML URL to PDF using Adobe PDF Services.
     *
     * Adobe supports creating HTMLToPDFJob directly from
     * an HTML URL.
     */
    private byte[] generatePdfFromUrl(String html) throws Exception {

        try {

            Credentials credentials = new ServicePrincipalCredentials(clientId, clientSecret);
            PDFServices pdfServices = new PDFServices(credentials);
            /*
             * HTML to PDF parameters.
            */
            HTMLToPDFParams params = getHtmlToPdfParams();
            /* Create HTML -> PDF job directly from URL */
            HTMLToPDFJob job = new HTMLToPDFJob(html).setParams(params);
            String location = pdfServices.submit(job);

            LOG.info("Adobe PDF job submitted. Location={}",location);
            PDFServicesResponse<HTMLToPDFResult> response = pdfServices.getJobResult(location,HTMLToPDFResult.class);

            Asset resultAsset = response.getResult().getAsset();
            StreamAsset resultStreamAsset = pdfServices.getContent(resultAsset);
            /* Convert Adobe response stream to byte[] */
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            try (InputStream pdfInputStream = resultStreamAsset.getInputStream()) {
                IOUtils.copy(pdfInputStream,output);
            }

            LOG.info("PDF generated successfully. Size={} bytes", output.size());
            return output.toByteArray();

        } catch (ServiceApiException | ServiceUsageException | SDKException e) {
            LOG.error("Adobe PDF generation failed", e);
            throw new Exception("Adobe PDF generation failed", e);
        }
    }

    /*private byte[] generatePdfFromHtml(String html) throws Exception {

        Credentials credentials = new ServicePrincipalCredentials(clientId, clientSecret);

        PDFServices pdfServices = new PDFServices(credentials);

        byte[] htmlBytes = html.getBytes(StandardCharsets.UTF_8);

        try (InputStream inputStream = new ByteArrayInputStream(htmlBytes)) {

            // Upload the HTML content to Adobe
            Asset htmlAsset = pdfServices.upload(inputStream, PDFServicesMediaType.HTML.getMediaType());

            HTMLToPDFParams params = getHtmlToPdfParams();

            // IMPORTANT: use Asset, NOT URL
            HTMLToPDFJob job = new HTMLToPDFJob(htmlAsset).setParams(params);

            String location = pdfServices.submit(job);

            LOG.info("Adobe PDF job submitted. Location={}", location);

            PDFServicesResponse<HTMLToPDFResult> response = pdfServices.getJobResult(location,HTMLToPDFResult.class);

            Asset resultAsset = response.getResult().getAsset();
            StreamAsset resultStreamAsset = pdfServices.getContent(resultAsset);
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            try (InputStream pdfInputStream = resultStreamAsset.getInputStream()) {

                IOUtils.copy(pdfInputStream, output);
            }

            LOG.info("PDF generated successfully. Size={} bytes", output.size());

            return output.toByteArray();
        }
    }*/

    /**
     * Adobe HTML-to-PDF configuration.
     */
    private HTMLToPDFParams getHtmlToPdfParams() {

        PageLayout pageLayout = new PageLayout();
        pageLayout.setPageSize( 7, 11.5);

        return new HTMLToPDFParams.Builder()
                .includeHeaderFooter(false)
                .withPageLayout(pageLayout)
                .build();
    }

    /**
     * Saves generated PDF to AEM DAM.
     */
    private String savePdfToDam(String pdfFileName,byte[] pdfBytes) throws LoginException {

        if (!pdfFileName.toLowerCase().endsWith(".pdf")) {
            pdfFileName += ".pdf";
        }

        String assetPath = DAM_FOLDER_PATH + "/" + pdfFileName;
        Map<String, Object> authInfo = Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, SERVICE_USER);

        try (ResourceResolver resourceResolver = resourceResolverFactory.getServiceResourceResolver(authInfo)) {

            AssetManager assetManager = resourceResolver.adaptTo(AssetManager.class);
            if (assetManager == null) {
                throw new IllegalStateException("Unable to obtain AssetManager");
            }
            /*
             * Create/update PDF asset.
             */
            assetManager.createAsset(assetPath, new ByteArrayInputStream(pdfBytes),"application/pdf",true);
            LOG.info("PDF successfully saved to DAM: {}", assetPath);
            return assetPath;
        }
    }

    private String fetchHtmlWithCookies(String htmlUrl, String cookie) throws Exception {

        HttpGet request = new HttpGet(htmlUrl);
        request.setHeader("Cookie", cookie);
        try (CloseableHttpClient client = HttpClients.createDefault();
            CloseableHttpResponse response = client.execute(request)) {

            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode != 200) {
                throw new IllegalStateException("Failed to fetch HTML. HTTP status: " + statusCode);
            }

            return EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);
        }
    }
}