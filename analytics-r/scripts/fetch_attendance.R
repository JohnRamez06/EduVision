# =============================================================================
# scripts/fetch_attendance.R
# =============================================================================
BASE_DIR <- normalizePath(file.path(dirname(sys.frame(1)$ofile), ".."))
if (!exists("fetch_query")) source(file.path(BASE_DIR, "scripts", "fetch_data.R"))

fetch_attendance <- function(session_id) {
  fetch_query(sprintf(
    "SELECT sa.student_id, sa.status, sa.joined_at, sa.left_at,
            TIMESTAMPDIFF(MINUTE, sa.joined_at, sa.left_at) AS duration_mins,
            CONCAT(u.first_name,' ',u.last_name) AS student_name,
            s.student_number
     FROM session_attendance sa
     JOIN users u    ON u.id = sa.student_id
     LEFT JOIN students s ON s.user_id = sa.student_id
     WHERE sa.session_id = '%s'
     ORDER BY u.last_name ASC", session_id
  ))
}

fetch_attendance_rate <- function(session_id) {
  df  <- fetch_attendance(session_id)
  if (nrow(df) == 0) return(list(total = 0, present = 0, rate = 0))
  list(
    total   = nrow(df),
    present = sum(df$status %in% c("present", "late")),
    rate    = round(mean(df$status %in% c("present", "late")) * 100, 1)
  )
}