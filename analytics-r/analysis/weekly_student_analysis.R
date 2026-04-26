# =============================================================================
# analysis/weekly_student_analysis.R
# =============================================================================
BASE_DIR <- normalizePath(file.path(dirname(sys.frame(1)$ofile), ".."))
source(file.path(BASE_DIR, "config.R"))
source(file.path(BASE_DIR, "scripts", "fetch_data.R"))
source(file.path(BASE_DIR, "scripts", "fetch_student_snapshots.R"))
source(file.path(BASE_DIR, "scripts", "fetch_attendance.R"))
source(file.path(BASE_DIR, "scripts", "calculate_statistics.R"))
library(dplyr); library(lubridate)

weekly_student_analysis <- function(student_id, week_id) {
  week     <- fetch_week_dates(week_id)
  snapshots <- fetch_student_weekly_snapshots(student_id, week$start_date, week$end_date)
  student   <- fetch_student_info(student_id)

  if (nrow(snapshots) == 0) {
    log_message(sprintf("No snapshots for student %s in week %s", student_id, week_id), "WARN")
    return(list(student_id = student_id, week_id = week_id, no_data = TRUE))
  }

  # Concentration: stored as enum — map to numeric
  conc_map <- c(high = 85, medium = 60, low = 35, distracted = 10)
  snapshots$conc_score <- conc_map[snapshots$concentration]

  # Drowsy episodes: sequences where concentration == "distracted" lasting > 3 consecutive snapshots
  drowsy_runs <- rle(snapshots$concentration == "distracted")
  drowsy_episodes <- sum(drowsy_runs$values & drowsy_runs$lengths >= 3)

  # Confusion duration (seconds) — confused snapshots * assumed 5s interval
  confused_count   <- sum(snapshots$emotion == "confused", na.rm = TRUE)
  confusion_duration_sec <- confused_count * 5

  # Dominant emotion
  emotion_counts  <- table(snapshots$emotion)
  dominant_emotion <- names(which.max(emotion_counts))

  # Attentive = high or medium concentration
  attentive_pct <- mean(snapshots$concentration %in% c("high", "medium"), na.rm = TRUE)

  # Sessions attended this week
  sessions_df <- query_df(sprintf(
    "SELECT DISTINCT ses.session_id FROM student_emotion_snapshots ses
     JOIN lecture_sessions ls ON ls.id = ses.session_id
     WHERE ses.student_id = '%s'
       AND ses.captured_at BETWEEN '%s' AND '%s'",
    student_id,
    format(week$start_date, "%Y-%m-%d %H:%M:%S"),
    format(week$end_date,   "%Y-%m-%d %H:%M:%S")
  ))

  trend <- trend_direction(snapshots$engagement_score)

  list(
    student_id          = student_id,
    student_name        = if (nrow(student) > 0) paste(student$first_name, student$last_name) else student_id,
    week_id             = week_id,
    week_number         = week$week_number,
    year                = week$year,
    n_snapshots         = nrow(snapshots),
    n_sessions          = nrow(sessions_df),
    avg_concentration   = round(mean(snapshots$conc_score, na.rm = TRUE), 1),
    avg_engagement      = round(mean(snapshots$engagement_score, na.rm = TRUE), 3),
    dominant_emotion    = dominant_emotion,
    attentive_pct       = round(attentive_pct * 100, 1),
    drowsy_episodes     = drowsy_episodes,
    confusion_duration_sec = confusion_duration_sec,
    engagement_trend    = trend$direction,
    engagement_slope    = trend$slope,
    emotion_breakdown   = as.list(prop.table(emotion_counts)),
    snapshots_df        = snapshots
  )
}