# ============================================================
# utils.R — Shared utility functions
# ============================================================

# Null-coalescing operator (used widely across analysis scripts)
`%||%` <- function(a, b) if (!is.null(a)) a else b

# Open a connection, run expr(con), then close it
with_connection <- function(expr) {
  con <- get_connection()
  on.exit(try(DBI::dbDisconnect(con), silent = TRUE), add = TRUE)
  expr(con)
}

#' Log a timestamped message to a log file and stdout.
log_message <- function(msg, log_file = NULL) {
  ts  <- format(Sys.time(), "%Y-%m-%d %H:%M:%S")
  txt <- sprintf("[%s] %s", ts, msg)
  message(txt)
  if (!is.null(log_file)) {
    cat(txt, "\n", file = log_file, append = TRUE)
  }
}

#' Ensure a directory exists; create it if it does not.
ensure_dir <- function(path) {
  if (!dir.exists(path)) dir.create(path, recursive = TRUE)
  invisible(path)
}

#' Return the analytics-r root directory.
analytics_home <- function() {
  env <- Sys.getenv("ANALYTICS_HOME", "")
  if (nchar(env) > 0) return(env)
  # Walk up from this file's location
  this <- tryCatch(normalizePath(dirname(sys.frame(1)$ofile)), error = function(e) ".")
  # Keep going up until we find config.R
  candidate <- this
  for (i in seq_len(6)) {
    if (file.exists(file.path(candidate, "config.R"))) return(candidate)
    candidate <- dirname(candidate)
  }
  getwd()
}

#' Format a decimal as a percentage string.
pct <- function(x, digits = 1) sprintf("%.*f%%", digits, x * 100)

#' Compute a rolling mean of a numeric vector.
rolling_mean <- function(x, k = 5) {
  n <- length(x)
  vapply(seq_len(n), function(i) {
    idx <- max(1, i - k + 1):i
    mean(x[idx], na.rm = TRUE)
  }, numeric(1))
}

#' Safely bind rows — handles NULL entries in list.
safe_bind_rows <- function(lst) {
  lst <- Filter(Negate(is.null), lst)
  if (length(lst) == 0) return(data.frame())
  dplyr::bind_rows(lst)
}

#' Convert concentration ENUM string to numeric score (0-100).
concentration_to_score <- function(level) {
  dplyr::case_when(
    level == "high"       ~ 85,
    level == "medium"     ~ 55,
    level == "low"        ~ 25,
    level == "distracted" ~ 10,
    TRUE                  ~ NA_real_
  )
}

#' Determine recommendation text from average concentration.
recommend_from_concentration <- function(avg_conc) {
  if (is.na(avg_conc) || avg_conc >= 70) {
    "Student is maintaining good concentration. Continue current study habits."
  } else if (avg_conc >= 45) {
    "Moderate concentration detected. Suggest shorter study blocks with breaks."
  } else {
    "Low concentration detected. Recommend one-on-one support or schedule review."
  }
}
