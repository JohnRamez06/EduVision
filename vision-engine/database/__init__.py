"""
Database access package.

Keeps connection pooling + small repositories isolated from the FastAPI layer.
"""

from .mysql_connector import get_connection  # noqa: F401
from .camera_repository import get_camera_configuration  # noqa: F401
from .consent_repository import has_consent  # noqa: F401
from .student_repository import is_student  # noqa: F401