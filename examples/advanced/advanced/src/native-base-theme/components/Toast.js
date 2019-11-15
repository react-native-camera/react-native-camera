// @flow



export default (variables) => {
  const platform = variables.platform;

  const toastTheme = {
    ".danger": {
      backgroundColor: variables.brandDanger
    },
    ".warning": {
      backgroundColor: variables.brandWarning
    },
    ".success": {
      backgroundColor: variables.brandSuccess
    },
    backgroundColor: "rgba(0,0,0,0.8)",
    borderRadius: platform === "ios" ? (5 * variables.sizeScaling) : 0,
    flexDirection: "row",
    justifyContent: "space-between",
    alignItems: "center",
    padding: 10 * variables.sizeScaling,
    minHeight: 50 * variables.sizeScaling,
    "NativeBase.Text": {
      color: "#fff",
      flex: 1
    },
    "NativeBase.Button": {
      backgroundColor: "transparent",
      height: 30 * variables.sizeScaling,
      elevation: 0,
      "NativeBase.Text": {
        fontSize: variables.fontSizeSmall,
        color: "#fff"
      }
    }
  };

  return toastTheme;
};
