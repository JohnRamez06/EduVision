# =============================================================================
# EduVision - Batch Face Enrollment
# face_learning/batch_enroll.R
#
# Usage: Rscript batch_enroll.R <student_id1> <student_id2> ...
#    or: Rscript batch_enroll.R ALL   (enroll all students missing face_encoding)
# =============================================================================

args <- commandArgs(trailingOnly = TRUE)
BASE_DIR <- normalizePath(file.path(dirname(sys.frame(1)$ofile), ".."))
source(file.path(BASE_DIR, "config.R"))

if (length(args) == 0) stop("Usage: Rscript batch_enroll.R <student_id1> ... | ALL")

if (args[1] == "ALL") {
  students_df <- query_df(
    "SELECT s.user_id FROM students s
     WHERE s.face_encoding IS NULL OR s.face_encoding = ''"
  )
  student_ids <- students_df$user_id
  log_message(sprintf("Batch enroll ALL: found %d students without face encodings", length(student_ids)))
} else {
  student_ids <- args
}

if (length(student_ids) == 0) {
  log_message("No students to enroll", "WARN")
  quit(status = 0)
}

results <- list()
for (sid in student_ids) {
  log_message(sprintf("--- Enrolling %s ---", sid))
  result <- tryCatch({
    script <- file.path(BASE_DIR, "face_learning", "enroll_student.R")
    system2("Rscript", args = c(script, sid), stdout = TRUE, stderr = TRUE)
    list(student_id = sid, status = "success")
  }, error = function(e) {
    log_message(paste("Failed:", sid, e$message), "ERROR")
    list(student_id = sid, status = "failed", error = e$message)
  })
  results[[sid]] <- result
}

success_count <- sum(sapply(results, function(r) r$status == "success"))
log_message(sprintf("Batch enroll complete: %d/%d succeeded", success_count, length(student_ids)))
cat(jsonlite::toJSON(results, auto_unbox = TRUE), "\n")