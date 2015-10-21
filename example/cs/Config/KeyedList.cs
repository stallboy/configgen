using System.Collections.Generic;

namespace Config
{
    public class KeyedList<TKey, TValue>
    {
        public KeyedList()
        {
            OrderedKeys = new List<TKey>();
            OrderedValues = new List<TValue>();
            Map = new Dictionary<TKey, TValue>();
        }

        public List<TKey> OrderedKeys { get; private set; }
        public List<TValue> OrderedValues { get; private set; }
        public Dictionary<TKey, TValue> Map { get; private set; }

        public void Add(TKey key, TValue value)
        {
            OrderedKeys.Add(key);
            OrderedValues.Add(value);
            Map.Add(key, value);
        }

        public bool TryGetValue(TKey key, out TValue val)
        {
            return Map.TryGetValue(key, out val);
        }

        public override string ToString()
        {
            var sdata = new string[OrderedKeys.Count];
            var i = 0;
            foreach (var k in OrderedKeys)
            {
                sdata[i] = k + "=" + OrderedValues[i];
                i++;
            }
            return "{" + string.Join(", ", sdata) + "}";
        }
    }
}
