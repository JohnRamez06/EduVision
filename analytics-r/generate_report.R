# Simple R script to generate HTML report directly
library(DBI)
library(RMariaDB)
library(ggplot2)
library(dplyr)

# Database connection
conn <- dbConnect(RMariaDB::MariaDB(), 
                  host = "localhost", 
                  user = "root", 
                  password = "", 
                  dbname = "eduvision")

student_id <- "st0020-0000-0000-0000-000000000001"

# Get student info
student_info <- dbGetQuery(conn, sprintf("
    SELECT 
        s.student_number,
        CONCAT(u.first_name, ' ', u.last_name) as student_name,
        COALESCE(s.program, 'Not Specified') as program,
        COALESCE(s.year_of_study, 1) as year_of_study
    FROM students s
    JOIN users u ON u.id = s.user_id
    WHERE s.user_id = '%s'
", student_id))

# Get attendance data
attendance <- dbGetQuery(conn, sprintf("
    SELECT 
        c.code as Course,
        wca.sessions_held as Sessions_Held,
        wca.sessions_attended as Attended,
        wca.attendance_rate as Rate,
        wca.status as Status
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
", student_id))

# Get concentration data
concentration <- dbGetQuery(conn, sprintf("
    SELECT 
        DATE(captured_at) as date,
        AVG(CASE 
            WHEN concentration = 'high' THEN 0.9
            WHEN concentration = 'medium' THEN 0.6
            WHEN concentration = 'low' THEN 0.3
            ELSE 0.5
        END) as avg_concentration
    FROM student_emotion_snapshots
    WHERE student_id = '%s'
    GROUP BY DATE(captured_at)
    ORDER BY date
", student_id))

dbDisconnect(conn)

# Create HTML output
html_file <- sprintf("C:/Users/john/Desktop/eduvision/analytics-r/output/student/student_report_%s.html", 
                     student_info$student_number[1])

# Start building HTML
html <- '<!DOCTYPE html>
<html>
<head>
<title>Student Weekly Report</title>
<style>
body { font-family: Arial, sans-serif; margin: 40px; line-height: 1.6; }
h1 { color: #2c3e50; border-bottom: 2px solid #3498db; padding-bottom: 10px; }
h2 { color: #34495e; margin-top: 30px; }
table { border-collapse: collapse; width: 100%; margin: 20px 0; }
th, td { border: 1px solid #ddd; padding: 10px; text-align: left; }
th { background-color: #3498db; color: white; }
.chart { margin: 30px 0; padding: 20px; background-color: #f9f9f9; border-radius: 5px; }
.recommendation { background-color: #e8f4f8; padding: 15px; border-left: 4px solid #3498db; margin: 20px 0; }
.footer { margin-top: 50px; text-align: center; color: #7f8c8d; font-size: 12px; }
</style>
</head>
<body>

<h1>Student Weekly Performance Report</h1>

<h2>Student Information</h2>
<table>
<tr><th>Field</th><th>Value</th></tr>
<tr><td>Name</td><td>' , student_info$student_name[1], '</td></tr>
<tr><td>Student ID</td><td>' , student_info$student_number[1], '</td></tr>
<tr><td>Program</td><td>' , student_info$program[1], '</td></tr>
<tr><td>Year of Study</td><td>' , student_info$year_of_study[1], '</td></tr>
</table>

<h2>1. Attendance Summary</h2>
'

if (nrow(attendance) > 0) {
  html <- paste0(html, '<table>
<tr><th>Course</th><th>Sessions Held</th><th>Attended</th><th>Rate (%)</th><th>Status</th></tr>')
  for (i in 1:nrow(attendance)) {
    html <- paste0(html, '<tr>')
    html <- paste0(html, '<td>', attendance$Course[i], '</td>')
    html <- paste0(html, '<td>', attendance$Sessions_Held[i], '</td>')
    html <- paste0(html, '<td>', attendance$Attended[i], '</td>')
    html <- paste0(html, '<td>', round(attendance$Rate[i], 1), '</td>')
    html <- paste0(html, '<td>', attendance$Status[i], '</td>')
    html <- paste0(html, '</tr>')
  }
  html <- paste0(html, '</table>')
} else {
  html <- paste0(html, '<p>No attendance records found.</p>')
}

html <- paste0(html, '

<h2>2. Emotion Distribution</h2>
<div class="chart">
')

if (nrow(emotions) > 0) {
  # Create bar chart as text
  html <- paste0(html, '<table>
<tr><th>Emotion</th><th>Count</th><th>Visual</th></tr>')
  max_count <- max(emotions$count)
  for (i in 1:nrow(emotions)) {
    bar_width <- (emotions$count[i] / max_count) * 30
    bar <- paste(rep("█", round(bar_width)), collapse = "")
    html <- paste0(html, '<tr>')
    html <- paste0(html, '<td>', emotions$emotion[i], '</td>')
    html <- paste0(html, '<td>', emotions$count[i], '</td>')
    html <- paste0(html, '<td><span style="color:#3498db;">', bar, '</span></td>')
    html <- paste0(html, '</tr>')
  }
  html <- paste0(html, '</table>')
} else {
  html <- paste0(html, '<p>No emotion data available.</p>')
}

html <- paste0(html, '
</div>

<h2>3. Concentration Analysis</h2>
<div class="chart">
')

if (nrow(concentration) > 0) {
  html <- paste0(html, '<table>
<tr><th>Date</th><th>Average Concentration</th><th>Visual</th></tr>')
  for (i in 1:nrow(concentration)) {
    bar_width <- concentration$avg_concentration[i] * 30
    bar <- paste(rep("█", round(bar_width)), collapse = "")
    level <- ifelse(concentration$avg_concentration[i] >= 0.7, "High", 
                   ifelse(concentration$avg_concentration[i] >= 0.4, "Medium", "Low"))
    html <- paste0(html, '<tr>')
    html <- paste0(html, '<td>', concentration$date[i], '</td>')
    html <- paste0(html, '<td>', round(concentration$avg_concentration[i] * 100, 1), '%</td>')
    html <- paste0(html, '<td><span style="color:#27ae60;">', bar, '</span> ', level, '</td>')
    html <- paste0(html, '</tr>')
  }
  html <- paste0(html, '</table>')
} else {
  html <- paste0(html, '<p>No concentration data available.</p>')
}

# Recommendations
html <- paste0(html, '
</div>

<h2>4. Recommendations</h2>
<div class="recommendation">
')

if (nrow(attendance) > 0) {
  avg_rate <- mean(attendance$Rate, na.rm = TRUE)
  if (avg_rate >= 80) {
    html <- paste0(html, '<p>✅ <strong>Excellent attendance!</strong> Keep up the good work.</p>')
  } else if (avg_rate >= 60) {
    html <- paste0(html, '<p>⚠️ <strong>Your attendance needs improvement.</strong> Aim for 80% or higher.</p>')
  } else {
    html <- paste0(html, '<p>❌ <strong>Low attendance detected.</strong> Please contact your academic advisor.</p>')
  }
}

html <- paste0(html, '
<h3>Study Tips:</h3>
<ul>
<li>📖 Review lecture materials before coming to class</li>
<li>🧠 Take regular breaks to maintain concentration</li>
<li>🗣️ Participate actively in class discussions</li>
<li>👥 Join study groups for collaborative learning</li>
</ul>
</div>

<div class="footer">
Report generated by EduVision System on ', Sys.Date(), '
</div>

</body>
</html>')

# Save HTML file
writeLines(html, html_file)
message(sprintf("✅ Report generated: %s", html_file))

# Open in browser
browseURL(html_file)