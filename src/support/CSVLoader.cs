using System.Collections.Generic;
using System.IO;
using System.Text;
using ICSharpCode.SharpZipLib.Zip;

namespace Config
{
    public static partial class CSVLoader
    {
        public static LoadErrors Load(string configdataZip)
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
                    return DoLoad(byter, allTextMap);
                }
            }
        }
    }
}