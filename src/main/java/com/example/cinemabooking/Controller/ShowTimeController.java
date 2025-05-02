package com.example.cinemabooking.Controller;

import com.example.cinemabooking.DAO.RoomDAO;
import com.example.cinemabooking.DAO.SeatDAO;
import com.example.cinemabooking.DAO.ShowTimeDAO;
import com.example.cinemabooking.DAO.TicketDAO;
import com.example.cinemabooking.DAO.MovieDAO;
import com.example.cinemabooking.Model.DatabaseConnection;
import com.example.cinemabooking.Model.Seat;
import com.example.cinemabooking.Model.ShowTime;
import com.example.cinemabooking.Model.Ticket;
import com.example.cinemabooking.Model.Room;
import com.example.cinemabooking.Model.Movie;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/showtimes")
public class ShowTimeController {

    private ShowTimeDAO showTimeDAO;
    private SeatDAO seatDAO;
    private TicketDAO ticketDAO;
    private RoomDAO roomDAO;
    private MovieDAO movieDAO;

    public ShowTimeController() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            this.showTimeDAO = new ShowTimeDAO(connection);
            this.seatDAO = new SeatDAO(connection);
            this.ticketDAO = new TicketDAO(connection);
            this.roomDAO = new RoomDAO(connection);
            this.movieDAO = new MovieDAO(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/{movieId}")
    public String getShowTimesForMovie(@PathVariable int movieId,
                                       @RequestParam(value = "showtimeId", required = false) Integer showtimeId,
                                       Model model, HttpSession session) {
        String mode = (String) session.getAttribute("mode");  // admin or customer
        if (mode == null) {
            return "redirect:/landing/";
        }
        if (mode.equals("customer")) {
            return "redirect:/booking/" + movieId;
        }
        model.addAttribute("mode", mode);

        List<ShowTime> showTimes = showTimeDAO.getShowTimesByMovieId(movieId);
        model.addAttribute("showtimes", showTimes);

        Movie movie = movieDAO.getMovieById(movieId);
        model.addAttribute("movie", movie);

        List<Room> rooms = roomDAO.getAllRooms();
        model.addAttribute("rooms", rooms);

        ShowTime showTime = new ShowTime();
        showTime.setMovieId(movieId);
        model.addAttribute("newShowtime", showTime);

        // If a showtimeId is provided, fetch its seats
        if (showtimeId != null) {
            ShowTime selectedShowtime = showTimeDAO.getShowTimeById(showtimeId);
            if (selectedShowtime != null) {
                List<Seat> seats = seatDAO.getAllSeatsByRoom(selectedShowtime.getRoomId());
                model.addAttribute("seats", seats);
                model.addAttribute("selectedShowtime", selectedShowtime);
                List<Ticket> tickets = ticketDAO.getTicketsByShowTimeId(showtimeId);
                Map<Integer, Boolean> seatBookingStatus = new HashMap<>();
                Map<Integer, String> ticketMap = new HashMap<>();
                for (Ticket ticket : tickets) {
                    seatBookingStatus.put(ticket.getSeat().getSeatId(), ticket.isBooked());
                    ticketMap.put(ticket.getSeat().getSeatId(), ticket.getCustomerPhone());
                }
                model.addAttribute("seatBookingStatus", seatBookingStatus);
                model.addAttribute("ticketMap", ticketMap);
            }
        }

        return "movie-showtime";
    }

    @PostMapping("/{movieId}")
    public String addShowtime(@PathVariable int movieId,
                              @ModelAttribute("newShowtime") ShowTime newShowtime,
                              HttpSession session, RedirectAttributes redirectAttributes) {
        String mode = (String) session.getAttribute("mode");  // admin or customer
        if (mode == null) {
            return "redirect:/landing/";
        }
        if (mode.equals("customer")) {
            return "redirect:/booking/" + movieId;
        }

        newShowtime.setMovieId(movieId);

        int showTimeId = showTimeDAO.addShowTime(newShowtime);

        if (showTimeId != -1) {
            newShowtime.setShowTimeId(showTimeId);
            List<Seat> seats = seatDAO.getAllSeatsByRoom(newShowtime.getRoomId());

            if (seats.isEmpty()) {
                redirectAttributes.addFlashAttribute("message", "No seats available in the selected room.");
            } else {
                for (Seat seat : seats) {
                    Ticket ticket = new Ticket(0, seat, "", newShowtime);
                    ticketDAO.addTicket(ticket);
                }
                redirectAttributes.addFlashAttribute("message", "Showtime added successfully and " + seats.size() + " tickets created.");
            }
        } else {
            redirectAttributes.addFlashAttribute("message", "Failed to add showtime. The showtime may have conflicted with another.");
        }

        redirectAttributes.addFlashAttribute("newShowtime", new ShowTime(0, movieId, 0, null, 0));

        return "redirect:/showtimes/" + movieId;
    }

}