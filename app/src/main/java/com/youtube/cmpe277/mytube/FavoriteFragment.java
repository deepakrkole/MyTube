package com.youtube.cmpe277.mytube;

import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class FavoriteFragment extends Fragment {

    FavoriteFragmentListener favoriteFragmentListener;

    public interface  FavoriteFragmentListener {

        public void didSelectFavoriteResult(String videoId);
        public void didModifyFavorites();
    }


    View rootView;
    private ArrayList<File> searchResults = new ArrayList<File>();
    int selectedIndex;
    String removeFromFavoritesResponseCode = "-1";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_favorite, container, false);

        addClickListener();

        return rootView;
    }

    private void addClickListener(){

        ListView favoriteVideos = (ListView)rootView.findViewById(R.id.favorite_videos);
        favoriteVideos.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos,
                                    long id) {

                System.out.println("onItemClick Adapter View Favorite fragment");
                String videoId = searchResults.get(pos).getId();

                favoriteFragmentListener.didSelectFavoriteResult(videoId);
            }

        });
    }

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);

        favoriteFragmentListener = (FavoriteFragmentListener)context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
    }

    @Override
    public void onStart() {

        super.onStart();

        getFavorites();
    }

    @Override
    public void onStop() {

        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
    }

    private void getFavorites() {

        new FavoriteTask().execute();
    }

    private void updateVideosFound(List <File> videoList) {

        ArrayAdapter<File> adapter = new ArrayAdapter<File>(getActivity().getApplicationContext(), R.layout.search_item, videoList) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                if(convertView == null) {

                    convertView = getActivity().getLayoutInflater().inflate(R.layout.search_item, parent, false);
                }

                File searchResult = searchResults.get(position);

                ImageView thumbnail = (ImageView)convertView.findViewById(R.id.video_thumbnail);
                TextView title = (TextView)convertView.findViewById(R.id.video_title);
                TextView publishedDate = (TextView)convertView.findViewById(R.id.publishedDate);
                TextView numberOfViews = (TextView)convertView.findViewById(R.id.numberOfViews);
                Button starButton = (Button)convertView.findViewById(R.id.star);
                starButton.setTag(position);

                starButton.setBackgroundResource(android.R.drawable.star_on);

                starButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        selectedIndex = (int)v.getTag();

                        File selectedVideo = searchResults.get(selectedIndex);

                        removeFromFavoritesResponseCode = "-1";
                        new RemoveFromFavoritesTask().execute(selectedVideo.getPlaylistId());
                    }
                });

                Picasso.with(getActivity().getApplicationContext()).load(searchResult.getThumbnailURL()).into(thumbnail);
                title.setText(searchResult.getTitle());
                publishedDate.setText(searchResult.getPublishedDate());
                numberOfViews.setText(searchResult.getNumberOfViews());

                return convertView;
            }
        };

        ListView favoriteVideos = (ListView)rootView.findViewById(R.id.favorite_videos);
        favoriteVideos.setAdapter(adapter);
    }

    private void updateVideoInSearchResults(Boolean isFavorite) {

        searchResults.remove(selectedIndex);

        updateVideosFound(searchResults);
    }




    public class FavoriteTask extends AsyncTask<String, String, ArrayList<File>> {

        @Override
        protected ArrayList<File> doInBackground(String... keyword) {

            try {

                searchResults = YouTubeConnector.getFavorites();
            } catch (Exception e) {

                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<File> items) {

            if (searchResults != null && searchResults.size() != 0) {

                updateVideosFound(searchResults);
            }
        }
    }



    private class RemoveFromFavoritesTask extends AsyncTask <String , Void, String> {

        @Override
        protected String doInBackground(String... videoId) {

            try {

                removeFromFavoritesResponseCode = YouTubeConnector.removeFromFavorites(videoId[0]);
            } catch (Exception e) {

                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String responseCode) {

            if (Integer.parseInt(removeFromFavoritesResponseCode) != -1) {

                updateVideoInSearchResults(false);
            }
        }
    }
}