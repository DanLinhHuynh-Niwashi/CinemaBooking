package com.example.cinemabooking.DAO;

import com.example.cinemabooking.Model.Room;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    private final Connection connection;

    public RoomDAO(Connection connection) {
        this.connection = connection;
    }
    public int addRoom(Room room) {
        String query = "INSERT INTO rooms (roomName, totalSeats) VALUES (?, ?)";

        try (PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, room.getRoomName());
            statement.setInt(2, room.getTotalSeats());

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            if (e.getErrorCode() == 1062) {
                System.out.println("Error: Room name must be unique.");
                return -1;
            }
            e.printStackTrace();
        }

        return -1;
    }

    public Room getRoomById(int roomId) {
        String query = "SELECT * FROM rooms WHERE roomId = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, roomId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return toRoomDTO(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String query = "SELECT * FROM rooms";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                rooms.add(toRoomDTO(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return rooms;
    }

    private Room toRoomDTO(ResultSet resultSet) throws SQLException {
        int roomId = resultSet.getInt("roomId");
        String roomName = resultSet.getString("roomName");
        int totalSeats = resultSet.getInt("totalSeats");

        return new Room(roomId, roomName, totalSeats);
    }
}
