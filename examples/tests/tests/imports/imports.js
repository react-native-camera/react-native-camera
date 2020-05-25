/**
 * @format
 * @flow strict-local
 */

import {RNCamera} from 'react-native-camera';
import {Platform} from 'react-native';

export const imports = tester => {
  const {describe, it, expect} = tester;

  describe('Constants', () => {
    if (Platform.OS === 'android') {
      it('should match Constants', () => {
        expect(RNCamera.Constants).toEqual({
          AutoFocus: {off: false, on: true},
          BarCodeType: {
            aztec: 'AZTEC',
            codabar: 'CODABAR',
            code128: 'CODE_128',
            code39: 'CODE_39',
            code93: 'CODE_93',
            datamatrix: 'DATA_MATRIX',
            ean13: 'EAN_13',
            ean8: 'EAN_8',
            interleaved2of5: 'ITF',
            maxicode: 'MAXICODE',
            pdf417: 'PDF_417',
            qr: 'QR_CODE',
            rss14: 'RSS_14',
            rssexpanded: 'RSS_EXPANDED',
            upc_a: 'UPC_A',
            upc_e: 'UPC_E',
            upc_ean: 'UPC_EAN_EXTENSION',
          },
          CameraStatus: {
            NOT_AUTHORIZED: 'NOT_AUTHORIZED',
            PENDING_AUTHORIZATION: 'PENDING_AUTHORIZATION',
            READY: 'READY',
          },
          FaceDetection: {
            Classifications: {all: 1, none: 0},
            Landmarks: {all: 1, none: 0},
            Mode: {accurate: 1, fast: 0},
          },
          FlashMode: {auto: 3, off: 0, on: 1, torch: 2},
          GoogleVisionBarcodeDetection: {
            BarcodeMode: {ALTERNATE: 1, INVERTED: 2, NORMAL: 0},
            BarcodeType: {
              ALL: 0,
              AZTEC: 4096,
              CALENDAR_EVENT: 11,
              CONTACT_INFO: 1,
              DATA_MATRIX: 16,
              DRIVER_LICENSE: 12,
              EAN_13: 32,
              EAN_8: 64,
              EMAIL: 2,
              GEO: 10,
              ISBN: 3,
              ITF: 128,
              None: -1,
              PDF417: 2048,
              PHONE: 4,
              PRODUCT: 5,
              QR_CODE: 256,
              SMS: 6,
              TEXT: 7,
              UPC_A: 512,
              UPC_E: 1024,
              URL: 8,
              WIFI: 9,
            },
          },
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
          Type: {back: 0, front: 1},
          VideoCodec: undefined,
          VideoQuality: {
            '1080p': 1,
            '2160p': 0,
            '480p': 3,
            '4:3': 4,
            '720p': 2,
          },
          VideoStabilization: undefined,
          WhiteBalance: {
            auto: 0,
            cloudy: 1,
            fluorescent: 4,
            incandescent: 5,
            shadow: 3,
            sunny: 2,
          },
        });
      });
    }

    if (Platform.OS === 'ios') {
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
    }
  });

  describe('ConversionTables', () => {
    if (Platform.OS === 'android') {
      it('should match ConversionsTables', () => {
        expect(RNCamera.ConversionTables).toEqual({
          autoFocus: {off: false, on: true},
          exposure: undefined,
          faceDetectionClassifications: {all: 1, none: 0},
          faceDetectionLandmarks: {all: 1, none: 0},
          faceDetectionMode: {accurate: 1, fast: 0},
          flashMode: {auto: 3, off: 0, on: 1, torch: 2},
          googleVisionBarcodeType: {
            ALL: 0,
            AZTEC: 4096,
            CALENDAR_EVENT: 11,
            CONTACT_INFO: 1,
            DATA_MATRIX: 16,
            DRIVER_LICENSE: 12,
            EAN_13: 32,
            EAN_8: 64,
            EMAIL: 2,
            GEO: 10,
            ISBN: 3,
            ITF: 128,
            None: -1,
            PDF417: 2048,
            PHONE: 4,
            PRODUCT: 5,
            QR_CODE: 256,
            SMS: 6,
            TEXT: 7,
            UPC_A: 512,
            UPC_E: 1024,
            URL: 8,
            WIFI: 9,
          },
          type: {back: 0, front: 1},
          videoStabilizationMode: {},
          whiteBalance: {
            auto: 0,
            cloudy: 1,
            fluorescent: 4,
            incandescent: 5,
            shadow: 3,
            sunny: 2,
          },
        });
      });
    }

    if (Platform.OS === 'ios') {
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
    }
  });
};
