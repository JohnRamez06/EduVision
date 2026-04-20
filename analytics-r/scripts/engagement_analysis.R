source("fetch_data.R")
library(forecast)
analyze_engagement <- function(session_id) {
  df <- fetch_session_snapshots(session_id)
  ts_data <- ts(df$engagement_score)
  list(mean=mean(ts_data,na.rm=T), sd=sd(ts_data,na.rm=T), model=auto.arima(ts_data))
}
