using Newtonsoft.Json.Linq;
using System.Collections.Generic;

namespace RNCamera
{
    static class JObjectExtensions
    {
        public static bool ContainsKey(this JObject json, string key)
        {
            return ((IDictionary<string, JToken>)json).ContainsKey(key);
        }
    }
}
