import React, {Component} from 'react';
import {
  Platform,
  StyleSheet,
  Dimensions,
  View,
  Alert,
  SafeAreaView,
  AppState,
  TouchableOpacity
} from 'react-native';
import Slider from '@react-native-community/slider';
import _ from 'underscore';
import { Container, Button, Text, Icon, Footer, FooterTab, Spinner, H2, connectStyle, Toast } from 'native-base';
import { RNCamera } from 'react-native-camera';
import {NavigationEvents} from 'react-navigation';

import conf from 'src/conf';
import {getOrientation} from 'src/baseComponents/orientation';
import KeyboardShiftView from 'src/baseComponents/KeyboardShiftView';
import ZoomView from 'src/baseComponents/ZoomView';
import {runAfterInteractions} from 'src/baseComponents/utils';
import MainHeader from 'src/baseComponents/MainHeader';


const IS_IOS = Platform.OS == 'ios';
const touchCoordsSize = 100 * conf.theme.variables.sizeScaling;
const flashIcons = {
  'on': <Icon transparent name='flash' type='MaterialCommunityIcons'></Icon>,
  'auto': <Icon transparent name='flash-auto' type='MaterialCommunityIcons'></Icon>,
  'off': <Icon transparent name='flash-off' type='MaterialCommunityIcons'></Icon>,
  'torch': <Icon transparent name='flashlight' type='MaterialCommunityIcons'></Icon>,
}
const MAX_ZOOM = 8; // iOS only
const ZOOM_F = IS_IOS ? 0.01 : 0.1;
const BACK_TYPE = RNCamera.Constants.Type.back;
const FRONT_TYPE = RNCamera.Constants.Type.front;

const WB_OPTIONS = [
  RNCamera.Constants.WhiteBalance.auto,
  RNCamera.Constants.WhiteBalance.sunny,
  RNCamera.Constants.WhiteBalance.cloudy,
  RNCamera.Constants.WhiteBalance.shadow,
  RNCamera.Constants.WhiteBalance.incandescent,
  RNCamera.Constants.WhiteBalance.fluorescent
];

const WB_OPTIONS_MAP = {
  0: 'WB',
  1: "SU",
  2: "CL",
  3: "SH",
  4: "IN",
  5: "FL",
  6: "CW"
}

const CUSTOM_WB_OPTIONS_MAP = {
  temperature:      {label: "Temp.", min: 1000, max: 10000, steps: 500},
  tint:             {label: "Tint", min: -20, max: 20, steps: 0.5},
  redGainOffset:    {label: "Red", min: -1.0, max: 1.0, steps: 0.05},
  greenGainOffset:  {label: "Green", min: -1.0, max: 1.0, steps: 0.05},
  blueGainOffset:   {label: "Blue", min: -1.0, max: 1.0, steps: 0.05},
};

const getCameraType = (type) => {

  if(type == 'AVCaptureDeviceTypeBuiltInTelephotoCamera'){
    return 'zoomed';
  }
  if(type == 'AVCaptureDeviceTypeBuiltInUltraWideCamera'){
    return 'wide';
  }

  return 'normal';
}


const flex1 = {flex: 1};

const styles = StyleSheet.create({
  content: {flex: 1},
  actionStyles: {
    position: 'absolute',
    bottom: 0,
    width: '100%',
    backgroundColor: 'transparent'
  },
  capturingStyle: {
    position: 'absolute',
    bottom: 0,
    width: '100%',
    backgroundColor: 'rgba(0,0,0,0.4)',
    padding: conf.theme.variables.contentPadding,
  },
  cameraLoading: {flex: 1, alignSelf: 'center'},
  cameraNotAuthorized: {
    padding: 20 * conf.theme.variables.sizeScaling,
    paddingTop: 35 * conf.theme.variables.sizeScaling
  },

  cameraButton: {
    flex: 1
  },

  buttonsView: {
    flex: 1,
    backgroundColor: 'black',
    width: '100%',
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center'
  },

  cameraSelectionRow: {
    flexDirection: 'row',
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center'
  },

  ratioButton: {
    width: 100 * conf.theme.variables.sizeScaling
  },

  customWBView: {
    backgroundColor: '#00000080',
    flex: 1,
    width: '100%',
    height: 50,
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 8,
  },

  customWBViewButton: {
    backgroundColor: 'transparent',
    alignSelf: 'center',
    width: '25%',
  },

  customWBViewText: {
    color: 'white',
  },

  customWBViewSlider: {
    flex: 2,
    marginRight: 6,
  },
})


