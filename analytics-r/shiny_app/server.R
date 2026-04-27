# ============================================================
# server.R — Shiny dashboard server
# ============================================================

server <- function(input, output, session) {

  # ----------------------------------------------------------
  # Shared reactive: fetch sessions for a course
  # ----------------------------------------------------------
  available_sessions <- reactive({
    with_connection(function(con) {
      dbGetQuery(con,
        "SELECT ls.id, CONCAT(c.code,' — ',ls.title) AS label,
                ls.actual_start
           FROM lecture_sessions ls
           JOIN courses c ON c.id = ls.course_id
          WHERE ls.status = 'completed'
          ORDER BY ls.actual_start DESC
          LIMIT 200")
    })
  })

  available_courses <- reactive({
    with_connection(function(con) {
      dbGetQuery(con, "SELECT id, CONCAT(code,' – ',title) AS label FROM courses ORDER BY code")
    })
  })

  available_lecturers <- reactive({
    with_connection(function(con) {
      dbGetQuery(con,
        "SELECT DISTINCT ls.lecturer_id AS id,
                CONCAT(u.first_name,' ',u.last_name) AS label
           FROM lecture_sessions ls
           JOIN users u ON u.id = ls.lecturer_id
          ORDER BY label")
    })
  })

  weekly_periods <- reactive({
    with_connection(function(con) {
      dbGetQuery(con,
        "SELECT id, CONCAT('Week ',week_number,' /',year,' (',start_date,')') AS label
           FROM weekly_periods ORDER BY year DESC, week_number DESC LIMIT 20")
    })
  })

  model_info <- reactive({
    with_connection(function(con) {
      dbGetQuery(con,
        "SELECT model_type, version_tag, accuracy, total_faces,
                last_trained_at, is_active
           FROM model_versions ORDER BY last_trained_at DESC")
    })
  })

  # ----------------------------------------------------------
  # Call module servers
  # ----------------------------------------------------------
  engagementTabServer("eng",         available_sessions)
  concentrationTabServer("conc",     available_sessions)
  studentSearchTabServer("stu",      available_sessions)
  lecturerComparisonTabServer("lect",available_lecturers, available_courses)
  weeklyReportTabServer("weekly",    weekly_periods, available_courses, available_lecturers)
  modelStatusTabServer("model",      model_info)
}
