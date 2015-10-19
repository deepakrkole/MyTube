package com.youtube.cmpe277.mytube;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    ISearchFragment fragmentListener;

    public interface ISearchFragment {

        public void playVideo(String videoId);
    }


    View v;
    private ArrayList<YouTubeDataModel> searchResults = new ArrayList<YouTubeDataModel>();
    String add = "-1";
    String remove = "-1";
    int selectedIndex;

    Button searchButton = null;
    EditText searchEditText = null;

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);

        fragmentListener = (ISearchFragment)context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStart() {

        super.onStart();
    }

    @Override
    public void onStop() {

        super.onStop();
        hideSoftKeyboard(getActivity(), this.getView());
        searchEditText.setText("");
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);
    }

    public static void hideSoftKeyboard (Activity activity, View view) {

        InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_search, container, false);

        addTextChangeListener();
        addClickListener();


        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);

        final InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }

    private void addTextChangeListener() {

        EditText searchEditText = (EditText) v.findViewById(R.id.search_input);

        searchEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                searchOnYoutube(s.toString());
            }
        });
    }

    private void searchOnYoutube(final String keywords) {

        new SearchTask().execute(keywords);
    }

    private void addClickListener(){

        ListView searchedVideos = (ListView) v.findViewById(R.id.search_videos);
        searchedVideos.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> av, View v, int pos,
                                    long id) {

                String videoId = searchResults.get(pos).getId();
                fragmentListener.playVideo(videoId);
            }

        });


        searchButton = (Button)v.findViewById(R.id.s_button);
        searchEditText = (EditText) v.findViewById(R.id.search_input);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                System.out.print(searchEditText.getText().toString());
                searchOnYoutube(searchEditText.getText().toString());


            }
        });
    }



    private void updateVideosFound(List <YouTubeDataModel> videoList) {

        ArrayAdapter<YouTubeDataModel> adapter =
                new ArrayAdapter<YouTubeDataModel>(
                        getActivity().getApplicationContext(),
                        R.layout.search_item,
                        videoList) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {

                if(convertView == null) {

                    convertView = getActivity().getLayoutInflater().inflate(R.layout.search_item, parent, false);
                }

                final YouTubeDataModel searchResult = searchResults.get(position);

                ImageView thumbnail = (ImageView)convertView.findViewById(R.id.video_thumbnail);
                TextView title = (TextView)convertView.findViewById(R.id.video_title);
                TextView publishedDate = (TextView)convertView.findViewById(R.id.publishedDate);
                TextView numberOfViews = (TextView)convertView.findViewById(R.id.numberOfViews);
                Button starButton = (Button)convertView.findViewById(R.id.star);
                starButton.setTag(position);

                if (searchResult.isFavorite()) {

                    starButton.setBackgroundResource(android.R.drawable.star_on);
                } else {

                    starButton.setBackgroundResource(android.R.drawable.star_off);
                }

                starButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {

                        selectedIndex = (int)v.getTag();

                        YouTubeDataModel selectedVideo = searchResults.get(selectedIndex);

                        if (!selectedVideo.isFavorite()) {

                            add = "-1";
                            new AddToFavoritesTask().execute(selectedVideo);
                        } else {

                            remove = "-1";
                            new RemoveTask().execute(selectedVideo.getPlaylistId());
                        }
                    }
                });

                Picasso.with(getActivity().getApplicationContext()).load(searchResult.getThumbnailURL()).into(thumbnail);
                title.setText(searchResult.getTitle());
                publishedDate.setText(searchResult.getPublishedDate());
                numberOfViews.setText(searchResult.getNumberOfViews());

                return convertView;
            }
        };

        ListView searchvideos = (ListView) v.findViewById(R.id.search_videos);
        searchvideos.setAdapter(adapter);
    }

    private void updateSearchResults(Boolean isFavorite) {

        YouTubeDataModel selectedVideo = searchResults.get(selectedIndex);

        selectedVideo.setFavorite(isFavorite);
        searchResults.set(selectedIndex, selectedVideo);

        updateVideosFound(searchResults);
    }



    private class SearchTask extends AsyncTask <String, String, ArrayList<YouTubeDataModel>> {


        @Override
        protected ArrayList<YouTubeDataModel> doInBackground(String... keyword) {

            try {

                searchResults = YouTubeUtil.searchVideos(keyword[0]);
            } catch (Exception e) {

                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<YouTubeDataModel> items) {

            if (searchResults != null && searchResults.size() != 0) {

                updateVideosFound(searchResults);
            }
        }
    }



    private class AddToFavoritesTask extends AsyncTask <YouTubeDataModel, Void, String> {

        @Override
        protected String doInBackground(YouTubeDataModel... file) {

            try {

                add = YouTubeUtil.insertInFavoritesList(file[0]);
            } catch (Exception e) {

                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String responseCode) {

            if (Integer.parseInt(add) == 200) {

                updateSearchResults(true);
            }
        }
    }



    private class RemoveTask extends AsyncTask <String , Void, String> {

        @Override
        protected String doInBackground(String... videoId) {

            try {

                remove = YouTubeUtil.removeFromFavorites(videoId[0]);
            } catch (Exception e) {

                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String responseCode) {

            if (Integer.parseInt(remove) == 204) {

                updateSearchResults(false);
            }
        }
    }
}