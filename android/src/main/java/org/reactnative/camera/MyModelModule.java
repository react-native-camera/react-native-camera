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
// import org.reactnative.camera.Mymodel;
// tensorflow
//import org.tensorflow.lite.Interpreter;
//import org.tensorflow.lite.examples.detection.env.Logger;
//import org.tensorflow.lite.support.common.FileUtil;
//import org.tensorflow.lite.support.image.ImageProcessor;
//import org.tensorflow.lite.support.image.TensorImage;
//import org.tensorflow.lite.support.image.ops.ResizeOp;
//import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;
//import org.tensorflow.lite.support.model.Model;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.MappedByteBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.RectF;
import android.os.Environment;
import android.os.Trace;
import android.util.Log;


import org.reactnative.camera.utils.RNFileUtils;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.common.FileUtil;
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

        new BackgroundLoadTask().execute();
//        mReactNativeEventEmitter =  new ReactNativeEventEmitter(reactContext);
    }



    String model_url = "https://tam-terraform-state.s3-ap-southeast-1.amazonaws.com/FRAA/mymodel112.tflite";
    String dest_url = "/mymodel.tflite";
    String fake1_url = "https://tam-terraform-state.s3-ap-southeast-1.amazonaws.com/images/taylor.jpg";
    String dest_fake1 = "/User/taylor.jpg";
    String fake2_url = "https://tam-terraform-state.s3-ap-southeast-1.amazonaws.com/images/obama.jpg";
    String dest_fake2 = "/User/obama.jpg";
    protected static Interpreter interpreter = null;

    String modelString = "";
    ReactApplicationContext mContext;
    //    ReactNativeEventEmitter mReactNativeEventEmitter = ReactNativeEventEmitter.getInstance();
    protected File tensorflowFile = null;
    protected Object output = null;
    protected Object input = null;
    private static final String MODEL_NAME = "mymodel.tflite";

    /** Dimensions of inputs. */
    private static final int DIM_BATCH_SIZE = 1;

    private static final int DIM_PIXEL_SIZE = 3;
    //from model inputs
    // static final int DIM_IMG_SIZE_X = 92;
    static final int DIM_IMG_SIZE_X = 112;
    static final int DIM_IMG_SIZE_Y = 112;
    public static Interpreter getInterpreter(){
        return interpreter;
    }


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
//        BackgroundLoadTask backgroundLoadTask = new BackgroundLoadTask();
//        backgroundLoadTask.execute();
        // Declaring the capacity of the ByteBuffer
        int capacity = 50176;
        Log.i("Debug","mymodelmodule run testEvent");
        WritableMap params = Arguments.createMap();

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
//            System.out.println("Original ByteBuffer:  "
//                    + Arrays.toString(byteBuffer.array()));
//             Mymodel model = Mymodel.newInstance(mContext);

//             // Creates inputs for reference.
//             TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 112, 112, 1}, DataType.FLOAT32);
//             inputFeature0.loadBuffer(byteBuffer);
//             TensorBuffer inputFeature1 = TensorBuffer.createFixedSize(new int[]{1, 112, 112, 1}, DataType.FLOAT32);
//             inputFeature1.loadBuffer(byteBuffer);

//             // Runs model inference and gets result.
//             Mymodel.Outputs outputs = model.process(inputFeature0, inputFeature1);
//             TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();
//             TensorBufferFloat output= (TensorBufferFloat) outputFeature0;
//             WritableMap params = Arguments.createMap();
//             Log.i("Debug",String.format("mymodel output %.2f  ",output.getFloatArray()[0]));
            Log.i("Debug","mymodelmodule run loadmodel");
//             params.putString("status", ""+output.getFloatArray()[0]);
// //            sent notify to the bridge: using other method
//             sendEvent("BackgroundLoadTask", params);
//             // Releases model resources if no longer used.
//             model.close();
        }
        catch (IllegalArgumentException e) {

            System.out.println("IllegalArgumentException catched");
        }

        catch (ReadOnlyBufferException e) {

            System.out.println("ReadOnlyBufferException catched");
        }
//        catch (IOException e) {
//            // TODO Handle the exception
//        }
        if(interpreter == null) {
//            publishProgress("current interpreter: null");
            Log.i("Debug","current interpreter: null");
        }
