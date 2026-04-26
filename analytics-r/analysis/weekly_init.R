# =============================================================================
# analysis/weekly_init.R
# Usage: Rscript weekly_init.R   (called by cron/Spring Boot scheduler)
# =============================================================================
BASE_DIR <- normalizePath(file.path(dirname(sys.frame(1)$ofile), ".."))
source(file.path(BASE_DIR, "config.R"))
library(lubridate)

initialize_weekly_period <- function(reference_date = Sys.Date()) {
  ref       <- as.Date(reference_date)
  week_num  <- isoweek(ref)
  yr        <- isoyear(ref)
  start_dt  <- floor_date(ref, "week", week_start = 1)   # Monday
  end_dt    <- start_dt + days(6)                         # Sunday

  existing <- query_df(sprintf(
    "SELECT id FROM weekly_periods WHERE week_number = %d AND year = %d", week_num, yr
  ))

  if (nrow(existing) > 0) {
    log_message(sprintf("Weekly period W%d/%d already exists (id=%s)", week_num, yr, existing$id[1]))
    return(existing$id[1])
  }

  new_id <- paste0(format(Sys.time(), "%Y%m%d%H%M%S"), sample(1000:9999, 1))
  execute_sql(sprintf(
    "INSERT INTO weekly_periods (id, week_number, year, start_date, end_date)
     VALUES ('%s', %d, %d, '%s', '%s')",
    new_id, week_num, yr,
    format(start_dt, "%Y-%m-%d"),
    format(end_dt,   "%Y-%m-%d")
  ))
  log_message(sprintf("Created weekly period W%d/%d: %s to %s (id=%s)",
                       week_num, yr, start_dt, end_dt, new_id))
  new_id
}

week_id <- initialize_weekly_period()
cat(jsonlite::toJSON(list(week_id = week_id), auto_unbox = TRUE), "\n")