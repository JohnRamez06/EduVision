# ============================================================
# generate_student_weekly.R
# Usage: Rscript generate_student_weekly.R <student_id> <week_id> [output_dir]
# Prints the PDF file path to stdout (first line) for Spring Boot to capture.
# ============================================================

GEN_DIR <- dirname(sys.frame(1)$ofile %||% ".")
ROOT    <- dirname(GEN_DIR)
source(file.path(ROOT, "config.R"),        local = TRUE)
source(file.path(ROOT, "scripts", "utils.R"), local = TRUE)

LOG_FILE <- file.path(ROOT, "logs", "report_generation.log")

args <- commandArgs(trailingOnly = TRUE)
if (length(args) < 2) {
  stop("Usage: Rscript generate_student_weekly.R <student_id> <week_id> [output_dir]")
}
student_id <- args[1]
week_id    <- args[2]
output_dir <- if (length(args) >= 3) args[3] else file.path(ROOT, "output")

ensure_dir(output_dir)

ts        <- format(Sys.time(), "%Y%m%d_%H%M%S")
out_file  <- file.path(output_dir,
               sprintf("student_%s_week%s_%s.pdf", student_id, week_id, ts))
tmpl_path <- file.path(ROOT, "reports", "student_weekly_template.Rmd")

Sys.setenv(ANALYTICS_HOME = ROOT)

log_message(sprintf("Generating student weekly report: %s / week %s",
                    student_id, week_id), LOG_FILE)

rmarkdown::render(
  input       = tmpl_path,
  output_file = out_file,
  params      = list(student_id = student_id, week_id = week_id),
  quiet       = TRUE
)

log_message(sprintf("Report saved: %s", out_file), LOG_FILE)

# First line of stdout = file path (read by Spring Boot ReportService)
cat(out_file, "\n")
    