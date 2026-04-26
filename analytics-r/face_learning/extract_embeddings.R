# =============================================================================
# EduVision - Extract Face Embeddings from Enrollment Photos
# face_learning/extract_embeddings.R
# =============================================================================

BASE_DIR <- normalizePath(file.path(dirname(sys.frame(1)$ofile), ".."))
source(file.path(BASE_DIR, "config.R"))
source(file.path(BASE_DIR, "reticulate", "init_python.R"))
source(file.path(BASE_DIR, "reticulate", "embedding_extraction.R"))
source(file.path(BASE_DIR, "reticulate", "face_detection.R"))

# -----------------------------------------------------------------------------
# extract_student_embeddings(student_id)
# Reads all valid photos from face_enrollment/{student_id}/
# Returns: list of 128-d numeric vectors (one per valid photo)
# -----------------------------------------------------------------------------
extract_student_embeddings <- function(student_id) {
  photo_dir <- file.path(FACE_DIR, student_id)
  if (!dir.exists(photo_dir)) stop("Photo directory not found: ", photo_dir)

  photos <- list.files(photo_dir, pattern = "\\.(jpg|jpeg|png|bmp)$",
                       full.names = TRUE, ignore.case = TRUE)
  if (length(photos) == 0) stop("No photos found for student: ", student_id)

  log_message(sprintf("Extracting embeddings for student %s from %d photos", student_id, length(photos)))

  embeddings <- list()
  failed     <- 0

  for (photo_path in photos) {
    emb <- tryCatch(
      extract_embedding(photo_path),
      error = function(e) { warning("Embedding failed for ", basename(photo_path), ": ", e$message); NULL }
    )
    if (!is.null(emb) && length(emb) == 128) {
      embeddings[[length(embeddings) + 1]] <- emb
    } else {
      failed <- failed + 1
    }
  }

  log_message(sprintf("Extracted %d valid embeddings, %d failed", length(embeddings), failed))

  if (length(embeddings) == 0) stop("No valid embeddings extracted for student: ", student_id)
  embeddings
}