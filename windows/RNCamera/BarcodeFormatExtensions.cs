using System;
using ZXing;

namespace RNCamera
{
    static class BarcodeFormatExtensions
    {
        public static string GetName(this BarcodeFormat barcodeFormat)
        {
            switch (barcodeFormat)
            {
                case BarcodeFormat.AZTEC:
                    return "aztec";
                case BarcodeFormat.CODE_39:
                    return "code39";
                case BarcodeFormat.CODE_93:
                    return "code93";
                case BarcodeFormat.CODE_128:
                    return "code128";
                case BarcodeFormat.DATA_MATRIX:
                    return "datamatrix";
                case BarcodeFormat.EAN_8:
                    return "ean8";
                case BarcodeFormat.EAN_13:
                    return "ean13";
                case BarcodeFormat.ITF:
                    return "interleaved2of5";
                case BarcodeFormat.PDF_417:
                    return "pdf417";
                case BarcodeFormat.QR_CODE:
                    return "qr";
                case BarcodeFormat.UPC_E:
                    return "upce";
                default:
                    throw new NotImplementedException();
            }
        }
    }
}
