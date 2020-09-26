/**
 * @format
 * @flow strict-local
 */

import * as React from 'react';
import {RNCamera} from 'react-native-camera';

const waitFor = delay => new Promise(resolve => setTimeout(resolve, delay));

export const photos = (
  {describe, it, expect, createSpy, any, objectContaining},
  render,
) => {
  describe('getAvailablePictureSizes', () => {
    it('should resolve to an array of strings', async () => {
      const camera = await render(<RNCamera />);
      const ratios = await camera.getAvailablePictureSizes();

      expect(ratios).toBeInstanceOf(Array);
      expect(ratios.length).toBeGreaterThan(0);
    });
  });

  describe('takePictureAsync', () => {
    it('should call onPictureTaken callback', async () => {
      const onPictureTaken = createSpy('onPictureTaken');
      const camera = await render(<RNCamera onPictureTaken={onPictureTaken} />);
      await waitFor(1000); // camera is still not ready? https://stackoverflow.com/a/40904906/7386122
      await camera.takePictureAsync();
      await waitFor(1000);

      expect(onPictureTaken).toHaveBeenCalledTimes(1);
    });

    it('should resolve to a TakePictureResponse', async () => {
      const camera = await render(<RNCamera />);
      await waitFor(1000); // camera is still not ready? https://stackoverflow.com/a/40904906/7386122

      const orientation = 1;
      const width = 100;
      const response = await camera.takePictureAsync({orientation, width});

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
    }, 10000);
  });
};
