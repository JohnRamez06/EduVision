# ============================================================
# config.R — EduVision Analytics: database connection helpers
# ============================================================

suppressPackageStartupMessages(library(DBI))
suppressPackageStartupMessages(library(RMySQL))

DB_HOST   <- Sys.getenv("DB_HOST",   "localhost")
DB_PORT   <- as.integer(Sys.getenv("DB_PORT", "3306"))
DB_USER   <- Sys.getenv("DB_USER",   "root")
DB_PASS   <- Sys.getenv("DB_PASS",   "")
DB_NAME   <- Sys.getenv("DB_NAME",   "eduvision")

# Returns a fresh DBI connection to the eduvision MySQL database.
# Caller is responsible for calling dbDisconnect() when done.
get_connection <- function() {
  dbConnect(
    RMySQL::MySQL(),
    host     = DB_HOST,
    port     = DB_PORT,
    user     = DB_USER,
    password = DB_PASS,
    dbname   = DB_NAME,
    client.flag = CLIENT_MULTI_STATEMENTS
  )
}

# Convenience wrapper: run a function with a connection, then disconnect.
with_connection <- function(fn) {
  con <- get_connection()
  on.exit(dbDisconnect(con), add = TRUE)
  fn(con)
}

# Resolve the analytics-r base directory regardless of working directory.
ANALYTICS_HOME <- normalizePath(
  file.path(dirname(sys.frame(1)$ofile %||% getwd())),
  mustWork = FALSE
)

# Fallback: allow env override
if (nchar(Sys.getenv("ANALYTICS_HOME")) > 0) {
  ANALYTICS_HOME <- Sys.getenv("ANALYTICS_HOME")
}

MODEL_DIR  <- file.path(ANALYTICS_HOME, "models")
OUTPUT_DIR <- file.path(ANALYTICS_HOME, "output")
DATA_DIR   <- file.path(ANALYTICS_HOME, "data")
LOGS_DIR   <- file.path(ANALYTICS_HOME, "logs")

# Null-coalescing operator
`%||%` <- function(a, b) if (!is.null(a) && length(a) > 0 && !is.na(a[1])) a else b
