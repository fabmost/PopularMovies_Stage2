package com.clicky.popularmovies2.adapters;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.clicky.popularmovies2.data.Movie;
import com.clicky.popularmovies2.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by fabianrodriguez on 9/25/15.
 *
 */
public class MoviesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    public static final int ITEM_MOVIE = 0;
    public static final int ITEM_LOAD = 1;
    public static final int ITEM_EMPTY = 2;

    private List<Movie> mObjects;
    private ViewHolder.itemClickListener listener;
    private boolean isFooterEnabled = false;

    public MoviesAdapter(List<Movie> objects, ViewHolder.itemClickListener listener){
        mObjects = objects;
        this.listener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        private itemClickListener listener;

        public View mView;
        public TextView mTtitle;
        public ImageView mPoster;

        public interface itemClickListener {
            void onItemClicked(int position);
        }

        public ViewHolder(View view, itemClickListener listener) {
            super(view);

            mView = view;
            mPoster = (ImageView)view.findViewById(R.id.img_poster);
            mTtitle = (TextView)view.findViewById(R.id.label_title);

            this.listener = listener;

            mView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v){
            if (listener != null) {
                listener.onItemClicked(getLayoutPosition());
            }
        }

    }

    public static class ProgressViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public ProgressViewHolder(View v) {
            super(v);
            progressBar = (ProgressBar)v.findViewById(R.id.progressBar);
        }
    }

    public static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public TextView mText;

        public EmptyViewHolder(View v) {
            super(v);
            mText = (TextView)v.findViewById(R.id.label_empty);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        if(viewType == ITEM_MOVIE) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_movie_card, parent, false);

            vh = new ViewHolder(v, listener);
        }else if(viewType == ITEM_EMPTY){
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_empty, parent, false);

            vh = new EmptyViewHolder(v);
        }else {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_loading, parent, false);

            vh = new ProgressViewHolder(v);
        }
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder mHolder, int pos) {
        if(mHolder instanceof ViewHolder) {
            ViewHolder holder = (ViewHolder)mHolder;
            final Movie movie = mObjects.get(pos);

            Uri uri = Uri.parse(movie.getPosterUrl());
            final Context mContext = holder.mPoster.getContext();

            holder.mTtitle.setText(movie.getTitle());

            Picasso.with(mContext)
                    .load(uri)
                    .into(holder.mPoster);
        }else if(mHolder instanceof EmptyViewHolder){
            EmptyViewHolder holder = (EmptyViewHolder)mHolder;

            holder.mText.setText(R.string.empty_favorites);
        }

    }

    @Override
    public int getItemCount() {
        if(isFooterEnabled){
            return mObjects.size() +1;
        }else if(mObjects.size() == 0){
            return 1;
        }
        return  mObjects.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(isFooterEnabled && position >= mObjects.size())
            return ITEM_LOAD;
        else if(mObjects.size() == 0)
            return ITEM_EMPTY;
        return ITEM_MOVIE;
    }

    /**
     * Enable or disable footer
     *
     * @param isEnabled boolean to turn on or off footer.
     */
    public void enableFooter(boolean isEnabled){
        this.isFooterEnabled = isEnabled;
    }

    public void addItem(Movie item){
        mObjects.add(item);
        notifyItemInserted(mObjects.size());
    }

    public int removeItem(Movie item){
        int pos = mObjects.indexOf(item);
        mObjects.remove(item);
        notifyItemRemoved(pos);
        return pos;
    }

    public void clearAll(){
        mObjects.clear();
        notifyDataSetChanged();
    }

    public void addList(ArrayList<Movie> list){
        mObjects = list;
        notifyDataSetChanged();
    }

}
