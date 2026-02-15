package com.project.back_end.controllers;

import java.util.Map;
import java.util.HashMap;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.project.back_end.models.Admin;
import com.project.back_end.services.Service;

@RestController
@RequestMapping("${api.path}" + "admin")
public class AdminController {

    private final Service service;

    public AdminController(Service service) {
        this.service = service;
    }

    @GetMapping("/login")
    public ResponseEntity<Map<String, String>> adminLoginInfo() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Use POST /api/admin/login with JSON body: {\"username\":\"...\",\"password\":\"...\"}");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Handles admin login requests.
     *
     * @param admin The admin credentials containing username and password
     * @return ResponseEntity with token if login is successful, error message otherwise
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> adminLogin(@RequestBody Admin admin) {
        return service.validateAdmin(admin);
    }
}
