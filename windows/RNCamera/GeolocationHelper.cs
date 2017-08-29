using System;
using Windows.Foundation.Collections;

namespace RNCamera
{
    static class GeolocationHelper
    {
        public static PropertySet GetLatitudeProperties(double latitude)
        {
            // Latitude and longitude are returned as double precision numbers,
            // but we want to convert to degrees/minutes/seconds format.
            var latRefText = (latitude >= 0) ? "N" : "S";
            var latDeg = Math.Floor(Math.Abs(latitude));
            var latMin = Math.Floor((Math.Abs(latitude) - latDeg) * 60);
            var latSec = (Math.Abs(latitude) - latDeg - latMin / 60) * 3600;

            uint[] latitudeNumerator =
            {
                (uint)latDeg,
                (uint)latMin,
                (uint)(latSec * 10000)
            };

            uint[] denominator =
            {
                1,
                1,
                10000
            };

            return new PropertySet
            {
                { "System.GPS.LatitudeRef", latRefText },
                { "System.GPS.LatitudeNumerator", latitudeNumerator },
                { "System.GPS.LatitudeDenominator", denominator },
            };
        }

        public static PropertySet GetLongitudeProperties(double longitude)
        {
            // Latitude and longitude are returned as double precision numbers,
            // but we want to convert to degrees/minutes/seconds format.
            var longRefText = (longitude >= 0) ? "E" : "W";
            var longDeg = Math.Floor(Math.Abs(longitude));
            var longMin = Math.Floor((Math.Abs(longitude) - longDeg) * 60);
            var longSec = (Math.Abs(longitude) - longDeg - longMin / 60) * 3600;

            uint[] longitudeNumerator =
            {
                (uint)longDeg,
                (uint)longMin,
                (uint)(longSec * 10000)
            };

            uint[] denominator =
            {
                1,
                1,
                10000
            };

            var pset = new PropertySet();

            return new PropertySet
            {
                { "System.GPS.LongitudeRef", longRefText },
                { "System.GPS.LongitudeNumerator", longitudeNumerator },
                { "System.GPS.LongitudeDenominator", denominator },
            };
        }
    }
}
