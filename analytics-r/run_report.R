# C:\Users\john\Desktop\eduvision\analytics-r\run_report.R
# Get student ID from command line argument
args <- commandArgs(trailingOnly = TRUE)
student_id <- if (length(args) > 0) args[1] else "st0020-0000-0000-0000-000000000001"

library(DBI)
library(RMariaDB)

# Connect to database
conn <- dbConnect(RMariaDB::MariaDB(), 
                  host = "localhost", 
                  user = "root", 
                  password = "", 
                  dbname = "eduvision")

# Get student info
student <- dbGetQuery(conn, sprintf("
    SELECT CONCAT(u.first_name, ' ', u.last_name) as name,
           s.student_number
    FROM students s
    JOIN users u ON u.id = s.user_id
    WHERE s.user_id = '%s'
", student_id))

# Get attendance data
attendance <- dbGetQuery(conn, sprintf("
    SELECT 
        c.code as course,
        wca.sessions_attended,
        wca.sessions_held,
        wca.attendance_rate,
        wca.status as weekly_status
    FROM weekly_course_attendance wca
    JOIN courses c ON c.id = wca.course_id
    WHERE wca.student_id = '%s'
", student_id))

# Get emotion data  
emotions <- dbGetQuery(conn, sprintf("
    SELECT emotion, COUNT(*) as count
    FROM student_emotion_snapshots
    WHERE student_id = '%s'
    GROUP BY emotion
    ORDER BY count DESC
", student_id))

# Get average concentration
concentration <- dbGetQuery(conn, sprintf("
    SELECT 
        AVG(CASE 
            WHEN concentration = 'high' THEN 0.9
            WHEN concentration = 'medium' THEN 0.6
            WHEN concentration = 'low' THEN 0.3
            ELSE 0.5
        END) as avg_concentration
    FROM student_emotion_snapshots
    WHERE student_id = '%s'
", student_id))

# Get negative emotion percentage
negative_emotions <- dbGetQuery(conn, sprintf("
    SELECT 
        SUM(CASE WHEN emotion IN ('sad', 'angry', 'fearful', 'confused') THEN 1 ELSE 0 END) as negative_count,
        COUNT(*) as total_count
    FROM student_emotion_snapshots
    WHERE student_id = '%s'
", student_id))

# NEW: Get presence/join-leave data for recent sessions
presence_data <- dbGetQuery(conn, sprintf("
    SELECT 
        ls.title as session_title,
        ls.scheduled_start,
        sa.joined_at,
        sa.left_at,
        sa.recorded_at as first_detected,
        COUNT(el.id) as times_away,
        SUM(COALESCE(el.exit_duration_minutes, 0)) as total_away_minutes,
        CASE 
            WHEN sa.left_at IS NULL THEN 'In Progress'
            WHEN sa.joined_at IS NOT NULL THEN 'Completed'
            ELSE 'No Data'
        END as session_status
    FROM session_attendance sa
    JOIN lecture_sessions ls ON ls.id = sa.session_id
    LEFT JOIN session_exit_logs el ON el.session_id = sa.session_id AND el.student_id = sa.student_id
    WHERE sa.student_id = '%s'
    GROUP BY ls.id, ls.title, ls.scheduled_start, sa.joined_at, sa.left_at, sa.recorded_at
    ORDER BY ls.scheduled_start DESC
    LIMIT 5
", student_id))

dbDisconnect(conn)

# Create output directory
dir.create("output/student", recursive = TRUE, showWarnings = FALSE)

# Calculate metrics
sessions_attended <- if(nrow(attendance) > 0) attendance$sessions_attended[1] else 0
is_present <- sessions_attended > 0

# Calculate engagement score
avg_conc <- if(nrow(concentration) > 0 && !is.na(concentration$avg_concentration)) concentration$avg_concentration else 0.5
negative_pct <- if(nrow(negative_emotions) > 0 && negative_emotions$total_count > 0) 
                  (negative_emotions$negative_count / negative_emotions$total_count) * 100 else 0

# Determine recommendations
if (!is_present) {
  recommendation_title <- "❌ Attendance Issue"
  recommendation_message <- "You did not attend any session this week. Please make sure to attend classes regularly."
  recommendation_type <- "warning"
} else if (avg_conc < 0.5) {
  recommendation_title <- "⚠️ Low Engagement Detected"
  recommendation_message <- "You attended classes but showed low concentration. Try these tips to improve focus."
  recommendation_type <- "warning"
} else if (negative_pct > 50) {
  recommendation_title <- "⚠️ High Negative Emotions"
  recommendation_message <- "You showed many negative emotions. Consider talking to a counselor or academic advisor."
  recommendation_type <- "warning"
} else {
  recommendation_title <- "✅ Good Performance"
  recommendation_message <- "You're doing well! Keep up the good work."
  recommendation_type <- "success"
}

# Emotion icons
emotion_icons <- c(
  "happy" = "😊",
  "sad" = "😔", 
  "angry" = "😠",
  "fearful" = "😨",
  "neutral" = "😐",
  "surprised" = "😮",
  "confused" = "🤔"
)

html_file <- "output/student/report.html"

html_content <- paste0(
'<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Student Report - ', student$name[1], '</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; background: #f5f5f5; }
        .container { max-width: 900px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        h1 { color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 10px; }
        h2 { color: #34495e; margin-top: 30px; }
        table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #3498db; color: white; }
        .bar-container { background-color: #f0f0f0; border-radius: 5px; overflow: hidden; }
        .bar { background-color: #3498db; height: 25px; color: white; line-height: 25px; padding-left: 10px; }
        .warning { background-color: #fff3cd; border-left: 4px solid #ffc107; padding: 15px; margin: 20px 0; border-radius: 5px; }
        .success { background-color: #d4edda; border-left: 4px solid #28a745; padding: 15px; margin: 20px 0; border-radius: 5px; }
        .info { background-color: #d1ecf1; border-left: 4px solid #17a2b8; padding: 15px; margin: 20px 0; border-radius: 5px; }
        .present { color: green; font-weight: bold; }
        .absent { color: red; font-weight: bold; }
        .excused { color: #d97706; font-weight: bold; }
        .footer { margin-top: 40px; text-align: center; color: #7f8c8d; font-size: 12px; border-top: 1px solid #eee; padding-top: 20px; }
        .metric { display: inline-block; margin: 10px; padding: 15px; background: #f8f9fa; border-radius: 8px; text-align: center; min-width: 120px; }
        .metric-value { font-size: 24px; font-weight: bold; color: #3498db; }
        .metric-label { font-size: 12px; color: #7f8c8d; margin-top: 5px; }
    </style>
</head>
<body>
<div class="container">

<h1>📊 Student Report</h1>

<h2>👤 Student Information</h2>
<p><strong>Name:</strong> ', student$name[1], '</p>
<p><strong>Student ID:</strong> ', student$student_number[1], '</p>

<h2>📚 Weekly Attendance Status</h2>
</tr>
    <tr><th>Course</th><th>Status</th><th>Sessions Attended</th><th>Total Sessions</th></tr>')

if (nrow(attendance) > 0) {
  for(i in 1:nrow(attendance)) {
    # Respect weekly_status (e.g. 'excused') and treat as excused even if sessions_attended == 0
    weekly_status <- tolower(as.character(ifelse(is.na(attendance$weekly_status[i]), "", attendance$weekly_status[i])))
    if (attendance$sessions_attended[i] > 0) {
      status_display <- '<span class="present">✅ PRESENT (attended at least once)</span>'
    } else if (weekly_status == 'excused') {
      status_display <- '<span class="excused">🟠 EXCUSED (approved absence)</span>'
    } else {
      status_display <- '<span class="absent">❌ ABSENT (no attendance this week)</span>'
    }
    html_content <- paste0(html_content, '
    <tr>
        <td>', attendance$course[i], '</td>
        <td>', status_display, '</td>
        <td>', attendance$sessions_attended[i], '</td>
        <td>', attendance$sessions_held[i], '</td>
    </tr>')
  }
} else {
  html_content <- paste0(html_content, '<tr><td colspan="4">No data available</td></tr>')
}

html_content <- paste0(html_content, '
</table>

<h2>⏱️ Session Join/Leave Times</h2>
<table>
    <thead>
        <tr><th>Session</th><th>Date</th><th>Join Time</th><th>Leave Time</th><th>Times Away</th><th>Away Minutes</th></tr>
    </thead>
    <tbody>')

if (nrow(presence_data) > 0) {
  for(i in 1:nrow(presence_data)) {
    join_time <- ifelse(is.na(presence_data$joined_at[i]), "N/A", 
                        format(as.POSIXct(presence_data$joined_at[i]), "%H:%M:%S"))
    leave_time <- ifelse(is.na(presence_data$left_at[i]), "Still Present", 
                         format(as.POSIXct(presence_data$left_at[i]), "%H:%M:%S"))
    html_content <- paste0(html_content, '
    <tr>
        <td>', presence_data$session_title[i], '</td>
        <td>', as.Date(presence_data$scheduled_start[i]), '</td>
        <td>', join_time, '</td>
        <td>', leave_time, '</td>
        <td>', presence_data$times_away[i], '</td>
        <td>', presence_data$total_away_minutes[i], ' min</td>
    </tr>')
  }
} else {
  html_content <- paste0(html_content, '<tr><td colspan="6">No session presence data available</td></tr>')
}

html_content <- paste0(html_content, '
    </tbody>
</td>

<h2>📈 Engagement Metrics</h2>
<div style="text-align: center; margin: 20px 0;">
    <div class="metric">
        <div class="metric-value">', round(avg_conc * 100), '%</div>
        <div class="metric-label">Average Concentration</div>
    </div>
    <div class="metric">
        <div class="metric-value">', round(negative_pct), '%</div>
        <div class="metric-label">Negative Emotions</div>
    </div>
</div>

<h2>😊 Emotion Distribution</h2>
<table>
    <thead>
        <tr><th>Emotion</th><th>Count</th><th>Distribution</th></tr>
    </thead>
    <tbody>')

if (nrow(emotions) > 0) {
  max_count <- max(emotions$count)
  for(i in 1:nrow(emotions)) {
    percent <- (emotions$count[i] / max_count) * 100
    icon <- ifelse(emotions$emotion[i] %in% names(emotion_icons), emotion_icons[emotions$emotion[i]], "😐")
    html_content <- paste0(html_content, '
    <tr>
        <td>', icon, ' ', emotions$emotion[i], '</td>
        <td>', emotions$count[i], '</td>
        <td>
            <div class="bar-container">
                <div class="bar" style="width: ', percent, '%;">', round(percent, 1), '%</div>
            </div>
         </td>
    </tr>')
  }
} else {
  html_content <- paste0(html_content, '<tr><td colspan="3">No data available</td></tr>')
}

html_content <- paste0(html_content, '
    </tbody>
</table>

<h2>💡 Recommendations</h2>
<div class="', recommendation_type, '">
    <strong>', recommendation_title, '</strong><br>
    ', recommendation_message, '
</div>')

if (avg_conc < 0.5) {
  html_content <- paste0(html_content, '
<h3>📖 Tips to Improve Engagement:</h3>
<ul>
    <li>🎯 Set specific goals before each study session</li>
    <li>📱 Remove distractions (phone, social media) during class</li>
    <li>✍️ Take active notes instead of just listening</li>
    <li>💡 Ask questions when you don\'t understand something</li>
    <li>🔄 Review material before class to stay engaged</li>
</ul>')
}

html_content <- paste0(html_content, '
<h3>General Study Tips:</h3>
<ul>
    <li>🧠 Take regular breaks using Pomodoro technique (25 min study, 5 min break)</li>
    <li>👥 Join study groups for collaborative learning</li>
    <li>📝 Practice active recall and spaced repetition</li>
    <li>😴 Get enough sleep (7-8 hours) for better concentration</li>
    <li>💧 Stay hydrated and take care of your physical health</li>
</ul>

<div class="footer">
    Report generated by EduVision System on ', Sys.Date(), '<br>
    <small>Based on weekly attendance, facial emotion analysis, and session presence tracking.</small>
</div>

</div>
</body>
</html>')

# Save HTML file
writeLines(html_content, html_file)
message("✅ Report generated for student: ", student$name[1])
message("📄 Report saved to: ", file.path(getwd(), html_file))