const cameraNotAuthorized = <Text transparent style={styles.cameraNotAuthorized}>Camera access was not granted. Please go to your phone's settings and allow camera access.</Text>;

const defaultCameraOptions = {
  flashMode: 'off', // on, auto, off, torch
  wb: 0,
  zoom: 0, // 0-1
  focusCoords: undefined,
  currentCustomWBOption: "temperature",
  customWhiteBalance: {
    temperature: 6000,
    tint: 0.0,
    redGainOffset: 0.0,
    greenGainOffset: 0.0,
    blueGainOffset: 0.0,
  },
};

function parseRatio(str){
  let [p1, p2] = str.split(":");
  p1 = parseInt(p1);
  p2 = parseInt(p2);
  return p1 / p2;
}

class CameraSelectorButton extends React.PureComponent{

  onChange = () => {
    if(!this.props.isSelected){
      this.props.onChange(this.props.camera.id);
    }
  }


  render(){
    let {camera, isSelected} = this.props;
    let cameraType = camera.cameraType;
    let IconComp;

    if(camera.type == BACK_TYPE){
      if(cameraType == 'wide'){
        IconComp = (props) => <Icon {...props} name='zoom-out' type='Feather'/>;

      }
      else if(cameraType == 'zoomed'){
        IconComp = (props) => <Icon {...props} name='zoom-in' type='Feather'/>;
      }
      else{
        IconComp = (props) => <Icon {...props} name='camera-rear' type='MaterialIcons'/>;
      }
    }
    else if(camera.type == FRONT_TYPE){
      IconComp = (props) => <Icon {...props} name='camera-front' type='MaterialIcons'/>;
    }
    // should never happen
    else{
      IconComp = (props) => <Icon {...props} normal name='ios-reverse-camera' type='Ionicons'/>;
    }


    return (
      <Button
        transparent
        rounded
        onPress={this.onChange}
        selfCenter
      >
        <IconComp transparent={!isSelected} warning={isSelected} />
      </Button>
    )
  }
}

class CameraSelector extends React.PureComponent{

  loopCamera = () => {
    let {cameraId, cameraIds, onChange} = this.props;

    if(cameraId && cameraIds.length){
      let newIdx = (cameraIds.findIndex(i => i.id == cameraId) + 1) % cameraIds.length;
      onChange(cameraIds[newIdx].id);

    }
    else{
      // if no available camera ids, always call with empty id
      onChange('');
    }
  }


  render(){
    let {cameraId, cameraIds} = this.props;

    if(!cameraIds){return null;}

    // camera ID is empty, means we have no info about the camera.
    // fallback to regular switch
    if(!cameraId){
      return (
        <Button
          transparent
          onPress={this.loopCamera}
          selfCenter
        >
          <Icon transparent normal name='ios-reverse-camera' type='Ionicons'></Icon>
        </Button>
      )
    }

    // 0 or 1 cameras, no button
    if(cameraIds.length <= 1){
      return null;
    }

    // 2 cameras, 1 button, no set default option
    if(cameraIds.length == 2){
      return (
        <Button
          transparent
          onPress={this.loopCamera}
          selfCenter
        >
          <Icon transparent normal name='ios-reverse-camera' type='Ionicons'></Icon>
        </Button>
      )
    }

    // 3 or more cameras, multiple buttons
    return (
      <React.Fragment>
        {cameraIds.map((v, i) => {
          return (
            <CameraSelectorButton
              key={`${i}`}
              camera={v}
              isSelected={cameraId == v.id}
              onChange={this.props.onChange}
            />
          )
        })}
      </React.Fragment>
    )
  }
}


