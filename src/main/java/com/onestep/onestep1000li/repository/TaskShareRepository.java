package com.onestep.onestep1000li.repository;

import com.onestep.onestep1000li.entity.Task;
import com.onestep.onestep1000li.entity.TaskShare;
import com.onestep.onestep1000li.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskShareRepository extends JpaRepository<TaskShare, Integer> {
    List<TaskShare> findByUser(User user); // 나에게 공유된 과제 찾기용
    List<TaskShare> findByTask(Task task);
}