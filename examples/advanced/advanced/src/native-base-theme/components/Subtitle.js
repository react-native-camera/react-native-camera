// @flow

import { Platform } from "react-native";



export default (variables) => {
  const subtitleTheme = {
    fontSize: variables.subTitleFontSize,
    fontFamily: variables.titleFontfamily,
    color: variables.subtitleColor,
    textAlign: "center",
    paddingLeft: (Platform.OS === "ios" ? 4 : 0) * variables.sizeScaling,
    marginLeft: Platform.OS === "ios" ? undefined : (-3 * variables.sizeScaling)
  };

  return subtitleTheme;
};
