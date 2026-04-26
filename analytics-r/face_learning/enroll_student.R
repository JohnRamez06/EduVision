# =============================================================================
# EduVision - Student Face Enrollment (MAIN SCRIPT)
# face_learning/enroll_student.R
#
# Usage: Rscript enroll_student.R <student_id>
# =============================================================================

args <- commandArgs(trailingOnly = TRUE)
if (length(args) < 1) stop("Usage: Rscript enroll_student.R <student_id>")
student_id <- args[1]

BASE_DIR <- normalizePath(file.path(dirname(sys.frame(1)$ofile), ".."))
source(file.path(BASE_DIR, "config.R"))
source(file.path(BASE_DIR, "face_learning", "verify_enrollment.R"))
source(file.path(BASE_DIR, "face_learning", "extract_embeddings.R"))
source(file.path(BASE_DIR, "face_learning", "update_recognition_model.R"))
source(file.path(BASE_DIR, "reticulate", "embedding_extraction.R"))

enroll_student <- function(student_id) {
  log_message(sprintf("=== Starting enrollment for student: %s ===", student_id))

  # 1. Verify student exists in DB
  student <- query_df(sprintf(
    "SELECT s.user_id, u.first_name, u.last_name FROM students s
     JOIN users u ON u.id = s.user_id WHERE s.user_id = '%s'", student_id
  ))
  if (nrow(student) == 0) stop("Student not found in database: ", student_id)
  log_message(sprintf("Student: %s %s", student$first_name, student$last_name))

  # 2. Verify photo quality
  log_message("Step 1/4: Verifying photos...")
  verification <- verify_enrollment(student_id)
  valid_photos  <- Filter(function(v) isTRUE(v$valid), verification)
  invalid       <- Filter(function(v) !isTRUE(v$valid), verification)

  if (length(invalid) > 0) {
    log_message(sprintf("WARNING: %d photos failed quality check:", length(invalid)), "WARN")
    lapply(invalid, function(v) log_message(sprintf("  - %s: %s", v$file, v$issues), "WARN"))
  }
  if (length(valid_photos) < 3) {
    stop(sprintf("Enrollment aborted: only %d valid photos (minimum 3 required)", length(valid_photos)))
  }
  log_message(sprintf("%d/%d photos passed quality check", length(valid_photos), length(verification)))

  # 3. Extract embeddings
  log_message("Step 2/4: Extracting face embeddings...")
  embeddings <- extract_student_embeddings(student_id)
  if (length(embeddings) == 0) stop("No embeddings extracted")

  # 4. Average embeddings into one representative vector
  log_message("Step 3/4: Computing average embedding...")
  avg_embedding <- average_embeddings(embeddings)
  if (is.null(avg_embedding)) stop("Failed to compute average embedding")

  embedding_json <- embedding_to_json(avg_embedding)

  # 5. Save to DB
  log_message("Step 4/4: Saving to database...")
  execute_sql(sprintf(
    "UPDATE students SET face_encoding = '%s' WHERE user_id = '%s'",
    gsub("'", "''", embedding_json), student_id
  ))
  log_message("Face encoding saved to students table")

  # 6. Also record in face_enrollment_photos table
  photo_dir <- file.path(FACE_DIR, student_id)
  photos    <- list.files(photo_dir, pattern = "\\.(jpg|jpeg|png|bmp)$",
                          full.names = FALSE, ignore.case = TRUE)
  for (fname in photos) {
    tryCatch(execute_sql(sprintf(
      "INSERT IGNORE INTO face_enrollment_photos (student_id, photo_filename, created_at)
       VALUES ('%s', '%s', NOW())",
      student_id, fname
    )), error = function(e) invisible())
  }

  # 7. Update recognition model
  log_message("Updating recognition model...")
  update_recognition_model()

  log_message(sprintf("=== Enrollment complete for student: %s ===", student_id))
  invisible(list(student_id = student_id, embedding_dim = length(avg_embedding), photos_used = length(embeddings)))
}

# Run when called from command line
result <- tryCatch(
  enroll_student(student_id),
  error = function(e) {
    log_message(paste("ENROLLMENT FAILED:", e$message), "ERROR")
    quit(status = 1)
  }
)
cat(jsonlite::toJSON(result, auto_unbox = TRUE), "\n")