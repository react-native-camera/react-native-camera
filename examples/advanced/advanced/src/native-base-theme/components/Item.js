// @flow

import { Platform } from "react-native";



export default (variables) => {
  const itemTheme = {
    ".floatingLabel": {
      "NativeBase.Input": {
        height: variables.inputHeightBase,
        top: 0,
        paddingTop: 0,
        paddingBottom: 0,
        ".multiline": {
          minHeight: variables.inputHeightBase
        }
      },
      "NativeBase.Label": {
        paddingTop: 0,
        marginBottom: 0,
      },
      "NativeBase.Icon": {
        top: 0,
        paddingTop: 0,
      },
      "NativeBase.IconNB": {
        top: 0,
        paddingTop: 0,
      }
    },
    ".fixedLabel": {
      "NativeBase.Label": {
        position: null,
        top: null,
        left: null,
        right: null,
        flex: 1,
        height: null,
        width: null,
        fontSize: variables.inputFontSize
      },
      "NativeBase.Input": {
        flex: 2,
        fontSize: variables.inputFontSize
      }
    },
    ".stackedLabel": {
      "NativeBase.Label": {
        position: null,
        top: null,
        left: null,
        right: null,
        paddingTop: 5 * variables.sizeScaling,
        alignSelf: "flex-start",
        fontSize: variables.inputFontSize - 2 * variables.sizeScaling
      },
      "NativeBase.Icon": {
        marginTop: 36 * variables.sizeScaling
      },
      "NativeBase.Input": {
        alignSelf: Platform.OS === "ios" ? "stretch" : "flex-start",
        flex: 1,
        width: Platform.OS === "ios" ? null : variables.deviceWidth - 25,
        fontSize: variables.inputFontSize,
        lineHeight: variables.inputLineHeight - 6 * variables.sizeScaling,
        ".secureTextEntry": {
          fontSize: variables.inputFontSize
        },
        ".multiline": {
          paddingTop: Platform.OS === "ios" ? (9 * variables.sizeScaling) : undefined,
          paddingBottom: Platform.OS === "ios" ? (9 * variables.sizeScaling) : undefined
        }
      },
      flexDirection: null,
      minHeight: variables.inputHeightBase + 15 * variables.sizeScaling
    },
    ".inlineLabel": {
      "NativeBase.Label": {
        position: null,
        top: null,
        left: null,
        right: null,
        paddingRight: 20 * variables.sizeScaling,
        height: null,
        width: null,
        fontSize: variables.inputFontSize
      },
      "NativeBase.Input": {
        paddingLeft: 5 * variables.sizeScaling,
        fontSize: variables.inputFontSize
      },
      flexDirection: "row"
    },
    "NativeBase.Label": {
      fontSize: variables.inputFontSize,
      color: variables.labelColor,
      paddingRight: 5 * variables.sizeScaling
    },
    "NativeBase.Icon": {
      fontSize: variables.buttonIconSize,
      paddingRight: 8 * variables.sizeScaling
    },
    "NativeBase.IconNB": {
      fontSize: variables.buttonIconSize,
      paddingRight: 8 * variables.sizeScaling
    },
    "NativeBase.Input": {
      ".multiline": {
        height: null
      },
      height: variables.inputHeightBase,
      color: variables.inputColor,
      flex: 1,
      //top: Platform.OS === "ios" ? (1.5 * variables.sizeScaling) : undefined,
      fontSize: variables.inputFontSize
    },
    ".underline": {
      "NativeBase.Input": {
        paddingLeft: 15 * variables.sizeScaling
      },
      ".success": {
        borderColor: variables.inputSuccessBorderColor
      },
      ".error": {
        borderColor: variables.inputErrorBorderColor
      },
      borderWidth: variables.borderWidth,
      borderTopWidth: 0,
      borderRightWidth: 0,
      borderLeftWidth: 0,
      borderColor: variables.inputBorderColor
    },
    ".regular": {
      "NativeBase.Input": {
        paddingLeft: 8 * variables.sizeScaling
      },
      "NativeBase.Icon": {
        paddingLeft: 10 * variables.sizeScaling
      },
      ".success": {
        borderColor: variables.inputSuccessBorderColor
      },
      ".error": {
        borderColor: variables.inputErrorBorderColor
      },
      borderWidth: variables.borderWidth,
      borderColor: variables.inputBorderColor
    },
    ".rounded": {
      "NativeBase.Input": {
        paddingLeft: 8 * variables.sizeScaling
      },
      "NativeBase.Icon": {
        paddingLeft: 10 * variables.sizeScaling
      },
      ".success": {
        borderColor: variables.inputSuccessBorderColor
      },
      ".error": {
        borderColor: variables.inputErrorBorderColor
      },
      borderWidth: variables.borderWidth,
      borderRadius: 30 * variables.sizeScaling,
      borderColor: variables.inputBorderColor
    },

    ".success": {
      "NativeBase.Icon": {
        color: variables.inputSuccessBorderColor
      },
      "NativeBase.IconNB": {
        color: variables.inputSuccessBorderColor
      },
      ".rounded": {
        borderRadius: 30 * variables.sizeScaling,
        borderColor: variables.inputSuccessBorderColor
      },
      ".regular": {
        borderColor: variables.inputSuccessBorderColor
      },
      ".underline": {
        borderWidth: variables.borderWidth,
        borderTopWidth: 0,
        borderRightWidth: 0,
        borderLeftWidth: 0,
        borderColor: variables.inputSuccessBorderColor
      },
      borderColor: variables.inputSuccessBorderColor
    },

    ".error": {
      "NativeBase.Icon": {
        color: variables.inputErrorBorderColor
      },
      "NativeBase.IconNB": {
        color: variables.inputErrorBorderColor
      },
      ".rounded": {
        borderRadius: 30 * variables.sizeScaling,
        borderColor: variables.inputErrorBorderColor
      },
      ".regular": {
        borderColor: variables.inputErrorBorderColor
      },
      ".underline": {
        borderWidth: variables.borderWidth,
        borderTopWidth: 0,
        borderRightWidth: 0,
        borderLeftWidth: 0,
        borderColor: variables.inputErrorBorderColor
      },
      borderColor: variables.inputErrorBorderColor
    },
    ".disabled": {
      "NativeBase.Icon": {
        color: "#384850"
      },
      "NativeBase.IconNB": {
        color: "#384850"
      }
    },
    ".picker": {
      marginLeft: 0
    },

    borderWidth: variables.borderWidth,
    borderTopWidth: 0,
    borderRightWidth: 0,
    borderLeftWidth: 0,
    borderColor: variables.inputBorderColor,
    backgroundColor: "transparent",
    flexDirection: "row",
    alignItems: "center",
    margin: 0,

    '.padder': {
      padding: variables.contentPadding,
    },
    '.marginBottom': {
      marginBottom: variables.contentPadding,
    }
  };

  return itemTheme;
};
