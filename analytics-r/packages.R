# ============================================================
# packages.R — Install (if needed) and load all required packages
# ============================================================

required_packages <- c(
  "DBI", "RMySQL",
  "reticulate",
  "ggplot2", "dplyr", "tidyr", "scales", "lubridate",
  "changepoint", "tseries",
  "rmarkdown", "knitr", "tinytex",
  "shiny", "shinydashboard", "shinyWidgets",
  "plotly",
  "keras",
  "jsonlite", "yaml"
)

install_if_missing <- function(pkgs) {
  new_pkgs <- pkgs[!(pkgs %in% installed.packages()[, "Package"])]
  if (length(new_pkgs) > 0) {
    message("Installing missing packages: ", paste(new_pkgs, collapse = ", "))
    install.packages(new_pkgs, repos = "https://cloud.r-project.org", quiet = TRUE)
  }
}

install_if_missing(required_packages)

for (pkg in required_packages) {
  suppressPackageStartupMessages(
    library(pkg, character.only = TRUE, warn.conflicts = FALSE)
  )
}

message("All packages loaded successfully.")
