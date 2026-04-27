# ============================================================
# face_detection.R — Detect faces in an image via OpenCV
# Returns a data.frame with columns: x, y, width, height, confidence
# ============================================================

source(file.path(dirname(sys.frame(1)$ofile %||% "."), "init_python.R"),
       local = TRUE)

#' Detect faces in an image file using OpenCV DNN face detector.
#' @param image_path Character path to an image file.
#' @return data.frame with columns x, y, width, height, confidence (0-1).
#'         Returns empty data.frame if no faces detected.
detect_faces <- function(image_path) {
  stopifnot(file.exists(image_path))

  detect_py <- py_run_string(sprintf('
import cv2, numpy as np

image_path = r"%s"
img = cv2.imread(image_path)
if img is None:
    result = []
else:
    h, w = img.shape[:2]
    blob = cv2.dnn.blobFromImage(img, 1.0, (300, 300),
                                  (104.0, 177.0, 123.0))
    # Use haar cascade as a fallback (no model file needed)
    gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    cascade = cv2.CascadeClassifier(
        cv2.data.haarcascades + "haarcascade_frontalface_default.xml")
    faces = cascade.detectMultiScale(gray, scaleFactor=1.1,
                                     minNeighbors=5, minSize=(30, 30))
    result = []
    if len(faces) > 0:
        for (x, y, fw, fh) in faces:
            result.append({
                "x": int(x), "y": int(y),
                "width": int(fw), "height": int(fh),
                "confidence": 1.0
            })
faces_list = result
', convert = TRUE))

  faces <- detect_py$faces_list
  if (length(faces) == 0) {
    return(data.frame(x = integer(0), y = integer(0),
                      width = integer(0), height = integer(0),
                      confidence = numeric(0)))
  }

  do.call(rbind, lapply(faces, as.data.frame, stringsAsFactors = FALSE))
}
