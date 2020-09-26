/**
 * @format
 * @flow strict-local
 */

import * as React from 'react';
import {Platform} from 'react-native';
import {RNCamera} from 'react-native-camera';

export const videos = ({describe, it, expect, createSpy, any}, render) => {
  describe('getSupportedRatiosAsync', () => {
    if (Platform.OS === 'android') {
      it('should resolve to an array of strings', async () => {
        const camera = await render(<RNCamera />);
        const ratios = await camera.getSupportedRatiosAsync();

        expect(ratios).toBeInstanceOf(Array);
        expect(ratios.length).toBeGreaterThan(0);
      });
    }

    if (Platform.OS === 'ios') {
      it('should throw an error', async () => {
        const camera = await render(<RNCamera />);
        expect(async () => await camera.getSupportedRatiosAsync()).toThrowError(
          'Ratio is not supported on iOS',
        );
      });
    }
  });

  describe('isRecording', () => {
    if (Platform.OS === 'android') {
      it('should throw an error', async () => {
        const camera = await render(<RNCamera />);
        expect(() => camera.isRecording()).toThrowError();
      });
    }

    if (Platform.OS === 'ios') {
      it('should indicate recording status', async () => {
        const camera = await render(<RNCamera />);
        expect(await camera.isRecording()).toBe(false);
        camera.recordAsync();
        expect(await camera.isRecording()).toBe(true);
        camera.stopRecording();
      });
    }
  });

  describe('recordAsync', () => {
    it('should call onRecording(Start|End) callbacks', async done => {
      const onRecordingStart = createSpy('onRecordingStart', () =>
        camera.stopRecording(),
      ).and.callThrough();
      const onRecordingEnd = createSpy('onRecordingEnd', () => {
        expect(onRecordingStart).toHaveBeenCalledTimes(1);
        expect(onRecordingEnd).toHaveBeenCalledTimes(1);

        done();
      }).and.callThrough();

      const camera = await render(
        <RNCamera
          onRecordingStart={onRecordingStart}
          onRecordingEnd={onRecordingEnd}
        />,
      );

      camera.recordAsync();
    });

    it('should resolve to a RecordResponse', async () => {
      const camera = await render(
        <RNCamera onRecordingStart={() => camera.stopRecording()} />,
      );
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
