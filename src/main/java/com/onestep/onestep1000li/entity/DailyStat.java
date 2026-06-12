package com.onestep.onestep1000li.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "daily_stats")
@Getter @Setter
@NoArgsConstructor
public class DailyStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stat_id")
    private Integer statId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "achievement_rate", precision = 5, scale = 2)
    private BigDecimal achievementRate;

    @Column(name = "focus_time")
    private Integer focusTime = 0;

    @Column(name = "streak_days")
    private Integer streakDays = 0;
}