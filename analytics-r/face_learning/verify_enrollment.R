# ============================================================
# verify_enrollment.R — Validate face photos before enrollment
# ============================================================

FL_DIR <- dirname(sys.frame(1)$ofile %||% ".")
source(file.path(dirname(FL_DIR), "reticulate", "init_python.R"), local = TRUE)
source(file.path(dirname(FL_DIR), "reticulate", "face_detection.R"), local = TRUE)

#' Check photo quality (blurriness via Laplacian variance).
#' Returns TRUE if variance above threshold (not blurry).
is_sharp_enough <- function(image_path, threshold = 100) {
  py_run_string(sprintf('
import cv2
img = cv2.imread(r"%s", cv2.IMREAD_GRAYSCALE)
if img is None:
    sharpness = 0.0
else:
    sharpness = float(cv2.Laplacian(img, cv2.CV_64F).var())
', image_path), convert = TRUE)
  as.numeric(py$sharpness) >= threshold
}

#' Verify all photos for a student.
#' @param student_id Character student ID.
#' @param photos_dir Directory containing the student's photos.
#' @return Named list: valid (logical), results (data.frame with columns
#'         file, has_face, is_sharp, issues).
verify_enrollment <- function(student_id,
                              photos_dir = file.path("face_enrollment",
                                                     student_id)) {
  if (!dir.exists(photos_dir)) {
    return(list(valid = FALSE,
                results = data.frame(),
                issues  = sprintf("Directory not found: %s", photos_dir)))
  }

  img_files <- list.files(photos_dir,
                          pattern = "\\.(jpg|jpeg|png|bmp)$",
                          ignore.case = TRUE,
                          full.names = TRUE)

  if (length(img_files) == 0) {
    return(list(valid = FALSE,
                results = data.frame(),
                issues  = "No image files found."))
  }

  results <- lapply(img_files, function(f) {
    faces    <- tryCatch(detect_faces(f), error = function(e) data.frame())
    has_face <- nrow(faces) > 0
    sharp    <- tryCatch(is_sharp_enough(f), error = function(e) FALSE)
    issues   <- character(0)
    if (!has_face) issues <- c(issues, "no face detected")
    if (!sharp)    issues <- c(issues, "image too blurry")
    data.frame(file     = basename(f),
               has_face = has_face,
               is_sharp = sharp,
               issues   = paste(issues, collapse = "; "),
               stringsAsFactors = FALSE)
  })
  results_df <- do.call(rbind, results)
  all_valid  <- all(results_df$has_face & results_df$is_sharp)

  list(valid   = all_valid,
       results = results_df,
       issues  = if (all_valid) "" else
                   paste(results_df$issues[nchar(results_df$issues) > 0],
                         collapse = " | "))
}
