package com.example.cinemabooking.DAO;

import com.example.cinemabooking.Model.ShowTime;
import com.example.cinemabooking.Model.Ticket;
import com.example.cinemabooking.Model.Seat;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TicketDAO {
    private final Connection connection;
    private final Lock lock;

    public TicketDAO(Connection connection) {
        this.connection = connection;
        this.lock = new ReentrantLock();
    }

    // Disable database lock to depend only on thread lock
    public boolean bookTicket(int ticketId, String customerPhone) {
//        String selectQuery = "SELECT isBooked FROM tickets WHERE ticketId = ? FOR UPDATE";
        String selectQuery = "SELECT isBooked FROM tickets WHERE ticketId = ?";
        String updateQuery = "UPDATE tickets SET isBooked = ?, customerPhone = ? WHERE ticketId = ?";

        lock.lock();
//        try {
//            //connection.setAutoCommit(false);

        try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
            selectStmt.setInt(1, ticketId);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                boolean isBooked = rs.getBoolean("isBooked");
                if (isBooked) {
                    //connection.rollback();
                    return false;
                }

                try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                    updateStmt.setBoolean(1, true);
                    updateStmt.setString(2, customerPhone);
                    updateStmt.setInt(3, ticketId);
                    int updated = updateStmt.executeUpdate();

                    if (updated > 0) {
                        //connection.commit();
                        return true;
                    }
                }
            }

//                connection.rollback();
//            } catch (SQLException e) {
//                connection.rollback();
//                throw e;
//            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
//            try {
//                connection.setAutoCommit(true);
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
            lock.unlock();
        }
        return false;
    }

    public boolean cancelTicket(int ticketId, String customerPhone) {
//        String selectQuery = "SELECT isBooked, customerPhone FROM tickets WHERE ticketId = ? FOR UPDATE";
        String selectQuery = "SELECT isBooked, customerPhone FROM tickets WHERE ticketId = ?";
        String updateQuery = "UPDATE tickets SET isBooked = FALSE, customerPhone = NULL WHERE ticketId = ?";

        lock.lock();
//        try {
//            connection.setAutoCommit(false);

        try (PreparedStatement selectStmt = connection.prepareStatement(selectQuery)) {
            selectStmt.setInt(1, ticketId);
            ResultSet rs = selectStmt.executeQuery();

            if (rs.next()) {
                boolean isBooked = rs.getBoolean("isBooked");
                String bookedPhone = rs.getString("customerPhone");

                // Ensure ticket is booked and by the same user
                if (!isBooked || !customerPhone.equals(bookedPhone)) {
//                        connection.rollback();
                    return false;
                }

                try (PreparedStatement updateStmt = connection.prepareStatement(updateQuery)) {
                    updateStmt.setInt(1, ticketId);
                    int updated = updateStmt.executeUpdate();

                    if (updated > 0) {
//                            connection.commit();
                        return true;
                    }
                }
            }

//                connection.rollback();
//            } catch (SQLException e) {
//                connection.rollback();
//                throw e;
//            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
//            try {
//                connection.setAutoCommit(true);
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
            lock.unlock();
        }
        return false;
    }


    public boolean addTicket(Ticket ticket) {
        String query = "INSERT INTO tickets (seatId, customerPhone, showTimeId, isBooked) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, ticket.getSeat().getSeatId());
            statement.setString(2, ticket.getCustomerPhone());
            statement.setInt(3, ticket.getShowTime().getShowTimeId());
            statement.setBoolean(4, ticket.isBooked());

            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Ticket getTicketById(int ticketId) {
        String query = "SELECT * FROM tickets WHERE ticketId = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, ticketId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return toTicketDTO(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Ticket> getTicketsByCustomerPhone(String customerPhone) {
        List<Ticket> tickets = new ArrayList<>();
        String query = "SELECT * FROM tickets WHERE customerPhone = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, customerPhone);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                tickets.add(toTicketDTO(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tickets;
    }

    public List<Ticket> getTicketsByShowTimeId(int showTimeId) {
        List<Ticket> tickets = new ArrayList<>();
        String query = "SELECT * FROM tickets WHERE showTimeId = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, showTimeId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                tickets.add(toTicketDTO(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tickets;
    }

    private Ticket toTicketDTO(ResultSet resultSet) throws SQLException {
        int ticketId = resultSet.getInt("ticketId");
        int seatId = resultSet.getInt("seatId");
        String customerPhone = resultSet.getString("customerPhone");
        int showTimeId = resultSet.getInt("showTimeId");
        boolean isBooked = resultSet.getBoolean("isBooked");

        Seat seat = new SeatDAO(connection).getSeatById(seatId);
        ShowTime showTime = new ShowTimeDAO(connection).getShowTimeById(showTimeId);

        Ticket ticket = new Ticket(ticketId, seat, customerPhone, showTime);
        ticket.setBooked(isBooked);
        return ticket;
    }
}
