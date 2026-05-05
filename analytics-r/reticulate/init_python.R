# C:/Users/john/Desktop/eduvision/analytics-r/reticulate/init_python.R

library(reticulate)

# ---------------------------------------------------------------------------
# Reticulate init
#
# Common failure mode on Windows: RETICULATE_PYTHON is set globally (User/System
# env var) and points at a different Python than the one you want. Reticulate
# will prefer that interpreter and ignore use_python(), causing import errors.
# ---------------------------------------------------------------------------

# %||% for base R (so this file can be sourced standalone)
`%||%` <- function(a, b) if (!is.null(a) && length(a) > 0 && !is.na(a)) a else b

# Clear RETICULATE_PYTHON if it is set, so this project can control the
# interpreter selection deterministically.
if (!is.na(Sys.getenv("RETICULATE_PYTHON", unset = NA))) {
  old <- Sys.getenv("RETICULATE_PYTHON")
  Sys.unsetenv("RETICULATE_PYTHON")
  message("[reticulate] Cleared RETICULATE_PYTHON (was: ", old, ")")
} else {
  message("[reticulate] RETICULATE_PYTHON not set")
}

# Force reticulate to use the vision-engine venv Python
PYTHON_BIN <- "C:/Users/john/Desktop/eduvision/vision-engine/.venv/Scripts/python.exe"

if (!file.exists(PYTHON_BIN)) {
  stop("[reticulate] Expected Python not found at: ", PYTHON_BIN,
       "\nFix the path or recreate the venv in vision-engine/.venv")
}

# required=TRUE makes reticulate error immediately if it cannot use this python
use_python(PYTHON_BIN, required = TRUE)
message("[reticulate] Using Python: ", PYTHON_BIN)

# Show resolved configuration (useful for debugging PATH / venv issues)
cfg <- py_config()
message("[reticulate] Resolved Python: ", cfg$python)
message("[reticulate] Python version: ", cfg$version)

# Helper: safe import with good error messages
safe_import <- function(module, alias = NULL) {
  alias <- alias %||% module
  message("[reticulate] Importing ", module, " ...")
  tryCatch(
    {
      m <- import(module, delay_load = FALSE)
      message("[reticulate] OK: ", module)
      m
    },
    error = function(e) {
      stop("[reticulate] FAILED importing '", module, "'.\n",
           "Python used: ", py_config()$python, "\n",
           "Error: ", conditionMessage(e), "\n",
           "\nTroubleshooting:\n",
           "- Confirm packages are installed in the venv:\n",
           "    ", PYTHON_BIN, " -m pip show ", module, "\n",
           "- Or install:\n",
           "    ", PYTHON_BIN, " -m pip install ", module, "\n",
           call. = FALSE)
    }
  )
}

# Import Python modules with error handling
np <- safe_import("numpy")
cv2 <- safe_import("cv2")
deepface <- safe_import("deepface")
tensorflow <- safe_import("tensorflow")

# Optional: reduce TensorFlow logging noise (if available)
tryCatch({
  tensorflow$compat$v1$logging$set_verbosity(tensorflow$compat$v1$logging$ERROR)
}, error = function(e) {
  # not fatal
})

message("[reticulate] Python environment initialized successfully")
message("[reticulate] Modules loaded: numpy, cv2, deepface, tensorflow")