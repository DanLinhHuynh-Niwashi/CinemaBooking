package com.example.cinemabooking.Controller;

import com.example.cinemabooking.DAO.*;
import com.example.cinemabooking.Model.*;

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
@RequestMapping("/booking")
public class BookingController {

    private ShowTimeDAO showTimeDAO;
    private SeatDAO seatDAO;
    private TicketDAO ticketDAO;
    private MovieDAO movieDAO;
    private RoomDAO roomDAO;

    public BookingController() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            this.showTimeDAO = new ShowTimeDAO(connection);
            this.seatDAO = new SeatDAO(connection);
            this.ticketDAO = new TicketDAO(connection);
            this.movieDAO = new MovieDAO(connection);
            this.roomDAO = new RoomDAO(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/{movieId}")
    public String getShowTimesForMovie(@PathVariable int movieId,
                                       @RequestParam(value = "showtimeId", required = false) Integer showtimeId,
                                       Model model, HttpSession session) {

        String mode = (String) session.getAttribute("mode");  // admin or customer
        String customerPhone = (String) session.getAttribute("customerPhone");
        if (mode == null) {
            return "redirect:/landing/";
        }
        if (mode.equals("admin")) {
            return "redirect:/showtimes/" + movieId;
        }
        if (customerPhone == null) {
            return "redirect:/landing/";
        }

        model.addAttribute("mode", mode);

        List<ShowTime> showTimes = showTimeDAO.getShowTimesByMovieId(movieId);
        model.addAttribute("showtimes", showTimes);

        Movie movie = movieDAO.getMovieById(movieId);
        model.addAttribute("movie", movie);

        if (showtimeId != null) {
            ShowTime selectedShowtime = showTimeDAO.getShowTimeById(showtimeId);
            if (selectedShowtime != null) {
                List<Seat> seats = seatDAO.getAllSeatsByRoom(selectedShowtime.getRoomId());
                model.addAttribute("seats", seats);
                model.addAttribute("selectedShowtime", selectedShowtime);
                List<Ticket> tickets = ticketDAO.getTicketsByShowTimeId(showtimeId);
                Map<Integer, Boolean> seatBookingStatus = new HashMap<>();
                Map<Integer, Integer> seatTicketMap = new HashMap<>();
                Map<Integer, Double> ticketPriceMap = new HashMap<>();
                for (Ticket ticket : tickets) {
                    seatBookingStatus.put(ticket.getSeat().getSeatId(), ticket.isBooked());
                    seatTicketMap.put(ticket.getSeat().getSeatId(), ticket.getTicketId());
                    ticketPriceMap.put(ticket.getSeat().getSeatId(), ticket.getTicketPrice());
                }
                model.addAttribute("seatBookingStatus", seatBookingStatus);
                model.addAttribute("seatTicketMap", seatTicketMap);
                model.addAttribute("ticketPriceMap", ticketPriceMap);
            }
        }

        return "movie-booking";
    }

    @GetMapping("/my-ticket")
    public String getMyTickets(HttpSession session, Model model) {

        String mode = (String) session.getAttribute("mode");  // admin or customer
        String customerPhone = (String) session.getAttribute("customerPhone");
        if (mode == null) {
            return "redirect:/landing/";
        }
        if (mode.equals("admin")) {
            return "redirect:/movie/list";
        }
        if (customerPhone == null) {
            return "redirect:/landing/";
        }

        model.addAttribute("mode", mode);

        List<Room> rooms = roomDAO.getAllRooms();
        Map<Integer, String> roomMap = new HashMap<>();
        for (Room room : rooms) {
            roomMap.put(room.getRoomId(), room.getRoomName());
        }
        List<Movie> movies = movieDAO.getAllMovies();
        Map<Integer, String> movieMap = new HashMap<>();

        for (Movie movie : movies) {
            movieMap.put(movie.getMovieId(), movie.getTitle());
        }

        List<Ticket> tickets = ticketDAO.getTicketsByCustomerPhone(customerPhone);
        model.addAttribute("tickets", tickets);
        model.addAttribute("roomMap", roomMap);
        model.addAttribute("movieMap", movieMap);

        return "list-ticket";
    }

    @PostMapping("/cancel")
    public String cancelTickets(@RequestParam("ticketIds") List<Integer> ticketIds,
                                HttpSession session, RedirectAttributes redirectAttributes) {
        String customerPhone = (String) session.getAttribute("customerPhone");
        int canceledCount = 0;
        for (int ticketId : ticketIds) {
            boolean canceled = ticketDAO.cancelTicket(ticketId, customerPhone);
            if (canceled) canceledCount++;
        }
        redirectAttributes.addFlashAttribute("message", "Canceled " + canceledCount + " tickets.");
        return "redirect:/booking/my-ticket";
    }

    @PostMapping("/{movieId}")
    public String bookSeat(@PathVariable int movieId,
                           @RequestParam("showtimeId") int showTimeId,
                           @RequestParam("ticketIds") List<Integer> ticketIds,
                           RedirectAttributes redirectAttributes,
                           HttpSession session) {
        String mode = (String) session.getAttribute("mode");  // admin or customer
        String customerPhone = (String) session.getAttribute("customerPhone");
        if (mode == null) {
            return "redirect:/landing/";
        }
        if (mode.equals("admin")) {
            return "redirect:/showtimes/" + movieId;
        }
        if (customerPhone == null) {
            return "redirect:/landing/";
        }

        if (ticketIds.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "No seats selected.");
            return "redirect:/booking/" + movieId + "?showtimeId=" + showTimeId;
        }

        int bookedCount = 0;
        for (Integer ticketId : ticketIds) {
            boolean booked = ticketDAO.bookTicket(ticketId, (String) session.getAttribute("customerPhone"));
            if (booked) {
                bookedCount++;
            }
        }

        if (bookedCount == ticketIds.size()) {
            redirectAttributes.addFlashAttribute("message", "Booking successful for " + ticketIds.size() + " seat(s).");
        } else if (bookedCount == 0) {
            redirectAttributes.addFlashAttribute("message", "Failed to book all your seats as they're occupied.");
        } else {
            redirectAttributes.addFlashAttribute("message", "Booking successful for " + bookedCount + " seat(s). Some seats are not booked successfully.");
        }

        return "redirect:/booking/" + movieId + "?showtimeId=" + showTimeId;
    }
}