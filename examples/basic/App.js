/* eslint-disable no-console */
import React from 'react';

// react-navigation
import { createStackNavigator, createAppContainer } from 'react-navigation';

import MainScreen from './MainScreen';
import CameraScreen from './CameraScreen';
import BarcodeScannerScreen from './BarcodeScannerScreen';

const AppNavigator = createStackNavigator(
  {
    Home: MainScreen,
    Camera: CameraScreen,
    BarcodeScanner: BarcodeScannerScreen,
  },
  {
    initialRouteName: 'Home',
  },
);

const AppContainer = createAppContainer(AppNavigator);

export default class App extends React.Component {
  render() {
    return <AppContainer />;
  }
}
