package org.reactnative.camera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.CamcorderProfile;
import android.media.MediaActionSound;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.SparseArray;
import android.view.View;
import android.os.AsyncTask;
import com.facebook.react.bridge.*;
import com.facebook.react.uimanager.ThemedReactContext;
import com.google.android.cameraview.CameraView;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import org.reactnative.barcodedetector.RNBarcodeDetector;
import org.reactnative.camera.tasks.*;
import org.reactnative.camera.utils.ImageDimensions;
import org.reactnative.camera.utils.RNFileUtils;
import org.reactnative.facedetector.RNFaceDetector;

import java.io.File;
import java.io.IOException;
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

  private boolean mIsPaused = false;
  private boolean mIsNew = true;
  private boolean invertImageData = false;

  // Concurrency lock for scanners to avoid flooding the runtime
  public volatile boolean barCodeScannerTaskLock = false;
  public volatile boolean faceDetectorTaskLock = false;
  public volatile boolean googleBarcodeDetectorTaskLock = false;
  public volatile boolean textRecognizerTaskLock = false;

  // Scanning-related properties
  private MultiFormatReader mMultiFormatReader;
  private RNFaceDetector mFaceDetector;
  private RNBarcodeDetector mGoogleBarcodeDetector;
  private TextRecognizer mTextRecognizer;
  private boolean mShouldDetectFaces = false;
  private boolean mShouldGoogleDetectBarcodes = false;
  private boolean mShouldScanBarCodes = false;
  private boolean mShouldRecognizeText = false;
  private int mFaceDetectorMode = RNFaceDetector.FAST_MODE;
  private int mFaceDetectionLandmarks = RNFaceDetector.NO_LANDMARKS;
  private int mFaceDetectionClassifications = RNFaceDetector.NO_CLASSIFICATIONS;
  private int mGoogleVisionBarCodeType = Barcode.ALL_FORMATS;
  private int mGoogleVisionBarCodeMode = RNBarcodeDetector.NORMAL_MODE;

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
      public void onPictureTaken(CameraView cameraView, final byte[] data) {
        Promise promise = mPictureTakenPromises.poll();
        ReadableMap options = mPictureTakenOptions.remove(promise);
        if (options.hasKey("fastMode") && options.getBoolean("fastMode")) {
            promise.resolve(null);
        }
        final File cacheDirectory = mPictureTakenDirectories.remove(promise);
        if(Build.VERSION.SDK_INT >= 11/*HONEYCOMB*/) {
          new ResolveTakenPictureAsyncTask(data, promise, options, cacheDirectory, RNCameraView.this)
                  .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
          new ResolveTakenPictureAsyncTask(data, promise, options, cacheDirectory, RNCameraView.this)
                  .execute();
        }
        RNCameraViewHelper.emitPictureTakenEvent(cameraView);
      }

      @Override
      public void onVideoRecorded(CameraView cameraView, String path) {
        if (mVideoRecordedPromise != null) {
          if (path != null) {
            WritableMap result = Arguments.createMap();
            result.putString("uri", RNFileUtils.uriFromFile(new File(path)).toString());
            mVideoRecordedPromise.resolve(result);
          } else {
            mVideoRecordedPromise.reject("E_RECORDING", "Couldn't stop recording - there is none in progress");
          }
          mVideoRecordedPromise = null;
        }
      }

      @Override
      public void onFramePreview(CameraView cameraView, byte[] data, int width, int height, int rotation) {
        int correctRotation = RNCameraViewHelper.getCorrectCameraRotation(rotation, getFacing());
        boolean willCallBarCodeTask = mShouldScanBarCodes && !barCodeScannerTaskLock && cameraView instanceof BarCodeScannerAsyncTaskDelegate;
        boolean willCallFaceTask = mShouldDetectFaces && !faceDetectorTaskLock && cameraView instanceof FaceDetectorAsyncTaskDelegate;
        boolean willCallGoogleBarcodeTask = mShouldGoogleDetectBarcodes && !googleBarcodeDetectorTaskLock && cameraView instanceof BarcodeDetectorAsyncTaskDelegate;
        boolean willCallTextTask = mShouldRecognizeText && !textRecognizerTaskLock && cameraView instanceof TextRecognizerAsyncTaskDelegate;
        if (!willCallBarCodeTask && !willCallFaceTask && !willCallGoogleBarcodeTask && !willCallTextTask) {
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
          new FaceDetectorAsyncTask(delegate, mFaceDetector, data, width, height, correctRotation).execute();
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
          new BarcodeDetectorAsyncTask(delegate, mGoogleBarcodeDetector, data, width, height, correctRotation).execute();
        }

        if (willCallTextTask) {
          textRecognizerTaskLock = true;
          TextRecognizerAsyncTaskDelegate delegate = (TextRecognizerAsyncTaskDelegate) cameraView;
          new TextRecognizerAsyncTask(delegate, mTextRecognizer, data, width, height, correctRotation).execute();
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
    preview.layout(paddingX, paddingY, correctWidth + paddingX, correctHeight + paddingY);
  }

  @SuppressLint("all")
  @Override
  public void requestLayout() {
    // React handles this for us, so we don't need to call super.requestLayout();
  }

  @Override
  public void onViewAdded(View child) {
    if (this.getView() == child || this.getView() == null) return;
    // remove and read view to make sure it is in the back.
    // @TODO figure out why there was a z order issue in the first place and fix accordingly.
    this.removeView(this.getView());
    this.addView(this.getView(), 0);
  }

  public void setBarCodeTypes(List<String> barCodeTypes) {
    mBarCodeTypes = barCodeTypes;
    initBarcodeReader();
  }

  public void setPlaySoundOnCapture(Boolean playSoundOnCapture) {
    mPlaySoundOnCapture = playSoundOnCapture;
  }

  public void takePicture(ReadableMap options, final Promise promise, File cacheDirectory) {
    mPictureTakenPromises.add(promise);
    mPictureTakenOptions.put(promise, options);
    mPictureTakenDirectories.put(promise, cacheDirectory);
    if (mPlaySoundOnCapture) {
      MediaActionSound sound = new MediaActionSound();
      sound.play(MediaActionSound.SHUTTER_CLICK);
    }
    try {
      super.takePicture(options);
    } catch (Exception e) {
      mPictureTakenPromises.remove(promise);
      mPictureTakenOptions.remove(promise);
      mPictureTakenDirectories.remove(promise);
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

      boolean recordAudio = !options.hasKey("mute");

      if (super.record(path, maxDuration * 1000, maxFileSize, recordAudio, profile)) {
        mVideoRecordedPromise = promise;
      } else {
        promise.reject("E_RECORDING_FAILED", "Starting video recording failed. Another recording might be in progress.");
      }
    } catch (IOException e) {
      promise.reject("E_RECORDING_FAILED", "Starting video recording failed - could not create video file.");
    }
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
          decodeFormats.add(BarcodeFormat.valueOf(code));
        }
      }
    }

    hints.put(DecodeHintType.POSSIBLE_FORMATS, decodeFormats);
    mMultiFormatReader.setHints(hints);
  }

  public void setShouldScanBarCodes(boolean shouldScanBarCodes) {
    if (shouldScanBarCodes && mMultiFormatReader == null) {
      initBarcodeReader();
    }
    this.mShouldScanBarCodes = shouldScanBarCodes;
    setScanning(mShouldDetectFaces || mShouldGoogleDetectBarcodes || mShouldScanBarCodes || mShouldRecognizeText);
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
    mFaceDetector.setTracking(true);
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

  public void setShouldDetectFaces(boolean shouldDetectFaces) {
    if (shouldDetectFaces && mFaceDetector == null) {
      setupFaceDetector();
    }
    this.mShouldDetectFaces = shouldDetectFaces;
    setScanning(mShouldDetectFaces || mShouldGoogleDetectBarcodes || mShouldScanBarCodes || mShouldRecognizeText);
  }

  public void setShouldGoogleDetectBarcodes(boolean shouldDetectBarcodes) {
    if (shouldDetectBarcodes && mGoogleBarcodeDetector == null) {
      setupBarcodeDetector();
    }
    this.mShouldGoogleDetectBarcodes = shouldDetectBarcodes;
    setScanning(mShouldDetectFaces || mShouldGoogleDetectBarcodes || mShouldScanBarCodes || mShouldRecognizeText);
  }

  public void onFacesDetected(SparseArray<Face> facesReported, int sourceWidth, int sourceHeight, int sourceRotation) {
    if (!mShouldDetectFaces) {
      return;
    }

    SparseArray<Face> facesDetected = facesReported == null ? new SparseArray<Face>() : facesReported;

    ImageDimensions dimensions = new ImageDimensions(sourceWidth, sourceHeight, sourceRotation, getFacing());
    RNCameraViewHelper.emitFacesDetectedEvent(this, facesDetected, dimensions);
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

  /**
   * Initial setup of the text recongizer
   */
  private void setupTextRecongnizer() {
    mTextRecognizer = new TextRecognizer.Builder(mThemedReactContext).build();
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

  public void onBarcodesDetected(SparseArray<Barcode> barcodesReported, int sourceWidth, int sourceHeight, int sourceRotation) {
    if (!mShouldGoogleDetectBarcodes) {
      return;
    }

    SparseArray<Barcode> barcodesDetected = barcodesReported == null ? new SparseArray<Barcode>() : barcodesReported;

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

  public void setShouldRecognizeText(boolean shouldRecognizeText) {
    if (shouldRecognizeText && mTextRecognizer == null) {
      setupTextRecongnizer();
    }
    this.mShouldRecognizeText = shouldRecognizeText;
    setScanning(mShouldDetectFaces || mShouldGoogleDetectBarcodes || mShouldScanBarCodes || mShouldRecognizeText);
  }

  @Override
  public void onTextRecognized(SparseArray<TextBlock> textBlocks, int sourceWidth, int sourceHeight, int sourceRotation) {
    if (!mShouldRecognizeText) {
      return;
    }

    SparseArray<TextBlock> textBlocksDetected = textBlocks == null ? new SparseArray<TextBlock>() : textBlocks;
    ImageDimensions dimensions = new ImageDimensions(sourceWidth, sourceHeight, sourceRotation, getFacing());

    RNCameraViewHelper.emitTextRecognizedEvent(this, textBlocksDetected, dimensions);
  }

  @Override
  public void onTextRecognizerTaskCompleted() {
    textRecognizerTaskLock = false;
  }

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
    if (mTextRecognizer != null) {
      mTextRecognizer.release();
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
