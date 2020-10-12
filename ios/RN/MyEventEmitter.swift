//
//  MyEventEmitter.swift
//  RNCamera
//
//  Created by Donbosco on 10/12/20.
//

import UIKit
import Foundation
@objc(MyEventEmmiter)
open class MyEventEmitter: RCTEventEmitter {
  override init() {
      super.init()
      
      EventEmitter.sharedInstance.registerEventEmitter(eventEmitter: self)
  }
  @objc
 static var serialQueue = DispatchQueue(label: "ReactNativeEventEmitter.serial.queue")
  
  /// Base overide for RCTEventEmitter.
  ///
  /// - Returns: all supported events
  @objc open override func supportedEvents() -> [String] {
      return EventEmitter.sharedInstance.allEvents
  }
}

