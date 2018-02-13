package org.reactnative.camera;

import android.media.CamcorderProfile;
import android.os.Build;
import android.os.Build.VERSION;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.util.SparseArray;
import android.view.View;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.ThemedReactContext;
import com.google.android.cameraview.CameraView;
import com.google.android.cameraview.CameraView.Callback;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import java.io.File;
import java.io.IOException;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.reactnative.camera.tasks.BarCodeScannerAsyncTask;
import org.reactnative.camera.tasks.BarCodeScannerAsyncTaskDelegate;
import org.reactnative.camera.tasks.OpenCVProcessorAsyncTask;
import org.reactnative.camera.tasks.OpenCVProcessorAsyncTaskDelegate;
import org.reactnative.camera.tasks.ResolveTakenPictureAsyncTask;
import org.reactnative.camera.utils.ImageDimensions;
import org.reactnative.camera.utils.RNFileUtils;
import org.reactnative.facedetector.RNFaceDetector;
import org.reactnative.opencv.OpenCVProcessor;

public class RNCameraView extends CameraView implements LifecycleEventListener, BarCodeScannerAsyncTaskDelegate, OpenCVProcessorAsyncTaskDelegate {
  public volatile boolean barCodeScannerTaskLock = false;
  public volatile boolean faceDetectorTaskLock = false;
  private List<String> mBarCodeTypes = null;
  private int mFaceDetectionClassifications = RNFaceDetector.NO_CLASSIFICATIONS;
  private int mFaceDetectionLandmarks = RNFaceDetector.NO_LANDMARKS;
  private final RNFaceDetector mFaceDetector;
  private int mFaceDetectorMode = RNFaceDetector.FAST_MODE;
  private boolean mIsNew = true;
  private boolean mIsPaused = false;
  private final MultiFormatReader mMultiFormatReader = new MultiFormatReader();
  private Map<Promise, File> mPictureTakenDirectories = new ConcurrentHashMap();
  private Map<Promise, ReadableMap> mPictureTakenOptions = new ConcurrentHashMap();
  private Queue<Promise> mPictureTakenPromises = new ConcurrentLinkedQueue();
  private boolean mShouldDetectFaces = false;
  private boolean mShouldScanBarCodes = false;
  private ThemedReactContext mThemedReactContext;
  private Promise mVideoRecordedPromise;
  private final OpenCVProcessor openCVProcessor;

  class C08941 extends Callback {
    C08941() {
    }

    public void onCameraOpened(CameraView cameraView) {
      RNCameraViewHelper.emitCameraReadyEvent(cameraView);
    }

    public void onMountError(CameraView cameraView) {
      RNCameraViewHelper.emitMountErrorEvent(cameraView);
    }

    public void onPictureTaken(CameraView cameraView, byte[] data) {
      Promise promise = (Promise) RNCameraView.this.mPictureTakenPromises.poll();
      new ResolveTakenPictureAsyncTask(data, promise, (ReadableMap) RNCameraView.this.mPictureTakenOptions.remove(promise), (File) RNCameraView.this.mPictureTakenDirectories.remove(promise)).execute(new Void[0]);
    }

    public void onVideoRecorded(CameraView cameraView, String path) {
      if (RNCameraView.this.mVideoRecordedPromise != null) {
        if (path != null) {
          WritableMap result = Arguments.createMap();
          result.putString("uri", RNFileUtils.uriFromFile(new File(path)).toString());
          RNCameraView.this.mVideoRecordedPromise.resolve(result);
        } else {
          RNCameraView.this.mVideoRecordedPromise.reject("E_RECORDING", "Couldn't stop recording - there is none in progress");
        }
        RNCameraView.this.mVideoRecordedPromise = null;
      }
    }

    public void onFramePreview(CameraView cameraView, byte[] data, int width, int height, int rotation) {
      int correctRotation = RNCameraViewHelper.getCorrectCameraRotation(rotation, RNCameraView.this.getFacing());
      if (RNCameraView.this.mShouldScanBarCodes && !RNCameraView.this.barCodeScannerTaskLock && (cameraView instanceof BarCodeScannerAsyncTaskDelegate)) {
        RNCameraView.this.barCodeScannerTaskLock = true;
        new BarCodeScannerAsyncTask((BarCodeScannerAsyncTaskDelegate) cameraView, RNCameraView.this.mMultiFormatReader, data, width, height).execute(new Void[0]);
      }
      new OpenCVProcessorAsyncTask((OpenCVProcessorAsyncTaskDelegate) cameraView, RNCameraView.this.openCVProcessor, data, width, height, correctRotation).execute(new Void[0]);
    }
  }

