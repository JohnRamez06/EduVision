package com.eduvision.observer;

import com.eduvision.model.AlertSeverity;
import com.eduvision.model.ConcentrationLevel;
import com.eduvision.model.EmotionSnapshot;
import com.eduvision.model.StudentEmotionSnapshot;
import com.eduvision.repository.EmotionSnapshotRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class EmotionProcessingService {

    private static final BigDecimal ENGAGEMENT_WARNING_THRESHOLD = new BigDecimal("0.55");
    private static final BigDecimal ENGAGEMENT_CRITICAL_THRESHOLD = new BigDecimal("0.30");
    private static final BigDecimal CONCENTRATION_DROP_THRESHOLD = new BigDecimal("0.20");

    private final List<SentimentEventObserver> observers = new ArrayList<>();
    private final ApplicationEventPublisher eventPublisher;
    private final EmotionSnapshotRepository emotionSnapshotRepository;

    public EmotionProcessingService(ApplicationEventPublisher eventPublisher,
                                    EmotionSnapshotRepository emotionSnapshotRepository) {
        this.eventPublisher = eventPublisher;
        this.emotionSnapshotRepository = emotionSnapshotRepository;
    }

    public EmotionProcessingService() {
        this.eventPublisher = null;
        this.emotionSnapshotRepository = null;
    }

    public void subscribe(SentimentEventObserver observer) {
        observers.add(observer);
    }

    public void unsubscribe(SentimentEventObserver observer) {
        observers.remove(observer);
    }

    public void publish(SentimentAlertEvent event) {
        observers.forEach(observer -> observer.onEvent(event));
        if (eventPublisher != null) {
            eventPublisher.publishEvent(event);
        }
    }

    public void processClassSnapshot(EmotionSnapshot snapshot) {
        if (snapshot == null || snapshot.getSession() == null || snapshot.getEngagementScore() == null) {
            return;
        }

        BigDecimal engagement = snapshot.getEngagementScore();
        BigDecimal concentration = snapshot.getAvgConcentration() == null
                ? BigDecimal.ZERO : snapshot.getAvgConcentration();

        if (engagement.compareTo(ENGAGEMENT_WARNING_THRESHOLD) < 0) {
            AlertSeverity severity = engagement.compareTo(ENGAGEMENT_CRITICAL_THRESHOLD) < 0
                    ? AlertSeverity.critical
                    : AlertSeverity.warning;
            SentimentAlertEvent alertEvent = new SentimentAlertEvent(
                    snapshot.getSession().getId(),
                    snapshot.getId(),
                    engagement.doubleValue(),
                    concentration.doubleValue(),
                    severity,
                    "Class engagement dropped below threshold");
            publish(alertEvent);
        }

        publishConcentrationDropIfNeeded(snapshot, concentration);
    }

    private void publishConcentrationDropIfNeeded(EmotionSnapshot snapshot, BigDecimal concentration) {
        if (emotionSnapshotRepository == null) {
            return;
        }

        List<EmotionSnapshot> history = emotionSnapshotRepository
                .findBySessionIdOrderByCapturedAtAsc(snapshot.getSession().getId());
        if (history.size() < 2) {
            return;
        }

        EmotionSnapshot previous = history.get(history.size() - 2);
        if (previous.getAvgConcentration() == null) {
            return;
        }

        BigDecimal drop = previous.getAvgConcentration().subtract(concentration);
        if (drop.compareTo(CONCENTRATION_DROP_THRESHOLD) >= 0 && eventPublisher != null) {
            eventPublisher.publishEvent(new ConcentrationDropEvent(
                    snapshot.getSession().getId(),
                    snapshot.getId(),
                    previous.getAvgConcentration().doubleValue(),
                    concentration.doubleValue()));
        }
    }

    public void processStudentSnapshots(List<StudentEmotionSnapshot> studentSnapshots) {
        if (studentSnapshots == null || studentSnapshots.isEmpty() || eventPublisher == null) {
            return;
        }

        for (StudentEmotionSnapshot studentSnapshot : studentSnapshots) {
            if (studentSnapshot.getSession() == null || studentSnapshot.getStudent() == null) {
                continue;
            }

            ConcentrationLevel concentration = studentSnapshot.getConcentration();
            if (concentration == ConcentrationLevel.low || concentration == ConcentrationLevel.distracted) {
                eventPublisher.publishEvent(new StudentAtRiskEvent(
                        studentSnapshot.getSession().getId(),
                        studentSnapshot.getStudent().getId(),
                        studentSnapshot.getSnapshot() == null ? null : studentSnapshot.getSnapshot().getId(),
                        studentSnapshot.getEmotion(),
                        concentration));
            }
        }
    }
}
