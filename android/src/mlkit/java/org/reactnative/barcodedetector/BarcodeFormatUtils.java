package org.reactnative.barcodedetector;

import android.util.SparseArray;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BarcodeFormatUtils {

  public static final SparseArray<String> FORMATS;
  public static final Map<String, Integer> REVERSE_FORMATS;
  public static final SparseArray<String> TYPES;
  public static final Map<String, Integer> REVERSE_TYPES;

  private static final int UNKNOWN_FORMAT_INT = FirebaseVisionBarcode.FORMAT_UNKNOWN;

  private static final String UNKNOWN_TYPE_STRING = "UNKNOWN_TYPE";
  private static final String UNKNOWN_FORMAT_STRING = "UNKNOWN_FORMAT";

  static {
    // Initialize integer to string map
    SparseArray<String> map = new SparseArray<>();
    map.put(FirebaseVisionBarcode.FORMAT_CODE_128, "CODE_128");
    map.put(FirebaseVisionBarcode.FORMAT_CODE_39, "CODE_39");
    map.put(FirebaseVisionBarcode.FORMAT_CODE_93, "CODE_93");
    map.put(FirebaseVisionBarcode.FORMAT_CODABAR, "CODABAR");
    map.put(FirebaseVisionBarcode.FORMAT_DATA_MATRIX, "DATA_MATRIX");
    map.put(FirebaseVisionBarcode.FORMAT_EAN_13, "EAN_13");
    map.put(FirebaseVisionBarcode.FORMAT_EAN_8, "EAN_8");
    map.put(FirebaseVisionBarcode.FORMAT_ITF, "ITF");
    map.put(FirebaseVisionBarcode.FORMAT_QR_CODE, "QR_CODE");
    map.put(FirebaseVisionBarcode.FORMAT_UPC_A, "UPC_A");
    map.put(FirebaseVisionBarcode.FORMAT_UPC_E, "UPC_E");
    map.put(FirebaseVisionBarcode.FORMAT_PDF417, "PDF417");
    map.put(FirebaseVisionBarcode.FORMAT_AZTEC, "AZTEC");
    map.put(FirebaseVisionBarcode.FORMAT_ALL_FORMATS, "ALL");
    map.put(FirebaseVisionBarcode.FORMAT_UPC_A, "UPC_A");
    map.put(-1, "None");
    FORMATS = map;


    // Initialize string to integer map
    Map<String, Integer> rmap = new HashMap<>();
    for (int i = 0; i < map.size(); i++) {
      rmap.put(map.valueAt(i), map.keyAt(i));
    }

    REVERSE_FORMATS = Collections.unmodifiableMap(rmap);
  }

  static {
    // Initialize integer to string map
    SparseArray<String> map = new SparseArray<>();
    map.put(FirebaseVisionBarcode.TYPE_CALENDAR_EVENT, "CALENDAR_EVENT");
    map.put(FirebaseVisionBarcode.TYPE_CONTACT_INFO, "CONTACT_INFO");
    map.put(FirebaseVisionBarcode.TYPE_DRIVER_LICENSE, "DRIVER_LICENSE");
    map.put(FirebaseVisionBarcode.TYPE_EMAIL, "EMAIL");
    map.put(FirebaseVisionBarcode.TYPE_GEO, "GEO");
    map.put(FirebaseVisionBarcode.TYPE_ISBN, "ISBN");
    map.put(FirebaseVisionBarcode.TYPE_PHONE, "PHONE");
    map.put(FirebaseVisionBarcode.TYPE_PRODUCT, "PRODUCT");
    map.put(FirebaseVisionBarcode.TYPE_SMS, "SMS");
    map.put(FirebaseVisionBarcode.TYPE_TEXT, "TEXT");
    map.put(FirebaseVisionBarcode.TYPE_URL, "URL");
    map.put(FirebaseVisionBarcode.TYPE_WIFI, "WIFI");
    map.put(-1, "None");
    TYPES = map;


    // Initialize string to integer map
    Map<String, Integer> rmap = new HashMap<>();
    for (int i = 0; i < map.size(); i++) {
      rmap.put(map.valueAt(i), map.keyAt(i));
    }

    REVERSE_TYPES = Collections.unmodifiableMap(rmap);
  }

  public static String get(int format) {
    return TYPES.get(format, UNKNOWN_TYPE_STRING);
  }
  public static String getFormat(int format) {
    return FORMATS.get(format, UNKNOWN_FORMAT_STRING);
  }

  public static int get(String format) {
    if (REVERSE_FORMATS.containsKey(format)) {
      return REVERSE_FORMATS.get(format);
    }

    return UNKNOWN_FORMAT_INT;
  }
}