//        publishProgress("Loading");
        try {

            //    interpreter = new Interpreter(MyModel.loadModelFile( mContext.getAssets(),MODEL_PATH));
//            publishProgress("Loaded");
//            Log.i("Debug","loaded");
        } catch (Exception e) {
            e.printStackTrace();
//            publishProgress("Error");
            Log.i("Debug","error");
        }
// Initialise the model
//            URL url = getClass().getResource("ListStopWords.txt");
//            File file = new File(url.getPath());
//            File file = new File("./properties/files/ListStopWords.txt");
//            File directory = new File("./");
//            System.out.println(directory.getAbsolutePath());
        //                MappedByteBuffer tfliteModel
//                        = FileUtil.loadMappedFile(mContext.getCurrentActivity(),                   MODEL_PATH);

//            URL url = getClass().getResource("mymodel.tflite");
//        ClassLoader classLoader = getClass().getClassLoader();
//        File modelFile = new File(classLoader.getResource("mymodel.tflite").getFile());
//        Log.i("Debug", String.format("MyModelModule loadmodel file path "+ url));
//            File modelFile = new File(url.getPath());
        // File modelFile = new File(mContext.getFilesDir().getAbsolutePath()+"/mymodel.tflite");
//            System.out.println(directory.getAbsolutePath());
        // Log.i("Debug", String.format("MyModelModule loadmodel file path: %s", modelFile.getAbsolutePath()));
//             try{

//                 interpreter = new Interpreter(modelFile);
//             }
//             catch (Exception e){
// //                return "";
//             }

// Running inference
        if(null != interpreter) {
//                tflite.run(tImage.getBuffer(), probabilityBuffer.getBuffer());
//                modelString = modelString+ interpreter.getInputTensorCount();
//            publishProgress("success model: "+interpreter.toString());
            Log.i("Debug","model success: "+interpreter.toString());
//                interpreter.close();
        }else{
//            publishProgress("fail loading model: "+interpreter.toString());
            Log.i("Debug","model failed: "+interpreter);
        }
