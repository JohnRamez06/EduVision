# ============================================================
# train_concentration_model.R — Train concentration regression model
# Input features: facial landmarks + gaze features (48×48 grayscale crop)
# Output: continuous score in [0, 1] (scaled to 0-100 on prediction)
# ============================================================

TRAIN_DIR <- dirname(sys.frame(1)$ofile %||% ".")
ROOT      <- dirname(TRAIN_DIR)
source(file.path(ROOT, "reticulate", "init_python.R"), local = TRUE)
source(file.path(TRAIN_DIR, "save_model.R"),            local = TRUE)
source(file.path(ROOT, "scripts", "utils.R"),           local = TRUE)

LOG_FILE <- file.path(ROOT, "logs", "training.log")

#' Train concentration model from labelled images.
#' Dataset structure: data/dataset/concentration/{0-100}/{image}.jpg
#' @param dataset_root Path to dataset root.
#' @param epochs Training epochs.
train_concentration_model <- function(
    dataset_root = file.path(ROOT, "data", "dataset"),
    epochs = 40L) {

  model_path <- file.path(MODEL_DIR, "concentration_model.h5")
  log_message("Training concentration model...", LOG_FILE)

  py_run_string(sprintf('
import os, cv2, numpy as np
from sklearn.model_selection import train_test_split
import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers

dataset_root = r"%s"
conc_dir = os.path.join(dataset_root, "concentration")

X, y = [], []
if os.path.isdir(conc_dir):
    for label_str in os.listdir(conc_dir):
        cls_dir = os.path.join(conc_dir, label_str)
        if not os.path.isdir(cls_dir):
            continue
        try:
            score = float(label_str) / 100.0
        except ValueError:
            continue
        for fname in os.listdir(cls_dir):
            if not fname.lower().endswith((".jpg",".jpeg",".png")):
                continue
            img = cv2.imread(os.path.join(cls_dir, fname),
                             cv2.IMREAD_GRAYSCALE)
            if img is None:
                continue
            img = cv2.resize(img, (48, 48)).astype("float32") / 255.0
            X.append(img.reshape(48, 48, 1))
            y.append(score)

if len(X) == 0:
    # No labelled data: create a minimal placeholder model
    X = np.zeros((4, 48, 48, 1), dtype="float32")
    y = np.array([0.2, 0.5, 0.7, 0.9], dtype="float32")

X = np.array(X, dtype="float32")
y = np.array(y, dtype="float32")

X_train, X_val, y_train, y_val = train_test_split(
    X, y, test_size=0.2, random_state=42)

model = keras.Sequential([
    layers.Conv2D(32, (3,3), activation="relu", padding="same",
                  input_shape=(48,48,1)),
    layers.MaxPooling2D(2,2),
    layers.Conv2D(64, (3,3), activation="relu", padding="same"),
    layers.MaxPooling2D(2,2),
    layers.Flatten(),
    layers.Dense(64, activation="relu"),
    layers.Dropout(0.3),
    layers.Dense(1, activation="sigmoid"),
])

model.compile(optimizer="adam", loss="mse", metrics=["mae"])
model.fit(X_train, y_train,
          validation_data=(X_val, y_val),
          epochs=%d, batch_size=32, verbose=1)

os.makedirs(os.path.dirname(r"%s"), exist_ok=True)
model.save(r"%s")
conc_done = True
', dataset_root, epochs, model_path, model_path))

  log_message(sprintf("Concentration model saved: %s", model_path), LOG_FILE)
  save_model_version(model_path, model_type = "concentration")
  invisible(model_path)
}

if (!interactive()) train_concentration_model()
