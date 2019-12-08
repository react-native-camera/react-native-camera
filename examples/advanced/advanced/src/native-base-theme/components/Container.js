// @flow

import { Platform, Dimensions } from "react-native";


const deviceHeight = Dimensions.get("window").height;

export default (variables) => {
  const theme = {
    flex: 1,
    //height: Platform.OS === "ios" ? deviceHeight : deviceHeight - 120,
    backgroundColor: variables.containerBgColor,
    '.toolbarColor': {
      backgroundColor: variables.toolbarDefaultBg
    },
    '.transparent': {
      backgroundColor: 'transparent'
    },
    '.semiTransparent': {
      backgroundColor: 'rgba(0,0,0, 0.9)'
    },
    '.fullBlack': {
      backgroundColor: 'black'
    }
  };

  return theme;
};
