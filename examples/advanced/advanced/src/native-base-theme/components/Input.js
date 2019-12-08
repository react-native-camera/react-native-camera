// @flow


export default (variables) => {
  const inputTheme = {
    '.multiline': {
      height: null,
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
    ".block": {
      width: '100%'
    },
    '.italic': {
      fontStyle: 'italic'
    },
    height: variables.inputHeightBase,
    color: variables.inputColor,
    paddingLeft: 0, //5,
    paddingRight: 0, //5
    flex: 1,
    fontSize: variables.inputFontSize
  };

  return inputTheme;
};
