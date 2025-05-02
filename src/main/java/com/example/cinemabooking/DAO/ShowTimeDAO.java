package com.example.cinemabooking.DAO;

import com.example.cinemabooking.Model.ShowTime;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class ShowTimeDAO {

    private final Connection connection;
    private final ReentrantLock lock;

    public ShowTimeDAO(Connection connection) {
        this.connection = connection;
        this.lock = new ReentrantLock();
    }

    public int addShowTime(ShowTime showTime) {
        lock.lock();
        try {
            if (isConflictingShowtime(showTime)) {
                return -1;
            }

            String query = "INSERT INTO showtimes (movieId, roomId, startTime, ticketPrice) VALUES (?, ?, ?, ?)";
            try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, showTime.getMovieId());
                statement.setInt(2, showTime.getRoomId());
                statement.setTimestamp(3, Timestamp.valueOf(showTime.getStartTime()));
                statement.setDouble(4, showTime.getTicketPrice());

                int rowsInserted = statement.executeUpdate();
                if (rowsInserted > 0) {
                    try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            return generatedKeys.getInt(1);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return -1;
            }
        } finally {
            lock.unlock();
        }
        return -1;
    }


    private boolean isConflictingShowtime(ShowTime showTime) {
        String query = "SELECT COUNT(*) FROM showtimes WHERE roomId = ? AND startTime = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, showTime.getRoomId());
            statement.setTimestamp(2, Timestamp.valueOf(showTime.getStartTime()));

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int count = resultSet.getInt(1);
                return count > 0;  // If count > 0, there is a conflicting showtime
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    public ShowTime getShowTimeById(int showTimeId) {
        String query = "SELECT * FROM showtimes WHERE showTimeId = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, showTimeId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return toShowTimeDTO(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<ShowTime> getShowTimesByRoomId(int roomId) {
        List<ShowTime> showTimes = new ArrayList<>();
        String query = "SELECT * FROM showtimes WHERE roomId = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, roomId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                showTimes.add(toShowTimeDTO(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return showTimes;
    }

    public List<ShowTime> getShowTimesByMovieId(int movieId) {
        List<ShowTime> showTimes = new ArrayList<>();
        String query = "SELECT * FROM showtimes WHERE movieId = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, movieId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                showTimes.add(toShowTimeDTO(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return showTimes;
    }

    private ShowTime toShowTimeDTO(ResultSet resultSet) throws SQLException {
        int showTimeId = resultSet.getInt("showTimeId");
        int movieId = resultSet.getInt("movieId");
        int roomId = resultSet.getInt("roomId");
        LocalDateTime startTime = resultSet.getTimestamp("startTime").toLocalDateTime();
        double ticketPrice = resultSet.getDouble("ticketPrice");

        return new ShowTime(showTimeId, movieId, roomId, startTime, ticketPrice);
    }
}
