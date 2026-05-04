# Dean Report - Lecturer Performance & Student Satisfaction
args <- commandArgs(trailingOnly = TRUE)
department <- if (length(args) > 0) args[1] else "Computer Science"

library(DBI)
library(RMariaDB)
library(ggplot2)

conn <- dbConnect(RMariaDB::MariaDB(), 
                  host = "localhost", 
                  user = "root", 
                  password = "", 
                  dbname = "eduvision")

# Get all lecturers and their courses
lecturers <- dbGetQuery(conn, sprintf("
    SELECT 
        l.user_id,
        CONCAT(u.first_name, ' ', u.last_name) as name,
        u.email,
        COUNT(DISTINCT cl.course_id) as courses_taught,
        GROUP_CONCAT(DISTINCT c.code) as course_codes
    FROM lecturers l
    JOIN users u ON u.id = l.user_id
    JOIN course_lecturers cl ON cl.lecturer_id = l.user_id
    JOIN courses c ON c.id = cl.course_id
    WHERE c.department = '%s'
    GROUP BY l.user_id
", department))

if (nrow(lecturers) == 0) {
  stop("No lecturers found in department: ", department)
}

# For each lecturer, calculate student engagement metrics
lecturer_metrics <- data.frame()

for(i in 1:nrow(lecturers)) {
  lecturer_id <- lecturers$user_id[i]
  
  # Get student emotions from sessions taught by this lecturer
  emotions <- dbGetQuery(conn, sprintf("
    SELECT 
        ses.emotion,
        COUNT(*) as count
    FROM student_emotion_snapshots ses
    JOIN lecture_sessions ls ON ls.id = ses.session_id
    WHERE ls.lecturer_id = '%s'
    GROUP BY ses.emotion
  ", lecturer_id))
  
  # Calculate positive vs negative emotion ratio
  positive <- c("happy", "surprised", "engaged")
  negative <- c("sad", "angry", "fearful", "confused")
  
  total_emotions <- sum(emotions$count)
  positive_count <- sum(emotions$count[emotions$emotion %in% positive])
  negative_count <- sum(emotions$count[emotions$emotion %in% negative])
  
  sentiment_score <- if(total_emotions > 0) (positive_count / total_emotions) * 100 else 0
  
  # Get average concentration
  concentration <- dbGetQuery(conn, sprintf("
    SELECT 
        AVG(CASE 
            WHEN concentration = 'high' THEN 0.9
            WHEN concentration = 'medium' THEN 0.6
            WHEN concentration = 'low' THEN 0.3
            ELSE 0.5
        END) as avg_concentration
    FROM student_emotion_snapshots ses
    JOIN lecture_sessions ls ON ls.id = ses.session_id
    WHERE ls.lecturer_id = '%s'
  ", lecturer_id))
  
  # Get attendance rates for courses taught
  attendance <- dbGetQuery(conn, sprintf("
    SELECT 
        AVG(wca.attendance_rate) as avg_attendance
    FROM weekly_course_attendance wca
    JOIN courses c ON c.id = wca.course_id
    JOIN course_lecturers cl ON cl.course_id = c.id
    WHERE cl.lecturer_id = '%s'
  ", lecturer_id))
  
  # Get total sessions taught
  sessions_taught <- dbGetQuery(conn, sprintf("
    SELECT 
        COUNT(*) as total_sessions,
        SUM(CASE WHEN status = 'completed' THEN 1 ELSE 0 END) as completed_sessions
    FROM lecture_sessions
    WHERE lecturer_id = '%s'
  ", lecturer_id))
  
  # Determine performance rating
  rating <- "Average"
  if(sentiment_score >= 70 && concentration$avg_concentration[1] > 0.6) {
    rating <- "Excellent ⭐"
  } else if(sentiment_score <= 40 || concentration$avg_concentration[1] < 0.4) {
    rating <- "Needs Improvement ⚠️"
  } else {
    rating <- "Good ✓"
  }
  
  lecturer_metrics <- rbind(lecturer_metrics, data.frame(
    Lecturer = lecturers$name[i],
    Email = lecturers$email[i],
    Courses = lecturers$course_codes[i],
    Student_Sentiment = round(sentiment_score, 1),
    Positive_Emotions = positive_count,
    Negative_Emotions = negative_count,
    Avg_Concentration = round(concentration$avg_concentration[1] * 100, 1),
    Avg_Attendance = round(attendance$avg_attendance[1], 1),
    Total_Sessions = sessions_taught$total_sessions[i],
    Completed_Sessions = sessions_taught$completed_sessions[i],
    Performance_Rating = rating,
    stringsAsFactors = FALSE
  ))
}

dbDisconnect(conn)

# Create output directory
dir.create("output/dean", recursive = TRUE, showWarnings = FALSE)

# Generate HTML report
html_file <- "output/dean/lecturer_performance_report.html"

html_content <- paste0(
'<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Dean Report - Lecturer Performance</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 40px; background: #f5f5f5; }
        .container { max-width: 1200px; margin: 0 auto; background: white; padding: 30px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
        h1 { color: #2c3e50; border-bottom: 3px solid #3498db; padding-bottom: 10px; }
        h2 { color: #34495e; margin-top: 30px; }
        table { width: 100%; border-collapse: collapse; margin: 20px 0; }
        th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }
        th { background-color: #3498db; color: white; position: sticky; top: 0; }
        .excellent { background-color: #d4edda; color: #155724; font-weight: bold; }
        .good { background-color: #d1ecf1; color: #0c5460; font-weight: bold; }
        .needs-improvement { background-color: #f8d7da; color: #721c24; font-weight: bold; }
        .rating-badge { display: inline-block; padding: 5px 10px; border-radius: 20px; font-size: 12px; }
        .positive { color: green; font-weight: bold; }
        .negative { color: red; }
        .chart-container { margin: 30px 0; text-align: center; }
        .summary-card { display: inline-block; margin: 10px; padding: 20px; background: #f8f9fa; border-radius: 10px; text-align: center; min-width: 180px; }
        .summary-value { font-size: 32px; font-weight: bold; color: #3498db; }
        .summary-label { font-size: 12px; color: #7f8c8d; margin-top: 5px; }
        .footer { margin-top: 40px; text-align: center; color: #7f8c8d; font-size: 12px; border-top: 1px solid #eee; padding-top: 20px; }
    </style>
</head>
<body>
<div class="container">

<h1>📊 Dean Report: Lecturer Performance & Student Satisfaction</h1>
<p><strong>Department:</strong> ', department, '</p>
<p><strong>Report Date:</strong> ', Sys.Date(), '</p>

<h2>📈 Department Summary</h2>
<div style="text-align: center;">
    <div class="summary-card">
        <div class="summary-value">', nrow(lecturer_metrics), '</div>
        <div class="summary-label">Total Lecturers</div>
    </div>
    <div class="summary-card">
        <div class="summary-value">', round(mean(lecturer_metrics$Student_Sentiment), 1), '%</div>
        <div class="summary-label">Avg Student Sentiment</div>
    </div>
    <div class="summary-card">
        <div class="summary-value">', round(mean(lecturer_metrics$Avg_Concentration), 1), '%</div>
        <div class="summary-label">Avg Concentration</div>
    </div>
    <div class="summary-card">
        <div class="summary-value">', round(mean(lecturer_metrics$Avg_Attendance), 1), '%</div>
        <div class="summary-label">Avg Attendance</div>
    </div>
</div>

<h2>👨‍🏫 Lecturer Performance Table</h2>
<table>
    <thead>
        <tr>
            <th>Lecturer</th>
            <th>Courses</th>
            <th>Student Sentiment</th>
            <th>Positive vs Negative</th>
            <th>Concentration</th>
            <th>Attendance</th>
            <th>Sessions</th>
            <th>Rating</th>
        </tr>
    </thead>
    <tbody>')

for(i in 1:nrow(lecturer_metrics)) {
  # Determine rating class
  rating_class <- ifelse(lecturer_metrics$Performance_Rating[i] == "Excellent ⭐", "excellent",
                  ifelse(lecturer_metrics$Performance_Rating[i] == "Good ✓", "good", "needs-improvement"))
  
  # Sentiment color
  sentiment_color <- if(lecturer_metrics$Student_Sentiment[i] >= 70) "positive" else if(lecturer_metrics$Student_Sentiment[i] <= 40) "negative" else ""
  
  html_content <- paste0(html_content, '
        <tr>
            <td><strong>', lecturer_metrics$Lecturer[i], '</strong><br><small style="color:#888;">', lecturer_metrics$Email[i], '</small></td>
            <td>', lecturer_metrics$Courses[i], '</td>
            <td class="', sentiment_color, '">', lecturer_metrics$Student_Sentiment[i], '%</td>
            <td>👍 ', lecturer_metrics$Positive_Emotions[i], ' / 👎 ', lecturer_metrics$Negative_Emotions[i], '</td>
            <td>', lecturer_metrics$Avg_Concentration[i], '%</td>
            <td>', lecturer_metrics$Avg_Attendance[i], '%</td>
            <td>', lecturer_metrics$Completed_Sessions[i], '/', lecturer_metrics$Total_Sessions[i], '</td>
            <td class="', rating_class, '">', lecturer_metrics$Performance_Rating[i], '</td>
        </tr>')
}

html_content <- paste0(html_content, '
    </tbody>
</table>

<h2>📊 Performance Insights</h2>
')

# Identify top and bottom performers
top_performer <- lecturer_metrics[which.max(lecturer_metrics$Student_Sentiment), ]
bottom_performer <- lecturer_metrics[which.min(lecturer_metrics$Student_Sentiment), ]

html_content <- paste0(html_content, '
<div style="display: flex; gap: 20px; margin: 20px 0;">
    <div style="flex: 1; padding: 15px; background: #d4edda; border-radius: 10px;">
        <h3>🏆 Best Performer</h3>
        <p><strong>', top_performer$Lecturer[1], '</strong></p>
        <p>Student Sentiment: ', top_performer$Student_Sentiment[1], '%<br>
        Concentration: ', top_performer$Avg_Concentration[1], '%<br>
        Rating: ', top_performer$Performance_Rating[1], '</p>
    </div>
    <div style="flex: 1; padding: 15px; background: #f8d7da; border-radius: 10px;">
        <h3>⚠️ Needs Attention</h3>
        <p><strong>', bottom_performer$Lecturer[1], '</strong></p>
        <p>Student Sentiment: ', bottom_performer$Student_Sentiment[1], '%<br>
        Concentration: ', bottom_performer$Avg_Concentration[1], '%<br>
        Rating: ', bottom_performer$Performance_Rating[1], '</p>
    </div>
</div>

<h2>💡 Recommendations</h2>
<ul>
')

# Generate recommendations
if(any(lecturer_metrics$Student_Sentiment < 50)) {
  html_content <- paste0(html_content, '<li>⚠️ Some lecturers have low student sentiment. Consider reviewing teaching methods or providing additional training.</li>\n')
}
if(any(lecturer_metrics$Avg_Concentration < 40)) {
  html_content <- paste0(html_content, '<li>📚 Low concentration levels detected in some classes. Suggest incorporating interactive activities.</li>\n')
}
if(any(lecturer_metrics$Avg_Attendance < 60)) {
  html_content <- paste0(html_content, '<li>📅 Low attendance rates identified. Review scheduling or implement attendance incentives.</li>\n')
}
if(any(lecturer_metrics$Positive_Emotions < lecturer_metrics$Negative_Emotions)) {
  html_content <- paste0(html_content, '<li>😔 Negative emotions outweigh positive ones in some classes. Check student feedback and course difficulty.</li>\n')
}

html_content <- paste0(html_content, '
</ul>

<div class="footer">
    Report generated by EduVision System on ', Sys.Date(), '<br>
    <small>Metrics based on student emotion analysis, concentration tracking, and attendance records.</small>
</div>

</div>
</body>
</html>')

writeLines(html_content, html_file)
message("✅ Dean report generated: ", file.path(getwd(), html_file))
browseURL(html_file)