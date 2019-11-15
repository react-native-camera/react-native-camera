import { PixelRatio, StatusBar } from "react-native";


export default (variables) => {
  const platformStyle = variables.platformStyle;
  const platform = variables.platform;

  const textStyle = {
    color: variables.toolbarBtnTextColor,
    top: 0,
    paddingHorizontal: 8 * variables.sizeScaling,
    '.transparent': {
      color: variables.toolbarBtnTextColor,
    }
  }

  const titleStyle = {
    color: variables.toolbarBtnTextColor,
    '.transparent': {
      color: variables.toolbarBtnTextColor,
    }
  }

  const transparentTextStyle = {
    color: variables.transparentColor,
    '.transparent': {
      color: variables.transparentColor
    }
  }

  const iconStyle = {
    color: variables.toolbarBtnTextColor,
    fontSize: variables.iconHeaderSize,
    marginTop: 0,
    marginHorizontal: 8 * variables.sizeScaling,
    paddingTop: 1 * variables.sizeScaling,
    '.transparent': {
      color: variables.toolbarBtnTextColor,
    }
  }

  const transparentIconStyle = {
    color: variables.transparentColor,
    '.transparent': {
      color: variables.transparentColor
    }
  }

  const buttonStyle = {
    ".transparent": {
      backgroundColor: "transparent",
      borderColor: null,
      elevation: 0,
      shadowColor: null,
      shadowOffset: null,
      shadowRadius: null,
      shadowOpacity: null,
      "NativeBase.Icon": {
        color: variables.toolbarBtnTextColor,
        '.transparent': {
          color: variables.toolbarBtnTextColor,
        }
      },
      "NativeBase.IconNB": {
        color: variables.toolbarBtnTextColor,
        '.transparent': {
          color: variables.toolbarBtnTextColor,
        }
      },
      "NativeBase.Text": {
        color: variables.toolbarBtnTextColor,
        '.transparent': {
          color: variables.toolbarBtnTextColor,
        }
      }
    },

    "NativeBase.Icon": iconStyle,
    "NativeBase.IconNB": iconStyle,
    "NativeBase.Text": textStyle,

    ".noSpacing": {
      "NativeBase.Text": {
        marginHorizontal: 0,
        paddingHorizontal: 8 * variables.sizeScaling,
      },
      "NativeBase.IconNB": {
        marginHorizontal: 0,
        paddingHorizontal: 8 * variables.sizeScaling,
      },
      "NativeBase.Icon": {
        marginHorizontal: 0,
        paddingHorizontal: 8 * variables.sizeScaling,
      },
      minWidth: 38 * variables.sizeScaling,

    },

    alignSelf: null
  }

  const transparentButton = {
    backgroundColor: "transparent",
    borderColor: null,
    elevation: 0,
    shadowColor: null,
    shadowOffset: null,
    shadowRadius: null,
    shadowOpacity: null,

    ".transparent": {
      "NativeBase.Icon": transparentIconStyle,
      "NativeBase.IconNB": transparentIconStyle,
      "NativeBase.Text": transparentTextStyle
    },

    "NativeBase.Icon": transparentIconStyle,
    "NativeBase.IconNB": transparentIconStyle,
    "NativeBase.Text": transparentTextStyle
  }




  const headerTheme = {
    ".span": {
      height: 128 * variables.sizeScaling,
      "NativeBase.Left": {
        alignSelf: "flex-start"
      },
      "NativeBase.Body": {
        alignSelf: "flex-end",
        alignItems: "flex-start",
        justifyContent: "center",
        paddingBottom: 26 * variables.sizeScaling
      },
      "NativeBase.Right": {
        alignSelf: "flex-start"
      }
    },
    ".hasSubtitle": {
      "NativeBase.Body": {
        "NativeBase.Title": {
          fontSize: variables.titleFontSize - 2 * variables.sizeScaling,
          fontFamily: variables.titleFontfamily,
          textAlign: "center",
          fontWeight: "500",
          paddingBottom: 3 * variables.sizeScaling
        },
        "NativeBase.Subtitle": {
          fontSize: variables.subTitleFontSize,
          fontFamily: variables.titleFontfamily,
          color: variables.subtitleColor,
          textAlign: "center"
        }
      }
    },
    ".noShadow": {
      elevation: 0,
      shadowColor: null,
      shadowOffset: null,
      shadowRadius: null,
      shadowOpacity: null
    },
    ".hasTabs": {
      elevation: 0,
      shadowColor: null,
      shadowOffset: null,
      shadowRadius: null,
      shadowOpacity: null,
      borderBottomWidth: null
    },
    ".hasSegment": {
      elevation: 0,
      shadowColor: null,
      shadowOffset: null,
      shadowRadius: null,
      shadowOpacity: null,
      borderBottomWidth: null,
      "NativeBase.Left": {
        flex: 0.3
      },
      "NativeBase.Right": {
        flex: 0.3
      },
      "NativeBase.Body": {
        flex: 1,
        "NativeBase.Segment": {
          marginRight: 0,
          alignSelf: "center",
          "NativeBase.Button": {
            paddingLeft: 0,
            paddingRight: 0
          }
        }
      }
    },
    ".noLeft": {
      "NativeBase.Left": {
        width: platform === "ios" ? undefined : 0,
        flex: platform === "ios" ? 1 : 0
      },
      "NativeBase.Body": {
        "NativeBase.Title": {
          paddingLeft: platform === "ios" ? undefined : (10 * variables.sizeScaling)
        },
        "NativeBase.Subtitle": {
          paddingLeft: platform === "ios" ? undefined : (10 * variables.sizeScaling)
        }
      }
    },

    ".searchBar": {
      "NativeBase.Item": {
        "NativeBase.Icon": {
          backgroundColor: "transparent",
          color: variables.dropdownLinkColor,
          fontSize: variables.toolbarSearchIconSize,
          alignItems: "center",
          marginTop: 2 * variables.sizeScaling,
          paddingRight: 10 * variables.sizeScaling,
          paddingLeft: 10 * variables.sizeScaling
        },
        "NativeBase.IconNB": {
          backgroundColor: "transparent",
          color: null,
          alignSelf: "center"
        },
        "NativeBase.Input": {
          alignSelf: "center",
          lineHeight: null,
          height: variables.searchBarInputHeight
        },
        alignSelf: "center",
        alignItems: "center",
        justifyContent: "flex-start",
        flex: 1,
        height: variables.searchBarHeight,
        borderColor: "transparent",
        backgroundColor: variables.toolbarInputColor
      },
      "NativeBase.Button": {
        ".transparent": {
          paddingLeft: platform === "ios" ? (10 * variables.sizeScaling) : null
        },
        width: platform === "ios" ? undefined : 0,
        height: platform === "ios" ? undefined : 0
      }
    },
    ".rounded": {
      "NativeBase.Item": {
        borderRadius: (platform === "ios" && platformStyle !== "material" ? 25 : 3) * variables.sizeScaling
      }
    },
    "NativeBase.Left": {
      "NativeBase.Button": buttonStyle,
      "NativeBase.Text": textStyle,
      "NativeBase.Title": titleStyle,
      "NativeBase.Icon": iconStyle,
      "NativeBase.IconNB": iconStyle,

      flex: 0,
      alignSelf: "center",
      alignItems: "flex-start",
      '.paddingRight': {
        paddingRight: variables.contentPadding
      },
      '.paddingLeft': {
        paddingLeft: variables.contentPadding
      }
    },
    "NativeBase.Body": {
      "NativeBase.Button": buttonStyle,
      "NativeBase.Text": textStyle,
      "NativeBase.Title": titleStyle,
      "NativeBase.Icon": iconStyle,
      "NativeBase.IconNB": iconStyle,
      "NativeBase.Segment": {
        borderWidth: 0,
        alignSelf: "flex-end",
        marginRight: (platform === "ios" ? -40 : -55) * variables.sizeScaling
      },

      flex: 1,
      alignItems: 'flex-start',
      alignSelf: "center",


      '.paddingRight': {
        paddingRight: variables.contentPadding
      },
      '.paddingLeft': {
        paddingLeft: variables.contentPadding
      }
    },
    "NativeBase.Right": {
      "NativeBase.Button": buttonStyle,
      "NativeBase.Text": textStyle,
      "NativeBase.Title": titleStyle,
      "NativeBase.Icon": iconStyle,
      "NativeBase.IconNB": iconStyle,

      flex: 0,
      alignSelf: "center",
      alignItems: "flex-end",
      flexDirection: "row",
      justifyContent: "flex-end",
      '.paddingRight': {
        paddingRight: variables.contentPadding
      },
      '.paddingLeft': {
        paddingLeft: variables.contentPadding
      }
    },
    backgroundColor: variables.toolbarDefaultBg,
    flexDirection: "row",
    paddingLeft: 0, //platform === "ios" && variables.platformStyle !== "material" ? 6 : 10,
    paddingRight: 0,
    justifyContent: "center",
    paddingTop: 0, // Header component will handle paddings.
    borderBottomWidth:
      platform === "ios" ? variables.borderWidth : 0,
    borderBottomColor: variables.toolbarDefaultBorder,
    height: variables.toolbarHeight,
    elevation: 3,
    shadowColor: platformStyle === "material" ? "#000" : undefined,
    shadowOffset:
      platformStyle === "material" ? { width: 0, height: 2 } : undefined,
    shadowOpacity: platformStyle === "material" ? 0.2 : undefined,
    shadowRadius: platformStyle === "material" ? 1.2 : undefined,
    top: 0,
    left: 0,
    right: 0,
    zIndex: 100,

    '.noPadding': {
      paddingLeft: 0,
      paddingRight: 0
    },

    ".transparent": {
      backgroundColor: "rgba(0,0,0,0.4)",
      borderBottomColor: "transparent",
      elevation: 0,
      shadowColor: null,
      shadowOffset: null,
      shadowRadius: null,
      shadowOpacity: null,

      "NativeBase.Button": transparentButton,
      "NativeBase.Text": transparentTextStyle,
      "NativeBase.Title": transparentTextStyle,
      "NativeBase.Icon": transparentIconStyle,
      "NativeBase.IconNB": transparentIconStyle,

      "NativeBase.Left": {
        "NativeBase.Button": transparentButton,
        "NativeBase.Text": transparentTextStyle,
        "NativeBase.Title": transparentTextStyle,
        "NativeBase.Icon": transparentIconStyle,
        "NativeBase.IconNB": transparentIconStyle,
      },
      "NativeBase.Body": {
        "NativeBase.Button": transparentButton,
        "NativeBase.Text": transparentTextStyle,
        "NativeBase.Title": transparentTextStyle,
        "NativeBase.Icon": transparentIconStyle,
        "NativeBase.IconNB": transparentIconStyle,
      },
      "NativeBase.Right": {
        "NativeBase.Button": transparentButton,
        "NativeBase.Text": transparentTextStyle,
        "NativeBase.Title": transparentTextStyle,
        "NativeBase.Icon": transparentIconStyle,
        "NativeBase.IconNB": transparentIconStyle,
      },
    }
  };

  return headerTheme;
};
