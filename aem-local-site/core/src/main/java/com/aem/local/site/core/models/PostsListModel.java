package com.aem.local.site.core.models;

import com.aem.local.site.core.services.PostService;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.OSGiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Model(
    adaptables = {SlingHttpServletRequest.class, Resource.class},
    defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL
)
public class PostsListModel {

    private static final Logger log = LoggerFactory.getLogger(PostsListModel.class);

    // Direct injection of the OSGi service bypassing the servlet
    @OSGiService
    private PostService postService;

    private List<PostDto> postsList;

    @PostConstruct
    protected void init() {
        try {
            // Fetch the JSON string representing all posts (passing null for no specific ID)
            String rawJson = postService.getPosts(null);

            if (rawJson != null && !rawJson.isEmpty()) {
                Gson gson = new Gson();
                
                PostDto[] postsArray = gson.fromJson(rawJson, PostDto[].class);
                if (postsArray != null) {
                    this.postsList = Arrays.asList(postsArray);
                }
            }
        } catch (Exception e) {
            log.error("Error occurred while loading posts data in Sling Model", e);
            this.postsList = Collections.emptyList();
        }
    }

    /**
     * Getter method to expose the list of parsed posts to HTL
     */
    public List<PostDto> getPostsList() {
        return postsList;
    }
}