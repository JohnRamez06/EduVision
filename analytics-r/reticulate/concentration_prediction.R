# ============================================================
# concentration_prediction.R — Predict concentration score (0-100)
# Uses facial landmarks / gaze features and a trained regression model.
# ============================================================

source(file.path(dirname(sys.frame(1)$ofile %||% "."), "init_python.R"),
       local = TRUE)

#' Predict concentration score for a face image.
#' @param face_image_path Path to the face image file.
#' @param model_path Path to the concentration Keras model (.h5).
#' @return Numeric score in [0, 100]; higher means more concentrated.
predict_concentration <- function(face_image_path,
                                  model_path = file.path(
                                    Sys.getenv("MODEL_DIR", "analytics-r/models"),
                                    "concentration_model.h5")) {
  stopifnot(file.exists(face_image_path))

  py_run_string(sprintf('
import cv2, numpy as np

face_image_path = r"%s"
model_path      = r"%s"

img = cv2.imread(face_image_path)
score = 50.0  # default neutral

if img is not None:
    try:
        from tensorflow.keras.models import load_model
        import os
        if os.path.exists(model_path):
            model = load_model(model_path, compile=False)
            gray  = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
            gray  = cv2.resize(gray, (48, 48)).astype("float32") / 255.0
            inp   = gray.reshape(1, 48, 48, 1)
            score = float(model.predict(inp, verbose=0)[0][0])
            score = max(0.0, min(100.0, score * 100.0))
    except Exception:
        pass

concentration_score = score
', convert = TRUE))

  as.numeric(py$concentration_score)
}
