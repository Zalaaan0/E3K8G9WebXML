package e3k8g9.domparse.hu;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * ========================================================================
 * DOM alapu XML olvaso program az EGYETEMI RENDSZER temaban (E3K8G9)
 * ========================================================================
 * Feladata:
 *  - Az XML dokumentum (E3K8G9_XML.xml) XSD semaval (E3K8G9_XMLSchema.xsd)
 *    torteno validalasa es DOM-faba toltese
 *  - A teljes dokumentum bejarasa es blokkszeru kiirasa magyar megnevezesekkel
 *  - Az eredmeny kiirasa konzolra es TXT fajlba (E3K8G9_XML_output.txt)
 * ========================================================================
 */
public class E3K8G9DOMRead {

    // Bemeneti es kimeneti fajlok eleresi utjai a projekt gyokereben
    private static final String XML_FILE_PATH = "E3K8G9_XML.xml";
    private static final String XSD_FILE_PATH = "E3K8G9_XMLSchema.xsd";
    private static final String OUTPUT_TXT_PATH = "E3K8G9_XML_output.txt";

    // Elemnevek magyar megfeleloinek tarolasa
    private static final Map<String, String> ADAT_NEVEK = new HashMap<>();

    // Attribútumnevek magyar megfeleloinek tarolasa
    private static final Map<String, String> KULCS_NEVEK = new HashMap<>();

    static {
        // ========= Gyokerelem es egyedek =========
        ADAT_NEVEK.put("EgyetemiRendszer", "Egyetemi rendszer");
        ADAT_NEVEK.put("Koltsegterites", "Költségtérítés");
        ADAT_NEVEK.put("hallgato", "Hallgató");
        ADAT_NEVEK.put("tantargy", "Tantárgy");
        ADAT_NEVEK.put("oktato", "Oktató");
        ADAT_NEVEK.put("tanszek", "Tanszék");

        // ========= Kapcsolatok =========
        ADAT_NEVEK.put("befizet", "Befizetés (1:1)");
        ADAT_NEVEK.put("jegy", "Érdemjegy (M:N)");
        ADAT_NEVEK.put("oktat", "Oktatás (M:N)");
        ADAT_NEVEK.put("gondoz", "Tantárgygondozás (1:N)");

        // ========= Tulajdonsagok / gyermekelemek =========
        ADAT_NEVEK.put("osszeg", "Összeg");
        ADAT_NEVEK.put("fizetesi_hatarido", "Fizetési határidő");
        ADAT_NEVEK.put("fizetes_modja", "Fizetés módja");
        ADAT_NEVEK.put("nev", "Név");
        ADAT_NEVEK.put("szuletesi_ido", "Születési idő");
        ADAT_NEVEK.put("Cim", "Cím");
        ADAT_NEVEK.put("Varos", "Város");
        ADAT_NEVEK.put("Utca", "Utca");
        ADAT_NEVEK.put("Hazszam", "Házszám");
        ADAT_NEVEK.put("Elerhetoseg", "Elérhetőség");
        ADAT_NEVEK.put("Email", "E-mail cím");
        ADAT_NEVEK.put("Telefon", "Telefonszám");
        ADAT_NEVEK.put("kredit", "Kredit");
        ADAT_NEVEK.put("tipus", "Típus");
        ADAT_NEVEK.put("beosztas", "Beosztás");
        ADAT_NEVEK.put("szakirany", "Szakirány");
        ADAT_NEVEK.put("vezeto", "Tanszékvezető");
        ADAT_NEVEK.put("letszam", "Létszám");
        ADAT_NEVEK.put("ertek", "Érték");
        ADAT_NEVEK.put("datum", "Dátum");
        ADAT_NEVEK.put("felev", "Félév");
        ADAT_NEVEK.put("orarendi_nap", "Órarendi nap");

        // ========= XML attributumok magyaritasa =========
        KULCS_NEVEK.put("szamla_szam", "Számlaszám (PK/FK)");
        KULCS_NEVEK.put("neptun", "Neptun-kód (PK/FK)");
        KULCS_NEVEK.put("tkod", "Tantárgykód (PK/FK)");
        KULCS_NEVEK.put("oktatoId", "Oktató azonosító (PK/FK)");
        KULCS_NEVEK.put("tanszekId", "Tanszék azonosító (PK/FK)");
    }

