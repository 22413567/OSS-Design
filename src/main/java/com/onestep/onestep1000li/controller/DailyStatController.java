package com.onestep.onestep1000li.controller;

import com.onestep.onestep1000li.service.DailyStatService;
import com.onestep.onestep1000li.entity.DailyStat;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map; 

@RestController
@RequestMapping("/api/stats")
public class DailyStatController {

    private final DailyStatService dailyStatService;

    public DailyStatController(DailyStatService dailyStatService) {
        this.dailyStatService = dailyStatService;
    }

    // 집중 시간 누적
    @PostMapping("/focus")
    public String addFocusTime(@RequestParam("focusTime") int focusTime, HttpSession session) {
        String userId = (String) session.getAttribute("loginUser");
        if (userId == null) return "로그인이 필요합니다.";

        try {
            dailyStatService.addFocusTime(userId, focusTime);
            return "오늘의 집중 시간에 " + focusTime + "분이 추가되었습니다! 🎉";
        } catch (Exception e) {
            return "기록 실패: " + e.getMessage();
        }
    }
 // 데일리 리포트 알림 요청
    @GetMapping("/weekly")
    public List<DailyStat> getWeeklyStats(HttpSession session) {
        String userId = (String) session.getAttribute("loginUser");
        if (userId == null) return null; 

        return dailyStatService.getWeeklyStats(userId);
    }
 // 랭킹 요청
    @GetMapping("/ranking")
    public List<Map<String, Object>> getDailyRanking(HttpSession session) {
        String userId = (String) session.getAttribute("loginUser");
        if (userId == null) return null;

        return dailyStatService.getDailyRanking(userId);
    }
}