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

    1. make a copy of barcode-finder.js and place it in your project
    2. add it to your project
    3. add it as a child to the Camera
      <Camera>
        <MyCustomBarcodeFinder />
      </Camera>

    NOTE: The scan area is cropped and as long the first to <View> components remain intact it should show the correct size.
    <View style={[styles.container]}>
      <View style={[styles.finder, this.getSizeStyles()]}>
        { place your design here }
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
      <View style={[styles.container]}>
        <View style={[styles.finder, this.getSizeStyles()]}>
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
      </View>
    );
  }
};

BarcodeFinder.propTypes = {
  style: PropTypes.object,
  width: PropTypes.number,
  height: PropTypes.number
};

var styles = StyleSheet.create({
  container: {
    alignItems: 'center',
    justifyContent: 'center',
    position: 'absolute',
    top: 0,
    right: 0,
    bottom: 0,
    left: 0,
  },
  finder: {
    alignItems: 'center',
    justifyContent: 'center',
  },
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
