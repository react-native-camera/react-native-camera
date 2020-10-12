//
//  MyModel.m
//  RNCamera
//
//  Created by Donbosco on 10/12/20.
//


#import <Foundation/Foundation.h>
// #import <React/RCTBridgeModule.h>
#import <React/RCTEventDispatcher.h>
#import <React/RCTEventEmitter.h>


@interface

RCT_EXTERN_MODULE(MyModel, NSObject);
// RCT_EXTERN_MODULE(ReactNativeEventEmitter, RCTEventEmitter);

// RCT_EXTERN_METHOD(supportedEvents);
RCT_EXTERN_METHOD(loadmodel);
////expose the method to get result
RCT_EXTERN_METHOD(getmodel: (RCTResponseSenderBlock)callback);
RCT_EXTERN_METHOD(doInterprete);
RCT_EXTERN_METHOD(testEvent);
// RCT_EXTERN_METHOD(supportedEvents)

// RCT_EXTERN_METHOD(addEvent:(NSString *)name location:(NSString *)location date:(nonnull NSNumber *)date)
- (dispatch_queue_t)methodQueue
{
  return dispatch_queue_create("com.facebook.React.AsyncLocalStorageQueue", DISPATCH_QUEUE_SERIAL);
}
// RCT_EXPORT_METHOD(doSomethingExpensive:(NSString *)param callback:(RCTResponseSenderBlock)callback)
// {
//   dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
//     // Call long-running code on background thread
 
    
//     NSString *greeting =   @"Welcome, you an administrator.";
// //    ...
//     // You can invoke callback from any thread/queue
//     callback(@[greeting]);
//   });
// }

////{
////  backgroundQueue = dispatch_queue_create("anynameisok", NULL);
////  dispatch_async(backgroundQueue, ^{
////    NSLog(@"processing background");
////    // dispatch a event to the app , through the bridge, with a name, and a body
////    [self.bridge.eventDispatcher sendAppEventWithName:@"backgroundProgress" body:@{@"status": @"Loading"}];
////    // sleep 5 second as if running codes...
////    [NSThread sleepForTimeInterval:5];
////    NSLog(@"slept");
////    // notify system that task completed
////    dispatch_async(dispatch_get_main_queue(), ^{
////      NSLog(@"Done processing; main thread");
////        // dispatch a event to the app , through the bridge, with a name, and a body
////      [self.bridge.eventDispatcher sendAppEventWithName:@"backgroundProgress" body:@{@"status": @"Done"}];
////    });
////  });
////}
//
//
//
+ (BOOL)requiresMainQueueSetup
{
//  return YES;  // only do this if your module initialization relies on calling UIKit!
   return NO;
}
@end


//@implementation MyModelManager
//
//RCT_EXPORT_MODULE();
//
//- (NSArray<NSString *> *)supportedEvents
//{
//  return @[@"EventReminder"];
//}
//
//- (void)calendarEventReminderReceived:(NSNotification *)notification
//{
//  NSString *eventName = notification.userInfo[@"name"];
//  [self sendEventWithName:@"EventReminder" body:@{@"name": eventName}];
//}
//
//@end
