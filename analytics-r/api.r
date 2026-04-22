source("packages.R")
source("config.R")

library(plumber)
library(dplyr)
library(lubridate)

#* @get /health
function() list(status="ok")

#* @get /sessions
function() {
  query_df("SELECT session_id, started_at FROM sessions ORDER BY started_at DESC LIMIT 50")
}

#* @get /session/summary
#* @param session_id
function(session_id="") {
  if (session_id == "") { res$status <- 400; return(list(error="session_id required")) }

  df <- query_df(sprintf("
    SELECT dominant_emotion, emotion_confidence, concentration_level, concentration_score, captured_at
    FROM detections
    WHERE session_id = '%s'
    ORDER BY captured_at ASC
  ", session_id))

  if (nrow(df) == 0) return(list(session_id=session_id, rows=0))

  list(
    session_id=session_id,
    rows=nrow(df),
    emotion_counts=as.list(table(df$dominant_emotion)),
    concentration_counts=as.list(table(df$concentration_level)),
    avg_concentration=mean(df$concentration_score, na.rm=TRUE),
    avg_emotion_confidence=mean(df$emotion_confidence, na.rm=TRUE)
  )
}