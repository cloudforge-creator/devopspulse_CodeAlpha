package com.devopspulse.controller;

import com.devopspulse.service.StatusService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Web controller responsible for rendering the Thymeleaf-based UI:
 * the home page, the SRE dashboard, and the about page.
 */
@Controller
public class HomeController {

    private final StatusService statusService;

    public HomeController(StatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("appName", statusService.getApplicationName());
        model.addAttribute("tagline", statusService.getTagline());
        model.addAttribute("version", statusService.getVersion());
        model.addAttribute("environment", statusService.getEnvironment());
        model.addAttribute("healthy", statusService.isHealthy());
        model.addAttribute("buildStatus", statusService.getBuildStatus());
        return "index";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("appName", statusService.getApplicationName());
        model.addAttribute("version", statusService.getVersion());
        model.addAttribute("environment", statusService.getEnvironment());
        model.addAttribute("healthy", statusService.isHealthy());
        model.addAttribute("buildStatus", statusService.getBuildStatus());
        model.addAttribute("uptime", statusService.getUptimeFormatted());
        model.addAttribute("startedAt", statusService.getStartTimeFormatted());
        model.addAttribute("javaVersion", statusService.getJavaVersion());
        model.addAttribute("availableProcessors", statusService.getAvailableProcessors());
        model.addAttribute("freeMemoryMb", statusService.getFreeMemoryMb());
        model.addAttribute("totalMemoryMb", statusService.getTotalMemoryMb());
        model.addAttribute("maxMemoryMb", statusService.getMaxMemoryMb());
        return "dashboard";
    }

    @GetMapping("/about")
    public String about(Model model) {
        model.addAttribute("appName", statusService.getApplicationName());
        model.addAttribute("version", statusService.getVersion());
        return "about";
    }
}
