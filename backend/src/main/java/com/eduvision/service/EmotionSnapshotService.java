package com.eduvision.service;

import com.eduvision.dto.emotion.AggregatedEmotionDTO;
import com.eduvision.dto.emotion.EmotionSnapshotDTO;
import com.eduvision.dto.emotion.StudentEmotionDTO;
import com.eduvision.exception.ResourceNotFoundException;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmotionSnapshotService {

    private final EmotionSnapshotRepository emotionSnapshotRepository;
    private final StudentEmotionSnapshotRepository studentEmotionSnapshotRepository;
    private final SessionRepository sessionRepository;
    private final UserRepository userRepository;
    private final CameraConfigurationRepository cameraConfigurationRepository;
    private final EmotionProcessingService emotionProcessingService;

    public EmotionSnapshotService(EmotionSnapshotRepository emotionSnapshotRepository,
                                  StudentEmotionSnapshotRepository studentEmotionSnapshotRepository,
                                  SessionRepository sessionRepository,
                                  UserRepository userRepository,
                                  CameraConfigurationRepository cameraConfigurationRepository,
                                  EmotionProcessingService emotionProcessingService) {
        this.emotionSnapshotRepository = emotionSnapshotRepository;
        this.studentEmotionSnapshotRepository = studentEmotionSnapshotRepository;
        this.sessionRepository = sessionRepository;
        this.userRepository = userRepository;
        this.cameraConfigurationRepository = cameraConfigurationRepository;
        this.emotionProcessingService = emotionProcessingService;
    }

    @Transactional
    public EmotionSnapshotDTO saveClassSnapshot(EmotionSnapshotDTO dto) {
        LectureSession session = sessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + dto.getSessionId()));

        EmotionSnapshot snapshot = new EmotionSnapshot();
        snapshot.setId(dto.getSnapshotId() != null ? dto.getSnapshotId() : UUID.randomUUID().toString());
        snapshot.setSession(session);
        snapshot.setSeqIndex(dto.getSeqIndex() != null ? dto.getSeqIndex() : 0L);
        snapshot.setCapturedAt(dto.getCapturedAt() != null ? dto.getCapturedAt() : LocalDateTime.now());
        snapshot.setFrameUrl(dto.getFrameUrl());
        snapshot.setStudentCount((short) (dto.getStudentCount() != null ? dto.getStudentCount() : 0));
        snapshot.setAvgConcentration(dto.getAvgConcentration());
        snapshot.setDominantEmotion(parseEmotion(dto.getDominantEmotion()));
        snapshot.setEngagementScore(dto.getEngagementScore());
        snapshot.setRawPayload(dto.getRawPayload());
        snapshot.setProcessingMs(dto.getProcessingMs());

        if (dto.getCameraId() != null) {
            CameraConfiguration camera = cameraConfigurationRepository.findById(dto.getCameraId())
                    .orElseThrow(() -> new ResourceNotFoundException("Camera not found: " + dto.getCameraId()));
            snapshot.setCamera(camera);
        }

        EmotionSnapshot saved = emotionSnapshotRepository.save(snapshot);
        EmotionSnapshotDTO mapped = toDto(saved);
        emotionProcessingService.processClassSnapshot(mapped);
        return mapped;
    }

    @Transactional
    public List<StudentEmotionDTO> saveStudentSnapshots(String snapshotId, List<StudentEmotionDTO> studentDtos) {
        EmotionSnapshot snapshot = emotionSnapshotRepository.findById(snapshotId)
                .orElseThrow(() -> new ResourceNotFoundException("Snapshot not found: " + snapshotId));

        List<StudentEmotionSnapshot> toSave = new ArrayList<>();
        for (StudentEmotionDTO dto : studentDtos) {
            StudentEmotionSnapshot studentSnapshot = new StudentEmotionSnapshot();
            studentSnapshot.setId(dto.getId() != null ? dto.getId() : UUID.randomUUID().toString());
            studentSnapshot.setSnapshot(snapshot);
            studentSnapshot.setSession(snapshot.getSession());
            studentSnapshot.setStudent(resolveStudent(dto.getStudentId()));
            studentSnapshot.setEmotion(parseEmotion(dto.getEmotion()));
            studentSnapshot.setConcentration(parseConcentration(dto.getConcentration()));
            studentSnapshot.setConfidenceScore(dto.getConfidenceScore() == null ? BigDecimal.ZERO : dto.getConfidenceScore());
            studentSnapshot.setBoundingBox(dto.getBoundingBox());
            studentSnapshot.setGazeDirection(dto.getGazeDirection());
            studentSnapshot.setCapturedAt(dto.getCapturedAt() != null ? dto.getCapturedAt() : LocalDateTime.now());
            studentSnapshot.setAnonymised(dto.isAnonymised());
            toSave.add(studentSnapshot);
        }

        List<StudentEmotionSnapshot> saved = studentEmotionSnapshotRepository.saveAll(toSave);
        EmotionSnapshotDTO classSnapshot = toDto(snapshot);
        List<StudentEmotionDTO> response = saved.stream().map(this::toDto).toList();
        emotionProcessingService.processStudentSnapshots(classSnapshot, response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<AggregatedEmotionDTO> getSessionHistory(String sessionId) {
        List<EmotionSnapshot> snapshots = emotionSnapshotRepository.findBySessionIdOrderByCapturedAtAsc(sessionId);
        List<AggregatedEmotionDTO> history = new ArrayList<>();
        for (EmotionSnapshot snapshot : snapshots) {
            AggregatedEmotionDTO aggregate = new AggregatedEmotionDTO();
            aggregate.setClassSnapshot(toDto(snapshot));
            List<StudentEmotionDTO> students = studentEmotionSnapshotRepository.findBySnapshotId(snapshot.getId())
                    .stream()
                    .map(this::toDto)
                    .toList();
            aggregate.setStudentSnapshots(students);
            history.add(aggregate);
        }
        return history;
    }

    @Transactional(readOnly = true)
    public AggregatedEmotionDTO getLatestSnapshot(String sessionId) {
        EmotionSnapshot snapshot = emotionSnapshotRepository.findTopBySessionIdOrderByCapturedAtDesc(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("No snapshots found for session: " + sessionId));

        AggregatedEmotionDTO aggregate = new AggregatedEmotionDTO();
        aggregate.setClassSnapshot(toDto(snapshot));
        aggregate.setStudentSnapshots(studentEmotionSnapshotRepository.findBySnapshotId(snapshot.getId())
                .stream()
                .map(this::toDto)
                .toList());
        return aggregate;
    }

    private User resolveStudent(String studentId) {
        if (studentId == null) {
            return null;
        }
        return userRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
    }

    private EmotionType parseEmotion(String value) {
        if (value == null) {
            return EmotionType.neutral;
        }
        return EmotionType.valueOf(value.toLowerCase(Locale.ROOT));
    }

    private ConcentrationLevel parseConcentration(String value) {
        if (value == null) {
            return ConcentrationLevel.medium;
        }
        return ConcentrationLevel.valueOf(value.toLowerCase(Locale.ROOT));
    }

    private EmotionSnapshotDTO toDto(EmotionSnapshot snapshot) {
        EmotionSnapshotDTO dto = new EmotionSnapshotDTO();
        dto.setSnapshotId(snapshot.getId());
        dto.setSessionId(snapshot.getSession().getId());
        dto.setCameraId(snapshot.getCamera() != null ? snapshot.getCamera().getId() : null);
        dto.setSeqIndex(snapshot.getSeqIndex());
        dto.setCapturedAt(snapshot.getCapturedAt());
        dto.setFrameUrl(snapshot.getFrameUrl());
        dto.setStudentCount((int) snapshot.getStudentCount());
        dto.setAvgConcentration(snapshot.getAvgConcentration());
        dto.setDominantEmotion(snapshot.getDominantEmotion() != null ? snapshot.getDominantEmotion().name() : null);
        dto.setEngagementScore(snapshot.getEngagementScore());
        dto.setRawPayload(snapshot.getRawPayload());
        dto.setProcessingMs(snapshot.getProcessingMs());
        return dto;
    }

    private StudentEmotionDTO toDto(StudentEmotionSnapshot snapshot) {
        StudentEmotionDTO dto = new StudentEmotionDTO();
        dto.setId(snapshot.getId());
        dto.setSnapshotId(snapshot.getSnapshot().getId());
        dto.setSessionId(snapshot.getSession().getId());
        dto.setStudentId(snapshot.getStudent() != null ? snapshot.getStudent().getId() : null);
        dto.setEmotion(snapshot.getEmotion() != null ? snapshot.getEmotion().name() : null);
        dto.setConcentration(snapshot.getConcentration() != null ? snapshot.getConcentration().name() : null);
        dto.setConfidenceScore(snapshot.getConfidenceScore());
        dto.setBoundingBox(snapshot.getBoundingBox());
        dto.setGazeDirection(snapshot.getGazeDirection());
        dto.setCapturedAt(snapshot.getCapturedAt());
        dto.setAnonymised(snapshot.isAnonymised());
        return dto;
    }
}
