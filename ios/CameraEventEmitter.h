
//  CameraEventEmitter.h
//  darkroom
//
//  Created by Jonathan Laflamme on 2019-05-17.
//  Copyright © 2019 Exposio. All rights reserved.
//

#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface CameraEventEmitter : RCTEventEmitter <RCTBridgeModule>

+ (id)allocWithZone:(NSZone *)zone;
- (BOOL)hasListener;
- (void)sendOnLowLightChange:(BOOL)isLowLight;
- (void)sendOnMovementChange:(BOOL)isMoving;

@end
