// @flow

import { StyleSheet } from "react-native";


export default (variables) => {
  const platform = variables.platform;

  const cardItemTheme = {
    "NativeBase.Left": {
      "NativeBase.Body": {
        flex: 1,
        marginLeft: 10 * variables.sizeScaling,
        alignItems: null
      },
      flex: 0,
      flexDirection: "row",
      alignItems: "center"
    },
    ".content": {
      "NativeBase.Text": {
        color: platform === "ios" ? "#555" : "#222",
        fontSize: variables.DefaultFontSize - 2 * variables.sizeScaling
      }
    },
    ".cardBody": {
      padding: -5 * variables.sizeScaling,
      "NativeBase.Text": {
        marginTop: 5 * variables.sizeScaling
      }
    },
    "NativeBase.Body": {
      flex: 1,
      alignSelf: "stretch",
      alignItems: "flex-start"
    },
    "NativeBase.Right": {

      "NativeBase.Thumbnail": {
        alignSelf: null
      },
      "NativeBase.Image": {
        alignSelf: null
      },
      "NativeBase.Radio": {
        alignSelf: null
      },
      "NativeBase.Checkbox": {
        alignSelf: null
      },
      "NativeBase.Switch": {
        alignSelf: null
      },
      flex: 0
    },
    ".header": {
      "NativeBase.Text": {
        fontSize: variables.headerFontSize,
        fontWeight: platform === "ios" ? "600" : "600"
      },
      borderTopWidth: 0,
      borderBottomWidth: variables.borderWidth,
      borderColor: variables.cardBorderColor,
      backgroundColor: variables.cardHeaderBg,
      borderRadius: variables.cardBorderRadius,
      borderBottomLeftRadius: 0,
      borderBottomRightRadius: 0,
      paddingVertical: variables.cardItemPadding
    },
    ".footer": {
      "NativeBase.Text": {
        fontSize: variables.headerFontSize,
        fontWeight: platform === "ios" ? "600" : "600"
      },
      borderTopWidth: variables.borderWidth,
      borderBottomWidth: 0,
      backgroundColor: variables.cardHeaderBg,
      borderRadius: variables.cardBorderRadius,
      borderTopLeftRadius: 0,
      borderTopRightRadius: 0
    },
    ".bordered": {
      borderBottomWidth: variables.borderWidth,
      borderColor: variables.cardBorderColor
    },
    '.first': {
      borderTopLeftRadius: variables.cardBorderRadius,
      borderTopRightRadius: variables.cardBorderRadius
    },
    '.last': {
      borderBottomLeftRadius: variables.cardBorderRadius,
      borderBottomRightRadius: variables.cardBorderRadius
    },
    flexDirection: "row",
    alignItems: "center",
    padding: variables.cardItemPadding,
    paddingVertical: variables.cardItemPadding,
    //backgroundColor: variables.cardDefaultBg,

    ".noPadding": {
      paddingHorizontal: 0
    },
    ".noRadius": {
      borderRadius: 0
    },
    '.noBorder': {
      borderWidth: 0,
    },
  };

  return cardItemTheme;
};
