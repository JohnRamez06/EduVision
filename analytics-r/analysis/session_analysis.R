# =============================================================================
# analysis/session_analysis.R
# =============================================================================
BASE_DIR <- normalizePath(file.path(dirname(sys.frame(1)$ofile), ".."))
source(file.path(BASE_DIR, "config.R"))
source(file.path(BASE_DIR, "scripts", "fetch_data.R"))
source(file.path(BASE_DIR, "scripts", "fetch_class_snapshots.R"))
source(file.path(BASE_DIR, "scripts", "fetch_attendance.R"))
source(file.path(BASE_DIR, "scripts", "fetch_alerts.R"))
source(file.path(BASE_DIR, "scripts", "calculate_statistics.R"))
library(dplyr); library(ggplot2)

session_analysis <- function(session_id) {
  session   <- fetch_session(session_id)
  snaps     <- fetch_class_snapshots(session_id)
  attend    <- fetch_attendance(session_id)
  alerts_df <- fetch_alerts(session_id)

  if (nrow(snaps) == 0) return(list(session_id = session_id, no_data = TRUE))

  snaps$captured_at <- as.POSIXct(snaps$captured_at)
  snaps$ma_engagement   <- as.numeric(filter(snaps$engagement_score,    rep(1/5,5), sides=2))
  snaps$ma_concentration <- as.numeric(filter(snaps$avg_concentration, rep(1/5,5), sides=2))

  # Decay curve: engagement over time relative to session start
  if (nrow(session) > 0 && !is.na(session$actual_start[1])) {
    session_start <- as.POSIXct(session$actual_start[1])
    snaps$minutes_elapsed <- as.numeric(difftime(snaps$captured_at, session_start, units = "mins"))
  }

  list(
    session_id        = session_id,
    session_info      = session,
    attendance        = fetch_attendance_rate(session_id),
    avg_engagement    = round(mean(snaps$engagement_score, na.rm = TRUE), 3),
    avg_concentration = round(mean(snaps$avg_concentration, na.rm = TRUE), 3),
    peak_engagement   = round(max(snaps$engagement_score,  na.rm = TRUE), 3),
    min_engagement    = round(min(snaps$engagement_score,  na.rm = TRUE), 3),
    n_alerts          = nrow(alerts_df),
    critical_alerts   = sum(alerts_df$severity == "critical", na.rm = TRUE),
    emotion_dist      = as.list(prop.table(table(snaps$dominant_emotion))),
    snapshots_df      = snaps,
    alerts_df         = alerts_df
  )
}