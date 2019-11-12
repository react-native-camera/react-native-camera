package org.reactnative.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaActionSound;
import android.os.Build;
import androidx.core.content.ContextCompat;
import android.view.View;
import android.os.AsyncTask;
import com.facebook.react.bridge.*;
import com.facebook.react.uimanager.ThemedReactContext;
import com.google.android.cameraview.CameraView;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import org.reactnative.barcodedetector.RNBarcodeDetector;
import org.reactnative.camera.tasks.*;
import org.reactnative.camera.utils.RNFileUtils;
import org.reactnative.facedetector.RNFaceDetector;
import org.reactnative.camera.utils.YuvToBitmap;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class RNCameraView extends CameraView implements LifecycleEventListener, BarCodeScannerAsyncTaskDelegate, FaceDetectorAsyncTaskDelegate,
    BarcodeDetectorAsyncTaskDelegate, TextRecognizerAsyncTaskDelegate, PictureSavedDelegate {
  private ThemedReactContext mThemedReactContext;
  private Queue<Promise> mPictureTakenPromises = new ConcurrentLinkedQueue<>();
  private Map<Promise, ReadableMap> mPictureTakenOptions = new ConcurrentHashMap<>();
  private Map<Promise, File> mPictureTakenDirectories = new ConcurrentHashMap<>();
  private Promise mVideoRecordedPromise;
  private List<String> mBarCodeTypes = null;
  private Boolean mPlaySoundOnCapture = false;

  public static final String CAMERA1SCANNONE = "none";
  public static final String CAMERA1SCANECO = "eco";
  public static final String CAMERA1SCANFAST = "fast";
  public static final String CAMERA1SCANSUPERFAST = "boost";

  private YuvToBitmap mYuvToBitmap = null;
  private Matrix mRotationMatrix = null;
  private int mLastRotation = -1;
  private String mCamera1ScanMode = CAMERA1SCANECO;
  private boolean mProcessingPreview = false;
  private Bitmap mRotated = null;
  private final Object COLLECTION_LOCK = new Object();

  private boolean mIsPaused = false;
  private boolean mIsNew = true;
  private boolean invertImageData = false;
  private Boolean mIsRecording = false;
  private Boolean mIsRecordingInterrupted = false;

  // Concurrency lock for scanners to avoid flooding the runtime
  public volatile boolean barCodeScannerTaskLock = false;
  public volatile boolean faceDetectorTaskLock = false;
  public volatile boolean googleBarcodeDetectorTaskLock = false;
  public volatile boolean textRecognizerTaskLock = false;

  // Scanning-related properties
  private MultiFormatReader mMultiFormatReader;
  private RNFaceDetector mFaceDetector;
  private RNBarcodeDetector mGoogleBarcodeDetector;
  private boolean mShouldDetectFaces = false;
  private boolean mShouldGoogleDetectBarcodes = false;
  private boolean mShouldScanBarCodes = false;
  private boolean mShouldRecognizeText = false;
  private int mFaceDetectorMode = RNFaceDetector.FAST_MODE;
  private int mFaceDetectionLandmarks = RNFaceDetector.NO_LANDMARKS;
  private int mFaceDetectionClassifications = RNFaceDetector.NO_CLASSIFICATIONS;
  private int mGoogleVisionBarCodeType = RNBarcodeDetector.ALL_FORMATS;
  private int mGoogleVisionBarCodeMode = RNBarcodeDetector.NORMAL_MODE;
  private boolean mTrackingEnabled = true;
  private int mPaddingX;
  private int mPaddingY;

  private synchronized void storeRotated(Bitmap rotated) {
    mRotated = rotated;
  }

  public RNCameraView(ThemedReactContext themedReactContext) {
    super(themedReactContext, true);
    mThemedReactContext = themedReactContext;
    themedReactContext.addLifecycleEventListener(this);

    addCallback(new Callback() {
      @Override
      public void onCameraOpened(CameraView cameraView) {
        RNCameraViewHelper.emitCameraReadyEvent(cameraView);
      }

      @Override
      public void onMountError(CameraView cameraView) {
        RNCameraViewHelper.emitMountErrorEvent(cameraView, "Camera view threw an error - component could not be rendered.");
      }

      @Override
      public void onPictureTaken(CameraView cameraView, final byte[] data, int deviceOrientation) {
        Promise promise = mPictureTakenPromises.poll();
        ReadableMap options = mPictureTakenOptions.remove(promise);
        if (options.hasKey("fastMode") && options.getBoolean("fastMode")) {
            promise.resolve(null);
        }
        final File cacheDirectory = mPictureTakenDirectories.remove(promise);
        if(Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
          new ResolveTakenPictureAsyncTask(data, promise, options, cacheDirectory, deviceOrientation, RNCameraView.this)
                  .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
          new ResolveTakenPictureAsyncTask(data, promise, options, cacheDirectory, deviceOrientation, RNCameraView.this)
                  .execute();
        }
        RNCameraViewHelper.emitPictureTakenEvent(cameraView);
      }

      @Override
      public void onVideoRecorded(CameraView cameraView, String path, int videoOrientation, int deviceOrientation) {
        if (mVideoRecordedPromise != null) {
          if (path != null) {
            WritableMap result = Arguments.createMap();
            result.putBoolean("isRecordingInterrupted", mIsRecordingInterrupted);
            result.putInt("videoOrientation", videoOrientation);
            result.putInt("deviceOrientation", deviceOrientation);
            result.putString("uri", RNFileUtils.uriFromFile(new File(path)).toString());
            mVideoRecordedPromise.resolve(result);
          } else {
            mVideoRecordedPromise.reject("E_RECORDING", "Couldn't stop recording - there is none in progress");
          }
          mIsRecording = false;
          mIsRecordingInterrupted = false;
          mVideoRecordedPromise = null;
        }
      }

      @Override
      public void onFramePreview(CameraView cameraView, final byte[] data, final int width, final int height, final int rotation) {
        final int correctRotation = RNCameraViewHelper.getCorrectCameraRotation(rotation, getFacing(), getCameraOrientation());
        boolean willCallBarCodeTask = mShouldScanBarCodes && !barCodeScannerTaskLock && cameraView instanceof BarCodeScannerAsyncTaskDelegate;
        boolean willCallFaceTask = mShouldDetectFaces && !faceDetectorTaskLock && cameraView instanceof FaceDetectorAsyncTaskDelegate;
        boolean willCallGoogleBarcodeTask = mShouldGoogleDetectBarcodes && !googleBarcodeDetectorTaskLock && cameraView instanceof BarcodeDetectorAsyncTaskDelegate;
        boolean willCallTextTask = mShouldRecognizeText && !textRecognizerTaskLock && cameraView instanceof TextRecognizerAsyncTaskDelegate;
        Promise pictureTakenPromise;
        synchronized (COLLECTION_LOCK) {
          pictureTakenPromise = mPictureTakenPromises.poll();
        }
        final boolean superFast = mCamera1ScanMode.equals(CAMERA1SCANSUPERFAST) && !mProcessingPreview;

        if (!willCallBarCodeTask && !willCallFaceTask && !willCallGoogleBarcodeTask && !willCallTextTask
                && pictureTakenPromise  == null && !superFast) {
          return;
        }

        if (data.length < (1.5 * width * height)) {
            return;
        }

        if (willCallBarCodeTask) {
          barCodeScannerTaskLock = true;
          BarCodeScannerAsyncTaskDelegate delegate = (BarCodeScannerAsyncTaskDelegate) cameraView;
          new BarCodeScannerAsyncTask(delegate, mMultiFormatReader, data, width, height).execute();
        }

        if (willCallFaceTask) {
          faceDetectorTaskLock = true;
          FaceDetectorAsyncTaskDelegate delegate = (FaceDetectorAsyncTaskDelegate) cameraView;
          new FaceDetectorAsyncTask(delegate, mFaceDetector, data, width, height, correctRotation, getResources().getDisplayMetrics().density, getFacing(), getWidth(), getHeight(), mPaddingX, mPaddingY).execute();
        }

        if (willCallGoogleBarcodeTask) {
          googleBarcodeDetectorTaskLock = true;
          if (mGoogleVisionBarCodeMode == RNBarcodeDetector.NORMAL_MODE) {
            invertImageData = false;
          } else if (mGoogleVisionBarCodeMode == RNBarcodeDetector.ALTERNATE_MODE) {
            invertImageData = !invertImageData;
          } else if (mGoogleVisionBarCodeMode == RNBarcodeDetector.INVERTED_MODE) {
            invertImageData = true;
          }
          if (invertImageData) {
            for (int y = 0; y < data.length; y++) {
              data[y] = (byte) ~data[y];
            }
          }
          BarcodeDetectorAsyncTaskDelegate delegate = (BarcodeDetectorAsyncTaskDelegate) cameraView;
          new BarcodeDetectorAsyncTask(delegate, mGoogleBarcodeDetector, data, width, height, correctRotation, getResources().getDisplayMetrics().density, getFacing(), getWidth(), getHeight(), mPaddingX, mPaddingY).execute();
        }

        if (willCallTextTask) {
          textRecognizerTaskLock = true;
          TextRecognizerAsyncTaskDelegate delegate = (TextRecognizerAsyncTaskDelegate) cameraView;
          new TextRecognizerAsyncTask(delegate, mThemedReactContext, data, width, height, correctRotation, getResources().getDisplayMetrics().density, getFacing(), getWidth(), getHeight(), mPaddingX, mPaddingY).execute();
        }

        if (pictureTakenPromise != null || superFast) {
          checkScanning();
          mProcessingPreview = true;

          if (mYuvToBitmap == null) {
            mYuvToBitmap = new YuvToBitmap(getContext());
          }

          final Promise aPictureTakenPromise = pictureTakenPromise;

          Thread thread = new Thread() {
            @Override
            public void run() {
              // Get RGB
              mYuvToBitmap.refreshBitmap(data, width, height);

              Bitmap rotated;
              if (correctRotation == 0) {
                rotated = mYuvToBitmap.getBimap();
              } else {
                // Rotate
                if (mLastRotation != correctRotation) {
                  mLastRotation = correctRotation;
                  mRotationMatrix = new Matrix();
                  mRotationMatrix.postRotate(correctRotation);
                }
                Bitmap bitmap = mYuvToBitmap.getBimap();
                rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mRotationMatrix, false);
              }

              mProcessingPreview = false;

              // Resolve
              synchronized (COLLECTION_LOCK) {
                Promise promise = aPictureTakenPromise;
                if (promise == null) {
                  promise = mPictureTakenPromises.poll();
                }
                if (promise == null) {
                  storeRotated(rotated);
                } else {
                  ReadableMap options = mPictureTakenOptions.remove(promise);
                  final File cacheDirectory = mPictureTakenDirectories.remove(promise);
                  resolveTakenPicture(rotated, options, promise, cacheDirectory);
                }
              }
            }
          };
          thread.start();
        }
      }
    });
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    View preview = getView();
    if (null == preview) {
      return;
    }
    float width = right - left;
    float height = bottom - top;
    float ratio = getAspectRatio().toFloat();
    int orientation = getResources().getConfiguration().orientation;
    int correctHeight;
    int correctWidth;
    this.setBackgroundColor(Color.BLACK);
    if (orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
      if (ratio * height < width) {
        correctHeight = (int) (width / ratio);
        correctWidth = (int) width;
      } else {
        correctWidth = (int) (height * ratio);
        correctHeight = (int) height;
      }
    } else {
      if (ratio * width > height) {
        correctHeight = (int) (width * ratio);
        correctWidth = (int) width;
      } else {
        correctWidth = (int) (height / ratio);
        correctHeight = (int) height;
      }
    }
    int paddingX = (int) ((width - correctWidth) / 2);
    int paddingY = (int) ((height - correctHeight) / 2);
    mPaddingX = paddingX;
    mPaddingY = paddingY;
    preview.layout(paddingX, paddingY, correctWidth + paddingX, correctHeight + paddingY);
  }

  @SuppressLint("all")
  @Override
  public void requestLayout() {
    // React handles this for us, so we don't need to call super.requestLayout();
  }

  public void setBarCodeTypes(List<String> barCodeTypes) {
    mBarCodeTypes = barCodeTypes;
    initBarcodeReader();
  }

  public void setPlaySoundOnCapture(Boolean playSoundOnCapture) {
    mPlaySoundOnCapture = playSoundOnCapture;
  }

  public Object getImpl() {
    try {
      Field field = CameraView.class.getDeclaredField("mImpl");
      field.setAccessible(true);
      Object value = field.get(this);
      field.setAccessible(false);
      return value;

    } catch (NoSuchFieldException e) {
      throw new RuntimeException(e);

    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
    }

  }

  private boolean usingCamera2() {
    Object impl = getImpl();

    if (impl == null) return false;

    String className = impl.getClass().getName();
    return className.contains("cameraview.Camera2");
  }

  private boolean useTakePicture() {
    return usingCamera2() || mCamera1ScanMode.equals(CAMERA1SCANNONE);
  }

  private void resolveTakenPicture(Bitmap picture, ReadableMap options, final Promise promise, File cacheDirectory) {
    ResolveTakenPictureAsyncTask task = new ResolveTakenPictureAsyncTask(
            picture, promise, options, cacheDirectory, 0, RNCameraView.this
    );

    if (Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
      task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    } else {
      task.execute();
    }
  }

  public void takePicture(ReadableMap options, final Promise promise, File cacheDirectory) {
    boolean takePicture = useTakePicture();

    if (mPlaySoundOnCapture) {
      AudioManager audioManager = (AudioManager)getContext().getSystemService(Context.AUDIO_SERVICE);
      if (audioManager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL) {
        MediaActionSound sound = new MediaActionSound();
        sound.play(MediaActionSound.SHUTTER_CLICK);
      }
    }

    if (!takePicture && mCamera1ScanMode.equals(CAMERA1SCANSUPERFAST) && mRotated != null) {
      resolveTakenPicture(mRotated, options, promise, cacheDirectory);
      storeRotated(null);
      return;
    }

    synchronized (COLLECTION_LOCK) {
      mPictureTakenPromises.add(promise);
      mPictureTakenOptions.put(promise, options);
      mPictureTakenDirectories.put(promise, cacheDirectory);
    }
    try {
      if (takePicture) {
        super.takePicture(options);
      } else {
        checkScanning();
      }
    } catch (Exception e) {
      synchronized (COLLECTION_LOCK) {
        mPictureTakenPromises.remove(promise);
        mPictureTakenOptions.remove(promise);
        mPictureTakenDirectories.remove(promise);
      }
      throw e;
    }
  }

  @Override
  public void onPictureSaved(WritableMap response) {
    RNCameraViewHelper.emitPictureSavedEvent(this, response);
  }

  public void record(ReadableMap options, final Promise promise, File cacheDirectory) {
    try {
      String path = options.hasKey("path") ? options.getString("path") : RNFileUtils.getOutputFilePath(cacheDirectory, ".mp4");
      int maxDuration = options.hasKey("maxDuration") ? options.getInt("maxDuration") : -1;
      int maxFileSize = options.hasKey("maxFileSize") ? options.getInt("maxFileSize") : -1;

      CamcorderProfile profile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
      if (options.hasKey("quality")) {
        profile = RNCameraViewHelper.getCamcorderProfile(options.getInt("quality"));
      }
      if (options.hasKey("videoBitrate")) {
        profile.videoBitRate = options.getInt("videoBitrate");
      }

      boolean recordAudio = true;
      if (options.hasKey("mute")) {
        recordAudio = !options.getBoolean("mute");
      }

      int orientation = Constants.ORIENTATION_AUTO;
      if (options.hasKey("orientation")) {
        orientation = options.getInt("orientation");
      }

      if (super.record(path, maxDuration * 1000, maxFileSize, recordAudio, profile, orientation)) {
        mIsRecording = true;
        mVideoRecordedPromise = promise;
      } else {
        promise.reject("E_RECORDING_FAILED", "Starting video recording failed. Another recording might be in progress.");
      }
    } catch (IOException e) {
      promise.reject("E_RECORDING_FAILED", "Starting video recording failed - could not create video file.");
    }
  }

  /**
   * Set camera1 scan mode (camera2 does not allow frame preview).
   * - none: use native takePicture()
   * - eco: run preview on picture request only
   * - fast: preview each frame and process when picture is requested
   */

  public void setCamera1ScanMode(String scanMode) {
    scanMode = scanMode.toLowerCase();
    if (scanMode.equals(CAMERA1SCANECO) || scanMode.equals(CAMERA1SCANFAST) || scanMode.equals(CAMERA1SCANSUPERFAST)) {
      mCamera1ScanMode = scanMode;
    } else {
      mCamera1ScanMode = CAMERA1SCANNONE;
    }
    checkScanning();
  }

  /**
   * Initialize the barcode decoder.
   * Supports all iOS codes except [code138, code39mod43, itf14]
   * Additionally supports [codabar, code128, maxicode, rss14, rssexpanded, upc_a, upc_ean]
   */
  private void initBarcodeReader() {
    mMultiFormatReader = new MultiFormatReader();
    EnumMap<DecodeHintType, Object> hints = new EnumMap<>(DecodeHintType.class);
    EnumSet<BarcodeFormat> decodeFormats = EnumSet.noneOf(BarcodeFormat.class);

    if (mBarCodeTypes != null) {
      for (String code : mBarCodeTypes) {
        String formatString = (String) CameraModule.VALID_BARCODE_TYPES.get(code);
        if (formatString != null) {
          decodeFormats.add(BarcodeFormat.valueOf(formatString));
        }
      }
    }

    hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
    mMultiFormatReader.setHints(hints);
  }

  private void checkScanning() {
    boolean fast = mCamera1ScanMode.equals(CAMERA1SCANFAST) || mCamera1ScanMode.equals(CAMERA1SCANSUPERFAST);
    boolean scanForPictures = mCamera1ScanMode != CAMERA1SCANNONE && (fast || !mPictureTakenPromises.isEmpty());
    setScanning(mShouldDetectFaces || mShouldGoogleDetectBarcodes || mShouldScanBarCodes || mShouldRecognizeText || scanForPictures);
  }

  public void setShouldScanBarCodes(boolean shouldScanBarCodes) {
    if (shouldScanBarCodes && mMultiFormatReader == null) {
      initBarcodeReader();
    }
    this.mShouldScanBarCodes = shouldScanBarCodes;
    checkScanning();
  }

  public void onBarCodeRead(Result barCode, int width, int height) {
    String barCodeType = barCode.getBarcodeFormat().toString();
    if (!mShouldScanBarCodes || !mBarCodeTypes.contains(barCodeType)) {
      return;
    }

    RNCameraViewHelper.emitBarCodeReadEvent(this, barCode,  width,  height);
  }

  public void onBarCodeScanningTaskCompleted() {
    barCodeScannerTaskLock = false;
    if(mMultiFormatReader != null) {
      mMultiFormatReader.reset();
    }
  }

  /**
   * Initial setup of the face detector
   */
  private void setupFaceDetector() {
    mFaceDetector = new RNFaceDetector(mThemedReactContext);
    mFaceDetector.setMode(mFaceDetectorMode);
    mFaceDetector.setLandmarkType(mFaceDetectionLandmarks);
    mFaceDetector.setClassificationType(mFaceDetectionClassifications);
    mFaceDetector.setTracking(mTrackingEnabled);
  }

  public void setFaceDetectionLandmarks(int landmarks) {
    mFaceDetectionLandmarks = landmarks;
    if (mFaceDetector != null) {
      mFaceDetector.setLandmarkType(landmarks);
    }
  }

  public void setFaceDetectionClassifications(int classifications) {
    mFaceDetectionClassifications = classifications;
    if (mFaceDetector != null) {
      mFaceDetector.setClassificationType(classifications);
    }
  }

  public void setFaceDetectionMode(int mode) {
    mFaceDetectorMode = mode;
    if (mFaceDetector != null) {
      mFaceDetector.setMode(mode);
    }
  }

  public void setTracking(boolean trackingEnabled) {
    mTrackingEnabled = trackingEnabled;
    if (mFaceDetector != null) {
      mFaceDetector.setTracking(trackingEnabled);
    }
  }

  public void setShouldDetectFaces(boolean shouldDetectFaces) {
    if (shouldDetectFaces && mFaceDetector == null) {
      setupFaceDetector();
    }
    this.mShouldDetectFaces = shouldDetectFaces;
    checkScanning();
  }

  public void onFacesDetected(WritableArray data) {
    if (!mShouldDetectFaces) {
      return;
    }

    RNCameraViewHelper.emitFacesDetectedEvent(this, data);
  }

  public void onFaceDetectionError(RNFaceDetector faceDetector) {
    if (!mShouldDetectFaces) {
      return;
    }

    RNCameraViewHelper.emitFaceDetectionErrorEvent(this, faceDetector);
  }

  @Override
  public void onFaceDetectingTaskCompleted() {
    faceDetectorTaskLock = false;
  }

  /**
   * Initial setup of the barcode detector
   */
  private void setupBarcodeDetector() {
    mGoogleBarcodeDetector = new RNBarcodeDetector(mThemedReactContext);
    mGoogleBarcodeDetector.setBarcodeType(mGoogleVisionBarCodeType);
  }

  public void setShouldGoogleDetectBarcodes(boolean shouldDetectBarcodes) {
    if (shouldDetectBarcodes && mGoogleBarcodeDetector == null) {
      setupBarcodeDetector();
    }
    this.mShouldGoogleDetectBarcodes = shouldDetectBarcodes;
    checkScanning();
  }

  public void setGoogleVisionBarcodeType(int barcodeType) {
    mGoogleVisionBarCodeType = barcodeType;
    if (mGoogleBarcodeDetector != null) {
      mGoogleBarcodeDetector.setBarcodeType(barcodeType);
    }
  }

  public void setGoogleVisionBarcodeMode(int barcodeMode) {
    mGoogleVisionBarCodeMode = barcodeMode;
  }

  public void onBarcodesDetected(WritableArray barcodesDetected) {
    if (!mShouldGoogleDetectBarcodes) {
      return;
    }
    RNCameraViewHelper.emitBarcodesDetectedEvent(this, barcodesDetected);
  }

  public void onBarcodeDetectionError(RNBarcodeDetector barcodeDetector) {
    if (!mShouldGoogleDetectBarcodes) {
      return;
    }

    RNCameraViewHelper.emitBarcodeDetectionErrorEvent(this, barcodeDetector);
  }

  @Override
  public void onBarcodeDetectingTaskCompleted() {
    googleBarcodeDetectorTaskLock = false;
  }

  /**
   *
   * Text recognition
   */

  public void setShouldRecognizeText(boolean shouldRecognizeText) {
    this.mShouldRecognizeText = shouldRecognizeText;
    checkScanning();
  }

  public void onTextRecognized(WritableArray serializedData) {
    if (!mShouldRecognizeText) {
      return;
    }

    RNCameraViewHelper.emitTextRecognizedEvent(this, serializedData);
  }

  @Override
  public void onTextRecognizerTaskCompleted() {
    textRecognizerTaskLock = false;
  }

  /**
  *
  * End Text Recognition */

  @Override
  public void onHostResume() {
    if (hasCameraPermissions()) {
      if ((mIsPaused && !isCameraOpened()) || mIsNew) {
        mIsPaused = false;
        mIsNew = false;
        start();
      }
    } else {
      RNCameraViewHelper.emitMountErrorEvent(this, "Camera permissions not granted - component could not be rendered.");
    }
  }

  @Override
  public void onHostPause() {
    if (mIsRecording) {
      mIsRecordingInterrupted = true;
    }
    if (!mIsPaused && isCameraOpened()) {
      mIsPaused = true;
      stop();
    }
  }

  @Override
  public void onHostDestroy() {
    if (mFaceDetector != null) {
      mFaceDetector.release();
    }
    if (mGoogleBarcodeDetector != null) {
      mGoogleBarcodeDetector.release();
    }
    mMultiFormatReader = null;
    stop();
    mThemedReactContext.removeLifecycleEventListener(this);
  }

  private boolean hasCameraPermissions() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      int result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA);
      return result == PackageManager.PERMISSION_GRANTED;
    } else {
      return true;
    }
  }
}
