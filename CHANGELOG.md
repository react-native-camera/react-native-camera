#### 1.0.3-1 (2018-03-24)

##### Chores

*  restored original CameraModule.java ([7bea109e](https://github.com/react-native-community/react-native-camera/commit/7bea109e47a5b7302069f9774a4c7fb2d1652275))
*  don't save demo image to disk ([17546355](https://github.com/react-native-community/react-native-camera/commit/17546355de7e9942adf4996cf8fff372f64091a5))
*  added lbpcascade-file ([587f0ea6](https://github.com/react-native-community/react-native-camera/commit/587f0ea6904295aecdd10f79aea5e6d326b58e36))

##### Documentation Changes

* **rncamera:**
  *  specifying onTextRecognized callback prototype ([48611212](https://github.com/react-native-community/react-native-camera/commit/48611212f56eed8d9594693c84fe3f00cbb8448b))
  *  docs for text recognition usage ([68639b82](https://github.com/react-native-community/react-native-camera/commit/68639b82ed98ef53ac1a0cc1762c35c5941b61b6))
* **codec:**  document ios codec option ([2b9d8db2](https://github.com/react-native-community/react-native-camera/commit/2b9d8db21389af624fd7ee3fe0eafa8348a3b776))

##### New Features

* **chore:**  try to automate changelog ([cc5f6e62](https://github.com/react-native-community/react-native-camera/commit/cc5f6e62eb78a7de884a3b770eaa12c03a626721))
* **android:**
  *  integrating Google Vision's text recognition ([fcaa9452](https://github.com/react-native-community/react-native-camera/commit/fcaa9452865247ba8aa63e6fd323bd86ea0f7401))
  *  Face Detection Improvements ([04b8e276](https://github.com/react-native-community/react-native-camera/commit/04b8e27681732c09bd9bd6b467d6b8df28242a20))
  *  added opencv framework compile reference and minor improvements ([58f8e0fe](https://github.com/react-native-community/react-native-camera/commit/58f8e0fef9dcf8a1c8e69aef73c0fd3b6c2b82dc))
  *  added and refactored lost files ([679a5d29](https://github.com/react-native-community/react-native-camera/commit/679a5d29bb59a5b2f631204d1caa0763017e5f33))
  *  add code for face detection ([32c5385b](https://github.com/react-native-community/react-native-camera/commit/32c5385bc34d13b894c1a14023f5040db64177f4))
* **Android:**
  *  event will also be fired if no object was found ([519fe42f](https://github.com/react-native-community/react-native-camera/commit/519fe42f074328440503c3c7f34a69d4aec29ecb))
  *  add TextBlock-Detection via new prop objectsToDetect ([5dfa6d2d](https://github.com/react-native-community/react-native-camera/commit/5dfa6d2d433005610abcdd2f58258bfbfb3c9ac6))
* **types:**  update types for video recording codec ([f9252254](https://github.com/react-native-community/react-native-camera/commit/f925225484ca1599652039b612fc7deba635de6f))
* **rn-camera:**  add codec option for ios ([c0d5aabf](https://github.com/react-native-community/react-native-camera/commit/c0d5aabf0b32f71326ff153d31e3cb5c588062da))
* **iOS:**
  *  finetuning ([7acd1f23](https://github.com/react-native-community/react-native-camera/commit/7acd1f23dbedbd43ad5fa0a541b95a6931667aef))
  *  improve text-block-algorithm ([39477dd9](https://github.com/react-native-community/react-native-camera/commit/39477dd91d190c8ed6318ce8c583f2497a5ee57f))
*  added Text-Block detection ([9af22643](https://github.com/react-native-community/react-native-camera/commit/9af226430fd6c4181638fbffcf5916b6e99127a5))
*  added property expectedFaceOrientation and handle it together with image orientation depending on used camera ([47780e54](https://github.com/react-native-community/react-native-camera/commit/47780e5485c4a13906c9bd2cf9a3c7198cd45eff))
*  added package and class structures for opencv face detection ([917692c1](https://github.com/react-native-community/react-native-camera/commit/917692c16ee5677f186c5205fa980a57010052bf))
*  Face Detection Orientation added ([00d1a5f8](https://github.com/react-native-community/react-native-camera/commit/00d1a5f84bc33645c6752e6d225a7dd1744fe195))
*  added orientation of face to callback ([8067d46a](https://github.com/react-native-community/react-native-camera/commit/8067d46ab95b894fb74fb1191f0bce9705903e36))
*  improved face detector and callback ([fa913bd4](https://github.com/react-native-community/react-native-camera/commit/fa913bd4ccdb733390c17c865ebfa7bc58269e13))
*  delegate back and dispatch event once faces were detected ([4019eb28](https://github.com/react-native-community/react-native-camera/commit/4019eb28d4306babcd2f48d9c6553e4c29ab9157))
*  added seperate files for face detection ([55da3e4e](https://github.com/react-native-community/react-native-camera/commit/55da3e4ec37228bfee2a617c9598b9dce9150645))
*  added faceDetection via openCV ([ae478436](https://github.com/react-native-community/react-native-camera/commit/ae478436deb56ca22d1d31ba45f98aecb8b361e1))
*  added a dummy AVCaptureVideoDataOutputSampleBufferDelegate ([e10c5450](https://github.com/react-native-community/react-native-camera/commit/e10c545054444855d00c751415a45fe75aac8e72))

##### Bug Fixes

* **typo:**  fix typo on package.json ([706278d8](https://github.com/react-native-community/react-native-camera/commit/706278d807edac5bc9eb606e29b3326790d7816c))
* **textrecognition:**  height of text block ([01e763b1](https://github.com/react-native-community/react-native-camera/commit/01e763b1430cdb65d82c78c08a5215da65706e6d))
*  issue [#1246](https://github.com/react-native-community/react-native-camera/pull/1246) - torch will be disabled when starting the record ([8c696017](https://github.com/react-native-community/react-native-camera/commit/8c6960178922492bf49fc44fbab25b638209dc4e))
* **ios-project:**  fix path to parent's ios project ([4496c321](https://github.com/react-native-community/react-native-camera/commit/4496c3217195853a36c261415f126140ddebbcc4))

##### Other Changes

*  typo ([236dc200](https://github.com/react-native-community/react-native-camera/commit/236dc2006aba32d5aa780eb64d18a272ddb30f54))
*  fixed last commit ([7efbd506](https://github.com/react-native-community/react-native-camera/commit/7efbd50676095a8f8037b7063203b193826c548b))
*  remove mistakenly added references ([199ce9da](https://github.com/react-native-community/react-native-camera/commit/199ce9daef54459cdcc492d6aba04532d8fda5bc))
*  remove references to FaceDetector so that the VideoDataOutput may be used by own code ([9f1d9bd1](https://github.com/react-native-community/react-native-camera/commit/9f1d9bd1fe387d56c4defa92057e366b6c34d705))

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
