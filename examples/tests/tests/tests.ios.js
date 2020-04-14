/**
 * @format
 * @flow strict-local
 */

import {RNCamera} from 'react-native-camera';

export const constants = tester => {
  const {describe, it, expect} = tester;
  describe('Constants', () => {
    it('should match Constants', () => {
      expect(RNCamera.Constants).toEqual({
        AutoFocus: {off: 0, on: 2},
        BarCodeType: {
          aztec: 'org.iso.Aztec',
          code128: 'org.iso.Code128',
          code39: 'org.iso.Code39',
          code39mod43: 'org.iso.Code39Mod43',
          code93: 'com.intermec.Code93',
          datamatrix: 'org.iso.DataMatrix',
          ean13: 'org.gs1.EAN-13',
          ean8: 'org.gs1.EAN-8',
          interleaved2of5: 'org.ansi.Interleaved2of5',
          itf14: 'org.gs1.ITF14',
          pdf417: 'org.iso.PDF417',
          qr: 'org.iso.QRCode',
          upc_e: 'org.gs1.UPC-E',
        },
        CameraStatus: {
          NOT_AUTHORIZED: 'NOT_AUTHORIZED',
          PENDING_AUTHORIZATION: 'PENDING_AUTHORIZATION',
          READY: 'READY',
        },
        FaceDetection: {},
        FlashMode: {auto: 2, off: 0, on: 1, torch: 3},
        GoogleVisionBarcodeDetection: {BarcodeType: {}},
        Orientation: {
          auto: 'auto',
          landscapeLeft: 'landscapeLeft',
          landscapeRight: 'landscapeRight',
          portrait: 'portrait',
          portraitUpsideDown: 'portraitUpsideDown',
        },
        RecordAudioPermissionStatus: {
          AUTHORIZED: 'AUTHORIZED',
          NOT_AUTHORIZED: 'NOT_AUTHORIZED',
          PENDING_AUTHORIZATION: 'PENDING_AUTHORIZATION',
        },
        Type: {back: 1, front: 2},
        VideoCodec: {
          AppleProRes422: 'apcn',
          AppleProRes4444: 'ap4h',
          H264: 'avc1',
          HVEC: 'hvc1',
          JPEG: 'jpeg',
        },
        VideoQuality: {
          '1080p': 1,
          '2160p': 0,
          '288p': 4,
          '480p': 3,
          '4:3': 3,
          '720p': 2,
        },
        VideoStabilization: {auto: -1, cinematic: 2, off: 0, standard: 1},
        WhiteBalance: {
          auto: 0,
          cloudy: 2,
          fluorescent: 6,
          incandescent: 5,
          shadow: 4,
          sunny: 1,
        },
      });
    });
  });
};

export const conversionTables = tester => {
  const {describe, it, expect} = tester;
  describe('ConversionTables', () => {
    it('should match ConversionsTables', () => {
      expect(RNCamera.ConversionTables).toEqual({
        autoFocus: {off: 0, on: 2},
        exposure: undefined,
        faceDetectionClassifications: undefined,
        faceDetectionLandmarks: undefined,
        faceDetectionMode: undefined,
        flashMode: {auto: 2, off: 0, on: 1, torch: 3},
        googleVisionBarcodeType: {},
        type: {back: 1, front: 2},
        videoStabilizationMode: {auto: -1, cinematic: 2, off: 0, standard: 1},
        whiteBalance: {
          auto: 0,
          cloudy: 2,
          fluorescent: 6,
          incandescent: 5,
          shadow: 4,
          sunny: 1,
        },
      });
    });
  });
};

export const tests = [constants, conversionTables];
