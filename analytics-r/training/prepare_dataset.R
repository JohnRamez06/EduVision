# ============================================================
# prepare_dataset.R — Load and preprocess emotion training images
# ============================================================

TRAIN_DIR <- dirname(sys.frame(1)$ofile %||% ".")
ROOT      <- dirname(TRAIN_DIR)
source(file.path(ROOT, "reticulate", "init_python.R"), local = TRUE)

EMOTION_CLASSES <- c("angry", "disgusted", "fearful",
                     "happy", "neutral", "sad", "surprised")

#' Load, resize, normalise images and split into train/val.
#' @param dataset_root Path to dataset root directory.
#' @param img_size Integer target image size (img_size x img_size).
#' @param val_split Fraction for validation set.
#' @param seed Random seed.
#' @return Named list: X_train, X_val, y_train, y_val (all R arrays/vectors).
prepare_dataset <- function(dataset_root = file.path(ROOT, "data", "dataset"),
                            img_size = 48L,
                            val_split = 0.2,
                            seed = 42L) {

  tmp_path <- tempfile(fileext = ".json")
  jsonlite::write_json(list(dataset_root = dataset_root,
                            img_size     = img_size,
                            val_split    = val_split,
                            seed         = seed,
                            classes      = EMOTION_CLASSES), tmp_path)

  py_run_string(sprintf('
import json, os, cv2, numpy as np
from sklearn.model_selection import train_test_split

with open(r"%s") as f:
    cfg = json.load(f)

dataset_root = cfg["dataset_root"]
img_size     = int(cfg["img_size"])
val_split    = float(cfg["val_split"])
seed         = int(cfg["seed"])
classes      = cfg["classes"]

X, y = [], []
train_dir = os.path.join(dataset_root, "train")

for label_idx, cls in enumerate(classes):
    cls_dir = os.path.join(train_dir, cls)
    if not os.path.isdir(cls_dir):
        continue
    for fname in os.listdir(cls_dir):
        if not fname.lower().endswith((".jpg",".jpeg",".png",".bmp")):
            continue
        img = cv2.imread(os.path.join(cls_dir, fname), cv2.IMREAD_GRAYSCALE)
        if img is None:
            continue
        img = cv2.resize(img, (img_size, img_size))
        X.append(img.flatten().astype("float32") / 255.0)
        y.append(label_idx)

X = np.array(X)
y = np.array(y)

X_train, X_val, y_train, y_val = train_test_split(
    X, y, test_size=val_split, random_state=seed, stratify=y)

# Reshape for CNN input (N, H, W, 1)
X_train = X_train.reshape(-1, img_size, img_size, 1)
X_val   = X_val.reshape(-1, img_size, img_size, 1)

dataset_ok = True
n_train = int(len(X_train))
n_val   = int(len(X_val))
', convert = TRUE))

  file.remove(tmp_path)

  list(
    X_train = py$X_train,
    X_val   = py$X_val,
    y_train = py$y_train,
    y_val   = py$y_val,
    n_train = as.integer(py$n_train),
    n_val   = as.integer(py$n_val)
  )
}
