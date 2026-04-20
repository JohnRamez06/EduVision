source("fetch_data.R")
analyze_concentration <- function(session_id) {
  df <- fetch_session_snapshots(session_id)
  list(mean_conc=mean(df$avg_concentration,na.rm=T), low_pct=mean(df$avg_concentration<0.3,na.rm=T))
}
