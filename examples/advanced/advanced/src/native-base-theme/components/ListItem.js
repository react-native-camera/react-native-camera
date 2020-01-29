// @flow

import { Platform, PixelRatio } from "react-native";

import pickerTheme from "./Picker";


export default (variables) => {
  const platform = variables.platform;
  const selectedStyle = {
    "NativeBase.Text": {
      color: variables.listItemSelected
    },
    "NativeBase.Icon": {
      color: variables.listItemSelected
    }
  };

  const listItemTheme = {
    "NativeBase.InputGroup": {
      "NativeBase.Icon": {
        paddingRight: 5 * variables.sizeScaling
      },
      "NativeBase.IconNB": {
        paddingRight: 5 * variables.sizeScaling
      },
      "NativeBase.Input": {
        paddingHorizontal: 5 * variables.sizeScaling
      },
      flex: 1,
      borderWidth: null,
      margin: -10 * variables.sizeScaling,
      borderBottomColor: "transparent"
    },
    ".searchBar": {
      "NativeBase.Item": {
        "NativeBase.Icon": {
          backgroundColor: "transparent",
          color: variables.dropdownLinkColor,
          fontSize:
            platform === "ios"
              ? variables.iconFontSize - 10 * variables.sizeScaling
              : variables.iconFontSize - 5 * variables.sizeScaling,
          alignItems: "center",
          marginTop: 2 * variables.sizeScaling,
          paddingRight: 8 * variables.sizeScaling
        },
        "NativeBase.IconNB": {
          backgroundColor: "transparent",
          color: null,
          alignSelf: "center"
        },
        "NativeBase.Input": {
          alignSelf: "center"
        },
        alignSelf: "center",
        alignItems: "center",
        justifyContent: "flex-start",
        flex: 1,
        height: (platform === "ios" ? 30 : 40) * variables.sizeScaling,
        borderColor: "transparent",
        backgroundColor: "#fff",
        borderRadius: 5 * variables.sizeScaling
      },
      "NativeBase.Button": {
        ".transparent": {
          "NativeBase.Text": {
            fontWeight: "500"
          },
          paddingHorizontal: null,
          paddingLeft: platform === "ios" ? (10 * variables.sizeScaling) : null
        },
        paddingHorizontal: platform === "ios" ? undefined : null,
        width: platform === "ios" ? undefined : 0,
        height: platform === "ios" ? undefined : 0
      },
      backgroundColor: variables.toolbarInputColor,
      padding: 10 * variables.sizeScaling,
      marginLeft: null
    },
    "NativeBase.CheckBox": {
      marginLeft: -10 * variables.sizeScaling,
      marginRight: 10 * variables.sizeScaling
    },
    ".first": {
      ".itemHeader": {
        paddingTop: variables.listItemPadding + 3 * variables.sizeScaling
      }
    },
    ".itemHeader": {
      ".first": {
        paddingTop: variables.listItemPadding + 3 * variables.sizeScaling
      },
      borderBottomWidth: platform === "ios" ? variables.borderWidth : null,
      marginLeft: null,
      padding: variables.listItemPadding,
      paddingLeft: variables.listItemPadding + 5 * variables.sizeScaling,
      paddingTop:
        platform === "ios" ? (variables.listItemPadding + 25 * variables.sizeScaling) : undefined,
      paddingBottom:
        platform === "android" ? (variables.listItemPadding + 20 * variables.sizeScaling): undefined,
      flexDirection: "row",
      borderColor: variables.listBorderColor,
      "NativeBase.Text": {
        fontSize: variables.fontSizeSmall,
        color: platform === "ios" ? undefined : variables.listNoteColor
      }
    },
    ".itemDivider": {
      borderBottomWidth: null,
      marginLeft: null,
      padding: variables.listItemPadding,
      paddingLeft: variables.listItemPadding + 5 * variables.sizeScaling,
      backgroundColor: variables.listDividerBg,
      flexDirection: "row",
      borderColor: variables.listBorderColor
    },
    ".selected": {
      "NativeBase.Left": {
        ...selectedStyle
      },
      "NativeBase.Body": {
        ...selectedStyle
      },
      "NativeBase.Right": {
        ...selectedStyle
      },
      ...selectedStyle
    },
    "NativeBase.Left": {
      "NativeBase.Body": {
        "NativeBase.Text": {
          ".note": {
            color: variables.listNoteColor,
            fontWeight: "200"
          },
          fontWeight: "600"
        },
        marginLeft: 10 * variables.sizeScaling,
        alignItems: null,
        alignSelf: null
      },
      "NativeBase.Icon": {
        width: variables.iconFontSize - 10 * variables.sizeScaling,
        fontSize: variables.iconFontSize - 10 * variables.sizeScaling
      },
      "NativeBase.IconNB": {
        width: variables.iconFontSize - 10 * variables.sizeScaling,
        fontSize: variables.iconFontSize - 10 * variables.sizeScaling
      },
      "NativeBase.Text": {
        alignSelf: "center"
      },
      flexDirection: "row"
    },
    "NativeBase.Body": {
      "NativeBase.Text": {
        marginHorizontal: variables.listItemPadding,
        ".note": {
          color: variables.listNoteColor,
          fontWeight: "200"
        }
      },
      alignSelf: null,
      alignItems: null
    },
    "NativeBase.Right": {
      "NativeBase.Badge": {
        alignSelf: null
      },
      "NativeBase.PickerNB": {
        "NativeBase.Button": {
          marginRight: -15 * variables.sizeScaling,
          "NativeBase.Text": {
            color: variables.topTabBarActiveTextColor
          }
        }
      },
      "NativeBase.Button": {
        alignSelf: null,
        ".transparent": {
          "NativeBase.Text": {
            color: variables.topTabBarActiveTextColor
          }
        }
      },
      "NativeBase.Icon": {
        alignSelf: null,
        fontSize: variables.iconFontSize - 8 * variables.sizeScaling,
        color: "#c9c8cd"
      },
      "NativeBase.IconNB": {
        alignSelf: null,
        fontSize: variables.iconFontSize - 8 * variables.sizeScaling,
        color: "#c9c8cd"
      },
      "NativeBase.Text": {
        ".note": {
          color: variables.listNoteColor,
          fontWeight: "200"
        },
        alignSelf: null
      },
      "NativeBase.Thumbnail": {
        alignSelf: null
      },
      "NativeBase.Image": {
        alignSelf: null
      },
      "NativeBase.Radio": {
        alignSelf: null
      },
      "NativeBase.Checkbox": {
        alignSelf: null
      },
      "NativeBase.Switch": {
        alignSelf: null
      },
      padding: null,
      flex: 0.28
    },
    "NativeBase.Text": {
      ".note": {
        color: variables.listNoteColor,
        fontWeight: "200"
      },
      alignSelf: "center"
    },
    ".last": {
      marginLeft: -(variables.listItemPadding + 5 * variables.sizeScaling),
      paddingLeft: (variables.listItemPadding + 5 * variables.sizeScaling) * 2,
      top: 1
    },
    ".avatar": {
      "NativeBase.Left": {
        flex: 0,
        alignSelf: "flex-start",
        paddingTop: 14 * variables.sizeScaling
      },
      "NativeBase.Body": {
        "NativeBase.Text": {
          marginLeft: null
        },
        flex: 1,
        paddingVertical: variables.listItemPadding,
        borderBottomWidth: variables.borderWidth,
        borderColor: variables.listBorderColor,
        marginLeft: variables.listItemPadding + 5 * variables.sizeScaling
      },
      "NativeBase.Right": {
        "NativeBase.Text": {
          ".note": {
            fontSize: variables.noteFontSize - 2
          }
        },
        flex: 0,
        paddingRight: variables.listItemPadding + 5 * variables.sizeScaling,
        alignSelf: "stretch",
        paddingVertical: variables.listItemPadding,
        borderBottomWidth: variables.borderWidth,
        borderColor: variables.listBorderColor
      },
      ".noBorder": {
        "NativeBase.Body": {
          borderBottomWidth: null
        },
        "NativeBase.Right": {
          borderBottomWidth: null
        }
      },
      borderBottomWidth: null,
      paddingVertical: null,
      paddingRight: null
    },
    ".thumbnail": {
      "NativeBase.Left": {
        flex: 0
      },
      "NativeBase.Body": {
        "NativeBase.Text": {
          marginLeft: null
        },
        flex: 1,
        paddingVertical: variables.listItemPadding + 8 * variables.sizeScaling,
        borderBottomWidth: variables.borderWidth,
        borderColor: variables.listBorderColor,
        marginLeft: variables.listItemPadding + 5 * variables.sizeScaling
      },
      "NativeBase.Right": {
        "NativeBase.Button": {
          ".transparent": {
            "NativeBase.Text": {
              fontSize: variables.listNoteSize,
              color: variables.sTabBarActiveTextColor
            }
          },
          height: null
        },
        flex: 0,
        justifyContent: "center",
        alignSelf: "stretch",
        paddingRight: variables.listItemPadding + 5 * variables.sizeScaling,
        paddingVertical: variables.listItemPadding + 5 * variables.sizeScaling,
        borderBottomWidth: variables.borderWidth,
        borderColor: variables.listBorderColor
      },
      ".noBorder": {
        "NativeBase.Body": {
          borderBottomWidth: null
        },
        "NativeBase.Right": {
          borderBottomWidth: null
        }
      },
      borderBottomWidth: null,
      paddingVertical: null,
      paddingRight: null
    },
    ".icon": {
      ".last": {
        "NativeBase.Body": {
          borderBottomWidth: null
        },
        "NativeBase.Right": {
          borderBottomWidth: null
        },
        borderBottomWidth: variables.borderWidth,
        borderColor: variables.listBorderColor
      },
      "NativeBase.Left": {
        "NativeBase.Button": {
          "NativeBase.IconNB": {
            marginHorizontal: null,
            fontSize: variables.iconFontSize - 5 * variables.sizeScaling
          },
          "NativeBase.Icon": {
            marginHorizontal: null,
            fontSize: variables.iconFontSize - 8 * variables.sizeScaling
          },
          alignSelf: "center",
          height: 29 * variables.sizeScaling,
          width: 29 * variables.sizeScaling,
          borderRadius: 6 * variables.sizeScaling,
          paddingVertical: null,
          paddingHorizontal: null,
          alignItems: "center",
          justifyContent: "center"
        },
        "NativeBase.Icon": {
          width: variables.iconFontSize - 5 * variables.sizeScaling,
          fontSize: variables.iconFontSize - 2 * variables.sizeScaling
        },
        "NativeBase.IconNB": {
          width: variables.iconFontSize - 5 * variables.sizeScaling,
          fontSize: variables.iconFontSize - 2 * variables.sizeScaling
        },
        paddingRight: variables.listItemPadding + 5 * variables.sizeScaling,
        flex: 0,
        height: 44 * variables.sizeScaling,
        justifyContent: "center",
        alignItems: "center"
      },
      "NativeBase.Body": {
        "NativeBase.Text": {
          marginLeft: null,
        },
        flex: 1,
        height: 44 * variables.sizeScaling,
        justifyContent: "center",
        borderBottomWidth: variables.borderWidth,
        borderColor: variables.listBorderColor
      },
      "NativeBase.Right": {
        "NativeBase.Text": {
          textAlign: "center",
          color: "#8F8E95"
        },
        "NativeBase.IconNB": {
          color: "#C8C7CC",
          fontSize: variables.iconFontSize - 10 * variables.sizeScaling,
          alignSelf: "center",
          paddingLeft: 10 * variables.sizeScaling,
          paddingTop: 3 * variables.sizeScaling
        },
        "NativeBase.Icon": {
          color: "#C8C7CC",
          fontSize: variables.iconFontSize - 10 * variables.sizeScaling,
          alignSelf: "center",
          paddingLeft: 10 * variables.sizeScaling,
          paddingTop: 3 * variables.sizeScaling
        },
        "NativeBase.Switch": {
          marginRight: Platform.OS === "ios" ? undefined : (-5 * variables.sizeScaling),
          alignSelf: null
        },
        "NativeBase.PickerNB": {
          ...pickerTheme(variables)
        },
        flexDirection: "row",
        alignItems: "center",
        flex: 0,
        alignSelf: "stretch",
        height: 44 * variables.sizeScaling,
        justifyContent: "flex-end",
        borderBottomWidth: variables.borderWidth,
        borderColor: variables.listBorderColor,
        paddingRight: variables.listItemPadding + 5 * variables.sizeScaling
      },
      ".noBorder": {
        "NativeBase.Body": {
          borderBottomWidth: null
        },
        "NativeBase.Right": {
          borderBottomWidth: null
        }
      },
      borderBottomWidth: null,
      paddingVertical: null,
      paddingRight: null,
      height: 44 * variables.sizeScaling,
      justifyContent: "center"
    },
    ".noBorder": {
      borderBottomWidth: null
    },
    ".noIndent": {
      marginLeft: null,
      padding: variables.listItemPadding,
      paddingLeft: variables.listItemPadding + 6 * variables.sizeScaling
    },
    alignItems: "center",
    flexDirection: "row",
    paddingRight: variables.listItemPadding + 6 * variables.sizeScaling,
    paddingVertical: variables.listItemPadding,
    marginLeft: variables.listItemPadding + 6 * variables.sizeScaling,
    borderBottomWidth: variables.borderWidth,
    backgroundColor: variables.listBg,
    borderColor: variables.listBorderColor
  };

  return listItemTheme;
};
