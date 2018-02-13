package org.reactnative.camera;

import android.graphics.Bitmap;
import android.os.Build;
import android.support.media.ExifInterface;
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
import com.facebook.react.uimanager.ViewProps;
import com.facebook.react.views.scroll.ReactScrollViewHelper;
import com.google.android.cameraview.AspectRatio;
import com.google.zxing.BarcodeFormat;
import com.lwansbrough.RCTCamera.RCTCameraModule;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import org.reactnative.camera.tasks.ResolveTakenPictureAsyncTask;
import org.reactnative.camera.utils.ScopedContext;
import org.reactnative.facedetector.RNFaceDetector;

public class CameraModule extends ReactContextBaseJavaModule {
    private static final String TAG = "CameraModule";
    public static final Map<String, Object> VALID_BARCODE_TYPES = Collections.unmodifiableMap(new C06121());
    static final int VIDEO_1080P = 1;
    static final int VIDEO_2160P = 0;
    static final int VIDEO_480P = 3;
    static final int VIDEO_4x3 = 4;
    static final int VIDEO_720P = 2;
    private ScopedContext mScopedContext;

    static class C06121 extends HashMap<String, Object> {
        C06121() {
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
    }

    class C06222 extends HashMap<String, Object> {

        class C06161 extends HashMap<String, Object> {

            class C06131 extends HashMap<String, Object> {
                C06131() {
                    put("fast", Integer.valueOf(RNFaceDetector.FAST_MODE));
                    put("accurate", Integer.valueOf(RNFaceDetector.ACCURATE_MODE));
                }
            }

            class C06142 extends HashMap<String, Object> {
                C06142() {
                    put("all", Integer.valueOf(RNFaceDetector.ALL_CLASSIFICATIONS));
                    put("none", Integer.valueOf(RNFaceDetector.NO_CLASSIFICATIONS));
                }
            }

            class C06153 extends HashMap<String, Object> {
                C06153() {
                    put("all", Integer.valueOf(RNFaceDetector.ALL_LANDMARKS));
                    put("none", Integer.valueOf(RNFaceDetector.NO_LANDMARKS));
                }
            }

            C06161() {
                put("Mode", getFaceDetectionModeConstants());
                put("Landmarks", getFaceDetectionLandmarksConstants());
                put("Classifications", getFaceDetectionClassificationsConstants());
            }

            private Map<String, Object> getFaceDetectionModeConstants() {
                return Collections.unmodifiableMap(new C06131());
            }

            private Map<String, Object> getFaceDetectionClassificationsConstants() {
                return Collections.unmodifiableMap(new C06142());
            }

            private Map<String, Object> getFaceDetectionLandmarksConstants() {
                return Collections.unmodifiableMap(new C06153());
            }
        }

        class C06172 extends HashMap<String, Object> {
            C06172() {
                put("front", Integer.valueOf(1));
                put("back", Integer.valueOf(0));
            }
        }

        class C06183 extends HashMap<String, Object> {
            C06183() {
                put("off", Integer.valueOf(0));
                put(ViewProps.ON, Integer.valueOf(1));
                put(ReactScrollViewHelper.AUTO, Integer.valueOf(3));
                put("torch", Integer.valueOf(2));
            }
        }

        class C06194 extends HashMap<String, Object> {
            C06194() {
                put(ViewProps.ON, Boolean.valueOf(true));
                put("off", Boolean.valueOf(false));
            }
        }

        class C06205 extends HashMap<String, Object> {
            C06205() {
                put(ReactScrollViewHelper.AUTO, Integer.valueOf(0));
                put("cloudy", Integer.valueOf(1));
                put("sunny", Integer.valueOf(2));
                put("shadow", Integer.valueOf(3));
                put("fluorescent", Integer.valueOf(4));
                put("incandescent", Integer.valueOf(5));
            }
        }

        class C06216 extends HashMap<String, Object> {
            C06216() {
                put("2160p", Integer.valueOf(0));
                put(RCTCameraModule.RCT_CAMERA_CAPTURE_QUALITY_1080P, Integer.valueOf(1));
                put(RCTCameraModule.RCT_CAMERA_CAPTURE_QUALITY_720P, Integer.valueOf(2));
                put(RCTCameraModule.RCT_CAMERA_CAPTURE_QUALITY_480P, Integer.valueOf(3));
                put("4:3", Integer.valueOf(4));
            }
        }

        C06222() {
            put("Type", getTypeConstants());
            put("FlashMode", getFlashModeConstants());
            put("AutoFocus", getAutoFocusConstants());
            put(ExifInterface.TAG_WHITE_BALANCE, getWhiteBalanceConstants());
            put("VideoQuality", getVideoQualityConstants());
            put("BarCodeType", getBarCodeConstants());
            put("FaceDetection", Collections.unmodifiableMap(new C06161()));
        }

