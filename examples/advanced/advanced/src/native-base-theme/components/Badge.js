// @flow



export default (variables) => {
  const badgeTheme = {
    ".primary": {
      backgroundColor: variables.buttonPrimaryBg
    },
    ".warning": {
      backgroundColor: variables.buttonWarningBg
    },
    ".info": {
      backgroundColor: variables.buttonInfoBg
    },
    ".success": {
      backgroundColor: variables.buttonSuccessBg
    },
    ".danger": {
      backgroundColor: variables.buttonDangerBg
    },
    "NativeBase.Text": {
      color: variables.badgeColor,
      fontSize: variables.fontSizeBase,
      lineHeight: variables.lineHeight - 1 * variables.sizeScaling,
      textAlign: "center",
      paddingHorizontal: 3 * variables.sizeScaling
    },
    '.small': {
      paddingHorizontal: 3 * variables.sizeScaling,
      height: 20 * variables.sizeScaling,
      "NativeBase.Text": {
        fontSize: variables.noteFontSize
      }
    },
    '.transparent': {
      backgroundColor: 'transparent',
      borderRadius: 0,
      "NativeBase.Text": {
        color: '#000'
      }
    },
    backgroundColor: variables.badgeBg,
    padding: variables.badgePadding,
    paddingHorizontal: 6 * variables.sizeScaling,
    alignSelf: "flex-end",
    justifyContent: "center", //variables.platform === "ios" ? "center" : undefined,
    borderRadius: 13.5 * variables.sizeScaling,
    height: 27 * variables.sizeScaling
  };
  return badgeTheme;
};
