package com.moshy.jchirp.controllers;

import com.moshy.jchirp.ApiConfig;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MetricsController {

    record Hits(long value) { }

    @GetMapping("/admin/metrics")
    String getMetrics(Model model) {
        model.addAttribute("hits", new Hits(ApiConfig.fileserverHits.get()));
        return "metrics";
    }

}

