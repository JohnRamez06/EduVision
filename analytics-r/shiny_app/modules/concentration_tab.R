library(shiny)
concentration_tabUI <- function(id) { ns <- NS(id); tagList(plotOutput(ns("plot"))) }
concentration_tabServer <- function(id) { moduleServer(id, function(input,output,session) { output$plot <- renderPlot({ plot(1) }) }) }
