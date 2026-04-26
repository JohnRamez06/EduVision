# =============================================================================
# analysis/engagement_analysis.R
# =============================================================================
BASE_DIR <- normalizePath(file.path(dirname(sys.frame(1)$ofile), ".."))
source(file.path(BASE_DIR, "config.R"))
source(file.path(BASE_DIR, "scripts", "calculate_statistics.R"))
library(forecast); library(ggplot2)

engagement_analysis <- function(snapshots_df) {
  x <- as.numeric(snapshots_df$engagement_score)
  x <- x[!is.na(x)]
  if (length(x) < 5) return(list(error = "Insufficient data"))

  ma5  <- as.numeric(stats::filter(x, rep(1/5,  5),  sides = 2))
  ma10 <- as.numeric(stats::filter(x, rep(1/10, 10), sides = 2))

  peaks <- which(diff(sign(diff(x))) == -2) + 1
  drops <- which(diff(sign(diff(x))) == 2)  + 1

  ts_model <- tryCatch(auto.arima(ts(x)), error = function(e) NULL)

  list(
    mean_engagement = round(mean(x), 3),
    sd_engagement   = round(sd(x),   3),
    trend           = trend_direction(x),
    moving_avg_5    = ma5,
    moving_avg_10   = ma10,
    peak_indices    = peaks,
    drop_indices    = drops,
    arima_model     = ts_model
  )
}