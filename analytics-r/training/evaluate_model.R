# ============================================================
# evaluate_model.R — Evaluate a trained model on the test set
# Usage: Rscript evaluate_model.R <model_type>
#        model_type: "emotion" | "concentration"
# ============================================================

TRAIN_DIR <- dirname(sys.frame(1)$ofile %||% ".")
ROOT      <- dirname(TRAIN_DIR)
source(file.path(ROOT, "reticulate", "init_python.R"), local = TRUE)

EMOTION_CLASSES <- c("angry", "disgusted", "fearful",
                     "happy", "neutral", "sad", "surprised")

#' Evaluate emotion model on test set.  Returns list with metrics.
evaluate_emotion_model <- function(
    dataset_root = file.path(ROOT, "data", "dataset"),
    model_path   = file.path(MODEL_DIR, "emotion_model.h5")) {

  py_run_string(sprintf('
import cv2, numpy as np, json, os
from tensorflow import keras
from sklearn.metrics import (accuracy_score, precision_score,
                              recall_score, f1_score, confusion_matrix)

model_path   = r"%s"
dataset_root = r"%s"
classes = ["angry","disgusted","fearful","happy","neutral","sad","surprised"]

test_dir = os.path.join(dataset_root, "test")
X_test, y_test = [], []

for idx, cls in enumerate(classes):
    cls_dir = os.path.join(test_dir, cls)
    if not os.path.isdir(cls_dir): continue
    for fname in os.listdir(cls_dir):
        if not fname.lower().endswith((".jpg",".jpeg",".png")): continue
        img = cv2.imread(os.path.join(cls_dir, fname), cv2.IMREAD_GRAYSCALE)
        if img is None: continue
        img = cv2.resize(img, (48,48)).astype("float32") / 255.0
        X_test.append(img.reshape(48,48,1))
        y_test.append(idx)

X_test = np.array(X_test)
y_test = np.array(y_test)

model  = keras.models.load_model(model_path, compile=False)
preds  = model.predict(X_test, verbose=0)
y_pred = preds.argmax(axis=1)

accuracy  = float(accuracy_score(y_test, y_pred))
precision = float(precision_score(y_test, y_pred, average="weighted",
                                   zero_division=0))
recall    = float(recall_score(y_test, y_pred, average="weighted",
                                zero_division=0))
f1        = float(f1_score(y_test, y_pred, average="weighted",
                            zero_division=0))
cm        = confusion_matrix(y_test, y_pred).tolist()
', convert = TRUE))

  list(
    accuracy         = as.numeric(py$accuracy),
    precision        = as.numeric(py$precision),
    recall           = as.numeric(py$recall),
    f1               = as.numeric(py$f1),
    confusion_matrix = py$cm,
    classes          = EMOTION_CLASSES
  )
}

# Run when called directly
if (!interactive()) {
  args       <- commandArgs(trailingOnly = TRUE)
  model_type <- if (length(args) >= 1) args[1] else "emotion"
  metrics    <- evaluate_emotion_model()
  cat(sprintf("Accuracy:  %.4f\nPrecision: %.4f\nRecall:    %.4f\nF1:        %.4f\n",
              metrics$accuracy, metrics$precision,
              metrics$recall,   metrics$f1))
}
