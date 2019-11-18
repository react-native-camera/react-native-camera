// @flow

import color from "color";

import { Platform, Dimensions, PixelRatio } from "react-native";

const deviceHeight = Dimensions.get("window").height;
const deviceWidth = Dimensions.get("window").width;
const platform = Platform.OS;
const platformStyle = undefined;
const isIphoneX =
platform === "ios" && (deviceHeight === 812 || deviceWidth === 812 || deviceHeight === 896 || deviceWidth === 896);


// ---------------------------
// In order to access the theme variables from within a component, we made a small work around
// because the library does not support it natively.
// Created a "Branding" theme variable/component (at ./components/index) that exports all variables
//
// import {connectStyle} from 'native-base';
// ... in render: this.props.style.[variable I want]
// export default connectStyle("Branding")(MyComponent);
//
// ---------------------------

export default function getVariables(overrides, sizeScaling=1){

  let primaryColor = overrides.primaryColor || '#FAAC18';
  let iosPrimaryColor = overrides.primaryColor || '#FAAC18';
  let toolbarColor = overrides.toolbarColor || '#FAAC18';
  let statusBarColor = overrides.statusBarColor || toolbarColor;
  let brandLight = overrides.brandLight || "#FFD185";

  let brandInfo = overrides.brandInfo ||  "#03a9f4";
  let brandSuccess = overrides.brandSuccess || "#5cb85c";
  let brandDanger = overrides.brandDanger || "#d9534f";
  let brandWarning = overrides.brandWarning || "#f0ad4e";
  let brandBlack = overrides.brandBlack || '#121212';
  let brandDark = overrides.brandDark || "#121212";


  let textColor = overrides.textColor || brandBlack;
  let labelColor = '#575757';
  let labelColorTransparent = '#a5a5a5';
  let inverseTextColor = overrides.inverseTextColor || "#FFF";
  let transparentColor = overrides.transparentColor || "#FFF";

  // becareful overriding these since they are used as static variables everywhere
  // and cannot be updated in real time.
  let inputColorPlaceholder = "#7b7b7b";
  let inputColorPlaceholderTransparent = '#a5a5a5';
  let inputBorderColor = "#575757";
  let darkBorderColor = "#121212";

  let headerTextColor = overrides.headerTextColor || inverseTextColor;

  let iosStatusbar = overrides.iosStatusbar || 'light-content'; // "dark-content" : "light-content",

  // these will be adjuster by scaling later.
  let fontSize = 15;
  let fontSizeSmall = 14;
  let fontSizeMedium = 17;
  let fontSizeLarge = 22;
  let headerFontSize = 16;
  let noteFontSize = 13;
  let buttonHeight = 45; // make sure this is smaller than large values from Button


  return {
    platformStyle,
    platform,
    sizeScaling,

    //Accordion
    accordionBorderColor: '#d3d3d3',
    accordionContentPadding: 10 * sizeScaling,
    accordionIconFontSize: 18 * sizeScaling,
    contentStyle: '#f5f4f5',
    expandedIconStyle: '#000',
    headerStyle: '#edebed',
    iconStyle: '#000',

    // ActionSheet
    elevation: 4,
    containerTouchableBackgroundColor: 'rgba(0,0,0,0.4)',
    innerTouchableBackgroundColor: '#fff',
    listItemHeight: 50 * sizeScaling,
    listItemBorderColor: 'transparent',
    marginHorizontal: -15 * sizeScaling,
    marginLeft: 14 * sizeScaling,
    marginTop: 15 * sizeScaling,
    minHeight: 56 * sizeScaling,
    padding: 15 * sizeScaling,
    touchableTextColor: '#757575',

    // Android
    androidRipple: false,
    androidRippleColor: "rgba(256, 256, 256, 0.35)",
    androidRippleColorDark: "rgba(0, 0, 0, 0.15)",
    buttonUppercaseAndroidText: false,

    // Badge
    badgeBg: "#ED1727",
    badgeColor: "#fff",
    badgePadding: 0,

    // Button
    buttonFontFamily: platform === "ios" ? "System" : "Roboto",
    buttonDisabledBg: "#a5a5a5",
    buttonDisabledText: '#f4f4f4',
    buttonPadding: 6 * sizeScaling,
    buttonDefaultActiveOpacity: 0.5,
    buttonDefaultFlex: 1,
    buttonDefaultBorderRadius: 2 * sizeScaling,
    buttonDefaultBorderWidth: 1,
    buttonHeight: buttonHeight * sizeScaling,

    get buttonPrimaryBg() {
      return this.brandPrimary;
    },
    get buttonPrimaryColor() {
      return this.inverseTextColor;
    },
    get buttonInfoBg() {
      return this.brandInfo;
    },
    get buttonInfoColor() {
      return this.inverseTextColor;
    },
    get buttonSuccessBg() {
      return this.brandSuccess;
    },
    get buttonSuccessColor() {
      return this.inverseTextColor;
    },
    get buttonDangerBg() {
      return this.brandDanger;
    },
    get buttonDangerColor() {
      return this.inverseTextColor;
    },
    get buttonWarningBg() {
      return this.brandWarning;
    },
    get buttonWarningColor() {
      return this.inverseTextColor;
    },
    get buttonTextSize() {
      return this.fontSizeBase; //platform === "ios" ? this.fontSizeBase : this.fontSizeBase;
    },
    get buttonTextSizeLarge() {
      return fontSizeLarge;
    },
    get buttonTextSizeSmall() {
      return fontSizeSmall;
    },
    get borderRadiusLarge() {
      return this.fontSizeBase * 3.8;
    },
    get iconSizeLarge() {
      return this.iconFontSize * 1.5;
    },
    get iconSizeSmall() {
      return this.iconFontSize * 0.6;
    },

    // Card
    cardDefaultBg: '#f6f8fa',//'"#fff",
    cardHeaderBg: '#eaebed',
    cardBorderColor: "#ccc",
    cardBorderRadius: 8 * sizeScaling,
    cardItemPadding: (platform === "ios" ? 10 : 12) * sizeScaling,

    // CheckBox
    CheckboxRadius: (platform === "ios" ? 10 : 0) * sizeScaling,
    CheckboxBorderWidth: platform === "ios" ? 1 : 2,
    CheckboxPaddingLeft: (platform === "ios" ? 5 : 2) * sizeScaling,
    CheckboxPaddingBottom: (platform === "ios" ? 5 : 5) * sizeScaling,
    CheckboxIconSize: (platform === "ios" ? 21 : 16) * sizeScaling,
    CheckboxIconMarginTop: platform === "ios" ? undefined : 1,
    CheckboxFontSize: (platform === "ios" ? 23 : 17) * sizeScaling,
    checkboxBgColor: platform === "ios" ? iosPrimaryColor : primaryColor,
    checkboxSize: 20 * sizeScaling,
    checkboxTickColor: inverseTextColor,
    checkboxDefaultColor: 'transparent',
    checkboxTextShadowRadius: 0,

    // Color
    brandPrimary: platform === "ios" ? iosPrimaryColor : primaryColor,
    brandInfo: brandInfo,
    brandSuccess: brandSuccess,
    brandDanger: brandDanger,
    brandWarning: brandWarning,
    brandDark: brandDark,
    brandBlack: brandBlack,
    brandLight: brandLight,

    //Container
    containerBgColor: "#fff",

    //Date Picker
    datePickerFlex: 1,
    datePickerPadding: 10 * sizeScaling,
    datePickerTextColor: "#000",
    datePickerBg: "transparent",

    // FAB
    fabWidth: 56 * sizeScaling,

    // Font
    DefaultFontSize: fontSize * sizeScaling,

    fontSizeBase: fontSize * sizeScaling,
    fontSizeSmall: fontSizeSmall * sizeScaling,
    fontSizeMedium: fontSizeMedium * sizeScaling,
    fontSizeLarge: fontSizeLarge * sizeScaling,
    headerFontSize: headerFontSize * sizeScaling,
    noteFontSize: noteFontSize * sizeScaling,

    fontFamily: platform === "ios" ? "System" : "Roboto",
    get fontSizeH1() {
      return this.fontSizeBase * 1.8;
    },
    get fontSizeH2() {
      return this.fontSizeBase * 1.6;
    },
    get fontSizeH3() {
      return this.fontSizeBase * 1.4;
    },

    // Footer
    footerHeight: (platform === "ios" ? 60 : 56) * sizeScaling,
    footerWidth: (platform === "ios" ? 60 : 60) * sizeScaling, // for landscape mode
    footerDefaultBg: toolbarColor, //platform === "ios" ? "#F8F8F8" : toolbarColor,
    footerPaddingBottom: 0,

    // FooterTab
    tabBarTextColor: headerTextColor, //platform === "ios" ? "#6b6b6b" : "#fff",
    tabBarTextSize: (platform === "ios" ? 15 : 15) * sizeScaling,
    activeTab: headerTextColor, //platform === "ios" ? iosPrimaryColor : "#fff",
    sTabBarActiveTextColor: iosPrimaryColor,
    tabBarActiveTextColor: headerTextColor, //platform === "ios" ? iosPrimaryColor : "#fff",
    tabActiveBgColor: primaryColor, //platform === "ios" ? "#cde1f9" : primaryColor,

    // Header
    toolbarBtnColor: headerTextColor, //platform === "ios" ? iosPrimaryColor : "#fff",
    toolbarDefaultBg: toolbarColor, //platform === "ios" ? "#F8F8F8" : toolbarColor,
    toolbarHeight: 52 * sizeScaling, // iOS header will adjust this as needed to compensate for status bars
    toolbarSearchIconSize: (platform === "ios" ? 20 : 23) * sizeScaling,
    toolbarInputColor: headerTextColor, //platform === "ios" ? "#CECDD2" : "#fff",
    searchBarHeight: (platform === "ios" ? 30 : 40) * sizeScaling,
    searchBarInputHeight: (platform === "ios" ? 30 : 30 ) * sizeScaling,
    toolbarBtnTextColor: headerTextColor, //platform === "ios" ? iosPrimaryColor : "#fff",
    toolbarDefaultBorder: primaryColor, //platform === "ios" ? "#a7a6ab" : primaryColor,
    transparentColor: transparentColor,
    iosStatusbar: iosStatusbar,
    statusBarColor: statusBarColor,
    // get statusBarColor() {
    //   return color(this.toolbarDefaultBg)
    //     .darken(0.2)
    //     .hex();
    // },
    get darkenHeader() {
      return color(this.tabBgColor)
        .darken(0.03)
        .hex();
    },

    // Icon
    iconFamily: "Ionicons",
    iconFontSize: (platform === "ios" ? 28 : 28) * sizeScaling,
    iconHeaderSize: (platform === "ios" ? 24 : 24) * sizeScaling,
    iconBackButtonSize: 28 * sizeScaling,
    buttonIconSize: (platform === "ios" ? 24 : 24) * sizeScaling,

    // InputGroup
    inputFontSize: fontSize * sizeScaling,
    inputBorderColor: inputBorderColor, //D9D5DC
    darkBorderColor: darkBorderColor,
    inputSuccessBorderColor: "#2b8339",
    inputErrorBorderColor: "#ed2f2f",
    inputHeightBase: 30 * sizeScaling,
    inputColor: textColor,
    inputColorPlaceholder: inputColorPlaceholder,
    inputColorPlaceholderTransparent: inputColorPlaceholderTransparent,

    // Line Height
    buttonLineHeight: 19 * sizeScaling,
    lineHeightH1: 32 * sizeScaling,
    lineHeightH2: 27 * sizeScaling,
    lineHeightH3: 22 * sizeScaling,
    lineHeight: 20 * sizeScaling, //platform === "ios" ? 20 : 20,
    listItemSelected: platform === "ios" ? iosPrimaryColor : primaryColor,

    // List
    listBg: "transparent",
    listBorderColor: "#c9c9c9",
    listDividerBg: "#f4f4f4",
    listBtnUnderlayColor: "#DDD",
    listItemPadding: (platform === "ios" ? 10 : 12) * sizeScaling,
    listNoteColor: inputColorPlaceholder,
    listNoteSize: 13 * sizeScaling,

    // Progress Bar
    defaultProgressColor: "#E4202D",
    inverseProgressColor: "#1A191B",

    // Radio Button
    radioBtnSize: (platform === "ios" ? 25 : 23) * sizeScaling,
    radioSelectedColorAndroid: primaryColor,
    radioBtnLineHeight: (platform === "ios" ? 29 : 24) * sizeScaling,
    get radioColor() {
      return this.brandPrimary;
    },

    // Segment
    segmentBackgroundColor: platform === "ios" ? iosPrimaryColor: primaryColor,
    segmentActiveBackgroundColor: "#fff", //platform === "ios" ? iosPrimaryColor : "#fff",
    segmentTextColor: "#fff", //platform === "ios" ? iosPrimaryColor : "#fff",
    segmentActiveTextColor: primaryColor, //platform === "ios" ? "#fff" : primaryColor,
    segmentBorderColor: "#fff", //platform === "ios" ? iosPrimaryColor : "#fff",
    segmentBorderColorMain: platform === "ios" ? iosPrimaryColor : primaryColor,

    // Spinner
    defaultSpinnerColor: "#45D56E",
    inverseSpinnerColor: "#1A191B",

    // Tab
    tabBarDisabledTextColor: '#BDBDBD',
    tabDefaultBg: platform === "ios" ? iosPrimaryColor : primaryColor,
    topTabBarTextColor: headerTextColor, //platform === "ios" ? "#6b6b6b" : "#b3c7f9",
    topTabBarActiveTextColor: headerTextColor, //platform === "ios" ? iosPrimaryColor: "#fff",
    topTabBarBorderColor: headerTextColor, //platform === "ios" ? "#a7a6ab" : "#fff",
    topTabBarActiveBorderColor: headerTextColor, //platform === "ios" ? iosPrimaryColor : "#fff",

    // Tabs
    tabBgColor: "#F8F8F8",
    tabFontSize: fontSize * sizeScaling,

    // Text
    textColor: textColor,
    labelColor: labelColor,
    labelColorTransparent: labelColorTransparent,
    inverseTextColor: inverseTextColor,
    get defaultTextColor() {
      return this.textColor;
    },

    // Title
    titleFontfamily: platform === "ios" ? "System" : "Roboto_medium",
    titleFontSize: (platform === "ios" ? 17 : 17) * sizeScaling,
    subTitleFontSize: (platform === "ios" ? 11 : 11) * sizeScaling,
    subtitleColor: headerTextColor, //platform === "ios" ? "#8e8e93" : "#FFF",
    titleFontColor: headerTextColor, //platform === "ios" ? "#000" : "#FFF",

    // Other
    borderRadiusBase: (platform === "ios" ? 5 : 2) * sizeScaling,
    //borderWidth: 0.5, /// PixelRatio.getPixelSizeForLayoutSize(1),
    borderWidth: 1 / PixelRatio.getPixelSizeForLayoutSize(1),
    contentPadding: 10 * sizeScaling,
    dropdownLinkColor: "#414142",
    inputLineHeight: 24 * sizeScaling,
    deviceWidth,
    deviceHeight,
    isIphoneX,
    inputGroupRoundedBorderRadius: 30 * sizeScaling,

    //iPhoneX SafeArea - barely used, use SafeAreaView instead
    Inset: isIphoneX ? {
      portrait: {
        topInset: 32,
        leftInset: 0,
        rightInset: 0,
        bottomInset: 20
      },
      landscape: {
        topInset: 15,
        leftInset: 0,
        rightInset: 0,
        bottomInset: 10
      }
    } :
    Platform.OS == 'ios' ?
      {
      portrait: {
        topInset: 20, // to account for portrait status bar
        leftInset: 0,
        rightInset: 0,
        bottomInset: 0
      },
      landscape: {
        topInset: 0,
        leftInset: 0,
        rightInset: 0,
        bottomInset: 0
      }
    }
    :
    { // so we don't need to check for iphoneX or not
      portrait: {
        topInset: 0, // Android toolbar/notch does not interfere with screen
        leftInset: 0,
        rightInset: 0,
        bottomInset: 0
      },
      landscape: {
        topInset: 0,
        leftInset: 0,
        rightInset: 0,
        bottomInset: 0
      }
    }
  };
}
