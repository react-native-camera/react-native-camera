#### 1.0.3-1 (2018-03-24)

##### Chores

*  restored original CameraModule.java ([7bea109e](https://github.com/react-native-community/react-native-camera/commit/7bea109e47a5b7302069f9774a4c7fb2d1652275))

##### Documentation Changes

* **rncamera:**
  *  specifying onTextRecognized callback prototype ([48611212](https://github.com/react-native-community/react-native-camera/commit/48611212f56eed8d9594693c84fe3f00cbb8448b))
  *  docs for text recognition usage ([68639b82](https://github.com/react-native-community/react-native-camera/commit/68639b82ed98ef53ac1a0cc1762c35c5941b61b6))
* **codec:**  document ios codec option ([2b9d8db2](https://github.com/react-native-community/react-native-camera/commit/2b9d8db21389af624fd7ee3fe0eafa8348a3b776))

##### New Features

* **chore:**  try to automate changelog ([cc5f6e62](https://github.com/react-native-community/react-native-camera/commit/cc5f6e62eb78a7de884a3b770eaa12c03a626721))
* **android:**
  *  integrating Google Vision's text recognition ([fcaa9452](https://github.com/react-native-community/react-native-camera/commit/fcaa9452865247ba8aa63e6fd323bd86ea0f7401))

* **Android:**
* **types:**  update types for video recording codec ([f9252254](https://github.com/react-native-community/react-native-camera/commit/f925225484ca1599652039b612fc7deba635de6f))
* **rn-camera:**  add codec option for ios ([c0d5aabf](https://github.com/react-native-community/react-native-camera/commit/c0d5aabf0b32f71326ff153d31e3cb5c588062da))

##### Bug Fixes

* **typo:**  fix typo on package.json ([706278d8](https://github.com/react-native-community/react-native-camera/commit/706278d807edac5bc9eb606e29b3326790d7816c))
* **textrecognition:**  height of text block ([01e763b1](https://github.com/react-native-community/react-native-camera/commit/01e763b1430cdb65d82c78c08a5215da65706e6d))
*  issue [#1246](https://github.com/react-native-community/react-native-camera/pull/1246) - torch will be disabled when starting the record ([8c696017](https://github.com/react-native-community/react-native-camera/commit/8c6960178922492bf49fc44fbab25b638209dc4e))
* **ios-project:**  fix path to parent's ios project ([4496c321](https://github.com/react-native-community/react-native-camera/commit/4496c3217195853a36c261415f126140ddebbcc4))

#### 1.0.2 (2018-03-10)

##### Chores

* **flow:**  add missing types to Picture options ([6bff4d93](https://github.com/react-native-community/react-native-camera/commit/6bff4d935ac421f4aea395c58f5916df78cdae0a))
* **types:**  add new keys to TakePictureOptions ([cc272036](https://github.com/react-native-community/react-native-camera/commit/cc272036581f68dbdce1b596644a158a42c471dc))
* **face-detector:**  make face detection stoppage smoother ([3b3c38dd](https://github.com/react-native-community/react-native-camera/commit/3b3c38dd7d08edd1dad3b6c7fb944515fcb1e9c4))

##### New Features

* **types:**
  *  add FaceDetector declarations ([ba218750](https://github.com/react-native-community/react-native-camera/commit/ba21875001df2e260feb87d71411ff89fe6942ea))
  *  add TypeScript definition files ([a94bad5e](https://github.com/react-native-community/react-native-camera/commit/a94bad5e3739927dd50b850f68ed57a59f782e99))

##### Bug Fixes

* **types:**
  *  fix onBarCodeRead type ([a9947b47](https://github.com/react-native-community/react-native-camera/commit/a9947b47d569227ed6b83ef2988a8cbd3e6b7b41))
  *  fix definition for RNCameraProps.ratio ([4d1616c5](https://github.com/react-native-community/react-native-camera/commit/4d1616c57a059127db07f52ca18a8b092ba559ad))
* **android-camera:**  revert to old camera api ([8d9c06ad](https://github.com/react-native-community/react-native-camera/commit/8d9c06ad903b40abc8bef67927d4621c494aeb3b))

#### 1.0.1 (2018-02-14)

##### New Features

* **release-script:**  add script to package json ([b0503dc8](https://github.com/react-native-community/react-native-camera/commit/b0503dc8aefc1d2a992c1778e00c5d0f8dfd6901))
* **changelog:**  add changelog script ([d2263937](https://github.com/react-native-community/react-native-camera/commit/d226393783748f973cc99032343fc55e45828717))
* **mirror:**  add option to give "mirrorImage" flag to takePicture. ([0b6f0abd](https://github.com/react-native-community/react-native-camera/commit/0b6f0abda07b8a9ff3daa1722a254087f30eec08))

##### Bug Fixes

* **focusWarning:**  fix focus depth warning being shown for ios. ([79698b81](https://github.com/react-native-community/react-native-camera/commit/79698b815b44507037a6e89fda40b5c505703c00))
* **imports:**  delete some useless imports which may cause problems ([a5b9f7e7](https://github.com/react-native-community/react-native-camera/commit/a5b9f7e717bc11aad9a8e5d9e9a449ad7fd9c9fa))

### master

### 1.0.0
- RNCamera as main camera implementation for both iOS and Android (base on expo module)
- FaceDetector feature for both iOS and Android (based on expo module)
- RCTCamera deprecated

### 0.13.0
- added RNCamera implementation for android
- added FaceDetector for android
