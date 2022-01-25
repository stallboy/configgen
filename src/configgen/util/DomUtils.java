package configgen.util;

import org.w3c.dom.*;
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
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

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

    public static void prettySaveDocument(Document document, File file, String encoding)  {
        try (OutputStream dst = new CachedFileOutputStream(file)) {
            prettySaveDocument(document, dst, encoding);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void prettySaveDocument(Document document, OutputStream destination, String encoding) {
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.INDENT, "yes");
            t.setOutputProperty(OutputKeys.METHOD, "xml");
            t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            t.setOutputProperty(OutputKeys.ENCODING, encoding);
            t.transform(new DOMSource(document), new StreamResult(destination));
        } catch (TransformerException e) {
            throw new RuntimeException(e);
        }
    }


    public static void permitElements(Element self, String... names) {
        HashSet<String> available = new HashSet<>(Arrays.asList(names));
        NodeList childNodes = self.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            org.w3c.dom.Node node = childNodes.item(i);
            if (org.w3c.dom.Node.ELEMENT_NODE != node.getNodeType())
                continue;

            Element e = (Element) node;
            if (!available.contains(e.getNodeName())) {
                System.err.println(self.getTagName() + " unknown element: "
                        + e.getNodeName());
            }
        }
    }

    public static List<Element> elements(Element self, String name) {
        List<Element> res = new ArrayList<>();
        NodeList childNodes = self.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            org.w3c.dom.Node node = childNodes.item(i);
            if (org.w3c.dom.Node.ELEMENT_NODE != node.getNodeType())
                continue;
            Element e = (Element) node;
            if (e.getNodeName().equals(name))
                res.add(e);
        }
        return res;
    }

    public static void permitAttributes(Element self, String... names) {
        HashSet<String> available = new HashSet<>(Arrays.asList(names));
        NamedNodeMap as = self.getAttributes();
        for (int i = 0; i < as.getLength(); ++i) {
            Attr a = (Attr) as.item(i);
            if (!available.contains(a.getName())) {
                System.err.println(self.getTagName() + " unknown attr: "
                        + a.getName() + "=" + a.getValue());
            }
        }
    }

    public static Element newChild(Element parent, String tag) {
        Element e = parent.getOwnerDocument().createElement(tag);
        parent.appendChild(e);
        return e;
    }

    public static String[] parseStringArray(Element self, String attrName) {
        String attr = self.getAttribute(attrName).trim();
        if (!attr.isEmpty())
            return attr.split("\\s*,\\s*");
        else
            return new String[0];
    }

}
