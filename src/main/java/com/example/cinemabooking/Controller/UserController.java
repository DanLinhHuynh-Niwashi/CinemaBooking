package com.example.cinemabooking.Controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/landing")
public class UserController {

    @GetMapping("/")
    public String showLandingPage(Model model, HttpSession session) {
        String mode = (String) session.getAttribute("mode");  // admin or customer
        String customerPhone = (String) session.getAttribute("customerPhone");
        if (mode != null) {
            if (mode.equals("customer") && customerPhone != null)
                return "redirect:/movie/list";
            if (mode.equals("admin"))
                return "redirect:/movie/list";
        }
        model.addAttribute("mode", "customer");
        return "landing";
    }

    @PostMapping("/")
    public String handlePhoneEntry(@RequestParam("customerPhone") String customerPhone,
                                   @RequestParam("mode") String mode,
                                   HttpSession session) {
        if (mode.equals("customer")) {
            session.setAttribute("customerPhone", customerPhone);
            session.setAttribute("mode", "customer");
            return "redirect:/movie/list";
        }
        session.setAttribute("mode", "admin");
        return "redirect:/movie/list";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.setAttribute("mode", null);
        session.setAttribute("customerPhone", null);

        redirectAttributes.addFlashAttribute("message", "You have been logged out.");

        return "redirect:/landing/";
    }
}
