# MySQL Connection to XAMPP
library(DBI)
library(RMySQL)

get_connection <- function() {
  dbConnect(RMySQL::MySQL(), host="localhost", port=3306, dbname="eduvision", user="root", password="")
}
query_df <- function(sql) {
  con <- get_connection()
  on.exit(dbDisconnect(con))
  dbGetQuery(con, sql)
}
