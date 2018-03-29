package org.reactnative.camera;

import android.graphics.Bitmap;
import android.os.Build;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.uimanager.NativeViewHierarchyManager;
import com.facebook.react.uimanager.UIBlock;
import com.facebook.react.uimanager.UIManagerModule;
import com.google.android.cameraview.AspectRatio;
import com.google.zxing.BarcodeFormat;

import org.reactnative.camera.tasks.ResolveTakenPictureAsyncTask;
import org.reactnative.camera.utils.ScopedContext;
import org.reactnative.facedetector.RNFaceDetector;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import java.io.ByteArrayOutputStream;

import javax.annotation.Nullable;

public class CameraModule extends ReactContextBaseJavaModule {
  private static final String TAG = "CameraModule";

  private ScopedContext mScopedContext;
  static final int VIDEO_2160P = 0;
  static final int VIDEO_1080P = 1;
  static final int VIDEO_720P = 2;
  static final int VIDEO_480P = 3;
  static final int VIDEO_4x3 = 4;

  public static final Map<String, Object> VALID_BARCODE_TYPES =
      Collections.unmodifiableMap(new HashMap<String, Object>() {
        {
          put("aztec", BarcodeFormat.AZTEC.toString());
          put("ean13", BarcodeFormat.EAN_13.toString());
          put("ean8", BarcodeFormat.EAN_8.toString());
          put("qr", BarcodeFormat.QR_CODE.toString());
          put("pdf417", BarcodeFormat.PDF_417.toString());
          put("upc_e", BarcodeFormat.UPC_E.toString());
          put("datamatrix", BarcodeFormat.DATA_MATRIX.toString());
          put("code39", BarcodeFormat.CODE_39.toString());
          put("code93", BarcodeFormat.CODE_93.toString());
          put("interleaved2of5", BarcodeFormat.ITF.toString());
          put("codabar", BarcodeFormat.CODABAR.toString());
          put("code128", BarcodeFormat.CODE_128.toString());
          put("maxicode", BarcodeFormat.MAXICODE.toString());
          put("rss14", BarcodeFormat.RSS_14.toString());
          put("rssexpanded", BarcodeFormat.RSS_EXPANDED.toString());
          put("upc_a", BarcodeFormat.UPC_A.toString());
          put("upc_ean", BarcodeFormat.UPC_EAN_EXTENSION.toString());
        }
      });

  public CameraModule(ReactApplicationContext reactContext) {
    super(reactContext);
    mScopedContext = new ScopedContext(reactContext);
  }

  public ScopedContext getScopedContext() {
    return mScopedContext;
  }

  @Override
  public String getName() {
    return "RNCameraModule";
  }

  @Nullable
  @Override
  public Map<String, Object> getConstants() {
    return Collections.unmodifiableMap(new HashMap<String, Object>() {
      {
        put("Type", getTypeConstants());
        put("FlashMode", getFlashModeConstants());
        put("AutoFocus", getAutoFocusConstants());
        put("WhiteBalance", getWhiteBalanceConstants());
        put("VideoQuality", getVideoQualityConstants());
        put("BarCodeType", getBarCodeConstants());
        put("FaceDetection", Collections.unmodifiableMap(new HashMap<String, Object>() {
          {
            put("Mode", getFaceDetectionModeConstants());
            put("Landmarks", getFaceDetectionLandmarksConstants());
            put("Classifications", getFaceDetectionClassificationsConstants());
          }

          private Map<String, Object> getFaceDetectionModeConstants() {
            return Collections.unmodifiableMap(new HashMap<String, Object>() {
              {
                put("fast", RNFaceDetector.FAST_MODE);
                put("accurate", RNFaceDetector.ACCURATE_MODE);
              }
            });
          }

          private Map<String, Object> getFaceDetectionClassificationsConstants() {
            return Collections.unmodifiableMap(new HashMap<String, Object>() {
              {
                put("all", RNFaceDetector.ALL_CLASSIFICATIONS);
                put("none", RNFaceDetector.NO_CLASSIFICATIONS);
              }
            });
          }

          private Map<String, Object> getFaceDetectionLandmarksConstants() {
            return Collections.unmodifiableMap(new HashMap<String, Object>() {
              {
                put("all", RNFaceDetector.ALL_LANDMARKS);
                put("none", RNFaceDetector.NO_LANDMARKS);
              }
            });
          }
        }));
      }

      private Map<String, Object> getTypeConstants() {
        return Collections.unmodifiableMap(new HashMap<String, Object>() {
          {
            put("front", Constants.FACING_FRONT);
            put("back", Constants.FACING_BACK);
          }
        });
      }

      private Map<String, Object> getFlashModeConstants() {
        return Collections.unmodifiableMap(new HashMap<String, Object>() {
          {
            put("off", Constants.FLASH_OFF);
            put("on", Constants.FLASH_ON);
            put("auto", Constants.FLASH_AUTO);
            put("torch", Constants.FLASH_TORCH);
          }
        });
      }

      private Map<String, Object> getAutoFocusConstants() {
        return Collections.unmodifiableMap(new HashMap<String, Object>() {
          {
            put("on", true);
            put("off", false);
          }
        });
      }

      private Map<String, Object> getWhiteBalanceConstants() {
        return Collections.unmodifiableMap(new HashMap<String, Object>() {
          {
            put("auto", Constants.WB_AUTO);
            put("cloudy", Constants.WB_CLOUDY);
            put("sunny", Constants.WB_SUNNY);
            put("shadow", Constants.WB_SHADOW);
            put("fluorescent", Constants.WB_FLUORESCENT);
            put("incandescent", Constants.WB_INCANDESCENT);
          }
        });
      }

      private Map<String, Object> getVideoQualityConstants() {
        return Collections.unmodifiableMap(new HashMap<String, Object>() {
          {
            put("2160p", VIDEO_2160P);
            put("1080p", VIDEO_1080P);
            put("720p", VIDEO_720P);
            put("480p", VIDEO_480P);
            put("4:3", VIDEO_4x3);
          }
        });
      }

      private Map<String, Object> getBarCodeConstants() {
        return VALID_BARCODE_TYPES;
      }
    });
  }

