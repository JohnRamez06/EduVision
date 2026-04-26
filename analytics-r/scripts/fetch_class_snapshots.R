# =============================================================================
# EduVision - Fetch Class-Level Emotion Snapshots
# scripts/fetch_class_snapshots.R
# =============================================================================

BASE_DIR <- normalizePath(file.path(dirname(sys.frame(1)$ofile), ".."))
if (!exists("fetch_query")) source(file.path(BASE_DIR, "scripts", "fetch_data.R"))

fetch_class_snapshots <- function(session_id) {
  fetch_query(sprintf(
    "SELECT es.id, es.seq_index, es.captured_at,
            es.student_count, es.avg_concentration,
            es.dominant_emotion, es.engagement_score, es.processing_ms
     FROM emotion_snapshots es
     WHERE es.session_id = '%s'
     ORDER BY es.seq_index ASC", session_id
  ))
}

fetch_class_snapshots_range <- function(session_id, from_seq, to_seq) {
  fetch_query(sprintf(
    "SELECT * FROM emotion_snapshots
     WHERE session_id = '%s' AND seq_index BETWEEN %d AND %d
     ORDER BY seq_index ASC", session_id, from_seq, to_seq
  ))
}