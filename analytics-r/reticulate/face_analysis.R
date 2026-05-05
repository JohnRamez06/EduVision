# C:/Users/john/Desktop/eduvision/analytics-r/reticulate/face_analysis.R

source("reticulate/init_python.R")

# Function to recognize face from image path
recognize_face <- function(image_path) {
  tryCatch({
    # Read image with OpenCV
    img <- cv2$imread(image_path)
    
    # Use DeepFace to find face
    result <- deepface$represent(
      img_path = img,
      model_name = "Facenet",
      enforce_detection = FALSE,
      detector_backend = "opencv"
    )
    
    if (length(result) > 0) {
      embedding <- result[[1]]$embedding
      return(list(
        success = TRUE,
        embedding = embedding,
        message = "Face recognized successfully"
      ))
    } else {
      return(list(success = FALSE, message = "No face detected"))
    }
  }, error = function(e) {
    return(list(success = FALSE, message = paste("Error:", e$message)))
  })
}

# Function to compare two faces
compare_faces <- function(image_path1, image_path2) {
  result1 <- recognize_face(image_path1)
  result2 <- recognize_face(image_path2)
  
  if (!result1$success || !result2$success) {
    return(list(success = FALSE, message = "Could not extract faces"))
  }
  
  # Calculate cosine similarity
  cosine_sim <- function(a, b) {
    sum(a * b) / (sqrt(sum(a^2)) * sqrt(sum(b^2)))
  }
  
  similarity <- cosine_sim(result1$embedding, result2$embedding)
  
  return(list(
    success = TRUE,
    similarity = similarity,
    match = similarity > 0.6,
    message = ifelse(similarity > 0.6, "Same person", "Different person")
  ))
}

cat("✅ Face analysis module loaded\n")