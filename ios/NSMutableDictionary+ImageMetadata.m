//
//  NSMutableDictionary+ImageMetadata.m
//  RCTCamera
//
//  Created by Jehan Tremback on 7/10/15.
//
//

#import <Foundation/Foundation.h>
#import <ImageIO/ImageIO.h>

@interface NSMutableDictionary(ImageMetadata)
- (void)mergeMetadata:(NSDictionary *)inputMetadata;
@end

@implementation NSMutableDictionary(ImageMetadata)

- (void)mergeMetadata:(NSDictionary *)inputMetadata {
  NSDictionary *inputMetadataLocation = [inputMetadata objectForKey:@"location"];
  if (inputMetadataLocation) {
    // Add GPS stuff
    [self setObject:[self getGPSDictionaryForLocation:inputMetadataLocation] forKey:(NSString *)kCGImagePropertyGPSDictionary];
  }
}

- (NSMutableDictionary *)getGPSDictionaryForLocation:(NSDictionary *)location {
  NSMutableDictionary *gps = [NSMutableDictionary dictionary];
  NSDictionary *coords = [location objectForKey:@"coords"];
  // GPS tag version
  [gps setObject:@"2.2.0.0" forKey:(NSString *)kCGImagePropertyGPSVersion];

  // Timestamp
  double timestamp = floor([[location objectForKey:@"timestamp"] doubleValue]);
  NSDate *date = [NSDate dateWithTimeIntervalSince1970:timestamp];
  NSDateFormatter *formatter = [[NSDateFormatter alloc] init];
  [formatter setDateFormat:@"HH:mm:ss.SSSSSS"];
  [formatter setTimeZone:[NSTimeZone timeZoneWithAbbreviation:@"UTC"]];
  [gps setObject:[formatter stringFromDate:date] forKey:(NSString *)kCGImagePropertyGPSTimeStamp];
  [formatter setDateFormat:@"yyyy:MM:dd"];
  [gps setObject:[formatter stringFromDate:date] forKey:(NSString *)kCGImagePropertyGPSDateStamp];

  // Latitude
  double latitude = [[coords objectForKey:@"latitude"] doubleValue];
  if (latitude < 0) {
    latitude = -latitude;
    [gps setObject:@"S" forKey:(NSString *)kCGImagePropertyGPSLatitudeRef];
  } else {
    [gps setObject:@"N" forKey:(NSString *)kCGImagePropertyGPSLatitudeRef];
  }
  [gps setObject:[NSNumber numberWithFloat:latitude] forKey:(NSString *)kCGImagePropertyGPSLatitude];

  // Longitude
  double longitude = [[coords objectForKey:@"longitude"] doubleValue];
  if (longitude < 0) {
    longitude = -longitude;
    [gps setObject:@"W" forKey:(NSString *)kCGImagePropertyGPSLongitudeRef];
  } else {
    [gps setObject:@"E" forKey:(NSString *)kCGImagePropertyGPSLongitudeRef];
  }
  [gps setObject:[NSNumber numberWithFloat:longitude] forKey:(NSString *)kCGImagePropertyGPSLongitude];

  // Altitude
  double altitude = [[coords objectForKey:@"altitude"] doubleValue];
  if (!isnan(altitude)){
    if (altitude < 0) {
      altitude = -altitude;
      [gps setObject:@"1" forKey:(NSString *)kCGImagePropertyGPSAltitudeRef];
    } else {
      [gps setObject:@"0" forKey:(NSString *)kCGImagePropertyGPSAltitudeRef];
    }
    [gps setObject:[NSNumber numberWithFloat:altitude] forKey:(NSString *)kCGImagePropertyGPSAltitude];
  }

  // Speed, must be converted from m/s to km/h
  double speed = [[coords objectForKey:@"speed"] doubleValue];
  if (speed >= 0){
    [gps setObject:@"K" forKey:(NSString *)kCGImagePropertyGPSSpeedRef];
    [gps setObject:[NSNumber numberWithFloat:speed*3.6] forKey:(NSString *)kCGImagePropertyGPSSpeed];
  }

  // Heading
  double heading = [[coords objectForKey:@"heading"] doubleValue];
  if (heading >= 0){
    [gps setObject:@"T" forKey:(NSString *)kCGImagePropertyGPSTrackRef];
    [gps setObject:[NSNumber numberWithFloat:heading] forKey:(NSString *)kCGImagePropertyGPSTrack];
  }

  return gps;
}
@end
