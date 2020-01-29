import React from 'react';
import {View} from 'react-native';

import {
  PinchGestureHandler,
  State,
} from 'react-native-gesture-handler';


export default class ZoomView extends React.Component {


  onGesturePinch = ({ nativeEvent }) => {
    this.props.onPinchProgress(nativeEvent.scale);
  }

  onPinchHandlerStateChange = event => {

    if (event.nativeEvent.state === State.END){
      this.props.onPinchEnd();
    }
    else if (event.nativeEvent.oldState === State.BEGAN && event.nativeEvent.state === State.ACTIVE){
      this.props.onPinchStart();
    }
  };

  render() {
    return (
      <PinchGestureHandler
        onGestureEvent={this.onGesturePinch}
        onHandlerStateChange={this.onPinchHandlerStateChange}
      >
        <View style={this.props.style}>
          {this.props.children}
        </View>
      </PinchGestureHandler>
    );
  }
}



ZoomView.defaultProps = {
  onPinchProgress: (p) => {},
  onPinchStart: () => {},
  onPinchEnd: () => {}
}
