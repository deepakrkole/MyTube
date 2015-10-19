package com.youtube.cmpe277.mytube;

import java.text.SimpleDateFormat;
import java.util.Date;

public class YouTubeDataModel {

    private boolean favorite;
    private String playlistId;
    private String id;
    private String title;
    private String numberOfViews;
    private String publishedDate;
    private String thumbnailURL;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumberOfViews() {
        return numberOfViews;
    }

    public void setNumberOfViews(String numberOfViews) {
        this.numberOfViews = numberOfViews;
    }

    public String getPublishedDate() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date convertedDate = null;

        try {

            convertedDate = sdf.parse(publishedDate);
            SimpleDateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");
            String convertedDateString = formatter.format(convertedDate);

            return convertedDateString;
        } catch(Exception ex){

            ex.printStackTrace();
            return null;
        }
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    public void setThumbnailURL(String thumbnail) {
        this.thumbnailURL = thumbnail;
    }

    public void setFavorite (boolean favorite) {

        this.favorite = favorite;
    }

    public boolean isFavorite() {

        return this.favorite;
    }

    public void setPlaylistId (String playlistId) {

        this.playlistId = playlistId;
    }

    public String getPlaylistId () {

        return this.playlistId;
    }
}