////
////  MLMultiArray+PoseEstimation.swift
////  react-native-camera
////
////  Created by Kasper Dissing Bargsteen on 11/05/2019.
////

import Foundation

import CoreML


@available(iOS 11.0, *)
extension MLMultiArray {
    
    func convertHeatmapToBodyPoints() -> [[Int]] {
        guard self.shape.count >= 3 else {
            print("heatmap's shape is invalid. \(self.shape)")
            return []
        }
        let keypoint_number = self.shape[0].intValue
        let heatmap_w = self.shape[1].intValue
        let heatmap_h = self.shape[2].intValue
        
        var n_kpoints = Array(repeating: Array(repeating: 0, count: 2), count: 14)
        
        for k in 0..<keypoint_number {
            var maxConfidence : Double = 0;
            for i in 0..<heatmap_w {
                for j in 0..<heatmap_h {
                    let index = k*(heatmap_w*heatmap_h) + i*(heatmap_h) + j
                    let confidence = self[index].doubleValue
                    guard confidence > 0 else { continue }
                    if n_kpoints[k] == [0, 0] || maxConfidence < confidence {
                        n_kpoints[k] = [j,i]
                        maxConfidence = confidence
                    }
                }
            }
        }
        
        
        // transpose to (1.0, 1.0)
//        n_kpoints = n_kpoints.map { kpoint -> BodyPoint? in
//            if let kp = kpoint {
//                return BodyPoint(maxPoint: CGPoint(x: (kp.maxPoint.x+0.5)/CGFloat(heatmap_w),
//                                                   y: (kp.maxPoint.y+0.5)/CGFloat(heatmap_h)),
//                                 maxConfidence: kp.maxConfidence)
//            } else {
//                return nil
//            }
//        }
        
        return n_kpoints
    }
}
