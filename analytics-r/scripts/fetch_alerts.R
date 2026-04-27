# ============================================================
# fetch_alerts.R — Fetch alert records
# ============================================================

SCRIPTS_DIR <- dirname(sys.frame(1)$ofile %||% ".")
source(file.path(dirname(SCRIPTS_DIR), "config.R"), local = TRUE)

#' Fetch alerts for a session.
fetch_alerts_for_session <- function(session_id) {
  with_connection(function(con) {
    dbGetQuery(con, sqlInterpolate(con,
      "SELECT id, session_id, student_id, alert_type, severity,
              message, status, triggered_at, resolved_at
         FROM alerts
        WHERE session_id = ?sess
        ORDER BY triggered_at ASC",
      sess = session_id))
  })
}

#' Fetch alerts for a student in a date range.
fetch_alerts_for_student <- function(student_id, date_from = NULL, date_to = NULL) {
  with_connection(function(con) {
    sql <- "
      SELECT a.id, a.session_id, a.alert_type, a.severity,
             a.message, a.status, a.triggered_at
        FROM alerts a
        JOIN lecture_sessions ls ON ls.id = a.session_id
       WHERE a.student_id = ?"
    params <- list(student_id)
    if (!is.null(date_from)) {
      sql    <- paste0(sql, " AND a.triggered_at >= ?")
      params <- c(params, list(format(date_from)))
    }
    if (!is.null(date_to)) {
      sql    <- paste0(sql, " AND a.triggered_at <= ?")
      params <- c(params, list(format(date_to)))
    }
    sql <- paste0(sql, " ORDER BY a.triggered_at ASC")
    dbGetQuery(con, do.call(sqlInterpolate, c(list(con, sql), params)))
  })
}

#' Fetch open (unresolved) alerts for all sessions of a lecturer.
fetch_open_alerts_for_lecturer <- function(lecturer_id) {
  with_connection(function(con) {
    dbGetQuery(con, sqlInterpolate(con,
      "SELECT a.id, a.session_id, a.student_id, a.alert_type,
              a.severity, a.message, a.triggered_at,
              CONCAT(u.first_name,' ',u.last_name) AS student_name
         FROM alerts a
         JOIN lecture_sessions ls ON ls.id = a.session_id
         JOIN users u ON u.id = a.student_id
        WHERE ls.lecturer_id = ?lid AND a.status = 'open'
        ORDER BY a.triggered_at DESC",
      lid = lecturer_id))
  })
}
