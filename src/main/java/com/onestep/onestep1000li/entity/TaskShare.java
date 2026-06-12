package com.onestep.onestep1000li.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class TaskShare {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task; // 원본 메인 과제

    @ManyToOne
    @JoinColumn(name = "cloned_task_id")
    private Task clonedTask; 

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}