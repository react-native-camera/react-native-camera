package org.reactnative.camera.tasks;

//import android.graphics.Point;
import android.graphics.Rect;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.barcode.Barcode;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.common.InputImage;

import org.reactnative.barcodedetector.BarcodeFormatUtils;
import org.reactnative.barcodedetector.RNBarcodeDetector;
import org.reactnative.camera.utils.ImageDimensions;

import java.util.List;

public class BarcodeDetectorAsyncTask extends android.os.AsyncTask<Void, Void, Void> {

  private byte[] mImageData;
  private int mWidth;
  private int mHeight;
  private int mRotation;
  private RNBarcodeDetector mBarcodeDetector;
  private BarcodeDetectorAsyncTaskDelegate mDelegate;
  private double mScaleX;
  private double mScaleY;
  private ImageDimensions mImageDimensions;
  private int mPaddingLeft;
  private int mPaddingTop;
  private String TAG = "RNCamera";

  public BarcodeDetectorAsyncTask(
      BarcodeDetectorAsyncTaskDelegate delegate,
      RNBarcodeDetector barcodeDetector,
      byte[] imageData,
      int width,
      int height,
      int rotation,
      float density,
      int facing,
      int viewWidth,
      int viewHeight,
      int viewPaddingLeft,
      int viewPaddingTop
  ) {
    mImageData = imageData;
    mWidth = width;
    mHeight = height;
    mRotation = rotation;
    mDelegate = delegate;
    mBarcodeDetector = barcodeDetector;
    mImageDimensions = new ImageDimensions(width, height, rotation, facing);
    mScaleX = (double) (viewWidth) / (mImageDimensions.getWidth() * density);
    mScaleY = 1 / density;
    mPaddingLeft = viewPaddingLeft;
    mPaddingTop = viewPaddingTop;
  }

