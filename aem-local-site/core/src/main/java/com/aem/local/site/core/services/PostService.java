package com.aem.local.site.core.services;

import java.io.IOException;

public interface PostService {
    /**
     * Fetches post data from the external API
     */
    String getPosts(String postId) throws IOException, InterruptedException;
}