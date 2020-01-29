// @flow



export default (variables) => {
  const textAreaTheme = {
    ".underline": {
      borderBottomWidth: variables.borderWidth,
      marginTop: 5 * variables.sizeScaling,
      borderColor: variables.inputBorderColor
    },
    ".bordered": {
      borderWidth: 1,
      marginTop: 5 * variables.sizeScaling,
      borderColor: variables.inputBorderColor
    },
    '.transparent': {
      color: variables.transparentColor
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
    '.success':{
      color: variables.brandSuccess
    },
    ".block": {
      width: '100%'
    },
    color: variables.textColor,
    paddingLeft: 10 * variables.sizeScaling,
    paddingRight: 5 * variables.sizeScaling,
    fontSize: variables.DefaultFontSize,
    textAlignVertical: "top"
  };

  return textAreaTheme;
};
