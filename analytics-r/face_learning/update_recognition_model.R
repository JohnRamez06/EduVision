# ============================================================
# update_recognition_model.R — Incremental SVM face recogniser update
# ============================================================

FL_DIR <- dirname(sys.frame(1)$ofile %||% ".")
source(file.path(dirname(FL_DIR), "config.R"), local = TRUE)
source(file.path(dirname(FL_DIR), "reticulate", "init_python.R"), local = TRUE)

#' Train / retrain a LinearSVC face recogniser from all enrolled embeddings
#' and save as models/face_recognizer.pkl.
#' @param full_retrain Logical.  If FALSE, only update if new faces added.
update_recognition_model <- function(full_retrain = FALSE) {
  # Fetch all students with a face_encoding
  data <- with_connection(function(con) {
    dbGetQuery(con,
      "SELECT user_id, face_encoding FROM students WHERE face_encoding IS NOT NULL")
  })

  if (nrow(data) < 2) {
    message("Not enough enrolled students for training (need >= 2).")
    return(invisible(FALSE))
  }

  model_path <- file.path(MODEL_DIR, "face_recognizer.pkl")

  # Serialise the embeddings table to a temp JSON for Python
  tmp <- tempfile(fileext = ".json")
  jsonlite::write_json(data, tmp, auto_unbox = TRUE)

  py_run_string(sprintf('
import json, numpy as np, pickle, os
from sklearn.svm import LinearSVC
from sklearn.preprocessing import LabelEncoder

data_path  = r"%s"
model_path = r"%s"

with open(data_path) as f:
    rows = json.load(f)

X, y = [], []
for row in rows:
    emb = json.loads(row["face_encoding"])
    X.append(emb)
    y.append(row["user_id"])

X = np.array(X)
le = LabelEncoder()
y_enc = le.fit_transform(y)

clf = LinearSVC(C=1.0, max_iter=5000)
clf.fit(X, y_enc)

os.makedirs(os.path.dirname(model_path), exist_ok=True)
with open(model_path, "wb") as f:
    pickle.dump({"classifier": clf, "label_encoder": le}, f)

trained_ok = True
total_faces = int(len(y))
', convert = TRUE), envir = py)

  file.remove(tmp)

  if (!isTRUE(py$trained_ok)) {
    warning("Python training step did not complete successfully.")
    return(invisible(FALSE))
  }

  total <- as.integer(py$total_faces)

  # Update model_versions table
  with_connection(function(con) {
    dbExecute(con,
      sqlInterpolate(con,
        "UPDATE model_versions
            SET total_faces = ?n, last_trained_at = NOW()
          WHERE is_active = 1 AND model_type = 'face_recognition'",
        n = total))
  })

  message(sprintf("Face recogniser updated: %d students, saved to %s",
                  total, model_path))
  invisible(TRUE)
}
