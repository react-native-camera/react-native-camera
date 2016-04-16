'use strict';
import React, {
  AppRegistry,
  View,
  Text,
  TouchableOpacity,
  Image,
  Component,
  Dimensions,
  StyleSheet} from 'react-native';
import Camera from 'react-native-camera';

class AwesomeProject extends Component {

  constructor(props) {
    super(props);
    this.state = {
      image: null
    };
  }


  render() {
    let {width, height} = Dimensions.get('window');
    let cameraSize = Math.min(width, height) - 64;

    return (
      <View style={styles.container}>

        <Camera ref="camera"
                aspect={Camera.constants.Aspect.Fill}
                style={styles.camera}/>

        <TouchableOpacity onPress={this.capture.bind(this)}>
          <Text style={styles.capture}>[CAPTURE]</Text>
        </TouchableOpacity>

        <TouchableOpacity onPress={this.record.bind(this)}>
          <Text style={styles.capture}>[START RECORD]</Text>
        </TouchableOpacity>

        <TouchableOpacity onPress={this.stoprecord.bind(this)}>
          <Text style={styles.capture}>[STOP RECORD]</Text>
        </TouchableOpacity>

        <View style={styles.imageContainer}>
          <Image ref="image"
                 source={{uri: this.state.image}}
                 style={styles.image}/>
        </View>
      </View>
    );
  }
  capture() {
    this.refs.camera.capture({
        target: Camera.constants.CaptureTarget.disk
      })
      .then(data => {
        console.log(data);
        this.setState({...this.state, image: data});
      })
      .catch(console.log)
  }  

  record() {
    this.refs.camera.capture({
        mode: Camera.constants.CaptureMode.video,
        target: Camera.constants.CaptureTarget.disk
      })
      .then(data => {
        console.log(data);
        this.setState({...this.state, image: data});
      })
      .catch(console.log)
  }

  stoprecord() {
    this.refs.camera.stopCapture({
      })
      .then(data => {
        console.log(data);
      })
      .catch(console.log)
  }
}  


const styles = StyleSheet.create({
  container: {
    flex: 1,
    margin: 32
  },
  camera: {
    flex: 1
  },
  capture: {
    alignSelf: 'center',
    marginVertical: 16
  },
  imageContainer: {
    flex: 1,
    borderColor: 'black',
    borderWidth: 1
  },
  image: {
    flex: 1
  }
});

AppRegistry.registerComponent('AwesomeProject', () => AwesomeProject);
