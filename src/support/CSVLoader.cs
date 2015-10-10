using System.Collections.Generic;
using System.IO;
using System.Text;
using ICSharpCode.SharpZipLib.Zip;

namespace Config
{
    public static partial class CSVLoader
    {
        public static LoadErrors LoadBin(string configdataZip)
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
                    return DoLoad(new List<BinaryReader>() {byter}, allTextMap);
                }
            }
        }

        public static LoadErrors LoadPack(string packDir)
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
                if (!name.Equals("text"))
                {
                    var z = new ZipFile(f);
                    zipFiles.Add(z);
                    var byter = new BinaryReader(z.GetInputStream(z.GetEntry(name)));
                    byterList.Add(byter);
                }
            }
            var errors =  DoLoad(byterList, allTextMap);
            foreach (var z in zipFiles)
            {
                z.Close();
            }
            return errors;
        }
    }
}