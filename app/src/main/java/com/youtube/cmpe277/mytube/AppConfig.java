package com.youtube.cmpe277.mytube;

public class AppConfig {

    private static AppConfig sharedSettings = null;


    private String favoritePlaylistId;
    private String oAuth2ClientId;
    private AppConfig() {

        favoritePlaylistId = "";
    }

    public static AppConfig getSharedSettings() {

        if (sharedSettings == null) {

            sharedSettings = new AppConfig();
        }

        return sharedSettings;
    }


    public String getFavoritePlaylistId () {

        return this.favoritePlaylistId;
    }

    public void setFavoritePlaylistId (String favoritePlaylistId) {

        this.favoritePlaylistId = favoritePlaylistId;
    }
}