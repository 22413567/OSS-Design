package com.onestep.onestep1000li.repository;

import com.onestep.onestep1000li.entity.Task;
import com.onestep.onestep1000li.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Integer> {
    // 특정 유저의 모든 과제를 가져오는 기능
    List<Task> findByUser(User user);
    List<Task> findByParentTask(Task parentTask);
    List<Task> findByOriginalTaskId(Integer originalTaskId);
    int countByParentTask(Task parentTask);
    boolean existsByParentTaskAndContent(Task parentTask, String content);
    boolean existsByUserAndContentAndParentTaskIsNullAndIsCompletedFalse(User user, String content);
}