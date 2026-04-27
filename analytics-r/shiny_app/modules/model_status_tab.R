# ============================================================
# model_status_tab.R — Model versions and health status
# ============================================================

modelStatusTabUI <- function(id) {
  ns <- NS(id)
  fluidRow(
    box(width = 12, title = "Active Model Versions",
        status = "primary", solidHeader = TRUE,
      tableOutput(ns("versions_tbl")),
      br(),
      actionButton(ns("refresh"), "Refresh", icon = icon("sync"))
    ),
    box(width = 6, title = "Face Recogniser", status = "info", solidHeader = TRUE,
      verbatimTextOutput(ns("face_info"))
    ),
    box(width = 6, title = "Actions", status = "warning", solidHeader = TRUE,
      actionButton(ns("retrain_face"), "Retrain Face Recogniser",
                   icon = icon("user-circle"), class = "btn-warning"),
      br(), br(),
      verbatimTextOutput(ns("action_result"))
    )
  )
}

modelStatusTabServer <- function(id, model_info) {
  moduleServer(id, function(input, output, session) {
    data <- reactive({
      input$refresh
      model_info()
    })

    output$versions_tbl <- renderTable({
      df <- data()
      if (nrow(df) == 0) return(data.frame(Message = "No model versions found."))
      df[, c("model_type","version_tag","accuracy","total_faces",
             "last_trained_at","is_active")]
    })

    output$face_info <- renderText({
      df <- data()
      face <- df[df$model_type == "face_recognition" &
                   as.logical(df$is_active), , drop = FALSE]
      if (nrow(face) == 0) return("Face recogniser: not trained yet.")
      sprintf("Model:      %s\nVersion:    %s\nFaces:      %s\nAccuracy:   %s\nTrained at: %s",
              face$model_type[1],
              face$version_tag[1],
              face$total_faces[1],
              ifelse(is.na(face$accuracy[1]), "N/A",
                     sprintf("%.2f%%", as.numeric(face$accuracy[1]) * 100)),
              face$last_trained_at[1])
    })

    observeEvent(input$retrain_face, {
      output$action_result <- renderText("Retraining... (may take a few minutes)")
      result <- tryCatch({
        script <- file.path(ANALYTICS_HOME, "face_learning", "retrain_full_model.R")
        out    <- system2("Rscript", args = script, stdout = TRUE, stderr = TRUE)
        paste(out, collapse = "\n")
      }, error = function(e) paste("Error:", e$message))
      output$action_result <- renderText(result)
    })
  })
}
