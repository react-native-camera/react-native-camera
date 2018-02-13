package org.reactnative.camera;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.media.CamcorderProfile;
import android.os.Build.VERSION;
import android.support.media.ExifInterface;
import android.support.v4.view.InputDeviceCompat;
import android.support.v4.view.ViewCompat;
import android.util.SparseArray;
import android.view.ViewGroup;
import com.drew.metadata.exif.makernotes.OlympusMakernoteDirectory;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.UIManagerModule;
import com.google.zxing.Result;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import org.opencv.imgproc.Imgproc;
import org.reactnative.camera.events.BarCodeReadEvent;
import org.reactnative.camera.events.CameraMountErrorEvent;
import org.reactnative.camera.events.CameraReadyEvent;
import org.reactnative.camera.events.FaceDetectionErrorEvent;
import org.reactnative.camera.events.FacesDetectedEvent;
import org.reactnative.camera.utils.ImageDimensions;
import org.reactnative.facedetector.RNFaceDetector;

public class RNCameraViewHelper {
  public static final String[][] exifTags;

  static {
    String[][] strArr = new String[129][];
    strArr[0] = new String[]{"string", ExifInterface.TAG_ARTIST};
    strArr[1] = new String[]{"int", ExifInterface.TAG_BITS_PER_SAMPLE};
    strArr[2] = new String[]{"int", ExifInterface.TAG_COMPRESSION};
    strArr[3] = new String[]{"string", ExifInterface.TAG_COPYRIGHT};
    strArr[4] = new String[]{"string", ExifInterface.TAG_DATETIME};
    strArr[5] = new String[]{"string", ExifInterface.TAG_IMAGE_DESCRIPTION};
    strArr[6] = new String[]{"int", ExifInterface.TAG_IMAGE_LENGTH};
    strArr[7] = new String[]{"int", ExifInterface.TAG_IMAGE_WIDTH};
    strArr[8] = new String[]{"int", ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT};
    strArr[9] = new String[]{"int", ExifInterface.TAG_JPEG_INTERCHANGE_FORMAT_LENGTH};
    strArr[10] = new String[]{"string", ExifInterface.TAG_MAKE};
    strArr[11] = new String[]{"string", ExifInterface.TAG_MODEL};
    strArr[12] = new String[]{"int", ExifInterface.TAG_ORIENTATION};
    strArr[13] = new String[]{"int", ExifInterface.TAG_PHOTOMETRIC_INTERPRETATION};
    strArr[14] = new String[]{"int", ExifInterface.TAG_PLANAR_CONFIGURATION};
    strArr[15] = new String[]{"double", ExifInterface.TAG_PRIMARY_CHROMATICITIES};
    strArr[16] = new String[]{"double", ExifInterface.TAG_REFERENCE_BLACK_WHITE};
    strArr[17] = new String[]{"int", ExifInterface.TAG_RESOLUTION_UNIT};
    strArr[18] = new String[]{"int", ExifInterface.TAG_ROWS_PER_STRIP};
    strArr[19] = new String[]{"int", ExifInterface.TAG_SAMPLES_PER_PIXEL};
    strArr[20] = new String[]{"string", ExifInterface.TAG_SOFTWARE};
    strArr[21] = new String[]{"int", ExifInterface.TAG_STRIP_BYTE_COUNTS};
    strArr[22] = new String[]{"int", ExifInterface.TAG_STRIP_OFFSETS};
    strArr[23] = new String[]{"int", ExifInterface.TAG_TRANSFER_FUNCTION};
    strArr[24] = new String[]{"double", ExifInterface.TAG_WHITE_POINT};
    strArr[25] = new String[]{"double", ExifInterface.TAG_X_RESOLUTION};
    strArr[26] = new String[]{"double", ExifInterface.TAG_Y_CB_CR_COEFFICIENTS};
    strArr[27] = new String[]{"int", ExifInterface.TAG_Y_CB_CR_POSITIONING};
    strArr[28] = new String[]{"int", ExifInterface.TAG_Y_CB_CR_SUB_SAMPLING};
    strArr[29] = new String[]{"double", ExifInterface.TAG_Y_RESOLUTION};
    strArr[30] = new String[]{"double", ExifInterface.TAG_APERTURE_VALUE};
    strArr[31] = new String[]{"double", ExifInterface.TAG_BRIGHTNESS_VALUE};
    strArr[32] = new String[]{"string", ExifInterface.TAG_CFA_PATTERN};
    strArr[33] = new String[]{"int", ExifInterface.TAG_COLOR_SPACE};
    strArr[34] = new String[]{"string", ExifInterface.TAG_COMPONENTS_CONFIGURATION};
    strArr[35] = new String[]{"double", ExifInterface.TAG_COMPRESSED_BITS_PER_PIXEL};
    strArr[36] = new String[]{"int", ExifInterface.TAG_CONTRAST};
    strArr[37] = new String[]{"int", ExifInterface.TAG_CUSTOM_RENDERED};
    strArr[38] = new String[]{"string", ExifInterface.TAG_DATETIME_DIGITIZED};
    strArr[39] = new String[]{"string", ExifInterface.TAG_DATETIME_ORIGINAL};
    strArr[40] = new String[]{"string", ExifInterface.TAG_DEVICE_SETTING_DESCRIPTION};
    strArr[41] = new String[]{"double", ExifInterface.TAG_DIGITAL_ZOOM_RATIO};
    strArr[42] = new String[]{"string", ExifInterface.TAG_EXIF_VERSION};
    strArr[43] = new String[]{"double", ExifInterface.TAG_EXPOSURE_BIAS_VALUE};
    strArr[44] = new String[]{"double", ExifInterface.TAG_EXPOSURE_INDEX};
    strArr[45] = new String[]{"int", ExifInterface.TAG_EXPOSURE_MODE};
    strArr[46] = new String[]{"int", ExifInterface.TAG_EXPOSURE_PROGRAM};
    strArr[47] = new String[]{"double", ExifInterface.TAG_EXPOSURE_TIME};
    strArr[48] = new String[]{"double", ExifInterface.TAG_F_NUMBER};
    strArr[49] = new String[]{"string", ExifInterface.TAG_FILE_SOURCE};
    strArr[50] = new String[]{"int", ExifInterface.TAG_FLASH};
    strArr[51] = new String[]{"double", ExifInterface.TAG_FLASH_ENERGY};
    strArr[52] = new String[]{"string", ExifInterface.TAG_FLASHPIX_VERSION};
    strArr[53] = new String[]{"double", ExifInterface.TAG_FOCAL_LENGTH};
    strArr[54] = new String[]{"int", ExifInterface.TAG_FOCAL_LENGTH_IN_35MM_FILM};
    strArr[55] = new String[]{"int", ExifInterface.TAG_FOCAL_PLANE_RESOLUTION_UNIT};
    strArr[56] = new String[]{"double", ExifInterface.TAG_FOCAL_PLANE_X_RESOLUTION};
    strArr[57] = new String[]{"double", ExifInterface.TAG_FOCAL_PLANE_Y_RESOLUTION};
    strArr[58] = new String[]{"int", ExifInterface.TAG_GAIN_CONTROL};
    strArr[59] = new String[]{"int", ExifInterface.TAG_ISO_SPEED_RATINGS};
    strArr[60] = new String[]{"string", ExifInterface.TAG_IMAGE_UNIQUE_ID};
    strArr[61] = new String[]{"int", ExifInterface.TAG_LIGHT_SOURCE};
    strArr[62] = new String[]{"string", ExifInterface.TAG_MAKER_NOTE};
    strArr[63] = new String[]{"double", ExifInterface.TAG_MAX_APERTURE_VALUE};
    strArr[64] = new String[]{"int", ExifInterface.TAG_METERING_MODE};
    strArr[65] = new String[]{"int", ExifInterface.TAG_NEW_SUBFILE_TYPE};
    strArr[66] = new String[]{"string", ExifInterface.TAG_OECF};
    strArr[67] = new String[]{"int", ExifInterface.TAG_PIXEL_X_DIMENSION};
    strArr[68] = new String[]{"int", ExifInterface.TAG_PIXEL_Y_DIMENSION};
    strArr[69] = new String[]{"string", ExifInterface.TAG_RELATED_SOUND_FILE};
    strArr[70] = new String[]{"int", ExifInterface.TAG_SATURATION};
    strArr[71] = new String[]{"int", ExifInterface.TAG_SCENE_CAPTURE_TYPE};
    strArr[72] = new String[]{"string", ExifInterface.TAG_SCENE_TYPE};
    strArr[73] = new String[]{"int", ExifInterface.TAG_SENSING_METHOD};
    strArr[74] = new String[]{"int", ExifInterface.TAG_SHARPNESS};
    strArr[75] = new String[]{"double", ExifInterface.TAG_SHUTTER_SPEED_VALUE};
    strArr[76] = new String[]{"string", ExifInterface.TAG_SPATIAL_FREQUENCY_RESPONSE};
    strArr[77] = new String[]{"string", ExifInterface.TAG_SPECTRAL_SENSITIVITY};
    strArr[78] = new String[]{"int", ExifInterface.TAG_SUBFILE_TYPE};
    strArr[79] = new String[]{"string", ExifInterface.TAG_SUBSEC_TIME};
    strArr[80] = new String[]{"string", ExifInterface.TAG_SUBSEC_TIME_DIGITIZED};
    strArr[81] = new String[]{"string", ExifInterface.TAG_SUBSEC_TIME_ORIGINAL};
    strArr[82] = new String[]{"int", ExifInterface.TAG_SUBJECT_AREA};
    strArr[83] = new String[]{"double", ExifInterface.TAG_SUBJECT_DISTANCE};
    strArr[84] = new String[]{"int", ExifInterface.TAG_SUBJECT_DISTANCE_RANGE};
    strArr[85] = new String[]{"int", ExifInterface.TAG_SUBJECT_LOCATION};
    strArr[86] = new String[]{"string", ExifInterface.TAG_USER_COMMENT};
    strArr[87] = new String[]{"int", ExifInterface.TAG_WHITE_BALANCE};
    strArr[88] = new String[]{"int", ExifInterface.TAG_GPS_ALTITUDE_REF};
    strArr[89] = new String[]{"string", ExifInterface.TAG_GPS_AREA_INFORMATION};
    strArr[90] = new String[]{"double", ExifInterface.TAG_GPS_DOP};
    strArr[91] = new String[]{"string", ExifInterface.TAG_GPS_DATESTAMP};
    strArr[92] = new String[]{"double", ExifInterface.TAG_GPS_DEST_BEARING};
    strArr[93] = new String[]{"string", ExifInterface.TAG_GPS_DEST_BEARING_REF};
    strArr[94] = new String[]{"double", ExifInterface.TAG_GPS_DEST_DISTANCE};
    strArr[95] = new String[]{"string", ExifInterface.TAG_GPS_DEST_DISTANCE_REF};
    strArr[96] = new String[]{"double", ExifInterface.TAG_GPS_DEST_LATITUDE};
    strArr[97] = new String[]{"string", ExifInterface.TAG_GPS_DEST_LATITUDE_REF};
    strArr[98] = new String[]{"double", ExifInterface.TAG_GPS_DEST_LONGITUDE};
    strArr[99] = new String[]{"string", ExifInterface.TAG_GPS_DEST_LONGITUDE_REF};
    strArr[100] = new String[]{"int", ExifInterface.TAG_GPS_DIFFERENTIAL};
    strArr[101] = new String[]{"double", ExifInterface.TAG_GPS_IMG_DIRECTION};
    strArr[102] = new String[]{"string", ExifInterface.TAG_GPS_IMG_DIRECTION_REF};
    strArr[103] = new String[]{"string", ExifInterface.TAG_GPS_LATITUDE_REF};
    strArr[104] = new String[]{"string", ExifInterface.TAG_GPS_LONGITUDE_REF};
    strArr[105] = new String[]{"string", ExifInterface.TAG_GPS_MAP_DATUM};
    strArr[106] = new String[]{"string", ExifInterface.TAG_GPS_MEASURE_MODE};
    strArr[107] = new String[]{"string", ExifInterface.TAG_GPS_PROCESSING_METHOD};
    strArr[108] = new String[]{"string", ExifInterface.TAG_GPS_SATELLITES};
    strArr[109] = new String[]{"double", ExifInterface.TAG_GPS_SPEED};
    strArr[110] = new String[]{"string", ExifInterface.TAG_GPS_SPEED_REF};
    strArr[111] = new String[]{"string", ExifInterface.TAG_GPS_STATUS};
    strArr[112] = new String[]{"string", ExifInterface.TAG_GPS_TIMESTAMP};
    strArr[113] = new String[]{"double", ExifInterface.TAG_GPS_TRACK};
    strArr[114] = new String[]{"string", ExifInterface.TAG_GPS_TRACK_REF};
    strArr[115] = new String[]{"string", ExifInterface.TAG_GPS_VERSION_ID};
    strArr[116] = new String[]{"string", ExifInterface.TAG_INTEROPERABILITY_INDEX};
    strArr[117] = new String[]{"int", ExifInterface.TAG_THUMBNAIL_IMAGE_LENGTH};
    strArr[118] = new String[]{"int", ExifInterface.TAG_THUMBNAIL_IMAGE_WIDTH};
    strArr[119] = new String[]{"int", ExifInterface.TAG_DNG_VERSION};
    strArr[120] = new String[]{"int", ExifInterface.TAG_DEFAULT_CROP_SIZE};
    strArr[Imgproc.COLOR_YUV2RGBA_YVYU] = new String[]{"int", ExifInterface.TAG_ORF_PREVIEW_IMAGE_START};
    strArr[Imgproc.COLOR_YUV2BGRA_YVYU] = new String[]{"int", ExifInterface.TAG_ORF_PREVIEW_IMAGE_LENGTH};
    strArr[123] = new String[]{"int", ExifInterface.TAG_ORF_ASPECT_FRAME};
    strArr[124] = new String[]{"int", ExifInterface.TAG_RW2_SENSOR_BOTTOM_BORDER};
    strArr[125] = new String[]{"int", ExifInterface.TAG_RW2_SENSOR_LEFT_BORDER};
    strArr[126] = new String[]{"int", ExifInterface.TAG_RW2_SENSOR_RIGHT_BORDER};
    strArr[127] = new String[]{"int", ExifInterface.TAG_RW2_SENSOR_TOP_BORDER};
    strArr[128] = new String[]{"int", ExifInterface.TAG_RW2_ISO};
    exifTags = strArr;
  }

