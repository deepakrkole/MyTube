package com.youtube.cmpe277.mytube;

import com.google.android.gms.common.Scopes;
import com.google.api.services.youtube.YouTubeScopes;

public class Constants {

    public static final String[] scopes = {"oauth2:"+ Scopes.PROFILE, "oauth2:"+ YouTubeScopes.YOUTUBE};

    public static final String PLAYLIST_NAME = "SJSU-CMPE-277";

    public static final String BASE_URL = "https://www.googleapis.com/youtube/v3/";

    public static final String SEARCH_ENDPOINT = "search";
    public static final String GET_VIDEO_ENDPOINT = "videos";

    public static final String STATISTICS = "statistics";
    public static final String MAX_RESULTS = "maxResults";

    public static final String PLAYLISTS_ENDPOINT = "playlists";
    public static final String PLAYLISTITEMS = "playlistItems";


    public static final String KEYWORD = "q";
    public static final String TYPE = "type";

    public static final long VIDEOS_PER_PAGE = 20;

    private static String accessToken="";

    public static String getAccessToken() {

        return accessToken;
    }

    public static void setAccessToken(String a) {

        accessToken = a;
    }

    public static final String MINE = "mine";
    public static final String ID = "id";
    public static final String PART = "part";
    public static final String SNIPPET = "snippet";

}