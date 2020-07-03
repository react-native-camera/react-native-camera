//
//  RNCustomWhiteBalanceSettings.h
//  RNCamera
//
//  Created by René Fischer on 02.04.20.
//

#import <Foundation/Foundation.h>

@interface RNCustomWhiteBalanceSettings : NSObject

@property(nonatomic, assign) float temperature;
@property(nonatomic, assign) float tint;
@property(nonatomic, assign) float redGainOffset;
@property(nonatomic, assign) float greenGainOffset;
@property(nonatomic, assign) float blueGainOffset;

@end
