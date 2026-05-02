package com.uga.stayanalytics.controller;

import com.uga.stayanalytics.service.AnalyticsService;
import com.uga.stayanalytics.service.MovieService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AnalyticsService analytics;
    private final MovieService movies;

    public AdminController(AnalyticsService analytics, MovieService movies) {
        this.analytics = analytics; this.movies = movies;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("perPlatform", analytics.perPlatform());
        model.addAttribute("trending",    movies.trending(10));
        return "admin";
    }
}
