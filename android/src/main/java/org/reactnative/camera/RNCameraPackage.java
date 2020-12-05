package org.reactnative.camera;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
   // =============<<<<<<<<<<<<<<<<< check here
//    still use the cameramodule in lwansbrough package
import com.lwansbrough.RCTCamera.RCTCameraModule;
import com.lwansbrough.RCTCamera.RCTCameraViewManager;
   // =============<<<<<<<<<<<<<<<<< check here
//    use the facedetector module in org reactnative facedetector package
import org.reactnative.facedetector.FaceDetectorModule;
import org.reactnative.facedetector.FaceDetectorModule;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by jgfidelis on 02/02/18.
 */

public class RNCameraPackage implements ReactPackage {
    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactApplicationContext) {
        return Arrays.<NativeModule>asList(
            // =============<<<<<<<<<<<<<<<<< check here
            // why two module at the same time?
                // new RCTCameraModule(reactApplicationContext),
                // this is returned for js bridge in android devices
                new CameraModule(reactApplicationContext),
                // use the FaceDetectorModule
                new FaceDetectorModule(reactApplicationContext),
                new MyModelModule(reactApplicationContext)

                // todo: check cameramodule, RCTCameraModule and facedetectormodule
                // todo: add verification module here
        );
    }

    // Deprecated in RN 0.47
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactApplicationContext) {
        return Arrays.<ViewManager>asList(
            // =============<<<<<<<<<<<<<<<<< check here
            // why use two view manager at the same time?
                // new RCTCameraViewManager(),
                new CameraViewManager()
                // todo: modify in cameraviewmanager
        );
    }
}
