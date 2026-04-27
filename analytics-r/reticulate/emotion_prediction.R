# ============================================================
# emotion_prediction.R — Predict emotion from a face image array
# Returns list(emotion, confidence) where emotion is a character label.
# ============================================================

source(file.path(dirname(sys.frame(1)$ofile %||% "."), "init_python.R"),
       local = TRUE)

EMOTION_LABELS <- c("angry", "disgusted", "fearful",
                    "happy", "neutral", "sad", "surprised")

#' Predict emotion for a face image.
#' @param face_image_path Path to a cropped face image file (any format readable by OpenCV).
#' @param model_path Path to the emotion Keras model (.h5).  Defaults to models/emotion_model.h5.
#' @return Named list: emotion (character), confidence (numeric 0-1),
#'         all_scores (named numeric vector).
predict_emotion <- function(face_image_path,
                            model_path = file.path(
                              Sys.getenv("MODEL_DIR", "analytics-r/models"),
                              "emotion_model.h5")) {
  stopifnot(file.exists(face_image_path))

  py_run_string(sprintf('
import cv2, numpy as np
from tensorflow import keras

model_path      = r"%s"
face_image_path = r"%s"

img = cv2.imread(face_image_path, cv2.IMREAD_GRAYSCALE)
img = cv2.resize(img, (48, 48))
img = img.astype("float32") / 255.0
img = img.reshape(1, 48, 48, 1)

model       = keras.models.load_model(model_path, compile=False)
preds       = model.predict(img, verbose=0)[0]
label_idx   = int(np.argmax(preds))
confidence  = float(preds[label_idx])
all_scores  = preds.tolist()
predicted_label = ["angry","disgusted","fearful","happy","neutral","sad","surprised"][label_idx]
', convert = TRUE))

  list(
    emotion    = py$predicted_label,
    confidence = py$confidence,
    all_scores = setNames(as.numeric(py$all_scores), EMOTION_LABELS)
  )
}
