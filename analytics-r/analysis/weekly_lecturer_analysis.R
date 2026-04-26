# =============================================================================
# analysis/weekly_lecturer_analysis.R
# =============================================================================
BASE_DIR <- normalizePath(file.path(dirname(sys.frame(1)$ofile), ".."))
source(file.path(BASE_DIR, "config.R"))
source(file.path(BASE_DIR, "scripts", "fetch_data.R"))
source(file.path(BASE_DIR, "scripts", "fetch_class_snapshots.R"))
source(file.path(BASE_DIR, "scripts", "fetch_alerts.R"))
source(file.path(BASE_DIR, "scripts", "calculate_statistics.R"))
library(dplyr); library(lubridate)

weekly_lecturer_analysis <- function(lecturer_id, week_id) {
  week <- fetch_week_dates(week_id)

  sessions <- query_df(sprintf(
    "SELECT ls.id, ls.course_id, c.code, c.title, ls.actual_start, ls.actual_end, ls.status
     FROM lecture_sessions ls
     JOIN courses c ON c.id = ls.course_id
     WHERE ls.lecturer_id = '%s'
       AND ls.actual_start BETWEEN '%s' AND '%s'
     ORDER BY ls.actual_start ASC",
    lecturer_id,
    format(week$start_date, "%Y-%m-%d %H:%M:%S"),
    format(week$end_date,   "%Y-%m-%d %H:%M:%S")
  ))

  if (nrow(sessions) == 0) {
    return(list(lecturer_id = lecturer_id, week_id = week_id, no_data = TRUE, n_sessions = 0))
  }

  session_stats <- lapply(sessions$id, function(sid) {
    snaps <- fetch_class_snapshots(sid)
    alerts_df <- fetch_alerts(sid)
    if (nrow(snaps) == 0) return(NULL)
    list(
      session_id       = sid,
      avg_engagement   = mean(snaps$engagement_score, na.rm = TRUE),
      avg_concentration = mean(snaps$avg_concentration, na.rm = TRUE),
      n_alerts         = nrow(alerts_df),
      critical_alerts  = sum(alerts_df$severity == "critical", na.rm = TRUE)
    )
  })
  session_stats <- Filter(Negate(is.null), session_stats)

  all_eng  <- sapply(session_stats, `[[`, "avg_engagement")
  all_conc <- sapply(session_stats, `[[`, "avg_concentration")

  # At-risk students this week
  at_risk_df <- query_df(sprintf(
    "SELECT DISTINCT ses.student_id, CONCAT(u.first_name,' ',u.last_name) AS name
     FROM student_emotion_snapshots ses
     JOIN lecture_sessions ls ON ls.id = ses.session_id
     JOIN users u              ON u.id = ses.student_id
     WHERE ls.lecturer_id = '%s'
       AND ses.captured_at BETWEEN '%s' AND '%s'
       AND ses.concentration = 'distracted'
     GROUP BY ses.student_id HAVING COUNT(*) > 10",
    lecturer_id,
    format(week$start_date, "%Y-%m-%d %H:%M:%S"),
    format(week$end_date,   "%Y-%m-%d %H:%M:%S")
  ))

  list(
    lecturer_id      = lecturer_id,
    week_id          = week_id,
    week_number      = week$week_number,
    n_sessions       = nrow(sessions),
    avg_engagement   = round(mean(all_eng,  na.rm = TRUE), 3),
    avg_concentration = round(mean(all_conc, na.rm = TRUE), 3),
    best_session_id  = session_stats[[which.max(all_eng)]]$session_id,
    worst_session_id = session_stats[[which.min(all_eng)]]$session_id,
    total_alerts     = sum(sapply(session_stats, `[[`, "n_alerts")),
    at_risk_students = at_risk_df,
    sessions_summary = sessions,
    session_stats    = session_stats
  )
}