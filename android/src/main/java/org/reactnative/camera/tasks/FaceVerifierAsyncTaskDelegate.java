package org.reactnative.camera.tasks;

public interface FaceVerifierAsyncTaskDelegate {
    // =============<<<<<<<<<<<<<<<<< check here
    void onFaceVerified(float result);

    void onFaceVerificationError();

    void onFaceVerificationTaskCompleted();
}
