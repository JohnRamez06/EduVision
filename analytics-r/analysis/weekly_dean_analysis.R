# ============================================================
# weekly_dean_analysis.R
# ============================================================

AN_DIR <- dirname(sys.frame(1)$ofile %||% ".")
ROOT   <- dirname(AN_DIR)
source(file.path(ROOT, "config.R"),   local = TRUE)
source(file.path(ROOT, "scripts", "utils.R"), local = TRUE)
source(file.path(AN_DIR, "weekly_lecturer_analysis.R"), local = TRUE)

#' Weekly department-level analysis.
#' @param dean_id  Character user id of the dean.
#' @param week_id  weekly_periods PK.
weekly_dean_analysis <- function(dean_id, week_id) {
  wp <- with_connection(function(con) {
    dbGetQuery(con, sqlInterpolate(con,
      "SELECT * FROM weekly_periods WHERE id = ?wid", wid = week_id))
  })
  if (nrow(wp) == 0) stop("weekly_period not found: ", week_id)

  date_from <- paste(wp$start_date[1], "00:00:00")
  date_to   <- paste(wp$end_date[1],   "23:59:59")

  # Fetch all sessions in the department for this week
  dept_data <- with_connection(function(con) {
    dbGetQuery(con, sqlInterpolate(con,
      "SELECT ls.id AS session_id, ls.lecturer_id,
              CONCAT(u.first_name,' ',u.last_name) AS lecturer_name,
              c.id AS course_id, c.code, c.title AS course_title,
              AVG(es.engagement_score)  AS avg_engagement,
              AVG(es.avg_concentration) AS avg_concentration,
              COUNT(DISTINCT sa.student_id) AS students_present
         FROM lecture_sessions ls
         JOIN courses c ON c.id = ls.course_id
         JOIN users   u ON u.id = ls.lecturer_id
    LEFT JOIN emotion_snapshots es ON es.session_id = ls.id
    LEFT JOIN session_attendance sa ON sa.session_id = ls.id
                                    AND sa.status IN ('present','late')
        WHERE ls.actual_start BETWEEN ?df AND ?dt
        GROUP BY ls.id, ls.lecturer_id, u.first_name, u.last_name,
                 c.id, c.code, c.title",
      df = date_from, dt = date_to))
  })

  if (nrow(dept_data) == 0) {
    return(list(dean_id = dean_id, week_id = week_id,
                n_sessions = 0L, n_lecturers = 0L, n_courses = 0L,
                avg_engagement = NA_real_, lecturer_ranking = data.frame(),
                at_risk_courses = data.frame()))
  }

  # Rank lecturers by average engagement
  lect_rank <- dept_data %>%
    dplyr::group_by(lecturer_id, lecturer_name) %>%
    dplyr::summarise(avg_eng  = mean(avg_engagement, na.rm = TRUE),
                     n_sess   = dplyr::n(), .groups = "drop") %>%
    dplyr::arrange(dplyr::desc(avg_eng))

  # Identify at-risk courses (avg engagement < 0.4)
  course_summary <- dept_data %>%
    dplyr::group_by(course_id, code, course_title) %>%
    dplyr::summarise(avg_eng = mean(avg_engagement, na.rm = TRUE),
                     .groups = "drop")
  at_risk <- course_summary %>% dplyr::filter(avg_eng < 0.4 | is.na(avg_eng))

  list(
    dean_id          = dean_id,
    week_id          = week_id,
    n_sessions       = nrow(dept_data),
    n_lecturers      = length(unique(dept_data$lecturer_id)),
    n_courses        = length(unique(dept_data$course_id)),
    avg_engagement   = mean(dept_data$avg_engagement, na.rm = TRUE),
    lecturer_ranking = lect_rank,
    at_risk_courses  = at_risk,
    course_summary   = course_summary
  )
}
