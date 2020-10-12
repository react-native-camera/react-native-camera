//
//  MyModel.swift
//  RNCamera
//
//  Created by Donbosco on 10/12/20.
//


import Foundation
import UIKit
import TensorFlowLite
  
import CoreML
import Vision
import VideoToolbox
// import React
@objc(MyModel)
class MyModel:NSObject{
//class MyModel:NSObject, RCTBridgeModule{
      // Swift doesn't have synthesize - just define the variable
//  var bridge: RCTBridge!
  @objc var bridge: RCTBridge?
  @objc var methodQueue: DispatchQueue?
//  @objc static func requiresMainQueueSetup() -> Bool {
//      return false
//  }
//  @objc static func methodQueue() -> DispatchQueue {
//      return DispatchQueue(label: "MyModel.serial.queue")
//  }
 
//  var myEventEmitter = ReactNativeEventEmitter()
//  coreML only support IOS 13
//  var model:   siemese_nn!
//  func getModel(_ modelPath: String) -> MLModel? {
//      if let m = models[modelPath] { return m }
//      let modelURL = URL(fileURLWithPath: modelPath)
//      if let m = try? MLModel(contentsOf: modelURL) {
//          models[modelPath] = m
//          return m
//      }
//      return nil
//  }
  
  @objc  func testEvent( )  {

    EventEmitter.sharedInstance.dispatch(name: "BackgroundInterprete", body:["status":"load with interpreter: begin....."])
    //  self.bridge.eventDispatcher().sendAppEventWithName( eventName, body: "Woot!" )
    // self.bridge.eventDispatcher().sendAppEvent(withName: "BackgroundLoadTask", body:["status":"load model starting..."]);
   
    
    // using vision framework : only support IOS 13
//    let model =   siemese_nn()
//    guard let visionModel = try? VNCoreMLModel(for: model.model) else {
//    //      fatalError("Someone did a baddie")
//            EventEmitter.sharedInstance.dispatch(name: "BackgroundInterprete", body:["status":"test event vision load model failed"])
//    }
    //load online url with new api
//    let modelUrl : URL = URL(path: "https://tam-terraform-state.s3-ap-southeast-1.amazonaws.com/FRAA/siemese_nn.mlmodel")
//    let compiledUrl = try MLModel.compileModel(at: modelUrl)
//    let model = try MLModel(contentsOf: compiledUrl)
    
    EventEmitter.sharedInstance.dispatch(name: "BackgroundLoadTask", body:["status":"load model starting..."])
     // Getting model path
    guard let modelPath = Bundle.main.path(forResource: "mymodel", ofType: "tflite") else {
      // Error handling...
       EventEmitter.sharedInstance.dispatch(name: "BackgroundLoadTask", body:["status":"load model error: file path?"])
        // self.bridge.eventDispatcher().sendAppEvent(withName: "BackgroundLoadTask", body:["status":"load model error: file path?"]);
        // throw LoadError.invalidSelection
        return
    }
  //  load imnage:
    guard let imagePath = Bundle.main.path(forResource: "image", ofType: "png") else {
      // Error handling...
       EventEmitter.sharedInstance.dispatch(name: "BackgroundLoadTask", body:["status":"load image error: file path?"])
        // self.bridge.eventDispatcher().sendAppEvent(withName: "BackgroundLoadTask", body:["status":"load model error: file path?"]);
        // throw LoadError.invalidSelection
        return
    }
  let serialQueue = DispatchQueue(label: "MyModel.serial.queue")
      serialQueue.async {
          // print("Task 1 started");
          // self.bridge.eventDispatcher().sendAppEvent(withName: "BackgroundLoadTask", body:["status":"load model fetching..."]);
          EventEmitter.sharedInstance.dispatch(name: "BackgroundLoadTask", body:["status":"load images..."])
          // Thread.sleep(forTimeInterval: 3)
          guard   let image = UIImage(contentsOfFile: imagePath) else {
            EventEmitter.sharedInstance.dispatch(name: "BackgroundLoadTask", body:["status":"load images failed"])
            return
          }
        
        //   guard
        //   let rgbData = image.scaledData(
        //     with: CGSize(width: self.inputImageWidth, height: self.inputImageHeight),
        //     byteCount: self.inputImageWidth * self.inputImageHeight * self.inputPixelSize
        //       * self.batchSize,
        //     isQuantized: false
        //   )
        // else {
        //   DispatchQueue.main.async {
        //     completion(.error(SegmentationError.invalidImage))
        //   }
        //   print("Failed to convert the image buffer to RGB data.")
        //   return
        // }
          // print("Task 1 finished");
          EventEmitter.sharedInstance.dispatch(name: "BackgroundLoadTask", body:["status":"load image finish"])
          // self.bridge.eventDispatcher().sendAppEvent(withName: "BackgroundLoadTask", body:["status":"load model finish"]);
     }

    do {
      
    //  let compiledUrl = try MLModel.compileModel(at: modelUrl)
    //  let model = try MLModel(contentsOf: compiledUrl)
      
        // Specify the options for the `Interpreter`.
      var options = Interpreter.Options()
      options.threadCount = 1
      // options.isErrorLoggingEnabled = true
      // Initialize an interpreter with the model.
      let interpreter = try Interpreter(modelPath: modelPath, options: options)
       EventEmitter.sharedInstance.dispatch(name: "BackgroundLoadTask", body:["status":"load model done"])
//  self.bridge.eventDispatcher().sendAppEvent(withName: "BackgroundLoadTask", body:["status":"load model done"]);
      // Allocate memory for the model's input `Tensor`s.
      try interpreter.allocateTensors()

      // let inputData: Data  // Should be initialized

      // input data preparation...
//
//      // Copy the input data to the input `Tensor`.
//      try self.interpreter.copy(inputData, toInputAt: 0)
//
//      // Run inference by invoking the `Interpreter`.
     try interpreter.invoke()
//
//      // Get the output `Tensor`
     let outputTensor = try interpreter.output(at: 0)
     // Read TF Lite model input and output shapes.
    let inputShape0 = try interpreter.input(at: 0).shape
    let inputShape1 = try interpreter.input(at: 1).shape
    let outputShape = try interpreter.output(at: 0).shape
     // Read input shape from model.
    let batchSize = inputShape0.dimensions[0]
    let inputImageWidth = inputShape0.dimensions[1]
    let inputImageHeight = inputShape0.dimensions[2]
    let inputPixelSize = inputShape0.dimensions[3]

      EventEmitter.sharedInstance.dispatch(name: "BackgroundLoadTask",
        body:["status":"load interpreter: success ",
        "inputShape0":"\(inputShape0)",
         "inputShape1":"\(inputShape1)",
        "outputShape":"\(outputShape)",
        "batchSize":"\(batchSize)",
        "inputImageWidth":"\(inputImageWidth)",
        "inputImageHeight":"\(inputImageHeight)",
        "inputPixelSize":"\(inputPixelSize)",
        ])
//
//      // Copy output to `Data` to process the inference results.
     let outputSize = outputTensor.shape.dimensions.reduce(1, {x, y in x * y})
     let outputData =
           UnsafeMutableBufferPointer<Float32>.allocate(capacity: outputSize)
     outputTensor.data.copyBytes(to: outputData)
     var result : Float32=0
     for (index, value) in outputData.enumerated() {
    // print("value \(index): \(value)")
        result += value
      }
      if(interpreter != nil) {
          EventEmitter.sharedInstance.dispatch(name: "BackgroundLoadTask",
            body:["status":"load interpreter: success (not null)","data":"\(outputData)","result":"\(result)"])
        //  self.bridge.eventDispatcher().sendAppEvent(withName: "BackgroundLoadTask", body:["status":"load interpreter: success (not null)"]);
      }
      else {
          EventEmitter.sharedInstance.dispatch(name: "BackgroundLoadTask", body:["status":"load interpreter  failed (null) "])
        //  self.bridge.eventDispatcher().sendAppEvent(withName: "BackgroundLoadTask", body:["status":"load interpreter  failed (null) "]);
      }
      // if (error != nil) { /* Error handling... */
      //   self.bridge.eventDispatcher().sendAppEvent(withName: "BackgroundLoadTask", body:["status":error.localizedDescription]);
      // }
    } catch let error {
      // Error handling...
        EventEmitter.sharedInstance.dispatch(name: "BackgroundLoadTask", body:["status":"error...\(error.localizedDescription)"])
      //  self.bridge.eventDispatcher().sendAppEvent(withName: "BackgroundLoadTask", body:["status":"error..."]);
       return
    }
 }

//  // 1  move the work to a background global queue and run the work in the closure asynchronously
//  DispatchQueue.global(qos: .userInitiated).async { [weak self] in
//    guard let self = self else {
//      return
//    }
//    let overlayImage = self.faceOverlayImageFrom(self.image)
//
//    // 2 the face detection processing is complete and you’ve generated a new image.
//    DispatchQueue.main.async { [weak self] in
//      // 3 you update the UI
//      self?.fadeInNewImage(overlayImage)
//    }
//  }
//private let concurrentPhotoQueue =
//DispatchQueue(
//  label: "com.raywenderlich.GooglyPuff.photoQueue",
//  attributes: .concurrent)
//  func addPhoto(_ photo: Photo) {
//    concurrentPhotoQueue.async(flags: .barrier) { [weak self] in
//      // 1
//      guard let self = self else {
//        return
//      }
//
//      // 2
//      self.unsafePhotos.append(photo)
//
//      // 3
//      DispatchQueue.main.async { [weak self] in
//        self?.postContentAddedNotification()
//      }
//    }
//  }
//
//  @IBAction func groupWaitAction(_ sender: AnyObject) {
//    let concurrentQueue = DispatchQueue(label: "com.gcd.demo.concurrent", attributes: .concurrent)
//    concurrentQueue.async {
//      let taskGroup = DispatchGroup()
//      for i in 0..<100 {
//        taskGroup.enter()
//
//        print("###task \(i) \n")
//        Thread.sleep(forTimeInterval: 0.5)
//
//        taskGroup.leave()
//      }
//
//      taskGroup.notify(queue: DispatchQueue.main, work: DispatchWorkItem(block: {
//        print("It's on main queue now")
//      }))
//    }
//  }
  
