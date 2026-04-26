# =============================================================================
# EduVision - Full Model Retraining from Scratch (weekly scheduled job)
# face_learning/retrain_full_model.R
#
# Usage: Rscript retrain_full_model.R
# =============================================================================

BASE_DIR <- normalizePath(file.path(dirname(sys.frame(1)$ofile), ".."))
source(file.path(BASE_DIR, "config.R"))
source(file.path(BASE_DIR, "reticulate", "init_python.R"))
source(file.path(BASE_DIR, "reticulate", "embedding_extraction.R"))
source(file.path(BASE_DIR, "face_learning", "update_recognition_model.R"))

retrain_full_model <- function() {
  log_message("=== Starting full model retraining ===")

  # Archive existing model
  model_path <- file.path(MODEL_DIR, "face_recognizer.pkl")
  if (file.exists(model_path)) {
    archive_path <- file.path(MODEL_DIR, paste0("face_recognizer_", format(Sys.time(), "%Y%m%d_%H%M%S"), ".pkl"))
    file.copy(model_path, archive_path)
    log_message(paste("Archived old model to:", basename(archive_path)))
  }

  # Count available embeddings
  count_df <- query_df("SELECT COUNT(*) as n FROM students WHERE face_encoding IS NOT NULL AND face_encoding != ''")
  n_students <- count_df$n[1]
  log_message(sprintf("Training on %d students with embeddings", n_students))

  if (n_students < 2) {
    log_message("Not enough students to train — need at least 2", "WARN")
    return(invisible(FALSE))
  }

  # Delegate to update_recognition_model (which already does full retraining)
  success <- update_recognition_model()

  # Record new version in model_versions
  version_num <- format(Sys.time(), "%Y%m%d%H%M")
  tryCatch(execute_sql(sprintf(
    "INSERT INTO model_versions (version, model_type, total_faces, is_active, created_at, last_trained_at)
     VALUES ('%s', 'face_recognition', %d, 0, NOW(), NOW())
     ON DUPLICATE KEY UPDATE total_faces = %d, last_trained_at = NOW()",
    version_num, n_students, n_students
  )), error = function(e) log_message(paste("model_versions insert failed:", e$message), "WARN"))

  log_message("=== Full retraining complete ===")
  invisible(success)
}

result <- tryCatch(retrain_full_model(), error = function(e) {
  log_message(paste("RETRAINING FAILED:", e$message), "ERROR")
  quit(status = 1)
})
cat(jsonlite::toJSON(list(success = isTRUE(result)), auto_unbox = TRUE), "\n")