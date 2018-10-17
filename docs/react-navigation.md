# react-navigation

React-navigation does not unmount components when switching between tabs. So when you leave and return back to the screen with the camera component it will just be black view. So a good solution is to use `componentDidMount` and added two listeners `willFocus` and `willBlur` to help you mount and unmount the views.

```jsx
componentDidMount() {
   const { navigation } = this.props;
   navigation.addListener('willFocus', () =>
     this.setState({ focusedScreen: true })
   );
   navigation.addListener('willBlur', () =>
     this.setState({ focusedScreen: false })
   );
 }

 render() {
   const { hasCameraPermission, focusedScreen } = this.state;
   if (hasCameraPermission === null) {
     return <View />;
   } else if (hasCameraPermission === false) {
     return <Text>No access to camera</Text>;
   } else if (focusedScreen){
     return (this.cameraView());
   } else {
     return <View />;
   }
 }
 ```