class Camera extends Component{

  constructor(props) {
    super(props);
    this.state = {
      ...defaultCameraOptions,
      orientation: getOrientation(),
      takingPic: false,
      recording: false,
      audioDisabled: false,
      elapsed: 0,
      cameraReady: false,
      cameraIds: null, // null means not checked, empty list means no results
      cameraType: BACK_TYPE,
      cameraId: '',
      aspectRatioStr: "4:3",
      aspectRatio: parseRatio("4:3")
    };

    this._prevPinch = 1;

  }


  componentDidMount(){
    this.mounted = true;
    AppState.addEventListener('change', this.handleAppStateChange);
    Dimensions.addEventListener('change', this.adjustOrientation);
  }

  componentWillUnmount(){
    this.mounted = false;
    AppState.removeEventListener('change', this.handleAppStateChange);
    this.stopVideo();
  }

  adjustOrientation = () => {
    setTimeout(()=>{
      if(this.mounted){
        this.setState({orientation: getOrientation()});
      }
    }, 50);
  }

  // audio permission will be android only
  onCameraStatusChange = (s) => {
    if(s.cameraStatus == 'READY'){

      let audioDisabled = s.recordAudioPermissionStatus == 'NOT_AUTHORIZED';
      this.setState({audioDisabled: audioDisabled}, async () => {

        let ids = [];

        // dummy for simulator test
        // uncomment above and below
        // let ids = [
        //   {id: '1', type: BACK_TYPE, deviceType: 'AVCaptureDeviceTypeBuiltInWideAngleCamera'},
        //   {id: '2', type: BACK_TYPE, deviceType: 'AVCaptureDeviceTypeBuiltInTelephotoCamera'},
        //   {id: '3', type: BACK_TYPE, deviceType: 'AVCaptureDeviceTypeBuiltInUltraWideCamera'},
        //   {id: '4', type: FRONT_TYPE, deviceType: 'AVCaptureDeviceTypeBuiltInWideAngleCamera'},
        // ]

        let cameraId = '';

        try{
          ids = await this.camera.getCameraIdsAsync();

          // map deviceType to our types
          ids = ids.map(d => {
            d.cameraType = getCameraType(d.deviceType);
            return d;
          });

          if(ids.length){

            // select the first back camera found
            cameraId = ids[0].id;

            for(let c of ids){

              if(c.type == BACK_TYPE){
                cameraId = c.id;
                break;
              }
            }
          }
        }
        catch(err){
          console.error("Failed to get camera ids", err.message || err);
        }

        // sort ids so front cameras are first
        ids = _.sortBy(ids, v => v.type == FRONT_TYPE ? 0 : 1);

        this.setState({cameraIds: ids, cameraId: cameraId});

      });
    }
    else{
      if(this.state.cameraReady){
        this.setState({cameraReady: false});
      }
    }
  }

  onCameraReady = () => {
    if(!this.state.cameraReady){
      this.setState({cameraReady: true});
    }
  }

  onCameraMountError = () => {
    setTimeout(()=>{
      Alert.alert("Error", "Camera start failed.");
    }, 150);
  }


  handleAppStateChange = (nextAppState) => {
  }


  onDidFocus = () => {
    this.focused = true;

  }

  onDidBlur = async () => {
    this.focused = false;
    this.stopVideo();
  }


  onPinchProgress = (p) => {
    let p2 = p - this._prevPinch;

    if(p2 > 0 && p2 > ZOOM_F){
      this._prevPinch = p;
      this.setState({zoom: Math.min(this.state.zoom + ZOOM_F, 1)})
    }
    else if (p2 < 0 && p2 < -ZOOM_F){
      this._prevPinch = p;
      this.setState({zoom: Math.max(this.state.zoom - ZOOM_F, 0)})
    }
  }

