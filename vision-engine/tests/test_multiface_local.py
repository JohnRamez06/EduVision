import json
import requests

# Put a local image path OR host it somewhere and use frame_url with /analyze/frame
API = "http://localhost:8000"


def main():
    # Change this to an image with multiple faces:
    img_path = "tests/sample_multiface.jpg"

    with open(img_path, "rb") as f:
        files = {"file": ("sample.jpg", f, "image/jpeg")}
        data = {
            "session_id": "test-session",
            "post_to_spring": "false",
        }
        r = requests.post(f"{API}/analyze/frame", files=files, data=data, timeout=60)
        r.raise_for_status()
        payload = r.json()

    print("student_count:", payload.get("student_count"))
    print(json.dumps(payload, indent=2))


if __name__ == "__main__":
    main()