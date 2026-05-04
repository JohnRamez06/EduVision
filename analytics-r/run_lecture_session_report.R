# Lecture Session Report - Detailed stats for one lecture
args <- commandArgs(trailingOnly = TRUE)
session_id <- if (length(args) > 0) args[1] else NULL

if (is.null(session_id)) {
  stop("Please provide session_id")
}

library(DBI)
library(RMariaDB)

conn <- dbConnect(RMariaDB::MariaDB(), 
                  host = "localhost", 
                  user = "root", 
                  password = "", 
                  dbname = "eduvision")

# Get session info with course and lecturer
session_info <- dbGetQuery(conn, sprintf("
    SELECT 
        ls.id,
        ls.title,
        ls.scheduled_start,
        ls.scheduled_end,
        ls.status,
        c.id as course_id,
        c.code as course_code,
        c.title as course_name,
        CONCAT(u.first_name, ' ', u.last_name) as lecturer_name
    FROM lecture_sessions ls
    JOIN courses c ON c.id = ls.course_id
    JOIN lecturers l ON l.user_id = ls.lecturer_id
    JOIN users u ON u.id = l.user_id
    WHERE ls.id = '%s'
", session_id))

if (nrow(session_info) == 0) {
  stop("Session not found")
}

# Get emotion distribution
emotions <- dbGetQuery(conn, sprintf("
    SELECT 
        ses.emotion,
        COUNT(*) as count
    FROM student_emotion_snapshots ses
    WHERE ses.session_id = '%s'
    GROUP BY ses.emotion
    ORDER BY count DESC
", session_id))

# Get concentration levels
concentration <- dbGetQuery(conn, sprintf("
    SELECT 
        concentration,
        COUNT(*) as count
    FROM student_emotion_snapshots ses
    WHERE ses.session_id = '%s'
    GROUP BY concentration
", session_id))

# Get students who attended with their stats
students <- dbGetQuery(conn, sprintf("
    SELECT 
        s.student_number,
        CONCAT(u.first_name, ' ', u.last_name) as student_name,
        ses.emotion as primary_emotion,
        ses.concentration
    FROM session_attendance sa
    JOIN students s ON s.user_id = sa.student_id
    JOIN users u ON u.id = s.user_id
    LEFT JOIN (
        SELECT student_id, emotion, concentration, 
               ROW_NUMBER() OVER (PARTITION BY student_id ORDER BY captured_at DESC) as rn
        FROM student_emotion_snapshots
        WHERE session_id = '%s'
    ) ses ON ses.student_id = s.user_id AND ses.rn = 1
    WHERE sa.session_id = '%s' AND sa.status = 'present'
", session_id, session_id))

# Calculate metrics
total_students <- nrow(students)
positive_emotions <- sum(emotions$count[emotions$emotion %in% c("happy", "surprised")])
negative_emotions <- sum(emotions$count[emotions$emotion %in% c("sad", "angry", "fearful", "confused")])
neutral_emotions <- sum(emotions$count[emotions$emotion == "neutral"])

total_emotions <- sum(emotions$count)
engagement_score <- if(total_emotions > 0) round((positive_emotions / total_emotions) * 100, 1) else 0

# Concentration breakdown
high_conc <- sum(concentration$count[concentration$concentration == "high"])
medium_conc <- sum(concentration$count[concentration$concentration == "medium"])
low_conc <- sum(concentration$count[concentration$concentration == "low"])

dbDisconnect(conn)

# Create output directory
dir.create("output/lecturer/sessions", recursive = TRUE, showWarnings = FALSE)

# Generate HTML
html_file <- sprintf("output/lecturer/sessions/session_report_%s.html", substr(session_id, 1, 8))

html_content <- paste0(
'<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Lecture Report - ', session_info$title[1], '</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; background: #f5f5f5; }
        .container { max-width: 1000px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        h1 { color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 10px; }
        h2 { color: #34495e; margin-top: 30px; }
        table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #3498db; color: white; }
        .info-card { display: inline-block; margin: 10px; padding: 15px; background: #f8f9fa; border-radius: 8px; text-align: center; min-width: 150px; }
        .card-value { font-size: 28px; font-weight: bold; }
        .positive { color: green; }
        .negative { color: red; }
        .neutral { color: gray; }
        .high { color: #10b981; }
        .medium { color: #f59e0b; }
        .low { color: #ef4444; }
        .footer { margin-top: 40px; text-align: center; color: #7f8c8d; font-size: 12px; border-top: 1px solid #eee; padding-top: 20px; }
    </style>
</head>
<body>
<div class="container">

<h1>📊 Lecture Session Report</h1>

<h2>📋 Session Details</h2>
<table>
    <tr><th>Course</th><td>', session_info$course_code[1], ' - ', session_info$course_name[1], '</td></tr>
    <tr><th>Lecture Title</th><td>', session_info$title[1], '</td></tr>
    <tr><th>Lecturer</th><td>', session_info$lecturer_name[1], '</td></tr>
    <tr><th>Date & Time</th><td>', session_info$scheduled_start[1], ' to ', session_info$scheduled_end[1], '</td></tr>
    <tr><th>Status</th><td>', session_info$status[1], '</td></tr>
</table>

<h2>📈 Session Statistics</h2>
<div style="text-align: center;">
    <div class="info-card"><div class="card-value">', total_students, '</div><div>Students Attended</div></div>
    <div class="info-card"><div class="card-value positive">', engagement_score, '%</div><div>Engagement Score</div></div>
    <div class="info-card"><div class="card-value positive">', positive_emotions, '</div><div>Positive Emotions</div></div>
    <div class="info-card"><div class="card-value negative">', negative_emotions, '</div><div>Negative Emotions</div></div>
</div>

<h2>😊 Emotion Distribution</h2>
<table>
    <tr><th>Emotion</th><th>Count</th><th>Percentage</th></tr>')

for(i in 1:nrow(emotions)) {
  pct <- round(emotions$count[i] / total_emotions * 100, 1)
  emotion_class <- ifelse(emotions$emotion[i] %in% c("happy", "surprised"), "positive", 
                   ifelse(emotions$emotion[i] %in% c("sad", "angry", "fearful", "confused"), "negative", "neutral"))
  html_content <- paste0(html_content, '
    <tr>
        <td>', emotions$emotion[i], '</td>
        <td>', emotions$count[i], '</td>
        <td class="', emotion_class, '">', pct, '%</td>
    </tr>')
}

html_content <- paste0(html_content, '
</table>

<h2>🎯 Concentration Analysis</h2>
<table>
    <tr><th>Level</th><th>Count</th><th>Percentage</th></tr>')

if(nrow(concentration) > 0) {
  total_conc <- sum(concentration$count)
  for(i in 1:nrow(concentration)) {
    pct <- round(concentration$count[i] / total_conc * 100, 1)
    conc_class <- ifelse(concentration$concentration[i] == "high", "high", 
                  ifelse(concentration$concentration[i] == "medium", "medium", "low"))
    html_content <- paste0(html_content, '
    <tr>
        <td>', concentration$concentration[i], '</td>
        <td>', concentration$count[i], '</td>
        <td class="', conc_class, '">', pct, '%</td>
    </tr>')
  }
}

html_content <- paste0(html_content, '
</table>

<h2>👥 Students Attended (', total_students, ')</h2>
<table>
    <tr><th>Student ID</th><th>Name</th><th>Primary Emotion</th><th>Concentration</th></tr>')

if(nrow(students) > 0) {
  for(i in 1:nrow(students)) {
    emotion_class <- ifelse(students$primary_emotion[i] %in% c("happy", "surprised"), "positive",
                     ifelse(students$primary_emotion[i] %in% c("sad", "angry", "fearful", "confused"), "negative", "neutral"))
    html_content <- paste0(html_content, '
    <tr>
        <td>', students$student_number[i], '</td>
        <td>', students$student_name[i], '</td>
        <td class="', emotion_class, '">', ifelse(is.na(students$primary_emotion[i]), "N/A", students$primary_emotion[i]), '</td>
        <td class="', ifelse(is.na(students$concentration[i]), "neutral", students$concentration[i]), '">', ifelse(is.na(students$concentration[i]), "N/A", students$concentration[i]), '</td>
    </tr>')
  }
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
message("✅ Lecture session report: ", html_file)
browseURL(html_file)