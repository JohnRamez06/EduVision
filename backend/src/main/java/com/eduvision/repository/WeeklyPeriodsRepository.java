package com.eduvision.repository;

import com.eduvision.model.WeeklyPeriods;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WeeklyPeriodsRepository extends JpaRepository<WeeklyPeriods, String> {

    Optional<WeeklyPeriods> findTopByWeekNumberAndYear(int weekNumber, int year);
}
