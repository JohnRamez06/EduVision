# ============================================================
# lecturer_comparison_tab.R — Side-by-side lecturer comparison module
# ============================================================

lecturerComparisonTabUI <- function(id) {
  ns <- NS(id)
  fluidRow(
    box(width = 4, title = "Select Lecturers", status = "primary", solidHeader = TRUE,
      uiOutput(ns("lecturer_picker")),
      actionButton(ns("compare"), "Compare", icon = icon("balance-scale"),
                   class = "btn-primary")
    ),
    box(width = 8, title = "Engagement Comparison", status = "info", solidHeader = TRUE,
      plotlyOutput(ns("eng_plot"), height = "320px")
    ),
    box(width = 6, title = "Concentration Comparison", status = "success",
        solidHeader = TRUE,
      plotlyOutput(ns("conc_plot"), height = "280px")
    ),
    box(width = 6, title = "Sessions Summary", status = "warning", solidHeader = TRUE,
      tableOutput(ns("summary_tbl"))
    )
  )
}

lecturerComparisonTabServer <- function(id, available_lecturers, available_courses) {
  moduleServer(id, function(input, output, session) {
    output$lecturer_picker <- renderUI({
      lects <- available_lecturers()
      ns    <- NS(id)
      checkboxGroupInput(ns("selected_lecturers"), "Lecturers",
                         choices  = setNames(lects$id, lects$label),
                         selected = head(lects$id, 3))
    })

    comp <- eventReactive(input$compare, {
      req(length(input$selected_lecturers) >= 2)
      compare_lecturers(input$selected_lecturers)
    })

    output$eng_plot <- renderPlotly({
      df <- comp()
      req(nrow(df) > 0)
      p <- ggplot(df, aes(x = reorder(lecturer_name, avg_engagement),
                          y = avg_engagement, fill = lecturer_name)) +
        geom_col(show.legend = FALSE) +
        scale_fill_brewer(palette = "Set1") +
        scale_y_continuous(limits = c(0, 1), labels = scales::percent) +
        coord_flip() +
        labs(x = "", y = "Avg Engagement", title = "Engagement by Lecturer") +
        theme_minimal()
      ggplotly(p)
    })

    output$conc_plot <- renderPlotly({
      df <- comp()
      req(nrow(df) > 0)
      p <- ggplot(df, aes(x = reorder(lecturer_name, avg_concentration),
                          y = avg_concentration, fill = lecturer_name)) +
        geom_col(show.legend = FALSE) +
        scale_fill_brewer(palette = "Set2") +
        coord_flip() +
        labs(x = "", y = "Avg Concentration", title = "Concentration by Lecturer") +
        theme_minimal()
      ggplotly(p)
    })

    output$summary_tbl <- renderTable({
      df <- comp()
      if (nrow(df) == 0) return(data.frame(Message = "No data."))
      df[, c("lecturer_name","n_sessions","total_students","avg_engagement")]
    })
  })
}
