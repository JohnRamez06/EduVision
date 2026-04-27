# ============================================================
# student_search_tab.R — Per-student analysis module
# ============================================================

studentSearchTabUI <- function(id) {
  ns <- NS(id)
  fluidRow(
    box(width = 4, title = "Search Student", status = "primary", solidHeader = TRUE,
      textInput(ns("student_id"), "Student ID or Name", placeholder = "e.g. uuid or name"),
      selectInput(ns("session"), "Session (optional)", choices = c("All" = "")),
      actionButton(ns("search"), "Search", icon = icon("search"), class = "btn-primary")
    ),
    box(width = 8, title = "Student Profile", status = "info", solidHeader = TRUE,
      tableOutput(ns("profile_tbl"))
    ),
    box(width = 12, title = "Concentration & Emotion Over Time", status = "success",
        solidHeader = TRUE,
      plotlyOutput(ns("timeline_plot"), height = "350px")
    ),
    box(width = 6, title = "Day-of-Week Pattern", status = "warning", solidHeader = TRUE,
      plotlyOutput(ns("dow_plot"), height = "280px")
    ),
    box(width = 6, title = "Focus Decay", status = "danger", solidHeader = TRUE,
      plotlyOutput(ns("decay_plot"), height = "280px")
    )
  )
}

studentSearchTabServer <- function(id, available_sessions) {
  moduleServer(id, function(input, output, session) {
    observe({
      sess <- available_sessions()
      choices <- c("All" = "", setNames(sess$id, sess$label))
      updateSelectInput(session, "session", choices = choices)
    })

    student_info <- eventReactive(input$search, {
      req(nchar(trimws(input$student_id)) > 0)
      sid <- trimws(input$student_id)
      # Try direct ID first, else search by name
      result <- tryCatch(fetch_student(sid), error = function(e) data.frame())
      if (nrow(result) == 0) {
        result <- with_connection(function(con) {
          dbGetQuery(con, sqlInterpolate(con,
            "SELECT u.id, u.first_name, u.last_name,
                    CONCAT(u.first_name,' ',u.last_name) AS full_name,
                    u.email, s.student_number, s.program, s.year_of_study
               FROM users u JOIN students s ON s.user_id = u.id
              WHERE CONCAT(u.first_name,' ',u.last_name) LIKE ?q LIMIT 10",
            q = paste0("%", sid, "%")))
        })
      }
      result
    })

    individual <- eventReactive(input$search, {
      req(nrow(student_info()) > 0)
      source(file.path(ANALYTICS_HOME, "analysis", "student_individual_analysis.R"),
             local = TRUE)
      student_individual_analysis(student_info()$id[1])
    })

    output$profile_tbl <- renderTable({ student_info() })

    output$timeline_plot <- renderPlotly({
      ind <- individual()
      req(!is.null(ind$time_decay) && nrow(ind$time_decay) > 0)
      p <- ggplot(ind$time_decay, aes(x = bucket, y = avg_conc)) +
        geom_line(colour = "#42A5F5", linewidth = 1.2) +
        geom_point(colour = "#1565C0", size = 2) +
        scale_y_continuous(limits = c(0, 100)) +
        labs(x = "Minutes into Session", y = "Avg Concentration",
             title = "Concentration vs Time within Session") +
        theme_minimal()
      ggplotly(p)
    })

    output$dow_plot <- renderPlotly({
      ind <- individual()
      req(!is.null(ind$dow_stats) && nrow(ind$dow_stats) > 0)
      p <- ggplot(ind$dow_stats, aes(x = reorder(day_of_week, -avg_conc),
                                      y = avg_conc, fill = day_of_week)) +
        geom_col(show.legend = FALSE) +
        scale_fill_brewer(palette = "Set2") +
        labs(x = "Day", y = "Avg Concentration", title = "Best Days of Week") +
        theme_minimal()
      ggplotly(p)
    })

    output$decay_plot <- renderPlotly({
      ind <- individual()
      req(!is.null(ind$time_decay) && nrow(ind$time_decay) > 0)
      p <- ggplot(ind$time_decay, aes(x = bucket, y = avg_conc)) +
        geom_area(fill = "#BBDEFB", alpha = 0.5) +
        geom_line(colour = "#0D47A1", linewidth = 1) +
        {if (!is.na(ind$focus_drop_mins))
          geom_vline(xintercept = ind$focus_drop_mins, colour = "red",
                     linetype = "dashed") else NULL} +
        scale_y_continuous(limits = c(0, 100)) +
        labs(x = "Minutes in Session", y = "Concentration",
             title = sprintf("Focus Drop at ~%s min",
                             ifelse(is.na(ind$focus_drop_mins), "?",
                                    round(ind$focus_drop_mins)))) +
        theme_minimal()
      ggplotly(p)
    })
  })
}
