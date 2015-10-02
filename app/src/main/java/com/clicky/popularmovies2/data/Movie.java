package com.clicky.popularmovies2.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 *
 * Created by fabianrodriguez on 9/25/15.
 *
 */
public class Movie implements Parcelable{

    private String id;
    private String title;
    private String backgroundUrl;
    private String synopsis;
    private String posterUrl;
    private String releaseDate;
    private double rating;
    private boolean isFavorite;

    public Movie(String id, String title, String backgroundUrl, String synopsis, String posterUrl,
                 String releaseDate, double rating){
        this.id = id;
        this.title = title;
        this.backgroundUrl = backgroundUrl;
        this.synopsis = synopsis;
        this.posterUrl = posterUrl;
        this.releaseDate = releaseDate;
        this.rating = rating;
        isFavorite = false;
    }

    public Movie(Parcel in) {
        this.id = in.readString();
        this.title = in.readString();
        this.backgroundUrl = in.readString();
        this.synopsis = in.readString();
        this.posterUrl = in.readString();
        this.releaseDate = in.readString();
        this.rating = in.readDouble();
    }

    public static final Creator CREATOR = new Creator() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public String getId(){
        return id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBackgroundUrl() {
        return backgroundUrl;
    }

    public void setBackgroundUrl(String backgroundUrl) {
        this.backgroundUrl = backgroundUrl;
    }

    public String getSynopsis() {
        return synopsis;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    public String getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public boolean isFavorite(){
        return isFavorite;
    }

    public void setFavorite(boolean isFavorite){
        this.isFavorite = isFavorite;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(id);
        parcel.writeString(title);
        parcel.writeString(backgroundUrl);
        parcel.writeString(synopsis);
        parcel.writeString(posterUrl);
        parcel.writeString(releaseDate);
        parcel.writeDouble(rating);
    }
}
