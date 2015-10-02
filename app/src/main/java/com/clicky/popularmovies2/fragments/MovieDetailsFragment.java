package com.clicky.popularmovies2.fragments;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.clicky.popularmovies2.MoviesApplication;
import com.clicky.popularmovies2.R;
import com.clicky.popularmovies2.data.FavoritesContentProvider;
import com.clicky.popularmovies2.data.FavoritesTable;
import com.clicky.popularmovies2.data.Movie;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * Created by fabianrodriguez on 9/27/15.
 *
 */
public class MovieDetailsFragment extends Fragment{

    public static final String EXTRA_MOVIE = "movie";
    private Movie mMovie;
    private LinearLayout layoutTrailers, layoutComments;
    private ImageButton btnFavorite;
    private boolean twoPaneMode = false;
    private String trailerUrl = "";

    private Callbacks mCallbacks;
    private DetailCallback mDetailCallbacks;

    public interface Callbacks {
        void updateFavorites(Movie movie);
    }

    public interface DetailCallback {
        void updateFavorites();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        if (getArguments().containsKey(EXTRA_MOVIE)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
            mMovie = getArguments().getParcelable(EXTRA_MOVIE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);

        // Show the dummy content as text in a TextView.
        if (mMovie != null) {
            ImageView img_back = (ImageView)rootView.findViewById(R.id.backdrop);
            ImageView imgPoster = (ImageView)rootView.findViewById(R.id.img_poster);
            TextView labelTitle = (TextView)rootView.findViewById(R.id.label_title);
            TextView labelRelease = (TextView)rootView.findViewById(R.id.label_release);
            TextView labelRate = (TextView)rootView.findViewById(R.id.label_rate);
            TextView labelSynopsis = (TextView)rootView.findViewById(R.id.label_synopsis);
            btnFavorite = (ImageButton)rootView.findViewById(R.id.btn_favorite);

            layoutTrailers = (LinearLayout)rootView.findViewById(R.id.layout_trailers);
            layoutComments = (LinearLayout)rootView.findViewById(R.id.layout_reviews);

            getTrailers(mMovie.getId());
            getReviews(mMovie.getId());

            Uri uri = Uri.parse(FavoritesContentProvider.CONTENT_URI + "/" + mMovie.getId());
            String[] projection = { FavoritesTable.COLUMN_ID };
            Cursor cursor = getActivity().getContentResolver().query(uri, projection, null, null,
                    null);
            if (cursor != null) {
                if(cursor.moveToFirst()) {
                    mMovie.setFavorite(true);
                    btnFavorite.setImageResource(R.drawable.ic_favorite_black_24dp);
                }
                cursor.close();
            }

            labelTitle.setText(mMovie.getTitle());
            labelRelease.setText(mMovie.getReleaseDate());
            labelRate.setText(String.valueOf(mMovie.getRating()));

            if(mMovie.getSynopsis() != null && !mMovie.getSynopsis().equals("null"))
                labelSynopsis.setText(mMovie.getSynopsis());
            else
                labelSynopsis.setText(R.string.no_synopsis);

            Picasso.with(getActivity())
                    .load(Uri.parse(mMovie.getBackgroundUrl()))
                    .into(img_back);

            Picasso.with(getActivity())
                    .load(Uri.parse(mMovie.getPosterUrl()))
                    .into(imgPoster);

            btnFavorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addRemoveFavorites();
                }
            });

        }

        return rootView;
    }

    @Override
    public void onAttach(Context mContext) {
        super.onAttach(mContext);

        if(twoPaneMode){
            Activity activity;

            if (mContext instanceof Activity) {
                activity = (Activity) mContext;

                if (!(activity instanceof Callbacks)) {
                    throw new IllegalStateException("Activity must implement fragment's callbacks.");
                }

                mCallbacks = (Callbacks) activity;
            }
        }else{
            Activity activity;

            if (mContext instanceof Activity) {
                activity = (Activity) mContext;

                if (!(activity instanceof DetailCallback)) {
                    throw new IllegalStateException("Activity must implement fragment's callbacks.");
                }

                mDetailCallbacks = (DetailCallback) activity;
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if(twoPaneMode)
            mCallbacks = null;
        else
            mDetailCallbacks = null;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_details, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_share:
                if(!trailerUrl.isEmpty()){
                    shareMainTrailer(trailerUrl);
                }else{
                    Toast.makeText(getActivity(), R.string.empty_share, Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void shareMainTrailer(String key){
        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("text/plain");

        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + " " + mMovie.getTitle() + " - https://youtu.be/" + key);
        startActivity(Intent.createChooser(intent, "Choose an app to share this trailer"));
    }

    public void twoPaneMode(boolean twoPaneMode){
        this.twoPaneMode = twoPaneMode;
    }

    private void getTrailers(String movieId){

        String MOVIE_URL =
                "http://api.themoviedb.org/3/movie/" + movieId +
                        "/videos?api_key=" + getResources().getString(R.string.movieDBAPI);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                MOVIE_URL, (String)null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                final String JSON_RESULTS = "results";
                final String JSON_KEY = "key";
                final String JSON_NAME = "name";

                try {
                    // Parsing json object response
                    // response will be a json object
                    JSONArray moviesArray = response.getJSONArray(JSON_RESULTS);

                    if(moviesArray.length() == 0)
                        addEmpty(0);
                    else {
                        for (int i = 0; i < moviesArray.length(); i++) {
                            String key;
                            String name;

                            // Get the JSON object representing the day
                            JSONObject movieJSON = moviesArray.getJSONObject(i);

                            key = movieJSON.getString(JSON_KEY);
                            name = movieJSON.getString(JSON_NAME);

                            if(i == 0)
                                trailerUrl = key;

                            addTrailer(name, key);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Volley", "Error: " + error.getMessage());
                Toast.makeText(getActivity(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        MoviesApplication.getInstance().addToRequestQueue(jsonObjReq);
    }

    private void getReviews(String movieId){

        String MOVIE_URL =
                "http://api.themoviedb.org/3/movie/" + movieId +
                        "/reviews?api_key=" + getResources().getString(R.string.movieDBAPI);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                MOVIE_URL, (String)null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                final String JSON_RESULTS = "results";
                final String JSON_NAME = "author";
                final String JSON_CONTENT = "content";

                try {
                    // Parsing json object response
                    // response will be a json object
                    JSONArray moviesArray = response.getJSONArray(JSON_RESULTS);

                    if(moviesArray.length() == 0)
                        addEmpty(1);
                    else {
                        for (int i = 0; i < moviesArray.length(); i++) {
                            String content;
                            String name;

                            // Get the JSON object representing the day
                            JSONObject movieJSON = moviesArray.getJSONObject(i);

                            content = movieJSON.getString(JSON_CONTENT);
                            name = movieJSON.getString(JSON_NAME);

                            addReview(name, content);
                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Volley", "Error: " + error.getMessage());
                Toast.makeText(getActivity(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        MoviesApplication.getInstance().addToRequestQueue(jsonObjReq);
    }

    private void addTrailer(String name, final String key){
        View trailerView = LayoutInflater.from(getActivity()).inflate(R.layout.item_trailer, null);
        ImageView imgTrailer = (ImageView)trailerView.findViewById(R.id.img_trailer);
        TextView labelTrailer = (TextView)trailerView.findViewById(R.id.label_name);
        ImageButton btnShare = (ImageButton)trailerView.findViewById(R.id.btn_share);

        Picasso.with(getActivity())
                .load("http://img.youtube.com/vi/" + key + "/hqdefault.jpg")
                .into(imgTrailer);
        labelTrailer.setText(name);

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(android.content.Intent.ACTION_SEND);
                intent.setType("text/plain");

                intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text) + " " + mMovie.getTitle() + " - https://youtu.be/" + key);
                startActivity(Intent.createChooser(intent, "Choose an app to share this trailer"));
            }
        });

        trailerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse("https://www.youtube.com/watch?v=" + key));
                startActivity(i);
            }
        });

        layoutTrailers.addView(trailerView);
    }

    private void addReview(String name, String content){
        View trailerView = LayoutInflater.from(getActivity()).inflate(R.layout.item_review, null);
        TextView labelAuthor = (TextView)trailerView.findViewById(R.id.label_author);
        TextView labelContent = (TextView)trailerView.findViewById(R.id.label_content);

        labelAuthor.setText(name);
        labelContent.setText(content);

        layoutComments.addView(trailerView);
    }

    private void addEmpty(int type){
        View emptyView = LayoutInflater.from(getActivity()).inflate(R.layout.item_empty, null);
        TextView labelEmpty = (TextView)emptyView.findViewById(R.id.label_empty);
        switch (type){
            case 0:
                labelEmpty.setText(R.string.empty_trailers);
                layoutTrailers.addView(emptyView);
                break;
            case 1:
                labelEmpty.setText(R.string.empty_reviews);
                layoutComments.addView(emptyView);
                break;
        }
    }

    private void addRemoveFavorites(){
        if(!mMovie.isFavorite()) {
            ContentValues values = new ContentValues();
            values.put(FavoritesTable.COLUMN_ID, mMovie.getId());
            values.put(FavoritesTable.COLUMN_TITLE, mMovie.getTitle());
            values.put(FavoritesTable.COLUMN_SYNOPSIS, mMovie.getSynopsis());
            values.put(FavoritesTable.COLUMN_DATE, mMovie.getReleaseDate());
            values.put(FavoritesTable.COLUMN_RATE, mMovie.getRating());
            values.put(FavoritesTable.COLUMN_POSTER, mMovie.getPosterUrl());
            values.put(FavoritesTable.COLUMN_BACK, mMovie.getBackgroundUrl());

            getActivity().getContentResolver().insert(FavoritesContentProvider.CONTENT_URI, values);

            mMovie.setFavorite(true);
            btnFavorite.setImageResource(R.drawable.ic_favorite_black_24dp);

            if(!twoPaneMode)
                mDetailCallbacks.updateFavorites();

            Toast.makeText(getActivity(), "Added " + mMovie.getTitle() + " to your favorites", Toast.LENGTH_SHORT).show();
        }else{
            Uri uri = Uri.parse(FavoritesContentProvider.CONTENT_URI + "/"
                    + mMovie.getId());
            getActivity().getContentResolver().delete(uri, null, null);

            mMovie.setFavorite(false);
            btnFavorite.setImageResource(R.drawable.ic_favorite_border_black_24dp);

            if(twoPaneMode)
                mCallbacks.updateFavorites(mMovie);
            else
                mDetailCallbacks.updateFavorites();

            Toast.makeText(getActivity(), "Removed " + mMovie.getTitle() + " from your favorites", Toast.LENGTH_SHORT).show();
        }

    }

}
