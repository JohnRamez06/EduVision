# ============================================================
# batch_enroll.R — Enroll multiple students sequentially
# Usage: Rscript batch_enroll.R <student_id1> <student_id2> ...
# ============================================================

FL_DIR <- dirname(sys.frame(1)$ofile %||% ".")
ROOT   <- dirname(FL_DIR)
source(file.path(ROOT, "scripts", "utils.R"), local = TRUE)

LOG_FILE <- file.path(ROOT, "logs", "enrollment.log")
ENROLL_SCRIPT <- file.path(FL_DIR, "enroll_student.R")

args <- commandArgs(trailingOnly = TRUE)
if (length(args) == 0) stop("Usage: Rscript batch_enroll.R <id1> [<id2> ...]")

log_message(sprintf("Batch enrollment: %d students", length(args)), LOG_FILE)

results <- vapply(args, function(sid) {
  log_message(sprintf("  Processing: %s", sid), LOG_FILE)
  ret <- tryCatch({
    system2("Rscript", args = c(ENROLL_SCRIPT, sid),
            stdout = TRUE, stderr = TRUE)
    "success"
  }, error = function(e) {
    paste("error:", e$message)
  })
  log_message(sprintf("  %s → %s", sid, ret), LOG_FILE)
  ret
}, character(1))

n_ok  <- sum(results == "success")
n_err <- sum(results != "success")
log_message(sprintf("Batch complete: %d ok, %d failed.", n_ok, n_err), LOG_FILE)
cat(sprintf("BATCH_COMPLETE: %d/%d enrolled\n", n_ok, length(args)))