  onTapToFocus = (touchOrigin) => {

    if(!this.cameraStyle || this.state.takingPic){
      return;
    }

    const {x, y} = touchOrigin;
    let {width, height, top, left} = this.cameraStyle;

    // compensate for top/left changes
    let pageX2 = x - left;
    let pageY2 = y - top;

    // normalize coords as described by https://gist.github.com/Craigtut/6632a9ac7cfff55e74fb561862bc4edb
    const x0 = pageX2 / width;
    const y0 = pageY2 / height;

    let computedX = x0;
    let computedY = y0;

    // if portrait, need to apply a transform because RNCamera always measures coords in landscape mode
    // with the home button on the right. If the phone is rotated with the home button to the left
    // we will have issues here, and we have no way to detect that orientation!
    // TODO: Fix this, however, that orientation should never be used due to camera positon
    if(this.state.orientation.isPortrait){
      computedX = y0;
      computedY = -x0 + 1;
    }

    this.setState({
      focusCoords: {
        x: computedX,
        y: computedY,
        autoExposure: true
      },
      touchCoords: {
        x: pageX2 - 50,
        y: pageY2 - 50
      }
    },this.onSetFocus);

    // remove focus rectangle
    if(this.focusTimeout){
      clearTimeout(this.focusTimeout);
      this.focusTimeout = null;
    }

  }

  onSetFocus = () => {
      this.focusTimeout = setTimeout(() => {
        if (this.mounted) {
          this.setState({touchCoords: null});
        }
      }, 1500);
  }

  onPinchStart = () => {
    this._prevPinch = 1;

  }

  onPinchEnd = () => {
    this._prevPinch = 1;
  }

  onAudioInterrupted = () => {
    this.setState({audioDisabled: true});
  }

  onAudioConnected = () => {
    this.setState({audioDisabled: false});
  }

  onPictureTaken = () => {
    this.setState({takingPic: false});
  }

  onRecordingStart = () => {
    this.reportRequestPrompt = true;

    if(this._recordingTimer){
      clearInterval(this._recordingTimer);
      this._recordingTimer = null;
    }

    if(this.state.recording){
      this.setState({elapsed: 0})
      this._recordingTimer = setInterval(()=>{
        this.setState({elapsed: this.state.elapsed + 1})
      }, 1000);
    }
  }

  onRecordingEnd = () => {
    this.reportRequestPrompt = true;

    if(this._recordingTimer){
      clearInterval(this._recordingTimer);
      this._recordingTimer = null;
    }
  }

  goBack = () => {
    this.props.navigation.goBack();
  }

