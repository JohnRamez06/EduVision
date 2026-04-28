# analytics-r/requirements.R
# Installs required packages for EduVision analytics reports

packages <- c(
  "DBI",
  "RMariaDB",
  "glue",
  "dplyr",
  "lubridate",
  "yaml",
  "rmarkdown",
  "knitr",
  "ggplot2"
)

installed <- rownames(installed.packages())
to_install <- setdiff(packages, installed)

if (length(to_install) > 0) {
  install.packages(to_install, dependencies = TRUE)
}

cat("OK: packages installed/verified:\n")
print(packages)