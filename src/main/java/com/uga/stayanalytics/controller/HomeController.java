package com.uga.stayanalytics.controller;

import com.uga.stayanalytics.service.MovieService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final MovieService movieService;

    public HomeController(MovieService movieService) { this.movieService = movieService; }

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("trending",  movieService.trending(20));
        model.addAttribute("platforms", movieService.allPlatforms());
        model.addAttribute("genres",    movieService.allGenres());
        return "home";
    }
}
