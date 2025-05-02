package com.example.cinemabooking.DAO;

import com.example.cinemabooking.Model.Ticket;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class TicketDAOTest {

    private Connection connection;
    private TicketDAO ticketDAO;

    @BeforeEach
    void setUp() throws Exception {
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb2;DB_CLOSE_DELAY=-1");

        Statement stmt = connection.createStatement();

        stmt.execute("CREATE TABLE seats (" +
                "seatId INT PRIMARY KEY, " +
                "seatName VARCHAR(10), " +
                "roomId INT, " +
                "seatType VARCHAR(20), " +
                "`row` INT, " +
                "`column` INT)");

        stmt.execute("CREATE TABLE showtimes (" +
                "showTimeId INT PRIMARY KEY, " +
                "movieId INT, " +
                "roomId INT, " +
                "startTime DATETIME, " +
                "ticketPrice DECIMAL(10, 2))");

        PreparedStatement insertSeats = connection.prepareStatement("INSERT INTO seats (seatId, seatName, roomId, seatType, `row`, `column`) VALUES (?, ?, ?, ?, ?, ?)");
        for (int i = 1; i <= 56; i++) {
            insertSeats.setInt(1, i);
            insertSeats.setString(2, "A" + i);
            insertSeats.setInt(3, 1);
            insertSeats.setString(4,"VIP");
            insertSeats.setInt(5, 1);
            insertSeats.setInt(6, i);
            insertSeats.addBatch();
        }
        insertSeats.executeBatch();

        PreparedStatement insertShowtimes = connection.prepareStatement("INSERT INTO showtimes (showTimeId, movieId, roomId, startTime, ticketPrice) VALUES (?, ?, ?, ?, ?)");
        insertShowtimes.setInt(1, 1);
        insertShowtimes.setInt(2, 1);
        insertShowtimes.setInt(3, 1);
        insertShowtimes.setTimestamp(4, Timestamp.valueOf("2025-05-02 20:00:00"));
        insertShowtimes.setBigDecimal(5, new BigDecimal("12.50"));
        insertShowtimes.executeUpdate();

        insertShowtimes.setInt(1, 2);  // showTimeId
        insertShowtimes.setInt(2, 2);  // movieId (just a placeholder)
        insertShowtimes.setInt(3, 2);  // roomId (assume Room 2)
        insertShowtimes.setTimestamp(4, Timestamp.valueOf("2025-05-03 15:00:00"));
        insertShowtimes.setBigDecimal(5, new BigDecimal("15.00"));
        insertShowtimes.executeUpdate();

        stmt.execute("CREATE TABLE tickets (" +
                "ticketId INT PRIMARY KEY, " +
                "seatId INT, " +
                "customerPhone VARCHAR(15), " +
                "showTimeId INT, " +
                "isBooked BOOLEAN DEFAULT FALSE, " +
                "FOREIGN KEY (seatId) REFERENCES seats(seatId), " +
                "FOREIGN KEY (showTimeId) REFERENCES showtimes(showTimeId), " +
                "UNIQUE (seatId, showTimeId))");

        PreparedStatement insertTickets = connection.prepareStatement(
                "INSERT INTO tickets (ticketId, seatId, customerPhone, showTimeId, isBooked) VALUES (?, ?, NULL, 1, FALSE)");
        int ticketId = 1;
        for (int seatId = 1; seatId <= 56; seatId++, ticketId++) {
            insertTickets.setInt(1, ticketId);
            insertTickets.setInt(2, seatId);
            insertTickets.addBatch();
        }
        insertTickets.executeBatch();

        ticketDAO = new TicketDAO(connection);
    }

    @AfterEach
    void tearDown() throws Exception {
        connection.close();
    }

    @Test
    void testConcurrentBookingAndCancellation() throws Exception {
        try (PreparedStatement stmt = connection.prepareStatement(
                "UPDATE tickets SET isBooked = FALSE, customerPhone = NULL WHERE ticketId BETWEEN 45 AND 100")) {
            stmt.executeUpdate();
        }

        List<Integer> allTickets = new ArrayList<>();
        for (int i = 1; i <= 56; i++) allTickets.add(i);

        Map<String, List<Integer>> userSuccessfulBookings = new ConcurrentHashMap<>();
        Map<String, List<Integer>> userCancelledBookings = new ConcurrentHashMap<>();
        List<Thread> allThreads = new ArrayList<>();

        for (int i = 0; i < 8; i++) {
            String phone = "user" + (i + 1) + "Phone";
            List<Integer> ticketsToBook = new ArrayList<>(allTickets);
            Collections.shuffle(ticketsToBook);

            Thread bookingThread = new Thread(() -> {
                for (Integer ticketId : ticketsToBook) {
                    try {
                        Thread.sleep(new Random().nextInt(10));
                        boolean success = ticketDAO.bookTicket(ticketId, phone);
                        if (success) {
                            userSuccessfulBookings
                                    .computeIfAbsent(phone, k -> Collections.synchronizedList(new ArrayList<>()))
                                    .add(ticketId);
                            System.out.println("[BOOKED] " + phone + " booked ticket " + ticketId);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            Thread cancelThread = new Thread(() -> {
                int count = 0;
                while (count < 2) {
                    try {
                        Thread.sleep(150);
                        while (!userSuccessfulBookings.containsKey(phone)) {
                            Thread.sleep(20);
                        }

                        List<Integer> toCancel;
                        synchronized (userSuccessfulBookings) {
                            toCancel = new ArrayList<>(userSuccessfulBookings.get(phone));
                        }

                        synchronized (userCancelledBookings) {
                            if (userCancelledBookings.containsKey(phone)) {
                                List<Integer> cancelledTickets = userCancelledBookings.get(phone);
                                toCancel.removeAll(cancelledTickets);
                            }
                        }
                        Collections.shuffle(toCancel);
                        toCancel = toCancel.stream().limit(3).toList();

                        for (Integer ticketId : toCancel) {
                            boolean cancelled = ticketDAO.cancelTicket(ticketId, phone);
                            if (cancelled) {
                                userCancelledBookings
                                        .computeIfAbsent(phone, k -> Collections.synchronizedList(new ArrayList<>()))
                                        .add(ticketId);
                                System.out.println("[CANCELLED] " + phone + " cancelled ticket " + ticketId);
                            } else {
                                System.out.println("[CANCEL FAIL] " + phone + " failed to cancel ticket " + ticketId);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    count++;
                }
            });

            allThreads.add(bookingThread);
            allThreads.add(cancelThread);

            bookingThread.start();
            cancelThread.start();
        }

        for (Thread t : allThreads) {
            t.join();
        }

        int bookingCount = 0;
        System.out.println("\n=== FINAL BOOKING STATUS ===");
        for (int ticketId = 1; ticketId <= 56; ticketId++) {
            Ticket t = ticketDAO.getTicketById(ticketId);
            if (t != null && t.isBooked()) {
                System.out.println("Ticket " + ticketId + " is booked by " + t.getCustomerPhone());
                bookingCount++;
            }
        }

        System.out.println("\n=== USER BOOKINGS AFTER CANCELLATION ===");
        for (Map.Entry<String, List<Integer>> entry : userSuccessfulBookings.entrySet()) {
            System.out.println(entry.getKey() + " successfully booked: " + entry.getValue());
        }
        System.out.println("\n=== USER CANCELLED TICKETS ===");
        for (Map.Entry<String, List<Integer>> entry : userCancelledBookings.entrySet()) {
            System.out.println(entry.getKey() + " cancelled: " + entry.getValue());
        }

        int finalBookingCount = 0;
        for (Map.Entry<String, List<Integer>> entry : userSuccessfulBookings.entrySet()) {
            String phone = entry.getKey();
            List<Integer> bookedTickets = entry.getValue();
            List<Integer> cancelledTickets = userCancelledBookings.getOrDefault(phone, Collections.emptyList());

            List<Integer> finalBookings = new ArrayList<>(bookedTickets);
            finalBookings.removeAll(cancelledTickets);

            finalBookingCount += finalBookings.size();
        }

        System.out.println("Total recorded final booked tickets: " + finalBookingCount);
        System.out.println("Total seat booked in database: " + bookingCount);

        assertEquals(bookingCount, finalBookingCount, "Database booking count should match final booking record.");
    }
}
