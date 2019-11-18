// @flow



export default (variables) => {
  const platform = variables.platform;

  const tabHeadingTheme = {
    flexDirection: "row",
    backgroundColor: variables.tabDefaultBg,
    flex: 1,
    alignItems: "center",
    justifyContent: "center",
    ".scrollable": {
      paddingHorizontal: 20 * variables.sizeScaling,
      flex: platform === "android" ? 0 : 1,
      minWidth: platform === "android" ? undefined : (60 * variables.sizeScaling)
    },
    "NativeBase.Text": {
      color: variables.topTabBarTextColor,
      marginHorizontal: 7 * variables.sizeScaling
    },
    "NativeBase.Icon": {
      color: variables.topTabBarTextColor,
      fontSize: platform === "ios" ? (26 * variables.sizeScaling) : undefined
    },
    ".active": {
      "NativeBase.Text": {
        color: variables.topTabBarActiveTextColor,
        fontWeight: "600"
      },
      "NativeBase.Icon": {
        color: variables.topTabBarActiveTextColor
      }
    }
  };

  return tabHeadingTheme;
};
