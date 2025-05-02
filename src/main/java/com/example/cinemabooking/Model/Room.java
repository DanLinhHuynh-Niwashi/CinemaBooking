package com.example.cinemabooking.Model;

public class Room {
    private int roomId;
    private String roomName;
    private int totalSeats;

    public Room() {
        this.roomId = 0;
        this.roomName = "";
        this.totalSeats = 0;
    }

    public Room(int roomId, String roomName, int totalSeats) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.totalSeats = totalSeats;
    }

    public int getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }
}

