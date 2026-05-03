# C:\Users\john\Desktop\eduvision\vision-engine\processors\frame_processor.py

import logging
from typing import Dict, Any
from processors.face_analyzer import FaceAnalyzer
from processors.aggregator import Aggregator

logger = logging.getLogger(__name__)

class FrameProcessor:
    """
    Facade that runs full per-frame pipeline:
    detect -> analyze faces -> aggregate summary
    """

    def __init__(self):
        self.face_analyzer = FaceAnalyzer()
        self.aggregator = Aggregator()
        self.current_session_id = None

    def _convert_concentration(self, concentration):
        """Convert concentration from string/dict to float"""
        if isinstance(concentration, (int, float)):
            return float(concentration)
        elif isinstance(concentration, str):
            concentration_map = {
                "low": 0.3,
                "medium": 0.6,
                "high": 0.9,
                "very_low": 0.1,
                "very_high": 0.95
            }
            return concentration_map.get(concentration.lower(), 0.5)
        elif isinstance(concentration, dict):
            level = concentration.get("level", 0.5)
            return self._convert_concentration(level)
        else:
            return 0.5

    def process(self, frame_bgr) -> Dict[str, Any]:
        """
        Process a single frame and return analysis results as dictionary.
        
        Returns:
            Dictionary with keys:
            - people: list of detected faces with their details
            - student_count: number of detected students
            - emotion_counts: dictionary of emotion frequencies
            - engagement_score: overall engagement score
            - avg_concentration: average concentration level
        """
        try:
            # Get face analysis results (list of faces)
            analysis = self.face_analyzer.analyze(frame_bgr)
            
            # Convert analysis to expected format if needed
            if isinstance(analysis, list):
                people = analysis
            elif isinstance(analysis, dict):
                people = analysis.get("people", analysis.get("faces", []))
            else:
                people = []
            
            # Convert concentration for each person and ensure proper format
            for person in people:
                # Ensure concentration is a float
                concentration = person.get("concentration", 0.5)
                person["concentration"] = self._convert_concentration(concentration)
                
                # Ensure student_id is properly set
                if "student_id" not in person:
                    person["student_id"] = person.get("user_id", None)
                
                # Ensure emotion is set
                if "dominant_emotion" not in person and "emotion" in person:
                    person["dominant_emotion"] = person["emotion"]
            
            # Calculate aggregate statistics
            student_count = len(people)
            
            # Count emotions
            emotion_counts = {}
            total_concentration = 0
            
            for person in people:
                # Get emotion
                emotion = person.get("dominant_emotion", person.get("emotion", "neutral"))
                emotion_counts[emotion] = emotion_counts.get(emotion, 0) + 1
                
                # Get concentration (already converted to float)
                total_concentration += person.get("concentration", 0.5)
            
            # Calculate averages
            avg_concentration = total_concentration / student_count if student_count > 0 else 0.5
            
            # Calculate engagement score (based on positive emotions and concentration)
            positive_emotions = emotion_counts.get("happy", 0) + emotion_counts.get("surprised", 0)
            total_emotions = sum(emotion_counts.values()) or 1
            engagement_score = (positive_emotions / total_emotions + avg_concentration) / 2
            
            # Build result dictionary
            result = {
                "people": people,
                "student_count": student_count,
                "emotion_counts": emotion_counts,
                "engagement_score": engagement_score,
                "avg_concentration": avg_concentration,
                "dominant_emotion": max(emotion_counts, key=emotion_counts.get) if emotion_counts else "neutral"
            }
            
            # Also store for aggregator if needed
            try:
                self.aggregator.collect(self.current_session_id, people)
            except Exception as e:
                logger.debug(f"Aggregator error: {e}")
            
            return result
            
        except Exception as e:
            logger.error(f"Frame processing error: {e}")
            # Return empty result on error
            return {
                "people": [],
                "student_count": 0,
                "emotion_counts": {},
                "engagement_score": 0.5,
                "avg_concentration": 0.5,
                "dominant_emotion": "neutral"
            }

    def set_session(self, session_id: str):
        """Set current session ID"""
        self.current_session_id = session_id