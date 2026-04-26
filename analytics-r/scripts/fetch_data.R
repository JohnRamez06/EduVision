# =============================================================================
# EduVision - Generic Data Fetch Utilities
# scripts/fetch_data.R
# =============================================================================

BASE_DIR <- normalizePath(file.path(dirname(sys.frame(1)$ofile), ".."))
if (!exists("get_connection")) source(file.path(BASE_DIR, "config.R"))

# -----------------------------------------------------------------------------
# fetch_query(sql) -> data.frame
# -----------------------------------------------------------------------------
fetch_query <- function(sql) {
  con <- get_connection()
  on.exit(dbDisconnect(con))
  tryCatch(
    dbGetQuery(con, sql),
    error = function(e) { log_message(paste("fetch_query failed:", e$message), "ERROR"); data.frame() }
  )
}

# -----------------------------------------------------------------------------
# fetch_week_dates(week_id) -> list(start_date, end_date, week_number, year)
# -----------------------------------------------------------------------------
fetch_week_dates <- function(week_id) {
  df <- fetch_query(sprintf(
    "SELECT week_number, year, start_date, end_date FROM weekly_periods WHERE id = '%s'", week_id
  ))
  if (nrow(df) == 0) stop("weekly_period not found: ", week_id)
  list(
    start_date   = as.POSIXct(df$start_date[1]),
    end_date     = as.POSIXct(df$end_date[1]),
    week_number  = df$week_number[1],
    year         = df$year[1]
  )
}

# -----------------------------------------------------------------------------
# fetch_session(session_id) -> single row data.frame
# -----------------------------------------------------------------------------
fetch_session <- function(session_id) {
  fetch_query(sprintf(
    "SELECT ls.*, c.code AS course_code, c.title AS course_title,
            CONCAT(u.first_name,' ',u.last_name) AS lecturer_name
     FROM lecture_sessions ls
     JOIN courses c ON c.id = ls.course_id
     JOIN users u   ON u.id = ls.lecturer_id
     WHERE ls.id = '%s'", session_id
  ))
}

# -----------------------------------------------------------------------------
# fetch_course(course_id) -> single row data.frame
# -----------------------------------------------------------------------------
fetch_course <- function(course_id) {
  fetch_query(sprintf(
    "SELECT c.*, CONCAT(u.first_name,' ',u.last_name) AS primary_lecturer
     FROM courses c
     LEFT JOIN course_lecturers cl ON cl.course_id = c.id AND cl.is_primary = 1
     LEFT JOIN users u ON u.id = cl.lecturer_id
     WHERE c.id = '%s'", course_id
  ))
}

# -----------------------------------------------------------------------------
# fetch_student_info(student_id) -> single row data.frame
# -----------------------------------------------------------------------------
fetch_student_info <- function(student_id) {
  fetch_query(sprintf(
    "SELECT u.first_name, u.last_name, u.email, s.student_number, s.program, s.year_of_study
     FROM users u JOIN students s ON s.user_id = u.id WHERE u.id = '%s'", student_id
  ))
}