package com.example.cinemabooking.Model;

public class Movie {
    private int movieId;
    private String title;
    private int durationMinutes;
    private String genre;

    public Movie() {
        this.movieId = 0;
        this.title = "";
        this.durationMinutes = 0;
        this.genre = "";
    }

    public Movie(int movieId, String title, int durationMinutes, String genre) {
        this.movieId = movieId;
        this.title = title;
        this.durationMinutes = durationMinutes;
        this.genre = genre;
    }

    public int getMovieId() {
        return movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public String getGenre() {
        return genre;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}

