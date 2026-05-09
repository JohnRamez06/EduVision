# ============================================================
# compute_student_summaries.R
#
# Usage:  Rscript scripts/compute_student_summaries.R <session_id>
#
# For every student who has emotion snapshots in the given session,
# compute all analytics metrics and upsert into student_lecture_summaries.
# This is the bridge between raw snapshot data and the Java analytics API.
# ============================================================

args <- commandArgs(trailingOnly = TRUE)
if (length(args) < 1) stop("Usage: Rscript compute_student_summaries.R <session_id>")
session_id <- args[1]

suppressPackageStartupMessages({
  library(DBI)
  library(RMariaDB)
  library(dplyr)
})

# ── Locate config.R ────────────────────────────────────────────────────────────
.cmd        <- commandArgs(trailingOnly = FALSE)
.file_flag  <- grep("^--file=", .cmd, value = TRUE)
.script_dir <- if (length(.file_flag) > 0) {
  normalizePath(dirname(sub("^--file=", "", .file_flag[1])))
} else {
  tryCatch(normalizePath(dirname(sys.frame(1)$ofile)), error = function(e) getwd())
}
source(file.path(dirname(.script_dir), "config.R"), local = TRUE)
source(file.path(dirname(.script_dir), "scripts", "utils.R"),               local = TRUE)
source(file.path(dirname(.script_dir), "scripts", "calculate_statistics.R"), local = TRUE)

# ── Connect ────────────────────────────────────────────────────────────────────
conn <- get_connection()
on.exit(try(DBI::dbDisconnect(conn), silent = TRUE), add = TRUE)

message(sprintf("[compute] Processing session: %s", session_id))

