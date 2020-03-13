/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow
 */

import React, { useCallback, useState } from 'react';
import {
  View,
  Dimensions,
  Button,
  Text
} from 'react-native';

import { RNCamera } from 'react-native-camera';

const App: () => React$Node = () => {
  const [showCamera, setShowCamera] = useState();

  const CAM_VIEW_HEIGHT = Dimensions.get('screen').width * 1.5;
  const CAM_VIEW_WIDTH = Dimensions.get('screen').width;

  // The following are based on landscape orientation with home button to the right

  // left
  const leftMargin = 100;
  // top
  const topMargin = 50;
  // width (height of portrait orientation)
  const frameWidth = 200;
  // height (width of portrait orientation)
  const frameHeight = 250;

  const scanAreaX = leftMargin / CAM_VIEW_HEIGHT;
  const scanAreaY = topMargin / CAM_VIEW_WIDTH;
  const scanAreaWidth = frameWidth / CAM_VIEW_HEIGHT;
  const scanAreaHeight = frameHeight / CAM_VIEW_WIDTH;


  const onBarCodeRead = useCallback((result) => {
    if (result) {
      const { type, data } = result;
      if (data) {
        console.log('code', data);
      }
    }
  }, []);

  return (
    <View style={{ flex: 1 }}>
      <Button title="Show/hide camera" onPress={() => {
        setShowCamera(!showCamera);
      }} />
      <View style={{ flex: 1 }} />
      {
        showCamera ? <RNCamera
          captureAudio={false}
          type={RNCamera.Constants.Type.back}
          onBarCodeRead={onBarCodeRead}
          rectOfInterest={{ x: scanAreaX, y: scanAreaY, width: scanAreaWidth, height: scanAreaHeight }}
          cameraViewDimensions={{ width: CAM_VIEW_WIDTH, height: CAM_VIEW_HEIGHT }}
          androidCameraPermissionOptions={{
            title: 'Permission to use camera',
            message: 'We need your permission to use your camera',
            buttonPositive: 'Ok',
            buttonNegative: 'Cancel',
          }}
          style={{ height: CAM_VIEW_HEIGHT }}
        >
          <View style={{
            position: 'absolute',
            top: leftMargin,
            right: topMargin,
            width: frameHeight,
            height: frameWidth,
            borderWidth: 2,
            borderColor: 'red',
            opacity: 0.5
          }} />
          {/* </View> */}
        </RNCamera> : <View />
      }

    </View>
  );
};

export default App;
