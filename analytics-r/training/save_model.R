# ============================================================
# save_model.R — Versioned model saving helper
# ============================================================

TRAIN_DIR <- dirname(sys.frame(1)$ofile %||% ".")
ROOT      <- dirname(TRAIN_DIR)
source(file.path(ROOT, "config.R"), local = TRUE)

#' Copy a trained Keras model to models/ with a version suffix and
#' INSERT a row into model_versions.
#'
#' @param model_path Source model path (e.g. models/emotion_model.h5).
#' @param model_type Character label ("emotion" | "concentration" | "face_recognition").
#' @param accuracy   Optional validation accuracy to store.
save_model_version <- function(model_path, model_type, accuracy = NA_real_) {
  ts         <- format(Sys.time(), "%Y%m%d_%H%M%S")
  versioned  <- sub("(\\.h5)$", sprintf("_%s\\1", ts), model_path)
  if (file.exists(model_path)) {
    file.copy(model_path, versioned, overwrite = TRUE)
    message("Versioned copy saved: ", versioned)
  }

  with_connection(function(con) {
    # Deactivate previous versions
    dbExecute(con, sqlInterpolate(con,
      "UPDATE model_versions SET is_active = 0 WHERE model_type = ?t",
      t = model_type))
    # Insert new active version
    dbExecute(con, sqlInterpolate(con,
      "INSERT INTO model_versions
         (model_type, model_path, version_tag, accuracy,
          is_active, trained_at)
       VALUES
         (?t, ?p, ?v, ?acc, 1, NOW())",
      t   = model_type,
      p   = versioned,
      v   = ts,
      acc = if (is.na(accuracy)) NULL else accuracy))
  })
  invisible(versioned)
}