  @ReactMethod
  public void takePicture(final ReadableMap options, final int viewTag, final Promise promise) {
    final ReactApplicationContext context = getReactApplicationContext();
    final File cacheDirectory = mScopedContext.getCacheDirectory();
    UIManagerModule uiManager = context.getNativeModule(UIManagerModule.class);
    uiManager.addUIBlock(new UIBlock() {
      @Override
      public void execute(NativeViewHierarchyManager nativeViewHierarchyManager) {
          RNCameraView cameraView = (RNCameraView) nativeViewHierarchyManager.resolveView(viewTag);
          try {
              if (!Build.FINGERPRINT.contains("generic")) {
                if (cameraView.isCameraOpened()) {
                  cameraView.takePicture(options, promise, cacheDirectory);
                } else {
                  promise.reject("E_CAMERA_UNAVAILABLE", "Camera is not running");
                }
              } else {
                  Bitmap image = RNCameraViewHelper.generateSimulatorPhoto(cameraView.getWidth(), cameraView.getHeight());
                  ByteArrayOutputStream stream = new ByteArrayOutputStream();
                  image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                  new ResolveTakenPictureAsyncTask(stream.toByteArray(), promise, options, cacheDirectory).execute();
              }
        } catch (Exception e) {
          promise.reject("E_CAMERA_BAD_VIEWTAG", "takePictureAsync: Expected a Camera component");
        }
      }
    });
  }

  @ReactMethod
  public void record(final ReadableMap options, final int viewTag, final Promise promise) {
      final ReactApplicationContext context = getReactApplicationContext();
      final File cacheDirectory = mScopedContext.getCacheDirectory();
      UIManagerModule uiManager = context.getNativeModule(UIManagerModule.class);

      uiManager.addUIBlock(new UIBlock() {
          @Override
          public void execute(NativeViewHierarchyManager nativeViewHierarchyManager) {
              final RNCameraView cameraView;

              try {
                  cameraView = (RNCameraView) nativeViewHierarchyManager.resolveView(viewTag);
                  if (cameraView.isCameraOpened()) {
                      cameraView.record(options, promise, cacheDirectory);
                  } else {
                      promise.reject("E_CAMERA_UNAVAILABLE", "Camera is not running");
                  }
              } catch (Exception e) {
                  promise.reject("E_CAMERA_BAD_VIEWTAG", "recordAsync: Expected a Camera component");
              }
          }
      });
  }

  @ReactMethod
  public void stopRecording(final int viewTag) {
      final ReactApplicationContext context = getReactApplicationContext();
      UIManagerModule uiManager = context.getNativeModule(UIManagerModule.class);
      uiManager.addUIBlock(new UIBlock() {
          @Override
          public void execute(NativeViewHierarchyManager nativeViewHierarchyManager) {
              final RNCameraView cameraView;

              try {
                  cameraView = (RNCameraView) nativeViewHierarchyManager.resolveView(viewTag);
                  if (cameraView.isCameraOpened()) {
                      cameraView.stopRecording();
                  }
              } catch (Exception e) {
                  e.printStackTrace();
              }
          }
      });
  }

  @ReactMethod
  public void getSupportedRatios(final int viewTag, final Promise promise) {
      final ReactApplicationContext context = getReactApplicationContext();
      UIManagerModule uiManager = context.getNativeModule(UIManagerModule.class);
      uiManager.addUIBlock(new UIBlock() {
          @Override
          public void execute(NativeViewHierarchyManager nativeViewHierarchyManager) {
              final RNCameraView cameraView;
              try {
                  cameraView = (RNCameraView) nativeViewHierarchyManager.resolveView(viewTag);
                  WritableArray result = Arguments.createArray();
                  if (cameraView.isCameraOpened()) {
                      Set<AspectRatio> ratios = cameraView.getSupportedAspectRatios();
                      for (AspectRatio ratio : ratios) {
                          result.pushString(ratio.toString());
                      }
                      promise.resolve(result);
                  } else {
                      promise.reject("E_CAMERA_UNAVAILABLE", "Camera is not running");
                  }
              } catch (Exception e) {
                  e.printStackTrace();
              }
          }
      });
  }
}
