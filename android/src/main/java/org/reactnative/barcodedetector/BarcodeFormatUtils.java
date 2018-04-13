package org.reactnative.barcodedetector;

import android.util.SparseArray;
import com.google.android.gms.vision.barcode.Barcode;

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
    map.put(Barcode.CODE_128, "CODE_128");
    map.put(Barcode.CODE_39, "CODE_39");
    map.put(Barcode.CODE_93, "CODE_93");
    map.put(Barcode.CODABAR, "CODABAR");
    map.put(Barcode.DATA_MATRIX, "DATA_MATRIX");
    map.put(Barcode.EAN_13, "EAN_13");
    map.put(Barcode.EAN_8, "EAN_8");
    map.put(Barcode.ITF, "ITF");
    map.put(Barcode.QR_CODE, "QR_CODE");
    map.put(Barcode.UPC_A, "UPC_A");
    map.put(Barcode.UPC_E, "UPC_E");
    map.put(Barcode.PDF417, "PDF417");
    map.put(Barcode.AZTEC, "AZTEC");
    map.put(Barcode.ALL_FORMATS, "ALL");
    map.put(Barcode.CALENDAR_EVENT, "CALENDAR_EVENT");
    map.put(Barcode.CONTACT_INFO, "CONTACT_INFO");
    map.put(Barcode.DRIVER_LICENSE, "DRIVER_LICENSE");
    map.put(Barcode.EMAIL, "EMAIL");
    map.put(Barcode.GEO, "GEO");
    map.put(Barcode.ISBN, "ISBN");
    map.put(Barcode.PHONE, "PHONE");
    map.put(Barcode.PRODUCT, "PRODUCT");
    map.put(Barcode.SMS, "SMS");
    map.put(Barcode.TEXT, "TEXT");
    map.put(Barcode.UPC_A, "UPC_A");
    map.put(Barcode.URL, "URL");
    map.put(Barcode.WIFI, "WIFI");
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
