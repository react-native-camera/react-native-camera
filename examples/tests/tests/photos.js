/**
 * @format
 * @flow strict-local
 */
import * as React from 'react';
import {RNCamera} from 'react-native-camera';

const waitFor = delay => new Promise(resolve => setTimeout(resolve, delay));

export const photos = (tester, render) => {
  const {describe, it, expect, createSpy, any, objectContaining} = tester;
  const renderCamera = props =>
    render({
      Component: (
        <RNCamera
          type={RNCamera.Constants.Type.front}
          style={{height: 100, width: 100}}
          {...props}
        />
      ),
      waitFor: 'onCameraReady',
    });

  describe('getAvailablePictureSizes', () => {
    it('should resolve to an array of strings', async () => {
      const camera = await renderCamera();
      const ratios = await camera.getAvailablePictureSizes();

      expect(ratios).toBeInstanceOf(Array);
      expect(ratios.length).toBeGreaterThan(0);
    });
  });

  describe('takePictureAsync', () => {
    it('should call onPictureTaken callback', async () => {
      const onPictureTaken = createSpy('onPictureTaken');
      const camera = await renderCamera({onPictureTaken});
      await camera.takePictureAsync();
      await waitFor(500);
      expect(onPictureTaken).toHaveBeenCalledTimes(1);
    });

    it('should resolve to a TakePictureResponse', async () => {
      const camera = await renderCamera();

      const orientation = 1;
      const width = 100;
      const response = await camera.takePictureAsync({
        orientation,
        width,
      });

      expect(response).toEqual({
        width,
        height: any(Number),
        uri: any(String),
        pictureOrientation: orientation,
        deviceOrientation: any(Number),
      });
      expect(response.uri).toMatch(/file:\/\/\/*/);

      const additionalExif = {foo: 'bar'};
      const responseWithOptionals = await camera.takePictureAsync({
        exif: true,
        base64: true,
        writeExif: additionalExif,
      });

      expect(responseWithOptionals).toEqual({
        width: any(Number),
        height: any(Number),
        uri: any(String),
        pictureOrientation: any(Number),
        deviceOrientation: any(Number),
        base64: any(String),
        exif: objectContaining(additionalExif),
      });
    });
  });
};
