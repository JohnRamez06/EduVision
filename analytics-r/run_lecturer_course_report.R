# C:/Users/john/Desktop/eduvision/analytics-r/run_lecturer_course_report.R

args <- commandArgs(trailingOnly = TRUE)
lecturer_email <- if (length(args) > 0) args[1] else NULL
course_id <- if (length(args) > 1) args[2] else NULL

if (is.null(lecturer_email)) {
  stop("Please provide lecturer email")
}

library(DBI)
library(RMariaDB)

conn <- dbConnect(RMariaDB::MariaDB(), 
                  host = "localhost", 
                  user = "root", 
                  password = "", 
                  dbname = "eduvision")

# Get lecturer info - FIXED: Use correct email
lecturer <- dbGetQuery(conn, sprintf("
    SELECT l.user_id, CONCAT(u.first_name, ' ', u.last_name) as name, u.email
    FROM lecturers l
    JOIN users u ON u.id = l.user_id
    WHERE u.email = '%s'
", lecturer_email))

if (nrow(lecturer) == 0) {
  # Try with gmail format
  lecturer <- dbGetQuery(conn, sprintf("
    SELECT l.user_id, CONCAT(u.first_name, ' ', u.last_name) as name, u.email
    FROM lecturers l
    JOIN users u ON u.id = l.user_id
    WHERE u.email = '%s'
  ", paste0(lecturer_email, "@gmail.com")))
}

if (nrow(lecturer) == 0) {
  print("Available lecturers:")
  all_lecturers <- dbGetQuery(conn, "
    SELECT u.email FROM lecturers l JOIN users u ON u.id = l.user_id
  ")
  print(all_lecturers)
  dbDisconnect(conn)
  stop("Lecturer not found. Use one of: ", paste(all_lecturers$email, collapse=", "))
}

message("✅ Found lecturer: ", lecturer$name[1])

# Get courses taught by this lecturer
courses <- dbGetQuery(conn, sprintf("
    SELECT 
        c.id,
        c.code,
        c.title,
        COUNT(DISTINCT cs.student_id) as enrolled_students
    FROM courses c
    JOIN course_lecturers cl ON cl.course_id = c.id
    LEFT JOIN course_students cs ON cs.course_id = c.id AND cs.dropped_at IS NULL
    WHERE cl.lecturer_id = '%s'
    GROUP BY c.id
", lecturer$user_id[1]))

if (nrow(courses) == 0) {
  dbDisconnect(conn)
  stop("No courses found for this lecturer")
}

message("Courses taught:")
print(courses[, c("code", "title")])

# If no course specified, use first one
if (is.null(course_id) || course_id == "") {
  course_id <- courses$id[1]
  message("Using course: ", courses$code[1])
}

# Get course details
course_info <- courses[courses$id == course_id,]
if (nrow(course_info) == 0) {
  dbDisconnect(conn)
  stop("Course not found")
}

# Get all sessions for this course
sessions <- dbGetQuery(conn, sprintf("
    SELECT 
        ls.id,
        ls.title,
        ls.scheduled_start,
        ls.status,
        COUNT(DISTINCT sa.student_id) as attendance_count
    FROM lecture_sessions ls
    LEFT JOIN session_attendance sa ON sa.session_id = ls.id
    WHERE ls.course_id = '%s' AND ls.lecturer_id = '%s'
    GROUP BY ls.id
    ORDER BY ls.scheduled_start DESC
", course_id, lecturer$user_id[1]))

# Get overall course statistics
course_stats <- dbGetQuery(conn, sprintf("
    SELECT 
        COUNT(DISTINCT ls.id) as total_sessions,
        SUM(CASE WHEN ls.status = 'completed' THEN 1 ELSE 0 END) as completed_sessions,
        AVG(wca.attendance_rate) as avg_attendance
    FROM lecture_sessions ls
    LEFT JOIN weekly_course_attendance wca ON wca.course_id = ls.course_id
    WHERE ls.course_id = '%s'
", course_id))

# Get emotion summary for this course
emotions <- dbGetQuery(conn, sprintf("
    SELECT 
        ses.emotion,
        COUNT(*) as count
    FROM student_emotion_snapshots ses
    JOIN lecture_sessions ls ON ls.id = ses.session_id
    WHERE ls.course_id = '%s'
    GROUP BY ses.emotion
    ORDER BY count DESC
", course_id))

# Get weekly trend
weekly_trend <- dbGetQuery(conn, sprintf("
    SELECT 
        wp.week_number,
        wp.year,
        AVG(wca.attendance_rate) as attendance_rate
    FROM weekly_course_attendance wca
    JOIN weekly_periods wp ON wp.id = wca.week_id
    WHERE wca.course_id = '%s'
    GROUP BY wp.week_number, wp.year
    ORDER BY wp.year DESC, wp.week_number DESC
    LIMIT 10
", course_id))

dbDisconnect(conn)

# Create output directory
dir.create("output/lecturer/courses", recursive = TRUE, showWarnings = FALSE)

html_file <- sprintf("output/lecturer/courses/course_report_%s.html", course_info$code[1])

# Calculate metrics
total_emotions <- sum(emotions$count)
positive_count <- sum(emotions$count[emotions$emotion %in% c("happy", "surprised")])
engagement_score <- if(total_emotions > 0) round(positive_count / total_emotions * 100, 1) else 0

html_content <- paste0(
'<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Course Report - ', course_info$code[1], '</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; background: #f5f5f5; }
        .container { max-width: 1000px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        h1 { color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 10px; }
        h2 { color: #34495e; margin-top: 30px; }
        table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #3498db; color: white; }
        .info-card { display: inline-block; margin: 10px; padding: 15px; background: #f8f9fa; border-radius: 8px; text-align: center; min-width: 150px; }
        .card-value { font-size: 28px; font-weight: bold; color: #3498db; }
        .positive { color: green; }
        .negative { color: red; }
        .footer { margin-top: 40px; text-align: center; color: #7f8c8d; font-size: 12px; border-top: 1px solid #eee; padding-top: 20px; }
    </style>
</head>
<body>
<div class="container">

<h1>📚 Course Report</h1>

<h2>📋 Course Information</h2>
<table>
    <tr><th>Course Code</th><td>', course_info$code[1], '</td></tr>
    <tr><th>Course Title</th><td>', course_info$title[1], '</td></tr>
    <tr><th>Lecturer</th><td>', lecturer$name[1], '</td></tr>
    <tr><th>Enrolled Students</th><td>', course_info$enrolled_students[1], '</td></tr>
</table>

<h2>📊 Course Overview</h2>
<div style="text-align: center;">
    <div class="info-card"><div class="card-value">', ifelse(is.na(course_stats$total_sessions[1]), 0, course_stats$total_sessions[1]), '</div><div>Total Sessions</div></div>
    <div class="info-card"><div class="card-value">', ifelse(is.na(course_stats$completed_sessions[1]), 0, course_stats$completed_sessions[1]), '</div><div>Completed</div></div>
    <div class="info-card"><div class="card-value">', ifelse(is.na(course_stats$avg_attendance[1]), 0, round(course_stats$avg_attendance[1], 1)), '%</div><div>Avg Attendance</div></div>
    <div class="info-card"><div class="card-value">', engagement_score, '%</div><div>Engagement Score</div></div>
</div>

<h2>📅 Weekly Performance</h2>
<table>
    <tr><th>Week</th><th>Attendance Rate</th></tr>')

for(i in 1:nrow(weekly_trend)) {
  html_content <- paste0(html_content, '
    <tr>
        <td>Week ', weekly_trend$week_number[i], ', ', weekly_trend$year[i], '</td>
        <td>', round(weekly_trend$attendance_rate[i], 1), '%</td>
    </tr>')
}

html_content <- paste0(html_content, '
</table>

<h2>😊 Emotion Summary</h2>
<table>
    <tr><th>Emotion</th><th>Count</th></tr>')

for(i in 1:nrow(emotions)) {
  emotion_class <- ifelse(emotions$emotion[i] %in% c("happy", "surprised"), "positive", "negative")
  html_content <- paste0(html_content, '
    <tr><td class="', emotion_class, '">', emotions$emotion[i], '</td><td>', emotions$count[i], '</td></tr>')
}

html_content <- paste0(html_content, '
</table>

<h2>📋 Session History</h2>
<table>
    <tr><th>Session Title</th><th>Date</th><th>Status</th><th>Attendance</th></tr>')

for(i in 1:nrow(sessions)) {
  html_content <- paste0(html_content, '
    <tr>
        <td>', sessions$title[i], '</td>
        <td>', sessions$scheduled_start[i], '</td>
        <td>', sessions$status[i], '</td>
        <td>', sessions$attendance_count[i], ' students</td>
    </tr>')
}

html_content <- paste0(html_content, '
</table>

<div class="footer">
    Report generated by EduVision System on ', Sys.Date(), '
</div>

</div>
</body>
</html>')

writeLines(html_content, html_file)
message("✅ Course report generated: ", file.path(getwd(), html_file))