  @objc
  static var count = 0;
//  @objc
//  static var serialQueue = DispatchQueue(label: "MyModel.serial.queue")
//  @objc
//  func loadmodel(_ callback: RCTResponseSenderBlock) {
//  //    NSNull() as the first element, which is consider an error in callback.
//    callback([NSNull(), MyModel.count])
//  }
  @objc
  func loadmodel() {
  //    NSNull() as the first element, which is consider an error in callback.
      print("load model is \(MyModel.count)");
      // self.bridge.eventDispatcher().sendAppEvent(withName: "BackgroundLoadTask", body:{"dfasdf"});
      EventEmitter.sharedInstance.dispatch(name: "BackgroundLoadTask", body:["status":"load  begin....."])
      let serialQueue = DispatchQueue(label: "MyModel.serial.queue")
      serialQueue.async {
          // print("Task 1 started");
          // self.bridge.eventDispatcher().sendAppEvent(withName: "BackgroundLoadTask", body:["status":"load model fetching..."]);
          EventEmitter.sharedInstance.dispatch(name: "BackgroundLoadTask", body:["status":"load model fetching..."])
          Thread.sleep(forTimeInterval: 3)
          // print("Task 1 finished");
          EventEmitter.sharedInstance.dispatch(name: "BackgroundLoadTask", body:["status":"load model finish"])
          // self.bridge.eventDispatcher().sendAppEvent(withName: "BackgroundLoadTask", body:["status":"load model finish"]);
     }
    
  }

