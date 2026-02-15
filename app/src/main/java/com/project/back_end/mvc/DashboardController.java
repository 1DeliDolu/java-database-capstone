package com.project.back_end.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.project.back_end.services.OrchestratorService;

@Controller
public class DashboardController {

    private final OrchestratorService service;

    public DashboardController(OrchestratorService service) {
        this.service = service;
    }

    @GetMapping("/adminDashboard/{token}")
    public String adminDashboard(@PathVariable String token) {
        // Validate the token for admin role
        var response = service.validateToken(token, "admin");
        
        // Check if the response body (map) is empty - if empty, token is valid
        var body = response.getBody();
        if (response.getStatusCode().is2xxSuccessful() && 
            (body == null || body.isEmpty())) {
            return "admin/adminDashboard";
        }
        
        // If token is invalid, redirect to login page
        return "redirect:/";
    }

    @GetMapping("/doctorDashboard/{token}")
    public String doctorDashboard(@PathVariable String token) {
        // Validate the token for doctor role
        var response = service.validateToken(token, "doctor");
        
        // Check if the response body (map) is empty - if empty, token is valid
        var body = response.getBody();
        if (response.getStatusCode().is2xxSuccessful() && 
            (body == null || body.isEmpty())) {
            return "doctor/doctorDashboard";
        }
        
        // If token is invalid, redirect to login page
        return "redirect:/";
    }
}
