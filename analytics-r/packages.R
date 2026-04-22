pkgs <- c("DBI","RMariaDB","plumber","dplyr","ggplot2","tidyr","lubridate","jsonlite")

for (p in pkgs) {
  if (!requireNamespace(p, quietly = TRUE)) {
    install.packages(p, repos = "https://cloud.r-project.org")
  }
}

message("All R packages ready")