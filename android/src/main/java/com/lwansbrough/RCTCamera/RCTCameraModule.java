package com.lwansbrough.RCTCamera;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.Callback;

/**
 * {@link NativeModule}
 */

public class RCTCameraModule extends ReactContextBaseJavaModule {

  ReactApplicationContext reactContext;

  public RCTCameraModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "RCTCamera";
  }

  @ReactMethod
  public void test(ReadableMap options, Callback callback) {
    callback.invoke(null, "test");
  }
}
