# Weekly Report for Lecturer - all courses per week
args <- commandArgs(trailingOnly = TRUE)
lecturer_email <- if (length(args) > 0) args[1] else NULL
week_number <- if (length(args) > 1) as.numeric(args[2]) else NULL

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

# Get lecturer info
lecturer <- dbGetQuery(conn, sprintf("
    SELECT l.user_id, CONCAT(u.first_name, ' ', u.last_name) as name
    FROM lecturers l
    JOIN users u ON u.id = l.user_id
    WHERE u.email = '%s'
", lecturer_email))

if (nrow(lecturer) == 0) {
  stop("Lecturer not found")
}

# If no week specified, get current week
if (is.null(week_number)) {
  week_info <- dbGetQuery(conn, "
    SELECT week_number, year, start_date, end_date 
    FROM weekly_periods 
    WHERE start_date <= CURDATE() AND end_date >= CURDATE()
    LIMIT 1
  ")
  if(nrow(week_info) == 0) {
    week_info <- dbGetQuery(conn, "SELECT week_number, year, start_date, end_date FROM weekly_periods ORDER BY start_date DESC LIMIT 1")
  }
  week_number <- week_info$week_number[1]
  week_year <- week_info$year[1]
  week_start <- week_info$start_date[1]
  week_end <- week_info$end_date[1]
} else {
  week_info <- dbGetQuery(conn, sprintf("
    SELECT week_number, year, start_date, end_date FROM weekly_periods WHERE week_number = %d ORDER BY year DESC LIMIT 1
  ", week_number))
  week_year <- week_info$year[1]
  week_start <- week_info$start_date[1]
  week_end <- week_info$end_date[1]
}

# Get weekly stats for all courses taught by lecturer
weekly_stats <- dbGetQuery(conn, sprintf("
    SELECT 
        c.code,
        c.title,
        wca.sessions_held,
        wca.sessions_attended,
        wca.attendance_rate,
        wca.status as student_status
    FROM weekly_course_attendance wca
    JOIN courses c ON c.id = wca.course_id
    WHERE wca.week_id IN (SELECT id FROM weekly_periods WHERE week_number = %d)
    AND c.id IN (SELECT course_id FROM course_lecturers WHERE lecturer_id = '%s')
", week_number, lecturer$user_id[1]))

# Get emotion summary for this week
emotions <- dbGetQuery(conn, sprintf("
    SELECT 
        ses.emotion,
        COUNT(*) as count
    FROM student_emotion_snapshots ses
    JOIN lecture_sessions ls ON ls.id = ses.session_id
    WHERE ls.lecturer_id = '%s'
    AND DATE(ses.captured_at) BETWEEN '%s' AND '%s'
    GROUP BY ses.emotion
    ORDER BY count DESC
", lecturer$user_id[1], week_start, week_end))

# Get concentration for this week
concentration <- dbGetQuery(conn, sprintf("
    SELECT 
        concentration,
        COUNT(*) as count
    FROM student_emotion_snapshots ses
    JOIN lecture_sessions ls ON ls.id = ses.session_id
    WHERE ls.lecturer_id = '%s'
    AND DATE(ses.captured_at) BETWEEN '%s' AND '%s'
    GROUP BY concentration
", lecturer$user_id[1], week_start, week_end))

dbDisconnect(conn)

# Create output directory
dir.create("output/lecturer/weekly", recursive = TRUE, showWarnings = FALSE)

html_file <- sprintf("output/lecturer/weekly/lecturer_week_%d_report.html", week_number)

# Calculate metrics
total_emotions <- sum(emotions$count)
positive_count <- sum(emotions$count[emotions$emotion %in% c("happy", "surprised")])
engagement_score <- if(total_emotions > 0) round(positive_count / total_emotions * 100, 1) else 0

html_content <- paste0(
'<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Weekly Report - ', lecturer$name[1], '</title>
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

<h1>📅 Weekly Lecturer Report</h1>

<p><strong>Lecturer:</strong> ', lecturer$name[1], '</p>
<p><strong>Week:</strong> ', week_number, ' (', week_start, ' to ', week_end, ')</p>

<h2>📊 Weekly Summary</h2>
<div style="text-align: center;">
    <div class="info-card"><div class="card-value">', nrow(weekly_stats), '</div><div>Courses</div></div>
    <div class="info-card"><div class="card-value">', ifelse(is.na(engagement_score), 0, engagement_score), '%</div><div>Engagement Score</div></div>
</div>

<h2>📚 Course Performance</h2>
<table>
    <tr><th>Course</th><th>Sessions Held</th><th>Attendance Rate</th><th>Status</th></tr>')

for(i in 1:nrow(weekly_stats)) {
  html_content <- paste0(html_content, '
    <tr>
        <td>', weekly_stats$code[i], ' - ', weekly_stats$title[i], '</td>
        <td>', weekly_stats$sessions_held[i], '</td>
        <td>', round(weekly_stats$attendance_rate[i], 1), '%</td>
        <td>', weekly_stats$student_status[i], '</td>
    </tr>')
}

html_content <- paste0(html_content, '
</table>

<h2>😊 Emotion Distribution (Week ', week_number, ')</h2>
<table>
    <tr><th>Emotion</th><th>Count</th></tr>')

for(i in 1:nrow(emotions)) {
  emotion_class <- ifelse(emotions$emotion[i] %in% c("happy", "surprised"), "positive", "negative")
  html_content <- paste0(html_content, '
    <tr><td class="', emotion_class, '">', emotions$emotion[i], '</td><td>', emotions$count[i], '</td></tr>')
}

html_content <- paste0(html_content, '
</table>

<h2>🎯 Concentration Levels</h2>
<table>
    <tr><th>Level</th><th>Count</th></tr>')

for(i in 1:nrow(concentration)) {
  html_content <- paste0(html_content, '
    <tr><td>', concentration$concentration[i], '</td><td>', concentration$count[i], '</td></tr>')
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
message("✅ Weekly report generated: ", html_file)
browseURL(html_file)