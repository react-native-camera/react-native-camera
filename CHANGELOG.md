### 1.2.0-7 (2018-08-09)

##### Build System / Dependencies

* **change-log:**  v1.1.5-2 ([e49e35a0](https://github.com/react-native-community/react-native-camera/commit/e49e35a085b1793cc8692d2c1600eb2e14ffbe75))

##### Documentation Changes

* **expo:**  explain how to migrate to and from expo camera module ([#1605](https://github.com/react-native-community/react-native-camera/pull/1605)) ([4a9322cb](https://github.com/react-native-community/react-native-camera/commit/4a9322cb8b7d455fc28f7e67a15bff2fd9d7ea3e))

##### New Features

* **preview:**
  *  add android code ([497a7039](https://github.com/react-native-community/react-native-camera/commit/497a703964e925b6e3e62e39a54a9734a7ed6c40))
  *  add new props to JS ([9bf9a2e3](https://github.com/react-native-community/react-native-camera/commit/9bf9a2e3162b919d98cab104029250394b2dd3a8))
  *  add preview methods and more fixes ([b9fb708f](https://github.com/react-native-community/react-native-camera/commit/b9fb708ffc3fd6865191ce6e2bd0a2404a9c657c))

##### Bug Fixes

* **rn-camera:**
  *  fix codec backwards compat ([91f5bf45](https://github.com/react-native-community/react-native-camera/commit/91f5bf45672a8b83253ed17c3f90eee64b0f07bf))
  *  fix types, conversions and casts ([83d0618e](https://github.com/react-native-community/react-native-camera/commit/83d0618e988656dfd9a216b85394ceb5f3a05e9b))
* **picture-size:**
  *  create None default value ([ad87c8e3](https://github.com/react-native-community/react-native-camera/commit/ad87c8e3421f2ff1836674a01cb86deb619cdc4e))
  *  export method and change default value ([9efb7f14](https://github.com/react-native-community/react-native-camera/commit/9efb7f141f8970ad160c852fa837427a79f3d0dc))

##### Other Changes

*  Implement video stabilization mode property for ios ([#1606](https://github.com/react-native-community/react-native-camera/pull/1606)) ([a090faa0](https://github.com/react-native-community/react-native-camera/commit/a090faa09b417afd41af3739ec2b895de9dca6b6))

#### 1.1.5-2 (2018-06-14)

##### Build System / Dependencies

* **change-log:**
  *  v1.1.4-6 ([86bf1d28](https://github.com/react-native-community/react-native-camera/commit/86bf1d284baf64caa94e3815c9ebed5b0e662369))
  *  v1.1.3-5 ([98b18950](https://github.com/react-native-community/react-native-camera/commit/98b1895038ccf47f94d7d27811f3540d3847feb7))
  *  v1.1.2-4 ([4f6b213d](https://github.com/react-native-community/react-native-camera/commit/4f6b213dc63e7ae96c77a1cf1627c14fcda99a94))
  *  v1.1.1-3 ([821a1b24](https://github.com/react-native-community/react-native-camera/commit/821a1b24e6251ad2a9ba9087c9a427a3b20d0778))
  *  v1.1.0 ([01e6c843](https://github.com/react-native-community/react-native-camera/commit/01e6c8434d87f4723feff7fec568028bfb140cb5))
  *  v1.1.0-2 ([deb42144](https://github.com/react-native-community/react-native-camera/commit/deb42144769c3ccc2e593d5dbf586abab244f219))

##### Chores

* **cameraview:**
  *  integrate google's cameraview directly on rncamera? ([d11ed319](https://github.com/react-native-community/react-native-camera/commit/d11ed31917c26df151b4fb46ab166d2921a9ac99))
  *  update camera view ([501ffe83](https://github.com/react-native-community/react-native-camera/commit/501ffe8336b9d8bc9743c1ed803fe20b77f2c270))
* **lint:**
  *  more lint checks ([3bb9a648](https://github.com/react-native-community/react-native-camera/commit/3bb9a6484af306ac66083dd05ac6c46de542f3b4))
  *  fix some warnings ([7967e2fb](https://github.com/react-native-community/react-native-camera/commit/7967e2fbce44b15a77ae0cbddf76f0b37fc530ba))
  *  fix lint to make ci work ([919d07b1](https://github.com/react-native-community/react-native-camera/commit/919d07b162f4a39a2454bebdb387224e21a4ba7a))
* **package:**  enforce no errors on lint and update packages ([00f4f4c1](https://github.com/react-native-community/react-native-camera/commit/00f4f4c13714a9d4e03a2cd76f2b19de7a78cfe4))
* **gms:**  change default gms to 12.0.0 ([94c8968b](https://github.com/react-native-community/react-native-camera/commit/94c8968b2633cfa4e16d1e4275eb831065232014))

##### Documentation Changes

* **expo:**  explain how to migrate to and from expo camera module ([#1605](https://github.com/react-native-community/react-native-camera/pull/1605)) ([4a9322cb](https://github.com/react-native-community/react-native-camera/commit/4a9322cb8b7d455fc28f7e67a15bff2fd9d7ea3e))
* **recipes:**  add some recipes ([ef5c2fef](https://github.com/react-native-community/react-native-camera/commit/ef5c2fef14530110b0c5aec3a044ca27dcfa8d72))

##### New Features

* **preview:**
  *  add android code ([497a7039](https://github.com/react-native-community/react-native-camera/commit/497a703964e925b6e3e62e39a54a9734a7ed6c40))
  *  add new props to JS ([9bf9a2e3](https://github.com/react-native-community/react-native-camera/commit/9bf9a2e3162b919d98cab104029250394b2dd3a8))
  *  add preview methods and more fixes ([b9fb708f](https://github.com/react-native-community/react-native-camera/commit/b9fb708ffc3fd6865191ce6e2bd0a2404a9c657c))
* **types:**
  *  add types for [#1547](https://github.com/react-native-community/react-native-camera/pull/1547) ([#1548](https://github.com/react-native-community/react-native-camera/pull/1548)) ([3ce3c80d](https://github.com/react-native-community/react-native-camera/commit/3ce3c80db670cc05dead7636d70dc8fc911a2c6b))
  *  add types for [#1523](https://github.com/react-native-community/react-native-camera/pull/1523) ([f61004de](https://github.com/react-native-community/react-native-camera/commit/f61004de623a2011e99a6a8092048b513025f5ed))
  *  add types for [#1518](https://github.com/react-native-community/react-native-camera/pull/1518) (FaCC) ([842dc1cb](https://github.com/react-native-community/react-native-camera/commit/842dc1cb581bd28653549dee86f70c2ff5d65ee2))
  *  add types for [#1441](https://github.com/react-native-community/react-native-camera/pull/1441) ([be3e0ebf](https://github.com/react-native-community/react-native-camera/commit/be3e0ebfb8ff42a48211b55054325548cd304694))
  *  add types for [#1428](https://github.com/react-native-community/react-native-camera/pull/1428) ([6cc3d89b](https://github.com/react-native-community/react-native-camera/commit/6cc3d89bec2a55b31c2e7c4f0e597eafc8c31323))
  *  add types for text detection feature ([c0ace2e9](https://github.com/react-native-community/react-native-camera/commit/c0ace2e94c47a9122a386bcbe99911182da80744))
* **rn-camera:**  use and export constants ([c8c6fdea](https://github.com/react-native-community/react-native-camera/commit/c8c6fdea0bf15de60c638f504f38dcb9ac80a3e4))
* **rn_camera:**  add function as children ([45cc8f25](https://github.com/react-native-community/react-native-camera/commit/45cc8f25d2de71b9eee29e1fe14e2f4f3d2feee9))
* **ci:**  add first circleci lint and check script ([ee385eec](https://github.com/react-native-community/react-native-camera/commit/ee385eec05b9be5e1f96524206e50aa96085ce19))
* **android:**  make android gradle check work ([1c7f231a](https://github.com/react-native-community/react-native-camera/commit/1c7f231af460127bebf1f9970367bf64987de34b))
* **play-sound:**  play sound on capture (android) ([69242183](https://github.com/react-native-community/react-native-camera/commit/69242183cc65460040795b866095f34090a9598d))

##### Bug Fixes

* **rn-camera:**
  *  fix codec backwards compat ([91f5bf45](https://github.com/react-native-community/react-native-camera/commit/91f5bf45672a8b83253ed17c3f90eee64b0f07bf))
  *  fix types, conversions and casts ([83d0618e](https://github.com/react-native-community/react-native-camera/commit/83d0618e988656dfd9a216b85394ceb5f3a05e9b))
  *  inject correct status ([858cc4c9](https://github.com/react-native-community/react-native-camera/commit/858cc4c9c8fd456390b274ee4cfddb62fee198ee))
* **picture-size:**
  *  create None default value ([ad87c8e3](https://github.com/react-native-community/react-native-camera/commit/ad87c8e3421f2ff1836674a01cb86deb619cdc4e))
  *  export method and change default value ([9efb7f14](https://github.com/react-native-community/react-native-camera/commit/9efb7f141f8970ad160c852fa837427a79f3d0dc))
* **cache:**  store video recordings in same directory as photos ([bba84a98](https://github.com/react-native-community/react-native-camera/commit/bba84a983446c25f76aa77793f49d4252cd63ea3))
* **rn_camera:**  improve naming ([3811d82c](https://github.com/react-native-community/react-native-camera/commit/3811d82c75ceedc27b8aa5550e352159d5daf2b8))
* **search-paths:**  remove unnecessary search paths and add missing one ([dee298b4](https://github.com/react-native-community/react-native-camera/commit/dee298b4fefca4659468fd43e914fd1c970ca930))
* **styles:**  place style sheet above everything,prevent undefined styles ([01501892](https://github.com/react-native-community/react-native-camera/commit/01501892b5711db765cc367a24ba7c3233678791))
* **warnings:**  remove inline styles ([716c4e38](https://github.com/react-native-community/react-native-camera/commit/716c4e389da45fd7d240a8b4acf60a620fa2c372))
* **barcode:**  better name google variables and correct init ([38e96ed2](https://github.com/react-native-community/react-native-camera/commit/38e96ed24d6b59e108a0ac175eefff22d7b33c27))
* **Android:**  image stretched instead of cropped ([73eb5fd2](https://github.com/react-native-community/react-native-camera/commit/73eb5fd272c28a6369705d30379dcabae3429301))
* **barcode-prop:**  fix default value and add more values ([2c87b44b](https://github.com/react-native-community/react-native-camera/commit/2c87b44b1660f44e9f2bc8e7fce207c872933806))
* **docs:**
  *  move skipProcessing to 'Supported options' ([8054200f](https://github.com/react-native-community/react-native-camera/commit/8054200f81a754ae2d29532b636f55331e996703))
  *  Header on the wrong position ([589a0819](https://github.com/react-native-community/react-native-camera/commit/589a08192930f96aa4f7cf255aa4ac0adfd31a12))
* **types:**  fix types for [#1402](https://github.com/react-native-community/react-native-camera/pull/1402) ([26f9a1e5](https://github.com/react-native-community/react-native-camera/commit/26f9a1e53b3f3b21b86f28d27236849995e7baf9))
* **ios:**  add video output early to avoid underexposed beginning ([9ef5b29a](https://github.com/react-native-community/react-native-camera/commit/9ef5b29ad5d66f0e6d52e504dab00b862148c60f))

##### Other Changes

*  Implement video stabilization mode property for ios ([#1606](https://github.com/react-native-community/react-native-camera/pull/1606)) ([a090faa0](https://github.com/react-native-community/react-native-camera/commit/a090faa09b417afd41af3739ec2b895de9dca6b6))
*  Fix java.lang.ArrayIndexOutOfBoundsException with image rotation ([6ce014d3](https://github.com/react-native-community/react-native-camera/commit/6ce014d3ca3805f908fbdcd30da9b982de3bc2da))

#### 1.1.4-6 (2018-05-21)

#### 1.1.3-5 (2018-05-18)

##### New Features

* **types:**
  *  add types for [#1547](https://github.com/react-native-community/react-native-camera/pull/1547) ([#1548](https://github.com/react-native-community/react-native-camera/pull/1548)) ([3ce3c80d](https://github.com/react-native-community/react-native-camera/commit/3ce3c80db670cc05dead7636d70dc8fc911a2c6b))
  *  add types for [#1523](https://github.com/react-native-community/react-native-camera/pull/1523) ([f61004de](https://github.com/react-native-community/react-native-camera/commit/f61004de623a2011e99a6a8092048b513025f5ed))
  *  add types for [#1518](https://github.com/react-native-community/react-native-camera/pull/1518) (FaCC) ([842dc1cb](https://github.com/react-native-community/react-native-camera/commit/842dc1cb581bd28653549dee86f70c2ff5d65ee2))
* **rn-camera:**  use and export constants ([c8c6fdea](https://github.com/react-native-community/react-native-camera/commit/c8c6fdea0bf15de60c638f504f38dcb9ac80a3e4))
* **rn_camera:**  add function as children ([45cc8f25](https://github.com/react-native-community/react-native-camera/commit/45cc8f25d2de71b9eee29e1fe14e2f4f3d2feee9))

##### Bug Fixes

* **rn-camera:**  inject correct status ([858cc4c9](https://github.com/react-native-community/react-native-camera/commit/858cc4c9c8fd456390b274ee4cfddb62fee198ee))
* **cache:**  store video recordings in same directory as photos ([bba84a98](https://github.com/react-native-community/react-native-camera/commit/bba84a983446c25f76aa77793f49d4252cd63ea3))
* **rn_camera:**  improve naming ([3811d82c](https://github.com/react-native-community/react-native-camera/commit/3811d82c75ceedc27b8aa5550e352159d5daf2b8))

##### Other Changes

*  Fix java.lang.ArrayIndexOutOfBoundsException with image rotation ([6ce014d3](https://github.com/react-native-community/react-native-camera/commit/6ce014d3ca3805f908fbdcd30da9b982de3bc2da))

#### 1.1.2-4 (2018-04-25)

##### Chores

* **cameraview:**  integrate google's cameraview directly on rncamera? ([d11ed319](https://github.com/react-native-community/react-native-camera/commit/d11ed31917c26df151b4fb46ab166d2921a9ac99))

##### Bug Fixes

* **search-paths:**  remove unnecessary search paths and add missing one ([dee298b4](https://github.com/react-native-community/react-native-camera/commit/dee298b4fefca4659468fd43e914fd1c970ca930))

#### 1.1.1-3 (2018-04-15)

##### Build System / Dependencies

* **change-log:**  v1.1.0 ([01e6c843](https://github.com/react-native-community/react-native-camera/commit/01e6c8434d87f4723feff7fec568028bfb140cb5))

##### Chores

* **lint:**
  *  more lint checks ([3bb9a648](https://github.com/react-native-community/react-native-camera/commit/3bb9a6484af306ac66083dd05ac6c46de542f3b4))
  *  fix some warnings ([7967e2fb](https://github.com/react-native-community/react-native-camera/commit/7967e2fbce44b15a77ae0cbddf76f0b37fc530ba))
  *  fix lint to make ci work ([919d07b1](https://github.com/react-native-community/react-native-camera/commit/919d07b162f4a39a2454bebdb387224e21a4ba7a))
* **package:**  enforce no errors on lint and update packages ([00f4f4c1](https://github.com/react-native-community/react-native-camera/commit/00f4f4c13714a9d4e03a2cd76f2b19de7a78cfe4))

##### New Features

* **ci:**  add first circleci lint and check script ([ee385eec](https://github.com/react-native-community/react-native-camera/commit/ee385eec05b9be5e1f96524206e50aa96085ce19))
* **android:**  make android gradle check work ([1c7f231a](https://github.com/react-native-community/react-native-camera/commit/1c7f231af460127bebf1f9970367bf64987de34b))

##### Bug Fixes

* **styles:**  place style sheet above everything,prevent undefined styles ([01501892](https://github.com/react-native-community/react-native-camera/commit/01501892b5711db765cc367a24ba7c3233678791))
* **warnings:**  remove inline styles ([716c4e38](https://github.com/react-native-community/react-native-camera/commit/716c4e389da45fd7d240a8b4acf60a620fa2c372))

### 1.1.0-2 (2018-04-15)

##### Chores

* **gms:**  change default gms to 12.0.0 ([94c8968b](https://github.com/react-native-community/react-native-camera/commit/94c8968b2633cfa4e16d1e4275eb831065232014))
* **cameraview:**  update camera view ([501ffe83](https://github.com/react-native-community/react-native-camera/commit/501ffe8336b9d8bc9743c1ed803fe20b77f2c270))

##### Documentation Changes

* **recipes:**  add some recipes ([ef5c2fef](https://github.com/react-native-community/react-native-camera/commit/ef5c2fef14530110b0c5aec3a044ca27dcfa8d72))

##### New Features

* **types:**
  *  add types for [#1441](https://github.com/react-native-community/react-native-camera/pull/1441) ([be3e0ebf](https://github.com/react-native-community/react-native-camera/commit/be3e0ebfb8ff42a48211b55054325548cd304694))
  *  add types for [#1428](https://github.com/react-native-community/react-native-camera/pull/1428) ([6cc3d89b](https://github.com/react-native-community/react-native-camera/commit/6cc3d89bec2a55b31c2e7c4f0e597eafc8c31323))
  *  add types for text detection feature ([c0ace2e9](https://github.com/react-native-community/react-native-camera/commit/c0ace2e94c47a9122a386bcbe99911182da80744))
* **play-sound:**  play sound on capture (android) ([69242183](https://github.com/react-native-community/react-native-camera/commit/69242183cc65460040795b866095f34090a9598d))

##### Bug Fixes

* **barcode:**  better name google variables and correct init ([38e96ed2](https://github.com/react-native-community/react-native-camera/commit/38e96ed24d6b59e108a0ac175eefff22d7b33c27))
* **Android:**  image stretched instead of cropped ([73eb5fd2](https://github.com/react-native-community/react-native-camera/commit/73eb5fd272c28a6369705d30379dcabae3429301))
* **barcode-prop:**  fix default value and add more values ([2c87b44b](https://github.com/react-native-community/react-native-camera/commit/2c87b44b1660f44e9f2bc8e7fce207c872933806))
* **docs:**
  *  move skipProcessing to 'Supported options' ([8054200f](https://github.com/react-native-community/react-native-camera/commit/8054200f81a754ae2d29532b636f55331e996703))
  *  Header on the wrong position ([589a0819](https://github.com/react-native-community/react-native-camera/commit/589a08192930f96aa4f7cf255aa4ac0adfd31a12))
* **types:**  fix types for [#1402](https://github.com/react-native-community/react-native-camera/pull/1402) ([26f9a1e5](https://github.com/react-native-community/react-native-camera/commit/26f9a1e53b3f3b21b86f28d27236849995e7baf9))
* **ios:**  add video output early to avoid underexposed beginning ([9ef5b29a](https://github.com/react-native-community/react-native-camera/commit/9ef5b29ad5d66f0e6d52e504dab00b862148c60f))

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
