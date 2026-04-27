# ============================================================
# export_analysis.R — Export analysis results to CSV
# ============================================================

AN_DIR <- dirname(sys.frame(1)$ofile %||% ".")
ROOT   <- dirname(AN_DIR)
source(file.path(ROOT, "config.R"),   local = TRUE)
source(file.path(ROOT, "scripts", "utils.R"), local = TRUE)

EXPORTS_DIR <- file.path(ROOT, "data", "exports")

#' Export a data.frame to a timestamped CSV in data/exports/.
#' @param df       data.frame to export.
#' @param filename Base filename (without extension).
#' @return Invisibly returns the written file path.
export_csv <- function(df, filename) {
  ensure_dir(EXPORTS_DIR)
  ts   <- format(Sys.time(), "%Y%m%d_%H%M%S")
  path <- file.path(EXPORTS_DIR, sprintf("%s_%s.csv", filename, ts))
  write.csv(df, path, row.names = FALSE)
  log_message(sprintf("Exported: %s (%d rows)", path, nrow(df)))
  invisible(path)
}

#' Export weekly student stats for all students in a week.
export_weekly_student_stats <- function(week_id) {
  data <- with_connection(function(con) {
    dbGetQuery(con, sqlInterpolate(con,
      "SELECT ses.student_id,
              CONCAT(u.first_name,' ',u.last_name) AS student_name,
              COUNT(*)                              AS n_snapshots,
              AVG(CASE WHEN ses.concentration='high'   THEN 85
                       WHEN ses.concentration='medium' THEN 55
                       WHEN ses.concentration='low'    THEN 25
                       ELSE 10 END)                AS avg_conc_score
         FROM student_emotion_snapshots ses
         JOIN users u ON u.id = ses.student_id
         JOIN lecture_sessions ls ON ls.id = ses.session_id
         JOIN weekly_periods wp ON wp.id = ?wid
        WHERE ses.captured_at BETWEEN wp.start_date AND
              DATE_ADD(wp.end_date, INTERVAL 1 DAY)
        GROUP BY ses.student_id, u.first_name, u.last_name",
      wid = week_id))
  })
  export_csv(data, paste0("weekly_student_stats_w", week_id))
}

#' Export session-level engagement summary.
export_session_engagement <- function(session_ids) {
  placeholders <- paste(sprintf("'%s'", session_ids), collapse = ",")
  data <- with_connection(function(con) {
    dbGetQuery(con, sprintf(
      "SELECT es.session_id,
              AVG(es.engagement_score)  AS avg_engagement,
              AVG(es.avg_concentration) AS avg_concentration,
              MAX(es.student_count)     AS max_students
         FROM emotion_snapshots es
        WHERE es.session_id IN (%s)
        GROUP BY es.session_id", placeholders))
  })
  export_csv(data, "session_engagement_export")
}
