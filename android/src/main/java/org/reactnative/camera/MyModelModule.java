package org.reactnative.camera;


// import packages
import androidx.annotation.NonNull;
//async task
import android.content.res.AssetFileDescriptor;
import android.os.AsyncTask;
// use all the react bridge code from facebook to expose this class as API to react javascript
//import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
//import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.fraa.ml.Mymodel;
// tensorflow
//import org.tensorflow.lite.Interpreter;
//import org.tensorflow.lite.examples.detection.env.Logger;
//import org.tensorflow.lite.support.common.FileUtil;
//import org.tensorflow.lite.support.image.ImageProcessor;
//import org.tensorflow.lite.support.image.TensorImage;
//import org.tensorflow.lite.support.image.ops.ResizeOp;
//import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
//import org.tensorflow.lite.support.model.Model;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Trace;
import android.util.Log;


import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.tensorbuffer.TensorBufferFloat;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;



// extend react module
public class MyModelModule extends ReactContextBaseJavaModule {
    //    constructor from react app context
    public MyModelModule(ReactApplicationContext reactContext) {
        super(reactContext);
        mContext = reactContext;
//        mReactNativeEventEmitter =  new ReactNativeEventEmitter(reactContext);
    }

    //    protected static Interpreter interpreter = null;
    protected static String interpreter = null;
    String modelString = "";
    ReactApplicationContext mContext;
    //    ReactNativeEventEmitter mReactNativeEventEmitter = ReactNativeEventEmitter.getInstance();
//    todo: read file
    protected File tensorflowFile = null;
    protected Object output = null;
    protected Object input = null;
    private static final String MODEL_PATH = "mymodel.tflite";
    /** Dimensions of inputs. */
    private static final int DIM_BATCH_SIZE = 1;

    private static final int DIM_PIXEL_SIZE = 3;
    //from model inputs
    // static final int DIM_IMG_SIZE_X = 92;
    static final int DIM_IMG_SIZE_X = 112;
    static final int DIM_IMG_SIZE_Y = 112;
    @NonNull
    @Override
    public String getName() {
//        return the name of this class, to use in javascript
        return "MyModel";
    }
//    @ReactMethod
//    public void getCurrentPackageFolderPath(final Promise promise) {
//        promise.resolve(this.mUpdateManager.getCurrentPackageFolderPath());
//    }

