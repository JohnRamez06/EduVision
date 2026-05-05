# C:/Users/john/Desktop/eduvision/analytics-r/reticulate/init_python.R

library(reticulate)

# Use your existing Python virtual environment
use_python("C:/Users/john/Desktop/eduvision/vision-engine/.venv/Scripts/python.exe")

# Or use conda environment (uncomment if using conda)
# use_condaenv("eduvision", required = TRUE)

# Import Python modules with error handling
np <- safe_import("numpy")
cv2 <- safe_import("cv2")
deepface <- safe_import("deepface")
tensorflow <- safe_import("tensorflow")

# Suppress TensorFlow warnings
tf <- import("tensorflow")
tf$compat$v1$logging$set_verbosity(tf$compat$v1$logging$ERROR)

cat("✅ Python environment initialized\n")
cat("   Python path:", py_config()$python, "\n")
cat("   Modules loaded: numpy, cv2, deepface, tensorflow\n")