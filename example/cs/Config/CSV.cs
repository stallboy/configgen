using System;
using System.Collections.Generic;
using System.IO;
using System.Text;

namespace Config
{
    public static class CSV
    {
        private const char Comma = ',';
        private const char Quote = '"';
        private const char Cr = '\r';
        private const char Lf = '\n';

        private enum State
        {
            Start,
            NotInQuote,
            InQuote,
            InQuote2,
            InCr,
        }

        public static List<List<string>> Parse(TextReader reader)
        {
            var result = new List<List<string>>();
            var record = new List<string>();
            var state = State.Start;
            StringBuilder field = null;

            for (int i = reader.Read(); i != -1; i = reader.Read())
            {
                var c = (char) i;

                switch (state)
                {
                    case State.Start:
                        switch (c)
                        {
                            case Comma:
                                record.Add("");
                                break;
                            case Quote:
                                field = new StringBuilder();
                                state = State.InQuote;
                                break;
                            case Cr:
                                field = new StringBuilder();
                                state = State.InCr;
                                break;
                            default:
                                field = new StringBuilder();
                                field.Append(c);
                                state = State.NotInQuote;
                                break;
                        }
                        break;

                    case State.NotInQuote:
                        switch (c)
                        {
                            case Comma:
                                record.Add(field.ToString());
                                state = State.Start;
                                break;
                            case Cr:
                                state = State.InCr;
                                break;
                            default:
                                field.Append(c);
                                break;
                        }
                        break;

                    case State.InQuote:
                        switch (c)
                        {
                            case Quote:
                                state = State.InQuote2;
                                break;
                            default:
                                field.Append(c);
                                break;
                        }
                        break;

                    case State.InQuote2:
                        switch (c)
                        {
                            case Comma:
                                record.Add(field.ToString());
                                state = State.Start;
                                break;
                            case Quote:
                                field.Append(Quote);
                                state = State.InQuote;
                                break;
                            case Cr:
                                state = State.InCr;
                                break;
                            default:
                                field.Append(c);
                                state = State.NotInQuote;
                                break;
                        }
                        break;

                    case State.InCr:
                        switch (c)
                        {
                            case Comma:
                                field.Append(Cr);
                                record.Add(field.ToString());
                                state = State.Start;
                                break;
                            case Lf:
                                record.Add(field.ToString());
                                result.Add(record);
                                record = new List<string>();
                                state = State.Start;
                                break;
                            default:
                                field.Append(Cr);
                                field.Append(c);
                                state = State.NotInQuote;
                                break;
                        }
                        break;
                }
            }

            switch (state)
            {
                case State.Start:
                    if (record.Count > 0)
                    {
                        record.Add("");
                        result.Add(record);
                    }
                    break;
                case State.InCr:
                    record.Add(field.Append(Cr).ToString());
                    result.Add(record);
                    break;
                default:
                    record.Add(field.ToString());
                    result.Add(record);
                    break;
            }

            var filtered = new List<List<string>>();
            foreach (var rec in result)
            {
                var allEmpty = true;
                foreach (var s in rec)
                {
                    if (s.Length > 0)
                    {
                        allEmpty = false;
                        break;
                    }
                }
                if (!allEmpty)
                    filtered.Add(rec);
            }

            return filtered;
        }

        public static Dictionary<string, Dictionary<ushort, string>> ParseCSVText(TextReader reader)
        {
            var result = new Dictionary<string, Dictionary<ushort, string>>();
            var one = new Dictionary<ushort, string>();

            foreach (var record in Parse(reader))
            {
                var s = record[0];
                if (s.StartsWith("#"))
                {
                    var csv = s.Substring(1);
                    one = new Dictionary<ushort, string>();
                    result.Add(csv, one);
                }
                else
                {
                    one.Add(UInt16.Parse(record[0]), record[1]);
                }
            }
            return result;
        }

        public static string ToString<T>(List<T> data)
        {
            var elements = new string[data.Count];
            var i = 0;
            foreach (var d in data)
            {
                elements[i] = d.ToString();
                i++;
            }
            return "[" + string.Join(", ", elements) + "]";
        }
    }
}
