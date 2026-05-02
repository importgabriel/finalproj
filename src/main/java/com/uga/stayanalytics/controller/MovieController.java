package com.uga.stayanalytics.controller;

import com.uga.stayanalytics.model.Content;
import com.uga.stayanalytics.model.User;
import com.uga.stayanalytics.repository.ReviewDao;
import com.uga.stayanalytics.service.MovieService;
import com.uga.stayanalytics.service.ReviewService;
import com.uga.stayanalytics.service.UserService;
import com.uga.stayanalytics.service.WatchlistService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/movies")
public class MovieController {

    private final MovieService movieService;
    private final ReviewService reviewService;
    private final WatchlistService watchlistService;
    private final UserService userService;

    public MovieController(MovieService movieService,
                           ReviewService reviewService,
                           WatchlistService watchlistService,
                           UserService userService) {
        this.movieService = movieService;
        this.reviewService = reviewService;
        this.watchlistService = watchlistService;
        this.userService = userService;
    }

    @GetMapping("/{contentId}")
    public String detail(@PathVariable String contentId,
                         @AuthenticationPrincipal UserDetails principal,
                         Model model) {
        Optional<Content> opt = movieService.findById(contentId);
        if (opt.isEmpty()) return "redirect:/home";
        Content content = opt.get();

        ReviewDao.CommunityStats stats = reviewService.statsFor(contentId);

        model.addAttribute("content",   content);
        model.addAttribute("reviews",   reviewService.forContent(contentId));
        model.addAttribute("communityAvg",   stats.avg);
        model.addAttribute("communityCount", stats.count);
        model.addAttribute("similar", movieService.similar(contentId));
        model.addAttribute("tags",    movieService.tagsFor(contentId));

        boolean inWatchlist = false;
        if (principal != null) {
            Optional<User> u = userService.findByUsername(principal.getUsername());
            if (u.isPresent()) {
                inWatchlist = watchlistService.contains(u.get().getId(), contentId);
            }
        }
        model.addAttribute("inWatchlist", inWatchlist);

        return "movie-detail";
    }

    @PostMapping("/{contentId}/reviews")
    public String postReview(@PathVariable String contentId,
                             @RequestParam int rating,
                             @RequestParam(required = false) String comment,
                             @AuthenticationPrincipal UserDetails principal,
                             RedirectAttributes ra) {
        if (principal == null) return "redirect:/login";
        long userId = userService.findByUsername(principal.getUsername()).orElseThrow().getId();
        try {
            reviewService.submit(userId, contentId, rating, comment);
            ra.addFlashAttribute("info", "Review saved.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/movies/" + contentId;
    }

    @PostMapping("/{contentId}/watchlist")
    public String addToWatchlist(@PathVariable String contentId,
                                 @AuthenticationPrincipal UserDetails principal,
                                 RedirectAttributes ra) {
        if (principal == null) return "redirect:/login";
        long userId = userService.findByUsername(principal.getUsername()).orElseThrow().getId();
        watchlistService.add(userId, contentId);
        ra.addFlashAttribute("info", "Added to watchlist.");
        return "redirect:/movies/" + contentId;
    }
}