        private Map<String, Object> getTypeConstants() {
            return Collections.unmodifiableMap(new C06172());
        }

        private Map<String, Object> getFlashModeConstants() {
            return Collections.unmodifiableMap(new C06183());
        }

        private Map<String, Object> getAutoFocusConstants() {
            return Collections.unmodifiableMap(new C06194());
        }

        private Map<String, Object> getWhiteBalanceConstants() {
            return Collections.unmodifiableMap(new C06205());
        }

        private Map<String, Object> getVideoQualityConstants() {
            return Collections.unmodifiableMap(new C06216());
        }

        private Map<String, Object> getBarCodeConstants() {
            return CameraModule.VALID_BARCODE_TYPES;
        }
    }

    public CameraModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.mScopedContext = new ScopedContext(reactContext);
    }

    public ScopedContext getScopedContext() {
        return this.mScopedContext;
    }

    public String getName() {
        return "RNCameraModule";
    }

    @Nullable
    public Map<String, Object> getConstants() {
        return Collections.unmodifiableMap(new C06222());
    }

    @ReactMethod
    public void takePicture(ReadableMap options, int viewTag, Promise promise) {
        ReactApplicationContext context = getReactApplicationContext();
        final File cacheDirectory = this.mScopedContext.getCacheDirectory();
        final int i = viewTag;
        final ReadableMap readableMap = options;
        final Promise promise2 = promise;
        ((UIManagerModule) context.getNativeModule(UIManagerModule.class)).addUIBlock(new UIBlock() {
            public void execute(NativeViewHierarchyManager nativeViewHierarchyManager) {
                RNCameraView cameraView = (RNCameraView) nativeViewHierarchyManager.resolveView(i);
                try {
                    if (Build.FINGERPRINT.contains("generic")) {
                        Bitmap image = RNCameraViewHelper.generateSimulatorPhoto(cameraView.getWidth(), cameraView.getHeight());
                        ByteBuffer byteBuffer = ByteBuffer.allocate(image.getRowBytes() * image.getHeight());
                        image.copyPixelsToBuffer(byteBuffer);
                        new ResolveTakenPictureAsyncTask(byteBuffer.array(), promise2, readableMap).execute(new Void[0]);
                    } else if (cameraView.isCameraOpened()) {
                        cameraView.takePicture(readableMap, promise2, cacheDirectory);
                    } else {
                        promise2.reject("E_CAMERA_UNAVAILABLE", "Camera is not running");
                    }
                } catch (Exception e) {
                    promise2.reject("E_CAMERA_BAD_VIEWTAG", "takePictureAsync: Expected a Camera component");
                }
            }
        });
    }

    @ReactMethod
    public void record(ReadableMap options, int viewTag, Promise promise) {
        ReactApplicationContext context = getReactApplicationContext();
        final File cacheDirectory = this.mScopedContext.getCacheDirectory();
        final int i = viewTag;
        final ReadableMap readableMap = options;
        final Promise promise2 = promise;
        ((UIManagerModule) context.getNativeModule(UIManagerModule.class)).addUIBlock(new UIBlock() {
            public void execute(NativeViewHierarchyManager nativeViewHierarchyManager) {
                try {
                    RNCameraView cameraView = (RNCameraView) nativeViewHierarchyManager.resolveView(i);
                    if (cameraView.isCameraOpened()) {
                        cameraView.record(readableMap, promise2, cacheDirectory);
                    } else {
                        promise2.reject("E_CAMERA_UNAVAILABLE", "Camera is not running");
                    }
                } catch (Exception e) {
                    promise2.reject("E_CAMERA_BAD_VIEWTAG", "recordAsync: Expected a Camera component");
                }
            }
        });
    }

    @ReactMethod
    public void stopRecording(final int viewTag) {
        ((UIManagerModule) getReactApplicationContext().getNativeModule(UIManagerModule.class)).addUIBlock(new UIBlock() {
            public void execute(NativeViewHierarchyManager nativeViewHierarchyManager) {
                try {
                    RNCameraView cameraView = (RNCameraView) nativeViewHierarchyManager.resolveView(viewTag);
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
        ((UIManagerModule) getReactApplicationContext().getNativeModule(UIManagerModule.class)).addUIBlock(new UIBlock() {
            public void execute(NativeViewHierarchyManager nativeViewHierarchyManager) {
                try {
                    RNCameraView cameraView = (RNCameraView) nativeViewHierarchyManager.resolveView(viewTag);
                    WritableArray result = Arguments.createArray();
                    if (cameraView.isCameraOpened()) {
                        for (AspectRatio ratio : cameraView.getSupportedAspectRatios()) {
                            result.pushString(ratio.toString());
                        }
                        promise.resolve(result);
                        return;
                    }
                    promise.reject("E_CAMERA_UNAVAILABLE", "Camera is not running");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}