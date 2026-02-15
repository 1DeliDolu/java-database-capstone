package com.project.back_end.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthPageController {

    @GetMapping("/login")
    public String centralLoginPage() {
        return "auth/centralLogin";
    }

    @GetMapping("/register")
    public String centralRegisterPage() {
        return "auth/centralRegister";
    }

    @GetMapping("/admin/login")
    public String adminLoginPage() {
        return "redirect:/login?role=admin";
    }

    @GetMapping("/doctor/login")
    public String doctorLoginPage() {
        return "redirect:/login?role=doctor";
    }
}
