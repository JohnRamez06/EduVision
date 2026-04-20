library(shiny)
student_search_tabUI <- function(id) { ns <- NS(id); tagList(plotOutput(ns("plot"))) }
student_search_tabServer <- function(id) { moduleServer(id, function(input,output,session) { output$plot <- renderPlot({ plot(1) }) }) }
