package com.clicky.popularmovies2;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.clicky.popularmovies2.data.FavoritesContentProvider;
import com.clicky.popularmovies2.data.FavoritesTable;
import com.clicky.popularmovies2.data.Movie;
import com.clicky.popularmovies2.fragments.MovieDetailsFragment;
import com.clicky.popularmovies2.fragments.MovieListFragment;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements MovieListFragment.Callbacks,
        MovieDetailsFragment.Callbacks, LoaderManager.LoaderCallbacks<Cursor> {

    private static final int DETAILS_ACTIVITY = 1;
    private boolean mTwoPane;
    private boolean favorites = false;
    private boolean updateFavorites = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (findViewById(R.id.item_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;
            getSupportActionBar().setElevation(0.0f);

            // In two-pane mode, list items should be given the
            // 'activated' state when touched.

            ((MovieListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.item_list))
                    .twoPaneMode(true);

        }

        this.getLoaderManager();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_popular:
                updateFavorites = false;
                ((MovieListFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.item_list)).changeMovies(0);
                return true;
            case R.id.item_rating:
                updateFavorites = false;
                ((MovieListFragment) getSupportFragmentManager()
                        .findFragmentById(R.id.item_list)).changeMovies(1);
                return true;
            case R.id.item_favorites:
                favorites = true;
                updateFavorites = true;
                getLoaderManager().initLoader(0, null, this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(Movie movie) {
        if (mTwoPane) {
            if(movie != null) {
                Bundle arguments = new Bundle();
                arguments.putParcelable(MovieDetailsFragment.EXTRA_MOVIE, movie);
                MovieDetailsFragment fragment = new MovieDetailsFragment();
                fragment.setArguments(arguments);
                fragment.twoPaneMode(true);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.item_detail_container, fragment)
                        .commit();
            }else{
                getSupportFragmentManager().beginTransaction()
                        .remove(getSupportFragmentManager()
                                .findFragmentById(R.id.item_detail_container))
                        .commit();
            }

        } else {
            // In single-pane mode, simply start the detail activity
            // for the selected item ID.
            Intent detailIntent = new Intent(this, MovieDetailsActivity.class);
            detailIntent.putExtra(MovieDetailsFragment.EXTRA_MOVIE, movie);
            startActivityForResult(detailIntent, DETAILS_ACTIVITY);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == DETAILS_ACTIVITY && resultCode == RESULT_OK){
            if(updateFavorites) {
                favorites = true;
                getLoaderManager().initLoader(0, null, this);
            }
        }
    }

    @Override
    public void updateFavorites(Movie movie){
        ((MovieListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.item_list)).removeFavorite(movie);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String[] projection = { FavoritesTable.COLUMN_ID, FavoritesTable.COLUMN_TITLE,
            FavoritesTable.COLUMN_SYNOPSIS, FavoritesTable.COLUMN_DATE, FavoritesTable.COLUMN_RATE,
            FavoritesTable.COLUMN_POSTER, FavoritesTable.COLUMN_BACK};
        return new CursorLoader(this,
                FavoritesContentProvider.CONTENT_URI, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        ArrayList<Movie> mMovies = new ArrayList<>();
        if(favorites) {
            if (data.moveToFirst()) {
                do {
                    String id = data.getString(data.getColumnIndexOrThrow(FavoritesTable.COLUMN_ID));
                    String title = data.getString(data.getColumnIndexOrThrow(FavoritesTable.COLUMN_TITLE));
                    String back = data.getString(data.getColumnIndexOrThrow(FavoritesTable.COLUMN_BACK));
                    String synopsis = data.getString(data.getColumnIndexOrThrow(FavoritesTable.COLUMN_SYNOPSIS));
                    String poster = data.getString(data.getColumnIndexOrThrow(FavoritesTable.COLUMN_POSTER));
                    String date = data.getString(data.getColumnIndexOrThrow(FavoritesTable.COLUMN_DATE));
                    double rate = data.getDouble(data.getColumnIndexOrThrow(FavoritesTable.COLUMN_RATE));

                    Movie mov = new Movie(id, title, back, synopsis, poster, date, rate);
                    mMovies.add(mov);

                } while (data.moveToNext());
            }
            ((MovieListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.item_list))
                    .setFavorites(mMovies);
            favorites = false;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // data is not available anymore, delete reference
        //adapter.swapCursor(null);
    }

}
