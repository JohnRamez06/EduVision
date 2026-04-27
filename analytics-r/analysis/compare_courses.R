# ============================================================
# compare_courses.R — Side-by-side course comparison
# ============================================================

AN_DIR <- dirname(sys.frame(1)$ofile %||% ".")
ROOT   <- dirname(AN_DIR)
source(file.path(ROOT, "config.R"),   local = TRUE)
source(file.path(ROOT, "scripts", "utils.R"), local = TRUE)

#' Compare multiple courses.
#' @param course_ids  Character vector.
#' @param date_from   POSIXct or NULL.
#' @param date_to     POSIXct or NULL.
#' @return data.frame with one row per course.
compare_courses <- function(course_ids, date_from = NULL, date_to = NULL) {
  results <- lapply(course_ids, function(cid) {
    sql <- "
      SELECT c.id AS course_id, c.code, c.title AS course_title,
             COUNT(DISTINCT ls.id)          AS n_sessions,
             AVG(es.engagement_score)       AS avg_engagement,
             AVG(es.avg_concentration)      AS avg_concentration,
             COUNT(DISTINCT sa.student_id)  AS total_students
        FROM courses c
        JOIN lecture_sessions ls ON ls.course_id = c.id
   LEFT JOIN emotion_snapshots es ON es.session_id = ls.id
   LEFT JOIN session_attendance sa ON sa.session_id = ls.id
                                   AND sa.status IN ('present','late')
       WHERE c.id = ?"
    params <- list(cid)
    if (!is.null(date_from)) {
      sql    <- paste0(sql, " AND ls.actual_start >= ?")
      params <- c(params, list(format(date_from)))
    }
    if (!is.null(date_to)) {
      sql    <- paste0(sql, " AND ls.actual_start <= ?")
      params <- c(params, list(format(date_to)))
    }
    sql <- paste0(sql, " GROUP BY c.id, c.code, c.title")
    with_connection(function(con) {
      dbGetQuery(con, do.call(sqlInterpolate, c(list(con, sql), params)))
    })
  })
  do.call(rbind, Filter(function(d) nrow(d) > 0, results))
}
