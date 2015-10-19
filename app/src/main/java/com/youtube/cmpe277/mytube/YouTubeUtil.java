package com.youtube.cmpe277.mytube;

import com.google.api.client.util.Joiner;
import com.google.api.services.youtube.model.Playlist;
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemSnippet;
import com.google.api.services.youtube.model.PlaylistSnippet;
import com.google.api.services.youtube.model.ResourceId;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class YouTubeUtil {



    //JSON Utilities

    public static Map<String, Object> jsonToMap(JSONObject json) throws JSONException {
        Map<String, Object> retMap = new HashMap<String, Object>();

        if(json != JSONObject.NULL) {
            retMap = convertToMap(json);
        }
        return retMap;
    }


    public static Map<String, Object> convertToMap(JSONObject object) throws JSONException {
        Map<String, Object> map = new HashMap<String, Object>();

        Iterator<String> keysItr = object.keys();
        while(keysItr.hasNext()) {
            String key = keysItr.next();
            Object value = object.get(key);

            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = convertToMap((JSONObject) value);
            }
            map.put(key, value);
        }
        return map;
    }

    public static List<Object> toList(JSONArray array) throws JSONException {
        List<Object> list = new ArrayList<Object>();
        for(int i = 0; i < array.length(); i++) {
            Object value = array.get(i);
            if(value instanceof JSONArray) {
                value = toList((JSONArray) value);
            }

            else if(value instanceof JSONObject) {
                value = convertToMap((JSONObject) value);
            }
            list.add(value);
        }
        return list;
    }

    public static ArrayList<YouTubeDataModel> searchVideos(String keywords) throws Exception {


        String searchVideoURL = Constants.BASE_URL+Constants.SEARCH_ENDPOINT;

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(Constants.PART).append("=" + "id,snippet");
        urlBuilder.append("&").append(Constants.MAX_RESULTS);
        urlBuilder.append("=" + Constants.VIDEOS_PER_PAGE);
        urlBuilder.append("&").append(Constants.KEYWORD).append("=").append(keywords);
        urlBuilder.append("&").append(Constants.TYPE).append("=").append("video");


        String reqParams = urlBuilder.toString();
        ArrayList <String> searchResponse = HTTPDataUtil
                .getResponse(
                        searchVideoURL,
                        reqParams,
                        Constants.getAccessToken());

        JSONObject searchVideosJSON = new JSONObject(searchResponse.get(0));

        Map<String, Object> searchVideoMap = convertToMap(searchVideosJSON);

        ArrayList <Object> searchResponseList = new ArrayList<Object>();
        searchResponseList.addAll((Collection<?>) searchVideoMap.get("items"));


        ArrayList <YouTubeDataModel> playlistItemList = getFavoriteVideos();
        ArrayList<YouTubeDataModel> dataList = processJsonData(searchResponseList,
                playlistItemList);

        return dataList;
    }

    private static ArrayList<YouTubeDataModel> processJsonData(
            ArrayList<Object> searchResponseList,
            ArrayList<YouTubeDataModel> playlistItemList) throws JSONException
    {
        ArrayList<YouTubeDataModel> dataList = new ArrayList<YouTubeDataModel>();
        for (int i = 0; i < searchResponseList.size(); i++) {

            YouTubeDataModel file = new YouTubeDataModel();

            HashMap idMap = ((HashMap)((HashMap)searchResponseList.get(i)).get("id"));

            Map<String, Object> videoMap = getVideoDetails((String) idMap.get("videoId"));

            ArrayList <Object> videoItems = new ArrayList<Object>();
            videoItems.addAll((Collection<?>) videoMap.get("items"));

            HashMap snippetMap = ((HashMap) ((HashMap) videoItems.get(0)).get("snippet"));
            HashMap statisticsMap = ((HashMap) ((HashMap) videoItems.get(0)).get("statistics"));

            file.setId((String) ((HashMap)videoItems.get(0)).get("id"));
            file.setPublishedDate((String) snippetMap.get("publishedAt"));
            file.setTitle((String) snippetMap.get("title"));
            file.setThumbnailURL((String) ((HashMap) (((HashMap) snippetMap.get("thumbnails"))).get("default")).get("url"));
            String playlistId = getPlayListId(playlistItemList, file.getId());
            file.setPlaylistId(playlistId);
            file.setFavorite((playlistId != "0"));
            file.setNumberOfViews((String) statisticsMap.get("viewCount"));

            dataList.add(file);
        }

        return dataList;
    }
    private static Map<String, Object> getVideoDetails (String id) throws JSONException {

        List<String> videoIds = new ArrayList<String>();
        videoIds.add(id);
        Joiner stringJoiner = Joiner.on(',');
        String videoId = stringJoiner.join(videoIds);

        String videoURL = Constants.BASE_URL+Constants.GET_VIDEO_ENDPOINT;

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(Constants.PART).append("=").append(Constants.SNIPPET)
                .append(",").append(Constants.STATISTICS);
        urlBuilder.append("&").append(Constants.ID).append("=" + videoId);

        String reqParams = urlBuilder.toString();
        ArrayList <String> videoResponse = HTTPDataUtil
                .getResponse(
                        videoURL,
                        reqParams,
                        Constants.getAccessToken());

        JSONObject videoJSON = new JSONObject(videoResponse.get(0));

        Map<String, Object> videoMap = convertToMap(videoJSON);

        return videoMap;
    }

    public static String createPlayList() throws JSONException {

        PlaylistSnippet playlistSnippet = new PlaylistSnippet();
        playlistSnippet.setTitle(Constants.PLAYLIST_NAME);

        Playlist playlist = new Playlist();
        playlist.setSnippet(playlistSnippet);

        JSONObject reqBody = new JSONObject(playlist);

        String url = Constants.BASE_URL+Constants.PLAYLISTS_ENDPOINT;

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(Constants.PART).append("=" + "snippet");
        String reqParams = urlBuilder.toString();

        String responseCode = HTTPDataUtil.postRequest(
                url,
                reqParams,
                reqBody.toString(),
                Constants.getAccessToken(), true);

        return responseCode;
    }

    public static ArrayList <YouTubeDataModel> getFavorites() throws JSONException {

        ArrayList <YouTubeDataModel> playlistItemList = getFavoriteVideos();

        for (int i = 0; i < playlistItemList.size(); i++) {

            YouTubeDataModel modelObj = playlistItemList.get(i);

            Map<String, Object> videoMap = getVideoDetails(modelObj.getId());

            ArrayList <Object> videoItems = new ArrayList<Object>();
            videoItems.addAll((Collection<?>) videoMap.get("items"));

            HashMap snippetMap = ((HashMap) ((HashMap) videoItems.get(0)).get(Constants.SNIPPET));
            HashMap statisticsMap = ((HashMap) ((HashMap) videoItems.get(0)).get(Constants.STATISTICS));

            modelObj.setPublishedDate((String) snippetMap.get("publishedAt"));
            modelObj.setTitle((String) snippetMap.get("title"));
            modelObj.setThumbnailURL((String) ((HashMap) (((HashMap) snippetMap.get("thumbnails"))).get("default")).get("url"));
            modelObj.setFavorite(true);
            modelObj.setNumberOfViews((String) statisticsMap.get("viewCount"));

            playlistItemList.set(i, modelObj);
        }

        return playlistItemList;
    }

    private static ArrayList <YouTubeDataModel> getFavoriteVideos() throws JSONException {

        String url = Constants.BASE_URL+Constants.PLAYLISTITEMS;

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(Constants.PART).append("=" + "snippet");
        urlBuilder.append("&").append(Constants.MAX_RESULTS).append("=").append("40");
        urlBuilder.append("&").append("playlistId");
        urlBuilder.append("=" + AppConfig.getSharedSettings().getFavoritePlaylistId());

        String reqParams = urlBuilder.toString();

        ArrayList <String> playlistItemsResponse = HTTPDataUtil
                .getResponse(
                        url,
                        reqParams,
                        Constants.getAccessToken());

        JSONObject playlistItemsJSON = new JSONObject(playlistItemsResponse.get(0));

        Map<String, Object> playlistItemsMap = convertToMap(playlistItemsJSON);

        ArrayList <Object> playlistVideoItems = new ArrayList<Object>();
        playlistVideoItems.addAll((Collection<?>) playlistItemsMap.get("items"));

        ArrayList <YouTubeDataModel> favoritePlayList = new ArrayList<YouTubeDataModel>();

        for (int i = 0; i < playlistVideoItems.size(); i++) {

            YouTubeDataModel modelObj = new YouTubeDataModel();

            HashMap snippetMap = ((HashMap)((HashMap) playlistVideoItems.get(i)).get("snippet"));
            modelObj.setId((String) ((HashMap) snippetMap.get("resourceId")).get("videoId"));
            modelObj.setPlaylistId(((String) ((HashMap) playlistVideoItems.get(i)).get("id")));

            favoritePlayList.add(modelObj);
        }

        return favoritePlayList;
    }

    public static String getFavoritesList(String playlistName) throws JSONException {

        String url = Constants.BASE_URL+Constants.PLAYLISTS_ENDPOINT;

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(Constants.PART).append("="+Constants.ID);
        urlBuilder.append(",").append("snippet");
        urlBuilder.append("&").append(Constants.MINE).append("=" + "true");

        String reqParams = urlBuilder.toString();
        ArrayList <String> playlistResponse =
                HTTPDataUtil.getResponse(
                        url,
                        reqParams,
                        Constants.getAccessToken());

        JSONObject playlistJSON = new JSONObject(playlistResponse.get(0));

        Map<String, Object> playlistMap = convertToMap(playlistJSON);

        ArrayList <Object> playlistList = new ArrayList<Object>();
        playlistList.addAll((Collection<?>) playlistMap.get("items"));

        String playlistId = (String)((HashMap) playlistList.get(0)).get("id");
        return playlistId;
    }

    public static String removeFromFavorites(String videoId) {

        String url = Constants.BASE_URL+Constants.PLAYLISTITEMS;

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(Constants.ID).append("=" + videoId);
        String insertPlaylistItemsParams = urlBuilder.toString();

        String responseCode = HTTPDataUtil.postRequest(
                url,
                insertPlaylistItemsParams,
                "",
                Constants.getAccessToken(),
                false);

        return responseCode;
    }

    public static String insertInFavoritesList(YouTubeDataModel file) {


        ResourceId resourceId = new ResourceId();
        resourceId.setKind("youtube#video");
        resourceId.setVideoId(file.getId());

        PlaylistItemSnippet playlistItemSnippet = new PlaylistItemSnippet();
        playlistItemSnippet.setTitle(file.getTitle());
        playlistItemSnippet.setPlaylistId(AppConfig.getSharedSettings().getFavoritePlaylistId());
        playlistItemSnippet.setResourceId(resourceId);

        PlaylistItem playlistItem = new PlaylistItem();
        playlistItem.setSnippet(playlistItemSnippet);

        JSONObject reqBody = new JSONObject(playlistItem);

        String url = Constants.BASE_URL+Constants.PLAYLISTITEMS;

        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(Constants.PART).append("="+"snippet");
        urlBuilder.append(",").append("contentDetails");
        String reqParams = urlBuilder.toString();

        String responseCode = HTTPDataUtil.postRequest(
                url,
                reqParams,
                reqBody.toString(),
                Constants.getAccessToken(), true);

        return responseCode;
    }

    private static String getPlayListId(
            ArrayList<YouTubeDataModel> playlistItemList,
            String videoId)
    {

        String playlistId = "0";
        for(YouTubeDataModel file:playlistItemList) {

            if (file.getId().equals(videoId)) {

                playlistId = file.getPlaylistId();
                break;
            }
        }

        return playlistId;
    }
}