  public RNCameraView(ThemedReactContext themedReactContext) {
    super(themedReactContext);
    initBarcodeReader();
    this.mThemedReactContext = themedReactContext;
    this.mFaceDetector = new RNFaceDetector(themedReactContext);
    this.openCVProcessor = new OpenCVProcessor(themedReactContext);
    setupFaceDetector();
    themedReactContext.addLifecycleEventListener(this);
    addCallback(new C08941());
  }

  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    View preview = getView();
    if (preview != null) {
      setBackgroundColor(ViewCompat.MEASURED_STATE_MASK);
      preview.layout(0, 0, right - left, bottom - top);
    }
  }

  public void requestLayout() {
  }

  public void onViewAdded(View child) {
    if (getView() != child && getView() != null) {
      removeView(getView());
      addView(getView(), 0);
    }
  }

  public void setBarCodeTypes(List<String> barCodeTypes) {
    this.mBarCodeTypes = barCodeTypes;
    initBarcodeReader();
  }

  public void takePicture(ReadableMap options, Promise promise, File cacheDirectory) {
    this.mPictureTakenPromises.add(promise);
    this.mPictureTakenOptions.put(promise, options);
    this.mPictureTakenDirectories.put(promise, cacheDirectory);
    super.takePicture();
  }

  public void record(ReadableMap options, Promise promise, File cacheDirectory) {
    boolean recordAudio = true;
    int maxFileSize = -1;
    try {
      int maxDuration;
      String path = RNFileUtils.getOutputFilePath(cacheDirectory, ".mp4");
      if (options.hasKey("maxDuration")) {
        maxDuration = options.getInt("maxDuration");
      } else {
        maxDuration = -1;
      }
      if (options.hasKey("maxFileSize")) {
        maxFileSize = options.getInt("maxFileSize");
      }
      CamcorderProfile profile = CamcorderProfile.get(1);
      if (options.hasKey("quality")) {
        profile = RNCameraViewHelper.getCamcorderProfile(options.getInt("quality"));
      }
      if (options.hasKey("mute")) {
        recordAudio = false;
      }
      if (super.record(path, maxDuration * 1000, maxFileSize, recordAudio, profile)) {
        this.mVideoRecordedPromise = promise;
      } else {
        promise.reject("E_RECORDING_FAILED", "Starting video recording failed. Another recording might be in progress.");
      }
    } catch (IOException e) {
      promise.reject("E_RECORDING_FAILED", "Starting video recording failed - could not create video file.");
    }
  }

  private void initBarcodeReader() {
    EnumMap<DecodeHintType, Object> hints = new EnumMap(DecodeHintType.class);
    EnumSet<BarcodeFormat> decodeFormats = EnumSet.noneOf(BarcodeFormat.class);
    if (this.mBarCodeTypes != null) {
      for (String code : this.mBarCodeTypes) {
        if (((String) CameraModule.VALID_BARCODE_TYPES.get(code)) != null) {
          decodeFormats.add(BarcodeFormat.valueOf(code));
        }
      }
    }
    hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
    this.mMultiFormatReader.setHints(hints);
  }

  public void setShouldScanBarCodes(boolean shouldScanBarCodes) {
    this.mShouldScanBarCodes = shouldScanBarCodes;
    boolean z = this.mShouldDetectFaces || this.mShouldScanBarCodes;
    setScanning(z);
  }

  public void onBarCodeRead(Result barCode) {
    String barCodeType = barCode.getBarcodeFormat().toString();
    if (this.mShouldScanBarCodes && this.mBarCodeTypes.contains(barCodeType)) {
      RNCameraViewHelper.emitBarCodeReadEvent(this, barCode);
    }
  }

  public void onBarCodeScanningTaskCompleted() {
    this.barCodeScannerTaskLock = false;
    this.mMultiFormatReader.reset();
  }

  private void setupFaceDetector() {
    this.mFaceDetector.setMode(this.mFaceDetectorMode);
    this.mFaceDetector.setLandmarkType(this.mFaceDetectionLandmarks);
    this.mFaceDetector.setClassificationType(this.mFaceDetectionClassifications);
    this.mFaceDetector.setTracking(true);
  }

  public void setFaceDetectionLandmarks(int landmarks) {
    this.mFaceDetectionLandmarks = landmarks;
    if (this.mFaceDetector != null) {
      this.mFaceDetector.setLandmarkType(landmarks);
    }
  }

  public void setFaceDetectionClassifications(int classifications) {
    this.mFaceDetectionClassifications = classifications;
    if (this.mFaceDetector != null) {
      this.mFaceDetector.setClassificationType(classifications);
    }
  }

  public void setFaceDetectionMode(int mode) {
    this.mFaceDetectorMode = mode;
    if (this.mFaceDetector != null) {
      this.mFaceDetector.setMode(mode);
    }
  }

  public void setShouldDetectFaces(boolean shouldDetectFaces) {
    this.mShouldDetectFaces = shouldDetectFaces;
    boolean z = this.mShouldDetectFaces || this.mShouldScanBarCodes;
    setScanning(z);
  }

  public void onFacesDetected(SparseArray<Map<String, Float>> facesReported, int sourceWidth, int sourceHeight, int sourceRotation) {
    if (this.mShouldDetectFaces && facesReported.size() != 0) {
      RNCameraViewHelper.emitFacesDetectedEvent(this, facesReported, new ImageDimensions(sourceWidth, sourceHeight, sourceRotation, getFacing()));
    }
  }

  public void onFaceDetectionError(OpenCVProcessor openCVProcessor) {
  }

  public void onFaceDetectionError(RNFaceDetector faceDetector) {
    if (this.mShouldDetectFaces) {
      RNCameraViewHelper.emitFaceDetectionErrorEvent(this, faceDetector);
    }
  }

  public void onFaceDetectingTaskCompleted() {
    this.faceDetectorTaskLock = false;
  }

  public void onHostResume() {
    if (!hasCameraPermissions()) {
      Arguments.createMap().putString("message", "Camera permissions not granted - component could not be rendered.");
      RNCameraViewHelper.emitMountErrorEvent(this);
    } else if ((this.mIsPaused && !isCameraOpened()) || this.mIsNew) {
      this.mIsPaused = false;
      this.mIsNew = false;
      if (!Build.FINGERPRINT.contains("generic")) {
        start();
      }
    }
  }

  public void onHostPause() {
    if (!this.mIsPaused && isCameraOpened()) {
      this.mIsPaused = true;
      stop();
    }
  }

  public void onHostDestroy() {
    this.mFaceDetector.release();
    stop();
  }

  private boolean hasCameraPermissions() {
    if (VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(getContext(), "android.permission.CAMERA") == 0) {
      return true;
    }
    return false;
  }
}