using System.Collections.Generic;
using System.IO;
using System.Text;
using ICSharpCode.SharpZipLib.Zip;

namespace Config
{
    public class Stream
    {
        private readonly Dictionary<string, Dictionary<ushort, string>> _allTextMap;
        private readonly List<BinaryReader> _byterList;

        private int _currentIndex;
        private BinaryReader _byter;
        private Dictionary<ushort, string> _textMap;

        public Stream(List<BinaryReader> byterList, Dictionary<string, Dictionary<ushort, string>> allTextMap)
        {
            _byterList = byterList;
            _allTextMap = allTextMap;
            _currentIndex = 0;
            _byter = byterList[0];
        }

        public string ReadCfg()
        {
            try
            {
                var cfg = ReadString();
                _allTextMap.TryGetValue(cfg, out _textMap);
                return cfg;
            }
            catch (EndOfStreamException)
            {
                _currentIndex ++;
                if (_currentIndex < _byterList.Count)
                {
                    _byter = _byterList[_currentIndex];
                    var cfg = ReadString();
                    _allTextMap.TryGetValue(cfg, out _textMap);
                    return cfg;
                }
            }
            return null;
        }

        public int ReadSize()
        {
            return _byter.ReadUInt16();
        }

        public string ReadString()
        {
            var count = _byter.ReadUInt16();
            return Encoding.UTF8.GetString(_byter.ReadBytes(count));
        }

        public int ReadInt32()
        {
            return _byter.ReadInt32();
        }

        public long ReadInt64()
        {
            return _byter.ReadInt64();
        }

        public bool ReadBool()
        {
            return _byter.ReadBoolean();
        }

        public float ReadSingle()
        {
            return _byter.ReadSingle();
        }

        public string ReadText()
        {
            return _textMap[_byter.ReadUInt16()];
        }
    }


    public static class CSVLoader
    {
        public delegate void ProcessConfigStream(Stream os);

        public static ProcessConfigStream Processor;

        public static void LoadBin(string configdataZip)
        {
            using (var z = new ZipFile(configdataZip))
            {
                Dictionary<string, Dictionary<ushort, string>> allTextMap;
                using (var texter = new StreamReader(z.GetInputStream(z.GetEntry("text.csv")), Encoding.UTF8))
                {
                    allTextMap = CSV.ParseCSVText(texter);
                }

                using (var byter = new BinaryReader(z.GetInputStream(z.GetEntry("csv.byte"))))
                {
                    Processor(new Stream(new List<BinaryReader>() {byter}, allTextMap));
                }
            }
        }

        public static void LoadPack(string packDir)
        {
            Dictionary<string, Dictionary<ushort, string>> allTextMap;
            using (var z = new ZipFile(Path.Combine(packDir, "text.zip")))
            {
                using (var texter = new StreamReader(z.GetInputStream(z.GetEntry("text.csv")), Encoding.UTF8))
                {
                    allTextMap = CSV.ParseCSVText(texter);
                }
            }

            var byterList = new List<BinaryReader>();
            var zipFiles = new List<ZipFile>();
            foreach (var f in Directory.GetFiles(packDir, "*.zip"))
            {
                var name = Path.GetFileNameWithoutExtension(f);
                if (name != null && !name.Equals("text"))
                {
                    var z = new ZipFile(f);
                    zipFiles.Add(z);
                    var byter = new BinaryReader(z.GetInputStream(z.GetEntry(name)));
                    byterList.Add(byter);
                }
            }

            Processor(new Stream(byterList, allTextMap));
            foreach (var z in zipFiles)
            {
                z.Close();
            }
        }
    }
}