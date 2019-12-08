---
id: react-navigation
title: React Navigation
sidebar_label: React Navigation
---

React-navigation does not unmount components when switching between tabs. So when you leave and return back to the screen with the camera component it will just be black view. So a good solution is to use `withNavigationFocus`, which is a higher order component, and wrap it around your component. Then, you can have access to `isFocused` from `props`.

```jsx

import { withNavigationFocus } from 'react-navigation' 

 render() {
    const { isFocused } = this.props
    const { hasCameraPermission } = this.state;
    if (hasCameraPermission === null) {
      return <View />;
    } else if (hasCameraPermission === false) {
      return <Text>No access to camera</Text>;
    } else if (isFocused){
      return (this.cameraView());
    } else {
      return <View />;
    }
 }

export default withNavigationFocus(YourComponent)
```
