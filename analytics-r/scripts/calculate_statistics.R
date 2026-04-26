# =============================================================================
# scripts/calculate_statistics.R
# =============================================================================

# Mean with 95% confidence interval
mean_ci <- function(x, conf = 0.95) {
  x   <- na.omit(x)
  n   <- length(x)
  if (n < 2) return(list(mean = mean(x), lower = NA, upper = NA, n = n))
  se  <- sd(x) / sqrt(n)
  t   <- qt((1 + conf) / 2, df = n - 1)
  list(mean = mean(x), lower = mean(x) - t * se, upper = mean(x) + t * se, n = n)
}

# Median + IQR
median_iqr <- function(x) {
  x <- na.omit(x)
  list(median = median(x), q1 = quantile(x, 0.25), q3 = quantile(x, 0.75), iqr = IQR(x))
}

# Trend direction from a numeric vector
# Returns: list(direction, slope, p_value)
trend_direction <- function(x) {
  x <- na.omit(as.numeric(x))
  if (length(x) < 3) return(list(direction = "stable", slope = 0, p_value = 1))
  t   <- seq_along(x)
  fit <- lm(x ~ t)
  sm  <- summary(fit)
  slope   <- coef(fit)[2]
  p_value <- sm$coefficients[2, 4]
  direction <- if (p_value > 0.05) "stable" else if (slope > 0) "improving" else "declining"
  list(direction = direction, slope = round(slope, 5), p_value = round(p_value, 4))
}

# Moving average
moving_avg <- function(x, window = 5) {
  filter(x, rep(1 / window, window), sides = 2)
}

# Engagement level classification
classify_engagement <- function(score) {
  dplyr::case_when(
    score >= 0.75 ~ "high",
    score >= 0.50 ~ "medium",
    score >= 0.25 ~ "low",
    TRUE          ~ "very_low"
  )
}

# Concentration classification matching DB enum
classify_concentration <- function(score_0_to_100) {
  dplyr::case_when(
    score_0_to_100 >= 70 ~ "high",
    score_0_to_100 >= 45 ~ "medium",
    score_0_to_100 >= 20 ~ "low",
    TRUE                 ~ "distracted"
  )
}