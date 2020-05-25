/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 *
 * @format
 * @flow strict-local
 */

import React from 'react';
import {SafeAreaView} from 'react-native';
import TestSuite from 'react-native-tests';

import {tests} from './tests';

const App = () => (
  <SafeAreaView>
    <TestSuite tests={tests} />
  </SafeAreaView>
);

export default App;
