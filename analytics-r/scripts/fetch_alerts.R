# =============================================================================
# scripts/fetch_alerts.R
# =============================================================================
BASE_DIR <- normalizePath(file.path(dirname(sys.frame(1)$ofile), ".."))
if (!exists("fetch_query")) source(file.path(BASE_DIR, "scripts", "fetch_data.R"))

fetch_alerts <- function(session_id) {
  fetch_query(sprintf(
    "SELECT a.id, a.severity, a.status, a.title, a.message,
            a.threshold_value, a.actual_value, a.triggered_at,
            a.acknowledged_at, a.resolved_at,
            st.name AS strategy_name
     FROM alerts a
     LEFT JOIN strategies st ON st.id = a.strategy_id
     WHERE a.session_id = '%s'
     ORDER BY a.triggered_at ASC", session_id
  ))
}

fetch_alerts_weekly <- function(start_date, end_date, lecturer_id = NULL) {
  base_sql <- sprintf(
    "SELECT a.*, ls.course_id, c.title AS course_title
     FROM alerts a
     JOIN lecture_sessions ls ON ls.id = a.session_id
     JOIN courses c           ON c.id  = ls.course_id
     WHERE a.triggered_at BETWEEN '%s' AND '%s'",
    format(start_date, "%Y-%m-%d %H:%M:%S"),
    format(end_date,   "%Y-%m-%d %H:%M:%S")
  )
  if (!is.null(lecturer_id)) {
    base_sql <- paste0(base_sql, sprintf(" AND ls.lecturer_id = '%s'", lecturer_id))
  }
  fetch_query(paste0(base_sql, " ORDER BY a.triggered_at DESC"))
}