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