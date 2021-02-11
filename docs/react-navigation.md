---
id: react-navigation
title: React Navigation
sidebar_label: React Navigation
---

React-navigation does not unmount components when switching between tabs. So when you leave and return back to the screen with the camera component it will just be black view. A good solution is to use the `useIsFocused` hook, so you can render the camera view conditionally.

```jsx
import { useIsFocused } from '@react-navigation/core';

export const Component = () => {
  const isFocused = useIsFocused();

  // ...

  if (hasCameraPermission === false) {
    return <Text>No access to camera</Text>;
  } else if (hasCameraPermission !== null && isFocused) {
    return <CameraView />;
  } else {
    return null;
  }
}
```
