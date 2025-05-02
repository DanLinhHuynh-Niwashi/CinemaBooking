package com.example.cinemabooking.Controller;

import com.example.cinemabooking.DAO.RoomDAO;
import com.example.cinemabooking.DAO.SeatDAO;
import com.example.cinemabooking.Model.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

@Controller
@RequestMapping("/room")
public class RoomController {

    private RoomDAO roomDAO;
    private SeatDAO seatDAO;

    public RoomController() {
        try {
            Connection connection = DatabaseConnection.getConnection();
            this.roomDAO = new RoomDAO(connection);
            this.seatDAO = new SeatDAO(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @GetMapping("/list")
    public String showAddRoomForm(@RequestParam(value = "roomId", required = false) Integer roomId,
                                  Model model, HttpSession session) {
        String mode = (String) session.getAttribute("mode");  // admin or customer
        if (mode == null) {
            return "redirect:/landing/";
        }
        if (mode.equals("customer")) {
            return "redirect:/movie/list";
        }

        model.addAttribute("mode", mode);
        model.addAttribute("room", new Room());
        List<Room> rooms = roomDAO.getAllRooms();
        model.addAttribute("rooms", rooms);

        if (roomId != null) {
            List<Seat> seats = seatDAO.getAllSeatsByRoom(roomId);
            if (!seats.isEmpty()) {
                model.addAttribute("seats", seats);
            }
        }

        return "list-room";
    }

    @PostMapping("/list")
    public String addRoom(@ModelAttribute Room room,
                          @RequestParam("seats") List<String> seatCoords,
                          @RequestParam Map<String, String> seatTypes,
                          RedirectAttributes redirectAttributes, HttpSession session) {

        String mode = (String) session.getAttribute("mode");  // admin or customer
        if (mode == null) {
            return "redirect:/landing/";
        }
        if (mode.equals("customer")) {
            return "redirect:/movie/list";
        }

        room.setTotalSeats(seatCoords.size());
        int roomId = roomDAO.addRoom(room);
        if (roomId < 0) {
            redirectAttributes.addFlashAttribute("message", "Failed to add room, your room name may exist.");
            return "redirect:/room/list";
        }

        Set<Integer> rowSet = new HashSet<>();
        Map<Integer, List<Integer>> rowColumnsMap = new HashMap<>();

        for (String coord : seatCoords) {
            String[] parts = coord.split("_");
            int row = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);

            rowSet.add(row);
            rowColumnsMap.computeIfAbsent(row, k -> new ArrayList<>()).add(col);
        }

        List<Integer> sortedRows = new ArrayList<>(rowSet);
        Collections.sort(sortedRows);

        Map<Integer, Character> rowLetterMap = new HashMap<>();
        char currentLetter = 'A';
        for (int row : sortedRows) {
            rowLetterMap.put(row, currentLetter++);
        }

        int seatId = 1;
        for (String coord : seatCoords) {
            String[] parts = coord.split("_");
            int row = Integer.parseInt(parts[0]);
            int col = Integer.parseInt(parts[1]);

            char rowLetter = rowLetterMap.get(row);

            String seatTypeKey = "seatTypes[" + row + "_" + col + "]";
            String seatTypeValue = seatTypes.getOrDefault(seatTypeKey, "STANDARD");

            SeatType selectedSeatType = SeatType.valueOf(seatTypeValue.toUpperCase());

            List<Integer> colList = rowColumnsMap.get(row);
            int colIndex = new ArrayList<>(new TreeSet<>(colList)).indexOf(col) + 1;

            String seatName = rowLetter + String.valueOf(colIndex); // A1, A2, B1, etc.
            Seat seat = new Seat(seatId++, seatName, roomId, selectedSeatType, row, col);
            seatDAO.addSeat(seat);
        }

        redirectAttributes.addFlashAttribute("message", "Failed to add room.");
        return "redirect:/room/list";
    }

}
