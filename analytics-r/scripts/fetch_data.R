# ============================================================
# fetch_data.R — Generic data-fetching helpers
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

#' Fetch all lecture sessions for a course between two dates.
fetch_sessions_for_course <- function(course_id, date_from = NULL, date_to = NULL) {
  with_connection(function(con) {
    sql <- "
      SELECT ls.id, ls.course_id, ls.lecturer_id,
             ls.title, ls.status,
             ls.scheduled_start, ls.actual_start, ls.actual_end,
             CONCAT(u.first_name, ' ', u.last_name) AS lecturer_name,
             c.code AS course_code, c.title AS course_title
        FROM lecture_sessions ls
        JOIN courses c ON c.id = ls.course_id
        JOIN users   u ON u.id = ls.lecturer_id
       WHERE ls.course_id = ?"
    params <- list(course_id)
    if (!is.null(date_from)) {
      sql <- paste0(sql, " AND ls.actual_start >= ?")
      params <- c(params, list(format(date_from)))
    }
    if (!is.null(date_to)) {
      sql <- paste0(sql, " AND ls.actual_start <= ?")
      params <- c(params, list(format(date_to)))
    }
    sql <- paste0(sql, " ORDER BY ls.actual_start")
    dbGetQuery(con, do.call(sqlInterpolate,
                            c(list(con, sql), params)))
  })
}

#' Fetch weekly period row by id (UUID) or week number (integer string).
fetch_weekly_period <- function(week_id) {
  with_connection(function(con) {
    if (grepl("^[0-9]+$", as.character(week_id))) {
      dbGetQuery(con, sqlInterpolate(con,
        "SELECT * FROM weekly_periods WHERE week_number = ?wn ORDER BY year DESC LIMIT 1",
        wn = as.integer(week_id)))
    } else {
      dbGetQuery(con, sqlInterpolate(con,
        "SELECT * FROM weekly_periods WHERE id = ?week_id",
        week_id = week_id))
    }
  })
}

#' Fetch all weekly periods for the past N weeks.
fetch_recent_weekly_periods <- function(n_weeks = 8) {
  with_connection(function(con) {
    dbGetQuery(con, sprintf(
      "SELECT * FROM weekly_periods ORDER BY year DESC, week_number DESC LIMIT %d",
      as.integer(n_weeks)))
  })
}

#' Fetch lecturer info row.
fetch_lecturer <- function(lecturer_id) {
  with_connection(function(con) {
    dbGetQuery(con, sqlInterpolate(con,
      "SELECT u.id, u.first_name, u.last_name, u.email,
              CONCAT(u.first_name,' ',u.last_name) AS full_name
         FROM users u WHERE u.id = ?lid", lid = lecturer_id))
  })
}

#' Fetch student info row.
fetch_student <- function(student_id) {
  with_connection(function(con) {
    dbGetQuery(con, sqlInterpolate(con,
      "SELECT u.id, u.first_name, u.last_name, u.email,
              CONCAT(u.first_name,' ',u.last_name) AS full_name,
              s.student_number, s.program, s.year_of_study
         FROM users u
         JOIN students s ON s.user_id = u.id
        WHERE u.id = ?sid", sid = student_id))
  })
}

#' Fetch all students enrolled in a course.
fetch_students_in_course <- function(course_id) {
  with_connection(function(con) {
    dbGetQuery(con, sqlInterpolate(con,
      "SELECT u.id, CONCAT(u.first_name,' ',u.last_name) AS full_name,
              s.student_number
         FROM users u
         JOIN students s ON s.user_id = u.id
         JOIN course_students cs ON cs.student_id = u.id
        WHERE cs.course_id = ?cid", cid = course_id))
  })
}
