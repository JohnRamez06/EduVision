# ============================================================
# calculate_statistics.R — Summary statistics helpers
# ============================================================

library(dplyr)

#' Compute summary stats for a numeric vector.
summary_stats <- function(x) {
  x <- x[!is.na(x)]
  if (length(x) == 0) {
    return(list(n = 0L, mean = NA_real_, median = NA_real_,
                sd = NA_real_, min = NA_real_, max = NA_real_,
                q25 = NA_real_, q75 = NA_real_))
  }
  list(
    n      = length(x),
    mean   = mean(x),
    median = median(x),
    sd     = sd(x),
    min    = min(x),
    max    = max(x),
    q25    = quantile(x, 0.25),
    q75    = quantile(x, 0.75)
  )
}

#' Compute dominant emotion from a character vector.
dominant_emotion <- function(emotions) {
  if (length(emotions) == 0 || all(is.na(emotions))) return(NA_character_)
  tbl <- table(emotions[!is.na(emotions)])
  names(which.max(tbl))
}

#' Compute attentive percentage.
#' Attentive = concentration IN ('high', 'medium') OR emotion = 'engaged'
attentive_percentage <- function(snapshots_df) {
  if (nrow(snapshots_df) == 0) return(NA_real_)
  attentive <- with(snapshots_df,
    (concentration %in% c("high", "medium")) | (emotion == "engaged"))
  mean(attentive, na.rm = TRUE)
}

#' Count drowsy episodes.
#' A drowsy episode is a run of consecutive snapshots where
#' emotion == 'neutral' and concentration == 'distracted' or 'low'.
count_drowsy_episodes <- function(snapshots_df) {
  if (nrow(snapshots_df) == 0) return(0L)
  drowsy <- with(snapshots_df,
    concentration %in% c("distracted", "low") &
    emotion %in% c("neutral", "sad"))
  # count transitions FALSE→TRUE
  sum(diff(c(FALSE, drowsy)) == 1, na.rm = TRUE)
}

#' Compute confusion duration in minutes from snapshots.
confusion_duration_mins <- function(snapshots_df, interval_secs = 5) {
  if (nrow(snapshots_df) == 0) return(0)
  confused <- sum(snapshots_df$emotion == "confused", na.rm = TRUE)
  confused * interval_secs / 60
}

#' Compute engagement score time series (numeric 0-1 from engagement_score col).
compute_engagement_series <- function(snapshots_df) {
  snapshots_df %>%
    dplyr::mutate(
      captured_at    = as.POSIXct(captured_at),
      engagement_val = as.numeric(engagement_score)
    ) %>%
    dplyr::select(captured_at, engagement_val) %>%
    dplyr::filter(!is.na(engagement_val)) %>%
    dplyr::arrange(captured_at)
}
