# analytics-r/reticulate/face_analysis.R
# ============================================================
# face_analysis.R — Face detection + optional DeepFace embeddings
#
# Usage:
#   source("reticulate/face_analysis.R")
#   res <- recognize_face("path/to/image.jpg")
# ============================================================

# %||% helper for base R
`%||%` <- function(a, b) if (!is.null(a) && length(a) > 0 && !is.na(a)) a else b

# Source Python initialization (forces the venv + imports)
source(file.path(dirname(sys.frame(1)$ofile %||% "."), "init_python.R"), local = TRUE)

#' recognize_face()
#' 1) Reads image with OpenCV
#' 2) Detects faces with Haar Cascade
#' 3) Returns list(success, face_count, embeddings if DeepFace available)
#'
#' @param image_path Path to an image file.
#' @return list(success, face_count, embeddings, deepface_available, error?)
recognize_face <- function(image_path) {
  if (!file.exists(image_path)) {
    return(list(success = FALSE, face_count = 0L, embeddings = list(),
                deepface_available = FALSE, error = "Image not found"))
  }

  if (!exists("cv2", inherits = TRUE)) {
    return(list(success = FALSE, face_count = 0L, embeddings = list(),
                deepface_available = FALSE, error = "cv2 not initialized (did init_python.R run?)"))
  }

  # 1) Read image using OpenCV
  img <- tryCatch(
    cv2$imread(normalizePath(image_path, winslash = "/")),
    error = function(e) NULL
  )
  if (is.null(img) || reticulate::py_is_null_xptr(img)) {
    return(list(success = FALSE, face_count = 0L, embeddings = list(),
                deepface_available = FALSE, error = "OpenCV could not read image"))
  }

  gray <- cv2$cvtColor(img, cv2$COLOR_BGR2GRAY)

  # 2) Haar cascade path (try OpenCV’s built-in data path first)
  cascade_path <- tryCatch({
    p <- cv2$data$haarcascades
    if (!is.null(p)) file.path(p, "haarcascade_frontalface_default.xml") else NULL
  }, error = function(e) NULL)

  if (is.null(cascade_path) || !file.exists(cascade_path)) {
    return(list(
      success = FALSE, face_count = 0L, embeddings = list(),
      deepface_available = exists("deepface", inherits = TRUE),
      error = paste0(
        "Could not locate haarcascade_frontalface_default.xml. ",
        "OpenCV's data path (cv2$data$haarcascades) was not available. ",
        "Install opencv-python properly in the venv."
      )
    ))
  }

  face_cascade <- cv2$CascadeClassifier(cascade_path)
  faces <- face_cascade$detectMultiScale(gray, scaleFactor = 1.1, minNeighbors = 5L)

  face_count <- 0L
  if (!is.null(faces)) {
    face_count <- as.integer(dim(faces)[1] %||% 0L)
  }

  # 3) Optional DeepFace embeddings
  deepface_available <- exists("deepface", inherits = TRUE)
  embeddings <- list()

  if (deepface_available && face_count > 0L) {
    embeddings <- tryCatch({
      res <- deepface$DeepFace$represent(
        img_path = normalizePath(image_path, winslash = "/"),
        model_name = "Facenet",
        enforce_detection = FALSE
      )

      rr <- reticulate::py_to_r(res)

      # DeepFace often returns list-of-dicts, each with $embedding
      if (is.list(rr)) {
        out <- lapply(rr, function(x) {
          if (is.list(x) && !is.null(x$embedding)) as.numeric(x$embedding) else NULL
        })
        Filter(Negate(is.null), out)
      } else {
        list()
      }
    }, error = function(e) {
      # DeepFace is optional; don't fail the whole function
      list()
    })
  }

  list(
    success = TRUE,
    face_count = face_count,
    embeddings = embeddings,
    deepface_available = deepface_available
  )
}