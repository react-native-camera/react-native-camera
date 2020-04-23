#import <opencv2/opencv.hpp>
#import <opencv2/imgcodecs/ios.h>
#import <opencv2/imgproc.hpp>

#import <Foundation/Foundation.h>
#import "RNDocumentScanner.h"

using namespace cv;
using namespace std;

@implementation RNDocumentScanner

bool compareContourAreas(const vector<cv::Point> &l, const vector<cv::Point> &r) {
    return contourArea(l) > contourArea(r);
}

bool comparePointSum(const cv::Point &l, const cv::Point &r) {
    return l.y + l.x < r.y + r.x;
}

bool comparePointDiff(const cv::Point &l, const cv::Point &r) {
    return l.y - l.x < r.y - r.x;
}

bool hasTooFewPoints(const vector<cv::Point> &v) {
    return v.size() < 4;
}

vector<cv::Point> selectCorners(vector<cv::Point> &points) {
    vector<cv::Point> result;
    vector<cv::Point>::iterator tl = min_element(points.begin(), points.end(), comparePointSum);
    result.push_back(*tl);
    
    vector<cv::Point>::iterator tr = min_element(points.begin(), points.end(), comparePointDiff);
    result.push_back(*tr);

    vector<cv::Point>::iterator br = max_element(points.begin(), points.end(), comparePointSum);
    result.push_back(*br);

    vector<cv::Point>::iterator bl = max_element(points.begin(), points.end(), comparePointDiff);
    result.push_back(*bl);
    return result;
}

bool isRectLargeEnough(const vector<cv::Point> &v, cv::Size2f &size) {
    cv::Point tl = v[0];
    cv::Point tr = v[1];
    cv::Point br = v[2];
    cv::Point bl = v[3];

    
    double w1 = hypot(tr.x - tl.x, tr.y - tl.y);
    double w2 = hypot(br.x - bl.x, br.y - bl.y);
    double w = max(w1, w2);
    
    double h1 = hypot(tr.x - br.x, tr.y-br.y);
    double h2 = hypot(tl.x - bl.x, tl.y - bl.y);
    double h = max(h1, h2);
    
    return w*h > .15*size.area();
}

static void findRects(const cv::Mat &openCVImage, vector<vector<cv::Point> > &rects, cv::Size2f &size) {
    Mat gray, canned, _unused;
    cvtColor(openCVImage, gray, COLOR_RGBA2GRAY, 4);
    GaussianBlur(gray, gray, cv::Size(5, 5), 0);
    double thresholdHigh = threshold(gray, _unused, 0, 255, THRESH_BINARY|THRESH_OTSU);
    Canny(gray, canned, .25 * thresholdHigh, thresholdHigh);
    
    vector<vector<cv::Point>> contours;
    findContours(canned, contours, RETR_EXTERNAL, CHAIN_APPROX_TC89_KCOS);
    
    // filter elements with < 4 points
    contours.erase(remove_if(begin(contours), end(contours), hasTooFewPoints), end(contours));
    
    // sort by contourArea
    sort(contours.begin(), contours.end(), compareContourAreas);
    
    // keep first 5 (should be biggest)
    if (contours.size() > 5) contours.resize(5);
    
    for (int i = 0; i < contours.size(); i++) {
        vector<cv::Point> approximation, quad;
        vector<cv::Point2f> input(contours[i].begin(), contours[i].end());
        double perimeter = arcLength(input, true);
        
        approxPolyDP(contours[i], approximation, 0.1 * perimeter, true);
        
        quad = selectCorners(approximation);
        if (isRectLargeEnough(quad, size)) {
            rects.push_back(quad);
        }
    }
}


- (BOOL)isRealDetector
{
    return true;
}

