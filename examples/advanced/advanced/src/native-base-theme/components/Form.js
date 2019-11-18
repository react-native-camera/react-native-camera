// @flow



export default (variables) => {
  const platform = variables.platform;

  const theme = {
    "NativeBase.Item": {
      ".fixedLabel": {
        "NativeBase.Label": {
          paddingLeft: null
        },
        marginLeft: 15 * variables.sizeScaling
      },
      ".inlineLabel": {
        "NativeBase.Label": {
          paddingLeft: null
        },
        marginLeft: 15 * variables.sizeScaling
      },
      ".placeholderLabel": {
        "NativeBase.Input": {}
      },
      ".stackedLabel": {
        "NativeBase.Label": {
          top: 5,
          paddingLeft: null
        },
        "NativeBase.Input": {
          paddingLeft: null,
          marginLeft: null
        },
        "NativeBase.Icon": {
          marginTop: 36 * variables.sizeScaling
        },
        marginLeft: 15 * variables.sizeScaling
      },
      ".floatingLabel": {
        "NativeBase.Input": {
          paddingLeft: null,
          top: 10,
          marginLeft: null
        },
        "NativeBase.Label": {
          left: 0,
          top: 6
        },
        "NativeBase.Icon": {
          top: 6
        },
        marginTop: 15 * variables.sizeScaling,
        marginLeft: 15 * variables.sizeScaling
      },
      ".regular": {
        "NativeBase.Label": {
          left: 0
        },
        marginLeft: 0
      },
      ".rounded": {
        "NativeBase.Label": {
          left: 0
        },
        marginLeft: 0
      },
      ".underline": {
        "NativeBase.Label": {
          left: 0,
          top: 0,
          position: "relative"
        },
        "NativeBase.Input": {
          left: -15 * variables.sizeScaling
        },
        marginLeft: 15 * variables.sizeScaling
      },
      ".last": {
        marginLeft: 0,
        paddingLeft: 15 * variables.sizeScaling
      },
      "NativeBase.Label": {
        paddingRight: 5 * variables.sizeScaling
      },
      marginLeft: 15 * variables.sizeScaling
    }
  };

  return theme;
};
