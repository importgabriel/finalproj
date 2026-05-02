package com.uga.stayanalytics.controller;

import com.uga.stayanalytics.service.MovieService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class SearchController {

    private final MovieService movieService;

    public SearchController(MovieService movieService) { this.movieService = movieService; }

    @GetMapping("/search")
    public String search(@RequestParam(required = false) String title,
                         @RequestParam(required = false) Integer genreId,
                         @RequestParam(required = false) Integer platformId,
                         @RequestParam(required = false) Integer year,
                         @RequestParam(required = false) String type,
                         Model model) {
        model.addAttribute("results",   movieService.search(title, genreId, platformId, year, type));
        model.addAttribute("platforms", movieService.allPlatforms());
        model.addAttribute("genres",    movieService.allGenres());
        model.addAttribute("title",     title);
        model.addAttribute("genreId",   genreId);
        model.addAttribute("platformId", platformId);
        model.addAttribute("year",      year);
        model.addAttribute("type",      type);
        return "search";
    }
}
