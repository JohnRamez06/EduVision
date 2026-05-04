# C:/Users/john/Desktop/eduvision/analytics-r/face_learning/verify_enrollment.R

# Remove the reticulate dependency since you already have embeddings in DB
library(DBI)
library(RMariaDB)

conn <- dbConnect(RMariaDB::MariaDB(), 
                  host = "localhost", 
                  user = "root", 
                  password = "", 
                  dbname = "eduvision")

# Check face_encodings
result <- dbGetQuery(conn, "
    SELECT 
        COUNT(*) as total_students,
        SUM(CASE WHEN face_encoding IS NOT NULL THEN 1 ELSE 0 END) as has_encoding,
        SUM(CASE WHEN LENGTH(face_encoding) = 512 THEN 1 ELSE 0 END) as valid_encoding
    FROM students
")

dbDisconnect(conn)

cat("\n=== FACE ENROLLMENT VERIFICATION ===\n")
cat("Total students:", result$total_students, "\n")
cat("Students with face encoding:", result$has_encoding, "\n")
cat("Students with valid 128-d embeddings:", result$valid_encoding, "\n")
cat("\n✅ Enrollment complete for", result$valid_encoding, "students\n")