// @flow



export default (variables) => {
  const platform = variables.platform;

  const segmentTheme = {
    height: variables.buttonHeight,
    borderColor: variables.segmentBorderColorMain,
    flexDirection: "row",
    justifyContent: "center",
    backgroundColor: variables.segmentBackgroundColor,
    "NativeBase.Button": {
      alignSelf: "center",
      borderRadius: 0,
      paddingTop: 3 * variables.sizeScaling,
      paddingBottom: 3 * variables.sizeScaling,
      height: 30 * variables.sizeScaling,
      backgroundColor: "transparent",
      borderWidth: 1,
      borderLeftWidth: 0,
      borderColor: variables.segmentBorderColor,
      elevation: 0,
      ".active": {
        backgroundColor: variables.segmentActiveBackgroundColor,
        "NativeBase.Text": {
          color: variables.segmentActiveTextColor
        },
        "NativeBase.Icon": {
          color: variables.segmentActiveTextColor
        }
      },
      ".first": {
        borderTopLeftRadius: platform === "ios" ? 5 : undefined,
        borderBottomLeftRadius: platform === "ios" ? 5 : undefined,
        borderLeftWidth: 1
      },
      ".last": {
        borderTopRightRadius: platform === "ios" ? 5 : undefined,
        borderBottomRightRadius: platform === "ios" ? 5 : undefined
      },
      "NativeBase.Text": {
        color: variables.segmentTextColor,
        fontSize: variables.fontSizeSmall
      },
      "NativeBase.Icon": {
        fontSize: 22 * variables.sizeScaling,
        paddingTop: 0,
        color: variables.segmentTextColor
      }
    }
  };

  return segmentTheme;
};
