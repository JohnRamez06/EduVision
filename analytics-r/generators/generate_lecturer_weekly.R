# analytics-r/generators/generate_lecturer_weekly.R
# Usage: Rscript generate_lecturer_weekly.R <lecturer_id> <week_id>
args <- commandArgs(trailingOnly = TRUE)
if (length(args) < 2) {
  stop("Usage: Rscript generate_lecturer_weekly.R <lecturer_id> <week_id>")
}
lecturer_id <- args[1]
week_id     <- args[2]

suppressPackageStartupMessages(library(rmarkdown))

# Reliably find this script's directory
.cmd <- commandArgs(trailingOnly = FALSE)
.file_flag <- grep("^--file=", .cmd, value = TRUE)
.script_dir <- if (length(.file_flag) > 0) {
  normalizePath(dirname(sub("^--file=", "", .file_flag[1])))
} else {
  tryCatch(normalizePath(dirname(sys.frame(1)$ofile)), error = function(e) getwd())
}
source(file.path(dirname(.script_dir), "config.R"), local = TRUE)
Sys.setenv(ANALYTICS_HOME = BASE_DIR)

LOG_FILE <- file.path(BASE_DIR, "logs", "report_generation.log")
dir.create(dirname(LOG_FILE), showWarnings = FALSE, recursive = TRUE)

ts <- format(Sys.time(), "%Y-%m-%d %H:%M:%S")
cat(sprintf("[%s] Generating lecturer weekly report: %s / week %s\n",
            ts, lecturer_id, week_id),
    file = LOG_FILE, append = TRUE)

# Java ReportService expects: analytics-r/output/lecturer/lecturer_<id>_week_<weekId>.pdf
output_file <- file.path(OUTPUT_DIRS$lecturer,
                         sprintf("lecturer_%s_week_%s.pdf", lecturer_id, week_id))
template    <- file.path(BASE_DIR, "reports", "lecturer_weekly_template.Rmd")

rmarkdown::render(
  input       = template,
  output_file = normalizePath(output_file, mustWork = FALSE),
  params      = list(lecturer_id = lecturer_id, week_id = week_id),
  quiet       = TRUE
)

ts2 <- format(Sys.time(), "%Y-%m-%d %H:%M:%S")
cat(sprintf("[%s] Report saved: %s\n", ts2, output_file), file = LOG_FILE, append = TRUE)
cat(output_file, "\n")
message("Lecturer weekly report saved: ", output_file)
