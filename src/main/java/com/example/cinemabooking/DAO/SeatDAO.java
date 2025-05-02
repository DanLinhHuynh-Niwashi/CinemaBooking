package com.example.cinemabooking.DAO;

import com.example.cinemabooking.Model.Seat;
import com.example.cinemabooking.Model.SeatType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SeatDAO {
    private final Connection connection;

    public SeatDAO(Connection connection) {
        this.connection = connection;
    }

    public boolean addSeat(Seat seat) {
        String query = "INSERT INTO seats (seatName, roomId, seatType, `row`, `column`) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, seat.getSeatName());
            statement.setInt(2, seat.getRoomId());
            statement.setString(3, seat.getSeatType().toString());  // SeatType enum to String
            statement.setInt(4, seat.getRow());
            statement.setInt(5, seat.getColumn());

            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Seat getSeatById(int seatId) {
        String query = "SELECT * FROM seats WHERE seatId = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, seatId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return toSeatDTO(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Seat> getAllSeatsByRoom(int roomId) {
        List<Seat> seats = new ArrayList<>();
        String query = "SELECT * FROM seats WHERE roomId = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, roomId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                seats.add(toSeatDTO(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return seats;
    }

    private Seat toSeatDTO(ResultSet resultSet) throws SQLException {
        int seatId = resultSet.getInt("seatId");
        String seatName = resultSet.getString("seatName");
        int roomId = resultSet.getInt("roomId");
        SeatType seatType = SeatType.valueOf(resultSet.getString("seatType"));
        int row = resultSet.getInt("row");
        int column = resultSet.getInt("column");

        return new Seat(seatId, seatName, roomId, seatType, row, column);
    }
}
