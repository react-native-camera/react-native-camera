'use strict';

// react
import React, { Component } from 'react';

import { StyleSheet, Dimensions, View, Text, Vibration } from 'react-native';
import { RNCamera } from 'react-native-camera';

export default class BarcodeScannerScreen extends React.Component {
  static navigationOptions = {
    title: 'Barcode Scanner',
  };

  state = {
    barcode: '',
  };

  constructor(props) {
    super(props);

    let { width } = Dimensions.get('window');
    this.maskLength = (width * 75) / 100;
  }

  _onBarCodeRead = event => {
    console.log(JSON.stringify(event));
    Vibration.vibrate(250);
    this.setState({ barcode: event.data });
  };

  _onGoogleVisionBarCodeRead = event => {
    console.log(JSON.stringify(event));
    const barcodes = event.barcodes;
    for (let i = 0; i < barcodes.length; i++) {
      const barcode = barcodes[i].data;
      if (this.state.barcode !== barcode) {
        Vibration.vibrate(250);
        this.setState({ barcode: barcode });
        break;
      }
    }
  };

  render() {
    return (
      <View style={styles.container}>
        <RNCamera
          ref={ref => {
            this.camera = ref;
          }}
          style={styles.preview}
          type={RNCamera.Constants.Type.back}
          autoFocus={RNCamera.Constants.AutoFocus.on}
          defaultTouchToFocus
          mirrorImage={false}
          captureAudio={false}
          // onBarCodeRead={this._onBarCodeRead}
          googleVisionBarcodeDetectorEnabled={true}
          onGoogleVisionBarcodesDetected={this._onGoogleVisionBarCodeRead}
          permissionDialogTitle={'Permission to use camera'}
          permissionDialogMessage={'Allow this app to access camera in order to scan barcode.'}
        >
          <View style={styles.overlay} />
          <View style={[styles.overlayContentRow, { height: this.maskLength / 2 }]}>
            <View style={styles.overlay} />
            <View
              style={[
                styles.overlayContent,
                { width: this.maskLength, height: this.maskLength / 2 },
              ]}
            />
            <View style={styles.overlay} />
          </View>
          <View style={styles.overlay} />
        </RNCamera>
        <View style={styles.bottomContainer}>
          <Text style={styles.text}>{this.state.barcode}</Text>
        </View>
      </View>
    );
  }
}
const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: 'white',
    flexDirection: 'column',
  },
  preview: {
    flex: 1,
    justifyContent: 'flex-end',
    alignItems: 'center',
  },
  overlay: {
    flex: 1,
  },
  overlayContentRow: {
    flexDirection: 'row',
  },
  overlayContent: {
    borderWidth: 3,
    borderColor: 'red',
    alignItems: 'center',
  },
  bottomContainer: {
    flex: 0,
    height: 80,
    justifyContent: 'center',
  },
  text: {
    alignSelf: 'stretch',
    textAlign: 'center',
    padding: 10,
    color: 'gray',
    backgroundColor: 'white',
  },
});
