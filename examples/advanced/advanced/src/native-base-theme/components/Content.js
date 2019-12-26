// @flow



export default (variables) => {
  const contentTheme = {
    flex: 1,
    backgroundColor: 'transparent',
    '.colored': {
      backgroundColor: variables.containerBgColor,
    },
    '.semiTransparent': {
      backgroundColor: 'rgba(0,0,0, 0.9)'
    },
    "NativeBase.Segment": {
      borderWidth: 0,
      backgroundColor: "transparent"
    }
  };

  return contentTheme;
};
