# =============================================================================
# analysis/correlation_analysis.R
# =============================================================================
BASE_DIR <- normalizePath(file.path(dirname(sys.frame(1)$ofile), ".."))
if (!exists("get_connection")) source(file.path(BASE_DIR, "config.R"))
if (!exists("fetch_query"))    source(file.path(BASE_DIR, "scripts", "fetch_data.R"))
library(dplyr)

run_correlation_analysis <- function(session_id = NULL) {
  if (!is.null(session_id)) {
    sql <- sprintf(
      "SELECT es.engagement_score, es.avg_concentration, es.student_count,
              HOUR(es.captured_at) AS hour_of_day,
              DAYOFWEEK(es.captured_at) AS day_of_week
       FROM emotion_snapshots es WHERE es.session_id = '%s'", session_id)
  } else {
    sql <- "SELECT es.engagement_score, es.avg_concentration, es.student_count,
                   HOUR(es.captured_at) AS hour_of_day,
                   DAYOFWEEK(es.captured_at) AS day_of_week
            FROM emotion_snapshots es LIMIT 5000"
  }

  df <- fetch_query(sql)
  if (nrow(df) < 5) return(list(error = "Insufficient data for correlation"))

  df <- df[complete.cases(df[, c("engagement_score","avg_concentration","student_count","hour_of_day")]),]

  pearson <- cor(df[, c("engagement_score","avg_concentration","student_count","hour_of_day")],
                 use = "pairwise.complete.obs", method = "pearson")

  # Individual correlations with p-values
  eng_vs_hour   <- cor.test(df$engagement_score, df$hour_of_day)
  eng_vs_size   <- cor.test(df$engagement_score, df$student_count)
  conc_vs_hour  <- cor.test(df$avg_concentration, df$hour_of_day)

  list(
    pearson_matrix      = pearson,
    engagement_vs_hour  = list(r = round(eng_vs_hour$estimate, 3),  p = round(eng_vs_hour$p.value,  4)),
    engagement_vs_size  = list(r = round(eng_vs_size$estimate, 3),  p = round(eng_vs_size$p.value,  4)),
    concentration_vs_hour = list(r = round(conc_vs_hour$estimate,3), p = round(conc_vs_hour$p.value, 4))
  )
}