    @ReactMethod
    public void loadmodel() {
        BackgroundLoadTask backgroundLoadTask = new BackgroundLoadTask();
        backgroundLoadTask.execute();
    // Declaring the capacity of the ByteBuffer
        int capacity = 50176;
        try {
            // creating object of ByteBuffer
            // and allocating size capacity
            ByteBuffer byteBuffer = createBuffer(capacity);

            // putting the int to byte typecast value
            // in ByteBuffer using putInt() method
//            byteBuffer.put((byte)20);
//            byteBuffer.put((byte)30);
//            byteBuffer.put((byte)40);
//            byteBuffer.put((byte)50);
//            byteBuffer.rewind();

            // print the ByteBuffer
            System.out.println("Original ByteBuffer:  "
                    + Arrays.toString(byteBuffer.array()));
            Mymodel model = Mymodel.newInstance(mContext);

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 112, 112, 1}, DataType.FLOAT32);
            inputFeature0.loadBuffer(byteBuffer);
            TensorBuffer inputFeature1 = TensorBuffer.createFixedSize(new int[]{1, 112, 112, 1}, DataType.FLOAT32);
            inputFeature1.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            Mymodel.Outputs outputs = model.process(inputFeature0, inputFeature1);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
            TensorBufferFloat output= (TensorBufferFloat) outputFeature0;
            WritableMap params = Arguments.createMap();
            Log.i("Debug",String.format("mymodel output %.2f  ",output.getFloatArray()[0]));
            params.putString("status", ""+output.getFloatArray()[0]);
//            sent notify to the bridge: using other method
            sendEvent("BackgroundLoadTask", params);
            // Releases model resources if no longer used.
            model.close();
        }
         catch (IllegalArgumentException e) {

            System.out.println("IllegalArgumentException catched");
        }

        catch (ReadOnlyBufferException e) {

            System.out.println("ReadOnlyBufferException catched");
        }
        catch (IOException e) {
            // TODO Handle the exception
        }
    }
    public ByteBuffer createBuffer(int size){
        ByteBuffer byteBuffer = ByteBuffer.allocate(size);
        for (int i = 0; i < size; i++) {
            byteBuffer.put((byte)(i/100.0));
        }
        return  byteBuffer;
    }
    @ReactMethod
    public void getmodel(Callback successCallback) {
        if(interpreter == null){
            successCallback.invoke(null, interpreter);
        }
        else {
            successCallback.invoke(null, "this model require input amount: "+ modelString);
        }


    }
    @ReactMethod
    public void doInterprete() {
        BackgroundInterprete backgroundInterprete = new BackgroundInterprete();
        backgroundInterprete.execute();
    }
    @ReactMethod
    public void testEvent() {
        WritableMap params = Arguments.createMap();
        params.putString("status", "tes event");
        sendEvent("BackgroundLoadTask", params);
    }

    /** Memory-map the model file in Assets. */
    private static MappedByteBuffer loadModelFile(AssetManager assets, String modelFilename) throws IOException {
        AssetFileDescriptor fileDescriptor = assets.openFd(modelFilename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
        // return Mymodel.newInstance(mContext);
    }


    private void sendEvent(String eventName, WritableMap params) {
//        mReactNativeEventEmitter.sendEvent(eventName,params);
//        ReactNativeEventEmitter.getInstance().sendEvent(eventName,params);
        getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }

    //    helper internal private class
    private class BackgroundLoadTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
//            public notification to the async progress
            if(interpreter == null) {
                publishProgress("current interpreter: null");
            }
            publishProgress("Loading");
            try {
            //    interpreter = new Interpreter(MyModel.loadModelFile( mContext.getAssets(),MODEL_PATH));
                publishProgress("Loaded");
            } catch (Exception e) {
                e.printStackTrace();
                publishProgress("Error");
            }
// Initialise the model
//            try{
//                MappedByteBuffer tfliteModel
//                        = FileUtil.loadMappedFile(mContext.getCurrentActivity(),                   MODEL_PATH);
//                interpreter = new Interpreter(tfliteModel);
//            } catch (IOException e){
//                Log.e("tfliteSupport", "Error reading model", e);
//            }

// Running inference
            if(null != interpreter) {
//                tflite.run(tImage.getBuffer(), probabilityBuffer.getBuffer());
//                modelString = modelString+ interpreter.getInputTensorCount();
                publishProgress("success model: "+interpreter.toString());
//                interpreter.close();
            }
            return "Done";
        }

        @Override
        protected void onProgressUpdate(String... values) {

            StringBuilder sb = new StringBuilder();
            for (String str : values)
                sb.append(str).append(", ");
            WritableMap params = Arguments.createMap();

            params.putString("status", sb.substring(0, sb.length() - 1));
//            sent notify to the bridge: using other method
            sendEvent("BackgroundLoadTask", params);
        }

        @Override
        protected void onPostExecute(String s) {
//            after the progress done and return, notify the bridge
            WritableMap params = Arguments.createMap();
            params.putString("status", "Done");
            sendEvent("BackgroundLoadTask", params);
        }
    }


    private class BackgroundInterprete extends AsyncTask<String, String, String> {
        @Override
        protected String doInBackground(String... params) {

//            public notification to the async progress
            publishProgress("interpreting...");
            //            implement the tasks
            try  {
                Thread.sleep(2000);
                // Object input = new Object();
                // Object output = new Object();
//                interpreter.run(input, output);
//                todo: set up inputs
//            interpreter.runForMultipleInputsOutputs(inputs, map_of_indices_to_outputs);

//            interpreter.runForMultipleInputsOutputs(inputs,outputs);
                //             public int getInputIndex(String opName);
//        public int getOutputIndex(String opName);
                // interpreter.close();

            } catch (Exception e) {
                e.printStackTrace();
                publishProgress("Error");
            }
            return "Done";
        }

        @Override
        protected void onProgressUpdate(String... values) {
            WritableMap params = Arguments.createMap();
            params.putString("status", "Loading");
//            sent notify to the bridge: using other method
            sendEvent("BackgroundInterprete", params);
        }

        @Override
        protected void onPostExecute(String s) {
//            after the progress done and return, notify the bridge
            WritableMap params = Arguments.createMap();
            params.putString("status", "Done");
            sendEvent("BackgroundInterprete", params);
        }
    }








//     public void loadModel(){
//         String file = "";
//         // Interpreter(@NotNull File modelFile);
//         // Interpreter(@NotNull MappedByteBuffer mappedByteBuffer);
//         try (Interpreter interpreter = new Interpreter(file_of_a_tensorflowlite_model)) {
//             // interpreter.run(input, output);
//             interpreter.runForMultipleInputsOutputs(inputs, map_of_indices_to_outputs);
// //             public int getInputIndex(String opName);
// public int getOutputIndex(String opName);
// // interpreter.close();
//           }
//     }
//    provide method being called from react. annotation for injection
    // @ReactMethod
    // public void increment(){
    //     count++;
    //     System.out.println(count);
    // }
//    callback is required to use with react native module api
    // @ReactMethod
    // public void getCount(Callback successCallback) {
    //     successCallback.invoke(null, count);
    // }


}
// val assetManager = context.assets
// val model = loadModelFile(assetManager, "mnist.tflite")
// Read input shape from model file
// val inputShape = interpreter.getInputTensor(0).shape()
// inputImageWidth = inputShape[1]
// inputImageHeight = inputShape[2]
// modelInputSize = FLOAT_TYPE_SIZE * inputImageWidth * inputImageHeight * PIXEL_SIZE

