package com.lwansbrough.RCTCamera;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.facebook.react.bridge.JavaScriptModule;

import org.reactnative.camera.CameraModule;
import org.reactnative.camera.CameraViewManager;
import org.reactnative.facedetector.FaceDetectorModule;

public class RCTCameraPackage implements ReactPackage {

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactApplicationContext) {
        return Arrays.<NativeModule>asList(
            new RCTCameraModule(reactApplicationContext),
            new CameraModule(reactApplicationContext),
            new FaceDetectorModule(reactApplicationContext)
        );
    }

    // Deprecated in RN 0.47
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactApplicationContext) {
        return Arrays.<ViewManager>asList(
            new RCTCameraViewManager(),
            new CameraViewManager()
        );
    }

}
