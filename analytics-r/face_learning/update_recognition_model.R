# =============================================================================
# EduVision - Update Face Recognition SVM Model (Incremental)
# face_learning/update_recognition_model.R
# =============================================================================

BASE_DIR <- normalizePath(file.path(dirname(sys.frame(1)$ofile), ".."))
source(file.path(BASE_DIR, "config.R"))
source(file.path(BASE_DIR, "reticulate", "init_python.R"))
source(file.path(BASE_DIR, "reticulate", "embedding_extraction.R"))

.svm_py <- NULL
.get_svm_py <- function() {
  if (is.null(.svm_py)) {
    .svm_py <<- py_run_string("
import pickle, os, numpy as np
from sklearn.svm import SVC
from sklearn.preprocessing import LabelEncoder

def train_svm(embeddings_list, labels_list, model_path):
    X = np.array(embeddings_list, dtype=np.float32)
    y = np.array(labels_list)
    le = LabelEncoder()
    y_enc = le.fit_transform(y)
    clf = SVC(kernel='rbf', probability=True, C=1.0)
    clf.fit(X, y_enc)
    os.makedirs(os.path.dirname(model_path), exist_ok=True)
    with open(model_path, 'wb') as f:
        pickle.dump({'model': clf, 'label_encoder': le}, f)
    return {'n_classes': len(le.classes_), 'n_samples': len(X)}

def predict_svm(embedding, model_path):
    if not os.path.exists(model_path):
        return None
    with open(model_path, 'rb') as f:
        bundle = pickle.load(f)
    clf = bundle['model']
    le  = bundle['label_encoder']
    X = np.array(embedding, dtype=np.float32).reshape(1, -1)
    pred  = clf.predict(X)[0]
    proba = clf.predict_proba(X)[0]
    return {'student_id': le.inverse_transform([pred])[0], 'confidence': float(proba.max())}
")
  }
  .svm_py
}

# -----------------------------------------------------------------------------
# update_recognition_model()
# Queries all students with face_encoding IS NOT NULL,
# trains SVM, saves to models/face_recognizer.pkl,
# updates model_versions table
# -----------------------------------------------------------------------------
update_recognition_model <- function() {
  log_message("update_recognition_model: loading all face encodings from DB")

  students <- query_df(
    "SELECT s.user_id, s.face_encoding
     FROM students s
     WHERE s.face_encoding IS NOT NULL AND s.face_encoding != ''"
  )

  if (nrow(students) == 0) {
    log_message("No students with face encodings found — skipping model update", "WARN")
    return(invisible(FALSE))
  }

  embeddings_list <- lapply(students$face_encoding, embedding_from_json)
  valid_idx       <- which(!sapply(embeddings_list, is.null))
  embeddings_list <- embeddings_list[valid_idx]
  labels_list     <- students$user_id[valid_idx]

  if (length(embeddings_list) < 2) {
    log_message("Need at least 2 students with embeddings to train SVM", "WARN")
    return(invisible(FALSE))
  }

  model_path <- file.path(MODEL_DIR, "face_recognizer.pkl")
  .get_svm_py()

  result <- .svm_py$train_svm(
    embeddings_list,
    as.list(labels_list),
    model_path
  )

  log_message(sprintf("SVM trained: %d classes, %d samples", result$n_classes, result$n_samples))

  # Update model_versions table
  execute_sql(sprintf(
    "UPDATE model_versions SET total_faces = %d, last_trained_at = NOW() WHERE is_active = 1",
    as.integer(result$n_samples)
  ))

  invisible(TRUE)
}

# -----------------------------------------------------------------------------
# predict_student(embedding) -> list(student_id, confidence) or NULL
# -----------------------------------------------------------------------------
predict_student <- function(embedding) {
  model_path <- file.path(MODEL_DIR, "face_recognizer.pkl")
  if (!file.exists(model_path)) {
    warning("Face recognizer model not found: ", model_path)
    return(NULL)
  }
  .get_svm_py()
  result <- .svm_py$predict_svm(as.list(as.numeric(embedding)), model_path)
  if (is.null(result)) return(NULL)
  list(student_id = result$student_id, confidence = as.numeric(result$confidence))
}