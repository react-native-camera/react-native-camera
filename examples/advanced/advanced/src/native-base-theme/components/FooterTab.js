// @flow

import { Platform } from "react-native";



export default (variables) => {
  const platform = variables.platform;

  const nativeBaseButton = {
    ".active": {
      "NativeBase.Text": {
        color: variables.tabBarActiveTextColor,
        fontSize: variables.tabBarTextSize,
        lineHeight: 16 * variables.sizeScaling
      },
      "NativeBase.Icon": {
        color: variables.tabBarActiveTextColor
      },
      "NativeBase.IconNB": {
        color: variables.tabBarActiveTextColor
      },
      backgroundColor: variables.tabActiveBgColor
    },
    ".transparent": {
      "NativeBase.Text": {
        color: variables.tabBarTextColor,
      },
      "NativeBase.H1": {
        color: variables.tabBarTextColor,
      },
      "NativeBase.H2": {
        color: variables.tabBarTextColor,
      },
      "NativeBase.H3": {
        color: variables.tabBarTextColor,
      },
      "NativeBase.Icon": {
        color: variables.tabBarTextColor
      },
      "NativeBase.IconNB": {
        color: variables.tabBarTextColor
      }
    },
    flexDirection: null,
    backgroundColor: "transparent",
    borderColor: null,
    elevation: 0,
    shadowColor: null,
    shadowOffset: null,
    shadowRadius: null,
    shadowOpacity: null,
    alignSelf: "center",
    flex: 1,
    height: variables.footerHeight - 5 * variables.sizeScaling,
    justifyContent: "center",
    ".badge": {
      "NativeBase.Badge": {
        "NativeBase.Text": {
          fontSize: 11 * variables.sizeScaling,
          fontWeight: platform === "ios" ? "600" : "600",
          lineHeight: 14 * variables.sizeScaling
        },
        top: -3,
        alignSelf: "center",
        left: 10,
        zIndex: 99,
        height: 18 * variables.sizeScaling,
        padding: 1.7 * variables.sizeScaling,
        paddingHorizontal: 3 * variables.sizeScaling
      },
      "NativeBase.Icon": {
        marginTop: -18 * variables.sizeScaling
      }
    },
    "NativeBase.Icon": {
      color: variables.tabBarTextColor
    },
    "NativeBase.IconNB": {
      color: variables.tabBarTextColor
    },
    "NativeBase.Text": {
      color: variables.tabBarTextColor,
      fontSize: variables.tabBarTextSize,
      lineHeight: 16 * variables.sizeScaling
    }
  }

  const footerTabTheme = {
    "NativeBase.Button": nativeBaseButton,
    backgroundColor: Platform.OS === "android"
      ? variables.footerDefaultBg
      : undefined,
    flexDirection: "row",
    justifyContent: "space-between",
    flex: 1,
    alignSelf: "stretch",

    '.landscape': {
      flexDirection: 'column',
      marginTop: variables.platform === 'ios' ? 15 : 0, // do not scale this one

      // gotta override these too
      "NativeBase.Button": Object.assign({}, nativeBaseButton, {
        width: '100%',
        margin: 0,
      })
    }
  };

  return footerTabTheme;
};
