//
//  PoseEstimatorMLCore.swift
//  react-native-camera
//
//  Created by Kasper Dissing Bargsteen on 11/05/2019.
//

import Foundation
import Vision


@available(iOS 11.0, *)
@objc class PoseEstimatorMLCore : NSObject {

    typealias PoseEstimationModel = model_hourglass_192

    var request: VNCoreMLRequest?
    var visionModel: VNCoreMLModel?
    var onInferenceComplete: (([[Int]]) -> ())?
    
    @objc
    public class func newInstance(onInferenceComplete: @escaping ([[Int]]) -> ()) -> PoseEstimatorMLCore {
        return PoseEstimatorMLCore(onInferenceComplete: onInferenceComplete)
    }

    init(onInferenceComplete: @escaping ([[Int]]) -> ()){
        super.init()
        self.onInferenceComplete = onInferenceComplete
        if let visionModel = try? VNCoreMLModel(for: PoseEstimationModel().model){
                        self.visionModel = visionModel
                        request = VNCoreMLRequest(model: visionModel, completionHandler: visionRequestDidComplete)
                        request?.imageCropAndScaleOption = .scaleFill
                    } else {
                        fatalError("Could not load PoseEstimation Model")
                    }
    }
    
    // MARK: - Inferencing
    @objc
    public func predictUsingVision(pixelBuffer: CVPixelBuffer) {
        guard let request = request else { fatalError() }
        // vision framework configures the input size of image following our model's input configuration automatically
        let handler = VNImageRequestHandler(cvPixelBuffer: pixelBuffer)
        try? handler.perform([request])
    }

    // MARK: - Poseprocessing
    func visionRequestDidComplete(request: VNRequest, error: Error?) {
        if let observations = request.results as? [VNCoreMLFeatureValueObservation],
            let heatmap = observations.first?.featureValue.multiArrayValue {

            // convert heatmap to Array<Array<Double>>
            let bodyPoints = heatmap.convertHeatmapToBodyPoints()

            DispatchQueue.main.sync {
                self.onInferenceComplete?(bodyPoints)
            }
        }
    }
}
