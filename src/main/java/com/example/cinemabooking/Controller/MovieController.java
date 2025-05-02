package com.example.cinemabooking.Controller;

import com.example.cinemabooking.DAO.MovieDAO;
import com.example.cinemabooking.Model.Movie;
import com.example.cinemabooking.Model.DatabaseConnection;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Connection;
import java.sql.SQLException;
@Controller
public class MovieController {

    private MovieDAO movieDAO;

    public MovieController() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            this.movieDAO = new MovieDAO(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/movie/list")
    public String showAddMovieForm(HttpSession session, Model model) {

        String mode = (String) session.getAttribute("mode");  // admin or customer
        if (mode == null) {
            return "redirect:/landing/";
        }

        model.addAttribute("mode", mode);
        model.addAttribute("movie", new Movie());
        model.addAttribute("movies", movieDAO.getAllMovies());
        return "list-movies";
    }

    @PostMapping("/movie/list")
    public String addMovie(Movie movie,
                           RedirectAttributes redirectAttributes, HttpSession session) {
        String mode = (String) session.getAttribute("mode");  // admin or customer
        if (mode == null) {
            return "redirect:/landing/";
        }
        if (mode.equals("customer")) {
            return "redirect:/movie/list";
        }
        boolean isAdded = movieDAO.addMovie(movie);
        redirectAttributes.addFlashAttribute("message", isAdded ? "Movie added successfully!" : "Failed to add movie.");
        return "redirect:/movie/list";
    }
}