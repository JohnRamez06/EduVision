# ============================================================
# enroll_student.R — Main enrollment entry point
# Usage: Rscript enroll_student.R <student_id>
# ============================================================

FL_DIR <- dirname(sys.frame(1)$ofile %||% ".")
ROOT   <- dirname(FL_DIR)
source(file.path(ROOT, "config.R"),                          local = TRUE)
source(file.path(ROOT, "scripts", "utils.R"),                local = TRUE)
source(file.path(FL_DIR, "verify_enrollment.R"),             local = TRUE)
source(file.path(FL_DIR, "extract_embeddings.R"),            local = TRUE)
source(file.path(FL_DIR, "update_recognition_model.R"),      local = TRUE)

LOG_FILE <- file.path(ROOT, "logs", "enrollment.log")

args <- commandArgs(trailingOnly = TRUE)
if (length(args) < 1) stop("Usage: Rscript enroll_student.R <student_id>")
student_id <- args[1]

log_message(sprintf("=== Enrolling student: %s ===", student_id), LOG_FILE)

# 1. Verify enrollment photos
photos_dir <- file.path("face_enrollment", student_id)
log_message("Step 1: Verifying photos...", LOG_FILE)
verification <- verify_enrollment(student_id, photos_dir)
if (!verification$valid) {
  log_message(sprintf("Verification FAILED: %s", verification$issues), LOG_FILE)
  stop(sprintf("Enrollment aborted for student %s: %s",
               student_id, verification$issues))
}
log_message(sprintf("Verification OK (%d photos).",
                    nrow(verification$results)), LOG_FILE)

# 2. Extract embeddings
log_message("Step 2: Extracting face embeddings...", LOG_FILE)
emb_list <- extract_student_embeddings(student_id, photos_dir)
if (length(emb_list) == 0) {
  log_message("No embeddings extracted.", LOG_FILE)
  stop("No valid embeddings could be extracted.")
}
log_message(sprintf("Extracted %d embeddings.", length(emb_list)), LOG_FILE)

# 3. Average embeddings
avg_emb <- average_embeddings(emb_list)
emb_json <- jsonlite::toJSON(as.numeric(avg_emb), auto_unbox = TRUE)

# 4. Store in DB
log_message("Step 3: Saving face encoding to DB...", LOG_FILE)
with_connection(function(con) {
  dbExecute(con,
    sqlInterpolate(con,
      "UPDATE students SET face_encoding = ?emb WHERE user_id = ?sid",
      emb = emb_json,
      sid = student_id))
})
log_message("Face encoding saved.", LOG_FILE)

# 5. Update recognition model
log_message("Step 4: Updating recognition model...", LOG_FILE)
update_recognition_model()

log_message(sprintf("Enrollment complete for student %s.", student_id), LOG_FILE)
cat("SUCCESS\n")
