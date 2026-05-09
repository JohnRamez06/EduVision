library(DBI)
library(RMariaDB)

# Resolve analytics-r root dynamically (works whether called via Rscript or sourced)
.resolve_base <- function() {
  # 1. Honour an env var set by the Java caller
  env <- Sys.getenv("ANALYTICS_HOME", "")
  if (nchar(env) > 0) return(normalizePath(env))

  # 2. Use --file= from commandArgs (reliable when run via Rscript)
  cmd <- commandArgs(trailingOnly = FALSE)
  file_flag <- grep("^--file=", cmd, value = TRUE)
  if (length(file_flag) > 0) {
    script_path <- normalizePath(sub("^--file=", "", file_flag[1]), mustWork = FALSE)
    # Walk up from the script's directory until we find config.R
    candidate <- dirname(script_path)
    for (i in seq_len(6)) {
      if (file.exists(file.path(candidate, "config.R"))) return(candidate)
      candidate <- dirname(candidate)
    }
  }

  # 3. Fall back to working directory
  getwd()
}

BASE_DIR <- .resolve_base()

DB_CONFIG <- list(
  host     = "127.0.0.1",
  port     = 3306,
  user     = "root",
  password = "",
  database = "eduvision"
)

OUTPUT_DIRS <- list(
  dean     = file.path(BASE_DIR, "output/dean"),
  lecturer = file.path(BASE_DIR, "output/lecturer"),
  student  = file.path(BASE_DIR, "output/student"),
  session  = file.path(BASE_DIR, "output/session")
)

for (dir_path in OUTPUT_DIRS) {
  if (!dir.exists(dir_path)) dir.create(dir_path, recursive = TRUE)
}

get_connection <- function() {
  dbConnect(RMariaDB::MariaDB(),
            host     = DB_CONFIG$host,
            port     = DB_CONFIG$port,
            user     = DB_CONFIG$user,
            password = DB_CONFIG$password,
            dbname   = DB_CONFIG$database)
}

message(sprintf("[config] BASE_DIR = %s", BASE_DIR))
