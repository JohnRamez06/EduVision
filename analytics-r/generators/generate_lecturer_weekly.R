# ============================================================
# generate_lecturer_weekly.R
# Usage: Rscript generate_lecturer_weekly.R <lecturer_id> <week_id> [output_dir]
# ============================================================

GEN_DIR <- dirname(sys.frame(1)$ofile %||% ".")
ROOT    <- dirname(GEN_DIR)
source(file.path(ROOT, "config.R"),           local = TRUE)
source(file.path(ROOT, "scripts", "utils.R"), local = TRUE)

LOG_FILE <- file.path(ROOT, "logs", "report_generation.log")

args <- commandArgs(trailingOnly = TRUE)
if (length(args) < 2) {
  stop("Usage: Rscript generate_lecturer_weekly.R <lecturer_id> <week_id> [output_dir]")
}
lecturer_id <- args[1]
week_id     <- args[2]
output_dir  <- if (length(args) >= 3) args[3] else file.path(ROOT, "output")

ensure_dir(output_dir)
Sys.setenv(ANALYTICS_HOME = ROOT)

ts       <- format(Sys.time(), "%Y%m%d_%H%M%S")
out_file <- file.path(output_dir,
              sprintf("lecturer_%s_week%s_%s.pdf", lecturer_id, week_id, ts))

log_message(sprintf("Generating lecturer weekly report: %s / week %s",
                    lecturer_id, week_id), LOG_FILE)

rmarkdown::render(
  input       = file.path(ROOT, "reports", "lecturer_weekly_template.Rmd"),
  output_file = out_file,
  params      = list(lecturer_id = lecturer_id, week_id = week_id),
  quiet       = TRUE
)

log_message(sprintf("Report saved: %s", out_file), LOG_FILE)
cat(out_file, "\n")
