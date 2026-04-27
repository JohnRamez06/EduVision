# ============================================================
# student_individual_analysis.R — Per-student cross-session patterns
# ============================================================

AN_DIR <- dirname(sys.frame(1)$ofile %||% ".")
ROOT   <- dirname(AN_DIR)
source(file.path(ROOT, "config.R"),                           local = TRUE)
source(file.path(ROOT, "scripts", "utils.R"),                 local = TRUE)
source(file.path(ROOT, "scripts", "fetch_student_snapshots.R"), local = TRUE)
source(file.path(ROOT, "scripts", "calculate_statistics.R"),  local = TRUE)

#' Individual analysis for a student across all sessions.
#' @param student_id Character.
#' @return Named list with per-day-of-week stats, time-within-session decay,
#'         and overall engagement patterns.
student_individual_analysis <- function(student_id) {
  snaps <- with_connection(function(con) {
    dbGetQuery(con, sqlInterpolate(con,
      "SELECT ses.session_id, ses.emotion, ses.concentration,
              ses.captured_at, ls.actual_start, ls.course_id
         FROM student_emotion_snapshots ses
         JOIN lecture_sessions ls ON ls.id = ses.session_id
        WHERE ses.student_id = ?sid
        ORDER BY ses.captured_at ASC",
      sid = student_id))
  })

  if (nrow(snaps) == 0) {
    return(list(student_id = student_id, n_snapshots = 0L,
                patterns = list()))
  }

  snaps <- snaps %>%
    dplyr::mutate(
      captured_at    = as.POSIXct(captured_at),
      actual_start   = as.POSIXct(actual_start),
      conc_score     = concentration_to_score(concentration),
      day_of_week    = weekdays(actual_start),
      minutes_in     = as.numeric(difftime(captured_at, actual_start,
                                           units = "mins")),
      hour_of_day    = as.integer(format(captured_at, "%H"))
    )

  # Day-of-week engagement
  dow_stats <- snaps %>%
    dplyr::group_by(day_of_week) %>%
    dplyr::summarise(avg_conc = mean(conc_score, na.rm = TRUE),
                     n        = dplyr::n(), .groups = "drop") %>%
    dplyr::arrange(dplyr::desc(avg_conc))

  # Concentration vs time-within-session (5-min buckets)
  time_decay <- snaps %>%
    dplyr::filter(minutes_in >= 0, minutes_in <= 150) %>%
    dplyr::mutate(bucket = floor(minutes_in / 5) * 5) %>%
    dplyr::group_by(bucket) %>%
    dplyr::summarise(avg_conc = mean(conc_score, na.rm = TRUE),
                     .groups = "drop")

  # Focus-drop threshold: first bucket where avg_conc drops below 50
  focus_drop <- time_decay$bucket[which(time_decay$avg_conc < 50)[1]]

  list(
    student_id      = student_id,
    n_snapshots     = nrow(snaps),
    dow_stats       = dow_stats,
    time_decay      = time_decay,
    focus_drop_mins = focus_drop,
    best_day        = dow_stats$day_of_week[1],
    overall_avg_conc = mean(snaps$conc_score, na.rm = TRUE)
  )
}
