# ============================================================
# change_point_detection.R — Find engagement drop timestamps via cpt.mean()
# ============================================================

AN_DIR <- dirname(sys.frame(1)$ofile %||% ".")
ROOT   <- dirname(AN_DIR)
source(file.path(ROOT, "config.R"),                          local = TRUE)
source(file.path(ROOT, "scripts", "utils.R"),                local = TRUE)
source(file.path(ROOT, "scripts", "fetch_class_snapshots.R"), local = TRUE)

library(changepoint)

#' Detect change points in the engagement score of a session.
#' @param session_id Character.
#' @param method     Changepoint method: "PELT" or "BinSeg".
#' @return Named list: changepoints (timestamps), series (data.frame).
change_point_detection <- function(session_id, method = "PELT") {
  snaps <- fetch_class_snapshots(session_id)
  if (nrow(snaps) < 10) {
    message("Not enough snapshots for change-point detection.")
    return(list(session_id = session_id, changepoints = character(0),
                series = data.frame()))
  }

  series <- snaps %>%
    dplyr::mutate(
      captured_at  = as.POSIXct(captured_at),
      eng          = as.numeric(engagement_score)
    ) %>%
    dplyr::arrange(captured_at) %>%
    dplyr::filter(!is.na(eng))

  eng_vec <- series$eng

  cp_result <- tryCatch(
    cpt.mean(eng_vec, method = method, penalty = "BIC"),
    error = function(e) {
      message("changepoint error: ", e$message)
      NULL
    })

  cp_indices <- if (!is.null(cp_result)) cpts(cp_result) else integer(0)
  cp_times   <- if (length(cp_indices) > 0)
                  as.character(series$captured_at[cp_indices])
                else character(0)

  list(
    session_id    = session_id,
    changepoints  = cp_times,
    cp_indices    = cp_indices,
    series        = series
  )
}
