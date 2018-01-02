import React, {
  Component,
} from 'react';
import PropTypes from 'prop-types';
import {
  Platform,
  StyleSheet,
  View,
} from 'react-native';

/**
    HOW TO MAKE CUSTOM BARCODE FINDER

    1. make a copy of src/BarcodeFinder.js and place it in your project
    2. add it to your project
    3. add it as a child to the Camera
      <Camera barcodeFinderComponent={<MyCustomBarcodeFinder />} />
**/

class BarcodeFinder extends Component {

  constructor(props) {
    super(props);
  }

  getSizeStyles() {
    return ({
      width: this.props.width,
      height: this.props.height
    });
  }

  render() {
    return (
      <View style={{flex:1}}>
          <View style={[
            {borderColor: this.props.style.borderColor},
            styles.topLeftEdge,
            {
              borderLeftWidth: this.props.style.borderWidth,
              borderTopWidth: this.props.style.borderWidth,
            }
          ]} />
          <View style={[
            {borderColor: this.props.style.borderColor},
            styles.topRightEdge,
            {
              borderRightWidth: this.props.style.borderWidth,
              borderTopWidth: this.props.style.borderWidth,
            }
          ]} />
          <View style={[
            {borderColor: this.props.style.borderColor},
            styles.bottomLeftEdge,
            {
              borderLeftWidth: this.props.style.borderWidth,
              borderBottomWidth: this.props.style.borderWidth,
            }
          ]} />
          <View style={[
            {borderColor: this.props.style.borderColor},
            styles.bottomRightEdge,
            {
              borderRightWidth: this.props.style.borderWidth,
              borderBottomWidth: this.props.style.borderWidth,
            }
          ]} />
      </View>
    );
  }
};


var styles = StyleSheet.create({
  topLeftEdge: {
    position: 'absolute',
    top: 0,
    left: 0,
    width: 40,
    height: 20,
  },
  topRightEdge: {
    position: 'absolute',
    top: 0,
    right: 0,
    width: 40,
    height: 20,
  },
  bottomLeftEdge: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    width: 40,
    height: 20,
  },
  bottomRightEdge: {
    position: 'absolute',
    bottom: 0,
    right: 0,
    width: 40,
    height: 20,
  },
});

module.exports = BarcodeFinder;
