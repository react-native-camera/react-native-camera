// @flow



export default (variables) => {
  const inputGroupTheme = {
    "NativeBase.Icon": {
      fontSize: variables.buttonIconSize,
      color: variables.sTabBarActiveTextColor,
      paddingHorizontal: 5 * variables.sizeScaling
    },
    "NativeBase.IconNB": {
      fontSize: variables.buttonIconSize,
      color: variables.sTabBarActiveTextColor,
      paddingHorizontal: 5 * variables.sizeScaling
    },
    "NativeBase.Input": {
      height: variables.inputHeightBase,
      color: variables.inputColor,
      paddingLeft: 5 * variables.sizeScaling,
      paddingRight: 5 * variables.sizeScaling,
      flex: 1,
      fontSize: variables.inputFontSize,
      lineHeight: variables.inputLineHeight
    },
    ".underline": {
      ".success": {
        borderColor: variables.inputSuccessBorderColor
      },
      ".error": {
        borderColor: variables.inputErrorBorderColor
      },
      paddingLeft: 5 * variables.sizeScaling,
      borderWidth: variables.borderWidth,
      borderTopWidth: 0,
      borderRightWidth: 0,
      borderLeftWidth: 0,
      borderColor: variables.inputBorderColor
    },
    ".regular": {
      ".success": {
        borderColor: variables.inputSuccessBorderColor
      },
      ".error": {
        borderColor: variables.inputErrorBorderColor
      },
      paddingLeft: 5 * variables.sizeScaling,
      borderWidth: variables.borderWidth,
      borderColor: variables.inputBorderColor
    },
    ".rounded": {
      ".success": {
        borderColor: variables.inputSuccessBorderColor
      },
      ".error": {
        borderColor: variables.inputErrorBorderColor
      },
      paddingLeft: 5 * variables.sizeScaling,
      borderWidth: variables.borderWidth,
      borderRadius: variables.inputGroupRoundedBorderRadius,
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

    paddingLeft: 5 * variables.sizeScaling,
    borderWidth: variables.borderWidth,
    borderTopWidth: 0,
    borderRightWidth: 0,
    borderLeftWidth: 0,
    borderColor: variables.inputBorderColor,
    backgroundColor: "transparent",
    flexDirection: "row",
    alignItems: "center"
  };

  return inputGroupTheme;
};
