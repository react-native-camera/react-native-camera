// @flow



export default (variables) => {
  const platform = variables.platform;

  const fabTheme = {
    "NativeBase.Button": {
      alignItems: "center",
      padding: null,
      justifyContent: "center",
      "NativeBase.Icon": {
        alignSelf: "center",
        fontSize: 20 * variables.sizeScaling,
        marginLeft: 0,
        marginRight: 0,
      },
      "NativeBase.IconNB": {
        alignSelf: "center",
        fontSize: 20 * variables.sizeScaling,
        marginLeft: 0,
        marginRight: 0,
      },
    },
  };

  return fabTheme;
};
