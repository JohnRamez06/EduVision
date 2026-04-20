library(shiny)
source("../config.R")
source("modules/engagement_tab.R")
source("modules/concentration_tab.R")
source("modules/student_search_tab.R")
ui <- fluidPage(title="EduVision Analytics", tabsetPanel(engagementTabUI("eng"), concentrationTabUI("conc"), studentSearchTabUI("search")))
server <- function(input,output,session) { engagementTabServer("eng"); concentrationTabServer("conc"); studentSearchTabServer("search") }
shinyApp(ui, server)
