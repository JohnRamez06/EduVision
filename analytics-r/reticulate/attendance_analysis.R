# C:/Users/john/Desktop/eduvision/analytics-r/reticulate/attendance_analysis.R

source("reticulate/init_python.R")

# Simulate sending attendance to Spring Boot via Python requests
library(httr)
library(jsonlite)

send_attendance_to_spring <- function(session_id, student_id, student_name) {
  tryCatch({
    # Use Python's requests via reticulate (optional)
    # Or use R's http
    payload <- list(
      sessionId = session_id,
      studentId = student_id,
      status = "present"
    )
    
    response <- POST(
      url = "http://localhost:8080/api/v1/attendance/record",
      body = toJSON(payload, auto_unbox = TRUE),
      encode = "json",
      content_type_json()
    )
    
    if (status_code(response) == 200) {
      return(list(success = TRUE, message = paste("Attendance recorded for", student_name)))
    } else {
      return(list(success = FALSE, message = paste("Failed:", status_code(response))))
    }
  }, error = function(e) {
    return(list(success = FALSE, message = paste("Error:", e$message)))
  })
}

# Alternative: Use Python's requests directly via reticulate
send_attendance_python <- function(session_id, student_id, student_name) {
  tryCatch({
    requests <- import("requests")
    
    payload <- list(
      sessionId = session_id,
      studentId = student_id,
      status = "present"
    )
    
    response <- requests$post(
      "http://localhost:8080/api/v1/attendance/record",
      json = payload
    )
    
    if (response$status_code == 200) {
      return(list(success = TRUE, message = paste("Attendance recorded for", student_name)))
    } else {
      return(list(success = FALSE, message = paste("Failed:", response$status_code)))
    }
  }, error = function(e) {
    return(list(success = FALSE, message = paste("Error:", e$message)))
  })
}

cat("✅ Attendance analysis module loaded\n")