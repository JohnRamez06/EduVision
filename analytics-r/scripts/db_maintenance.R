# ============================================================
# db_maintenance.R — Scheduled DB maintenance tasks
# ============================================================

SCRIPTS_DIR <- dirname(sys.frame(1)$ofile %||% ".")
source(file.path(dirname(SCRIPTS_DIR), "config.R"), local = TRUE)
source(file.path(SCRIPTS_DIR, "utils.R"), local = TRUE)

LOG_FILE <- file.path(dirname(SCRIPTS_DIR), "logs", "error.log")

#' Anonymise old student snapshots (older than retention_days).
anonymise_old_snapshots <- function(retention_days = 365) {
  with_connection(function(con) {
    cutoff <- format(Sys.time() - retention_days * 86400, "%Y-%m-%d %H:%M:%S")
    rows <- dbExecute(con, sqlInterpolate(con,
      "UPDATE student_emotion_snapshots
          SET face_embedding = NULL, bounding_box = NULL, gaze_direction = NULL,
              is_anonymised = 1
        WHERE is_anonymised = 0 AND captured_at < ?cutoff",
      cutoff = cutoff))
    log_message(sprintf("Anonymised %d snapshots older than %d days.",
                        rows, retention_days), LOG_FILE)
  })
}

#' Delete failed report rows older than N days.
purge_failed_reports <- function(days = 7) {
  with_connection(function(con) {
    cutoff <- format(Sys.time() - days * 86400, "%Y-%m-%d %H:%M:%S")
    rows <- dbExecute(con, sqlInterpolate(con,
      "DELETE FROM reports WHERE status = 'failed' AND requested_at < ?cutoff",
      cutoff = cutoff))
    log_message(sprintf("Purged %d failed report rows.", rows), LOG_FILE)
  })
}

#' Vacuum / optimise key tables.
optimise_tables <- function() {
  with_connection(function(con) {
    tables <- c("emotion_snapshots", "student_emotion_snapshots",
                "session_attendance", "alerts", "reports")
    for (tbl in tables) {
      dbExecute(con, sprintf("OPTIMIZE TABLE `%s`", tbl))
    }
    log_message("Database tables optimised.", LOG_FILE)
  })
}

# ---- Run when called directly ----
if (!interactive()) {
  log_message("Running DB maintenance...")
  anonymise_old_snapshots()
  purge_failed_reports()
  optimise_tables()
  log_message("DB maintenance complete.")
}
