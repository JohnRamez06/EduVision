# =============================================================================
# analysis/weekly_dean_analysis.R
# =============================================================================
BASE_DIR <- normalizePath(file.path(dirname(sys.frame(1)$ofile), ".."))
source(file.path(BASE_DIR, "config.R"))
source(file.path(BASE_DIR, "scripts", "fetch_data.R"))
library(dplyr)

weekly_dean_analysis <- function(week_id) {
  week <- fetch_week_dates(week_id)

  # All sessions this week
  sessions <- query_df(sprintf(
    "SELECT ls.id, ls.lecturer_id, ls.course_id, c.title AS course_title,
            c.department, es_agg.avg_eng, es_agg.avg_conc
     FROM lecture_sessions ls
     JOIN courses c ON c.id = ls.course_id
     LEFT JOIN (
       SELECT session_id,
              AVG(engagement_score) AS avg_eng,
              AVG(avg_concentration) AS avg_conc
       FROM emotion_snapshots GROUP BY session_id
     ) es_agg ON es_agg.session_id = ls.id
     WHERE ls.actual_start BETWEEN '%s' AND '%s'
     ORDER BY es_agg.avg_eng DESC",
    format(week$start_date, "%Y-%m-%d %H:%M:%S"),
    format(week$end_date,   "%Y-%m-%d %H:%M:%S")
  ))

  # Course rankings by avg engagement
  course_ranking <- sessions %>%
    group_by(course_id, course_title) %>%
    summarise(avg_engagement = mean(avg_eng, na.rm = TRUE), .groups = "drop") %>%
    arrange(desc(avg_engagement))

  # At-risk courses (avg engagement < 0.4)
  at_risk_courses <- course_ranking[course_ranking$avg_engagement < 0.4, ]

  # Lecturer performance
  lecturer_perf <- sessions %>%
    group_by(lecturer_id) %>%
    summarise(avg_engagement = mean(avg_eng, na.rm = TRUE),
              n_sessions = n(), .groups = "drop") %>%
    arrange(desc(avg_engagement))

  list(
    week_id        = week_id,
    week_number    = week$week_number,
    n_sessions     = nrow(sessions),
    course_ranking = course_ranking,
    at_risk_courses = at_risk_courses,
    lecturer_perf  = lecturer_perf
  )
}