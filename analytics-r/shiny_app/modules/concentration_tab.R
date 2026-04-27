# ============================================================
# concentration_tab.R — Concentration distribution module
# ============================================================

concentrationTabUI <- function(id) {
  ns <- NS(id)
  fluidRow(
    box(width = 3, title = "Controls", status = "primary", solidHeader = TRUE,
      selectInput(ns("session"), "Select Session", choices = NULL),
      actionButton(ns("go"), "Analyse", icon = icon("play"), class = "btn-primary")
    ),
    box(width = 9, title = "Concentration Distribution", status = "info",
        solidHeader = TRUE,
      plotlyOutput(ns("dist_plot"), height = "320px")
    ),
    box(width = 12, title = "Low-Concentration Periods", status = "warning",
        solidHeader = TRUE,
      tableOutput(ns("low_tbl"))
    )
  )
}

concentrationTabServer <- function(id, available_sessions) {
  moduleServer(id, function(input, output, session) {
    observe({
      sess <- available_sessions()
      updateSelectInput(session, "session",
                        choices = setNames(sess$id, sess$label))
    })

    ca_data <- eventReactive(input$go, {
      req(input$session)
      concentration_analysis(input$session)
    })

    output$dist_plot <- renderPlotly({
      cd <- ca_data()
      req(length(cd$distribution) > 0)
      df <- as.data.frame(cd$distribution)
      names(df) <- c("Level", "Count")
      p <- ggplot(df, aes(x = Level, y = Count, fill = Level)) +
        geom_col(show.legend = FALSE) +
        scale_fill_manual(values = c(
          high       = "#66BB6A",
          medium     = "#FFA726",
          low        = "#EF5350",
          distracted = "#B71C1C")) +
        labs(title = "Concentration Level Distribution",
             x = "Level", y = "Count") +
        theme_minimal()
      ggplotly(p)
    })

    output$low_tbl <- renderTable({
      lp <- ca_data()$low_periods
      if (nrow(lp) == 0) return(data.frame(Message = "No low-concentration periods."))
      lp
    })
  })
}
