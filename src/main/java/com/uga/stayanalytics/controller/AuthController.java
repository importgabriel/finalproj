package com.uga.stayanalytics.controller;

import com.uga.stayanalytics.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) { this.userService = userService; }

    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "registered", required = false) String registered,
                            Model model) {
        if (error != null) model.addAttribute("error", "Invalid username or password.");
        if (registered != null) model.addAttribute("info", "Account created — please log in.");
        return "login";
    }

    @GetMapping("/signup")
    public String signupForm() { return "signup"; }

    @PostMapping("/signup")
    public String signupSubmit(@RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String password,
                               Model model) {
        UserService.RegistrationResult r = userService.register(username, email, password);
        if (!r.success) {
            model.addAttribute("error", r.error);
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            return "signup";
        }
        return "redirect:/login?registered";
    }
}
