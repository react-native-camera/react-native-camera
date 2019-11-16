// @flow



export default (variables) => {
  const h1Theme = {
    color: variables.textColor,
    fontSize: variables.fontSizeH1,
    lineHeight: variables.lineHeightH1,
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
    ".block": {
      width: '100%'
    },
  };

  return h1Theme;
};
