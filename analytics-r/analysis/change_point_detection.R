# =============================================================================
# analysis/change_point_detection.R
# =============================================================================
BASE_DIR <- normalizePath(file.path(dirname(sys.frame(1)$ofile), ".."))
if (!exists("get_connection")) source(file.path(BASE_DIR, "config.R"))
library(changepoint)

detect_engagement_changepoints <- function(snapshots_df) {
  x <- as.numeric(snapshots_df$engagement_score)
  x <- x[!is.na(x)]
  if (length(x) < 10) return(list(changepoints = integer(0), n_changepoints = 0))

  cpt_obj  <- cpt.mean(x, method = "PELT", penalty = "BIC")
  cpt_locs <- cpts(cpt_obj)

  # Map indices back to timestamps if available
  timestamps <- NULL
  if ("captured_at" %in% names(snapshots_df)) {
    ts_vec <- as.POSIXct(snapshots_df$captured_at)
    ts_vec <- ts_vec[!is.na(snapshots_df$engagement_score)]
    timestamps <- ts_vec[cpt_locs]
  }

  # Mean before/after each changepoint
  segments <- lapply(seq_along(cpt_locs), function(i) {
    start <- if (i == 1) 1 else cpt_locs[i - 1] + 1
    end   <- cpt_locs[i]
    list(
      start_idx  = start,
      end_idx    = end,
      mean_before = round(mean(x[start:end]), 3),
      timestamp   = if (!is.null(timestamps)) timestamps[i] else NA,
      drop        = if (i > 1) round(mean(x[start:end]) - mean(x[max(1, start-10):(start-1)]), 3) else NA
    )
  })

  list(
    changepoints   = cpt_locs,
    n_changepoints = length(cpt_locs),
    timestamps     = timestamps,
    segments       = segments,
    cpt_object     = cpt_obj
  )
}