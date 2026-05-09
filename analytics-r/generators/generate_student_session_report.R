# analytics-r/generators/generate_student_session_report.R
# Usage: Rscript generate_student_session_report.R <student_id> <session_id>
#
# Generates a per-student PDF session report in output/student/
# Output file: student_<studentId>_session_<sessionId>.pdf

args <- commandArgs(trailingOnly = TRUE)
if (length(args) < 2) {
  stop("Usage: Rscript generate_student_session_report.R <student_id> <session_id>")
}
student_id <- args[1]
session_id <- args[2]

suppressPackageStartupMessages({
  library(rmarkdown)
  library(DBI)
  library(RMariaDB)
})

# ── Locate analytics-r root ───────────────────────────────────────────────────
.cmd        <- commandArgs(trailingOnly = FALSE)
.file_flag  <- grep("^--file=", .cmd, value = TRUE)
.script_dir <- if (length(.file_flag) > 0) {
  normalizePath(dirname(sub("^--file=", "", .file_flag[1])))
} else {
  tryCatch(normalizePath(dirname(sys.frame(1)$ofile)), error = function(e) getwd())
}
source(file.path(dirname(.script_dir), "config.R"), local = TRUE)
Sys.setenv(ANALYTICS_HOME = BASE_DIR)

# ── Fetch display metadata ────────────────────────────────────────────────────
conn <- get_connection()
on.exit(try(DBI::dbDisconnect(conn), silent = TRUE), add = TRUE)

student_info <- dbGetQuery(conn, sprintf("
  SELECT CONCAT(u.first_name, ' ', u.last_name) AS student_name
  FROM users u WHERE u.id = '%s'
", student_id))

session_info <- dbGetQuery(conn, sprintf("
  SELECT ls.title AS session_title,
         c.title  AS course_title,
         COALESCE(ls.actual_start, ls.scheduled_start) AS session_date
  FROM lecture_sessions ls
  JOIN courses c ON c.id = ls.course_id
  WHERE ls.id = '%s'
", session_id))

if (nrow(session_info) == 0) stop(paste("Session not found:", session_id))

student_name <- if (nrow(student_info) > 0) student_info$student_name[1] else "Student"
course_name  <- session_info$course_title[1]
session_date <- format(as.Date(session_info$session_date[1]), "%d %B %Y")

DBI::dbDisconnect(conn)
on.exit(NULL)   # clear the disconnect handler since we already did it

# ── Render report ─────────────────────────────────────────────────────────────
output_file <- file.path(OUTPUT_DIRS$student,
                         sprintf("student_%s_session_%s.pdf", student_id, session_id))
template    <- file.path(BASE_DIR, "reports", "student_session_template.Rmd")

if (!file.exists(template)) stop(paste("Template not found:", template))

rmarkdown::render(
  input       = template,
  output_file = normalizePath(output_file, mustWork = FALSE),
  params      = list(
    student_id   = student_id,
    session_id   = session_id,
    student_name = student_name,
    course_name  = course_name,
    session_date = session_date
  ),
  quiet = TRUE
)

cat(normalizePath(output_file, mustWork = FALSE), "\n")
message("Student session report saved: ", output_file)
