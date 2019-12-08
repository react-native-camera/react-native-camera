// @flow



export default (variables) => {
  const labelTheme = {
    fontSize: variables.DefaultFontSize,
    fontFamily: variables.fontFamily,
    color: variables.labelColor,
    ".focused": {
      width: 0
    },
    ".block": {
      width: '100%'
    },
    '.transparent': {
      color: variables.transparentColor
    },
    '.inverse': {
      color: variables.inverseTextColor,
    },
    '.light':{
      color: variables.brandLight
    },
    '.float': {
      fontSize: variables.DefaultFontSize,
      fontFamily: variables.fontFamily,
      color: variables.labelColor,
    }
  };

  return labelTheme;
};
