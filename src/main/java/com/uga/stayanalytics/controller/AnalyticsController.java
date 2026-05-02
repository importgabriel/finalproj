package com.uga.stayanalytics.controller;

import com.uga.stayanalytics.service.AnalyticsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AnalyticsController {

    private final AnalyticsService analytics;

    public AnalyticsController(AnalyticsService analytics) { this.analytics = analytics; }

    @GetMapping("/analytics")
    public String dashboard(Model model) {
        model.addAttribute("perPlatform", analytics.perPlatform());
        model.addAttribute("perGenrePlatform", analytics.perGenrePlatform());
        model.addAttribute("topRated", analytics.topCommunityRated());
        return "analytics";
    }
}
