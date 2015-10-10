package configgen.define;

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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class DomUtils {
    public static Element rootElement(File file) {
        try {
            return DocumentBuilderFactory.newInstance()
                    .newDocumentBuilder().parse(file)
                    .getDocumentElement();
        } catch (SAXException | IOException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Document newDocument() {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static void prettySaveDocument(Document document, File file, String encoding) throws IOException {
        try (OutputStream dst = new FileOutputStream(file)) {
            prettySaveDocument(document, dst, encoding);
        }
    }

    public static void prettySaveDocument(Document document, OutputStream destination, String encoding) {
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
            throw new RuntimeException(e);
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
            Integer index = name2index.get(e.getNodeName());

            if (index != null) {
                res.get(index).add(e);
            } else {
                System.err.println(self.getTagName() + " unknown node " + e.getNodeName());
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
}
