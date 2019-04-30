package org.reactnative.camera.tasks;

import com.facebook.react.bridge.WritableArray;

public interface PoseEstimatorAsyncTaskDelegate {
    void onPoseEstimated(WritableArray poses);
    void onPoseEstimatorTaskCompleted();
}
