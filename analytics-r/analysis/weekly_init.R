# C:/Users/john/Desktop/eduvision/analytics-r/analysis/weekly_init.R

# This script initializes weekly analysis
source("config.R")

# Create the with_connection function if it doesn't exist
with_connection <- function(conn, expr) {
  tryCatch({
    eval(expr)
  }, error = function(e) {
    message("Error in with_connection: ", e$message)
  })
}

message("✅ Weekly analysis initialized")
message("Available functions:")
message("  - with_connection(conn, { ... })")