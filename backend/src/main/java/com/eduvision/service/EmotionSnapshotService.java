package com.eduvision.service;

import com.eduvision.dto.emotion.AggregatedEmotionDTO;
import com.eduvision.dto.emotion.EmotionSnapshotDTO;
import com.eduvision.dto.emotion.StudentEmotionDTO;
import com.eduvision.exception.ResourceNotFoundException;
import com.eduvision.iterator.SentimentHistoryBuffer;
import com.eduvision.model.CameraConfiguration;
import com.eduvision.model.ConcentrationLevel;
import com.eduvision.model.EmotionSnapshot;
import com.eduvision.model.EmotionType;
import com.eduvision.model.LectureSession;
import com.eduvision.model.StudentEmotionSnapshot;
import com.eduvision.model.User;
import com.eduvision.observer.EmotionProcessingService;
import com.eduvision.repository.CameraConfigurationRepository;
import com.eduvision.repository.EmotionSnapshotRepository;
import com.eduvision.repository.SessionRepository;
import com.eduvision.repository.StudentEmotionSnapshotRepository;
import com.eduvision.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmotionSnapshotService {

    private final EmotionSnapshotRepository emotionSnapshotRepository;
    private final StudentEmotionSnapshotRepository studentEmotionSnapshotRepository;
    private final SessionRepository sessionRepository;
    private final CameraConfigurationRepository cameraConfigurationRepository;
    private final UserRepository userRepository;
    private final EmotionProcessingService emotionProcessingService;

    public EmotionSnapshotService(EmotionSnapshotRepository emotionSnapshotRepository,
                                  StudentEmotionSnapshotRepository studentEmotionSnapshotRepository,
                                  SessionRepository sessionRepository,
                                  CameraConfigurationRepository cameraConfigurationRepository,
                                  UserRepository userRepository,
                                  EmotionProcessingService emotionProcessingService) {
        this.emotionSnapshotRepository = emotionSnapshotRepository;
        this.studentEmotionSnapshotRepository = studentEmotionSnapshotRepository;
        this.sessionRepository = sessionRepository;
        this.cameraConfigurationRepository = cameraConfigurationRepository;
        this.userRepository = userRepository;
        this.emotionProcessingService = emotionProcessingService;
    }

    @Transactional
    public EmotionSnapshotDTO saveClassSnapshot(EmotionSnapshotDTO dto) {
        LectureSession session = sessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + dto.getSessionId()));

        EmotionSnapshot snapshot = new EmotionSnapshot();
        snapshot.setId(dto.getId() == null ? UUID.randomUUID().toString() : dto.getId());
        snapshot.setSession(session);
        snapshot.setSeqIndex(dto.getSeqIndex() == null ? System.currentTimeMillis() : dto.getSeqIndex());
        snapshot.setCapturedAt(dto.getCapturedAt() == null ? LocalDateTime.now() : dto.getCapturedAt());
        snapshot.setFrameUrl(dto.getFrameUrl());
        snapshot.setStudentCount(dto.getStudentCount());
        snapshot.setAvgConcentration(dto.getAvgConcentration() == null ? BigDecimal.ZERO : dto.getAvgConcentration());
        snapshot.setDominantEmotion(dto.getDominantEmotion());
        snapshot.setEngagementScore(dto.getEngagementScore() == null ? BigDecimal.ZERO : dto.getEngagementScore());
        snapshot.setRawPayload(dto.getRawPayload());
        snapshot.setProcessingMs(dto.getProcessingMs());

        if (dto.getCameraId() != null) {
            CameraConfiguration camera = cameraConfigurationRepository.findById(dto.getCameraId())
                    .orElseThrow(() -> new ResourceNotFoundException("Camera not found: " + dto.getCameraId()));
            snapshot.setCamera(camera);
        }

        EmotionSnapshot saved = emotionSnapshotRepository.save(snapshot);
        emotionProcessingService.processClassSnapshot(saved);
        return toSnapshotDto(saved);
    }

    @Transactional
    public List<StudentEmotionDTO> saveStudentSnapshots(String snapshotId, List<StudentEmotionDTO> dtos) {
        EmotionSnapshot snapshot = emotionSnapshotRepository.findById(snapshotId)
                .orElseThrow(() -> new ResourceNotFoundException("Snapshot not found: " + snapshotId));

        List<StudentEmotionSnapshot> entities = dtos.stream().map(dto -> {
            User student = userRepository.findById(dto.getStudentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + dto.getStudentId()));

            StudentEmotionSnapshot entity = new StudentEmotionSnapshot();
            entity.setId(dto.getId() == null ? UUID.randomUUID().toString() : dto.getId());
            entity.setSnapshot(snapshot);
            entity.setSession(snapshot.getSession());
            entity.setStudent(student);
            entity.setEmotion(dto.getEmotion() == null ? EmotionType.neutral : dto.getEmotion());
            entity.setConcentration(dto.getConcentration() == null ? ConcentrationLevel.medium : dto.getConcentration());
            entity.setConfidenceScore(dto.getConfidenceScore() == null ? BigDecimal.ZERO : dto.getConfidenceScore());
            entity.setBoundingBox(dto.getBoundingBox());
            entity.setGazeDirection(dto.getGazeDirection());
            entity.setCapturedAt(dto.getCapturedAt() == null ? LocalDateTime.now() : dto.getCapturedAt());
            entity.setAnonymised(dto.isAnonymised());
            return entity;
        }).toList();

        List<StudentEmotionSnapshot> saved = studentEmotionSnapshotRepository.saveAll(entities);
        emotionProcessingService.processStudentSnapshots(saved);
        return saved.stream().map(this::toStudentDto).toList();
    }

    @Transactional(readOnly = true)
    public AggregatedEmotionDTO getSessionHistory(String sessionId) {
        List<EmotionSnapshot> snapshots = emotionSnapshotRepository.findBySessionIdOrderByCapturedAtAsc(sessionId);
        List<StudentEmotionSnapshot> studentSnapshots =
                studentEmotionSnapshotRepository.findBySessionIdOrderByCapturedAtAsc(sessionId);

        SentimentHistoryBuffer historyBuffer = new SentimentHistoryBuffer(Math.max(1, snapshots.size()));
        snapshots.forEach(historyBuffer::add);

        AggregatedEmotionDTO response = new AggregatedEmotionDTO();
        response.setSessionId(sessionId);
        response.setSnapshots(historyBuffer.asList().stream().map(this::toSnapshotDto).toList());
        response.setStudentSnapshots(studentSnapshots.stream().map(this::toStudentDto).toList());
        return response;
    }

    @Transactional(readOnly = true)
    public EmotionSnapshotDTO getLatestSnapshot(String sessionId) {
        EmotionSnapshot latest = emotionSnapshotRepository.findTopBySessionIdOrderByCapturedAtDesc(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("No snapshots found for session: " + sessionId));
        return toSnapshotDto(latest);
    }

    private EmotionSnapshotDTO toSnapshotDto(EmotionSnapshot snapshot) {
        EmotionSnapshotDTO dto = new EmotionSnapshotDTO();
        dto.setId(snapshot.getId());
        dto.setSessionId(snapshot.getSession() == null ? null : snapshot.getSession().getId());
        dto.setCameraId(snapshot.getCamera() == null ? null : snapshot.getCamera().getId());
        dto.setSeqIndex(snapshot.getSeqIndex());
        dto.setCapturedAt(snapshot.getCapturedAt());
        dto.setFrameUrl(snapshot.getFrameUrl());
        dto.setStudentCount(snapshot.getStudentCount());
        dto.setAvgConcentration(snapshot.getAvgConcentration());
        dto.setDominantEmotion(snapshot.getDominantEmotion());
        dto.setEngagementScore(snapshot.getEngagementScore());
        dto.setRawPayload(snapshot.getRawPayload());
        dto.setProcessingMs(snapshot.getProcessingMs());
        return dto;
    }

    private StudentEmotionDTO toStudentDto(StudentEmotionSnapshot snapshot) {
        StudentEmotionDTO dto = new StudentEmotionDTO();
        dto.setId(snapshot.getId());
        dto.setSnapshotId(snapshot.getSnapshot() == null ? null : snapshot.getSnapshot().getId());
        dto.setSessionId(snapshot.getSession() == null ? null : snapshot.getSession().getId());
        dto.setStudentId(snapshot.getStudent() == null ? null : snapshot.getStudent().getId());
        dto.setEmotion(snapshot.getEmotion());
        dto.setConcentration(snapshot.getConcentration());
        dto.setConfidenceScore(snapshot.getConfidenceScore());
        dto.setBoundingBox(snapshot.getBoundingBox());
        dto.setGazeDirection(snapshot.getGazeDirection());
        dto.setCapturedAt(snapshot.getCapturedAt());
        dto.setAnonymised(snapshot.isAnonymised());
        return dto;
    }
}
