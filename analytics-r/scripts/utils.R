# =============================================================================
# scripts/utils.R
# =============================================================================

format_date <- function(dt, fmt = "%d %b %Y") {
  format(as.POSIXct(dt), fmt)
}

format_percentage <- function(x, digits = 1) {
  sprintf("%.*f%%", digits, as.numeric(x) * 100)
}

safe_divide <- function(numerator, denominator, fallback = 0) {
  ifelse(denominator == 0 | is.na(denominator), fallback, numerator / denominator)
}

# Create output subfolder if it doesn't exist, return path
ensure_output_dir <- function(subdir) {
  BASE_DIR  <- Sys.getenv("R_BASE_DIR", normalizePath(file.path(dirname(sys.frame(1)$ofile), "..")))
  OUTPUT    <- Sys.getenv("OUTPUT_DIR",  file.path(BASE_DIR, "output"))
  full_path <- file.path(OUTPUT, subdir)
  if (!dir.exists(full_path)) dir.create(full_path, recursive = TRUE)
  full_path
}

# Emotion colour palette consistent across all charts
emotion_colours <- function() {
  c(
    happy     = "#22c55e", engaged   = "#3b82f6",
    neutral   = "#94a3b8", confused  = "#f59e0b",
    sad       = "#6366f1", angry     = "#ef4444",
    fearful   = "#8b5cf6", disgusted = "#84cc16",
    surprised = "#ec4899"
  )
}