// @flow



export default (variables) => {
  const cardTheme = {
    ".transparent": {
      shadowColor: null,
      shadowOffset: null,
      shadowOpacity: null,
      shadowRadius: null,
      elevation: null,
      backgroundColor: "transparent",
      borderWidth: 0
    },
    ".noShadow": {
      shadowColor: null,
      shadowOffset: null,
      shadowOpacity: null,
      elevation: null
    },
    ".noRadius": {
      borderRadius: 0
    },
    ".noRadiusTop": {
      borderTopLeftRadius: 0,
      borderTopRightRadius: 0
    },
    ".primary": {
      borderColor: variables.brandPrimary,
      //backgroundColor: variables.brandPrimary,

      "NativeBase.CardItem": {
        ".header": {
          backgroundColor: variables.brandPrimary,
          borderColor: variables.brandPrimary,
          "NativeBase.Text": {
            color: variables.inverseTextColor,
          },
        },
        ".bordered": {
          borderColor: variables.brandPrimary
        }
      }
    },
    marginVertical: 5 * variables.sizeScaling,
    borderWidth: variables.borderWidth,
    borderRadius: variables.cardBorderRadius,
    borderColor: variables.cardBorderColor,
    flexWrap: "nowrap",
    backgroundColor: variables.cardDefaultBg,
    shadowColor: "#000",
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 1.5,
    elevation: 1
  };

  return cardTheme;
};
