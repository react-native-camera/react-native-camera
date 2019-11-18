import conf from 'src/conf';
import React from 'react';
import {
  StatusBar,
  Text as RNText
} from 'react-native';

import {StyleProvider, Root, Input} from 'native-base';
import {createAppContainer} from "react-navigation";
import { createStackNavigator } from 'react-navigation-stack';

import {routesMap} from 'src/routes';
import MainHeader from 'src/baseComponents/MainHeader';



// Disable text font scaling, it is a mess with native-base
RNText.defaultProps = RNText.defaultProps || {};
RNText.defaultProps.allowFontScaling = false;
RNText.defaultProps.fontSize = conf.theme.variables.DefaultFontSize;
Input.defaultProps = Input.defaultProps || {};
Input.defaultProps.allowFontScaling = false;
Input.defaultProps.fontSize = conf.theme.variables.DefaultFontSize;



// Main stack navigator, used for authenticated routes only.
// Login won't use navigation. If we want further non authenticated screens
// we will need a separate router for it.
const StackNavigator = createStackNavigator (
  routesMap,
  {
    initialRouteName: "Home",
    transitionConfig: () => ({
      transitionSpec: {
        isInteraction: true,
        useNativeDriver: true
      }
    }),

    headerMode: 'screen',

    defaultNavigationOptions: ({ navigation }) => {
      return {
        header: props => {
          return <MainHeader back={true} title={props.scene.descriptor.options.title} navigation={navigation}/>;
        },
        gesturesEnabled: false
      }
    }
  }
)

const NavigatorApp = createAppContainer(StackNavigator);


const App = () => {
  return (
    <StyleProvider style={conf.theme}>
      <Root>
        <StatusBar
          barStyle="light-content"
          backgroundColor={conf.theme.variables.statusBarColor}
        />
        <NavigatorApp />
      </Root>
    </StyleProvider>
  )
}



export default App;
