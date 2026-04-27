# ============================================================
# fetch_class_snapshots.R — Fetch class-level emotion snapshots
# ============================================================

SCRIPTS_DIR <- dirname(sys.frame(1)$ofile %||% ".")
source(file.path(dirname(SCRIPTS_DIR), "config.R"), local = TRUE)

#' Fetch all class-level emotion snapshots for a session.
#' Columns: id, session_id, seq_index, captured_at, student_count,
#'          avg_concentration, dominant_emotion, engagement_score.
fetch_class_snapshots <- function(session_id) {
  with_connection(function(con) {
    dbGetQuery(con, sqlInterpolate(con,
      "SELECT id, session_id, seq_index, captured_at,
              student_count, avg_concentration, dominant_emotion,
              engagement_score, processing_ms
         FROM emotion_snapshots
        WHERE session_id = ?sid
        ORDER BY seq_index ASC",
      sid = session_id))
  })
}

#' Fetch class snapshots for multiple sessions (e.g. a whole week).
fetch_class_snapshots_bulk <- function(session_ids) {
  if (length(session_ids) == 0) return(data.frame())
  with_connection(function(con) {
    placeholders <- paste(rep("?", length(session_ids)), collapse = ", ")
    sql <- sprintf(
      "SELECT es.id, es.session_id, es.seq_index, es.captured_at,
              es.student_count, es.avg_concentration, es.dominant_emotion,
              es.engagement_score
         FROM emotion_snapshots es
        WHERE es.session_id IN (%s)
        ORDER BY es.session_id, es.seq_index", placeholders)
    query <- DBI::sqlInterpolate(con, sql,
               .dots = setNames(as.list(session_ids),
                                rep("", length(session_ids))))
    dbGetQuery(con, query)
  })
}