  public static void emitMountErrorEvent(ViewGroup view) {
    ((UIManagerModule) ((ReactContext) view.getContext()).getNativeModule(UIManagerModule.class)).getEventDispatcher().dispatchEvent(CameraMountErrorEvent.obtain(view.getId()));
  }

  public static void emitCameraReadyEvent(ViewGroup view) {
    ((UIManagerModule) ((ReactContext) view.getContext()).getNativeModule(UIManagerModule.class)).getEventDispatcher().dispatchEvent(CameraReadyEvent.obtain(view.getId()));
  }

  public static void emitFacesDetectedEvent(ViewGroup view, SparseArray<Map<String, Float>> faces, ImageDimensions dimensions) {
    float density = view.getResources().getDisplayMetrics().density;
    ((UIManagerModule) ((ReactContext) view.getContext()).getNativeModule(UIManagerModule.class)).getEventDispatcher().dispatchEvent(FacesDetectedEvent.obtain(view.getId(), faces, dimensions, ((double) view.getWidth()) / ((double) (((float) dimensions.getWidth()) * density)), ((double) view.getHeight()) / ((double) (((float) dimensions.getHeight()) * density))));
  }

  public static void emitFaceDetectionErrorEvent(ViewGroup view, RNFaceDetector faceDetector) {
    ((UIManagerModule) ((ReactContext) view.getContext()).getNativeModule(UIManagerModule.class)).getEventDispatcher().dispatchEvent(FaceDetectionErrorEvent.obtain(view.getId(), faceDetector));
  }

