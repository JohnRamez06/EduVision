# C:/Users/john/Desktop/eduvision/analytics-r/main_analysis.R

# This script demonstrates using reticulate to call Python
# and then using R for analysis

cat("========================================\n")
cat("EduVision Reticulate Integration Demo\n")
cat("========================================\n")

# Load all modules
source("reticulate/init_python.R")
source("reticulate/face_analysis.R")
source("reticulate/emotion_analysis.R")
source("reticulate/attendance_analysis.R")

# Also load your existing R scripts
source("config.R")
source("scripts/fetch_data.R")

# Function: Process a student's face from enrollment photo
process_student_face <- function(student_id, photo_path) {
  cat("\n--- Processing Student:", student_id, "---\n")
  
  # 1. Recognize face using Python (via reticulate)
  face_result <- recognize_face(photo_path)
  
  if (face_result$success) {
    cat("✅ Face detected\n")
    cat("   Embedding dimension:", length(face_result$embedding), "\n")
  } else {
    cat("❌", face_result$message, "\n")
    return(NULL)
  }
  
  # 2. Analyze emotion using Python (via reticulate)
  emotion_result <- analyze_emotion(photo_path)
  
  if (emotion_result$success) {
    cat("✅ Emotion analyzed\n")
    cat("   Dominant emotion:", emotion_result$dominant_emotion, "\n")
  } else {
    cat("❌", emotion_result$message, "\n")
  }
  
  return(list(
    face = face_result,
    emotion = emotion_result
  ))
}

# Function: Generate student report using R (existing generator)
generate_report_with_r <- function(student_id) {
  cat("\n--- Generating Student Report ---\n")
  
  # Source your existing generator
  source("generators/generate_student_weekly.R")
  
  # Generate report
  result <- generate_student_weekly_report(student_id)
  
  cat("✅ Report generated:", result, "\n")
  return(result)
}

# Function: Get live session data via Python API
get_live_session_data <- function(session_id) {
  cat("\n--- Fetching Live Session Data ---\n")
  
  tryCatch({
    requests <- import("requests")
    
    # Get class snapshot from Spring Boot
    response <- requests$get(
      paste0("http://localhost:8080/api/v1/emotion-data/session/", session_id)
    )
    
    if (response$status_code == 200) {
      data <- response$json()
      cat("✅ Live data fetched\n")
      return(data)
    } else {
      cat("❌ Failed:", response$status_code, "\n")
      return(NULL)
    }
  }, error = function(e) {
    cat("❌ Error:", e$message, "\n")
    return(NULL)
  })
}

# Run all examples
if (interactive()) {
  cat("\n========================================\n")
  cat("Available Functions:\n")
  cat("========================================\n")
  cat("1. process_student_face(student_id, photo_path)\n")
  cat("2. generate_report_with_r(student_id)\n")
  cat("3. get_live_session_data(session_id)\n")
  cat("4. recognize_face(image_path)\n")
  cat("5. analyze_emotion(image_path)\n")
  cat("6. compare_faces(path1, path2)\n")
  cat("7. send_attendance_to_spring(session_id, student_id, name)\n")
  cat("========================================\n")
  
  # Example: Process a test face (uncomment to test)
  # test_photo <- "../face_enrollment/231002467/photo_1.jpg"
  # if(file.exists(test_photo)) {
  #   process_student_face("231002467", test_photo)
  # }
}

cat("\n✅ Master analysis module loaded successfully!\n")