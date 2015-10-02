package com.clicky.popularmovies2.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.clicky.popularmovies2.MoviesApplication;
import com.clicky.popularmovies2.MoviesDecoration;
import com.clicky.popularmovies2.R;
import com.clicky.popularmovies2.adapters.MoviesAdapter;
import com.clicky.popularmovies2.data.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 *
 * Created by fabianrodriguez on 9/27/15.
 *
 */
public class MovieListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        MoviesAdapter.ViewHolder.itemClickListener{

    private static final String SCROLL_STATE = "scrolled";
    private static final String LIST_STATE = "listItems";
    private static final String SORT_STATE = "sort";

    private Callbacks mCallbacks;

    private RecyclerView list;
    private SwipeRefreshLayout swipeLayout;
    private GridLayoutManager mLayoutManager;
    private MoviesAdapter adapter;
    private String sortTypes[] = {"popularity.desc","vote_average.desc"};
    private ArrayList<Movie> mMovies;
    private int page = 1;
    private int sortSelected = 0;
    private int previousTotal = 0;
    private boolean loading = true;
    private boolean twoPaneMode = false;
    private int visibleThreshold = 5;
    int firstVisibleItem, visibleItemCount, totalItemCount;

    public interface Callbacks {
        void onItemSelected(Movie movie);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMovies = new ArrayList<>();

        adapter = new MoviesAdapter(mMovies, this);
        mLayoutManager = new GridLayoutManager(getActivity(), getResources().getInteger(R.integer.grid_columns));
        mLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (adapter.getItemViewType(position)) {
                    case MoviesAdapter.ITEM_MOVIE:
                        return 1;
                    case MoviesAdapter.ITEM_LOAD:
                        return 2;
                    case MoviesAdapter.ITEM_EMPTY:
                        return 2;
                    default:
                        return -1;
                }
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_movie_list, container, false);

        list = (RecyclerView)rootView.findViewById(R.id.list);
        swipeLayout = (SwipeRefreshLayout)rootView.findViewById(R.id.swipeContainer);

        swipeLayout.setColorSchemeResources(R.color.primary);
        swipeLayout.setOnRefreshListener(this);

        list.setHasFixedSize(true);
        list.addItemDecoration(new MoviesDecoration(getResources().getDimensionPixelSize(R.dimen.grid_spacing),
                getResources().getInteger(R.integer.grid_columns)));
        list.setLayoutManager(mLayoutManager);
        list.setAdapter(adapter);

