# ============================================================
# drowsiness_pattern_analysis.R
# ============================================================

ROOT <- {
  env <- Sys.getenv("ANALYTICS_HOME", "")
  if (nchar(env) > 0) env else {
    d <- tryCatch(normalizePath(dirname(sys.frame(1)$ofile)), error = function(e) getwd())
    dirname(d)
  }
}
if (!exists("get_connection")) source(file.path(ROOT, "config.R"), local = TRUE)
if (!exists("%||%"))           source(file.path(ROOT, "scripts", "utils.R"), local = TRUE)
source(file.path(ROOT, "scripts", "fetch_student_snapshots.R"), local = TRUE)
source(file.path(ROOT, "scripts", "calculate_statistics.R"),   local = TRUE)

#' Analyse drowsiness patterns for a student across all sessions.
#' @param student_id Character.
#' @return Named list.
drowsiness_pattern_analysis <- function(student_id) {
  snaps <- with_connection(function(con) {
    dbGetQuery(con, sqlInterpolate(con,
      "SELECT ses.session_id, ses.emotion, ses.concentration,
              ses.captured_at, ls.actual_start
         FROM student_emotion_snapshots ses
         JOIN lecture_sessions ls ON ls.id = ses.session_id
        WHERE ses.student_id = ?sid
        ORDER BY ses.captured_at ASC",
      sid = student_id))
  })

  if (nrow(snaps) == 0) {
    return(list(student_id = student_id, total_episodes = 0L,
                avg_duration_mins = NA_real_, time_pattern = data.frame()))
  }

  snaps <- snaps %>%
    dplyr::mutate(
      captured_at  = as.POSIXct(captured_at),
      actual_start = as.POSIXct(actual_start),
      is_drowsy    = concentration %in% c("distracted","low") &
                     emotion %in% c("neutral","sad","fearful"),
      minutes_in   = as.numeric(difftime(captured_at, actual_start, units = "mins")),
      hour_of_day  = as.integer(format(captured_at, "%H"))
    )

  # Count episodes (runs of is_drowsy == TRUE)
  rle_res  <- rle(snaps$is_drowsy)
  n_ep     <- sum(rle_res$values == TRUE)
  ep_lens  <- rle_res$lengths[rle_res$values == TRUE]
  avg_dur  <- if (length(ep_lens) > 0) mean(ep_lens) * 5 / 60 else NA_real_ # minutes

  # Time patterns
  drowsy_snaps <- snaps[snaps$is_drowsy, ]
  time_pattern <- if (nrow(drowsy_snaps) > 0) {
    drowsy_snaps %>%
      dplyr::mutate(time_bucket = floor(minutes_in / 15) * 15) %>%
      dplyr::group_by(time_bucket) %>%
      dplyr::summarise(n = dplyr::n(), .groups = "drop")
  } else data.frame()

  hour_pattern <- if (nrow(drowsy_snaps) > 0) {
    drowsy_snaps %>%
      dplyr::group_by(hour_of_day) %>%
      dplyr::summarise(n = dplyr::n(), .groups = "drop")
  } else data.frame()

  list(
    student_id        = student_id,
    total_episodes    = n_ep,
    avg_duration_mins = avg_dur,
    time_pattern      = time_pattern,
    hour_pattern      = hour_pattern
  )
}
