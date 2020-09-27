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

bool isRectLargeEnough(const vector<cv::Point> &quad, const cv::Size2f &size) {
    cv::Point tl = quad[0], tr = quad[1], br = quad[2], bl = quad[3];

    double w1 = hypot(tr.x - tl.x, tr.y - tl.y);
    double w2 = hypot(br.x - bl.x, br.y - bl.y);
    double w = max(w1, w2);
    
    double h1 = hypot(tr.x - br.x, tr.y-br.y);
    double h2 = hypot(tl.x - bl.x, tl.y - bl.y);
    double h = max(h1, h2);
    
    return w*h > .15*size.area();
}

bool hasFourDifferentPoints(const vector<cv::Point> &quad) {
    cv::Point tl = quad[0], tr = quad[1], br = quad[2], bl = quad[3];
    return tl.x != tr.x && tl.y != bl.y && tr.y != br.y && bl.x != br.x;
}

void scalePoints(vector<cv::Point> &p, const float &ratio) {
    for (int i=0; i<p.size(); i++) {
        p[i].x = ratio * p[i].x;
        p[i].y = ratio * p[i].y;
    }
}

static void findRects(const cv::Mat &openCVImage, vector<vector<cv::Point>> &rects, const cv::Size2f &size) {
    float ratio = size.height > 500 ? size.height / 500.0F : 1;
    int height = round(size.height / ratio);
    int width = round(size.width / ratio);
    cv::Size2f rszdSize = Size2f(width, height);

    Mat resized, gray, canned, _unused;

    resize(openCVImage, resized, cv::Size(width, height));
    cvtColor(resized, gray, COLOR_RGBA2GRAY, 4);
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
    if (contours.size() > 20) contours.resize(20);
    
    for (int i = 0; i < contours.size(); i++) {
        vector<cv::Point> approximation, quad;
        vector<cv::Point2f> input(contours[i].begin(), contours[i].end());
        double perimeter = arcLength(input, true);
        
        approxPolyDP(contours[i], approximation, 0.1 * perimeter, true);
        
        quad = selectCorners(approximation);
        if (hasFourDifferentPoints(quad) && isRectLargeEnough(quad, rszdSize)) {
            scalePoints(quad, ratio); // scale back to original dims
            rects.push_back(quad);
        }
    }

}

static void correctPerspective(cv::Mat &src, cv::Mat &doc, vector<cv::Point> &rect)
{
    cv::Point2f tl = cv::Point2f(rect[0].x, rect[0].y);
    cv::Point2f tr = cv::Point2f(rect[1].x, rect[1].y);
    cv::Point2f br = cv::Point2f(rect[2].x, rect[2].y);
    cv::Point2f bl = cv::Point2f(rect[3].x, rect[3].y);

    double widthA = hypot(br.x - bl.x, br.y - bl.y);
    double widthB = hypot(tr.x - tl.x, tr.y - tl.y);
    double width = max(widthA, widthB);
    int maxWidth = round(width);

    double heightA = hypot(tr.x - br.x, tr.y - br.y);
    double heightB = hypot(tl.x - bl.x, tl.y - bl.y);
    double height = max(heightA, heightB);
    int maxHeight = round(height);

    doc = Mat(maxHeight, maxWidth, CV_8UC4);

    const Point2f src_pts[4] = {tl, tr, br, bl};
    
    cv::Point2f dtl = cv::Point2f(0.0, 0.0);
    cv::Point2f dtr = cv::Point2f(width, 0.0);
    cv::Point2f dbr = cv::Point2f(width, height);
    cv::Point2f dbl = cv::Point2f(0.0, height);
    const Point2f dst_pts[4] = {dtl, dtr, dbr, dbl};

    Mat m = getPerspectiveTransform(src_pts, dst_pts);

    warpPerspective(src, doc, m, doc.size());
}


- (BOOL)isRealDetector
{
    return true;
}

- (UIImage *)normalizedImage:(UIImage *)image {
    if ([image imageOrientation] == UIImageOrientationUp) return image;

    UIGraphicsBeginImageContextWithOptions(image.size, NO, image.scale);
    [image drawInRect:(CGRect){0, 0, image.size}];
    UIImage *normalizedImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return normalizedImage;
}

- (UIImage *)getDocument:(UIImage *)image
{
    image = [self normalizedImage:image];
    
    Mat openCVImage;
    UIImageToMat(image, openCVImage, true);

    vector<vector<cv::Point>> rects;
    Size2f size = Size2f(openCVImage.cols, openCVImage.rows);
    findRects(openCVImage, rects, size);

    if (rects.size() == 0) return image;
    
    Mat doc;
    correctPerspective(openCVImage, doc, rects[0]);
    image = MatToUIImage(doc);

    return image;
}


- (void)findDocumentInFrame:(UIImage *)image
                     scaleX:(float)scaleX
                     scaleY:(float)scaleY
                  completed:(postDetectionBlock)completed
{
    CGSize imgSize = image.size;
    Size2f size = Size2f(imgSize.width, imgSize.height);
    vector<vector<cv::Point>> rects;

    Mat openCVImage;
    UIImageToMat(image, openCVImage, true);

    findRects(openCVImage, rects, size);
    
    if (rects.size() == 0) return completed(nil);

    return completed(@{
            @"tl": @{@"x": [NSNumber numberWithFloat:rects[0][0].x * scaleX],
                     @"y": [NSNumber numberWithFloat:rects[0][0].y * scaleY]},
            @"tr": @{@"x": [NSNumber numberWithFloat:rects[0][1].x * scaleX],
                     @"y": [NSNumber numberWithFloat:rects[0][1].y * scaleY]},
            @"br": @{@"x": [NSNumber numberWithFloat:rects[0][2].x * scaleX],
                     @"y": [NSNumber numberWithFloat:rects[0][2].y * scaleY]},
            @"bl": @{@"x": [NSNumber numberWithFloat:rects[0][3].x * scaleX],
                     @"y": [NSNumber numberWithFloat:rects[0][3].y * scaleY]}
            });

}

@end
