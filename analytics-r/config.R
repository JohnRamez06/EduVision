library(DBI)
library(RMariaDB)

get_connection <- function() {
  dbConnect(
    RMariaDB::MariaDB(),
    host = Sys.getenv("DB_HOST", "localhost"),
    port = as.integer(Sys.getenv("DB_PORT", "3306")),
    dbname = Sys.getenv("DB_NAME", "eduvision"),
    user = Sys.getenv("DB_USER", "root"),
    password = Sys.getenv("DB_PASSWORD", "")
  )
}

query_df <- function(sql) {
  con <- get_connection()
  on.exit(dbDisconnect(con), add = TRUE)
  dbGetQuery(con, sql)
}