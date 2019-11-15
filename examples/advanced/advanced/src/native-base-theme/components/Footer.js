// @flow



export default (variables) => {
  const platformStyle = variables.platformStyle;
  const platform = variables.platform;

  const iconCommon = {
    "NativeBase.Icon": {
      color: variables.tabBarActiveTextColor
    }
  };
  const iconNBCommon = {
    "NativeBase.IconNB": {
      color: variables.tabBarActiveTextColor
    }
  };
  const textCommon = {
    "NativeBase.Text": {
      color: variables.tabBarActiveTextColor
    }
  };
  const footerTheme = {
    "NativeBase.Left": {
      "NativeBase.Button": {
        ".transparent": {
          backgroundColor: "transparent",
          borderColor: null,
          elevation: 0,
          shadowColor: null,
          shadowOffset: null,
          shadowRadius: null,
          shadowOpacity: null,
          ...iconCommon,
          ...iconNBCommon,
          ...textCommon
        },
        alignSelf: null,
        ...iconCommon,
        ...iconNBCommon,
        // ...textCommon
      },
      flex: 1,
      alignSelf: "center",
      alignItems: "flex-start"
    },
    "NativeBase.Body": {
      flex: 1,
      alignItems: "center",
      alignSelf: "center",
      flexDirection: "row",
      "NativeBase.Button": {
        alignSelf: "center",
        ".transparent": {
          backgroundColor: "transparent",
          borderColor: null,
          elevation: 0,
          shadowColor: null,
          shadowOffset: null,
          shadowRadius: null,
          shadowOpacity: null,
          ...iconCommon,
          ...iconNBCommon,
          ...textCommon
        },
        ".full": {
          height: variables.footerHeight,
          paddingBottom: variables.footerPaddingBottom,
          flex: 1
        },
        ...iconCommon,
        ...iconNBCommon,
        // ...textCommon
      }
    },
    "NativeBase.Right": {
      "NativeBase.Button": {
        ".transparent": {
          backgroundColor: "transparent",
          borderColor: null,
          elevation: 0,
          shadowColor: null,
          shadowOffset: null,
          shadowRadius: null,
          shadowOpacity: null,
          ...iconCommon,
          ...iconNBCommon,
          ...textCommon
        },
        alignSelf: null,
        ...iconCommon,
        ...iconNBCommon,
        // ...textCommon
      },
      flex: 1,
      alignSelf: "center",
      alignItems: "flex-end"
    },
    backgroundColor: variables.footerDefaultBg,
    flexDirection: "row",
    justifyContent: "center",
    borderTopWidth: undefined,
      // platform === "ios" && platformStyle !== "material"
      //   ? variables.borderWidth
      //   : undefined,
    borderColor: undefined,
      // platform === "ios" && platformStyle !== "material"
      //   ? "#cbcbcb"
      //   : undefined,
    height: variables.footerHeight,
    paddingBottom: variables.footerPaddingBottom,
    elevation: 3,
    left: 0,
    right: 0,

    '.landscape': {
      position: 'absolute',
      top: 0,
      // right: !variables.isIphoneX ? 0 : null,
      // left: !variables.isIphoneX ? null : 0,
      right: 0,
      left: null,
      height: !variables.isIphoneX ? '100%' : Math.min(variables.deviceWidth, variables.deviceHeight),
      width: variables.footerWidth,
    }
  };
  return footerTheme;
};
