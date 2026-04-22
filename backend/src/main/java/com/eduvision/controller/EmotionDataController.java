package com.eduvision.controller;

import com.eduvision.dto.emotion.AggregatedEmotionDTO;
import com.eduvision.dto.emotion.EmotionSnapshotDTO;
import com.eduvision.dto.emotion.StudentEmotionDTO;
import com.eduvision.service.EmotionSnapshotService;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/emotion-data")
public class EmotionDataController {

    private final EmotionSnapshotService emotionSnapshotService;

    public EmotionDataController(EmotionSnapshotService emotionSnapshotService) {
        this.emotionSnapshotService = emotionSnapshotService;
    }

    @PostMapping("/class-snapshot")
    public ResponseEntity<EmotionSnapshotDTO> saveClassSnapshot(@RequestBody EmotionSnapshotDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(emotionSnapshotService.saveClassSnapshot(request));
    }

    @PostMapping("/student-snapshots")
    public ResponseEntity<List<StudentEmotionDTO>> saveStudentSnapshots(
            @RequestParam("snapshotId") String snapshotId,
            @RequestBody List<StudentEmotionDTO> request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(emotionSnapshotService.saveStudentSnapshots(snapshotId, request));
    }

    @GetMapping("/session/{id}")
    public ResponseEntity<?> getSessionHistory(@PathVariable("id") String sessionId,
                                               @RequestParam(value = "latest", defaultValue = "false") boolean latest) {
        if (latest) {
            AggregatedEmotionDTO snapshot = emotionSnapshotService.getLatestSnapshot(sessionId);
            return ResponseEntity.ok(snapshot);
        }
        return ResponseEntity.ok(Map.of("sessionId", sessionId, "history", emotionSnapshotService.getSessionHistory(sessionId)));
    }
}