  @Override
  protected Void doInBackground(Void... ignored) {
    if (isCancelled() || mDelegate == null || mBarcodeDetector == null) {
      return null;
    }

    InputImage image = InputImage.fromByteArray(mImageData, mWidth, mHeight, getFirebaseRotation(), InputImage.IMAGE_FORMAT_YV12);

    BarcodeScanner barcode = mBarcodeDetector.getDetector();
    barcode.process(image)
            .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
              @Override
              public void onSuccess(List<Barcode> barcodes) {
                WritableArray serializedBarcodes = serializeEventData(barcodes);
                mDelegate.onBarcodesDetected(serializedBarcodes, mWidth, mHeight, mImageData);
                mDelegate.onBarcodeDetectingTaskCompleted();
              }
            })
            .addOnFailureListener(new OnFailureListener() {
              @Override
              public void onFailure(Exception e) {
                Log.e(TAG, "Text recognition task failed" + e);
                mDelegate.onBarcodeDetectingTaskCompleted();
              }
            });
    return null;
  }

  private int getFirebaseRotation(){
    int result;
    switch (mRotation) {
      case 0:
        result = 0;
        break;
      case 90:
        result = 90;
        break;
      case 180:
        result = 180;
        break;
      case -90:
      case 270:
        result = 270;
        break;
      default:
        result = 0;
        Log.e(TAG, "Bad rotation value: " + mRotation);
    }
    return result;
  }


  private WritableArray serializeEventData(List<Barcode> barcodes) {
    WritableArray barcodesList = Arguments.createArray();

    for (Barcode barcode: barcodes) {
      // TODO implement position and data from all barcode types
      Rect bounds = barcode.getBoundingBox();
//      Point[] corners = barcode.getCornerPoints();

      String rawValue = barcode.getRawValue();

      int valueType = barcode.getValueType();
      int valueFormat = barcode.getFormat();

      WritableMap serializedBarcode = Arguments.createMap();

      switch (valueType) {
        case Barcode.TYPE_WIFI:
          String ssid = barcode.getWifi().getSsid();
          String password = barcode.getWifi().getPassword();
          int type = barcode.getWifi().getEncryptionType();
          String typeString = "UNKNOWN";
          switch (type) {
            case Barcode.WiFi.TYPE_OPEN:
              typeString = "Open";
              break;
            case Barcode.WiFi.TYPE_WEP:
              typeString = "WEP";
              break;
            case Barcode.WiFi.TYPE_WPA:
              typeString = "WPA";
              break;
          }
          serializedBarcode.putString("encryptionType", typeString);
          serializedBarcode.putString("password", password);
          serializedBarcode.putString("ssid", ssid);
          break;
        case Barcode.TYPE_URL:
          String title = barcode.getUrl().getTitle();
          String url = barcode.getUrl().getUrl();
          serializedBarcode.putString("url", url);
          serializedBarcode.putString("title", title);
          break;
        case Barcode.TYPE_SMS:
          String message = barcode.getSms().getMessage();
          String phoneNumber = barcode.getSms().getPhoneNumber();
          serializedBarcode.putString("message", message);
          serializedBarcode.putString("title", phoneNumber);
          break;
        case Barcode.TYPE_PHONE:
          String number = barcode.getPhone().getNumber();
          int typePhone = barcode.getPhone().getType();
          serializedBarcode.putString("number", number);
          String typeStringPhone = getPhoneType(typePhone);
          serializedBarcode.putString("phoneType", typeStringPhone);
          break;
        case Barcode.TYPE_CALENDAR_EVENT:
          serializedBarcode.putString("description", barcode.getCalendarEvent().getDescription());
          serializedBarcode.putString("location", barcode.getCalendarEvent().getLocation());
          serializedBarcode.putString("organizer", barcode.getCalendarEvent().getOrganizer());
          serializedBarcode.putString("status", barcode.getCalendarEvent().getStatus());
          serializedBarcode.putString("summary", barcode.getCalendarEvent().getSummary());
          Barcode.CalendarDateTime start = barcode.getCalendarEvent().getStart();
          Barcode.CalendarDateTime end = barcode.getCalendarEvent().getEnd();
          if (start != null) {
            serializedBarcode.putString("start", start.getRawValue());
          }
          if (end != null) {
            serializedBarcode.putString("end", start.getRawValue());
          }
          break;
        case Barcode.TYPE_DRIVER_LICENSE:
          serializedBarcode.putString("addressCity", barcode.getDriverLicense().getAddressCity());
          serializedBarcode.putString("addressState", barcode.getDriverLicense().getAddressState());
          serializedBarcode.putString("addressStreet", barcode.getDriverLicense().getAddressStreet());
          serializedBarcode.putString("addressZip", barcode.getDriverLicense().getAddressZip());
          serializedBarcode.putString("birthDate", barcode.getDriverLicense().getBirthDate());
          serializedBarcode.putString("documentType", barcode.getDriverLicense().getDocumentType());
          serializedBarcode.putString("expiryDate", barcode.getDriverLicense().getExpiryDate());
          serializedBarcode.putString("firstName", barcode.getDriverLicense().getFirstName());
          serializedBarcode.putString("middleName", barcode.getDriverLicense().getMiddleName());
          serializedBarcode.putString("lastName", barcode.getDriverLicense().getLastName());
          serializedBarcode.putString("gender", barcode.getDriverLicense().getGender());
          serializedBarcode.putString("issueDate", barcode.getDriverLicense().getIssueDate());
          serializedBarcode.putString("issuingCountry", barcode.getDriverLicense().getIssuingCountry());
          serializedBarcode.putString("licenseNumber", barcode.getDriverLicense().getLicenseNumber());
          break;
        case Barcode.TYPE_GEO:
          serializedBarcode.putDouble("latitude", barcode.getGeoPoint().getLat());
          serializedBarcode.putDouble("longitude", barcode.getGeoPoint().getLng());
          break;
        case Barcode.TYPE_CONTACT_INFO:
          serializedBarcode.putString("organization", barcode.getContactInfo().getOrganization());
          serializedBarcode.putString("title", barcode.getContactInfo().getTitle());
          Barcode.PersonName name = barcode.getContactInfo().getName();
          if (name != null) {
            serializedBarcode.putString("firstName", name.getFirst());
            serializedBarcode.putString("lastName", name.getLast());
            serializedBarcode.putString("middleName", name.getMiddle());
            serializedBarcode.putString("formattedName", name.getFormattedName());
            serializedBarcode.putString("prefix", name.getPrefix());
            serializedBarcode.putString("pronunciation", name.getPronunciation());
            serializedBarcode.putString("suffix", name.getSuffix());
          }
          List<Barcode.Phone> phones = barcode.getContactInfo().getPhones();
          WritableArray phonesList = Arguments.createArray();
          for (Barcode.Phone phone : phones) {
            WritableMap phoneObject = Arguments.createMap();
            phoneObject.putString("number", phone.getNumber());
            phoneObject.putString("phoneType", getPhoneType(phone.getType()));
            phonesList.pushMap(phoneObject);
          }
          serializedBarcode.putArray("phones", phonesList);
          List<Barcode.Address> addresses = barcode.getContactInfo().getAddresses();
          WritableArray addressesList = Arguments.createArray();
          for (Barcode.Address address : addresses) {
            WritableMap addressesData = Arguments.createMap();
            WritableArray addressesLinesList = Arguments.createArray();
            String[] addressesLines = address.getAddressLines();
            for (String line : addressesLines) {
              addressesLinesList.pushString(line);
            }
            addressesData.putArray("addressLines", addressesLinesList);

            int addressType = address.getType();
            String addressTypeString = "UNKNOWN";
            switch(addressType) {
              case Barcode.Address.TYPE_WORK:
                addressTypeString = "Work";
                break;
              case Barcode.Address.TYPE_HOME:
                addressTypeString = "Home";
                break;
            }
            addressesData.putString("addressType", addressTypeString);
            addressesList.pushMap(addressesData);
          }
          serializedBarcode.putArray("addresses", addressesList);
          List<Barcode.Email> emails = barcode.getContactInfo().getEmails();
          WritableArray emailsList = Arguments.createArray();
          for (Barcode.Email email : emails) {
            WritableMap emailData = processEmail(email);
            emailsList.pushMap(emailData);
          }
          serializedBarcode.putArray("emails", emailsList);
          List<String> urls = barcode.getContactInfo().getUrls();
          WritableArray urlsList = Arguments.createArray();
          for (String urlContact : urls) {
            urlsList.pushString(urlContact);
          }
          serializedBarcode.putArray("urls", urlsList);
          break;
        case Barcode.TYPE_EMAIL:
          WritableMap emailData = processEmail(barcode.getEmail());
          serializedBarcode.putMap("email", emailData);
          break;
      }

      serializedBarcode.putString("data", barcode.getDisplayValue());
      serializedBarcode.putString("dataRaw", rawValue);
      serializedBarcode.putString("type", BarcodeFormatUtils.get(valueType));
      serializedBarcode.putString("format", BarcodeFormatUtils.getFormat(valueFormat));
      serializedBarcode.putMap("bounds", processBounds(bounds));
      barcodesList.pushMap(serializedBarcode);
    }

    return barcodesList;
  }

  private WritableMap processEmail(Barcode.Email email) {
    WritableMap emailData = Arguments.createMap();
    emailData.putString("address", email.getAddress());
    emailData.putString("body", email.getBody());
    emailData.putString("subject", email.getSubject());
    int emailType = email.getType();
    String emailTypeString = "UNKNOWN";
    switch (emailType) {
      case Barcode.Email.TYPE_WORK:
        emailTypeString = "Work";
        break;
      case Barcode.Email.TYPE_HOME:
        emailTypeString = "Home";
        break;
    }
    emailData.putString("emailType", emailTypeString);
    return emailData;
  }

  private String getPhoneType(int typePhone) {
    String typeStringPhone = "UNKNOWN";
    switch(typePhone) {
      case Barcode.Phone.TYPE_WORK:
        typeStringPhone = "Work";
        break;
      case Barcode.Phone.TYPE_HOME:
        typeStringPhone = "Home";
        break;
      case Barcode.Phone.TYPE_FAX:
        typeStringPhone = "Fax";
        break;
      case Barcode.Phone.TYPE_MOBILE:
        typeStringPhone = "Mobile";
        break;
    }
    return typeStringPhone;
  }

  private WritableMap processBounds(Rect frame) {
    WritableMap origin = Arguments.createMap();
    int x = frame.left;
    int y = frame.top;

    if (frame.left < mWidth / 2) {
      x = x + mPaddingLeft / 2;
    } else if (frame.left > mWidth /2) {
      x = x - mPaddingLeft / 2;
    }

    y = y + mPaddingTop;

    origin.putDouble("x", x * mScaleX);
    origin.putDouble("y", y * mScaleY);

    WritableMap size = Arguments.createMap();
    size.putDouble("width", frame.width() * mScaleX);
    size.putDouble("height", frame.height() * mScaleY);

    WritableMap bounds = Arguments.createMap();
    bounds.putMap("origin", origin);
    bounds.putMap("size", size);
    return bounds;
  }

}
