package com.example.logbook2_image_album.model;

import android.net.Uri;

/**
 * Data model representing an image entry in the album.
 * Supports both local drawable resources and external Uris.
 */
public class ImageItem {
    private final Integer resId;
    private final Uri uri;
    private String caption;

    public ImageItem(Integer resId, String caption) {
        this.resId = resId;
        this.uri = null;
        this.caption = caption;
    }

    public ImageItem(Uri uri, String caption) {
        this.resId = null;
        this.uri = uri;
        this.caption = caption;
    }

    public Integer getResId() {
        return resId;
    }

    public Uri getUri() {
        return uri;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public boolean isResourceBased() {
        return resId != null;
    }
}
