# ============================================================
# retrain_full_model.R — Full face recogniser retraining from scratch
# Intended to be called by a weekly cron job.
# Usage: Rscript retrain_full_model.R
# ============================================================

FL_DIR <- dirname(sys.frame(1)$ofile %||% ".")
ROOT   <- dirname(FL_DIR)
source(file.path(ROOT, "config.R"),                     local = TRUE)
source(file.path(ROOT, "scripts", "utils.R"),           local = TRUE)
source(file.path(ROOT, "reticulate", "init_python.R"),  local = TRUE)

LOG_FILE <- file.path(ROOT, "logs", "enrollment.log")

log_message("=== Full face recogniser retraining started ===", LOG_FILE)

# Fetch all enrolled students
data <- with_connection(function(con) {
  dbGetQuery(con,
    "SELECT user_id, face_encoding FROM students WHERE face_encoding IS NOT NULL")
})

if (nrow(data) < 2) {
  log_message("Not enough enrolled students (need >= 2). Aborting.", LOG_FILE)
  quit(status = 1)
}

log_message(sprintf("Training on %d students...", nrow(data)), LOG_FILE)

model_path <- file.path(MODEL_DIR, "face_recognizer.pkl")
tmp <- tempfile(fileext = ".json")
jsonlite::write_json(data, tmp, auto_unbox = TRUE)

py_run_string(sprintf('
import json, numpy as np, pickle, os, shutil, datetime
from sklearn.svm import LinearSVC
from sklearn.preprocessing import LabelEncoder
from sklearn.model_selection import cross_val_score

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

clf = LinearSVC(C=1.0, max_iter=10000)
clf.fit(X, y_enc)

cv_scores = cross_val_score(clf, X, y_enc, cv=min(3, len(set(y_enc))))
cv_accuracy = float(cv_scores.mean())

# Backup old model
if os.path.exists(model_path):
    ts = datetime.datetime.now().strftime("%%Y%%m%%d_%%H%%M%%S")
    shutil.copy(model_path, model_path + "." + ts + ".bak")

os.makedirs(os.path.dirname(model_path), exist_ok=True)
with open(model_path, "wb") as f:
    pickle.dump({"classifier": clf, "label_encoder": le}, f)

total_faces = int(len(y))
retrain_ok  = True
', convert = TRUE))

file.remove(tmp)

cv_acc <- as.numeric(py$cv_accuracy)
total  <- as.integer(py$total_faces)
log_message(sprintf("Retrain complete: %d students, CV accuracy: %.1f%%",
                    total, cv_acc * 100), LOG_FILE)

# Update model_versions
with_connection(function(con) {
  dbExecute(con,
    sqlInterpolate(con,
      "UPDATE model_versions
          SET total_faces = ?n, last_trained_at = NOW(), accuracy = ?acc
        WHERE is_active = 1 AND model_type = 'face_recognition'",
      n   = total,
      acc = cv_acc))
})

log_message("model_versions updated.", LOG_FILE)
cat("RETRAIN_OK\n")
