# ============================================================
# fetch_attendance.R — Fetch session attendance data
# ============================================================

SCRIPTS_DIR <- dirname(sys.frame(1)$ofile %||% ".")
source(file.path(dirname(SCRIPTS_DIR), "config.R"), local = TRUE)

#' Fetch attendance records for a single session.
fetch_attendance_for_session <- function(session_id) {
  with_connection(function(con) {
    dbGetQuery(con, sqlInterpolate(con,
      "SELECT sa.id, sa.session_id, sa.student_id, sa.status,
              sa.joined_at, sa.left_at,
              CONCAT(u.first_name,' ',u.last_name) AS student_name
         FROM session_attendance sa
         JOIN users u ON u.id = sa.student_id
        WHERE sa.session_id = ?sess",
      sess = session_id))
  })
}

#' Fetch attendance for a student over a date range.
fetch_student_attendance_range <- function(student_id, date_from, date_to) {
  with_connection(function(con) {
    dbGetQuery(con, sqlInterpolate(con,
      "SELECT sa.session_id, sa.status, sa.joined_at, sa.left_at,
              ls.actual_start, ls.course_id, c.code AS course_code
         FROM session_attendance sa
         JOIN lecture_sessions ls ON ls.id = sa.session_id
         JOIN courses c ON c.id = ls.course_id
        WHERE sa.student_id = ?sid
          AND ls.actual_start BETWEEN ?df AND ?dt
        ORDER BY ls.actual_start ASC",
      sid = student_id,
      df  = format(as.POSIXct(date_from), "%Y-%m-%d %H:%M:%S"),
      dt  = format(as.POSIXct(date_to),   "%Y-%m-%d %H:%M:%S")))
  })
}

#' Compute attendance rate for a student.
compute_attendance_rate <- function(student_id, date_from = NULL, date_to = NULL) {
  with_connection(function(con) {
    sql <- "
      SELECT COUNT(*) AS total,
             SUM(CASE WHEN sa.status IN ('present','late') THEN 1 ELSE 0 END) AS attended
        FROM session_attendance sa
        JOIN lecture_sessions ls ON ls.id = sa.session_id
       WHERE sa.student_id = ?"
    params <- list(student_id)
    if (!is.null(date_from)) {
      sql    <- paste0(sql, " AND ls.actual_start >= ?")
      params <- c(params, list(format(date_from)))
    }
    if (!is.null(date_to)) {
      sql    <- paste0(sql, " AND ls.actual_start <= ?")
      params <- c(params, list(format(date_to)))
    }
    res <- dbGetQuery(con, do.call(sqlInterpolate,
                                   c(list(con, sql), params)))
    if (res$total == 0) return(NA_real_)
    res$attended / res$total
  })
}
