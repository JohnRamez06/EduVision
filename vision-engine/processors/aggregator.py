from collections import Counter


class Aggregator:
    """
    Aggregates per-face detections into per-frame summary.
    """

    def summarize(self, analysis: dict) -> dict:
        people = analysis.get("people", [])
        emotions = [p.get("dominant_emotion", "neutral") for p in people]
        conc_levels = [p.get("concentration", {}).get("level", "unknown") for p in people]

        emotion_counts = dict(Counter(emotions))
        concentration_counts = dict(Counter(conc_levels))

        # Engagement heuristic: happy/neutral = higher engagement (placeholder)
        engaged_emotions = {"happy", "neutral", "surprised"}
        engaged = sum(1 for e in emotions if e in engaged_emotions)
        engagement_score = engaged / max(1, len(people))

        return {
            "student_count": int(len(people)),
            "emotion_counts": emotion_counts,
            "concentration_counts": concentration_counts,
            "engagement_score": float(engagement_score),
        }
    # aggregator.py — updated summarize() and collect(); merge into existing class

import time
from collections import defaultdict
import logging

logger = logging.getLogger(__name__)

FLUSH_INTERVAL_SECONDS = 60

# Inside your existing Aggregator class:

def __init__(self, spring_client):
    self.spring_client = spring_client
    self._buffer: dict[str, list] = defaultdict(list)   # session_id -> frames
    self._last_flush: dict[str, float] = {}             # session_id -> timestamp
    self._known_students: dict[str, set] = defaultdict(set)   # session_id -> student_ids seen

def collect(self, session_id: str, frame_results: list[dict]):
    """Buffer per-frame face data."""
    now = time.time()
    self._buffer[session_id].extend(frame_results)

    # Track student presence for attendance
    for r in frame_results:
        if r["student_id"]:
            self._known_students[session_id].add(r["student_id"])

    # Flush every 60 seconds
    if now - self._last_flush.get(session_id, 0) >= FLUSH_INTERVAL_SECONDS:
        self.flush(session_id)
        self._last_flush[session_id] = now

def flush(self, session_id: str):
    """Summarize buffered data and send to Spring Boot."""
    frames = self._buffer.pop(session_id, [])
    if not frames:
        return
    summary = self.summarize(session_id, frames)
    self.spring_client.send_class_snapshot(session_id, summary)

    # Per-student snapshots
    per_student = self._build_student_snapshots(session_id, frames)
    if per_student:
        snapshot_id = summary.get("snapshot_id")
        self.spring_client.send_student_snapshots(session_id, snapshot_id, per_student)

def summarize(self, session_id: str, frames: list[dict]) -> dict:
    """
    Aggregate a list of per-face dicts into a class-level snapshot.
    """
    EMOTION_KEYS = ["happy", "neutral", "confused", "sad", "surprised", "angry"]
    counts = {e: 0 for e in EMOTION_KEYS}
    total_concentration = 0.0
    face_count = 0
    seen_students = set()

    for face in frames:
        face_count += 1
        emotion = face.get("emotion", "neutral").lower()
        if emotion in counts:
            counts[emotion] += 1
        total_concentration += face.get("concentration", 0.0)
        if face.get("student_id"):
            seen_students.add(face["student_id"])

    avg_concentration = (total_concentration / face_count) if face_count else 0.0

    # Engagement score: weighted blend of concentration + positive emotions
    positive_ratio = (counts["happy"] + counts["neutral"]) / face_count if face_count else 0
    engagement_score = round((avg_concentration * 0.6 + positive_ratio * 0.4) * 100, 2)

    return {
        "happyCount":       counts["happy"],
        "neutralCount":     counts["neutral"],
        "confusedCount":    counts["confused"],
        "sadCount":         counts["sad"],
        "surprisedCount":   counts["surprised"],
        "angryCount":       counts["angry"],
        "avgConcentration": round(avg_concentration, 4),
        "totalFaces":       face_count,
        "uniqueStudents":   len(seen_students),
        "engagementScore":  engagement_score,
    }

def _build_student_snapshots(self, session_id: str, frames: list[dict]) -> list[dict]:
    """Aggregate per-student data from buffered frames."""
    student_data: dict[str, dict] = defaultdict(lambda: {
        "emotions": [], "concentrations": [], "confidences": []
    })

    for face in frames:
        sid = face.get("student_id")
        if not sid:
            continue
        student_data[sid]["emotions"].append(face.get("emotion", "neutral"))
        student_data[sid]["concentrations"].append(face.get("concentration", 0.0))
        student_data[sid]["confidences"].append(face.get("confidence", 0.0))

    snapshots = []
    for student_id, data in student_data.items():
        dominant_emotion = max(set(data["emotions"]), key=data["emotions"].count)
        avg_concentration = sum(data["concentrations"]) / len(data["concentrations"])
        avg_confidence    = sum(data["confidences"])    / len(data["confidences"])
        is_attentive      = avg_concentration >= 0.6
        is_drowsy         = avg_concentration < 0.3

        snapshots.append({
            "studentId":        student_id,
            "dominantEmotion":  dominant_emotion,
            "concentration":    round(avg_concentration, 4),
            "confidenceScore":  round(avg_confidence, 4),
            "isDrowsy":         is_drowsy,
            "isAttentive":      is_attentive,
            "sessionId":        session_id,
        })
    return snapshots