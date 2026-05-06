# analytics-r/generators/generate_student_weekly.R
# Usage: Rscript generate_student_weekly.R <student_id> <week_id>
args <- commandArgs(trailingOnly = TRUE)
if (length(args) < 2) {
  stop("Usage: Rscript generate_student_weekly.R <student_id> <week_id>")
}
student_id <- args[1]
week_id    <- args[2]

suppressPackageStartupMessages({
  library(rmarkdown)
  library(knitr)
  library(ggplot2)
  library(dplyr)
})

.cmd <- commandArgs(trailingOnly = FALSE)
.file_flag <- grep("^--file=", .cmd, value = TRUE)
.script_dir <- if (length(.file_flag) > 0) {
  normalizePath(dirname(sub("^--file=", "", .file_flag[1])))
} else {
  tryCatch(normalizePath(dirname(sys.frame(1)$ofile)), error = function(e) getwd())
}
source(file.path(dirname(.script_dir), "config.R"), local = TRUE)

conn <- get_connection()
on.exit(dbDisconnect(conn), add = TRUE)

student_info <- dbGetQuery(conn, sprintf("
  SELECT s.user_id AS student_id, s.student_number,
         CONCAT(u.first_name, ' ', u.last_name) AS student_name,
         COALESCE(s.program, 'Not Specified') AS program,
         COALESCE(s.year_of_study, 1) AS year_of_study,
         u.email
  FROM students s JOIN users u ON u.id = s.user_id
  WHERE s.user_id = '%s'
", student_id))

if (nrow(student_info) == 0) stop(paste("Student not found:", student_id))
message(sprintf("Found student: %s", student_info$student_name[1]))

week_info <- dbGetQuery(conn, sprintf("
  SELECT id, week_number, year, start_date, end_date
  FROM weekly_periods
  WHERE week_number = %s
  ORDER BY year DESC LIMIT 1
", week_id))

if (nrow(week_info) == 0) {
  week_info <- dbGetQuery(conn, "
    SELECT id, week_number, year, start_date, end_date
    FROM weekly_periods ORDER BY year DESC, week_number DESC LIMIT 1
  ")
}
if (nrow(week_info) == 0) stop("No week periods found in database.")
week_display <- sprintf("Week %d, %d", week_info$week_number[1], week_info$year[1])

weekly_attendance <- dbGetQuery(conn, sprintf("
  SELECT wp.week_number, wp.year, wp.start_date, wp.end_date,
         wca.course_id, c.code AS course_name,
         wca.sessions_held, wca.sessions_attended,
         wca.sessions_missed, wca.attendance_rate, wca.status
  FROM weekly_course_attendance wca
  JOIN courses c ON c.id = wca.course_id
  JOIN weekly_periods wp ON wp.id = wca.week_id
  WHERE wca.student_id = '%s'
  ORDER BY wp.year DESC, wp.week_number DESC LIMIT 10
", student_id))

emotion_trends <- dbGetQuery(conn, sprintf("
  SELECT DATE(ses.captured_at) AS date, ses.emotion, COUNT(*) AS count
  FROM student_emotion_snapshots ses
  WHERE ses.student_id = '%s'
  GROUP BY DATE(ses.captured_at), ses.emotion
  ORDER BY date DESC LIMIT 30
", student_id))

concentration_trends <- dbGetQuery(conn, sprintf("
  SELECT DATE(ses.captured_at) AS date,
         AVG(CASE WHEN ses.concentration = 'high'   THEN 0.9
                  WHEN ses.concentration = 'medium' THEN 0.6
                  WHEN ses.concentration = 'low'    THEN 0.3
                  ELSE 0.5 END) AS avg_concentration
  FROM student_emotion_snapshots ses
  WHERE ses.student_id = '%s'
  GROUP BY DATE(ses.captured_at) ORDER BY date DESC LIMIT 30
", student_id))

# Java ReportService expects: analytics-r/output/student/student_<studentId>_week_<weekId>.pdf
output_file <- file.path(OUTPUT_DIRS$student,
                         sprintf("student_%s_week_%s.pdf", student_id, week_id))
template    <- file.path(BASE_DIR, "reports", "student_weekly_template.Rmd")

if (!file.exists(template)) stop(paste("Template not found:", template))

rmarkdown::render(
  input       = template,
  output_file = normalizePath(output_file, mustWork = FALSE),
  params      = list(
    student_name         = student_info$student_name[1],
    student_number       = student_info$student_number[1],
    program              = student_info$program[1],
    year_of_study        = student_info$year_of_study[1],
    week_display         = week_display,
    week_start           = as.character(week_info$start_date[1]),
    week_end             = as.character(week_info$end_date[1]),
    weekly_attendance    = weekly_attendance,
    emotion_trends       = emotion_trends,
    concentration_trends = concentration_trends
  ),
  quiet = TRUE
)

cat(output_file, "\n")
message("Student weekly report saved: ", output_file)
