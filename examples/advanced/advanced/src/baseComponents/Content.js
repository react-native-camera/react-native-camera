import React from "react";
import {connectStyle} from "native-base";
import {SafeAreaView, ScrollView} from 'react-native';
import KeyboardShiftView from './KeyboardShiftView';
import conf from 'src/conf';

// Replacement for NB's <Content> Component
// We will assign the same styles, but replace the component
// to use our own keyboard aware component since the one used by native-base is quite outdated

const containerStyle = { flex: 1 };
const contentPadding = conf.theme.variables.contentPadding;


// Copied from native-base source
class Content extends React.Component{

  constructor(props) {
    super(props);
    this.state = {
    };
  }

  render(){
    let {style, padder, contentContainerStyle, ...others} = this.props;
    return (
      <SafeAreaView style={containerStyle}>
        <KeyboardShiftView
          Component={ScrollView}
          style={style}
          contentContainerStyle={[
            { padding: padder ? contentPadding : undefined },
            contentContainerStyle
          ]}
          {...others}
        />
      </SafeAreaView>
    )
  }
}

const StyledContent = connectStyle('baseComponents.Content')(Content);

export { StyledContent as Content };