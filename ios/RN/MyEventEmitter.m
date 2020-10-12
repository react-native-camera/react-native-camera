//
//  MyEventEmitter.m
//  RNCamera
//
//  Created by Donbosco on 10/12/20.
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>
#import <React/RCTEventEmitter.h>

@interface

RCT_EXTERN_MODULE(MyEventEmmiter, RCTEventEmitter)

RCT_EXTERN_METHOD(supportedEvents)

// - (dispatch_queue_t)methodQueue
// {
//   return dispatch_queue_create("com.facebook.React.AsyncLocalStorageQueue", DISPATCH_QUEUE_SERIAL);
// }
 + (BOOL)requiresMainQueueSetup
 {
 //  return YES;  // only do this if your module initialization relies on calling UIKit!
    return NO;
 }
@end
