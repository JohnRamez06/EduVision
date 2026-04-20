def anonymize_snapshot(data: dict) -> dict:
    data.pop("face_embedding", None)
    data["is_anonymised"] = True
    return data
