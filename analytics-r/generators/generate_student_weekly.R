# analytics-r/generators/generate_student_weekly.R
args <- commandArgs(trailingOnly = TRUE)
if (length(args) < 2) stop("Usage: Rscript generate_student_weekly.R <student_id> <week_id>")

student_id <- args[1]
week_id    <- args[2]

suppressPackageStartupMessages({
  library(DBI)
  library(RMySQL)
  library(dplyr)
  library(ggplot2)
  library(rmarkdown)
})

# ── Database connection ────────────────────────────────────────────────────────
con <- dbConnect(RMySQL::MySQL(),
                 host     = "localhost",
                 user     = "root",
                 password = "",
                 dbname   = "eduvision")
on.exit(dbDisconnect(con), add = TRUE)

# ── Emotion snapshots ──────────────────────────────────────────────────────────
emotion_data <- dbGetQuery(con, sprintf("
  SELECT ses.dominant_emotion, ses.concentration, ses.is_attentive,
         es.snapshot_time, ls.session_id
  FROM   student_emotion_snapshots ses
  JOIN   emotion_snapshots es  ON es.snapshot_id  = ses.snapshot_id
  JOIN   lecture_sessions  ls  ON ls.session_id   = es.session_id
  JOIN   weekly_periods    wp  ON wp.week_id       = '%s'
  WHERE  ses.student_id = '%s'
    AND  es.snapshot_time BETWEEN wp.start_date AND wp.end_date
", week_id, student_id))

avg_concentration <- if (nrow(emotion_data) > 0) mean(emotion_data$concentration, na.rm=TRUE) else 0
dominant_emotion  <- if (nrow(emotion_data) > 0) {
                       names(sort(table(emotion_data$dominant_emotion), decreasing=TRUE))[1]
                     } else {
                       "N/A"
                     }
attentive_pct     <- if (nrow(emotion_data) > 0) {
                       round(mean(emotion_data$is_attentive, na.rm=TRUE) * 100, 1)
                     } else {
                       0
                     }

# ── Attendance ─────────────────────────────────────────────────────────────────
attendance <- dbGetQuery(con, sprintf("
  SELECT sa.status, sa.joined_at, sa.left_at
  FROM   session_attendance sa
  JOIN   lecture_sessions   ls ON ls.session_id = sa.session_id
  JOIN   weekly_periods     wp ON wp.week_id     = '%s'
  WHERE  sa.student_id = '%s'
    AND  ls.start_time BETWEEN wp.start_date AND wp.end_date
", week_id, student_id))

attendance_rate <- if (nrow(attendance) > 0) {
  round(mean(attendance$status != "absent") * 100, 1)
} else {
  0
}

# ── Exit summary ───────────────────────────────────────────────────────────────
exits <- dbGetQuery(con, sprintf("
  SELECT COUNT(*) AS total_exits,
         AVG(TIMESTAMPDIFF(MINUTE, exit_time, return_time)) AS avg_exit_mins
  FROM   session_exit_logs el
  JOIN   lecture_sessions  ls ON ls.session_id = el.session_id
  JOIN   weekly_periods    wp ON wp.week_id     = '%s'
  WHERE  el.student_id = '%s'
    AND  ls.start_time BETWEEN wp.start_date AND wp.end_date
", week_id, student_id))

# ── Student info ───────────────────────────────────────────────────────────────
student_info <- dbGetQuery(con, sprintf("
  SELECT CONCAT(u.first_name, ' ', u.last_name) AS full_name, u.email
  FROM   students s JOIN users u ON u.user_id = s.user_id
  WHERE  s.student_id = '%s'
", student_id))

# ── Output paths ───────────────────────────────────────────────────────────────
output_dir  <- "analytics-r/output/student"
dir.create(output_dir, showWarnings = FALSE, recursive = TRUE)
output_file <- file.path(output_dir, paste0("student_", student_id, "_week_", week_id, ".pdf"))

# ── Render ─────────────────────────────────────────────────────────────────────
rmarkdown::render(
  input       = "analytics-r/templates/student_weekly_template.Rmd",
  output_file = normalizePath(output_file, mustWork = FALSE),
  params = list(
    student_id        = student_id,
    week_id           = week_id,
    full_name         = if (nrow(student_info) > 0) student_info$full_name[1] else "Unknown",
    avg_concentration = round(avg_concentration, 3),
    dominant_emotion  = dominant_emotion,
    attentive_pct     = attentive_pct,
    attendance_rate   = attendance_rate,
    total_exits       = exits$total_exits[1],
    avg_exit_mins     = round(exits$avg_exit_mins[1], 1),
    emotion_data      = emotion_data,
    attendance        = attendance
  ),
  quiet = TRUE
)

cat(normalizePath(output_file, mustWork = FALSE), "\n")
message("Student weekly report saved: ", output_file)