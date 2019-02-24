# BarcodeFinderMask

> A subview for RNCamera to show barcode style layout

<p align="center" >
    <img 
        height="300" 
        src="https://user-images.githubusercontent.com/20506431/53305262-40d37200-38a1-11e9-9f87-c83a5cb44ac6.gif"
    >
</p>

## Usage

All you need is to `import` `{ BarcodeFinderMask }` from the `react-native-camera` module and then use it inside
`<RNCamera>...</RNCamera>` tag as a child component.

```javascript
'use strict';
import React, { Component } from 'react';
import { RNCamera, BarcodeFinderMask } from 'react-native-camera';

...
    <RNCamera
        ...
    >
        <BarcodeFinderMask />
    </RNCamera>
...
```

## Properties

#### `width`

Value: number | string (`%`)  
Default: `280`

Finder's width (the visible area)

#### `height`

Value: number | string (`%`)  
Default: `230`

Finder's height (the visible area)

#### `edgeWidth`

Value: number | string (`%`)  
Default: `20`

Edge/Corner's width

#### `edgeHeight`

Value: number | string (`%`)  
Default: `20`

Edge/Corner's height

#### `edgeColor`

Value: string  
Default: `#FFF`

Use this to give custom color to edges

#### `edgeBorderWidth`

Value: number | string (`%`)  
Default: `4`

Use this to modify the border (thickness) of edges

#### `transparency`

Value: float from `0` to `1.0`  
Default: `0.6`

Use this to modify the transparency of area around finder

#### `showAnimatedLine`

Value: boolean `true` | `false`  
Default: `true`

#### `animatedLineColor`

Value: string  
Default: `#FFF`

#### `animatedLineHeight`

Value: number  
Default: `2`

#### `lineAnimationDuration`

Value: number  
Default: `1500`

## Examples

Few style modifications (respectively):

```javascript
<BarcodeFinderMask width={300} height={100} />
<BarcodeFinderMask edgeColor={"#62B1F6"} showAnimatedLine={false}/>
<BarcodeFinderMask width={100} height={300} showAnimatedLine={false} transparency={0.8}/>
<BarcodeFinderMask width={300} height={100} edgeBorderWidth={1} />
```

<p align="center" >
    <img 
        height="300" 
        src="https://user-images.githubusercontent.com/20506431/53305263-40d37200-38a1-11e9-8106-6c79f4d59ead.png"
    >
    <img 
        height="300" 
        src="https://user-images.githubusercontent.com/20506431/53305265-416c0880-38a1-11e9-9364-7fd0b987207a.png"
    >
    <img 
        height="300" 
        src="https://user-images.githubusercontent.com/20506431/53305266-416c0880-38a1-11e9-8ef0-9ec9912fd355.png"
    >
    <img 
        height="300" 
        src="https://user-images.githubusercontent.com/20506431/53305264-40d37200-38a1-11e9-8752-1c5deaf78c65.png"
    >
</p>