  @objc
  func getmodel(_ callback: RCTResponseSenderBlock) {
     EventEmitter.sharedInstance.dispatch(name: "BackgroundInterprete", body:["status":"load with interpreter: begin....."])
  //    NSNull() as the first element, which is consider an error in callback.
    callback([NSNull(), "get modeal callback: model data"])
  }
  @objc
  func doInterprete() {
  //    NSNull() as the first element, which is consider an error in callback.
//      callback([NSNull(), "get model "+MyModel.count])
      print("doInterprete model \(MyModel.count  )")
  }
//  @objc
//  func doInterprete(_ callback: RCTResponseSenderBlock) {
//  //    NSNull() as the first element, which is consider an error in callback.
//    callback([NSNull(), "interprete from swift"])
//  }
  // make  Native module code main thread to run on MainQueue
  @objc
  static func requiresMainQueueSetup() -> Bool {
  return true
  }
  static func moduleName() -> String! {
        return "MyModel";
  }
  @objc(addEvent:location:date:)
  func addEvent(name: String, location: String, date: NSNumber) -> Void {
    // Date is ready to use!
  }

//  @objc
//  func constantsToExport() -> [String: Any]! {
//    return ["someKey": "someValue"]
//  }

 func filesInDocumentDir() -> [URL]? {
  //  let documents = NSSearchPathForDirectoriesInDomains(.documentDirectory,
  //                                                               .userDomainMask,
  //                                                               true)[0] as! String
  // let writePath = documents.stringByAppendingPathComponent("file.plist")
//  let swiftArray = NSArray(contentsOfFile: filePath) as? [String]
// if let swiftArray = swiftArray {
//     // now we can use Swift-native array methods
//     find(swiftArray, "findable string")
//     // cast back to NSArray to write
//     (swiftArray as NSArray).writeToFile(filePath, atomically: true)
// }


     if let documentsUrl = FileManager.default.urls(for: .documentDirectory, in: .userDomainMask).first {
        do {
            let directoryContents = try FileManager.default.contentsOfDirectory(at: documentsUrl, includingPropertiesForKeys: nil)
            // return directoryContents.filter{ $0.pathExtension == "m4a" }
            return directoryContents
          } catch {
            return nil
        }
     }
     return nil
  }


       
  @objc
  func constantsToExport() -> [AnyHashable : Any]! {
        return ["bundlePath": Bundle.main.bundlePath,
                "bundleURL": Bundle.main.bundleURL.absoluteString,
                "DocumentDirURL":filesInDocumentDir(),
                "someKey":"someValue"]
    }
    // MARK: - Utils
  /// Construct an UIImage from a list of sRGB pixels.
  private static func imageFromSRGBColorArray(pixels: [UInt32], width: Int, height: Int) -> UIImage?
  {
    guard width > 0 && height > 0 else { return nil }
    guard pixels.count == width * height else { return nil }

    // Make a mutable copy
    var data = pixels

    // Convert array of pixels to a CGImage instance.
    let cgImage = data.withUnsafeMutableBytes { (ptr) -> CGImage in
      let ctx = CGContext(
        data: ptr.baseAddress,
        width: width,
        height: height,
        bitsPerComponent: 8,
        bytesPerRow: MemoryLayout<UInt32>.size * width,
        space: CGColorSpace(name: CGColorSpace.sRGB)!,
        bitmapInfo: CGBitmapInfo.byteOrder32Little.rawValue
          + CGImageAlphaInfo.premultipliedFirst.rawValue
      )!
      return ctx.makeImage()!
    }

    // Convert the CGImage instance to an UIImage instance.
    return UIImage(cgImage: cgImage)
  }

  /// Convert 3-dimension index (image_width x image_height x class_count) to 1-dimension index
  // private func coordinateToIndex(x: Int, y: Int, z: Int) -> Int {
  //   return x * outputImageHeight * outputClassCount + y * outputClassCount + z
  // }

}


  /// - Parameter size: The size to scale the image to.
  /// - Returns: The scaled image or `nil` if image could not be resized.
  // func scaledImage(with size: CGSize) -> UIImage? {
  //   UIGraphicsBeginImageContextWithOptions(size, false, scale)
  //   defer { UIGraphicsEndImageContext() }
  //   draw(in: CGRect(origin: .zero, size: size))
  //   return UIGraphicsGetImageFromCurrentImageContext()?.data.flatMap(UIImage.init)
  // }
