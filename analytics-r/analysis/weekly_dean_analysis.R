# C:/Users/john/Desktop/eduvision/analytics-r/analysis/weekly_dean_analysis.R

source("config.R")

dean_analysis <- function() {
  conn <- get_connection()
  on.exit(dbDisconnect(conn))
  
  # Get department overview
  dept_stats <- dbGetQuery(conn, "
    SELECT 
      c.department,
      COUNT(DISTINCT c.id) as total_courses,
      COUNT(DISTINCT cs.student_id) as total_students,
      COUNT(DISTINCT cl.lecturer_id) as total_lecturers
    FROM courses c
    LEFT JOIN course_students cs ON cs.course_id = c.id AND cs.dropped_at IS NULL
    LEFT JOIN course_lecturers cl ON cl.course_id = c.id
    GROUP BY c.department
  ")
  
  print(dept_stats)
  
  # Save to CSV
  write.csv(dept_stats, "output/dean_stats.csv", row.names = FALSE)
  message("✅ Dean analysis saved to output/dean_stats.csv")
}

dean_analysis()