# ============================================================
# weekly_trend_analysis.R
# ============================================================

AN_DIR <- dirname(sys.frame(1)$ofile %||% ".")
ROOT   <- dirname(AN_DIR)
source(file.path(ROOT, "config.R"),   local = TRUE)
source(file.path(ROOT, "scripts", "utils.R"), local = TRUE)
source(file.path(AN_DIR, "weekly_student_analysis.R"), local = TRUE)

#' Compute multi-week trend for a student.
#' @param student_id Character.
#' @param num_weeks  Integer number of recent weeks to analyse.
#' @return Named list: weekly_stats (data.frame), trend_slope, trend_direction.
weekly_trend_analysis <- function(student_id, num_weeks = 8L) {
  # Fetch last N weekly periods
  weeks <- with_connection(function(con) {
    dbGetQuery(con, sprintf(
      "SELECT * FROM weekly_periods ORDER BY year DESC, week_number DESC LIMIT %d",
      as.integer(num_weeks)))
  })
  if (nrow(weeks) == 0) stop("No weekly_periods found.")

  # Analyse each week
  stats_list <- lapply(seq_len(nrow(weeks)), function(i) {
    wid  <- weeks$id[i]
    stat <- tryCatch(weekly_student_analysis(student_id, wid),
                     error = function(e) NULL)
    if (is.null(stat)) return(NULL)
    data.frame(
      week_id              = wid,
      week_number          = weeks$week_number[i],
      year                 = weeks$year[i],
      start_date           = as.character(weeks$start_date[i]),
      avg_concentration    = stat$avg_concentration,
      attentive_percentage = stat$attentive_percentage,
      drowsy_episodes      = stat$drowsy_episodes,
      sessions_attended    = stat$sessions_attended,
      stringsAsFactors = FALSE
    )
  })

  weekly_df <- do.call(rbind, Filter(Negate(is.null), stats_list))
  if (is.null(weekly_df) || nrow(weekly_df) == 0) {
    return(list(student_id = student_id, weekly_stats = data.frame(),
                trend_slope = NA_real_, trend_direction = "unknown"))
  }

  # Sort chronologically
  weekly_df <- weekly_df[order(weekly_df$year, weekly_df$week_number), ]

  # Linear trend of concentration
  x <- seq_len(nrow(weekly_df))
  y <- weekly_df$avg_concentration
  valid <- !is.na(y)
  slope <- NA_real_
  if (sum(valid) >= 2) {
    fit   <- lm(y[valid] ~ x[valid])
    slope <- as.numeric(coef(fit)[2])
  }

  direction <- if (is.na(slope))  "unknown" else
               if (slope > 1)     "improving" else
               if (slope < -1)    "declining" else "stable"

  list(
    student_id      = student_id,
    weekly_stats    = weekly_df,
    trend_slope     = slope,
    trend_direction = direction
  )
}
