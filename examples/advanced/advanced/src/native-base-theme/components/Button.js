// @flow



export default (variables) => {
  const platformStyle = variables.platformStyle;
  const platform = variables.platform;

  const blackCommon = {
    "NativeBase.Text": {
      color: variables.brandBlack
    },
    "NativeBase.H1": {
      color: variables.brandBlack
    },
    "NativeBase.H2": {
      color: variables.brandBlack
    },
    "NativeBase.H3": {
      color: variables.brandBlack
    },
    "NativeBase.Icon": {
      color: variables.brandBlack
    },
    "NativeBase.IconNB": {
      color: variables.brandBlack
    }
  };

  const blackCommonTransparent = {
    "NativeBase.Text": {
      color: variables.transparentColor
    },
    "NativeBase.H1": {
      color: variables.transparentColor
    },
    "NativeBase.H2": {
      color: variables.transparentColor
    },
    "NativeBase.H3": {
      color: variables.transparentColor
    },
    "NativeBase.Icon": {
      color: variables.transparentColor
    },
    "NativeBase.IconNB": {
      color: variables.transparentColor
    }
  };

  const darkCommon = {
    "NativeBase.Text": {
      color: variables.brandDark
    },
    "NativeBase.H1": {
      color: variables.brandDark
    },
    "NativeBase.H2": {
      color: variables.brandDark
    },
    "NativeBase.H3": {
      color: variables.brandDark
    },
    "NativeBase.Icon": {
      color: variables.brandDark
    },
    "NativeBase.IconNB": {
      color: variables.brandDark
    }
  };

  const darkCommonTransparent = {
    "NativeBase.Text": {
      color: variables.transparentColor
    },
    "NativeBase.H1": {
      color: variables.transparentColor
    },
    "NativeBase.H2": {
      color: variables.transparentColor
    },
    "NativeBase.H3": {
      color: variables.transparentColor
    },
    "NativeBase.Icon": {
      color: variables.transparentColor
    },
    "NativeBase.IconNB": {
      color: variables.transparentColor
    }
  };

  const lightCommon = {
    "NativeBase.Text": {
      color: variables.brandLight
    },
    "NativeBase.H1": {
      color: variables.brandLight
    },
    "NativeBase.H2": {
      color: variables.brandLight
    },
    "NativeBase.H3": {
      color: variables.brandLight
    },
    "NativeBase.Icon": {
      color: variables.brandLight
    },
    "NativeBase.IconNB": {
      color: variables.brandLight
    }
  };
  const primaryCommon = {
    "NativeBase.Text": {
      color: variables.buttonPrimaryBg
    },
    "NativeBase.H1": {
      color: variables.buttonPrimaryBg
    },
    "NativeBase.H2": {
      color: variables.buttonPrimaryBg
    },
    "NativeBase.H3": {
      color: variables.buttonPrimaryBg
    },
    "NativeBase.Icon": {
      color: variables.buttonPrimaryBg
    },
    "NativeBase.IconNB": {
      color: variables.buttonPrimaryBg
    }
  };
  const successCommon = {
    "NativeBase.Text": {
      color: variables.buttonSuccessBg
    },
    "NativeBase.H1": {
      color: variables.buttonSuccessBg
    },
    "NativeBase.H2": {
      color: variables.buttonSuccessBg
    },
    "NativeBase.H3": {
      color: variables.buttonSuccessBg
    },
    "NativeBase.Icon": {
      color: variables.buttonSuccessBg
    },
    "NativeBase.IconNB": {
      color: variables.buttonSuccessBg
    }
  };
  const infoCommon = {
    "NativeBase.Text": {
      color: variables.buttonInfoBg
    },
    "NativeBase.H1": {
      color: variables.buttonInfoBg
    },
    "NativeBase.H2": {
      color: variables.buttonInfoBg
    },
    "NativeBase.H3": {
      color: variables.buttonInfoBg
    },
    "NativeBase.Icon": {
      color: variables.buttonInfoBg
    },
    "NativeBase.IconNB": {
      color: variables.buttonInfoBg
    }
  };
  const warningCommon = {
    "NativeBase.Text": {
      color: variables.buttonWarningBg
    },
    "NativeBase.H1": {
      color: variables.buttonWarningBg
    },
    "NativeBase.H2": {
      color: variables.buttonWarningBg
    },
    "NativeBase.H3": {
      color: variables.buttonWarningBg
    },
    "NativeBase.Icon": {
      color: variables.buttonWarningBg
    },
    "NativeBase.IconNB": {
      color: variables.buttonWarningBg
    }
  };
  const dangerCommon = {
    "NativeBase.Text": {
      color: variables.buttonDangerBg
    },
    "NativeBase.H1": {
      color: variables.buttonDangerBg
    },
    "NativeBase.H2": {
      color: variables.buttonDangerBg
    },
    "NativeBase.H3": {
      color: variables.buttonDangerBg
    },
    "NativeBase.Icon": {
      color: variables.buttonDangerBg
    },
    "NativeBase.IconNB": {
      color: variables.buttonDangerBg
    }
  };
  const buttonTheme = {
    ".disabled": {
      ".transparent": {
        backgroundColor: 'transparent',
        "NativeBase.Text": {
          color: variables.buttonDisabledBg
        },
        "NativeBase.H1": {
          color: variables.buttonDisabledBg
        },
        "NativeBase.H2": {
          color: variables.buttonDisabledBg
        },
        "NativeBase.H3": {
          color: variables.buttonDisabledBg
        },
        "NativeBase.Icon": {
          color: variables.buttonDisabledBg
        },
        "NativeBase.IconNB": {
          color: variables.buttonDisabledBg
        }
      },
      "NativeBase.Text": {
        color: variables.buttonDisabledText
      },
      "NativeBase.Icon": {
        color: variables.buttonDisabledText
      },
      "NativeBase.IconNB": {
        color: variables.buttonDisabledText
      },
      backgroundColor: variables.buttonDisabledBg
    },
    ".bordered": {
      ".black": {
        ...blackCommon,
        backgroundColor: "transparent",
        borderColor: variables.brandBlack,
        borderWidth: variables.borderWidth
      },
      ".dark": {
        ...darkCommon,
        backgroundColor: "transparent",
        borderColor: variables.brandDark,
        borderWidth: variables.borderWidth
      },
      ".light": {
        ...lightCommon,
        backgroundColor: "transparent",
        borderColor: variables.brandLight,
        borderWidth: variables.borderWidth
      },
      ".primary": {
        ...primaryCommon,
        backgroundColor: "transparent",
        borderColor: variables.buttonPrimaryBg,
        borderWidth: variables.borderWidth
      },
      ".success": {
        ...successCommon,
        backgroundColor: "transparent",
        borderColor: variables.buttonSuccessBg,
        borderWidth: variables.borderWidth
      },
      ".info": {
        ...infoCommon,
        backgroundColor: "transparent",
        borderColor: variables.buttonInfoBg,
        borderWidth: variables.borderWidth
      },
      ".warning": {
        ...warningCommon,
        backgroundColor: "transparent",
        borderColor: variables.buttonWarningBg,
        borderWidth: variables.borderWidth
      },
      ".danger": {
        ...dangerCommon,
        backgroundColor: "transparent",
        borderColor: variables.buttonDangerBg,
        borderWidth: variables.borderWidth
      },
      ".disabled": {
        backgroundColor: 'transparent',
        borderColor: variables.buttonDisabledBg,
        borderWidth: variables.borderWidth,
        "NativeBase.Text": {
          color: variables.buttonDisabledBg
        }
      },
      ...primaryCommon,
      borderWidth: variables.borderWidth,
      elevation: 0,
      shadowColor: null,
      shadowOffset: null,
      shadowOpacity: null,
      shadowRadius: null,
      backgroundColor: "transparent"
    },

    ".black": {
      ".bordered": {
        ...blackCommon
      },
      backgroundColor: variables.brandBlack
    },
    ".dark": {
      ".bordered": {
        ...darkCommon
      },
      backgroundColor: variables.brandDark
    },
    ".light": {
      ".bordered": {
        ...lightCommon
      },
      ...blackCommon,
      backgroundColor: variables.brandLight
    },

    ".primary": {
      ".bordered": {
        ...primaryCommon
      },
      backgroundColor: variables.buttonPrimaryBg
    },

    ".success": {
      ".bordered": {
        ...successCommon
      },
      backgroundColor: variables.buttonSuccessBg
    },

    ".info": {
      ".bordered": {
        ...infoCommon
      },
      backgroundColor: variables.buttonInfoBg
    },

    ".warning": {
      ".bordered": {
        ...warningCommon
      },
      backgroundColor: variables.buttonWarningBg
    },

    ".danger": {
      ".bordered": {
        ...dangerCommon
      },
      backgroundColor: variables.buttonDangerBg
    },

    ".block": {
      justifyContent: "center",
      alignSelf: "stretch"
    },

    ".full": {
      justifyContent: "center",
      alignSelf: "stretch",
      borderRadius: 0
    },

    ".rounded": {
      // paddingHorizontal: variables.buttonPadding + 20,
      borderRadius: variables.borderRadiusLarge
    },

    ".transparent": {
      backgroundColor: "transparent",
      elevation: 0,
      shadowColor: null,
      shadowOffset: null,
      shadowRadius: null,
      shadowOpacity: null,
      ...primaryCommon,

      ".black": {
        ...blackCommon,
        backgroundColor: 'transparent'
      },
      ".dark": {
        ...darkCommon,
        backgroundColor: 'transparent'
      },
      ".danger": {
        ...dangerCommon,
        backgroundColor: 'transparent'
      },
      ".warning": {
        ...warningCommon,
        backgroundColor: 'transparent'
      },
      ".info": {
        ...infoCommon,
        backgroundColor: 'transparent'
      },
      ".primary": {
        ...primaryCommon,
        backgroundColor: 'transparent'
      },
      ".success": {
        ...successCommon,
        backgroundColor: 'transparent'
      },
      ".light": {
        ...lightCommon,
        backgroundColor: 'transparent'
      },
      ".disabled": {
        backgroundColor: "transparent",
        borderColor: variables.buttonDisabledBg,
        borderWidth: variables.borderWidth,
        "NativeBase.Text": {
          color: variables.buttonDisabledBg
        },
        "NativeBase.Icon": {
          color: variables.buttonDisabledBg
        },
        "NativeBase.IconNB": {
          color: variables.buttonDisabledBg
        }
      }
    },

    ".small": {
      height: 30 * variables.sizeScaling,
      minWidth: 30 * variables.sizeScaling,
      "NativeBase.Text": {
        fontSize: variables.fontSizeSmall
      },
      "NativeBase.Icon": {
        fontSize: 20 * variables.sizeScaling,
        paddingTop: 0
      },
      "NativeBase.IconNB": {
        fontSize: 20 * variables.sizeScaling,
        paddingTop: 0
      },
      '.noWidth':{
        minWidth: 0
      }
    },

    ".large": {
      height: 60 * variables.sizeScaling,
      minWidth: 60 * variables.sizeScaling,
      "NativeBase.Text": {
        fontSize: variables.fontSizeLarge,
      },
      '.noWidth':{
        minWidth: 0
      }
    },

    ".medium": {
      height: 50 * variables.sizeScaling,
      minWidth: 50 * variables.sizeScaling,
      "NativeBase.Text": {
        fontSize: variables.fontSizeMedium,
      },
      '.noWidth':{
        minWidth: 0
      }
    },

    ".capitalize": {},

    ".vertical": {
      flexDirection: "column",
      height: null
    },

    "NativeBase.Text": {
      fontFamily: variables.buttonFontFamily,
      marginLeft: 0,
      marginRight: 0,
      color: variables.inverseTextColor,
      fontSize: variables.buttonTextSize,
      paddingHorizontal: 16 * variables.sizeScaling,
      backgroundColor: "transparent"
      // childPosition: 1
    },

    "NativeBase.Icon": {
      color: variables.inverseTextColor,
      fontSize: variables.buttonIconSize,
      marginHorizontal: 16 * variables.sizeScaling,
      paddingTop: 0
    },
    "NativeBase.IconNB": {
      color: variables.inverseTextColor,
      fontSize: variables.buttonIconSize,
      marginHorizontal: 16 * variables.sizeScaling,
      paddingTop: 0
    },

    ".iconLeft": {
      "NativeBase.Text": {
        marginLeft: 0
      },
      "NativeBase.IconNB": {
        marginRight: 0,
        marginLeft: 16 * variables.sizeScaling
      },
      "NativeBase.Icon": {
        marginRight: 0,
        marginLeft: 16 * variables.sizeScaling
      }
    },
    ".iconRight": {
      "NativeBase.Text": {
        marginRight: 0
      },
      "NativeBase.IconNB": {
        marginLeft: 0,
        marginRight: 16 * variables.sizeScaling
      },
      "NativeBase.Icon": {
        marginLeft: 0,
        marginRight: 16 * variables.sizeScaling
      }
    },
    ".picker": {
      "NativeBase.Text": {
        ".note": {
          fontSize: variables.noteFontSize,
          lineHeight: null
        }
      }
    },
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
      }
    },

    paddingVertical: variables.buttonPadding,
    paddingHorizontal: 0,
    backgroundColor: variables.buttonPrimaryBg,
    borderRadius: variables.borderRadiusBase,
    borderColor: variables.buttonPrimaryBg,
    borderWidth: null,
    height: variables.buttonHeight,
    minWidth: variables.buttonHeight,
    alignSelf: "flex-start",
    flexDirection: "row",
    elevation: 0,
    shadowColor: platformStyle === "material" ? variables.brandBlack : undefined,
    shadowOffset:
      platformStyle === "material" ? { width: 0, height: 2 } : undefined,
    shadowOpacity: platformStyle === "material" ? 0.2 : undefined,
    shadowRadius: platformStyle === "material" ? 1.2 : undefined,
    alignItems: "center",
    justifyContent: "center",

    '.leftJustified': {
      justifyContent: 'flex-start'
    },
    '.rightJustified': {
      justifyContent: 'flex-end'
    },
    '.topJustified': {
      alignItems: "flex-start",
    },
    '.noWidth':{
      minWidth: 0
    },
    '.autoHeight': {
      height: 'auto'
    },
    '.selfCenter': {
      alignSelf: 'center'
    },
    '.marginLeft': {
      marginLeft: variables.contentPadding
    },
    '.marginRight': {
      marginRight: variables.contentPadding
    },
    '.marginBottom': {
      marginBottom: variables.contentPadding
    },
    '.flex1': {
      flex: 1
    }

  };
  return buttonTheme;
};