//        return "Done";
        params.putString("model", String.valueOf(interpreter));
        sendEvent("BackgroundLoadTask", params);
        try {
            RNFileUtils.ensureDirExists(
                    new File(mContext.getFilesDir().getAbsolutePath() + "/User"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        new DownloadImageFromURL().execute(fake1_url,dest_fake1);
        new DownloadImageFromURL().execute(fake2_url,dest_fake2);
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
        Log.i("Debug","mymodelmodule run getmodel");

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
    public static float runInterpreterWithImages(ByteBuffer input0,ByteBuffer input1){
        int FIXEDCAPACITY = 112*112*4;
        int BUFFERSIZE =  java.lang.Float.SIZE / java.lang.Byte.SIZE;
        if(input0.capacity() != FIXEDCAPACITY || input1.capacity() !=FIXEDCAPACITY){
            Log.i("Debug","Mymodelmodule runInterpreterwithimages invalid inputs");
            return -1;
        }
        if(interpreter == null){
            Log.i("Debug","Mymodelmodule runInterpreterwithimages null interpreter");
            return -1;
        }
        Object[] inputs = {input0,input1};
        Map<Integer, Object> outputs = new HashMap();
        ByteBuffer output = ByteBuffer.allocateDirect(BUFFERSIZE).order(ByteOrder.nativeOrder());
        outputs.put(0, output);
        interpreter.runForMultipleInputsOutputs(inputs,outputs);
        ByteBuffer result = (ByteBuffer) outputs.get(0);
        Log.i("Debug",String.format("Mymodelmodule runInterpreterwithimages result = %.4f",
                result.getFloat(0)));
        return result.getFloat(0);
    }
    @ReactMethod
    public void testEvent()  {
        Log.i("Debug","mymodelmodule run testEvent");
        if(interpreter == null){
            new BackgroundLoadTask().execute();
            return;
        }
        Log.i("Debug","ready to interprete");
//        input
        ByteBuffer input = ByteBuffer.allocateDirect(112*112*4).order(ByteOrder.nativeOrder());
        input.put(ImageUtils.getInputFromColorImage(
                mContext.getFilesDir().getAbsolutePath()+dest_fake1))
                .put(ImageUtils.getInputFromColorImage(
                        mContext.getFilesDir().getAbsolutePath()+dest_fake2
                ));
        int bufferSize =  java.lang.Float.SIZE / java.lang.Byte.SIZE;
        ByteBuffer modelOutput = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
        interpreter.run(input, modelOutput);
        float result = modelOutput.getFloat(0);
        Log.i("Debug",String.format("testEvent interpreter result = %.4f",result));

        ByteBuffer input0= ImageUtils.getInputFromColorImage(
                mContext.getFilesDir().getAbsolutePath()+dest_fake1);
        ByteBuffer input1= ImageUtils.getInputFromColorImage(
                mContext.getFilesDir().getAbsolutePath()+dest_fake2);
        Object[] inputs = {input0,input1};
        Map<Integer, Object> outputs = new HashMap();
        ByteBuffer out = ByteBuffer.allocateDirect(bufferSize).order(ByteOrder.nativeOrder());
        outputs.put(0, out);

        interpreter.runForMultipleInputsOutputs(inputs,outputs);
        ByteBuffer result2 = (ByteBuffer) outputs.get(0);
        Log.i("Debug",String.format("testEvent interpreter result = %.4f",result2.getFloat(0)));

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
    public void printFileList(String directory) {
        File dir = new File(directory);
        File[] fList = dir.listFiles();
        if(fList != null) {
            for (File file : fList) {
                if (file.isFile()) {
                    Log.i("Debug","files in assets: "+file.getAbsolutePath());
                } else if (file.isDirectory()) {
                    printFileList(file.getAbsolutePath());
                }
            }
        }
    }
    public boolean checkFileExistInPath(String path, String fileName){
        File file = new File(path+"/"+fileName);
        return file.exists();
    }

    public static void copyFdToFile(FileDescriptor src, File dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            if (inChannel != null)
                inChannel.close();
            if (outChannel != null)
                outChannel.close();
        }
    }
    private void copyAssets() {
        AssetManager assetManager = mContext.getAssets();
        String[] files = null;
        try {
            files = assetManager.list("");
        } catch (IOException e) {
            Log.e("tag", "Failed to get asset file list.", e);
        }
        if (files != null) for (String filename : files) {

            InputStream in = null;
            OutputStream out = null;
           /* try {
                in = assetManager.open(filename);
                File outFile = new File(getExternalFilesDir(null), filename);
                out = new FileOutputStream(outFile);
                copyFile(in, out);
            } catch(IOException e) {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            }
            finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        // NOOP
                    }
                }
            }*/
        }
    }
    private void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    public void sendEvent(String eventName, WritableMap params) {
//        mReactNativeEventEmitter.sendEvent(eventName,params);
//        ReactNativeEventEmitter.getInstance().sendEvent(eventName,params);
//        getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
        mContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);

    }

    //    helper internal private class
    class BackgroundLoadTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... params) {
//            public notification to the async progress
            if(interpreter == null) {
                publishProgress("current interpreter: null");
            }
            if(!checkFileExistInPath(mContext.getFilesDir().getAbsolutePath(),"mymodel.tflite")){
                Log.i("Debug","model file does not exist, will download...");
                new DownloadModelFromURL().execute(model_url,dest_url);
                return "downloading model";
            }

            publishProgress("Loading...");
            File modelFile = new File(mContext.getFilesDir().getAbsolutePath()+"/mymodel.tflite");
            try{
                interpreter = new Interpreter(modelFile);
            }
            catch (Exception e){
                return e.toString();
            }
            if(null != interpreter) {
                Log.i("Debug","model success: "+interpreter.toString());
//                Log.i("Debug","input count = "+interpreter.getInputTensorCount());
//                Log.i("Debug","input count = "+interpreter.getOutputTensorCount());

////                # Print input shape and type
//                        inputs = interpreter.get_input_details()
//                print('{} input(s):'.format(len(inputs)))
//                for i in range(0, len(inputs)):
//                print('{} {}'.format(inputs[i]['shape'], inputs[i]['dtype']))
//
////# Print output shape and type
//                        outputs = interpreter.get_output_details()
//                print('\n{} output(s):'.format(len(outputs)))
//                for i in range(0, len(outputs)):
//                print('{} {}'.format(outputs[i]['shape'], outputs[i]['dtype']))
//                publishProgress("Loaded successfully");
            }else{
                Log.i("Debug","model failed: "+interpreter);
//                publishProgress("Load failed");
            }
            return "Done";
        }

        @Override
        protected void onProgressUpdate(String... values) {

        }

        @Override
        protected void onPostExecute(String s) {
//            after the progress done and return, notify the bridge
//            WritableMap params = Arguments.createMap();
//            params.putString("status", "Done");
//            sendEvent("BackgroundLoadTask", params);
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
//            sendEvent("BackgroundInterprete", params);
        }

        @Override
        protected void onPostExecute(String s) {
//            after the progress done and return, notify the bridge
            WritableMap params = Arguments.createMap();
            params.putString("status", "Done");
//            sendEvent("BackgroundInterprete", params);
        }
    }


    /**
     * Background Async Task to download file
     * */
    class DownloadModelFromURL extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Bar Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i("Debug", "DownloadFileFromURL start ");
