# C:/Users/john/Desktop/eduvision/analytics-r/config.R

# Load required libraries
library(DBI)
library(RMariaDB)

# Set base directory (absolute path)
BASE_DIR <- "C:/Users/john/Desktop/eduvision/analytics-r"

# Database configuration
DB_CONFIG <- list(
  host = "localhost",
  port = 3306,
  user = "root",
  password = "",
  database = "eduvision"
)

# Output directories (using absolute paths)
OUTPUT_DIRS <- list(
  dean = file.path(BASE_DIR, "output/dean"),
  lecturer = file.path(BASE_DIR, "output/lecturer"),
  student = file.path(BASE_DIR, "output/student"),
  session = file.path(BASE_DIR, "output/session")
)

# Create directories if they don't exist
for (dir_name in OUTPUT_DIRS) {
  if (!dir.exists(dir_name)) {
    dir.create(dir_name, recursive = TRUE)
    message(sprintf("Created directory: %s", dir_name))
  }
}

# Connect to database
get_connection <- function() {
  dbConnect(RMariaDB::MariaDB(),
            host = DB_CONFIG$host,
            port = DB_CONFIG$port,
            user = DB_CONFIG$user,
            password = DB_CONFIG$password,
            dbname = DB_CONFIG$database
  )
}

# Print confirmation
message(sprintf("Base directory: %s", BASE_DIR))
message(sprintf("Student output directory: %s", OUTPUT_DIRS$student))
message(sprintf("Directory exists: %s", dir.exists(OUTPUT_DIRS$student)))