package org.reactnative.facedetector;

import android.graphics.PointF;
import android.graphics.Rect;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceLandmark;

public class FaceDetectorUtils {
  // All the landmarks reported by Google Mobile Vision in constants' order.
  // https://developers.google.com/android/reference/com/google/android/gms/vision/face/Landmark
  private static final String[] landmarkNames = {
    "bottomMouthPosition", "leftCheekPosition", "leftEarPosition", "leftEarTipPosition",
      "leftEyePosition", "leftMouthPosition", "noseBasePosition", "rightCheekPosition",
      "rightEarPosition", "rightEarTipPosition", "rightEyePosition", "rightMouthPosition"
  };

  public static WritableMap serializeFace(Face face) {
    return serializeFace(face, 1, 1, 0, 0, 0, 0);
  }

  public static WritableMap serializeFace(Face face, double scaleX, double scaleY, int width, int height, int paddingLeft, int paddingTop) {
    WritableMap encodedFace = Arguments.createMap();

    encodedFace.putInt("faceID", face.getTrackingId());
    encodedFace.putDouble("rollAngle", face.getHeadEulerAngleZ());
    encodedFace.putDouble("yawAngle", face.getHeadEulerAngleY());

    if (face.getSmilingProbability() >= 0) {
      encodedFace.putDouble("smilingProbability", face.getSmilingProbability());
    }
    if (face.getLeftEyeOpenProbability() >= 0) {
      encodedFace.putDouble("leftEyeOpenProbability", face.getLeftEyeOpenProbability());
    }
    if (face.getRightEyeOpenProbability() >= 0) {
      encodedFace.putDouble("rightEyeOpenProbability", face.getRightEyeOpenProbability());
    }

    for(FaceLandmark landmark : face.getAllLandmarks()) {
      encodedFace.putMap(landmarkNames[landmark.getLandmarkType()], mapFromPoint(landmark.getPosition(), scaleX, scaleY, width, height, paddingLeft, paddingTop));
    }

    WritableMap origin = Arguments.createMap();
    Rect boundingBox = face.getBoundingBox();
    int x = boundingBox.left;
    int y = boundingBox.top;
    if (x < width / 2) {
      x = x + paddingLeft / 2;
    } else if (x > width / 2) {
      x = x - paddingLeft / 2;
    }

    if (y < height / 2) {
      y = y + paddingTop / 2;
    } else if (y > height / 2) {
      y = y - paddingTop / 2;
    }
    origin.putDouble("x", x * scaleX);
    origin.putDouble("y", y * scaleY);

    WritableMap size = Arguments.createMap();
    size.putDouble("width", boundingBox.width() * scaleX);
    size.putDouble("height", boundingBox.height() * scaleY);

    WritableMap bounds = Arguments.createMap();
    bounds.putMap("origin", origin);
    bounds.putMap("size", size);

    encodedFace.putMap("bounds", bounds);

    return encodedFace;
  }

  public static WritableMap rotateFaceX(WritableMap face, int sourceWidth, double scaleX) {
    ReadableMap faceBounds = face.getMap("bounds");

    ReadableMap oldOrigin = faceBounds.getMap("origin");
    WritableMap mirroredOrigin = positionMirroredHorizontally(oldOrigin, sourceWidth, scaleX);

    double translateX = -faceBounds.getMap("size").getDouble("width");
    WritableMap translatedMirroredOrigin = positionTranslatedHorizontally(mirroredOrigin, translateX);

    WritableMap newBounds = Arguments.createMap();
    newBounds.merge(faceBounds);
    newBounds.putMap("origin", translatedMirroredOrigin);

    for (String landmarkName : landmarkNames) {
      ReadableMap landmark = face.hasKey(landmarkName) ? face.getMap(landmarkName) : null;
      if (landmark != null) {
        WritableMap mirroredPosition = positionMirroredHorizontally(landmark, sourceWidth, scaleX);
        face.putMap(landmarkName, mirroredPosition);
      }
    }

    face.putMap("bounds", newBounds);

    return face;
  }

  public static WritableMap changeAnglesDirection(WritableMap face) {
    face.putDouble("rollAngle", (-face.getDouble("rollAngle") + 360) % 360);
    face.putDouble("yawAngle", (-face.getDouble("yawAngle") + 360) % 360);
    return face;
  }

  public static WritableMap mapFromPoint(PointF point, double scaleX, double scaleY, int width, int height, int paddingLeft, int paddingTop) {
    WritableMap map = Arguments.createMap();
    Float x = point.x;
    Float y = point.y;
    if (point.x < width / 2) {
      x = (x + paddingLeft / 2);
    } else if (point.x > width / 2) {
      x = (x - paddingLeft / 2);
    }

    if (point.y < height / 2) {
      y = (y + paddingTop / 2);
    } else if (point.y > height / 2) {
      y = (y - paddingTop / 2);
    }
    map.putDouble("x", point.x * scaleX);
    map.putDouble("y", point.y * scaleY);
    return map;
  }

  public static WritableMap positionTranslatedHorizontally(ReadableMap position, double translateX) {
    WritableMap newPosition = Arguments.createMap();
    newPosition.merge(position);
    newPosition.putDouble("x", position.getDouble("x") + translateX);
    return newPosition;
  }

  public static WritableMap positionMirroredHorizontally(ReadableMap position, int containerWidth, double scaleX) {
    WritableMap newPosition = Arguments.createMap();
    newPosition.merge(position);
    newPosition.putDouble("x", valueMirroredHorizontally(position.getDouble("x"), containerWidth, scaleX));
    return newPosition;
  }

  public static double valueMirroredHorizontally(double elementX, int containerWidth, double scaleX) {
    double originalX = elementX / scaleX;
    double mirroredX = containerWidth - originalX;
    return mirroredX * scaleX;
  }
}