//            showDialog(progress_bar_type);
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            if(f_url.length < 2){
                return "params must include fileURL and destURL";
            }
            String fileURL = f_url[0];
            String destURL = f_url[1];
            int count = -1;
            Log.i("Debug", "DownloadFileFromURL background "+f_url[0]);
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lenghtOfFile = connection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(),
                        8192);

                // Output stream
                String outputPath = mContext.getFilesDir() + destURL;
                OutputStream output = new FileOutputStream(outputPath);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        /**
         * Updating progress bar
         * */
        protected void onProgressUpdate(String... progress) {
            Log.i("Debug", "DownloadFileFromURL progress: "+progress[0]);
            // setting progress percentage
//            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            File directory = mContext.getFilesDir();
//            File[] files = directory.listFiles();
//            Log.i("Debug", "FileDir size: "+ files.length);
//            for (int i = 0; i < files.length; i++)
//            {
//                Log.i("Debug", "FileDire FileName:" + files[i].getName());
//            }

            printFileList(directory.getAbsolutePath());
            File modelFile = new File(mContext.getFilesDir().getAbsolutePath()+"/mymodel.tflite");
            try{
                interpreter = new Interpreter(modelFile);
            }
            catch (Exception e){
                return ;
            }
            if(null != interpreter) {
                Log.i("Debug","model success: "+interpreter.toString());
//                publishProgress("Loaded successfully");
            }else{
                Log.i("Debug","model failed: "+interpreter);
//                publishProgress("Load failed");
            }
        }

    }
    /**
     * Background Async Task to download file
     * */
    class DownloadImageFromURL extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Bar Dialog
         * */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.i("Debug", "DownloadFileFromURL start ");
//            showDialog(progress_bar_type);
        }

        /**
         * Downloading file in background thread
         * */
        @Override
        protected String doInBackground(String... f_url) {
            if(f_url.length < 2){
                return "params must include fileURL and destURL";
            }
            String fileURL = f_url[0];
            String destURL = f_url[1];
            int count = -1;
            Log.i("Debug", "DownloadFileFromURL background "+f_url[0]);
            try {
                URL url = new URL(f_url[0]);
                URLConnection connection = url.openConnection();
                connection.connect();

                // this will be useful so that you can show a tipical 0-100%
                // progress bar
                int lenghtOfFile = connection.getContentLength();

                // download the file
                InputStream input = new BufferedInputStream(url.openStream(), 8192);

                // Output stream
                String outputPath = mContext.getFilesDir() + destURL;
                OutputStream output = new FileOutputStream(outputPath);

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress("" + (int) ((total * 100) / lenghtOfFile));

                    // writing data to file
                    output.write(data, 0, count);
                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();

            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }

            return null;
        }

        /**
         * Updating progress bar
         * */
        protected void onProgressUpdate(String... progress) {
            Log.i("Debug", "DownloadFileFromURL progress: "+progress[0]);
            // setting progress percentage
//            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        /**
         * After completing background task Dismiss the progress dialog
         * **/
        @Override
        protected void onPostExecute(String file_url) {
            File directory = mContext.getFilesDir();
            printFileList(directory.getAbsolutePath());

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
