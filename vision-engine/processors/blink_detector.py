import cv2
import numpy as np
import mediapipe as mp


def _dist(a, b) -> float:
    return float(np.linalg.norm(a - b))


class BlinkDetector:
    """
    Blink detection using MediaPipe FaceMesh + EAR (eye aspect ratio).

    Output:
      {
        "blink": bool,
        "ear": float,
        "closed": bool
      }
    """

    # FaceMesh landmark indexes for eyes
    # Using a common subset for EAR calculation:
    # Left eye: 33, 160, 158, 133, 153, 144
    # Right eye: 362, 385, 387, 263, 373, 380
    LEFT = [33, 160, 158, 133, 153, 144]
    RIGHT = [362, 385, 387, 263, 373, 380]

    def __init__(self, ear_threshold: float = 0.20, min_consec_frames: int = 2):
        self.ear_threshold = ear_threshold
        self.min_consec_frames = min_consec_frames

        self._mesh = mp.solutions.face_mesh.FaceMesh(
            static_image_mode=False,
            max_num_faces=1,
            refine_landmarks=True,
            min_detection_confidence=0.5,
            min_tracking_confidence=0.5,
        )

        self._closed_frames = 0
        self._blink_latched = False

    def _ear(self, pts: np.ndarray) -> float:
        # EAR = (||p2-p6|| + ||p3-p5||) / (2*||p1-p4||)
        p1, p2, p3, p4, p5, p6 = pts
        return (_dist(p2, p6) + _dist(p3, p5)) / (2.0 * _dist(p1, p4) + 1e-6)

    def predict(self, face_bgr: np.ndarray) -> dict:
        if face_bgr is None or face_bgr.size == 0:
            return {"blink": False, "ear": 0.0, "closed": False}

        rgb = cv2.cvtColor(face_bgr, cv2.COLOR_BGR2RGB)
        res = self._mesh.process(rgb)
        if not res.multi_face_landmarks:
            # No landmarks found
            self._closed_frames = 0
            self._blink_latched = False
            return {"blink": False, "ear": 0.0, "closed": False}

        h, w = face_bgr.shape[:2]
        lm = res.multi_face_landmarks[0].landmark

        def get_pts(idxs):
            pts = []
            for i in idxs:
                pts.append(np.array([lm[i].x * w, lm[i].y * h], dtype=np.float32))
            return np.stack(pts, axis=0)

        left_pts = get_pts(self.LEFT)
        right_pts = get_pts(self.RIGHT)

        ear = (self._ear(left_pts) + self._ear(right_pts)) / 2.0
        closed = ear < self.ear_threshold

        blink = False
        if closed:
            self._closed_frames += 1
            self._blink_latched = True
        else:
            # Eye reopened
            if self._blink_latched and self._closed_frames >= self.min_consec_frames:
                blink = True
            self._closed_frames = 0
            self._blink_latched = False

        return {"blink": bool(blink), "ear": float(ear), "closed": bool(closed)}