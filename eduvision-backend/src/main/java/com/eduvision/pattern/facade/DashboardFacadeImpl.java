package com.eduvision.pattern.facade;
import org.springframework.stereotype.Service;
@Service
public class DashboardFacadeImpl implements DashboardFacade {
    @Override
    public Object getSessionDashboard(String sessionId) {
        // TODO: aggregate from SessionService, EmotionService, AlertService
        return null;
    }
}
