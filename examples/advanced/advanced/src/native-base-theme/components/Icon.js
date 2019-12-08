// @flow



export default (variables) => {
  const iconTheme = {
    fontSize: variables.iconFontSize,
    color: variables.textColor,

    '.transparent': {
      color: variables.transparentColor
    },
    '.inverse': {
      color: variables.inverseTextColor,
    },
    '.dark': {
      color: variables.brandDark
    },
    '.black': {
      color: variables.brandBlack
    },
    '.light':{
      color: variables.brandLight
    },
    '.danger':{
      color: variables.brandDanger
    },
    '.success':{
      color: variables.brandSuccess
    },
    ".warning": {
      color: variables.brandWarning
    },
    '.brandInfo': {
      color: variables.brandInfo
    },
    '.noMargin':{
      marginHorizontal: 0,
    },
    '.noMarginLeft':{
      marginLeft: 0
    },
    '.noMarginRight':{
      marginRight: 0
    },
    '.normal': {
      fontSize: variables.iconFontSize
    },
    '.small': {
      fontSize: variables.iconFontSize - 10 * variables.sizeScaling,
    },
    '.large': {
      fontSize: variables.iconFontSize + 5 * variables.sizeScaling,
    }
  };

  return iconTheme;
};
