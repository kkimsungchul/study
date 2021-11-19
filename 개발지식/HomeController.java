package com.example.demo.springsecurity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@Slf4j
public class HomeController {

//    @GetMapping("/")
//    public String home(Model model){
//        log.info("home controller");
//        return "home";
//    }
    @GetMapping("/user")
    public String dispUser(Model model){
        log.info("home controller");
        return "/user/user";
    }
    @GetMapping("/manager")
    public String dispManager(Model model){
        log.info("home controller");
        return "/user/manager";
    }
    @GetMapping("/admin")
    public String dispAdmin(Model model){
        log.info("home controller");
        return "/user/admin";
    }
}
