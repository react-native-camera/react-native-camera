// @flow


export default (variables) => {
  const thumbnailTheme = {
    '.square': {
      borderRadius: 0,
      '.small': {
        width: 36 * variables.sizeScaling,
        height: 36 * variables.sizeScaling,
        borderRadius: 0,
      },
      '.large': {
        width: 80 * variables.sizeScaling,
        height: 80 * variables.sizeScaling,
        borderRadius: 0,
      },
    },
    '.small': {
      width: 36 * variables.sizeScaling,
      height: 36 * variables.sizeScaling,
      borderRadius: 18 * variables.sizeScaling,
      '.square': {
        borderRadius: 0,
      },
    },
    '.large': {
      width: 80 * variables.sizeScaling,
      height: 80 * variables.sizeScaling,
      borderRadius: 40 * variables.sizeScaling,
      '.square': {
        borderRadius: 0,
      },
    },
    width: 56 * variables.sizeScaling,
    height: 56 * variables.sizeScaling,
    borderRadius: 28 * variables.sizeScaling,
  };

  return thumbnailTheme;
};