// // Finish interpreter initialization
// this.interpreter = interpreter
// interpreter?.close()
//{

/** Executor to run inference task in the background. */
//     private val executorService: ExecutorService = Executors.newCachedThreadPool()

//     private var inputImageWidth: Int = 0 // will be inferred from TF Lite model.
//     private var inputImageHeight: Int = 0 // will be inferred from TF Lite model.
//     private var modelInputSize: Int = 0 // will be inferred from TF Lite model.

//     fun initialize(): Task<Void> {
//     val task = TaskCompletionSource<Void>()
//     executorService.execute {
//         try {
//         initializeInterpreter()
//         task.setResult(null)
//         } catch (e: IOException) {
//         task.setException(e)
//         }
//     }
//     return task.task
//     }


//     @Throws(IOException::class)
//     private fun initializeInterpreter() {
//     // TODO: Load the TF Lite model from file and initialize an interpreter.

//     // Load the TF Lite model from asset folder and initialize TF Lite Interpreter with NNAPI enabled.
//     val assetManager = context.assets
//     val model = loadModelFile(assetManager, "mnist.tflite")
//     val options = Interpreter.Options()
//     options.setUseNNAPI(true)
//     val interpreter = Interpreter(model, options)

//     // TODO: Read the model input shape from model file.

//     // Read input shape from model file.
//     val inputShape = interpreter.getInputTensor(0).shape()
//     inputImageWidth = inputShape[1]
//     inputImageHeight = inputShape[2]
//     modelInputSize = FLOAT_TYPE_SIZE * inputImageWidth *
//         inputImageHeight * PIXEL_SIZE

//     // Finish interpreter initialization.
//     this.interpreter = interpreter

//     isInitialized = true
//     Log.d(TAG, "Initialized TFLite interpreter.")
//     }

//     @Throws(IOException::class)
//     private fun loadModelFile(assetManager: AssetManager, filename: String): ByteBuffer {
//     val fileDescriptor = assetManager.openFd(filename)
//     val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
//     val fileChannel = inputStream.channel
//     val startOffset = fileDescriptor.startOffset
//     val declaredLength = fileDescriptor.declaredLength
//     return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
//     }

//     private fun classify(bitmap: Bitmap): String {
//     check(isInitialized) { "TF Lite Interpreter is not initialized yet." }

//     // TODO: Add code to run inference with TF Lite.
//     // Pre-processing: resize the input image to match the model input shape.
//     val resizedImage = Bitmap.createScaledBitmap(
//         bitmap,
//         inputImageWidth,
//         inputImageHeight,
//         true
//     )
//     val byteBuffer = convertBitmapToByteBuffer(resizedImage)

//     // Define an array to store the model output.
//     val output = Array(1) { FloatArray(OUTPUT_CLASSES_COUNT) }

//     // Run inference with the input data.
//     interpreter?.run(byteBuffer, output)

//     // Post-processing: find the digit that has the highest probability
//     // and return it a human-readable string.
//     val result = output[0]
//     val maxIndex = result.indices.maxBy { result[it] } ?: -1
//     val resultString =
//         "Prediction Result: %d\nConfidence: %2f"
//         .format(maxIndex, result[maxIndex])

//     return resultString
//     }

//     fun classifyAsync(bitmap: Bitmap): Task<String> {
//     val task = TaskCompletionSource<String>()
//     executorService.execute {
//         val result = classify(bitmap)
//         task.setResult(result)
//     }
//     return task.task
//     }

//     fun close() {
//     executorService.execute {
//         interpreter?.close()
//         Log.d(TAG, "Closed TFLite interpreter.")
//     }
//     }

//     private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
//     val byteBuffer = ByteBuffer.allocateDirect(modelInputSize)
//     byteBuffer.order(ByteOrder.nativeOrder())

//     val pixels = IntArray(inputImageWidth * inputImageHeight)
//     bitmap.getPixels(pixels, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

//     for (pixelValue in pixels) {
//         val r = (pixelValue shr 16 and 0xFF)
//         val g = (pixelValue shr 8 and 0xFF)
//         val b = (pixelValue and 0xFF)

//         // Convert RGB to grayscale and normalize pixel value to [0..1].
//         val normalizedPixelValue = (r + g + b) / 3.0f / 255.0f
//         byteBuffer.putFloat(normalizedPixelValue)
//     }

//     return byteBuffer
//     }

//     companion object {
//     private const val TAG = "DigitClassifier"

//     private const val FLOAT_TYPE_SIZE = 4
//     private const val PIXEL_SIZE = 1

//     private const val OUTPUT_CLASSES_COUNT = 10
//     }
// }
