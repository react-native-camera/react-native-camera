//
//  CameraEventEmmiter.m
//  RCTCamera
//
//  Created by Jonathan Laflamme on 2019-05-17.
//

#import <Foundation/Foundation.h>
#import "CameraEventEmitter.h"

@implementation CameraEventEmitter
{
    bool hasListeners;
}



RCT_EXPORT_MODULE();

+ (id)allocWithZone:(NSZone *)zone {
    static CameraEventEmitter *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [super allocWithZone:zone];
    });
    return sharedInstance;
}

- (BOOL)hasListener {
    return hasListeners;
}

// Will be called when this module's first listener is added.
-(void)startObserving {
    hasListeners = YES;
    // Set up any upstream listeners or background tasks as necessary
}

// Will be called when this module's last listener is removed, or on dealloc.
-(void)stopObserving {
    hasListeners = NO;
    // Remove upstream listeners, stop unnecessary background tasks
}

- (NSArray<NSString *> *)supportedEvents {
    return @[@"onLowLightChange", @"onMovementChange", @"onCameraDimensionChange"];
}

- (void)sendOnLowLightChange:(BOOL)isLowLight {
    if(hasListeners) {
        [self sendEventWithName:@"onLowLightChange" body:isLowLight? @YES: @NO];
    }
}

- (void)sendOnMovementChange:(BOOL)isMoving {
    if(hasListeners) {
        [self sendEventWithName:@"onMovementChange" body:isMoving? @YES: @NO];
    }
}

- (void)sendOnDimensionChange:(NSDictionary *)dimensions {
    if(hasListeners) {
        [self sendEventWithName:@"onCameraDimensionChange" body:dimensions];
    }
}

@end
