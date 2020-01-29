// @flow

import { Platform } from "react-native";



export default (variables) => {
  const titleTheme = {
    fontSize: variables.titleFontSize,
    fontFamily: variables.titleFontfamily,
    fontWeight: "600", //Platform.OS === "ios" ? "700" : undefined,
    textAlign: "center",
    paddingLeft: 0,//Platform.OS === "ios" ? 4 : 0,
    paddingTop: 0,

    '.transparent': {
      color: variables.transparentColor,
    },
    '.inverse': {
      color: variables.inverseTextColor,
    },

    '.padder': {
      marginHorizontal: 0,
      paddingHorizontal: 8 * variables.sizeScaling
    }
  };

  return titleTheme;
};
