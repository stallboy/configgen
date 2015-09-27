package configgen;

import configgen.gen.CachedFileOutputStream;
import org.w3c.dom.*;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;

public final class Utils {
    public static Element rootElement(File file) throws ParserConfigurationException, IOException, SAXException {
        return DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().parse(file)
                .getDocumentElement();
    }

    public static Document newDocument() throws ParserConfigurationException, IOException, SAXException {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
    }

    public static void prettySaveDocument(Document document, File file, String encoding) throws IOException {
        try (OutputStream dst = new FileOutputStream(file)) {
            prettySaveDocument(document, dst, encoding);
        }
    }

    public static void prettySaveDocument(Document document, OutputStream destination, String encoding) throws IOException {
        DOMImplementation impl = document.getImplementation();
        Object f = impl.getFeature("LS", "3.0");
        if (f != null) {
            DOMImplementationLS ls = (DOMImplementationLS) f;
            LSSerializer s = ls.createLSSerializer();
            s.setNewLine("\r\n");
            DOMConfiguration cfg = s.getDomConfig();
            cfg.setParameter("format-pretty-print", Boolean.TRUE);
            LSOutput dst = ls.createLSOutput();
            dst.setEncoding(encoding);
            dst.setByteStream(destination);
            s.write(document, dst);
            return;
        }

        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty(OutputKeys.METHOD, "xml");
            t.setOutputProperty(OutputKeys.ENCODING, encoding);
            t.transform(new DOMSource(document), new StreamResult(destination));
        } catch (TransformerException e) {
            throw new IOException(e);
        }
    }

    public static List<List<Element>> elementsList(Element self, String... names) {
        List<List<Element>> res = new ArrayList<>();
        Map<String, Integer> name2index = new HashMap<>();
        int idx = 0;
        for (String name : names) {
            res.add(new ArrayList<>());
            name2index.put(name, idx);
            idx += 1;
        }

        NodeList childNodes = self.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            org.w3c.dom.Node node = childNodes.item(i);
            if (org.w3c.dom.Node.ELEMENT_NODE != node.getNodeType())
                continue;

            Element e = (Element) node;
            String nodename = e.getNodeName();
            Integer index = name2index.get(nodename);

            if (index != null) {
                res.get(index).add(e);
            } else {
                System.err.println(self.getTagName() + " unknown node "
                        + nodename);
            }
        }
        return res;
    }

    public static String[] attributes(Element self, String... names) {
        String[] res = new String[names.length];

        Map<String, String> attr = new HashMap<>();
        NamedNodeMap as = self.getAttributes();
        for (int i = 0; i < as.getLength(); ++i) {
            Attr a = (Attr) as.item(i);
            attr.put(a.getName(), a.getValue());
        }

        int i = 0;
        for (String n : names) {
            String a = attr.remove(n);
            res[i++] = (a == null ? "" : a.trim());
        }

        for (Map.Entry<String, String> e : attr.entrySet())
            System.err.println(self.getTagName() + " unknown attr: "
                    + e.getKey() + "=" + e.getValue());
        return res;
    }

    public static Element newChild(Element parent, String tag) {
        Element e = parent.getOwnerDocument().createElement(tag);
        parent.appendChild(e);
        return e;
    }

    public static String upper1(String value) {
        return value.substring(0, 1).toUpperCase() + value.substring(1);
    }

    public static String lower1(String value) {
        return value.substring(0, 1).toLowerCase() + value.substring(1);
    }

    public static String path2Name(String p) {
        String[] res = p.split("\\\\|/");
        if (res.length > 0) {
            String last = res[res.length - 1];
            res[res.length - 1] = last.substring(0, last.length() - 4);
        }
        return String.join(".", res);
    }

    public static PrintStream cachedPrintStream(File file, String encoding) throws IOException {
        return new PrintStream(new CachedFileOutputStream(file), false, encoding);
    }

    private static boolean verboseEnabled = false;
    public static void enableVerbose(boolean enable){
        verboseEnabled = enable;
    }

    private final static SimpleDateFormat df = new SimpleDateFormat("HH.mm.ss.SSS");
    public static void verbose(String s) {
        if (verboseEnabled) {
            System.out.println(df.format(Calendar.getInstance().getTime()) + ": " + s);
        }
    }
}
