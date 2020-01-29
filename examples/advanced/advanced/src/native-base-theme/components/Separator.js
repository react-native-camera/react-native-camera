// @flow


export default (variables) => {
  const theme = {
    '.group': {
      height: 50 * variables.sizeScaling,
      paddingVertical: variables.listItemPadding - 8 * variables.sizeScaling,
      paddingTop: variables.listItemPadding + 12 * variables.sizeScaling,
      '.bordered': {
        height: 50 * variables.sizeScaling,
        paddingVertical: variables.listItemPadding - 8 * variables.sizeScaling,
        paddingTop: variables.listItemPadding + 12 * variables.sizeScaling,
      },
    },
    '.bordered': {
      '.noTopBorder': {
        borderTopWidth: 0,
      },
      '.noBottomBorder': {
        borderBottomWidth: 0,
      },
      height: 40 * variables.sizeScaling,
      paddingTop: variables.listItemPadding + 2 * variables.sizeScaling,
      paddingBottom: variables.listItemPadding,
      borderBottomWidth: variables.borderWidth,
      borderTopWidth: variables.borderWidth,
      borderColor: variables.listBorderColor,
    },
    'NativeBase.Text': {
      fontSize: variables.tabBarTextSize,
      color: '#777',
    },
    '.noTopBorder': {
      borderTopWidth: 0,
    },
    '.noBottomBorder': {
      borderBottomWidth: 0,
    },
    height: 42 * variables.sizeScaling,
    backgroundColor: '#F0EFF5',
    flex: 1,
    justifyContent: 'center',
    paddingLeft: variables.listItemPadding + 5 * variables.sizeScaling,
  };

  return theme;
};
