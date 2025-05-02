package com.example.cinemabooking.DAO;

import com.example.cinemabooking.Model.Movie;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MovieDAO {

    private final Connection connection;

    public MovieDAO(Connection connection) {
        this.connection = connection;
    }

    public boolean addMovie(Movie movie) {
        String query = "INSERT INTO movies (title, durationMinutes, genre) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, movie.getTitle());
            statement.setInt(2, movie.getDurationMinutes());
            statement.setString(3, movie.getGenre());

            int rowsInserted = statement.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Movie getMovieById(int movieId) {
        String query = "SELECT * FROM movies WHERE movieId = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, movieId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return toMovieDTO(resultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Movie> getAllMovies() {
        List<Movie> movies = new ArrayList<>();
        String query = "SELECT * FROM movies";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                movies.add(toMovieDTO(resultSet));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return movies;
    }

    private Movie toMovieDTO(ResultSet resultSet) throws SQLException {
        int movieId = resultSet.getInt("movieId");
        String title = resultSet.getString("title");
        int durationMinutes = resultSet.getInt("durationMinutes");
        String genre = resultSet.getString("genre");

        return new Movie(movieId, title, durationMinutes, genre);
    }
}
