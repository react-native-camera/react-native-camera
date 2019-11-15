// @flow



export default (variables) => {
  const textTheme = {
    fontSize: variables.DefaultFontSize,
    fontFamily: variables.fontFamily,
    color: variables.textColor,
    ".note": {
      //color: "#a7a7a7",
      fontSize: variables.noteFontSize
    },
    '.medium': {
      fontSize: variables.fontSizeMedium,
    },
    '.large': {
      fontSize: variables.fontSizeLarge,
    },
    ".label": {
      color: variables.labelColor,
    },
    '.transparent': {
      color: variables.transparentColor,
    },
    '.inverse': {
      color: variables.inverseTextColor,
    },
    '.light':{
      color: variables.brandLight
    },
    '.danger':{
      color: variables.brandDanger
    },
    '.warning':{
      color: variables.brandWarning
    },
    '.dark': {
      color: variables.brandDark
    },
    '.black': {
      color: variables.brandBlack
    },
    '.success': {
      color: variables.brandSuccess
    },
    ".block": {
      width: '100%'
    },
    '.placeholder': {
      color: variables.inputColorPlaceholder,
      '.transparent': {
        color: variables.inputColorPlaceholderTransparent
      },
      '.inverse': {
        color: variables.inverseTextColor,
      }
    },
    '.disabled': {
      color: variables.buttonDisabledBg
    },
    ".underlined": {
      textDecorationLine: 'underline'
    },
    '.italic': {
      fontStyle: 'italic'
    },
    '.bold': {
      fontWeight: 'bold'
    },
    '.flex1': {
      flex: 1
    },
    '.paddingBottom': {
      paddingBottom: variables.contentPadding
    }
  };

  return textTheme;
};
