package com.project.back_end.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.project.back_end.models.Admin;
import com.project.back_end.services.Service;

@Controller
@RequestMapping("${api.path}" + "admin")
public class AdminController {

    private final Service service;

    public AdminController(Service service) {
        this.service = service;
    }

    @GetMapping("/login")
    public String adminLoginPage() {
        return "redirect:/login?role=admin";
    }

    /**
     * Handles admin login requests.
     *
     * @param admin The admin credentials containing username and password
     * @return ResponseEntity with token if login is successful, error message otherwise
     */
    @PostMapping("/login")
    @ResponseBody
    public ResponseEntity<Map<String, String>> adminLogin(@RequestBody Admin admin) {
        return service.validateAdmin(admin);
    }
}
