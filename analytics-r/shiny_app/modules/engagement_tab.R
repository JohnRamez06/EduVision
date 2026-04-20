library(shiny)
engagement_tabUI <- function(id) { ns <- NS(id); tagList(plotOutput(ns("plot"))) }
engagement_tabServer <- function(id) { moduleServer(id, function(input,output,session) { output$plot <- renderPlot({ plot(1) }) }) }
