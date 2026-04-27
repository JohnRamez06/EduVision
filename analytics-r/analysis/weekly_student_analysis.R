# ============================================================
# weekly_student_analysis.R
# ============================================================

AN_DIR <- dirname(sys.frame(1)$ofile %||% ".")
ROOT   <- dirname(AN_DIR)
source(file.path(ROOT, "config.R"),                         local = TRUE)
source(file.path(ROOT, "scripts", "utils.R"),               local = TRUE)
source(file.path(ROOT, "scripts", "fetch_student_snapshots.R"), local = TRUE)
source(file.path(ROOT, "scripts", "fetch_attendance.R"),    local = TRUE)
source(file.path(ROOT, "scripts", "calculate_statistics.R"), local = TRUE)

#' Weekly analysis for a single student.
#' @param student_id Character.
#' @param week_id    Integer or character primary-key of weekly_periods row.
#' @return Named list with analysis stats.
weekly_student_analysis <- function(student_id, week_id) {
  # Fetch week dates
  wp <- with_connection(function(con) {
    dbGetQuery(con, sqlInterpolate(con,
      "SELECT * FROM weekly_periods WHERE id = ?wid", wid = week_id))
  })
  if (nrow(wp) == 0) stop("weekly_period not found: ", week_id)

  date_from <- as.POSIXct(paste(wp$start_date[1], "00:00:00"))
  date_to   <- as.POSIXct(paste(wp$end_date[1],   "23:59:59"))

  # Snapshots in range
  snaps <- fetch_student_snapshots_range(student_id, date_from, date_to)

  if (nrow(snaps) == 0) {
    return(list(student_id           = student_id,
                week_id              = week_id,
                n_snapshots          = 0L,
                avg_concentration    = NA_real_,
                dominant_emotion     = NA_character_,
                attentive_percentage = NA_real_,
                drowsy_episodes      = 0L,
                confusion_duration   = 0,
                sessions_attended    = 0L))
  }

  conc_scores <- concentration_to_score(snaps$concentration)

  list(
    student_id           = student_id,
    week_id              = week_id,
    n_snapshots          = nrow(snaps),
    avg_concentration    = mean(conc_scores, na.rm = TRUE),
    dominant_emotion     = dominant_emotion(snaps$emotion),
    attentive_percentage = attentive_percentage(snaps),
    drowsy_episodes      = count_drowsy_episodes(snaps),
    confusion_duration   = confusion_duration_mins(snaps),
    sessions_attended    = length(unique(snaps$session_id))
  )
}
