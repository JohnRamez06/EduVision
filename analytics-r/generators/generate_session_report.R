# analytics-r/generators/generate_session_report.R
args <- commandArgs(trailingOnly = TRUE)
if (length(args) < 1) stop("Usage: Rscript generate_session_report.R <session_id>")

session_id <- args[1]

suppressPackageStartupMessages({
  library(DBI)
  library(RMySQL)
  library(dplyr)
  library(ggplot2)
  library(rmarkdown)
})

con <- dbConnect(RMySQL::MySQL(),
                 host = "localhost", user = "root", password = "", dbname = "eduvision")
on.exit(dbDisconnect(con), add = TRUE)

# ── Class-level emotion timeline ───────────────────────────────────────────────
emotion_timeline <- dbGetQuery(con, sprintf("
  SELECT snapshot_time, happy_count, neutral_count, confused_count,
         sad_count, surprised_count, angry_count,
         avg_concentration, engagement_score, total_faces
  FROM   emotion_snapshots
  WHERE  session_id = '%s'
  ORDER  BY snapshot_time
", session_id))

# ── Per-student breakdown ──────────────────────────────────────────────────────
student_breakdown <- dbGetQuery(con, sprintf("
  SELECT CONCAT(u.first_name, ' ', u.last_name) AS student_name,
         ses.dominant_emotion, AVG(ses.concentration) AS avg_concentration,
         AVG(ses.confidence_score) AS avg_confidence,
         SUM(ses.is_attentive) / COUNT(*) * 100 AS attentive_pct,
         SUM(ses.is_drowsy)    / COUNT(*) * 100 AS drowsy_pct
  FROM   student_emotion_snapshots ses
  JOIN   emotion_snapshots es ON es.snapshot_id = ses.snapshot_id
  JOIN   students s           ON s.student_id   = ses.student_id
  JOIN   users u              ON u.user_id       = s.user_id
  WHERE  es.session_id = '%s'
  GROUP  BY ses.student_id, u.first_name, u.last_name, ses.dominant_emotion
", session_id))

# ── Exit / attendance summary ──────────────────────────────────────────────────
exit_summary <- dbGetQuery(con, sprintf("
  SELECT CONCAT(u.first_name, ' ', u.last_name) AS student_name,
         COUNT(*) AS total_exits,
         SUM(CASE WHEN return_time IS NULL THEN 1 ELSE 0 END) AS left_early,
         AVG(TIMESTAMPDIFF(MINUTE, exit_time, return_time)) AS avg_duration
  FROM   session_exit_logs el
  JOIN   students s ON s.student_id = el.student_id
  JOIN   users u    ON u.user_id    = s.user_id
  WHERE  el.session_id = '%s'
  GROUP  BY el.student_id, u.first_name, u.last_name
", session_id))

# ── Plots ──────────────────────────────────────────────────────────────────────
plots_dir <- "analytics-r/output/session/plots"
dir.create(plots_dir, showWarnings = FALSE, recursive = TRUE)

# 1. Engagement over time
if (nrow(emotion_timeline) > 0) {
  p_engagement <- ggplot(emotion_timeline, aes(x = snapshot_time, y = engagement_score)) +
    geom_line(color = "#4F81BD", linewidth = 1) +
    geom_smooth(method = "loess", se = FALSE, color = "#C0504D", linetype = "dashed") +
    labs(title = "Engagement Score Over Time",
         x = "Time", y = "Engagement Score (0–100)") +
    theme_minimal()
  ggsave(file.path(plots_dir, paste0("engagement_", session_id, ".png")),
         p_engagement, width = 8, height = 4)
}

# 2. Emotion distribution (overall)
if (nrow(emotion_timeline) > 0) {
  emotion_totals <- data.frame(
    emotion = c("Happy","Neutral","Confused","Sad","Surprised","Angry"),
    count   = c(sum(emotion_timeline$happy_count),
                sum(emotion_timeline$neutral_count),
                sum(emotion_timeline$confused_count),
                sum(emotion_timeline$sad_count),
                sum(emotion_timeline$surprised_count),
                sum(emotion_timeline$angry_count))
  ) %>% filter(count > 0)

  p_pie <- ggplot(emotion_totals, aes(x = "", y = count, fill = emotion)) +
    geom_bar(stat = "identity", width = 1) +
    coord_polar("y") +
    labs(title = "Emotion Distribution") +
    theme_void() +
    theme(legend.position = "right")
  ggsave(file.path(plots_dir, paste0("emotions_", session_id, ".png")),
         p_pie, width = 6, height = 6)
}

# 3. Concentration timeline
if (nrow(emotion_timeline) > 0) {
  p_conc <- ggplot(emotion_timeline, aes(x = snapshot_time, y = avg_concentration)) +
    geom_area(fill = "#9BBB59", alpha = 0.4) +
    geom_line(color = "#9BBB59", linewidth = 1) +
    scale_y_continuous(limits = c(0, 1)) +
    labs(title = "Average Concentration Over Time",
         x = "Time", y = "Concentration (0–1)") +
    theme_minimal()
  ggsave(file.path(plots_dir, paste0("concentration_", session_id, ".png")),
         p_conc, width = 8, height = 4)
}

# ── Render PDF ─────────────────────────────────────────────────────────────────
output_dir  <- "analytics-r/output/session"
dir.create(output_dir, showWarnings = FALSE, recursive = TRUE)
output_file <- file.path(output_dir, paste0("session_", session_id, ".pdf"))

rmarkdown::render(
  input       = "analytics-r/templates/session_template.Rmd",
  output_file = normalizePath(output_file, mustWork = FALSE),
  params = list(
    session_id       = session_id,
    emotion_timeline = emotion_timeline,
    student_breakdown = student_breakdown,
    exit_summary     = exit_summary,
    plots_dir        = normalizePath(plots_dir, mustWork = FALSE)
  ),
  quiet = TRUE
)

cat(normalizePath(output_file, mustWork = FALSE), "\n")
message("Session report saved: ", output_file)