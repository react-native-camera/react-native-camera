package org.reactnative.barcodedetector;

import android.util.SparseArray;
import com.google.mlkit.vision.barcode.Barcode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BarcodeFormatUtils {

  public static final SparseArray<String> FORMATS;
  public static final Map<String, Integer> REVERSE_FORMATS;

  private static final String UNKNOWN_FORMAT_STRING = "UNKNOWN_FORMAT";
  private static final int UNKNOWN_FORMAT_INT = -1;

  static {
    // Initialize integer to string map
    SparseArray<String> map = new SparseArray<>();
    map.put(Barcode.FORMAT_CODE_128, "CODE_128");
    map.put(Barcode.FORMAT_CODE_39, "CODE_39");
    map.put(Barcode.FORMAT_CODE_93, "CODE_93");
    map.put(Barcode.FORMAT_CODABAR, "CODABAR");
    map.put(Barcode.FORMAT_DATA_MATRIX, "DATA_MATRIX");
    map.put(Barcode.FORMAT_EAN_13, "EAN_13");
    map.put(Barcode.FORMAT_EAN_8, "EAN_8");
    map.put(Barcode.FORMAT_ITF, "ITF");
    map.put(Barcode.FORMAT_QR_CODE, "QR_CODE");
    map.put(Barcode.FORMAT_UPC_A, "UPC_A");
    map.put(Barcode.FORMAT_UPC_E, "UPC_E");
    map.put(Barcode.FORMAT_PDF417, "PDF417");
    map.put(Barcode.FORMAT_AZTEC, "AZTEC");
    map.put(Barcode.FORMAT_ALL_FORMATS, "ALL");
    map.put(Barcode.TYPE_CALENDAR_EVENT, "CALENDAR_EVENT");
    map.put(Barcode.TYPE_CONTACT_INFO, "CONTACT_INFO");
    map.put(Barcode.TYPE_DRIVER_LICENSE, "DRIVER_LICENSE");
    map.put(Barcode.TYPE_EMAIL, "EMAIL");
    map.put(Barcode.TYPE_GEO, "GEO");
    map.put(Barcode.TYPE_ISBN, "ISBN");
    map.put(Barcode.TYPE_PHONE, "PHONE");
    map.put(Barcode.TYPE_PRODUCT, "PRODUCT");
    map.put(Barcode.TYPE_SMS, "SMS");
    map.put(Barcode.TYPE_TEXT, "TEXT");
    map.put(Barcode.TYPE_URL, "URL");
    map.put(Barcode.TYPE_WIFI, "WIFI");
    map.put(-1, "None");
    FORMATS = map;


    // Initialize string to integer map
    Map<String, Integer> rmap = new HashMap<>();
    for (int i = 0; i < map.size(); i++) {
      rmap.put(map.valueAt(i), map.keyAt(i));
    }

    REVERSE_FORMATS = Collections.unmodifiableMap(rmap);
  }

  public static String get(int format) {
    return FORMATS.get(format, UNKNOWN_FORMAT_STRING);
  }

  public static int get(String format) {
    if (REVERSE_FORMATS.containsKey(format)) {
      return REVERSE_FORMATS.get(format);
    }

    return UNKNOWN_FORMAT_INT;
  }
}
