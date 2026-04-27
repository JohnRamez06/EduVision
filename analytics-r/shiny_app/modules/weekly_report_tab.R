# ============================================================
# weekly_report_tab.R — Generate weekly PDF reports from the UI
# ============================================================

weeklyReportTabUI <- function(id) {
  ns <- NS(id)
  fluidRow(
    box(width = 4, title = "Report Settings", status = "primary", solidHeader = TRUE,
      selectInput(ns("report_type"), "Report Type",
                  choices = c("Student Weekly"   = "student",
                              "Lecturer Weekly"  = "lecturer",
                              "Dean Weekly"      = "dean")),
      uiOutput(ns("entity_picker")),
      uiOutput(ns("week_picker")),
      actionButton(ns("generate"), "Generate PDF", icon = icon("file-pdf"),
                   class = "btn-danger")
    ),
    box(width = 8, title = "Generation Status", status = "info", solidHeader = TRUE,
      verbatimTextOutput(ns("status")),
      uiOutput(ns("download_link"))
    )
  )
}

weeklyReportTabServer <- function(id, weekly_periods, available_courses,
                                   available_lecturers) {
  moduleServer(id, function(input, output, session) {
    output$week_picker <- renderUI({
      ns  <- NS(id)
      wps <- weekly_periods()
      selectInput(ns("week_id"), "Week",
                  choices = setNames(wps$id, wps$label))
    })

    output$entity_picker <- renderUI({
      ns <- NS(id)
      switch(input$report_type,
        student  = textInput(ns("entity_id"), "Student ID"),
        lecturer = {
          lects <- available_lecturers()
          selectInput(ns("entity_id"), "Lecturer",
                      choices = setNames(lects$id, lects$label))
        },
        dean     = textInput(ns("entity_id"), "Dean User ID")
      )
    })

    report_path <- reactiveVal(NULL)

    observeEvent(input$generate, {
      req(input$entity_id, input$week_id)
      output$status <- renderText("Generating report, please wait...")
      report_path(NULL)

      gen_dir <- file.path(ANALYTICS_HOME, "generators")
      script  <- switch(input$report_type,
                   student  = file.path(gen_dir, "generate_student_weekly.R"),
                   lecturer = file.path(gen_dir, "generate_lecturer_weekly.R"),
                   dean     = file.path(gen_dir, "generate_dean_weekly.R"))

      out_dir <- file.path(ANALYTICS_HOME, "output")
      result  <- tryCatch({
        out <- system2("Rscript",
                       args   = c(script, input$entity_id, input$week_id, out_dir),
                       stdout = TRUE, stderr = TRUE)
        first_line <- trimws(out[1])
        if (file.exists(first_line)) {
          report_path(first_line)
          paste("Report generated:\n", first_line)
        } else {
          paste("Script output:\n", paste(out, collapse = "\n"))
        }
      }, error = function(e) paste("Error:", e$message))

      output$status <- renderText(result)
    })

    output$download_link <- renderUI({
      path <- report_path()
      if (is.null(path)) return(NULL)
      tags$p(tags$strong("File: "), path)
    })
  })
}
