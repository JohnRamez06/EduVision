# ============================================================
# generate_comparison_report.R
# Usage: Rscript generate_comparison_report.R <lecturers|courses> <id1,id2,...> [output_dir]
# ============================================================

GEN_DIR <- dirname(sys.frame(1)$ofile %||% ".")
ROOT    <- dirname(GEN_DIR)
source(file.path(ROOT, "config.R"),           local = TRUE)
source(file.path(ROOT, "scripts", "utils.R"), local = TRUE)

LOG_FILE <- file.path(ROOT, "logs", "report_generation.log")

args <- commandArgs(trailingOnly = TRUE)
if (length(args) < 2) {
  stop("Usage: Rscript generate_comparison_report.R <lecturers|courses> <id1,id2,...> [output_dir]")
}
compare_type <- args[1]
ids_csv      <- args[2]
output_dir   <- if (length(args) >= 3) args[3] else file.path(ROOT, "output")

ensure_dir(output_dir)
Sys.setenv(ANALYTICS_HOME = ROOT)

ts       <- format(Sys.time(), "%Y%m%d_%H%M%S")
out_file <- file.path(output_dir,
              sprintf("comparison_%s_%s.pdf", compare_type, ts))

log_message(sprintf("Generating comparison report: %s [%s]",
                    compare_type, ids_csv), LOG_FILE)

rmarkdown::render(
  input       = file.path(ROOT, "reports", "comparison_template.Rmd"),
  output_file = out_file,
  params      = list(compare_type = compare_type, ids = ids_csv),
  quiet       = TRUE
)

log_message(sprintf("Report saved: %s", out_file), LOG_FILE)
cat(out_file, "\n")
