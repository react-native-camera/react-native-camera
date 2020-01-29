import {Dimensions, StatusBar, Platform} from 'react-native';
import conf from 'src/conf';

// orientation helper to handle extra spaces needed to layout the camera perfectly
// on various devices
// This coul be a reducer or something if needed globally

const IS_IOS = Platform.OS == 'ios';
const IS_IPHONEX = conf.theme.variables.isIphoneX;

const IPHONE_INSET_TOP = conf.theme.variables.Inset.portrait.topInset;
const IPHONE_INSET_TOP_LANDSCAPE = conf.theme.variables.Inset.landscape.topInset;

const IPHONE_INSET_BOTTOM = conf.theme.variables.Inset.portrait.bottomInset;
const IPHONE_INSET_BOTTOM_LANDSCAPE = conf.theme.variables.Inset.landscape.bottomInset;



export function isLandscape(){
  let {width, height} = Dimensions.get('window');
  return width > height;
}



// height factor that we need sometimes to consider
// such as status bar height, beause different OS report height
// differently or status bar hides/shows on IOS, or notches.

// insetTop also used to position stuff with absolute position and consider
// status bars and notches based on orientation.
// Try to use this only for absolute positioning, use SafeAreaView otherwise
let getMinusHeight, getInsetTop, getInsetBottom;

if(!IS_IOS){
  // on Android, we always have a fixed status bar throught the app
  getMinusHeight = (isPortrait) => {
    return StatusBar.currentHeight;
  }
  getInsetTop = (isPortrait) => {
    return 0;
  }
  getInsetBottom = (isPortrait) => {
    return 0;
  }
}
else{
  if(IS_IPHONEX){
    getMinusHeight = (isPortrait) => {
      return 0;
    }
    getInsetTop = (isPortrait) => {
      return isPortrait ? IPHONE_INSET_TOP : IPHONE_INSET_TOP_LANDSCAPE;
    }
    getInsetBottom = (isPortrait) => {
      return isPortrait ? IPHONE_INSET_BOTTOM : IPHONE_INSET_BOTTOM_LANDSCAPE;
    }
  }
  else{
    getMinusHeight = (isPortrait) => {
      return 0
    }
    getInsetTop = (isPortrait) => {
      return isPortrait ? IPHONE_INSET_TOP : IPHONE_INSET_TOP_LANDSCAPE;
    }
    getInsetBottom = (isPortrait) => {
      return 0;
    }
  }
}


export function getOrientation(){
  let {width, height} = Dimensions.get('window');
  let isLandscape = width > height;

  return {
    width: width,
    height: height,
    isLandscape: isLandscape,
    isPortrait: !isLandscape,
    minusHeight: getMinusHeight(!isLandscape),
    insetTop: getInsetTop(!isLandscape),
    insetBottom: getInsetBottom(!isLandscape),
  }
}

// Hide statusbar on iOS landscape so it is consistent with iOS 13.
Dimensions.addEventListener('change', function(){
  // give time to UI to adjust

  setTimeout(()=>{
    // On Android, always visible since it has a bunch of issues.
    StatusBar.setHidden(IS_IOS && isLandscape(), 'none');
  }, 100);
});

