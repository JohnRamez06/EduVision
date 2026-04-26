# =============================================================================
# EduVision - Verify Enrollment Photos Quality
# face_learning/verify_enrollment.R
# =============================================================================

BASE_DIR <- normalizePath(file.path(dirname(sys.frame(1)$ofile), ".."))
source(file.path(BASE_DIR, "config.R"))
source(file.path(BASE_DIR, "reticulate", "init_python.R"))
source(file.path(BASE_DIR, "reticulate", "face_detection.R"))

.verify_py <- NULL
.get_verify_py <- function() {
  if (is.null(.verify_py)) {
    .verify_py <<- py_run_string("
import cv2
import numpy as np

def check_image_quality(image_path):
    img = cv2.imread(image_path)
    if img is None:
        return {'valid': False, 'issue': 'Cannot read image file'}
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    # Blur check (Laplacian variance)
    lap_var = float(cv2.Laplacian(gray, cv2.CV_64F).var())
    if lap_var < 50:
        return {'valid': False, 'issue': f'Image too blurry (score={lap_var:.1f})'}
    # Size check
    h, w = img.shape[:2]
    if h < 100 or w < 100:
        return {'valid': False, 'issue': f'Image too small ({w}x{h})'}
    return {'valid': True, 'issue': '', 'blur_score': lap_var}
")
  }
  .verify_py
}

# -----------------------------------------------------------------------------
# verify_enrollment(student_id)
# Checks all photos in face_enrollment/{student_id}/
# Returns: list of list(file, valid, issues)
# -----------------------------------------------------------------------------
verify_enrollment <- function(student_id) {
  photo_dir <- file.path(FACE_DIR, student_id)
  if (!dir.exists(photo_dir)) {
    return(list(list(file = NA, valid = FALSE, issues = "Photo directory does not exist")))
  }

  photos <- list.files(photo_dir, pattern = "\\.(jpg|jpeg|png|bmp)$",
                       full.names = TRUE, ignore.case = TRUE)
  if (length(photos) == 0) {
    return(list(list(file = NA, valid = FALSE, issues = "No photos found")))
  }

  .get_verify_py()

  results <- lapply(photos, function(photo_path) {
    fname  <- basename(photo_path)
    issues <- character(0)

    # Quality check via Python
    quality <- tryCatch(.verify_py$check_image_quality(photo_path), error = function(e) list(valid = FALSE, issue = e$message))
    if (!isTRUE(quality$valid)) {
      issues <- c(issues, quality$issue)
    }

    # Face detection check
    faces <- tryCatch(detect_faces(photo_path), error = function(e) NULL)
    if (is.null(faces) || nrow(faces) == 0) {
      issues <- c(issues, "No face detected")
    } else if (nrow(faces) > 1) {
      issues <- c(issues, paste0("Multiple faces detected (", nrow(faces), ")"))
    }

    list(
      file   = fname,
      valid  = length(issues) == 0,
      issues = if (length(issues) == 0) "OK" else paste(issues, collapse = "; ")
    )
  })

  valid_count <- sum(sapply(results, `[[`, "valid"))
  log_message(sprintf("verify_enrollment(%s): %d/%d photos valid", student_id, valid_count, length(photos)))
  results
}

# -----------------------------------------------------------------------------
# is_enrollment_ready(student_id, min_valid = 3)
# Returns TRUE if enough valid photos exist
# -----------------------------------------------------------------------------
is_enrollment_ready <- function(student_id, min_valid = 3) {
  results <- verify_enrollment(student_id)
  sum(sapply(results, `[[`, "valid")) >= min_valid
}