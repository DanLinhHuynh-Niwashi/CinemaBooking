package com.example.cinemabooking.Model;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Seat {
    private int seatId;
    private String seatName;
    private int roomId;
    private SeatType seatType;
    private int row;
    private int column;

    public Seat(int seatId, String seatName, int roomId, SeatType seatType, int row, int column) {
        this.seatId = seatId;
        this.seatName = seatName;
        this.roomId = roomId;
        this.seatType = seatType;
        this.row = row;
        this.column = column;
    }

    public int getSeatId() {
        return seatId;
    }

    public String getSeatName() {
        return seatName;
    }

    public int getRoomId() {
        return roomId;
    }

    public SeatType getSeatType() {
        return seatType;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }
}
