/**
 * @format
 * @flow strict-local
 */

import * as React from 'react';

import {imports} from './imports';
import {photos} from './photos';
import {videos} from './videos';

export const tests = [
  (tester, render) => {
    const {describe} = tester;

    const renderCamera = (props = {}) => element =>
      render({
        Component: React.cloneElement(element, {
          style: {height: 100, width: 100},
          ratio: '16:9',
          ...props,
        }),
        waitFor: 'onCameraReady',
      });

    describe('imports', () => imports(tester, renderCamera()));

    describe('useCamera2Api={true}, type="front"', () => {
      const props = {useCamera2Api: true, type: 'front'};

      photos(tester, renderCamera(props));
      videos(tester, renderCamera(props));
    });

    describe('useCamera2Api={true}, type="back"', () => {
      const props = {useCamera2Api: true, type: 'back'};

      photos(tester, renderCamera(props));
      videos(tester, renderCamera(props));
    });

    describe('type="front"', () => {
      const props = {type: 'front'};

      photos(tester, renderCamera(props));
      videos(tester, renderCamera(props));
    });

    describe('type="back"', () => {
      const props = {type: 'back'};

      photos(tester, renderCamera(props));
      videos(tester, renderCamera(props));
    });
  },
];
