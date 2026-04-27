# ============================================================
# weekly_init.R — Insert current week into weekly_periods
# Called by cron job at the start of each week.
# Usage: Rscript weekly_init.R
# ============================================================

AN_DIR <- dirname(sys.frame(1)$ofile %||% ".")
ROOT   <- dirname(AN_DIR)
source(file.path(ROOT, "config.R"), local = TRUE)
source(file.path(ROOT, "scripts", "utils.R"), local = TRUE)

today      <- Sys.Date()
week_num   <- as.integer(format(today, "%V"))
year       <- as.integer(format(today, "%Y"))
start_date <- today - (as.integer(format(today, "%u")) - 1)  # Monday
end_date   <- start_date + 6L                                  # Sunday

with_connection(function(con) {
  existing <- dbGetQuery(con, sqlInterpolate(con,
    "SELECT id FROM weekly_periods WHERE week_number = ?w AND year = ?y",
    w = week_num, y = year))

  if (nrow(existing) > 0) {
    log_message(sprintf("Weekly period week %d / %d already exists (id=%s).",
                        week_num, year, existing$id[1]))
    return(invisible(NULL))
  }

  dbExecute(con, sqlInterpolate(con,
    "INSERT INTO weekly_periods (week_number, year, start_date, end_date)
     VALUES (?w, ?y, ?sd, ?ed)",
    w  = week_num,
    y  = year,
    sd = format(start_date),
    ed = format(end_date)))

  log_message(sprintf("Created weekly_period: week %d / %d (%s – %s)",
                      week_num, year, start_date, end_date))
})
