package com.aem.local.site.core.models;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.RequestAttribute;
import org.apache.sling.models.annotations.injectorspecific.ScriptVariable;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.annotation.PostConstruct;
import javax.inject.Named;
import javax.jcr.Node;
import javax.jcr.Session;

import static org.apache.sling.api.resource.ResourceResolver.PROPERTY_RESOURCE_TYPE;

@Model(adaptables = {
    Resource.class,
    SlingHttpServletRequest.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class Teaser {

    private static final Logger LOG = LoggerFactory.getLogger(Teaser.class);

    @ValueMapValue(name=PROPERTY_RESOURCE_TYPE)
    @Default(values="No resourceType")
    protected String resourceType;

    @SlingObject
    private Resource currentResource;

    @ScriptVariable
    private Page currentPage;
    

    @RequestAttribute(name = "currentPage")
    private Page requestCurrentPage;

    @Named("jcr:lastModifiedBy")
    private String lastModifiedBy;

    @SlingObject
    private ResourceResolver resourceResolver;

    private String message;

    @ValueMapValue
    @Default(values="Adventure awaits! Explore the world with our curated travel experiences.")
    @Named("jcr:title")
    protected String title;

    @ValueMapValue
    protected String description;

    @ValueMapValue
    protected String fileReference;

    @ValueMapValue(injectionStrategy=InjectionStrategy.OPTIONAL)
    protected String linkURL;

    @ValueMapValue
    protected String linkText;

    @ValueMapValue
    protected String linkType;

    @ValueMapValue
    protected String internalLinkID;

    @ValueMapValue
    protected String externalLink;

    @Self
    private SlingHttpServletRequest request;

    @PostConstruct
    protected void init() {

        //PageManager page = slingScriptHelper.getService(PageManager.class);

        PageManager pageManager = resourceResolver.adaptTo(PageManager.class);

        //To get the resourceResolver from the request and then get the current page from the request
        ResourceResolver resolver = request.getResourceResolver();   
        if(resolver != null) {
            if(pageManager != null) {
                currentPage = pageManager.getContainingPage(currentResource);
            }
        }
        String resourcePath = request.getRequestPathInfo().getResourcePath();
        Session session = resolver.adaptTo(Session.class);
        UserManager userManager = resolver.adaptTo(UserManager.class);
        
        Node node = currentResource.adaptTo(Node.class);
        Page page = requestCurrentPage.adaptTo(Page.class);

        LOG.info("Page path: {}", (page != null) ? page.getPath() : "Page is null");

        if(session == null && userManager == null) {
           LOG.warn("Session or UserManager is null. Cannot validate user access");
        }

        try{
            User currentUser = (User) userManager.getAuthorizable(session.getUserID());
            LOG.info("Current user ID is: {}", currentUser.getID());
            LOG.info("Current node {}", (node != null) ? node.getPath() : "Node is null");
        }catch(Exception e){
            LOG.error("Error while getting current user: ", e);
        }



        message = "Teaser custom component!\n"
                + "Resource type is: " + resourceType + "\n"
                + "Current page is:  " + requestCurrentPage.getPath() + "\n"
                + "Current page from Request: " + currentPage.getPath() + "\n"
                + "Current resource path is: " + resourcePath + "\n";

        LOG.info("Teaser component message: {}", message);
    }

    public String getTitle() {
        return title;
    }

    public String getPageTitle() {
        return (currentPage != null) ? currentPage.getTitle() : "No page context found";
    }

    public String getDescription() {
        return description;
    }
    
    public String getFileReference() {
        return fileReference;
    }

    public String getLinkURL() {
        return linkURL;
    }
     
    public String getLinkText() {
        return linkText;
    }

    public String getLinkType() {
        return linkType;
    }

    public String getExternalLink() {
        return externalLink;
    }

    public String getInternalLinkID() {
        return internalLinkID;
    }

}
