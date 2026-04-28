# ============================================================
# train_emotion_model.R — Train CNN emotion classifier
# Architecture: Conv2D(32)→MaxPool→Conv2D(64)→MaxPool→Conv2D(128)→MaxPool
#               →Dense(128)→Dropout(0.5)→Dense(7,softmax)
# ============================================================

TRAIN_DIR <- dirname(sys.frame(1)$ofile %||% ".")
ROOT      <- dirname(TRAIN_DIR)
source(file.path(ROOT, "reticulate", "init_python.R"), local = TRUE)
source(file.path(TRAIN_DIR, "prepare_dataset.R"),      local = TRUE)
source(file.path(TRAIN_DIR, "save_model.R"),            local = TRUE)
source(file.path(ROOT, "scripts", "utils.R"),           local = TRUE)

LOG_FILE <- file.path(ROOT, "logs", "training.log")

#' Train the emotion CNN model.
#' @param epochs Integer number of training epochs.
#' @param batch_size Mini-batch size.
#' @param dataset_root Path to dataset root.
#' @return Invisible path to saved model.
train_emotion_model <- function(epochs = 50L, batch_size = 64L,
                                dataset_root = file.path(ROOT, "data", "dataset")) {
  log_message("Preparing dataset...", LOG_FILE)
  ds <- prepare_dataset(dataset_root)
  log_message(sprintf("Dataset ready: %d train, %d val", ds$n_train, ds$n_val),
              LOG_FILE)

  model_path <- file.path(MODEL_DIR, "emotion_model.h5")

  py_run_string(sprintf('
import numpy as np
import tensorflow as tf
from tensorflow import keras
from tensorflow.keras import layers
import os

X_train = np.array(r_X_train, dtype="float32").reshape(-1, 48, 48, 1)
X_val   = np.array(r_X_val,   dtype="float32").reshape(-1, 48, 48, 1)
y_train = np.array(r_y_train, dtype="int32")
y_val   = np.array(r_y_val,   dtype="int32")

model = keras.Sequential([
    layers.Conv2D(32,  (3,3), activation="relu", padding="same",
                  input_shape=(48, 48, 1)),
    layers.BatchNormalization(),
    layers.MaxPooling2D(2, 2),

    layers.Conv2D(64,  (3,3), activation="relu", padding="same"),
    layers.BatchNormalization(),
    layers.MaxPooling2D(2, 2),

    layers.Conv2D(128, (3,3), activation="relu", padding="same"),
    layers.BatchNormalization(),
    layers.MaxPooling2D(2, 2),

    layers.Flatten(),
    layers.Dense(128, activation="relu"),
    layers.Dropout(0.5),
    layers.Dense(7,   activation="softmax"),
])

model.compile(optimizer="adam",
              loss="sparse_categorical_crossentropy",
              metrics=["accuracy"])

callbacks = [
    keras.callbacks.EarlyStopping(patience=8, restore_best_weights=True),
    keras.callbacks.ReduceLROnPlateau(patience=4, factor=0.5, min_lr=1e-6),
]

history = model.fit(
    X_train, y_train,
    validation_data=(X_val, y_val),
    epochs=%d, batch_size=%d,
    callbacks=callbacks, verbose=1
)

val_acc = float(history.history["val_accuracy"][-1])
os.makedirs(os.path.dirname(r"%s"), exist_ok=True)
model.save(r"%s")
train_done = True
', epochs, batch_size, model_path, model_path), convert = FALSE,
    envir = list(r_X_train = ds$X_train, r_X_val = ds$X_val,
                 r_y_train = ds$y_train, r_y_val = ds$y_val))

  log_message(sprintf("Emotion model saved: %s", model_path), LOG_FILE)

  save_model_version(model_path, model_type = "emotion",
                     accuracy = as.numeric(py$val_acc))
  invisible(model_path)
}

# Run when called directly
if (!interactive()) train_emotion_model()
