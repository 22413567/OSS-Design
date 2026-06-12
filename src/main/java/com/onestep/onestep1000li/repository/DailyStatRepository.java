package com.onestep.onestep1000li.repository;

import com.onestep.onestep1000li.entity.DailyStat;
import com.onestep.onestep1000li.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyStatRepository extends JpaRepository<DailyStat, Integer> {
    Optional<DailyStat> findByUserAndStatDate(User user, LocalDate statDate);
    List<DailyStat> findByUserAndStatDateBetweenOrderByStatDateAsc(User user, LocalDate startDate, LocalDate endDate);
}