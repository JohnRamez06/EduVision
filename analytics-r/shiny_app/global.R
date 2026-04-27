# ============================================================
# global.R — Shiny app shared setup
# ============================================================

library(shiny)
library(shinydashboard)
library(shinyWidgets)
library(ggplot2)
library(dplyr)
library(plotly)
library(DBI)
library(RMySQL)

# Load analytics-r config
ANALYTICS_HOME <- Sys.getenv("ANALYTICS_HOME",
                              normalizePath(file.path(dirname(
                                sys.frame(1)$ofile %||% "."), "..")))
source(file.path(ANALYTICS_HOME, "config.R"),        local = FALSE)
source(file.path(ANALYTICS_HOME, "scripts", "utils.R"), local = FALSE)

# Pre-load analysis helpers
source(file.path(ANALYTICS_HOME, "analysis", "session_analysis.R"),         local = FALSE)
source(file.path(ANALYTICS_HOME, "analysis", "engagement_analysis.R"),      local = FALSE)
source(file.path(ANALYTICS_HOME, "analysis", "concentration_analysis.R"),   local = FALSE)
source(file.path(ANALYTICS_HOME, "analysis", "compare_lecturers.R"),        local = FALSE)
source(file.path(ANALYTICS_HOME, "analysis", "compare_courses.R"),          local = FALSE)
source(file.path(ANALYTICS_HOME, "analysis", "weekly_student_analysis.R"),  local = FALSE)
source(file.path(ANALYTICS_HOME, "scripts", "fetch_data.R"),                local = FALSE)
source(file.path(ANALYTICS_HOME, "scripts", "fetch_class_snapshots.R"),     local = FALSE)
source(file.path(ANALYTICS_HOME, "scripts", "fetch_student_snapshots.R"),   local = FALSE)

APP_TITLE <- "EduVision Analytics Dashboard"
