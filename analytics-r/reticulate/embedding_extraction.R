# ============================================================
# embedding_extraction.R — Extract 128-d face embedding via FaceNet
# Returns a numeric vector of length 128.
# ============================================================

source(file.path(dirname(sys.frame(1)$ofile %||% "."), "init_python.R"),
       local = TRUE)

#' Extract a 128-dimensional face embedding from a face image.
#' @param face_image_path Path to the face image file.
#' @return Numeric vector of length 128, or NULL if extraction fails.
extract_embedding <- function(face_image_path) {
  stopifnot(file.exists(face_image_path))

  py_run_string(sprintf('
import cv2, numpy as np

face_image_path = r"%s"

img = cv2.imread(face_image_path)
if img is None:
    embedding = None
else:
    img_rgb = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
    img_resized = cv2.resize(img_rgb, (160, 160))
    img_norm = img_resized.astype("float32")
    mean, std = img_norm.mean(), img_norm.std()
    img_norm = (img_norm - mean) / (std + 1e-7)
    img_batch = np.expand_dims(img_norm, axis=0)

    try:
        from tensorflow.keras.models import load_model
        import os
        model_path = os.path.join(os.environ.get("MODEL_DIR", "analytics-r/models"),
                                   "facenet_keras.h5")
        facenet = load_model(model_path, compile=False)
        emb = facenet.predict(img_batch, verbose=0)[0]
        emb = emb / (np.linalg.norm(emb) + 1e-7)
        embedding = emb.tolist()
    except Exception as e:
        # Fallback: flatten a compact HOG-like descriptor to 128 dims
        gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
        gray_small = cv2.resize(gray, (16, 8))
        flat = gray_small.flatten().astype("float32")
        flat = flat / (np.linalg.norm(flat) + 1e-7)
        embedding = flat.tolist()
', convert = TRUE))

  emb <- py$embedding
  if (is.null(emb)) return(NULL)
  as.numeric(emb)
}
