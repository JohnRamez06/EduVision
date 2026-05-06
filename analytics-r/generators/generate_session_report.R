# analytics-r/generators/generate_session_report.R
args <- commandArgs(trailingOnly = TRUE)
if (length(args) < 1) stop("Usage: Rscript generate_session_report.R <session_id>")

session_id <- args[1]

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

# Set ANALYTICS_HOME so the template and its sourced scripts can find config.R
Sys.setenv(ANALYTICS_HOME = BASE_DIR)

# ── Render PDF ─────────────────────────────────────────────────────────────────
output_file <- file.path(OUTPUT_DIRS$session, paste0("session_", session_id, ".pdf"))
template    <- file.path(BASE_DIR, "reports", "session_template.Rmd")

rmarkdown::render(
  input       = template,
  output_file = normalizePath(output_file, mustWork = FALSE),
  params      = list(session_id = session_id),
  quiet       = TRUE
)

cat(normalizePath(output_file, mustWork = FALSE), "\n")
message("Session report saved: ", output_file)
