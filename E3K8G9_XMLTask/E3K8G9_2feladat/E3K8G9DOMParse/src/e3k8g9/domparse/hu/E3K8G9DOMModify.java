package e3k8g9.domparse.hu;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * ========================================================================
 * DOM alapu XML modosito program az EGYETEMI RENDSZER temaban (E3K8G9)
 * ========================================================================
 * Feladata:
 *  - Az XML dokumentum (E3K8G9_XML.xml) beolvasasa es XSD validalasa
 *  - Meglevo elemek tartalmanak modositasa a DOM-fan belul
 *  - A modositott entitások kiirasa a konzolra magyarositott azonositokkal
 * ========================================================================
 */
public class E3K8G9DOMModify {

    private static final String XML_FILE_PATH = "E3K8G9_XML.xml";
    private static final String XSD_FILE_PATH = "E3K8G9_XMLSchema.xsd";

    // ========== Attributumnevek magyaritasa a megjeleniteshez ==========
    private static final Map<String, String> kulcsnevek = new HashMap<>();
    static {
        kulcsnevek.put("szamla_szam", "Számlaszám (PK/FK)");
        kulcsnevek.put("neptun", "Neptun-kód (PK/FK)");
        kulcsnevek.put("tkod", "Tantárgykód (PK/FK)");
        kulcsnevek.put("oktatoId", "Oktató azonosító (PK/FK)");
        kulcsnevek.put("tanszekId", "Tanszék azonosító (PK/FK)");
    }

    // ========= Mely XML elemek jelenjenek meg a konzolon =========
    private static final Set<String> megjelenitendo = new HashSet<>(Arrays.asList(
            "jegy",
            "tantargy",
            "Koltsegterites",
            "oktato"
    ));

    public static void main(String[] args) {
        try {

            // ======================== XML BEOLVASAS ============================
            File xmlFile = new File(XML_FILE_PATH);

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new File(XSD_FILE_PATH));

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setSchema(schema);

            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();


            // ===================================================================
            // 1) MODOSITAS: Hallgato (neptun=E3K8G9) jegye (tkod=GEIAK120B): 5 -> 4
            // ===================================================================
            NodeList jegyLista = doc.getElementsByTagName("jegy");

            for (int i = 0; i < jegyLista.getLength(); i++) {
                Element j = (Element) jegyLista.item(i);

                if ("E3K8G9".equals(j.getAttribute("neptun")) && "GEIAK120B".equals(j.getAttribute("tkod"))) {
                    Node ertek = j.getElementsByTagName("ertek").item(0);
                    if (ertek != null) {
                        System.out.println("Eredeti erdemjegy: " + ertek.getTextContent().trim());
                        ertek.setTextContent("4");
                        System.out.println("Uj erdemjegy -> 4\n");
                    }
                    break;
                }
            }


            // ===================================================================
            // 2) MODOSITAS: Tantargy (tkod=GEIAK120B) kredit erteke: 5 -> 6
            // ===================================================================
            NodeList tantargyLista = doc.getElementsByTagName("tantargy");

            for (int i = 0; i < tantargyLista.getLength(); i++) {
                Element t = (Element) tantargyLista.item(i);

                if ("GEIAK120B".equals(t.getAttribute("tkod"))) {
                    Node kredit = t.getElementsByTagName("kredit").item(0);
                    if (kredit != null) {
                        System.out.println("Eredeti kredit: " + kredit.getTextContent().trim());
                        kredit.setTextContent("6");
                        System.out.println("Uj kredit -> 6\n");
                    }
                    break;
                }
            }


            // ===================================================================
            // 3) MODOSITAS: Koltsegterites (szamla_szam=SZ-2026-001) osszeg: 185000 -> 195000
            // ===================================================================
            NodeList koltsegLista = doc.getElementsByTagName("Koltsegterites");

            for (int i = 0; i < koltsegLista.getLength(); i++) {
                Element kt = (Element) koltsegLista.item(i);

                if ("SZ-2026-001".equals(kt.getAttribute("szamla_szam"))) {
                    Node osszeg = kt.getElementsByTagName("osszeg").item(0);
                    if (osszeg != null) {
                        System.out.println("Eredeti osszeg: " + osszeg.getTextContent().trim());
                        osszeg.setTextContent("195000");
                        System.out.println("Uj osszeg -> 195000\n");
                    }
                    break;
                }
            }


            // ===================================================================
            // 4) MODOSITAS: Oktato (oktatoId=OKT-01) beosztasa: "Egyetemi docens" -> "Egyetemi tanar"
            // ===================================================================
            NodeList oktatoLista = doc.getElementsByTagName("oktato");

            for (int i = 0; i < oktatoLista.getLength(); i++) {
                Element o = (Element) oktatoLista.item(i);

                if ("OKT-01".equals(o.getAttribute("oktatoId"))) {
                    Node beosztas = o.getElementsByTagName("beosztas").item(0);
                    if (beosztas != null) {
                        System.out.println("Eredeti beosztas: " + beosztas.getTextContent().trim());
                        beosztas.setTextContent("Egyetemi tanar");
                        System.out.println("Uj beosztas -> Egyetemi tanar\n");
                    }
                    break;
                }
            }


            // ===================================================================
            // 5) KIVALASZTOTT ENTITASOK KIIRASA (MODOSITOTT)
            // ===================================================================
            System.out.println("\n====== KIVALASZTOTT ENTITASOK (MODOSITOTT) ======\n");

            Node root = doc.getDocumentElement();
            NodeList children = root.getChildNodes();

            for (int i = 0; i < children.getLength(); i++) {
                Node n = children.item(i);

                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) n;

                    // Csak a kivalasztott elemek
                    if (!megjelenitendo.contains(elem.getNodeName())) {
                        continue;
                    }

                    System.out.println("Elem: " + elem.getNodeName());
                    System.out.println("--------------------------------");

                    // ===== ATTRIBUTUMOK MAGYARITASA =====
                    NamedNodeMap attrs = elem.getAttributes();
                    for (int a = 0; a < attrs.getLength(); a++) {
                        Node attr = attrs.item(a);
                        String kulcs = kulcsnevek.getOrDefault(attr.getNodeName(), attr.getNodeName());
                        System.out.println(kulcs + ": " + attr.getNodeValue());
                    }

                    // ===== CHILD NODES KIIRASA =====
                    NodeList subs = elem.getChildNodes();
                    for (int j = 0; j < subs.getLength(); j++) {
                        Node sub = subs.item(j);

                        if (sub.getNodeType() == Node.ELEMENT_NODE) {
                            Element se = (Element) sub;

                            // Ha az elemnek vannak tovabbi elemei (pl. osszetett blokk)
                            if (hasElementChild(se)) {
                                System.out.println(se.getNodeName() + ":");
                                NodeList deep = se.getChildNodes();
                                for (int k = 0; k < deep.getLength(); k++) {
                                    Node d = deep.item(k);
                                    if (d.getNodeType() == Node.ELEMENT_NODE) {
                                        System.out.println("    " + d.getNodeName() + ": " + d.getTextContent().trim());
                                    }
                                }
                            } else {
                                System.out.println(se.getNodeName() + ": " + se.getTextContent().trim());
                            }
                        }
                    }

                    System.out.println("--------------------------------\n");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===============================================================
    // Segedfuggveny: megallapitja, hogy van-e tovabbi elem gyerek
    // ===============================================================
    private static boolean hasElementChild(Element e) {
        NodeList list = e.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
                return true;
            }
        }
        return false;
    }
}