- (UIImage *)getDocument:(UIImage *)image
{
    CGSize imgSize = image.size;
    Size2f size = Size2f(imgSize.height, imgSize.width);
    vector<vector<cv::Point>> rects;

    Mat openCVImage;
    UIImageToMat(image, openCVImage, true);
        
    findRects(openCVImage, rects, size);
    
    if (rects.size() == 0) return image;

    CIImage *cimage = [[CIImage alloc] initWithCGImage:image.CGImage];
    cimage = [self correctPerspectiveForImage:cimage withRect:@{
        @"topLeft": @{
                @"x": @(rects[0][0].x),
                @"y": @(rects[0][0].y)},
        @"topRight": @{
                @"x": @(rects[0][1].x),
                @"y": @(rects[0][1].y)},
        @"bottomRight": @{
                @"x": @(rects[0][2].x),
                @"y": @(rects[0][2].y)},
        @"bottomLeft": @{
                @"x": @(rects[0][3].x),
                @"y": @(rects[0][3].y)}
    }];

    UIGraphicsBeginImageContext(CGSizeMake(cimage.extent.size.height, cimage.extent.size.width));
    [[UIImage imageWithCIImage:cimage scale:1.0 orientation:UIImageOrientationRight] drawInRect:CGRectMake(0, 0, cimage.extent.size.height, cimage.extent.size.width)];
    image = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();

    return image;
}


- (void)findDocumentInFrame:(UIImage *)image
                     scaleX:(float)scaleX
                     scaleY:(float)scaleY
                  completed:(postDetectionBlock)completed
{
    CGSize imgSize = image.size;
    Size2f size = Size2f(imgSize.height, imgSize.width);
    vector<vector<cv::Point>> rects;

    Mat openCVImage;
    UIImageToMat(image, openCVImage, true);
        
    findRects(openCVImage, rects, size);
    
    if (rects.size() == 0) return completed(nil);
    
    
    return completed(@{
                @"tl": @{@"x": [NSNumber numberWithFloat:rects[0][3].x * scaleX],
                         @"y": [NSNumber numberWithFloat:(image.size.height - rects[0][3].y) * scaleY]},
                @"tr": @{@"x": [NSNumber numberWithFloat:rects[0][0].x * scaleX],
                         @"y": [NSNumber numberWithFloat:(image.size.height - rects[0][0].y) * scaleY]},
                @"br": @{@"x": [NSNumber numberWithFloat:rects[0][1].x * scaleX],
                         @"y": [NSNumber numberWithFloat:(image.size.height - rects[0][1].y) * scaleY]},
                @"bl": @{@"x": [NSNumber numberWithFloat:rects[0][2].x * scaleX],
                         @"y": [NSNumber numberWithFloat:(image.size.height - rects[0][2].y) * scaleY]}
                });

}

- (CIImage *)correctPerspectiveForImage:(CIImage *)image withRect:(NSDictionary *)rect
{
    CGPoint newLeft = CGPointMake([rect[@"topLeft"][@"x"] floatValue], [rect[@"topLeft"][@"y"] floatValue]);
    CGPoint newRight = CGPointMake([rect[@"topRight"][@"x"] floatValue], [rect[@"topRight"][@"y"] floatValue]);
    CGPoint newBottomRight = CGPointMake([rect[@"bottomRight"][@"x"] floatValue], [rect[@"bottomRight"][@"y"] floatValue]);
    CGPoint newBottomLeft = CGPointMake([rect[@"bottomLeft"][@"x"] floatValue], [rect[@"bottomLeft"][@"y"] floatValue]);

    NSDictionary *rectangleCoordinates = @{
        @"inputTopLeft": [CIVector vectorWithCGPoint:newLeft],
        @"inputTopRight": [CIVector vectorWithCGPoint:newRight],
        @"inputBottomLeft": [CIVector vectorWithCGPoint:newBottomLeft],
        @"inputBottomRight": [CIVector vectorWithCGPoint:newBottomRight]
    };

    return [image imageByApplyingFilter:@"CIPerspectiveCorrection" withInputParameters:rectangleCoordinates];
}

@end
