package com.lwansbrough.RCTCamera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Base64;
import android.util.Log;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.facebook.react.bridge.ReadableMap;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MutableImage {
    private static final String TAG = "RNCamera";

    private final byte[] originalImageData;
    private Bitmap currentRepresentation;
    private Metadata originalImageMetaData;
    private boolean hasBeenReoriented = false;

    public MutableImage(byte[] originalImageData) {
        this.originalImageData = originalImageData;
        this.currentRepresentation = toBitmap(originalImageData);
    }

    public void mirrorImage() throws ImageMutationFailedException {
        Matrix m = new Matrix();

        m.preScale(-1, 1);

        Bitmap bitmap = Bitmap.createBitmap(
                currentRepresentation,
                0,
                0,
                currentRepresentation.getWidth(),
                currentRepresentation.getHeight(),
                m,
                false
        );

        if (bitmap == null)
            throw new ImageMutationFailedException("failed to mirror");

        this.currentRepresentation = bitmap;
    }

    public void fixOrientation() throws ImageMutationFailedException {
        try {
            Metadata metadata = originalImageMetaData();

            ExifIFD0Directory exifIFD0Directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
            if (exifIFD0Directory == null) {
                return;
            } else if (exifIFD0Directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                int exifOrientation = exifIFD0Directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
                if(exifOrientation != 1) {
                    rotate(exifOrientation);
                    exifIFD0Directory.setInt(ExifIFD0Directory.TAG_ORIENTATION, 1);
                }
            }
        } catch (ImageProcessingException | IOException | MetadataException e) {
            throw new ImageMutationFailedException("failed to fix orientation", e);
        }
    }

    //see http://www.impulseadventure.com/photo/exif-orientation.html
    private void rotate(int exifOrientation) throws ImageMutationFailedException {
        final Matrix bitmapMatrix = new Matrix();
        switch (exifOrientation) {
            case 1:
                return;//no rotation required
            case 2:
                bitmapMatrix.postScale(-1, 1);
                break;
            case 3:
                bitmapMatrix.postRotate(180);
                break;
            case 4:
                bitmapMatrix.postRotate(180);
                bitmapMatrix.postScale(-1, 1);
                break;
            case 5:
                bitmapMatrix.postRotate(90);
                bitmapMatrix.postScale(-1, 1);
                break;
            case 6:
                bitmapMatrix.postRotate(90);
                break;
            case 7:
                bitmapMatrix.postRotate(270);
                bitmapMatrix.postScale(-1, 1);
                break;
            case 8:
                bitmapMatrix.postRotate(270);
                break;
            default:
                break;
        }

        Bitmap transformedBitmap = Bitmap.createBitmap(
                currentRepresentation,
                0,
                0,
                currentRepresentation.getWidth(),
                currentRepresentation.getHeight(),
                bitmapMatrix,
                false
        );

        if (transformedBitmap == null)
            throw new ImageMutationFailedException("failed to rotate");

        this.currentRepresentation = transformedBitmap;
        this.hasBeenReoriented = true;
    }

    private static Bitmap toBitmap(byte[] data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
            Bitmap photo = BitmapFactory.decodeStream(inputStream);
            inputStream.close();
            return photo;
        } catch (IOException e) {
            throw new IllegalStateException("Will not happen", e);
        }
    }

    public String toBase64(int jpegQualityPercent) {
        return Base64.encodeToString(toJpeg(currentRepresentation, jpegQualityPercent), Base64.DEFAULT);
    }

    public void writeDataToFile(File file, ReadableMap options, int jpegQualityPercent) throws IOException {
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(toJpeg(currentRepresentation, jpegQualityPercent));
        fos.close();

        try {
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());

            // copy original exif data to the output exif...
            // unfortunately, this Android ExifInterface class doesn't understand all the tags so we lose some
            for (Directory directory : originalImageMetaData().getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    int tagType = tag.getTagType();
                    Object object = directory.getObject(tagType);
                    exif.setAttribute(tag.getTagName(), object.toString());
                }
            }

            writeLocationExifData(options, exif);

            if(hasBeenReoriented)
                rewriteOrientation(exif);

            exif.saveAttributes();
        } catch (ImageProcessingException  | IOException e) {
            Log.e(TAG, "failed to save exif data", e);
        }
    }

    private void rewriteOrientation(ExifInterface exif) {
        exif.setAttribute(ExifInterface.TAG_ORIENTATION, String.valueOf(ExifInterface.ORIENTATION_NORMAL));
    }

    private void writeLocationExifData(ReadableMap options, ExifInterface exif) {
        if(!options.hasKey("metadata"))
            return;

        ReadableMap metadata = options.getMap("metadata");
        if (!metadata.hasKey("location"))
            return;

        ReadableMap location = metadata.getMap("location");
        if(!location.hasKey("coords"))
            return;

        try {
            ReadableMap coords = location.getMap("coords");
            double latitude = coords.getDouble("latitude");
            double longitude = coords.getDouble("longitude");

            GPS.writeExifData(latitude, longitude, exif);
        } catch (IOException e) {
            Log.e(TAG, "Couldn't write location data", e);
        }
    }

    private Metadata originalImageMetaData() throws ImageProcessingException, IOException {
        if(this.originalImageMetaData == null) {//this is expensive, don't do it more than once
            originalImageMetaData = ImageMetadataReader.readMetadata(
                    new BufferedInputStream(new ByteArrayInputStream(originalImageData)),
                    originalImageData.length
            );
        }
        return originalImageMetaData;
    }

    private static byte[] toJpeg(Bitmap bitmap, int quality) throws OutOfMemoryError {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);

        try {
            return outputStream.toByteArray();
        } finally {
            try {
                outputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "problem compressing jpeg", e);
            }
        }
    }

    public static class ImageMutationFailedException extends Exception {
        public ImageMutationFailedException(String detailMessage, Throwable throwable) {
            super(detailMessage, throwable);
        }

        public ImageMutationFailedException(String detailMessage) {
            super(detailMessage);
        }
    }

    private static class GPS {
        public static void writeExifData(double latitude, double longitude, ExifInterface exif) throws IOException {
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, toDegreeMinuteSecods(latitude));
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, latitudeRef(latitude));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, toDegreeMinuteSecods(longitude));
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, longitudeRef(longitude));
        }

        private static String latitudeRef(double latitude) {
            return latitude < 0.0d ? "S" : "N";
        }

        private static String longitudeRef(double longitude) {
            return longitude < 0.0d ? "W" : "E";
        }

        private static String toDegreeMinuteSecods(double latitude) {
            latitude = Math.abs(latitude);
            int degree = (int) latitude;
            latitude *= 60;
            latitude -= (degree * 60.0d);
            int minute = (int) latitude;
            latitude *= 60;
            latitude -= (minute * 60.0d);
            int second = (int) (latitude * 1000.0d);

            StringBuffer sb = new StringBuffer();
            sb.append(degree);
            sb.append("/1,");
            sb.append(minute);
            sb.append("/1,");
            sb.append(second);
            sb.append("/1000,");
            return sb.toString();
        }
    }
}
