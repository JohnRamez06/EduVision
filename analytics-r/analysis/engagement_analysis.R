# ============================================================
# engagement_analysis.R — Time-series engagement with smoothing
# ============================================================

AN_DIR <- dirname(sys.frame(1)$ofile %||% ".")
ROOT   <- dirname(AN_DIR)
source(file.path(ROOT, "config.R"),                          local = TRUE)
source(file.path(ROOT, "scripts", "utils.R"),                local = TRUE)
source(file.path(ROOT, "scripts", "fetch_class_snapshots.R"), local = TRUE)
source(file.path(ROOT, "scripts", "calculate_statistics.R"),  local = TRUE)

#' Compute smoothed engagement time-series and find peaks/drops.
#' @param session_id Character.
#' @param k          Window size for rolling mean.
#' @return Named list.
engagement_analysis <- function(session_id, k = 5L) {
  snaps <- fetch_class_snapshots(session_id)
  if (nrow(snaps) == 0) {
    return(list(session_id = session_id, series = data.frame(),
                peaks = integer(0), drops = integer(0)))
  }

  series <- snaps %>%
    dplyr::mutate(
      captured_at  = as.POSIXct(captured_at),
      raw_eng      = as.numeric(engagement_score),
      smooth_eng   = rolling_mean(as.numeric(engagement_score), k = k)
    ) %>%
    dplyr::arrange(captured_at)

  # Detect peaks: local maxima in smoothed series
  sm      <- series$smooth_eng
  n       <- length(sm)
  peaks   <- which(
    !is.na(sm) &
    c(FALSE, sm[-n] < sm[-1]) &
    c(sm[-1] < sm[-n], FALSE))
  drops   <- which(
    !is.na(sm) &
    c(FALSE, sm[-n] > sm[-1]) &
    c(sm[-1] > sm[-n], FALSE) &
    sm < 0.4)

  list(
    session_id = session_id,
    series     = series,
    peaks      = peaks,
    drops      = drops,
    stats      = summary_stats(series$raw_eng)
  )
}
