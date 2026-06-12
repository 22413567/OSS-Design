package com.onestep.onestep1000li.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Getter @Setter
@NoArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT 처리
    @Column(name = "task_id")
    private Integer taskId;

    // 여러 과제가 하나의 유저에 속하므로 다대일(N:1) 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String content;

    @Column(name = "is_main", nullable = false)
    private Boolean isMain = false;

    // 하위 과제일 경우 어떤 메인 과제에 속하는지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_task_id")
    private Task parentTask;

    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;

    @Column(name = "is_joint", nullable = false)
    private Boolean isJoint = false;

    @Column(name = "origin_task_id")
    private Integer originTaskId;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private Integer originalTaskId;
}