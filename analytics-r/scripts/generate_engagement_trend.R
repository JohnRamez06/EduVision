source("../config.R")
# Entry point for generate_engagement_trend
# Usage: Rscript generate_engagement_trend.R <id>
args <- commandArgs(trailingOnly=TRUE)
if (length(args)==0) stop("Provide an ID argument")
message("Generating report for ID: ", args[1])
# TODO: render R Markdown template
