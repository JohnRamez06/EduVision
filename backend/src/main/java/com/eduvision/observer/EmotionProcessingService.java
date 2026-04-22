package com.eduvision.observer;

import com.eduvision.dto.emotion.EmotionSnapshotDTO;
import com.eduvision.dto.emotion.StudentEmotionDTO;
import com.eduvision.model.ConcentrationLevel;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class EmotionProcessingService {

    private static final BigDecimal ENGAGEMENT_THRESHOLD = BigDecimal.valueOf(0.55);
    private static final BigDecimal LOW_CONFIDENCE_THRESHOLD = BigDecimal.valueOf(0.45);

    private final ApplicationEventPublisher eventPublisher;
    private final List<SentimentEventObserver> observers = new ArrayList<>();

    public EmotionProcessingService() {
        this.eventPublisher = null;
    }

    public EmotionProcessingService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void subscribe(SentimentEventObserver observer) {
        observers.add(observer);
    }

    public void unsubscribe(SentimentEventObserver observer) {
        observers.remove(observer);
    }

    public void publish(SentimentAlertEvent event) {
        for (SentimentEventObserver observer : observers) {
            observer.onEvent(event);
        }
        if (eventPublisher != null) {
            eventPublisher.publishEvent(event);
        }
    }

    public void processClassSnapshot(EmotionSnapshotDTO snapshot) {
        if (snapshot == null || snapshot.getEngagementScore() == null) {
            return;
        }

        BigDecimal concentration = snapshot.getAvgConcentration() == null
                ? BigDecimal.ZERO
                : snapshot.getAvgConcentration();

        if (snapshot.getEngagementScore().compareTo(ENGAGEMENT_THRESHOLD) < 0) {
            publish(new SentimentAlertEvent(
                    snapshot.getSessionId(),
                    snapshot.getSnapshotId(),
                    snapshot.getEngagementScore(),
                    concentration));
        }
    }

    public void processStudentSnapshots(EmotionSnapshotDTO classSnapshot, List<StudentEmotionDTO> studentSnapshots) {
        if (studentSnapshots == null || studentSnapshots.isEmpty()) {
            return;
        }

        for (StudentEmotionDTO student : studentSnapshots) {
            if (student.getConfidenceScore() != null
                    && student.getConfidenceScore().compareTo(LOW_CONFIDENCE_THRESHOLD) < 0) {
                publishStudentAtRisk(classSnapshot, student);
            }

            if (isLowConcentration(student.getConcentration())) {
                publishConcentrationDrop(classSnapshot, student);
            }
        }
    }

    private void publishStudentAtRisk(EmotionSnapshotDTO classSnapshot, StudentEmotionDTO student) {
        StudentAtRiskEvent event = new StudentAtRiskEvent(
                student.getSessionId(),
                student.getSnapshotId(),
                student.getStudentId(),
                student.getConfidenceScore());
        if (eventPublisher != null) {
            eventPublisher.publishEvent(event);
        }
    }

    private void publishConcentrationDrop(EmotionSnapshotDTO classSnapshot, StudentEmotionDTO student) {
        ConcentrationDropEvent event = new ConcentrationDropEvent(
                student.getSessionId(),
                student.getSnapshotId(),
                student.getStudentId(),
                classSnapshot != null ? classSnapshot.getAvgConcentration() : BigDecimal.ZERO);
        if (eventPublisher != null) {
            eventPublisher.publishEvent(event);
        }
    }

    private boolean isLowConcentration(String concentration) {
        if (concentration == null) {
            return false;
        }

        try {
            ConcentrationLevel level = ConcentrationLevel.valueOf(concentration.toLowerCase(Locale.ROOT));
            return level == ConcentrationLevel.low || level == ConcentrationLevel.distracted;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
