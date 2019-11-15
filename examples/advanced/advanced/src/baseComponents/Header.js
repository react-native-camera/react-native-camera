import React from "react";
import {connectStyle} from "native-base";
import {View, Dimensions} from 'react-native';

import conf from 'src/conf';


let variables = conf.theme.variables;


// We won't use NativeBase Header component since it interferes with Android's status bar
// So we will just create a View component that acts as header
// and set those styles in the native-base config the same as Header
// Copied from native-base source

class Header extends React.PureComponent{

  constructor(props) {
    super(props);

    let {width, height} = Dimensions.get('window');
    this.state = {
      isPortrait: width <= height
    };
  }


  calculateHeight() {
    let inset = variables.Inset;

    const { style } = this.props;

    const InsetValues = this.state.isPortrait ? inset.portrait : inset.landscape;
    let oldHeight = null;

    if (style.height != undefined) {
      oldHeight = style.height;
    } else if (style[1]) {
      oldHeight = style[1].height ? style[1].height : style[0].height;
    } else {
      oldHeight = style[0].height;
    }

    let height = oldHeight + InsetValues.topInset;

    return height;
  }

  calculatePadder() {
    let inset = variables.Inset;
    const InsetValues = this.state.isPortrait ? inset.portrait : inset.landscape;
    return InsetValues.topInset;
  }

  onLayout = (event) => {
    let {width, height} = Dimensions.get('window');
    let isPortrait = width <= height;
    if(isPortrait != this.state.isPortrait){
      this.setState({isPortrait: isPortrait});
    }
    if(this.props.onLayout){
      this.props.onLayout(event);
    }

  }

  render(){
    let {children, style, ...others} = this.props;

    return(
      <View
        {...others}
        onLayout={this.onLayout}
        style={[style, {
          height: this.calculateHeight(),
          paddingTop: this.calculatePadder()
        }]}
      >
        {children}
      </View>
    )

  }
}

Header = connectStyle('baseComponents.Header')(Header);

export {Header};
