/**
 * @format
 * @flow strict-local
 */
import * as React from 'react';
import {RNCamera} from 'react-native-camera';

const waitFor = delay => new Promise(resolve => setTimeout(resolve, delay));

export const videos = (tester, render) => {
  const {describe, it, expect, createSpy, any} = tester;
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

  describe('getSupportedRatiosAsync', () => {
    if (Platform.OS === 'android') {
      it('should resolve to an array of strings', async () => {
        const camera = await renderCamera();
        const ratios = await camera.getSupportedRatiosAsync();

        expect(ratios).toBeInstanceOf(Array);
        expect(ratios.length).toBeGreaterThan(0);
      });
    }

    if (Platform.OS === 'ios') {
      it('should throw an error', async () => {
        const camera = await renderCamera();
        expect(async () => await camera.getSupportedRatiosAsync()).toThrowError(
          'Ratio is not supported on iOS',
        );
      });
    }
  });

  describe('isRecording', () => {
    if (Platform.OS === 'android') {
      it('should throw an error', async () => {
        const camera = await renderCamera();
        expect(() => camera.isRecording()).toThrowError();
      });
    }

    if (Platform.OS === 'ios') {
      it('should indicate recording status', async () => {
        const camera = await renderCamera();
        expect(await camera.isRecording()).toBe(false);
        camera.recordAsync();
        expect(await camera.isRecording()).toBe(true);
        camera.stopRecording();
      });
    }
  });

  describe('recordAsync', () => {
    it('should call onRecording(Start|End) callbacks', async () => {
      const onRecordingStart = createSpy('onRecordingStart');
      const onRecordingEnd = createSpy('onRecordingEnd');
      const camera = await renderCamera({
        onRecordingStart,
        onRecordingEnd,
      });

      setTimeout(() => camera.stopRecording(), 100);
      await camera.recordAsync();
      await waitFor(500);

      expect(onRecordingStart).toHaveBeenCalledTimes(1);
      expect(onRecordingEnd).toHaveBeenCalledTimes(1);
    });

    it('should resolve to a RecordResponse', async () => {
      const camera = await renderCamera();
      setTimeout(() => camera.stopRecording(), 100);
      const response = await camera.recordAsync();

      expect(response).toEqual({
        uri: any(String),
        videoOrientation: any(Number),
        deviceOrientation: any(Number),
        isRecordingInterrupted: any(Boolean),
      });
      expect(response.uri).toMatch(/file:\/\/\/*/);
    });
  });
};
