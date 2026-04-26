# =============================================================================
# scripts/fetch_student_snapshots.R
# =============================================================================
BASE_DIR <- normalizePath(file.path(dirname(sys.frame(1)$ofile), ".."))
if (!exists("fetch_query")) source(file.path(BASE_DIR, "scripts", "fetch_data.R"))

fetch_student_snapshots <- function(student_id, session_id = NULL) {
  if (!is.null(session_id)) {
    sql <- sprintf(
      "SELECT ses.id, ses.emotion, ses.concentration, ses.confidence_score,
              ses.is_anonymised, ses.captured_at,
              es.seq_index, es.engagement_score
       FROM student_emotion_snapshots ses
       JOIN emotion_snapshots es ON es.id = ses.snapshot_id
       WHERE ses.student_id = '%s' AND ses.session_id = '%s'
       ORDER BY es.seq_index ASC", student_id, session_id
    )
  } else {
    sql <- sprintf(
      "SELECT ses.*, es.seq_index, es.captured_at AS snap_time, ls.course_id
       FROM student_emotion_snapshots ses
       JOIN emotion_snapshots es ON es.id = ses.snapshot_id
       JOIN lecture_sessions ls  ON ls.id = ses.session_id
       WHERE ses.student_id = '%s'
       ORDER BY ses.captured_at DESC", student_id
    )
  }
  fetch_query(sql)
}

fetch_student_weekly_snapshots <- function(student_id, start_date, end_date) {
  fetch_query(sprintf(
    "SELECT ses.emotion, ses.concentration, ses.confidence_score, ses.captured_at,
            es.engagement_score, ls.id AS session_id, ls.course_id
     FROM student_emotion_snapshots ses
     JOIN emotion_snapshots es ON es.id = ses.snapshot_id
     JOIN lecture_sessions ls  ON ls.id = ses.session_id
     WHERE ses.student_id = '%s'
       AND ses.captured_at BETWEEN '%s' AND '%s'
     ORDER BY ses.captured_at ASC",
    student_id,
    format(start_date, "%Y-%m-%d %H:%M:%S"),
    format(end_date,   "%Y-%m-%d %H:%M:%S")
  ))
}