# ============================================================
# correlation_analysis.R — Pearson correlations between key metrics
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

#' Compute a table of Pearson correlations for a set of sessions.
#' Metrics: confusion_rate, engagement_score, concentration_score,
#'          hour_of_day, class_size.
#' @param session_ids Character vector of session IDs.
#' @return data.frame with columns: var1, var2, r, p_value.
correlation_analysis <- function(session_ids) {
  data <- with_connection(function(con) {
    placeholders <- paste(sprintf("'%s'", session_ids), collapse = ",")
    dbGetQuery(con, sprintf("
      SELECT
        HOUR(es.captured_at)                                     AS hour_of_day,
        es.student_count                                         AS class_size,
        es.engagement_score,
        es.avg_concentration,
        SUM(CASE WHEN ses.emotion='confused' THEN 1 ELSE 0 END) /
          NULLIF(COUNT(ses.id),0)                                AS confusion_rate
      FROM emotion_snapshots es
      LEFT JOIN student_emotion_snapshots ses ON ses.snapshot_id = es.id
      WHERE es.session_id IN (%s)
      GROUP BY es.id, es.captured_at, es.student_count,
               es.engagement_score, es.avg_concentration
    ", placeholders))
  })

  if (nrow(data) < 5) {
    message("Not enough data points for correlation analysis.")
    return(data.frame())
  }

  pairs <- list(
    c("confusion_rate",   "hour_of_day"),
    c("engagement_score", "class_size"),
    c("avg_concentration","hour_of_day"),
    c("engagement_score", "avg_concentration"),
    c("confusion_rate",   "engagement_score")
  )

  results <- lapply(pairs, function(p) {
    x <- as.numeric(data[[p[1]]])
    y <- as.numeric(data[[p[2]]])
    valid <- !is.na(x) & !is.na(y)
    if (sum(valid) < 5) return(NULL)
    ct <- cor.test(x[valid], y[valid], method = "pearson")
    data.frame(var1    = p[1],
               var2    = p[2],
               r       = round(as.numeric(ct$estimate), 4),
               p_value = round(ct$p.value, 4),
               stringsAsFactors = FALSE)
  })

  do.call(rbind, Filter(Negate(is.null), results))
}