  render() {

    let {orientation, takingPic, cameraReady, recording, audioDisabled, zoom, wb, cameraType, cameraId, cameraIds, flashMode, elapsed} = this.state;
    let {style} = this.props;

    let isPortrait = orientation.isPortrait;

    let disable = takingPic || !cameraReady;
    let disableOrRecording = disable || recording;

    // flag to decide how to layout camera buttons
    let cameraCount = 0;

    // we have queried the list of cameras
    if(cameraIds != null){
      if(!cameraId){
        cameraCount = 2; // no camera id info, assume 2 cameras to switch from back and front
      }
      else{
        cameraCount = cameraIds.length;
      }
    }


    let buttons = (
      <React.Fragment>

        <Button
          transparent
          rounded
          onPress={this.takePicture}
          disabled={disableOrRecording}
          style={styles.cameraButton}
        >
          <Icon name={disableOrRecording ? 'camera-off' :'camera'} type='MaterialCommunityIcons'></Icon>
        </Button>

        {recording ?
          <Button
            transparent
            rounded
            onPress={this.stopVideo}
            danger
          >
            <Icon name='video-slash' type='FontAwesome5'></Icon>
          </Button>
          :
          <Button
            transparent
            rounded
            onPress={this.startVideo}
            disabled={disable}
          >
            <Icon name='video' type='FontAwesome5'></Icon>
          </Button>
        }



      </React.Fragment>);


    let cameraStyle;

    // style to cover all the screen exactly
    // leaving footer and extra heights
    let mainViewStyle = {
      flex: 1,
      width: isPortrait ? orientation.width : orientation.width - style.footerWidth,
      height: orientation.height - (style.footerHeight * isPortrait) - orientation.minusHeight - orientation.insetBottom
    }

    if(isPortrait){
      let height = orientation.width * this.state.aspectRatio;
      cameraStyle = {
        position: 'absolute',
        top: Math.max(0, (mainViewStyle.height - height) / 2),
        left: 0,
        width: orientation.width,
        height: height
      }
    }
    else{
      let height = orientation.height - orientation.minusHeight;
      let width = height * this.state.aspectRatio;

      cameraStyle = {
        position: 'absolute',
        top: 0,
        left: Math.max(0, (mainViewStyle.width - width) / 2),
        width: width,
        height: height
      }
    }

    this.cameraStyle = cameraStyle;

    let isCustomWhiteBalance = wb >= WB_OPTIONS.length;
    let whiteBalance = isCustomWhiteBalance ? this.state.customWhiteBalance : WB_OPTIONS[wb];
    const { currentCustomWBOption } = this.state;
    let customWhiteBalanceValue = this.state.customWhiteBalance[currentCustomWBOption];
    let customWhiteBalanceOption = CUSTOM_WB_OPTIONS_MAP[currentCustomWBOption]

    return (

      <Container fullBlack>

        <KeyboardShiftView style={styles.content} keyboardShouldPersistTaps={'never'} extraHeight={0} bounces={false}>
          <NavigationEvents
            onDidFocus={this.onDidFocus}
            onDidBlur={this.onDidBlur}
          />

          <View style={mainViewStyle}>
            <MainHeader
              transparent
              back={true}
              title={"Camera"}
              navigation={this.props.navigation}
            />

            <RNCamera
              ref={ref => {
                this.camera = ref;
              }}
              style={cameraStyle}
              type={cameraType}
              cameraId={cameraId}
              //useCamera2Api={true}
              onAudioInterrupted={this.onAudioInterrupted}
              onAudioConnected={this.onAudioConnected}
              onPictureTaken={this.onPictureTaken}
              onRecordingStart={this.onRecordingStart}
              onRecordingEnd={this.onRecordingEnd}
              ratio={this.state.aspectRatioStr}
              flashMode={flashMode}
              zoom={zoom}
              maxZoom={MAX_ZOOM}
              useNativeZoom={true}
              onTap={this.onTapToFocus}
              whiteBalance={whiteBalance}
              autoFocusPointOfInterest={this.state.focusCoords}
              androidCameraPermissionOptions={{
                title: 'Permission to use camera',
                message: 'We need your permission to use your camera',
                buttonPositive: 'Ok',
                buttonNegative: 'Cancel',
              }}
              androidRecordAudioPermissionOptions={{
                title: 'Permission to use audio recording',
                message: 'We need your permission to use your audio',
                buttonPositive: 'Ok',
                buttonNegative: 'Cancel',
              }}
              onStatusChange={this.onCameraStatusChange}
              onCameraReady={this.onCameraReady}
              onMountError={this.onCameraMountError}
              pendingAuthorizationView={
                <SafeAreaView style={styles.cameraLoading}>
                  <Spinner color={style.brandLight}/>
                </SafeAreaView>
              }
              notAuthorizedView={
                <View>
                  {cameraNotAuthorized}
                </View>
              }
            >
                  {this.state.touchCoords ?
                    <View style={{
                      borderWidth: 2,
                      borderColor: takingPic ? 'red' : 'gray',
                      position: 'absolute',
                      top: this.state.touchCoords.y,
                      left: this.state.touchCoords.x,
                      width: touchCoordsSize,
                      height: touchCoordsSize
                    }}>
                    </View>
                  : null}
            </RNCamera>

            {!takingPic && !recording && !this.state.spinnerVisible && cameraReady ?
              <SafeAreaView
                style={styles.actionStyles}
              >
                <React.Fragment>
                  {cameraCount > 2 ?
                    <View style={styles.cameraSelectionRow}>
                      <Button
                        transparent
                        onPress={this.changeWB}
                        selfCenter
                      >
                        <Text transparent>{WB_OPTIONS_MAP[wb]}</Text>
                      </Button>
                      <CameraSelector
                        cameraId={cameraId}
                        cameraIds={this.state.cameraIds}
                        onChange={this.onCameraChange}
                      />
                    </View>
                  : null}
                  {isCustomWhiteBalance && (
                    <View style={styles.customWBView}>
                      <Button style={styles.customWBViewButton} onPress={this.changeCustomWBOption}>
                        <Text style={styles.customWBViewText}>
                          {customWhiteBalanceOption.label}
                        </Text>
                      </Button>
                      <Slider
                        style={styles.customWBViewSlider}
                        value={customWhiteBalanceValue}
                        step={customWhiteBalanceOption.steps}
                        minimumValue={customWhiteBalanceOption.min}
                        maximumValue={customWhiteBalanceOption.max}
                        minimumTrackTintColor="#FFFFFF"
                        maximumTrackTintColor="#000000"
                        onValueChange={this.changeCustomWBOptionValue}
                      />
                      <Text style={[styles.customWBViewText, {minWidth: '15%'}]}>
                        {customWhiteBalanceValue.toFixed(1)}
                      </Text>
                    </View>
                  )}
                  <View style={styles.buttonsView}>
                    <Button
                      transparent
                      onPress={this.resetZoom}
                      selfCenter
                    >
                      <Text transparent>{`${(zoom * 100).toFixed(0)}%`}</Text>
                    </Button>

                    {cameraCount <= 2 ?
                      <Button
                        transparent
                        onPress={this.changeWB}
                        selfCenter
                      >
                        <Text transparent>{WB_OPTIONS_MAP[wb]}</Text>
                      </Button>
                    : null}

                    <Button
                      transparent
                      onPress={this.toggleRatio}
                      style={styles.ratioButton}
                      selfCenter
                    >
                      <Text transparent>{this.state.aspectRatioStr}</Text>
                    </Button>

                    <Button
                      transparent
                      onPress={this.toggleFlash}
                      selfCenter
                    >
                      {flashIcons[flashMode]}
                    </Button>

                    {(cameraCount > 1 && cameraCount <= 2) ?
                      <CameraSelector
                        cameraId={cameraId}
                        cameraIds={this.state.cameraIds}
                        onChange={this.onCameraChange}
                      />
                    : null}
                  </View>
                </React.Fragment>

              </SafeAreaView>
            : null }

            {(takingPic || recording)?
              <View
                style={styles.capturingStyle}
              >
              {takingPic ? <H2 transparent>Capturing Picture...</H2> : <H2 transparent>{`Capturing Video${audioDisabled ? ' (muted)' : ''}... (${elapsed != -1 ? elapsed : "Preparing Camera..."})`}</H2>}
              </View>
            : null}

          </View>
        </KeyboardShiftView>


        {isPortrait ?
          <Footer>
            <FooterTab>
              {buttons}
            </FooterTab>
          </Footer>
        :
        <Footer landscape>
          <FooterTab landscape>
            {buttons}
          </FooterTab>
        </Footer>
        }
      </Container>
    );
  }

