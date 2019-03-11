/**
 * This file overrides metro config so
 */
'use strict';

const path = require('path');
const blacklist = require('metro-config/src/defaults/blacklist');

const reactNativeCameraRoot = path.resolve(__dirname, '..', '..');

module.exports = {
  watchFolders: [path.resolve(__dirname, 'node_modules'), reactNativeCameraRoot],
  resolver: {
    blacklistRE: blacklist([new RegExp(`${reactNativeCameraRoot}/examples/mlkit/.*`), new RegExp(`${reactNativeCameraRoot}/node_modules/react-native/.*`)]),
  },
};
