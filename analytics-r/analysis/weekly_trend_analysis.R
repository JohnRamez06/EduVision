# C:/Users/john/Desktop/eduvision/analytics-r/analysis/weekly_trend_analysis.R

source("config.R")

trend_analysis <- function() {
  conn <- get_connection()
  on.exit(dbDisconnect(conn))
  
  # Get weekly trends
  trends <- dbGetQuery(conn, "
    SELECT 
      wp.week_number,
      wp.year,
      AVG(wca.attendance_rate) as avg_attendance,
      SUM(CASE WHEN wca.status = 'regular' THEN 1 ELSE 0 END) as present_count,
      COUNT(*) as total_count
    FROM weekly_course_attendance wca
    JOIN weekly_periods wp ON wp.id = wca.week_id
    GROUP BY wp.week_number, wp.year
    ORDER BY wp.year DESC, wp.week_number DESC
  ")
  
  print(trends)
  
  # Save to CSV
  write.csv(trends, "output/weekly_trends.csv", row.names = FALSE)
  message("✅ Trend analysis saved to output/weekly_trends.csv")
}

trend_analysis()