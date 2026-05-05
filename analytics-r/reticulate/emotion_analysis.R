# C:/Users/john/Desktop/eduvision/analytics-r/reticulate/emotion_analysis.R

source("reticulate/init_python.R")

# Analyze emotion from image
analyze_emotion <- function(image_path) {
  tryCatch({
    img <- cv2$imread(image_path)
    
    result <- deepface$analyze(
      img_path = img,
      actions = list("emotion"),
      detector_backend = "opencv",
      enforce_detection = FALSE
    )
    
    if (length(result) > 0) {
      emotions <- result[[1]]$emotion
      dominant <- names(emotions)[which.max(unlist(emotions))]
      
      return(list(
        success = TRUE,
        dominant_emotion = dominant,
        all_emotions = emotions,
        message = paste("Dominant emotion:", dominant)
      ))
    } else {
      return(list(success = FALSE, message = "No face detected"))
    }
  }, error = function(e) {
    return(list(success = FALSE, message = paste("Error:", e$message)))
  })
}

# Batch analyze multiple images
batch_analyze_emotions <- function(image_paths) {
  results <- list()
  for (path in image_paths) {
    results[[basename(path)]] <- analyze_emotion(path)
  }
  return(results)
}

cat("✅ Emotion analysis module loaded\n")