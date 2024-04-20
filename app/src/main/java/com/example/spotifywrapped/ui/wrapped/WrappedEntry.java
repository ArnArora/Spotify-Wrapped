package com.example.spotifywrapped.ui.wrapped;

public class WrappedEntry {
    String date;
    String artistString;
    String trackString;

    public WrappedEntry(String d, String as, String ts) {
        date = d;
        artistString = as;
        trackString = ts;
    }

    public String getArtistString() {
        return artistString;
    }

    public void setArtistString(String artistString) {
        this.artistString = artistString;
    }

    public String getTrackString() {
        return trackString;
    }

    public void setTrackString(String trackString) {
        this.trackString = trackString;
    }


    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