  takePicture = async () => {
    if (this.camera) {

      if(this.state.takingPic || this.state.recording || !this.state.cameraReady){
        return;
      }
      // if we have a non original quality, skip processing and compression.
      // we will use JPEG compression on resize.
      let options = {
          quality: 0.85,
          fixOrientation: true,
          forceUpOrientation: true,
          writeExif: true
      };

      this.setState({takingPic: true});

      let data = null;

      try{
        data = await this.camera.takePictureAsync(options);
      }
      catch(err){
        Alert.alert("Error", "Failed to take picture: " + (err.message || err));
        return;
      }

      Alert.alert("Picture Taken!", JSON.stringify(data, null, 2));

    }
  }

  startVideo = async () => {
    if (this.camera && !this.state.recording) {

      // need to do this in order to avoid race conditions
      this.state.recording = true;

      const options = {
        quality: '480p',
        maxDuration: 60,
        maxFileSize: 100 * 1024 * 1024
       };


      this.setState({recording: true, elapsed: -1}, async () => {

        let result = null;
        try {
          result = await this.camera.recordAsync(options);
        }
        catch(err){
          console.warn("VIDEO RECORD FAIL", err.message, err);
          Alert.alert("Error", "Failed to store recorded video: " + err.message);
        }


        if(result){
          Alert.alert("Video recorded!", JSON.stringify(result));
        }

        // give time for the camera to recover
        setTimeout(()=>{
          this.setState({recording: false});
        }, 500);

        // might be cleared on recording stop or
        // here if we had errors
        if(this._recordingTimer){
          clearInterval(this._recordingTimer);
          this._recordingTimer = null;
        }

      });

    }
  }

