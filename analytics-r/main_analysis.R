
# main_analysis.R - Reticulate Integration Demo

cat("========================================\n")
cat("EduVision Reticulate Integration\n")
cat("========================================\n\n")

# Load reticulate library
library(reticulate)

# Set Python path (adjust if needed)
use_python("C:/Users/john/Desktop/eduvision/vision-engine/.venv/Scripts/python.exe", required = TRUE)

cat("✅ Python version:", py_config()$version, "\n")
cat("✅ Python path:", py_config()$python, "\n\n")

# Import Python modules
cat("Importing Python modules...\n")
np <- import("numpy")
cat("  ✓ numpy\n")

cv2 <- import("cv2")
cat("  ✓ opencv-python\n")

# Test OpenCV
test_img <- np$ones(c(100, 100, 3), dtype = "uint8") * 255
cat("✅ OpenCV test passed\n\n")

# Load existing R modules
if(file.exists("config.R")) {
  source("config.R")
  cat("✅ Database config loaded\n")
}

if(file.exists("run_report.R")) {
  source("run_report.R")
  cat("✅ Student report generator loaded\n")
}

cat("\n========================================\n")
cat("Ready to use reticulate with EduVision!\n")
cat("========================================\n")

