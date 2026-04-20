source("../config.R")
# Entry point for generate_course_analytics
# Usage: Rscript generate_course_analytics.R <id>
args <- commandArgs(trailingOnly=TRUE)
if (length(args)==0) stop("Provide an ID argument")
message("Generating report for ID: ", args[1])
# TODO: render R Markdown template
