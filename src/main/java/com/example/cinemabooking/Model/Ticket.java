package com.example.cinemabooking.Model;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Ticket {
    private final int ticketId;
    private final Seat seat;
    private final String customerPhone;
    private final ShowTime showTime;

    private boolean isBooked;

    private final Lock lock;

    public Ticket(int ticketId, Seat seat, String customerPhone, ShowTime showTime) {
        this.ticketId = ticketId;
        this.seat = seat;
        this.customerPhone = customerPhone;
        this.showTime = showTime;
        this.isBooked = false;
        this.lock = new ReentrantLock(); // Initialize the lock
    }

    // Only use for in-app database
    public boolean bookTicket() {
        lock.lock();
        try {
            if (!isBooked) {
                isBooked = true;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    // Only use for in-app database
    public boolean cancelTicket() {
        lock.lock();
        try {
            if (this.isBooked) {
                this.isBooked = false;
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    public void setBooked(boolean booked) {
        isBooked = booked;
    }
    public int getTicketId() {
        return ticketId;
    }

    public Seat getSeat() {
        return seat;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public boolean isBooked() {
        return isBooked;
    }

    public ShowTime getShowTime() {
        return showTime;
    }

    public double getTicketPrice() {
        return showTime.getTicketPrice() * seat.getSeatType().getPriceMultiplier();
    }
}
