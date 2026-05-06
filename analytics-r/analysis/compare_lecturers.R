# ============================================================
# compare_lecturers.R — Side-by-side lecturer comparison
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

#' Compare multiple lecturers on engagement, concentration, and attendance.
#' @param lecturer_ids Character vector of lecturer user IDs.
#' @param date_from    POSIXct or NULL.
#' @param date_to      POSIXct or NULL.
#' @return data.frame with one row per lecturer.
compare_lecturers <- function(lecturer_ids,
                              date_from = NULL,
                              date_to   = NULL) {
  results <- lapply(lecturer_ids, function(lid) {
    sql <- "
      SELECT ls.lecturer_id,
             CONCAT(u.first_name,' ',u.last_name)  AS lecturer_name,
             COUNT(DISTINCT ls.id)                 AS n_sessions,
             AVG(es.engagement_score)              AS avg_engagement,
             AVG(es.avg_concentration)             AS avg_concentration,
             COUNT(DISTINCT sa.student_id)         AS total_students
        FROM lecture_sessions ls
        JOIN users u ON u.id = ls.lecturer_id
   LEFT JOIN emotion_snapshots es ON es.session_id = ls.id
   LEFT JOIN session_attendance sa ON sa.session_id = ls.id
                                   AND sa.status IN ('present','late')
       WHERE ls.lecturer_id = ?"
    params <- list(lid)
    if (!is.null(date_from)) {
      sql    <- paste0(sql, " AND ls.actual_start >= ?")
      params <- c(params, list(format(date_from)))
    }
    if (!is.null(date_to)) {
      sql    <- paste0(sql, " AND ls.actual_start <= ?")
      params <- c(params, list(format(date_to)))
    }
    sql <- paste0(sql, " GROUP BY ls.lecturer_id, u.first_name, u.last_name")
    with_connection(function(con) {
      dbGetQuery(con, do.call(sqlInterpolate, c(list(con, sql), params)))
    })
  })
  do.call(rbind, Filter(function(d) nrow(d) > 0, results))
}
