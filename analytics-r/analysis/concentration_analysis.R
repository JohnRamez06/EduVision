# ============================================================
# concentration_analysis.R — Distribution and low-concentration periods
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

#' Distribution analysis of concentration scores for a session.
#' @param session_id Character.
#' @return Named list.
concentration_analysis <- function(session_id) {
  snaps <- fetch_all_student_snapshots_for_session(session_id)
  if (nrow(snaps) == 0) {
    return(list(session_id = session_id, distribution = table(),
                low_periods = data.frame(), stats = list()))
  }

  snaps <- snaps %>%
    dplyr::mutate(
      conc_score  = concentration_to_score(concentration),
      captured_at = as.POSIXct(captured_at)
    )

  # Distribution table
  dist <- table(snaps$concentration)

  # Identify low-concentration periods (5-min buckets where avg < 35)
  low_periods <- snaps %>%
    dplyr::mutate(bucket = floor(
      as.numeric(difftime(captured_at,
                          min(captured_at, na.rm = TRUE),
                          units = "mins")) / 5) * 5) %>%
    dplyr::group_by(bucket) %>%
    dplyr::summarise(avg_conc = mean(conc_score, na.rm = TRUE),
                     .groups = "drop") %>%
    dplyr::filter(avg_conc < 35)

  list(
    session_id  = session_id,
    distribution = dist,
    low_periods  = low_periods,
    stats        = summary_stats(snaps$conc_score)
  )
}
