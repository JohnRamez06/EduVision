# ============================================================
# weekly_lecturer_analysis.R
# ============================================================

ROOT <- {
  env <- Sys.getenv("ANALYTICS_HOME", "")
  if (nchar(env) > 0) env else {
    d <- tryCatch(normalizePath(dirname(sys.frame(1)$ofile)), error = function(e) getwd())
    dirname(d)
  }
}
if (!exists("get_connection")) source(file.path(ROOT, "config.R"), local = TRUE)
if (!exists("%||%"))           source(file.path(ROOT, "scripts", "utils.R"), local = TRUE)
source(file.path(ROOT, "scripts", "fetch_class_snapshots.R"), local = TRUE)
source(file.path(ROOT, "scripts", "calculate_statistics.R"),  local = TRUE)

#' Weekly analysis for a lecturer.
#' @param lecturer_id Character.
#' @param week_id     Integer or character weekly_periods PK.
#' @return Named list.
weekly_lecturer_analysis <- function(lecturer_id, week_id) {
  wp <- with_connection(function(con) {
    if (grepl("^[0-9]+$", as.character(week_id))) {
      dbGetQuery(con, sqlInterpolate(con,
        "SELECT * FROM weekly_periods WHERE week_number = ?wn ORDER BY year DESC LIMIT 1",
        wn = as.integer(week_id)))
    } else {
      dbGetQuery(con, sqlInterpolate(con,
        "SELECT * FROM weekly_periods WHERE id = ?wid", wid = week_id))
    }
  })
  if (nrow(wp) == 0) stop("weekly_period not found: ", week_id)

  date_from <- paste(wp$start_date[1], "00:00:00")
  date_to   <- paste(wp$end_date[1],   "23:59:59")

  # Fetch sessions for this lecturer in this week
  sessions <- with_connection(function(con) {
    dbGetQuery(con, sqlInterpolate(con,
      "SELECT ls.id, ls.title, ls.actual_start, ls.actual_end,
              c.code AS course_code, c.title AS course_title,
              COUNT(DISTINCT sa.student_id) AS students_present
         FROM lecture_sessions ls
         JOIN courses c ON c.id = ls.course_id
    LEFT JOIN session_attendance sa ON sa.session_id = ls.id
                                    AND sa.status IN ('present','late')
        WHERE ls.lecturer_id = ?lid
          AND ls.actual_start BETWEEN ?df AND ?dt
        GROUP BY ls.id, ls.title, ls.actual_start, ls.actual_end,
                 c.code, c.title",
      lid = lecturer_id, df = date_from, dt = date_to))
  })

  if (nrow(sessions) == 0) {
    return(list(lecturer_id        = lecturer_id,
                week_id            = week_id,
                n_sessions         = 0L,
                avg_engagement     = NA_real_,
                avg_concentration  = NA_real_,
                total_students     = 0L,
                session_details    = data.frame()))
  }

  # For each session, compute avg engagement from class snapshots
  session_stats <- lapply(sessions$id, function(sid) {
    snaps <- fetch_class_snapshots(sid)
    if (nrow(snaps) == 0) {
      return(data.frame(session_id = sid, avg_eng = NA_real_,
                        avg_conc = NA_real_))
    }
    data.frame(session_id = sid,
               avg_eng    = mean(as.numeric(snaps$engagement_score),  na.rm = TRUE),
               avg_conc   = mean(as.numeric(snaps$avg_concentration), na.rm = TRUE))
  })
  ss <- do.call(rbind, session_stats)

  list(
    lecturer_id       = lecturer_id,
    week_id           = week_id,
    n_sessions        = nrow(sessions),
    avg_engagement    = mean(ss$avg_eng,  na.rm = TRUE),
    avg_concentration = mean(ss$avg_conc, na.rm = TRUE),
    total_students    = sum(sessions$students_present, na.rm = TRUE),
    session_details   = merge(sessions, ss, by.x = "id", by.y = "session_id")
  )
}
