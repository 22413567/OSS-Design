package com.onestep.onestep1000li.service;

import com.onestep.onestep1000li.entity.DailyStat;
import com.onestep.onestep1000li.entity.User;
import com.onestep.onestep1000li.entity.Friendship;
import com.onestep.onestep1000li.entity.Task; // 🚀 추가됨 (과제 정보)
import com.onestep.onestep1000li.repository.DailyStatRepository;
import com.onestep.onestep1000li.repository.UserRepository;
import com.onestep.onestep1000li.repository.FriendshipRepository;
import com.onestep.onestep1000li.repository.TaskRepository; // 🚀 추가됨 (과제 DB 접근)
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Service
public class DailyStatService {

    private final DailyStatRepository dailyStatRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final TaskRepository taskRepository; 

    public DailyStatService(DailyStatRepository dailyStatRepository, UserRepository userRepository, FriendshipRepository friendshipRepository, TaskRepository taskRepository) {
        this.dailyStatRepository = dailyStatRepository;
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
        this.taskRepository = taskRepository;
    }

    public DailyStat addFocusTime(String userId, int addedTime) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        LocalDate today = LocalDate.now();

        DailyStat stat = dailyStatRepository.findByUserAndStatDate(user, today)
                .orElseGet(() -> {
                    DailyStat newStat = new DailyStat();
                    newStat.setUser(user);
                    newStat.setStatDate(today);
                    newStat.setFocusTime(0); 
                    return newStat;
                });

        int currentFocusTime = stat.getFocusTime() == null ? 0 : stat.getFocusTime();
        stat.setFocusTime(currentFocusTime + addedTime);

        return dailyStatRepository.save(stat);
    }
    
    public List<DailyStat> getWeeklyStats(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저입니다."));

        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(6);

        return dailyStatRepository.findByUserAndStatDateBetweenOrderByStatDateAsc(user, weekAgo, today);
    }

    // =========================================================================
    // 과제 달성률 기준으로 랭킹 산출
    // =========================================================================
    public List<Map<String, Object>> getDailyRanking(String userId) {
        User me = userRepository.findById(userId).orElseThrow();

        List<User> targetUsers = new ArrayList<>();
        targetUsers.add(me);

        List<Friendship> friendships = friendshipRepository.findByRequester(me); 
        for (Friendship f : friendships) {
            targetUsers.add(f.getReceiver()); 
        }

     // 과제 달성률 계산
        List<Map<String, Object>> ranking = new ArrayList<>();
        for (User u : targetUsers) {
            List<Task> userTasks = taskRepository.findByUser(u);
            
            int totalTasks = 0;
            int completedTasks = 0;
            
            for (Task t : userTasks) {
                if (!t.getIsMain()) {
                    totalTasks++;
                    if (t.getIsCompleted()) {
                        completedTasks++;
                    }
                }
            }
            
            // 달성률 계산
            int completionRate = totalTasks == 0 ? 0 : Math.round(((float) completedTasks / totalTasks) * 100);

            Map<String, Object> map = new HashMap<>();
            map.put("nickname", u.getNickname());
            map.put("userId", u.getUserId());
            map.put("completionRate", completionRate); 
            ranking.add(map);
        }

     // 3. 달성률(%)이 높은 순서대로 내림차순 정렬
        ranking.sort((a, b) -> {
            int rateCompare = (Integer) b.get("completionRate") - (Integer) a.get("completionRate");
            
            // 2b. 달성률이 완전히 똑같은 경우
            if (rateCompare == 0) {
                String nameA = (String) a.get("nickname");
                String nameB = (String) b.get("nickname");
                return nameA.compareTo(nameB); // 닉네임 기준 오름차순(가나다순) 정렬
            }
            
            return rateCompare; // 달성률이 다르면 달성률 기준으로 정렬
        });

        return ranking;
    }
}