        list.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = list.getChildCount();
                totalItemCount = mLayoutManager.getItemCount();
                firstVisibleItem = mLayoutManager.findFirstVisibleItemPosition();

                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount)
                        <= (firstVisibleItem + visibleThreshold)) {

                    if (sortSelected != 3) {
                        page++;
                        getMovies(sortTypes[sortSelected]);
                        loading = true;
                    }
                }
            }
        });

        if (savedInstanceState != null) {
            sortSelected = savedInstanceState.getInt(SORT_STATE);
            mMovies = savedInstanceState.getParcelableArrayList(LIST_STATE);
            adapter.addList(mMovies);
            mLayoutManager.scrollToPosition(savedInstanceState.getInt(SCROLL_STATE));

        }
        if(mMovies.size() == 0)
            getMovies(sortTypes[sortSelected]);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putParcelableArrayList(LIST_STATE, mMovies);
        savedInstanceState.putInt(SCROLL_STATE, mLayoutManager.findFirstVisibleItemPosition());
        savedInstanceState.putInt(SORT_STATE, sortSelected);
    }

    @Override
    public void onAttach(Context mContext) {
        super.onAttach(mContext);

        Activity activity;

        if (mContext instanceof Activity) {
            activity = (Activity) mContext;

            if (!(activity instanceof Callbacks)) {
                throw new IllegalStateException("Activity must implement fragment's callbacks.");
            }

            mCallbacks = (Callbacks) activity;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        mCallbacks = null;
    }

    @Override
    public void onRefresh() {
        adapter.clearAll();
        page = 1;
        getMovies(sortTypes[sortSelected]);
    }

    @Override
    public void onItemClicked(int pos) {
        if(pos != -1)
            mCallbacks.onItemSelected(mMovies.get(pos));
        else
            mCallbacks.onItemSelected(null);
    }

    public void twoPaneMode(boolean twoPaneMode){
        this.twoPaneMode = twoPaneMode;
    }

    private void getMovies(String sort){

        adapter.enableFooter(true);

        String MOVIE_URL =
                "http://api.themoviedb.org/3/discover/movie?page=" + page +"&sort_by="
                        + sort + "&api_key=" + getResources().getString(R.string.movieDBAPI);

        JsonObjectRequest jsonObjReq = new JsonObjectRequest(Request.Method.GET,
                MOVIE_URL, (String)null, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                final String JSON_RESULTS = "results";
                final String JSON_ID = "id";
                final String JSON_BACKGROUND = "backdrop_path";
                final String JSON_TITLE = "original_title";
                final String JSON_SYNOPSIS = "overview";
                final String JSON_DATE = "release_date";
                final String JSON_POSTER = "poster_path";
                final String JSON_RATE = "vote_average";

                try {
                    // Parsing json object response
                    // response will be a json object
                    JSONArray moviesArray = response.getJSONArray(JSON_RESULTS);

                    for(int i = 0; i < moviesArray.length(); i++) {
                        String id;
                        String title;
                        String synopsis;
                        String date;
                        String posterUrl;
                        String backgroundUrl;
                        double rate;

                        // Get the JSON object representing the day
                        JSONObject movieJSON = moviesArray.getJSONObject(i);

                        id = movieJSON.getString(JSON_ID);
                        title = movieJSON.getString(JSON_TITLE);
                        synopsis = movieJSON.getString(JSON_SYNOPSIS);
                        date = movieJSON.getString(JSON_DATE);
                        posterUrl = movieJSON.getString(JSON_POSTER);
                        backgroundUrl = movieJSON.getString(JSON_BACKGROUND);
                        rate = movieJSON.getDouble(JSON_RATE);

                        Movie movie = new Movie(id, title, "http://image.tmdb.org/t/p/w780" + backgroundUrl, synopsis,
                                "http://image.tmdb.org/t/p/w185" + posterUrl, date, rate);

                        adapter.addItem(movie);
                    }


                    //txtResponse.setText(jsonResponse);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getActivity(),
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                }

                adapter.enableFooter(false);
                swipeLayout.setRefreshing(false);

                if(twoPaneMode && page == 1){
                    onItemClicked(0);
                }
            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.d("Volley", "Error: " + error.getMessage());
                Toast.makeText(getActivity(),
                        error.getMessage(), Toast.LENGTH_SHORT).show();
                adapter.enableFooter(false);
                swipeLayout.setRefreshing(false);
            }
        });

        MoviesApplication.getInstance().addToRequestQueue(jsonObjReq);
    }

    public void setFavorites(ArrayList<Movie> mMovies){
        if(sortSelected != 3) {
            sortSelected = 3;
            swipeLayout.setEnabled(false);
            this.mMovies = mMovies;
            adapter.clearAll();
            adapter.addList(mMovies);

            if (twoPaneMode && mMovies.size() >= 1) {
                onItemClicked(0);
            }else if(twoPaneMode && mMovies.size() == 0){
                onItemClicked(-1);
            }
        }else if(!twoPaneMode){
            this.mMovies = mMovies;
            adapter.clearAll();
            adapter.addList(mMovies);
        }
    }

    public void changeMovies(int type){
        if(sortSelected != type) {
            swipeLayout.setEnabled(true);
            adapter.enableFooter(false);
            sortSelected = type;
            adapter.clearAll();
            page = 1;
            getMovies(sortTypes[sortSelected]);
        }
    }

    public void removeFavorite(Movie movie){
        if(sortSelected == 3) {
            int newPos = adapter.removeItem(movie);
            if(mMovies.size() == 0){
                onItemClicked(-1);
            }else {
                if (newPos < mMovies.size())
                    onItemClicked(newPos);
                else
                    onItemClicked(newPos - 1);
            }
        }
    }

}
