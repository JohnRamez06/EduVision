# ============================================================
# fetch_student_snapshots.R — Fetch per-student emotion snapshots
# ============================================================

SCRIPTS_DIR <- dirname(sys.frame(1)$ofile %||% ".")
source(file.path(dirname(SCRIPTS_DIR), "config.R"), local = TRUE)

#' Fetch student-level snapshots for one student in one session.
#' Returns: id, student_id, session_id, emotion, concentration,
#'          confidence_score, captured_at
fetch_student_snapshots <- function(student_id, session_id) {
  with_connection(function(con) {
    dbGetQuery(con, sqlInterpolate(con,
      "SELECT id, student_id, session_id, emotion, concentration,
              confidence_score, captured_at
         FROM student_emotion_snapshots
        WHERE student_id = ?sid AND session_id = ?sess
        ORDER BY captured_at ASC",
      sid  = student_id,
      sess = session_id))
  })
}

#' Fetch student snapshots for a date range.
fetch_student_snapshots_range <- function(student_id, date_from, date_to) {
  with_connection(function(con) {
    dbGetQuery(con, sqlInterpolate(con,
      "SELECT ses.id, ses.student_id, ses.session_id,
              ses.emotion, ses.concentration, ses.confidence_score,
              ses.captured_at,
              ls.course_id, ls.actual_start
         FROM student_emotion_snapshots ses
         JOIN lecture_sessions ls ON ls.id = ses.session_id
        WHERE ses.student_id = ?sid
          AND ses.captured_at BETWEEN ?df AND ?dt
        ORDER BY ses.captured_at ASC",
      sid = student_id,
      df  = format(as.POSIXct(date_from), "%Y-%m-%d %H:%M:%S"),
      dt  = format(as.POSIXct(date_to),   "%Y-%m-%d %H:%M:%S")))
  })
}

#' Fetch snapshots for all students in a session.
fetch_all_student_snapshots_for_session <- function(session_id) {
  with_connection(function(con) {
    dbGetQuery(con, sqlInterpolate(con,
      "SELECT ses.id, ses.student_id, ses.session_id,
              ses.emotion, ses.concentration, ses.confidence_score,
              ses.captured_at,
              CONCAT(u.first_name,' ',u.last_name) AS student_name
         FROM student_emotion_snapshots ses
         JOIN users u ON u.id = ses.student_id
        WHERE ses.session_id = ?sess
        ORDER BY ses.captured_at ASC",
      sess = session_id))
  })
}
