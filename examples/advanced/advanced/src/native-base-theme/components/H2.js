// @flow



export default (variables) => {
  const h2Theme = {
    color: variables.textColor,
    fontSize: variables.fontSizeH2,
    lineHeight: variables.lineHeightH2,
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

  return h2Theme;
};
