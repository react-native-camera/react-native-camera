/*  UIImage+SimpleResize.h
 *
 *  Modified by Robert Ryan on 5/19/11.
 */

@import UIKit;

/** Image resizing category.
 *
 *  Modified by Robert Ryan on 5/19/11.
 *
 *  Inspired by http://ofcodeandmen.poltras.com/2008/10/30/undocumented-uiimage-resizing/
 *  but adjusted to support AspectFill and AspectFit modes.
 */

@interface UIImage (SimpleResize)

/** Resize the image to be the required size, stretching it as needed.
 *
 * @param size         The new size of the image.
 * @param contentMode  The `UIViewContentMode` to be applied when resizing image.
 *                     Either `UIViewContentModeScaleToFill`, `UIViewContentModeScaleAspectFill`, or
 *                     `UIViewContentModeScaleAspectFit`.
 *
 * @return             Return `UIImage` of resized image.
 */

- (UIImage * _Nullable)imageByScalingToSize:(CGSize)size contentMode:(UIViewContentMode)contentMode;

/** Resize the image to be the required size, stretching it as needed.
 *
 * @param size         The new size of the image.
 * @param contentMode  The `UIViewContentMode` to be applied when resizing image.
 *                     Either `UIViewContentModeScaleToFill`, `UIViewContentModeScaleAspectFill`, or
 *                     `UIViewContentModeScaleAspectFit`.
 * @param scale        The scale factor to apply to the bitmap. If you specify a value of 0.0, the scale factor is set to the scale factor of the device’s main screen.
 *
 * @return             Return `UIImage` of resized image.
 */

- (UIImage * _Nullable)imageByScalingToSize:(CGSize)size contentMode:(UIViewContentMode)contentMode scale:(CGFloat)scale;

/** Crop the image to be the required size.
 *
 * @param bounds       The bounds to which the new image should be cropped.
 *
 * @return             Cropped `UIImage`.
 */

- (UIImage * _Nullable)imageByCroppingToBounds:(CGRect)bounds;

/** Crop the image to be the required size.
 *
 * @param bounds       The bounds to which the new image should be cropped.
 * @param scale        The scale factor to apply to the bitmap. If you specify a value of 0.0, the scale factor is set to the scale factor of the device’s main screen.
 *
 * @return             Cropped `UIImage`.
 */

- (UIImage * _Nullable)imageByCroppingToBounds:(CGRect)bounds scale:(CGFloat)scale;

/** Resize the image to fill the rectange of the specified size, preserving the aspect ratio, trimming if needed.
 *
 * @param size    The new size of the image.
 *
 * @return        Return `UIImage` of resized image.
 */

- (UIImage * _Nullable)imageByScalingAspectFillSize:(CGSize)size;

/** Resize the image to fill the rectange of the specified size, preserving the aspect ratio, trimming if needed.
 *
 * @param size    The new size of the image.
 * @param scale   The scale factor to apply to the bitmap. If you specify a value of 0.0, the scale factor is set to the scale factor of the device’s main screen.
 *
 * @return        Return `UIImage` of resized image.
 */

- (UIImage * _Nullable)imageByScalingAspectFillSize:(CGSize)size scale:(CGFloat)scale;

/** Resize the image to be the required size, stretching it as needed.
 *
 * @param size    The new size of the image.
 *
 * @return        Resized `UIImage` of resized image.
 */

- (UIImage * _Nullable)imageByScalingToFillSize:(CGSize)size;

/** Resize the image to be the required size, stretching it as needed.
 *
 * @param size    The new size of the image.
 * @param scale   The scale factor to apply to the bitmap. If you specify a value of 0.0, the scale factor is set to the scale factor of the device’s main screen.
 *
 * @return        Resized `UIImage` of resized image.
 */

- (UIImage * _Nullable)imageByScalingToFillSize:(CGSize)size scale:(CGFloat)scale;

/** Resize the image to fit within the required size, preserving the aspect ratio, with no trimming taking place.
 *
 * @param size    The new size of the image.
 *
 * @return        Return `UIImage` of resized image.
 */

- (UIImage * _Nullable)imageByScalingAspectFitSize:(CGSize)size;

/** Resize the image to fit within the required size, preserving the aspect ratio, with no trimming taking place.
 *
 * @param size    The new size of the image.
 * @param scale   The scale factor to apply to the bitmap. If you specify a value of 0.0, the scale factor is set to the scale factor of the device’s main screen.
 *
 * @return        Return `UIImage` of resized image.
 */

- (UIImage * _Nullable)imageByScalingAspectFitSize:(CGSize)size scale:(CGFloat)scale;

@end
