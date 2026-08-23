package com.aem.local.site.core.models;

public class PostDto {
    private int userId;
    private int id;
    private String title;
    private String body;

    // Getters are required so HTL (Sightly) can read these properties
    public int getUserId() {
        return userId;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }
}
