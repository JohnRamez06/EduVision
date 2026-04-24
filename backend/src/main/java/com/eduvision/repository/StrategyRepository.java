package com.eduvision.repository;

import com.eduvision.model.Strategy;
import com.eduvision.model.StrategyType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StrategyRepository extends JpaRepository<Strategy, String> {
    
    List<Strategy> findByTypeAndActiveTrue(StrategyType type);
    
    // ✅ CORRECT: Matches field name 'defaultStrategy'
    List<Strategy> findByTypeAndDefaultStrategyTrue(StrategyType type);
    
    Optional<Strategy> findByName(String name);
    
    List<Strategy> findByType(StrategyType type);
}