  public static void emitBarCodeReadEvent(ViewGroup view, Result barCode) {
    ((UIManagerModule) ((ReactContext) view.getContext()).getNativeModule(UIManagerModule.class)).getEventDispatcher().dispatchEvent(BarCodeReadEvent.obtain(view.getId(), barCode));
  }

  public static int getCorrectCameraRotation(int rotation, int facing) {
    if (facing == 1) {
      return ((rotation - 90) + 360) % 360;
    }
    return (((-rotation) + 90) + 360) % 360;
  }

  public static CamcorderProfile getCamcorderProfile(int quality) {
    CamcorderProfile profile = CamcorderProfile.get(1);
    switch (quality) {
      case 0:
        if (VERSION.SDK_INT >= 21) {
          return CamcorderProfile.get(8);
        }
        return profile;
      case 1:
        return CamcorderProfile.get(6);
      case 2:
        return CamcorderProfile.get(5);
      case 3:
        return CamcorderProfile.get(4);
      case 4:
        profile = CamcorderProfile.get(4);
        profile.videoFrameWidth = OlympusMakernoteDirectory.TAG_PREVIEW_IMAGE;
        return profile;
      default:
        return profile;
    }
  }

  public static WritableMap getExifData(ExifInterface exifInterface) {
    WritableMap exifMap = Arguments.createMap();
    for (String[] tagInfo : exifTags) {
      String name = tagInfo[1];
      if (exifInterface.getAttribute(name) != null) {
        String type = tagInfo[0];
        Object obj = -1;
        switch (type.hashCode()) {
          case -1325958191:
            if (type.equals("double")) {
              obj = 2;
              break;
            }
            break;
          case -891985903:
            if (type.equals("string")) {
              obj = null;
              break;
            }
            break;
          case 104431:
            if (type.equals("int")) {
              obj = 1;
              break;
            }
            break;
        }
        switch (obj) {
          case null:
            exifMap.putString(name, exifInterface.getAttribute(name));
            break;
          case 1:
            exifMap.putInt(name, exifInterface.getAttributeInt(name, 0));
            break;
          case 2:
            exifMap.putDouble(name, exifInterface.getAttributeDouble(name, 0.0d));
            break;
          default:
            break;
        }
      }
    }
    double[] latLong = exifInterface.getLatLong();
    if (latLong != null) {
      exifMap.putDouble(ExifInterface.TAG_GPS_LATITUDE, latLong[0]);
      exifMap.putDouble(ExifInterface.TAG_GPS_LONGITUDE, latLong[1]);
      exifMap.putDouble(ExifInterface.TAG_GPS_ALTITUDE, exifInterface.getAltitude(0.0d));
    }
    return exifMap;
  }

  public static Bitmap generateSimulatorPhoto(int width, int height) {
    Bitmap fakePhoto = Bitmap.createBitmap(width, height, Config.ARGB_8888);
    Canvas canvas = new Canvas(fakePhoto);
    Paint background = new Paint();
    background.setColor(ViewCompat.MEASURED_STATE_MASK);
    canvas.drawRect(0.0f, 0.0f, (float) width, (float) height, background);
    Paint textPaint = new Paint();
    textPaint.setColor(InputDeviceCompat.SOURCE_ANY);
    textPaint.setTextSize(35.0f);
    canvas.drawText(new SimpleDateFormat("dd.MM.YY HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().getTime()), ((float) width) * 0.1f, ((float) height) * 0.9f, textPaint);
    return fakePhoto;
  }
}