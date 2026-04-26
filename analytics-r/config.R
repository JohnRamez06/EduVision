# =============================================================================
# EduVision - Database Configuration
# config.R - MySQL connection for XAMPP
# =============================================================================

library(DBI)
library(RMySQL)

# Database connection function
get_connection <- function() {
    con <- dbConnect(
        MySQL(),
        host = "localhost",
        user = "root",
        password = "",
        dbname = "eduvision"
    )
    return(con)
}

# Logging function
log_message <- function(msg, level = "INFO") {
    cat(sprintf("[%s] %s: %s\n", Sys.time(), level, msg))
}

# Ensure output directory exists
ensure_output_dir <- function(dir_path) {
    if (!dir.exists(dir_path)) {
        dir.create(dir_path, recursive = TRUE)
    }
}