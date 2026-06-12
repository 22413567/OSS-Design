package com.onestep.onestep1000li.controller;

import com.onestep.onestep1000li.entity.Friendship;
import com.onestep.onestep1000li.service.FriendshipService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/friends")
public class FriendshipController {

    private final FriendshipService friendshipService;

    public FriendshipController(FriendshipService friendshipService) {
        this.friendshipService = friendshipService;
    }

    // 친구 추가
    @PostMapping("/add")
    public String addFriend(@RequestParam("friendId") String friendId, HttpSession session) {
        String myId = (String) session.getAttribute("loginUser");
        if (myId == null) return "로그인이 필요합니다.";
        
        try {
            return friendshipService.addFriend(myId, friendId);
        } catch (Exception e) {
            return "추가 실패: " + e.getMessage();
        }
    }

    // 친구 목록 조회
    @GetMapping("/list")
    public List<String> getFriends(HttpSession session) {
        String myId = (String) session.getAttribute("loginUser");
        if (myId == null) return new ArrayList<>();
        List<Friendship> friendships = friendshipService.getFriends(myId);
        List<String> friendList = new ArrayList<>();
        for (Friendship f : friendships) {
            friendList.add(f.getReceiver().getNickname() + " (" + f.getReceiver().getUserId() + ")");
        }
        
        return friendList;
    }

    // 친구 삭제
    @PostMapping("/delete")
    public String deleteFriend(@RequestParam("friendId") String friendId, HttpSession session) {
        String myId = (String) session.getAttribute("loginUser");
        if (myId == null) return "로그인이 필요합니다.";
        
        try {
            return friendshipService.deleteFriend(myId, friendId);
        } catch (Exception e) {
            return e.getMessage(); 
        }
    }

    // 공유 과제 확인
    @GetMapping("/check-shared")
    public String checkSharedTasks(@RequestParam("friendId") String friendId, HttpSession session) {
        String myId = (String) session.getAttribute("loginUser");
        if (myId == null) return "false";
        
        boolean hasShared = friendshipService.hasSharedTasks(myId, friendId);
        return String.valueOf(hasShared);
    }
}