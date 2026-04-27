# ============================================================
# init_python.R — Initialise the Python environment via reticulate
# ============================================================

library(reticulate)
library(yaml)

# Resolve config path relative to this file
cfg_path <- file.path(dirname(sys.frame(1)$ofile %||% "."),
                      "python_config.yml")
if (!file.exists(cfg_path)) {
  cfg_path <- file.path("analytics-r", "reticulate", "python_config.yml")
}
cfg <- yaml::read_yaml(cfg_path)

venv_path <- cfg$virtualenv_path
if (!nchar(Sys.getenv("RETICULATE_PYTHON")) == 0) {
  use_python(Sys.getenv("RETICULATE_PYTHON"), required = TRUE)
} else if (virtualenv_exists(venv_path)) {
  use_virtualenv(venv_path, required = TRUE)
} else {
  message("Creating virtualenv at: ", venv_path)
  virtualenv_create(venv_path, python = cfg$python_version)
  required <- cfg$required_python_packages
  virtualenv_install(venv_path, packages = required, ignore_installed = FALSE)
  use_virtualenv(venv_path, required = TRUE)
}

# Import core modules and expose them globally
cv2       <<- import("cv2",       convert = FALSE)
np        <<- import("numpy",     convert = FALSE)
tf        <<- import("tensorflow", convert = FALSE)
sklearn   <<- import("sklearn",   convert = FALSE)

message("Python environment initialised: ", py_config()$python)
