# ============================================================
# session_analysis.R — Detailed single-session analysis
# ============================================================

AN_DIR <- dirname(sys.frame(1)$ofile %||% ".")
ROOT   <- dirname(AN_DIR)
source(file.path(ROOT, "config.R"),                          local = TRUE)
source(file.path(ROOT, "scripts", "utils.R"),                local = TRUE)
source(file.path(ROOT, "scripts", "fetch_class_snapshots.R"), local = TRUE)
source(file.path(ROOT, "scripts", "fetch_student_snapshots.R"), local = TRUE)
source(file.path(ROOT, "scripts", "fetch_attendance.R"),     local = TRUE)
source(file.path(ROOT, "scripts", "fetch_alerts.R"),         local = TRUE)
source(file.path(ROOT, "scripts", "calculate_statistics.R"), local = TRUE)

#' Full single-session analysis.
#' @param session_id Character.
#' @return Named list with timeline, heatmap data, engagement decay, and stats.
session_analysis <- function(session_id) {
  # Session meta
  session <- with_connection(function(con) {
    dbGetQuery(con, sqlInterpolate(con,
      "SELECT ls.*, c.code AS course_code, c.title AS course_title,
              CONCAT(u.first_name,' ',u.last_name) AS lecturer_name
         FROM lecture_sessions ls
         JOIN courses c ON c.id = ls.course_id
         JOIN users   u ON u.id = ls.lecturer_id
        WHERE ls.id = ?sid", sid = session_id))
  })

  class_snaps    <- fetch_class_snapshots(session_id)
  student_snaps  <- fetch_all_student_snapshots_for_session(session_id)
  attendance     <- fetch_attendance_for_session(session_id)
  alerts         <- fetch_alerts_for_session(session_id)

  # Engagement timeline (class level)
  eng_timeline <- if (nrow(class_snaps) > 0) {
    class_snaps %>%
      dplyr::mutate(
        captured_at    = as.POSIXct(captured_at),
        engagement_val = as.numeric(engagement_score),
        smooth_eng     = rolling_mean(as.numeric(engagement_score), k = 5)
      ) %>%
      dplyr::select(captured_at, engagement_val, smooth_eng, dominant_emotion,
                    avg_concentration, student_count)
  } else data.frame()

  # Engagement decay: linear fit over time
  decay_slope <- NA_real_
  if (nrow(eng_timeline) >= 3) {
    x     <- seq_len(nrow(eng_timeline))
    y     <- eng_timeline$engagement_val
    valid <- !is.na(y)
    if (sum(valid) >= 3) {
      fit         <- lm(y[valid] ~ x[valid])
      decay_slope <- as.numeric(coef(fit)[2])
    }
  }

  # Per-student concentration heatmap data
  conc_heatmap <- if (nrow(student_snaps) > 0) {
    student_snaps %>%
      dplyr::mutate(
        conc_score  = concentration_to_score(concentration),
        captured_at = as.POSIXct(captured_at)
      ) %>%
      dplyr::group_by(student_id, student_name) %>%
      dplyr::summarise(avg_conc = mean(conc_score, na.rm = TRUE),
                       .groups = "drop") %>%
      dplyr::arrange(avg_conc)
  } else data.frame()

  # Emotion distribution
  emotion_dist <- if (nrow(student_snaps) > 0) {
    table(student_snaps$emotion) %>% as.data.frame() %>%
      setNames(c("emotion", "count")) %>%
      dplyr::mutate(pct = count / sum(count))
  } else data.frame()

  list(
    session_id    = session_id,
    session_meta  = session,
    n_present     = nrow(attendance[attendance$status %in% c("present","late"), ]),
    engagement_timeline = eng_timeline,
    decay_slope   = decay_slope,
    conc_heatmap  = conc_heatmap,
    emotion_dist  = emotion_dist,
    alerts        = alerts,
    stats = list(
      avg_engagement   = mean(as.numeric(class_snaps$engagement_score),  na.rm = TRUE),
      avg_concentration = mean(as.numeric(class_snaps$avg_concentration), na.rm = TRUE),
      dominant_emotion = dominant_emotion(student_snaps$emotion),
      n_alerts         = nrow(alerts)
    )
  )
}