# ── Fetch session metadata ─────────────────────────────────────────────────────
session_meta <- dbGetQuery(conn, sprintf("
  SELECT ls.id AS session_id, ls.course_id,
         ls.actual_start, ls.scheduled_start
  FROM lecture_sessions ls
  WHERE ls.id = '%s'
", session_id))

if (nrow(session_meta) == 0) stop(paste("Session not found:", session_id))
course_id <- session_meta$course_id[1]

# ── Fetch all student snapshots for this session ───────────────────────────────
snaps <- dbGetQuery(conn, sprintf("
  SELECT ses.id, ses.student_id, ses.emotion, ses.concentration,
         ses.confidence_score, ses.captured_at
  FROM student_emotion_snapshots ses
  WHERE ses.session_id = '%s'
  ORDER BY ses.student_id, ses.captured_at
", session_id))

if (nrow(snaps) == 0) {
  message("[compute] No student snapshots found for session ", session_id, " — nothing to compute.")
  quit(status = 0)
}

# ── Fetch overall engagement from class-level snapshots ────────────────────────
class_eng <- dbGetQuery(conn, sprintf("
  SELECT AVG(COALESCE(engagement_score, 0)) AS overall_engagement
  FROM emotion_snapshots
  WHERE session_id = '%s'
", session_id))
overall_session_eng <- as.numeric(class_eng$overall_engagement[1])
if (is.na(overall_session_eng)) overall_session_eng <- NA_real_

# ── Helper: upsert one row into student_lecture_summaries ─────────────────────
upsert_summary <- function(con, row) {
  # Check if a record already exists for this (student_id, session_id) pair
  existing <- dbGetQuery(con, sprintf("
    SELECT id FROM student_lecture_summaries
    WHERE student_id = '%s' AND session_id = '%s'
    LIMIT 1
  ", row$student_id, session_id))

  now_ts <- format(Sys.time(), "%Y-%m-%d %H:%M:%S")

  if (nrow(existing) > 0) {
    rec_id <- existing$id[1]
    sql <- sprintf("
      UPDATE student_lecture_summaries SET
        pct_happy            = %.4f,
        pct_sad              = %.4f,
        pct_angry            = %.4f,
        pct_confused         = %.4f,
        pct_neutral          = %.4f,
        pct_engaged          = %.4f,
        pct_high_conc        = %.4f,
        pct_med_conc         = %.4f,
        pct_low_conc         = %.4f,
        pct_distracted       = %.4f,
        overall_engagement   = %s,
        attention_score      = %.4f,
        participation_score  = %.4f,
        avg_concentration    = %.4f,
        attentive_percentage = %.4f,
        dominant_emotion     = '%s',
        snapshot_count       = %d,
        updated_at           = '%s'
      WHERE id = '%s'
    ",
      row$pct_happy, row$pct_sad, row$pct_angry, row$pct_confused,
      row$pct_neutral, row$pct_engaged,
      row$pct_high_conc, row$pct_med_conc, row$pct_low_conc, row$pct_distracted,
      if (is.na(row$overall_engagement)) "NULL" else sprintf("%.4f", row$overall_engagement),
      row$attention_score, row$participation_score,
      row$avg_concentration, row$attentive_percentage,
      row$dominant_emotion,
      row$snapshot_count, now_ts, rec_id
    )
    dbExecute(con, sql)
    message(sprintf("  [update] student=%s  conc=%.0f%%  attentive=%.0f%%  emotion=%s",
                    row$student_id,
                    row$avg_concentration * 100,
                    row$attentive_percentage * 100,
                    row$dominant_emotion))
  } else {
    new_id <- paste0(
      paste(sample(c(letters[1:6], 0:9), 8, TRUE), collapse = ""), "-",
      paste(sample(c(letters[1:6], 0:9), 4, TRUE), collapse = ""), "-",
      paste(sample(c(letters[1:6], 0:9), 4, TRUE), collapse = ""), "-",
      paste(sample(c(letters[1:6], 0:9), 4, TRUE), collapse = ""), "-",
      paste(sample(c(letters[1:6], 0:9), 12, TRUE), collapse = "")
    )
    sql <- sprintf("
      INSERT INTO student_lecture_summaries
        (id, student_id, session_id, course_id,
         pct_happy, pct_sad, pct_angry, pct_confused, pct_neutral, pct_engaged,
         pct_high_conc, pct_med_conc, pct_low_conc, pct_distracted,
         overall_engagement, attention_score, participation_score,
         avg_concentration, attentive_percentage, dominant_emotion,
         snapshot_count, generated_at, updated_at)
      VALUES
        ('%s', '%s', '%s', '%s',
         %.4f, %.4f, %.4f, %.4f, %.4f, %.4f,
         %.4f, %.4f, %.4f, %.4f,
         %s, %.4f, %.4f,
         %.4f, %.4f, '%s',
         %d, '%s', '%s')
    ",
      new_id, row$student_id, session_id, course_id,
      row$pct_happy, row$pct_sad, row$pct_angry, row$pct_confused,
      row$pct_neutral, row$pct_engaged,
      row$pct_high_conc, row$pct_med_conc, row$pct_low_conc, row$pct_distracted,
      if (is.na(row$overall_engagement)) "NULL" else sprintf("%.4f", row$overall_engagement),
      row$attention_score, row$participation_score,
      row$avg_concentration, row$attentive_percentage,
      row$dominant_emotion,
      row$snapshot_count, now_ts, now_ts
    )
    dbExecute(con, sql)
    message(sprintf("  [insert] student=%s  conc=%.0f%%  attentive=%.0f%%  emotion=%s",
                    row$student_id,
                    row$avg_concentration * 100,
                    row$attentive_percentage * 100,
                    row$dominant_emotion))
  }
}

# ── Compute metrics per student and upsert ────────────────────────────────────
student_ids <- unique(snaps$student_id)
message(sprintf("[compute] Found %d student(s) with snapshots", length(student_ids)))

for (sid in student_ids) {
  stu <- snaps[snaps$student_id == sid, ]
  n   <- nrow(stu)
  if (n == 0) next

  # Emotion percentages (normalised to 0-1)
  emotion_counts <- table(factor(
    tolower(stu$emotion),
    levels = c("happy", "sad", "angry", "confused", "neutral", "engaged",
               "surprised", "fearful", "disgusted")
  ))
  pct_happy    <- as.numeric(emotion_counts["happy"])    / n
  pct_sad      <- as.numeric(emotion_counts["sad"])      / n
  pct_angry    <- as.numeric(emotion_counts["angry"])    / n
  pct_confused <- as.numeric(emotion_counts["confused"]) / n
  pct_neutral  <- as.numeric(emotion_counts["neutral"])  / n
  pct_engaged  <- as.numeric(emotion_counts["engaged"])  / n

  # Concentration percentages
  conc_counts <- table(factor(
    tolower(stu$concentration),
    levels = c("high", "medium", "low", "distracted")
  ))
  pct_high_conc  <- as.numeric(conc_counts["high"])       / n
  pct_med_conc   <- as.numeric(conc_counts["medium"])     / n
  pct_low_conc   <- as.numeric(conc_counts["low"])        / n
  pct_distracted <- as.numeric(conc_counts["distracted"]) / n

  # Weighted average concentration (0-1 scale)
  conc_scores      <- concentration_to_score(tolower(stu$concentration))
  avg_concentration <- mean(conc_scores, na.rm = TRUE) / 100   # convert 0-100 → 0-1

  # Attentiveness: high/medium concentration OR engaged emotion
  attentive        <- (tolower(stu$concentration) %in% c("high", "medium")) |
                      (tolower(stu$emotion) == "engaged")
  attentive_pct    <- mean(attentive, na.rm = TRUE)

  # Attention score = high + medium concentration
  attention_score  <- pct_high_conc + pct_med_conc

  # Participation score = positive-affect emotions (happy + engaged + surprised)
  positive_counts  <- sum(tolower(stu$emotion) %in% c("happy", "engaged", "surprised"))
  participation    <- positive_counts / n

  # Dominant emotion
  dom_emotion <- dominant_emotion(tolower(stu$emotion))
  if (is.na(dom_emotion)) dom_emotion <- "neutral"

  row <- list(
    student_id           = sid,
    pct_happy            = pct_happy,
    pct_sad              = pct_sad,
    pct_angry            = pct_angry,
    pct_confused         = pct_confused,
    pct_neutral          = pct_neutral,
    pct_engaged          = pct_engaged,
    pct_high_conc        = pct_high_conc,
    pct_med_conc         = pct_med_conc,
    pct_low_conc         = pct_low_conc,
    pct_distracted       = pct_distracted,
    overall_engagement   = overall_session_eng,
    attention_score      = attention_score,
    participation_score  = participation,
    avg_concentration    = avg_concentration,
    attentive_percentage = attentive_pct,
    dominant_emotion     = dom_emotion,
    snapshot_count       = n
  )

  tryCatch(
    upsert_summary(conn, row),
    error = function(e) message("  [error] student=", sid, " : ", conditionMessage(e))
  )
}

message(sprintf("[compute] Done — processed %d student(s) for session %s",
                length(student_ids), session_id))
