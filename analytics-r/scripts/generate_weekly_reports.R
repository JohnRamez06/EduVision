# analytics-r/scripts/generate_weekly_reports.R

library(DBI)
library(RMySQL)
library(ggplot2)
library(rmarkdown)

# Database connection
con <- dbConnect(RMySQL::MySQL(),
                 host = "localhost",
                 user = "root",
                 password = "",
                 dbname = "eduvision")

# Get current week data
current_week <- dbGetQuery(con, "
    SELECT wp.id as period_id, wp.start_date, wp.end_date, wp.week_name
    FROM weekly_periods wp
    WHERE wp.start_date <= CURDATE() AND wp.end_date >= CURDATE()
    LIMIT 1
")

if(nrow(current_week) > 0) {
    period_id <- current_week$period_id[1]
    
    # Lecturer report
    lecturer_report <- dbGetQuery(con, sprintf("
        SELECT 
            l.user_id as lecturer_id,
            CONCAT(lu.first_name, ' ', lu.last_name) as lecturer_name,
            c.course_name,
            COUNT(DISTINCT cs.student_id) as total_students,
            SUM(CASE WHEN wca.attendance_status = 'present' THEN 1 ELSE 0 END) as present_count,
            SUM(CASE WHEN wca.attendance_status = 'absent' THEN 1 ELSE 0 END) as absent_count,
            ROUND(SUM(CASE WHEN wca.attendance_status = 'present' THEN 1 ELSE 0 END) * 100.0 / COUNT(DISTINCT cs.student_id), 1) as attendance_percentage
        FROM weekly_course_attendance wca
        JOIN course_students cs ON cs.student_id = wca.student_id AND cs.course_id = wca.course_id
        JOIN lecturers l ON l.user_id = wca.lecturer_id
        JOIN users lu ON lu.id = l.user_id
        JOIN courses c ON c.id = wca.course_id
        WHERE wca.period_id = %d
        GROUP BY l.user_id, c.id
        ORDER BY attendance_percentage ASC
    ", period_id))
    
    # Student report (students with 2+ consecutive absences)
    at_risk_students <- dbGetQuery(con, sprintf("
        SELECT 
            s.user_id as student_id,
            CONCAT(u.first_name, ' ', u.last_name) as student_name,
            c.course_name,
            cs.consecutive_absences,
            cs.total_sessions_attended
        FROM course_students cs
        JOIN students s ON s.user_id = cs.student_id
        JOIN users u ON u.id = s.user_id
        JOIN courses c ON c.id = cs.course_id
        WHERE cs.consecutive_absences >= 2
        ORDER BY cs.consecutive_absences DESC
    "))
    
    # Save reports
    saveRDS(lecturer_report, sprintf("analytics-r/output/lecturer/weekly_report_%d.rds", period_id))
    saveRDS(at_risk_students, sprintf("analytics-r/output/at_risk_students_%d.rds", period_id))
    
    # Generate CSV exports
    write.csv(lecturer_report, sprintf("analytics-r/output/lecturer_attendance_week_%d.csv", period_id), row.names = FALSE)
    write.csv(at_risk_students, sprintf("analytics-r/output/at_risk_students_week_%d.csv", period_id), row.names = FALSE)
    
    cat(sprintf("✅ Reports generated for week %d\n", period_id))
    cat(sprintf("   - %d lecturers with attendance data\n", nrow(lecturer_report)))
    cat(sprintf("   - %d students at risk (2+ weeks absent)\n", nrow(at_risk_students)))
}

dbDisconnect(con)