# =============================================================================
# analysis/concentration_analysis.R
# =============================================================================
BASE_DIR <- normalizePath(file.path(dirname(sys.frame(1)$ofile), ".."))
source(file.path(BASE_DIR, "config.R"))
source(file.path(BASE_DIR, "scripts", "calculate_statistics.R"))
library(dplyr)

concentration_analysis <- function(snapshots_df) {
  conc_map <- c(high = 85, medium = 60, low = 35, distracted = 10)
  if ("concentration" %in% names(snapshots_df)) {
    scores <- conc_map[snapshots_df$concentration]
  } else {
    scores <- as.numeric(snapshots_df$avg_concentration)
  }
  scores <- scores[!is.na(scores)]
  if (length(scores) == 0) return(list(error = "No concentration data"))

  dist <- table(classify_concentration(scores))
  low_periods <- which(scores < 35)
  runs <- rle(scores < 35)
  sustained_low <- sum(runs$values & runs$lengths >= 5)

  list(
    mean_concentration = round(mean(scores), 1),
    median_concentration = round(median(scores), 1),
    distribution       = as.list(prop.table(dist)),
    low_concentration_indices = low_periods,
    sustained_low_periods = sustained_low,
    trend             = trend_direction(scores)
  )
}