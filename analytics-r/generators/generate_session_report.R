# ============================================================
# generate_session_report.R
# Usage: Rscript generate_session_report.R <session_id> [output_dir]
# ============================================================

GEN_DIR <- dirname(sys.frame(1)$ofile %||% ".")
ROOT    <- dirname(GEN_DIR)
source(file.path(ROOT, "config.R"),           local = TRUE)
source(file.path(ROOT, "scripts", "utils.R"), local = TRUE)

LOG_FILE <- file.path(ROOT, "logs", "report_generation.log")

args <- commandArgs(trailingOnly = TRUE)
if (length(args) < 1) {
  stop("Usage: Rscript generate_session_report.R <session_id> [output_dir]")
}
session_id <- args[1]
output_dir <- if (length(args) >= 2) args[2] else file.path(ROOT, "output")

ensure_dir(output_dir)
Sys.setenv(ANALYTICS_HOME = ROOT)

ts       <- format(Sys.time(), "%Y%m%d_%H%M%S")
out_file <- file.path(output_dir,
              sprintf("session_%s_%s.pdf", session_id, ts))

log_message(sprintf("Generating session report: %s", session_id), LOG_FILE)

rmarkdown::render(
  input       = file.path(ROOT, "reports", "session_template.Rmd"),
  output_file = out_file,
  params      = list(session_id = session_id),
  quiet       = TRUE
)

log_message(sprintf("Report saved: %s", out_file), LOG_FILE)
cat(out_file, "\n")
