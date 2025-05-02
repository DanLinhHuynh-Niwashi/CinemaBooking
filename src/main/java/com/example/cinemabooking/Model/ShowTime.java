package com.example.cinemabooking.Model;

import java.time.LocalDateTime;

public class ShowTime {
    private int showTimeId;
    private int movieId;
    private int roomId;
    private LocalDateTime startTime;
    private double ticketPrice;

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setTicketPrice(double ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public ShowTime() {
        this.showTimeId = 0;
        this.movieId = 0;
        this.roomId = 0;
        this.startTime = null;
        this.ticketPrice = 0;
    }
    public ShowTime(int showTimeId, int movieId, int roomId, LocalDateTime startTime, double ticketPrice) {
        this.showTimeId = showTimeId;
        this.movieId = movieId;
        this.roomId = roomId;
        this.startTime = startTime;
        this.ticketPrice = ticketPrice;
    }

    public int getShowTimeId() {
        return showTimeId;
    }

    public int getMovieId() {
        return movieId;
    }

    public int getRoomId() {
        return roomId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public double getTicketPrice() {
        return ticketPrice;
    }

    public void setShowTimeId(int showTimeId) {
        this.showTimeId = showTimeId;
    }
}
