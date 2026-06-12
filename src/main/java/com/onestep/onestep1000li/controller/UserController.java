package com.onestep.onestep1000li.controller;

import com.onestep.onestep1000li.entity.User;
import com.onestep.onestep1000li.service.UserService;
import jakarta.servlet.http.HttpSession; // 세션 수입!
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // 회원가입
    @PostMapping("/signup")
    public String signup(@RequestParam("userId") String userId,
                         @RequestParam("password") String password,
                         @RequestParam("nickname") String nickname) {
        try {
            userService.registerUser(userId, password, nickname);
            return "회원가입이 완료되었습니다!";
        } catch (Exception e) {
            return "회원가입 실패: " + e.getMessage();
        }
    }

    // 로그인
    @PostMapping("/login")
    public String login(@RequestParam("userId") String userId,
                        @RequestParam("password") String password,
                        HttpSession session) { 
        User user = userService.login(userId, password);
        
        if (user != null) {
            session.setAttribute("loginUser", user.getUserId()); 
            return "로그인 성공";
        } else {
            return "로그인 실패: 아이디나 비밀번호를 확인해주세요.";
        }
    }

    // 로그아웃
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); 
        return "로그아웃 되었습니다.";
    }

    // 현재 로그인 확인
    @GetMapping("/me")
    public String checkLogin(HttpSession session) {
        String loginUser = (String) session.getAttribute("loginUser");
        if (loginUser == null) {
            return "NOT_LOGGED_IN"; 
        }
        return loginUser; 
    }
}