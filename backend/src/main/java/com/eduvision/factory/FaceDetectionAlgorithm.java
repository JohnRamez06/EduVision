package com.eduvision.factory;
import java.util.List;
public interface FaceDetectionAlgorithm {
    List<Object> detect(byte[] frame);
}
