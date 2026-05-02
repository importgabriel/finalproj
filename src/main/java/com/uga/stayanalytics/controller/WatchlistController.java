package com.uga.stayanalytics.controller;

import com.uga.stayanalytics.service.UserService;
import com.uga.stayanalytics.service.WatchlistService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/watchlist")
public class WatchlistController {

    private final WatchlistService watchlistService;
    private final UserService userService;

    public WatchlistController(WatchlistService w, UserService u) {
        this.watchlistService = w; this.userService = u;
    }

    @GetMapping
    public String view(@AuthenticationPrincipal UserDetails principal, Model model) {
        long userId = userService.findByUsername(principal.getUsername()).orElseThrow().getId();
        model.addAttribute("entries", watchlistService.forUser(userId));
        return "watchlist";
    }

    @PostMapping("/{contentId}/status")
    public String updateStatus(@PathVariable String contentId,
                               @RequestParam String status,
                               @AuthenticationPrincipal UserDetails principal,
                               RedirectAttributes ra) {
        long userId = userService.findByUsername(principal.getUsername()).orElseThrow().getId();
        try {
            watchlistService.updateStatus(userId, contentId, status);
            ra.addFlashAttribute("info", "Status updated.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/watchlist";
    }

    @PostMapping("/{contentId}/remove")
    public String remove(@PathVariable String contentId,
                         @AuthenticationPrincipal UserDetails principal,
                         RedirectAttributes ra) {
        long userId = userService.findByUsername(principal.getUsername()).orElseThrow().getId();
        watchlistService.remove(userId, contentId);
        ra.addFlashAttribute("info", "Removed from watchlist.");
        return "redirect:/watchlist";
    }
}
