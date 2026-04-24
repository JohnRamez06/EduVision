package com.eduvision.service;

import com.eduvision.model.Strategy;
import com.eduvision.model.StrategyType;
import com.eduvision.repository.StrategyRepository;
import com.eduvision.strategy.alert.AlertStrategy;
import com.eduvision.strategy.authorization.AuthorizationStrategy;
import com.eduvision.strategy.privacy.PrivacyStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class StrategyRegistryService {

    @Autowired
    private StrategyRepository strategyRepository;

    @Autowired
    private ApplicationContext applicationContext;

    private Map<String, AlertStrategy> alertStrategies = new HashMap<>();
    private Map<String, AuthorizationStrategy> authStrategies = new HashMap<>();
    private Map<String, PrivacyStrategy> privacyStrategies = new HashMap<>();

    public void loadStrategies() {
        List<Strategy> strategies = strategyRepository.findAll();
        for (Strategy strategy : strategies) {
            if (!strategy.isActive()) continue;
            try {
                Class<?> clazz = Class.forName(strategy.getHandlerClass());
                Object bean = applicationContext.getBean(clazz);
                switch (strategy.getType()) {
                    case alert:
                        alertStrategies.put(strategy.getId(), (AlertStrategy) bean);
                        break;
                    case authorization:
                        authStrategies.put(strategy.getId(), (AuthorizationStrategy) bean);
                        break;
                    case privacy:
                        privacyStrategies.put(strategy.getId(), (PrivacyStrategy) bean);
                        break;
                }
            } catch (Exception e) {
                // Log error
            }
        }
    }

    public Object getStrategy(StrategyType type, String id) {
        switch (type) {
            case alert: return alertStrategies.get(id);
            case authorization: return authStrategies.get(id);
            case privacy: return privacyStrategies.get(id);
            default: return null;
        }
    }

    public Object getDefaultStrategy(StrategyType type) {
       List<Strategy> defaults = strategyRepository.findByTypeAndDefaultStrategyTrue(type);
        if (!defaults.isEmpty()) {
            return getStrategy(type, defaults.get(0).getId());
        }
        return null;
    }

    public void reloadCache() {
        alertStrategies.clear();
        authStrategies.clear();
        privacyStrategies.clear();
        loadStrategies();
    }
}