  stopVideo = () => {
    if(this.camera && this.state.recording){
      this.camera.stopRecording();
    }
  }

  toggleFlash = () => {
    if (this.state.flashMode === 'torch') {
      this.setState({ flashMode: 'off' });
    } else if (this.state.flashMode === 'off') {
      this.setState({ flashMode: 'auto' });
    } else if (this.state.flashMode === 'auto') {
      this.setState({ flashMode: 'on' });
    } else if (this.state.flashMode === 'on') {
      this.setState({ flashMode: 'torch' });
    }
  }


  changeWB = () => {
    // The custom white balance feature is only available on iOS (#2774)
    const numberOfOptions = IS_IOS ? Object.keys(WB_OPTIONS_MAP).length : WB_OPTIONS.length;
    this.setState({
      wb: (this.state.wb + 1) % numberOfOptions
    });
  }

  changeCustomWBOption = () => {
    const optionKeys = Object.keys(CUSTOM_WB_OPTIONS_MAP);
    let currentOptionIndex = optionKeys.indexOf(this.state.currentCustomWBOption);
    let nextOptionIndex = (currentOptionIndex + 1) % optionKeys.length;
    this.setState({
      currentCustomWBOption: optionKeys[nextOptionIndex]
    });
  }

  changeCustomWBOptionValue = (value) => {
    this.setState((state) => ({
      customWhiteBalance: {
        ...state.customWhiteBalance,
        [state.currentCustomWBOption]: value,
      },
    }));
  }

  toggleRatio = () => {
    if(this.state.aspectRatioStr == "4:3"){
      this.setState({
        aspectRatioStr: "1:1",
        aspectRatio: parseRatio("1:1")
      });
    }
    else if(this.state.aspectRatioStr == "1:1"){
      this.setState({
        aspectRatioStr: "16:9",
        aspectRatio: parseRatio("16:9")
      });
    }
    else{
      this.setState({
        aspectRatioStr: "4:3",
        aspectRatio: parseRatio("4:3")
      });
    }
  }

  onCameraChange = (cameraId) => {

    this.setState({cameraReady: false}, () => {
      runAfterInteractions(() => {

        // cameraId will be empty if we failed to get a camera by ID or
        // our id list is empty. Fallback to back/front setting

        if(!cameraId){
          let cameraType = this.state.cameraType;
          if(cameraType == FRONT_TYPE){
            this.setState({cameraType: BACK_TYPE, cameraId: '', ...defaultCameraOptions});
          }
          else{
            this.setState({cameraType: FRONT_TYPE, cameraId: '', ...defaultCameraOptions});
          }
        }
        else{
          this.setState({cameraId: cameraId, ...defaultCameraOptions});
        }
      });
    });
  }


  resetZoom = () => {
    this._prevPinch = 1;
    this.setState({zoom: 0});
  }
}

Camera.navigationOptions = ({ navigation }) => {
  return {
    header: props => null
  }
}



Camera = connectStyle("Branding")(Camera);


export default Camera;
