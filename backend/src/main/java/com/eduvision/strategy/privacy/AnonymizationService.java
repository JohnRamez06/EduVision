package com.eduvision.strategy.privacy;
import org.springframework.stereotype.Service;
@Service
public class AnonymizationService {
    public Object anonymize(Object data) {
        // TODO: strip face embeddings, blur frames
        return data;
    }
}
