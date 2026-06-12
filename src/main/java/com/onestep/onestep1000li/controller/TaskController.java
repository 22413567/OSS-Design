package com.onestep.onestep1000li.controller;

import com.onestep.onestep1000li.entity.Task;
import com.onestep.onestep1000li.service.TaskService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

 // 메인 과제 추가
    @PostMapping("/add")
    public String addTask(@RequestParam("content") String content, 
                          @RequestParam(value = "friendId", defaultValue = "none") String friendId, 
                          HttpSession session) {
        String userId = (String) session.getAttribute("loginUser");
        if (userId == null) return "로그인이 필요합니다.";

        try {
            taskService.addSharedTask(userId, content, friendId);
            return "등록 성공";
        } catch (Exception e) {
            return "실패: " + e.getMessage();
        }
    }

    // 하위 과제 추가
    @PostMapping("/addSub")
    public String addSubTask(@RequestParam("content") String content,
                             @RequestParam("parentTaskId") Integer parentTaskId,
                             HttpSession session) {
        String userId = (String) session.getAttribute("loginUser"); 
        if (userId == null) return "로그인이 필요합니다.";

        try {
            taskService.addSubTask(userId, content, parentTaskId);
            return "등록 성공";
        } catch (Exception e) {
            return "실패: " + e.getMessage();
        }
    }

    // 과제 토글
    @PostMapping("/toggle")
    public String toggleTask(@RequestParam("taskId") Integer taskId) {
        try {
            taskService.toggleTask(taskId);
            return "상태가 변경되었습니다.";
        } catch (Exception e) {
            return "상태 변경 실패: " + e.getMessage();
        }
    }

    // 로그인된 유저의 목록만 반환
    @GetMapping("/list")
    public List<Task> getTasks(HttpSession session) {
        String userId = (String) session.getAttribute("loginUser");
        if (userId == null) return null; 
        
        return taskService.getTasksByUser(userId);
    }
    
 // 과제 수정
    @PostMapping("/update")
    public String updateTask(@RequestParam("taskId") Integer taskId,
                             @RequestParam("newContent") String newContent,
                             HttpSession session) {
        
        String userId = (String) session.getAttribute("loginUser");
        if (userId == null) return "로그인이 필요합니다.";

        try {
            taskService.updateTask(taskId, newContent, userId);
            return "수정 성공";
        } catch (Exception e) {
            return "수정 실패: " + e.getMessage();
        }
    }

 // 삭제 시 에러 메시지
    @PostMapping("/delete")
    public String deleteTask(@RequestParam("taskId") Integer taskId, HttpSession session) {
        String userId = (String) session.getAttribute("loginUser");
        if (userId == null) return "로그인이 필요합니다.";

        try {
            taskService.deleteTask(taskId);
            return "삭제 성공";
        } catch (Exception e) {
            return "삭제 실패: " + e.getMessage(); 
        }
    }
}