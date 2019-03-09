/* eslint-disable no-console */
import React from 'react';
import { StyleSheet, Text, View, TouchableOpacity, Slider } from 'react-native';

export default class MainScreen extends React.Component {
  static navigationOptions = {
    title: 'React Native Camera',
  };

  _navigateToCameraScreen() {
    this.props.navigation.navigate('Camera');
  }

  _navigateToBarcodeScannerScreen() {
    this.props.navigation.navigate('BarcodeScanner');
  }

  render() {
    return (
      <View style={styles.container}>
        <TouchableOpacity style={styles.button} onPress={this._navigateToCameraScreen.bind(this)}>
          <Text style={styles.text}> Camera </Text>
        </TouchableOpacity>
        <TouchableOpacity
          style={styles.button}
          onPress={this._navigateToBarcodeScannerScreen.bind(this)}
        >
          <Text style={styles.text}> Barcode Scanner </Text>
        </TouchableOpacity>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    paddingLeft: 20,
    paddingRight: 20,
    paddingBottom: 20,
    backgroundColor: '#fff',
    flexDirection: 'column',
  },
  button: {
    flexDirection: 'row',
    height: 60,
    marginTop: 20,
    borderRadius: 8,
    padding: 5,
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'gray',
  },
  text: {
    color: 'white',
    fontSize: 15,
  },
});
