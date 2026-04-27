# ============================================================
# extract_embeddings.R — Extract 128-d embeddings from enrollment photos
# ============================================================

FL_DIR <- dirname(sys.frame(1)$ofile %||% ".")
source(file.path(dirname(FL_DIR), "reticulate", "init_python.R"), local = TRUE)
source(file.path(dirname(FL_DIR), "reticulate", "face_detection.R"), local = TRUE)
source(file.path(dirname(FL_DIR), "reticulate", "embedding_extraction.R"), local = TRUE)

#' Extract embeddings for all valid photos of a student.
#' @param student_id Character.
#' @param photos_dir Directory containing photos.  Defaults to
#'                   face_enrollment/{student_id}/ relative to working dir.
#' @return List of numeric vectors (each 128-d).  Invalid photos are skipped.
extract_student_embeddings <- function(student_id,
                                       photos_dir = file.path("face_enrollment",
                                                              student_id)) {
  img_files <- list.files(photos_dir,
                          pattern = "\\.(jpg|jpeg|png|bmp)$",
                          ignore.case = TRUE,
                          full.names = TRUE)

  embeddings <- lapply(img_files, function(f) {
    tryCatch({
      faces <- detect_faces(f)
      if (nrow(faces) == 0) {
        message("  No face in: ", basename(f), " — skipping.")
        return(NULL)
      }
      emb <- extract_embedding(f)
      if (is.null(emb)) {
        message("  Embedding failed for: ", basename(f))
        return(NULL)
      }
      emb
    }, error = function(e) {
      message("  Error processing ", basename(f), ": ", e$message)
      NULL
    })
  })

  # Remove NULLs
  Filter(Negate(is.null), embeddings)
}

#' Average a list of embedding vectors into one representative vector.
average_embeddings <- function(emb_list) {
  if (length(emb_list) == 0) stop("No embeddings to average.")
  mat <- do.call(rbind, emb_list)
  avg <- colMeans(mat)
  # L2-normalise
  avg / sqrt(sum(avg^2) + 1e-9)
}
