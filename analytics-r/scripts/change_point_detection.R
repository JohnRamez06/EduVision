source("fetch_data.R")
library(changepoint)
detect_drops <- function(session_id) {
  df <- fetch_session_snapshots(session_id)
  cpt.mean(df$engagement_score, method="PELT")
}
