# =============================================================================
# analysis/drowsiness_pattern_analysis.R
# =============================================================================
BASE_DIR <- normalizePath(file.path(dirname(sys.frame(1)$ofile), ".."))
if (!exists("get_connection")) source(file.path(BASE_DIR, "config.R"))
library(dplyr)

drowsiness_pattern_analysis <- function(student_id, n_sessions = 10) {
  df <- query_df(sprintf(
    "SELECT ses.concentration, ses.captured_at, ses.session_id,
            HOUR(ses.captured_at) AS hour
     FROM student_emotion_snapshots ses
     WHERE ses.student_id = '%s' AND ses.concentration = 'distracted'
     ORDER BY ses.captured_at DESC LIMIT %d",
    student_id, n_sessions * 100
  ))
  if (nrow(df) == 0) return(list(student_id = student_id, episodes = 0))

  hour_dist <- table(df$hour)
  peak_hour <- as.integer(names(which.max(hour_dist)))

  df$captured_at <- as.POSIXct(df$captured_at)
  df <- df %>% arrange(captured_at)
  gaps <- as.numeric(diff(df$captured_at), units = "secs")
  episode_breaks <- c(TRUE, gaps > 30)
  n_episodes <- sum(episode_breaks)

  list(
    student_id   = student_id,
    episodes     = n_episodes,
    peak_hour    = peak_hour,
    hour_distribution = as.list(hour_dist),
    total_distracted_snapshots = nrow(df)
  )
}

# =============================================================================
# analysis/compare_lecturers.R
# =============================================================================
compare_lecturers <- function(lecturer_ids) {
  if (length(lecturer_ids) < 2) stop("Need at least 2 lecturer IDs")
  ids_sql <- paste0("'", lecturer_ids, "'", collapse = ",")
  df <- query_df(sprintf(
    "SELECT ls.lecturer_id, CONCAT(u.first_name,' ',u.last_name) AS name,
            AVG(es.engagement_score) AS avg_engagement,
            AVG(es.avg_concentration) AS avg_concentration,
            COUNT(DISTINCT ls.id) AS n_sessions
     FROM lecture_sessions ls
     JOIN emotion_snapshots es ON es.session_id = ls.id
     JOIN users u              ON u.id = ls.lecturer_id
     WHERE ls.lecturer_id IN (%s)
     GROUP BY ls.lecturer_id, u.first_name, u.last_name
     ORDER BY avg_engagement DESC", ids_sql
  ))
  df
}

# =============================================================================
# analysis/compare_courses.R
# =============================================================================
compare_courses <- function(course_ids) {
  if (length(course_ids) < 2) stop("Need at least 2 course IDs")
  ids_sql <- paste0("'", course_ids, "'", collapse = ",")
  df <- query_df(sprintf(
    "SELECT ls.course_id, c.code, c.title,
            AVG(es.engagement_score) AS avg_engagement,
            AVG(es.avg_concentration) AS avg_concentration,
            COUNT(DISTINCT ls.id) AS n_sessions
     FROM lecture_sessions ls
     JOIN emotion_snapshots es ON es.session_id = ls.id
     JOIN courses c            ON c.id = ls.course_id
     WHERE ls.course_id IN (%s)
     GROUP BY ls.course_id, c.code, c.title
     ORDER BY avg_engagement DESC", ids_sql
  ))
  df
}

# =============================================================================
# analysis/weekly_trend_analysis.R
# =============================================================================
weekly_trend_analysis <- function(student_id, num_weeks = 8) {
  df <- query_df(sprintf(
    "SELECT wp.week_number, wp.year, wp.start_date,
            AVG(ses.confidence_score) AS avg_confidence,
            COUNT(*) AS n_snapshots
     FROM student_emotion_snapshots ses
     JOIN lecture_sessions ls ON ls.id = ses.session_id
     JOIN weekly_periods wp   ON ses.captured_at BETWEEN wp.start_date AND wp.end_date
     WHERE ses.student_id = '%s'
     GROUP BY wp.id, wp.week_number, wp.year, wp.start_date
     ORDER BY wp.start_date DESC LIMIT %d", student_id, num_weeks
  ))
  if (nrow(df) < 2) return(list(student_id = student_id, error = "Insufficient weekly data"))
  df <- df[order(df$start_date), ]
  t   <- lm(avg_confidence ~ I(seq_len(nrow(df))), data = df)
  list(
    student_id    = student_id,
    num_weeks     = nrow(df),
    weekly_data   = df,
    slope         = round(coef(t)[2], 5),
    direction     = if (coef(t)[2] > 0.001) "improving" else if (coef(t)[2] < -0.001) "declining" else "stable"
  )
}

# =============================================================================
# analysis/student_individual_analysis.R
# =============================================================================
student_individual_analysis <- function(student_id) {
  df <- query_df(sprintf(
    "SELECT ses.emotion, ses.concentration, ses.confidence_score, ses.captured_at,
            HOUR(ses.captured_at) AS hour, DAYNAME(ses.captured_at) AS day_name
     FROM student_emotion_snapshots ses
     WHERE ses.student_id = '%s'
     ORDER BY ses.captured_at ASC", student_id
  ))
  if (nrow(df) == 0) return(list(student_id = student_id, no_data = TRUE))

  best_hour <- as.integer(names(which.max(tapply(df$confidence_score, df$hour, mean, na.rm = TRUE))))
  best_day  <- names(which.max(tapply(df$confidence_score, df$day_name, mean, na.rm = TRUE)))

  conc_map  <- c(high = 85, medium = 60, low = 35, distracted = 10)
  conc_by_hour <- tapply(conc_map[df$concentration], df$hour, mean, na.rm = TRUE)
  focus_drops_after_min <- if (any(!is.na(conc_by_hour))) {
    sorted <- sort(conc_by_hour, decreasing = FALSE)
    as.integer(names(sorted)[1])
  } else NA

  list(
    student_id       = student_id,
    best_hour        = best_hour,
    best_day         = best_day,
    focus_drop_hour  = focus_drops_after_min,
    total_sessions   = length(unique(df$session_id %||% character(0))),
    dominant_emotion = names(which.max(table(df$emotion))),
    avg_confidence   = round(mean(df$confidence_score, na.rm = TRUE), 3)
  )
}

# =============================================================================
# analysis/export_analysis.R
# =============================================================================
export_analysis <- function(data, filename, subdir = "exports") {
  BASE_DIR   <- Sys.getenv("R_BASE_DIR", normalizePath(file.path(dirname(sys.frame(1)$ofile), "..")))
  export_dir <- file.path(BASE_DIR, "data", subdir)
  if (!dir.exists(export_dir)) dir.create(export_dir, recursive = TRUE)
  full_path <- file.path(export_dir, filename)
  if (is.data.frame(data)) {
    write.csv(data, full_path, row.names = FALSE)
  } else {
    write(jsonlite::toJSON(data, auto_unbox = TRUE, pretty = TRUE), full_path)
  }
  log_message(paste("Exported to:", full_path))
  full_path
}