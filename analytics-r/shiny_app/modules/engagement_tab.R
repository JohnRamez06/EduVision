# ============================================================
# engagement_tab.R — Engagement time-series module
# ============================================================

engagementTabUI <- function(id) {
  ns <- NS(id)
  fluidRow(
    box(width = 3, title = "Controls", status = "primary", solidHeader = TRUE,
      selectInput(ns("session"), "Select Session", choices = NULL),
      sliderInput(ns("smooth_k"), "Smoothing Window", min = 1, max = 20, value = 5),
      actionButton(ns("refresh"), "Refresh", icon = icon("sync"), class = "btn-primary")
    ),
    box(width = 9, title = "Engagement Timeline", status = "info", solidHeader = TRUE,
      plotlyOutput(ns("eng_plot"), height = "350px")
    ),
    box(width = 6, title = "Summary Stats", status = "success", solidHeader = TRUE,
      tableOutput(ns("stats_tbl"))
    ),
    box(width = 6, title = "Change Points", status = "warning", solidHeader = TRUE,
      verbatimTextOutput(ns("change_points"))
    )
  )
}

engagementTabServer <- function(id, available_sessions) {
  moduleServer(id, function(input, output, session) {
    observe({
      sess <- available_sessions()
      choices <- setNames(sess$id, sess$label)
      updateSelectInput(session, "session", choices = choices)
    })

    eng_data <- eventReactive(c(input$refresh, input$session), {
      req(input$session)
      engagement_analysis(input$session, k = input$smooth_k)
    })

    output$eng_plot <- renderPlotly({
      ed <- eng_data()
      req(nrow(ed$series) > 0)
      p <- ggplot(ed$series, aes(x = captured_at)) +
        geom_line(aes(y = engagement_val), colour = "#90CAF9", linewidth = 0.6) +
        geom_line(aes(y = smooth_eng),     colour = "#1565C0", linewidth = 1.2) +
        scale_y_continuous(limits = c(0, 1), labels = scales::percent) +
        labs(x = "Time", y = "Engagement",
             title = "Class Engagement (blue = smoothed)") +
        theme_minimal()
      # Highlight drop points
      if (length(ed$drops) > 0) {
        drop_df <- ed$series[ed$drops, ]
        p <- p + geom_point(data = drop_df,
                            aes(x = captured_at, y = engagement_val),
                            colour = "red", size = 3)
      }
      ggplotly(p)
    })

    output$stats_tbl <- renderTable({
      s <- eng_data()$stats
      data.frame(
        Metric = c("N", "Mean", "Median", "Std Dev", "Min", "Max"),
        Value  = round(c(s$n, s$mean, s$median, s$sd, s$min, s$max), 3),
        stringsAsFactors = FALSE
      )
    })

    output$change_points <- renderText({
      source(file.path(ANALYTICS_HOME, "analysis", "change_point_detection.R"),
             local = TRUE)
      cp <- change_point_detection(input$session)
      if (length(cp$changepoints) == 0) "No change points detected." else
        paste("Change points at:\n", paste(cp$changepoints, collapse = "\n"))
    })
  })
}