    public static void main(String[] args) {
        FileWriter writer = null;

        try {
            // =================================================================
            // 1) XSD sema betoltese es DOM parser beallitasa validalassal
            // =================================================================
            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new File(XSD_FILE_PATH));

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            dbFactory.setSchema(schema);
            dbFactory.setIgnoringElementContentWhitespace(true);

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(new File(XML_FILE_PATH));
            doc.getDocumentElement().normalize();

            // =================================================================
            // 2) Kimeneti TXT fajl megnyitasa (UTF-8 kodolassal)
            // =================================================================
            writer = new FileWriter(OUTPUT_TXT_PATH, StandardCharsets.UTF_8);

            // =================================================================
            // 3) Gyokerelem es validacios visszajelzes kiirasa
            // =================================================================
            String rootName = doc.getDocumentElement().getNodeName();
            printLine("XSD validalas sikeres! Gyoker elem: " + at(rootName) + " (" + rootName + ")", writer);
            printLine("================================================================", writer);

            // =================================================================
            // 4) Gyoker alatti egyed- es kapcsolo elemek bejarasa
            // =================================================================
            NodeList rootChildren = doc.getDocumentElement().getChildNodes();

            for (int i = 0; i < rootChildren.getLength(); i++) {
                Node node = rootChildren.item(i);

                // Csak az XML elemcsomopontokat dolgozzuk fel
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element elem = (Element) node;

                    printLine("\nElem: " + at(elem.getNodeName()), writer);
                    printLine("--------------------------------", writer);

                    // 4/A) Attribútumok kiirasa magyarositott nevvel
                    NamedNodeMap attributes = elem.getAttributes();
                    if (attributes != null && attributes.getLength() > 0) {
                        for (int a = 0; a < attributes.getLength(); a++) {
                            Node attr = attributes.item(a);
                            String magyarKulcs = KULCS_NEVEK.getOrDefault(attr.getNodeName(), attr.getNodeName());
                            printLine(magyarKulcs + ": " + attr.getNodeValue(), writer);
                        }
                    }

                    // 4/B) Gyermekelemek (egyszeru es osszetett/tobbertekű) kiirasa
                    NodeList subNodes = elem.getChildNodes();
                    for (int j = 0; j < subNodes.getLength(); j++) {
                        Node sub = subNodes.item(j);

                        if (sub.getNodeType() == Node.ELEMENT_NODE) {
                            Element subElem = (Element) sub;

                            if (hasElementChild(subElem)) {
                                // Osszetett elem (pl. Cim, Elerhetoseg)
                                printLine(at(subElem.getNodeName()) + ":", writer);

                                NodeList deepList = subElem.getChildNodes();
                                for (int k = 0; k < deepList.getLength(); k++) {
                                    Node deep = deepList.item(k);

                                    if (deep.getNodeType() == Node.ELEMENT_NODE) {
                                        printLine(
                                                "    " + at(deep.getNodeName()) + ": " + deep.getTextContent().trim(),
                                                writer
                                        );
                                    }
                                }
                            } else {
                                // Egyszeru vagy tobbertekű level elem (pl. nev, kredit, szakirany)
                                printLine(
                                        at(subElem.getNodeName()) + ": " + subElem.getTextContent().trim(),
                                        writer
                                );
                            }
                        }
                    }

                    printLine("--------------------------------", writer);
                }
            }

            System.out.println("\nKimenet elmentve a kovetkezo fajlba: " + OUTPUT_TXT_PATH);

        } catch (Exception e) {
            System.err.println("Hiba tortent a DOM olvasas vagy validalas soran:");
            e.printStackTrace();
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * Megallapitja, hogy az adott elemnek van-e legalabb egy gyermek ELEM csomopontja.
     */
    private static boolean hasElementChild(Element elem) {
        NodeList list = elem.getChildNodes();
        for (int i = 0; i < list.getLength(); i++) {
            if (list.item(i).getNodeType() == Node.ELEMENT_NODE) {
                return true;
            }
        }
        return false;
    }

    /**
     * Visszaadja az elem magyar megnevezeset, ha szerepel a map-ben, egyebkent az eredeti nevet.
     */
    private static String at(String name) {
        return ADAT_NEVEK.getOrDefault(name, name);
    }

    /**
     * Kiir egy sort a konzolra es a TXT fajlba is.
     */
    private static void printLine(String text, FileWriter writer) throws Exception {
        System.out.println(text);
        writer.write(text + System.lineSeparator());
    }
}