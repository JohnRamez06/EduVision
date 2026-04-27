# ============================================================
# ui.R — Shiny dashboard UI
# ============================================================

source("global.R", local = FALSE)

# Load modules
source("modules/engagement_tab.R",         local = FALSE)
source("modules/concentration_tab.R",      local = FALSE)
source("modules/student_search_tab.R",     local = FALSE)
source("modules/lecturer_comparison_tab.R", local = FALSE)
source("modules/weekly_report_tab.R",      local = FALSE)
source("modules/model_status_tab.R",       local = FALSE)

ui <- dashboardPage(
  skin = "blue",

  dashboardHeader(title = APP_TITLE),

  dashboardSidebar(
    sidebarMenu(
      menuItem("Engagement",        tabName = "engagement",   icon = icon("chart-line")),
      menuItem("Concentration",     tabName = "concentration", icon = icon("brain")),
      menuItem("Student Search",    tabName = "student",      icon = icon("search")),
      menuItem("Lecturer Compare",  tabName = "lecturer",     icon = icon("users")),
      menuItem("Weekly Reports",    tabName = "weekly",       icon = icon("calendar-week")),
      menuItem("Model Status",      tabName = "model",        icon = icon("cogs"))
    )
  ),

  dashboardBody(
    tags$head(
      tags$link(rel = "stylesheet", type = "text/css", href = "styles.css")
    ),
    tabItems(
      tabItem(tabName = "engagement",   engagementTabUI("eng")),
      tabItem(tabName = "concentration", concentrationTabUI("conc")),
      tabItem(tabName = "student",      studentSearchTabUI("stu")),
      tabItem(tabName = "lecturer",     lecturerComparisonTabUI("lect")),
      tabItem(tabName = "weekly",       weeklyReportTabUI("weekly")),
      tabItem(tabName = "model",        modelStatusTabUI("model"))
    )
  )
)
