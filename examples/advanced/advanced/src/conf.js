import {Dimensions} from 'react-native';
import getTheme from './native-base-theme/components';
import platform from './native-base-theme/variables/platform';


let conf = {};

// Returns the recommended scaling based on screen dimensions
// as a string
const recommendedScaling = () => {
  try{

    // our guideline will be 375 width, based on iphone 7,8 and Xs
    // ideas taken from https://blog.solutotlv.com/size-matters/
    const baseWidth = 375;
    const factor = 0.3; // resize factor

    // Tested Widths in DPI
    // 375 -> iphone 7, 8, X, 11 [Pro] (4.7 to 5.8 inches) --> normal, no prompt
    // 414 -> iPhone X/Xs/11 Max (6.5 inches) --> normal, no prompt
    // 768 -> iPad Pro 9.7 inch (should be the same as ipad mini) --> average, prompt
    // 834 -> iPad Pro 11 inch --> large, prompt
    // 1024 -> iPad Pro 12.9 inch --> large prompt

    let res = "1.0"; // our normal sizing

    let {width, height} = Dimensions.get('window');
    let value = Math.min(width, height); // just in case the device starts up rotated.

    let scale, moderateScale;
    if(value != 0){

    scale = value / baseWidth;
    moderateScale = 1 + (scale - 1) * factor;

    // map our ratio to our internal scaling values
    if(moderateScale >= 2.5){
      res = "2.5";
    }
    else if(moderateScale <= 0.7){ // should never happen unless using a tiny phone
      res = "0.7";
    }
    else{
      res = (Math.round(moderateScale * 10 ) / 10).toFixed(1);
    }

    }
    else{
      console.warn("Warning, failed to get device screen ratio for scaling calculations, it was 0.");
    }

    return res;

  }
  catch(err){
    console.error("CRITICAL: Failed to get recommended scaling", err.message || err)
  }
}

// preconfigured, see native-base-theme/variables/platform for more options
// key will be the value of the primary color so we can auto select the others
conf.themes = {

  // default: was typical orange, switched to gray. See gray below
  'default': {
    primaryColor: '#404040',
    toolbarColor: '#404040',
    brandLight: '#cfd8dc'
  },


  // red dark
  '#d32f2f': {
    primaryColor: '#d32f2f',
    toolbarColor: '#d32f2f',
    brandLight: '#e57373',
    brandDanger: '#b71c1c'
  },


  // super blue light
  '#bbdefb': {
    primaryColor: '#bbdefb',
    toolbarColor: '#bbdefb',
    brandLight: '#e4f1fb',

    iosStatusbar: 'dark-content',
    brandDark: '#d9d9d9',
    brandDanger: '#eb7168',
    brandSuccess: '#81c784',
    brandInfo: '#4fc3f7',
    brandWarning: '#fad25c',
    brandBlack: '#3c3d3d',
    inverseTextColor: '#3c3d3d'
  },

  // blue dark
  '#1976d2': {
    primaryColor: '#1976d2',
    toolbarColor: '#1976d2',
    brandLight: '#64b5f6'
  },

  // gray
  '#404040': {
    primaryColor: '#404040',
    toolbarColor: '#404040',
    brandLight: '#cfd8dc'
  },

  // black and white
  'black': {
    primaryColor: 'black',
    toolbarColor: 'black',
    brandLight: 'white'
  },

}

const getAppTheme = (overrides, scaling) => getTheme(platform(overrides, scaling));
const buildTheme = (overrides) => {
  return getAppTheme(overrides, sizeScaling);
}

let sizeScaling = parseFloat(recommendedScaling()) || 1;

conf.theme = buildTheme(conf.themes.default);


export default conf;