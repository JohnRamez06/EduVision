# C:\Users\john\Desktop\eduvision\analytics-r\generators\generate_student_weekly.R

source("config.R")

generate_student_weekly_report <- function(student_id, week_id = NULL, output_dir = NULL) {
  
  # Use absolute path for output directory
  if (is.null(output_dir)) {
    output_dir <- OUTPUT_DIRS$student
  }
  
  conn <- get_connection()
  on.exit(dbDisconnect(conn))
  
  # Get student info
  student_info <- dbGetQuery(conn, sprintf("
        SELECT 
            s.user_id as student_id,
            s.student_number,
            CONCAT(u.first_name, ' ', u.last_name) as student_name,
            COALESCE(s.program, 'Not Specified') as program,
            COALESCE(s.year_of_study, 1) as year_of_study,
            u.email
        FROM students s
        JOIN users u ON u.id = s.user_id
        WHERE s.user_id = '%s'
    ", student_id))
  
  if (nrow(student_info) == 0) {
    stop(paste("Student not found with ID:", student_id))
  }
  
  message(sprintf("✅ Found student: %s", student_info$student_name[1]))
  
  # Get most recent week period
  week_info <- dbGetQuery(conn, "
        SELECT id, week_number, year, start_date, end_date 
        FROM weekly_periods 
        ORDER BY year DESC, week_number DESC 
        LIMIT 1
    ")
  
  if (nrow(week_info) == 0) {
    stop("No week periods found in database.")
  }
  
  week_period_id <- week_info$id[1]
  week_display <- sprintf("Week %d, %d", week_info$week_number[1], week_info$year[1])
  message(sprintf("📅 Using week: %s", week_display))
  
  # Get weekly attendance summary
  weekly_attendance <- dbGetQuery(conn, sprintf("
        SELECT 
            wp.week_number,
            wp.year,
            wp.start_date,
            wp.end_date,
            wca.course_id,
            c.code as course_name,
            wca.sessions_held,
            wca.sessions_attended,
            wca.sessions_missed,
            wca.attendance_rate,
            wca.status
        FROM weekly_course_attendance wca
        JOIN courses c ON c.id = wca.course_id
        JOIN weekly_periods wp ON wp.id = wca.week_id
        WHERE wca.student_id = '%s'
        ORDER BY wp.year DESC, wp.week_number DESC
        LIMIT 10
    ", student_id))
  
  message(sprintf("📊 Found %d weekly attendance records", nrow(weekly_attendance)))
  
  # Get emotion trends
  emotion_trends <- dbGetQuery(conn, sprintf("
        SELECT 
            DATE(ses.captured_at) as date,
            ses.emotion,
            COUNT(*) as count
        FROM student_emotion_snapshots ses
        WHERE ses.student_id = '%s'
        GROUP BY DATE(ses.captured_at), ses.emotion
        ORDER BY date DESC
        LIMIT 30
    ", student_id))
  
  message(sprintf("😊 Found %d emotion records", nrow(emotion_trends)))
  
  # Get concentration trends
  concentration_trends <- dbGetQuery(conn, sprintf("
        SELECT 
            DATE(ses.captured_at) as date,
            AVG(CASE 
                WHEN ses.concentration = 'high' THEN 0.9
                WHEN ses.concentration = 'medium' THEN 0.6
                WHEN ses.concentration = 'low' THEN 0.3
                ELSE 0.5
            END) as avg_concentration
        FROM student_emotion_snapshots ses
        WHERE ses.student_id = '%s'
        GROUP BY DATE(ses.captured_at)
        ORDER BY date DESC
        LIMIT 30
    ", student_id))
  
  message(sprintf("📈 Found %d concentration records", nrow(concentration_trends)))
  
  # Check if output directory exists
  if (!dir.exists(output_dir)) {
    dir.create(output_dir, recursive = TRUE)
    message(sprintf("Created output directory: %s", output_dir))
  }
  
  # Check if template exists
  template_path <- file.path(BASE_DIR, "reports/student_weekly_template.Rmd")
  if (!file.exists(template_path)) {
    stop(paste("Template not found:", template_path))
  }
  
  # Load required libraries
  library(rmarkdown)
  library(knitr)
  library(ggplot2)
  library(dplyr)
  
  # Render report
  output_file <- file.path(output_dir, sprintf("student_report_%s_week%d.pdf", 
                                               student_info$student_number[1],
                                               week_info$week_number[1]))
  
  message(sprintf("📄 Generating report at: %s", output_file))
  
  rmarkdown::render(
    input = template_path,
    output_file = output_file,
    params = list(
      student_name = student_info$student_name[1],
      student_number = student_info$student_number[1],
      program = student_info$program[1],
      year_of_study = student_info$year_of_study[1],
      week_display = week_display,
      week_start = as.character(week_info$start_date[1]),
      week_end = as.character(week_info$end_date[1]),
      weekly_attendance = weekly_attendance,
      emotion_trends = emotion_trends,
      concentration_trends = concentration_trends
    )
  )
  
  message(sprintf("✅ Report generated: %s", output_file))
  return(output_file)
}