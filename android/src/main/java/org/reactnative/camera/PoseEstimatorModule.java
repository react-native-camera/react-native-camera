package org.reactnative.camera;


import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.common.FirebaseMLException;
import com.google.firebase.ml.common.modeldownload.FirebaseLocalModel;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;

import java.io.IOException;
import java.io.InputStream;

public class PoseEstimatorModule {
    private FirebaseModelInterpreter interpreter;
    private FirebaseModelInputOutputOptions ioOptions;
    public float[][][] Output;

    private static final int IN_DIM = 192;
    private static final int OUT_DIM = 96;
    private static final int COLORS_PER_PIXEL = 3;
    private static final int BODYPART_COUNT = 14;

    public PoseEstimatorModule(){
        FirebaseLocalModel localSource =
                new FirebaseLocalModel.Builder("local-model")  // Assign a name to this model
                        .setAssetFilePath("cpm-model-192.tflite")
                        .build();
        FirebaseModelManager.getInstance().registerLocalModel(localSource);

        FirebaseModelOptions options = new FirebaseModelOptions.Builder()
                //.setRemoteModelName("my_cloud_model")
                .setLocalModelName("local-model")
                .build();

        try {
            interpreter =
                    FirebaseModelInterpreter.getInstance(options);
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }

        try {
            ioOptions =
                    new FirebaseModelInputOutputOptions.Builder()
                            .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, IN_DIM, IN_DIM, COLORS_PER_PIXEL})
                            .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, OUT_DIM, OUT_DIM, BODYPART_COUNT})
                            .build();
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }
    }

    public static Bitmap getBitmapFromAsset(Context context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream istr;
        Bitmap bitmap = null;
        try {
            istr = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            Log.e("BITMAP LOADING TEST", "getBitmapFromAsset: " + filePath);
        }

        return bitmap;
    }

    public void run(Bitmap bitmap) {
        float[][][][] data = transformImage(bitmap);

        FirebaseModelInputs inputs = null;
        try {
            inputs = new FirebaseModelInputs.Builder()
                    .add(data)  // add() as many input arrays as your model requires
                    .build();

            interpreter.run(inputs, ioOptions)
                    .addOnSuccessListener(
                            new OnSuccessListener<FirebaseModelOutputs>() {
                                @Override
                                public void onSuccess(FirebaseModelOutputs result) {
                                    float[][][][] outputs = result.getOutput(0);
                                    Output = outputs[0];
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    e.printStackTrace();
                                }
                            });
        } catch (FirebaseMLException e) {
            e.printStackTrace();
        }
    }

    private float[][][][] transformImage(Bitmap bitmap) {
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, IN_DIM, IN_DIM, true);

        int batchNum = 0;
        float[][][][] input = new float[1][IN_DIM][IN_DIM][COLORS_PER_PIXEL];
        for (int x = 0; x < IN_DIM; x++) {
            for (int y = 0; y < IN_DIM; y++) {
                int pixel = scaledBitmap.getPixel(x, y);
                int red = Color.red(pixel);
                int green = Color.green(pixel);
                int blue = Color.blue(pixel);
                input[batchNum][x][y][0] = red;
                input[batchNum][x][y][1] = green;
                input[batchNum][x][y][2] = blue;
            }
        }
        return input;
    }
}
