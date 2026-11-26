package e3k8g9.domparse.hu;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * ========================================================================
 * XML lekerdezo program DOM parserrel az EGYETEMI RENDSZER temaban (E3K8G9)
 * ========================================================================
 * Feladata:
 *  - Beolvassa es XSD semaval validalja az E3K8G9_XML.xml dokumentumot
 *  - Osszekapcsolja a hallgatokat, jegyeket, tantargyakat, oktatast es oktatokat
 *  - Hallgatonkent kiirja a kapott jegyeket a tantargy, felev/nap es oktato adataival
 *  - Kiegészitokent kilistazza a tantargygondozasokat es befizeteseket is
 * ========================================================================
 */
public class E3K8G9DOMQuery {

    private static final String XML_FILE_PATH = "E3K8G9_XML.xml";
    private static final String XSD_FILE_PATH = "E3K8G9_XMLSchema.xsd";

    /**
     * Adatszerkezet egy hallgato altal kapott jegy es a kapcsolodo adatok tarolasara.
     */
    static class KapottJegy {
        String tkod;
        String tantargyNev;
        String kredit;
        String ertek;
        String datum;
        String felev;
        String orarendiNap;
        String oktatoNev;

        KapottJegy(String tkod, String tantargyNev, String kredit, String ertek,
                   String datum, String felev, String orarendiNap, String oktatoNev) {
            this.tkod = tkod;
            this.tantargyNev = tantargyNev;
            this.kredit = kredit;
            this.ertek = ertek;
            this.datum = datum;
            this.felev = felev;
            this.orarendiNap = orarendiNap;
            this.oktatoNev = oktatoNev;
        }
    }

    public static void main(String[] args) {
        try {
            // =====================================================================
            // 1) XML BEOLVASASA ES XSD VALIDALASA
            // =====================================================================
            File inputFile = new File(XML_FILE_PATH);

            SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new File(XSD_FILE_PATH));

            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setNamespaceAware(true);
            dbFactory.setSchema(schema);

            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

            // DOM fa felépitese
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();


            // =====================================================================
            // 2) OKTATOK BEOLVASASA (oktatoId -> nev)
            // =====================================================================
            Map<String, String> oktatoNevek = new HashMap<>();
            NodeList oktatoLista = doc.getElementsByTagName("oktato");

            for (int i = 0; i < oktatoLista.getLength(); i++) {
                Element o = (Element) oktatoLista.item(i);
                String oktatoId = o.getAttribute("oktatoId");
                String nev = getTextContentOfChild(o, "nev");
                oktatoNevek.put(oktatoId, nev != null ? nev : "Ismeretlen oktato");
            }


            // =====================================================================
            // 3) TANSZEKEK BEOLVASASA (tanszekId -> nev)
            // =====================================================================
            Map<String, String> tanszekNevek = new HashMap<>();
            NodeList tanszekLista = doc.getElementsByTagName("tanszek");

            for (int i = 0; i < tanszekLista.getLength(); i++) {
                Element ts = (Element) tanszekLista.item(i);
                String tanszekId = ts.getAttribute("tanszekId");
                String nev = getTextContentOfChild(ts, "nev");
                tanszekNevek.put(tanszekId, nev != null ? nev : "Ismeretlen tanszek");
            }


            // =====================================================================
            // 4) TANTARGYAK BEOLVASASA (tkod -> nev, tkod -> kredit)
            // =====================================================================
            Map<String, String> tantargyNevek = new HashMap<>();
            Map<String, String> tantargyKreditek = new HashMap<>();
            NodeList tantargyLista = doc.getElementsByTagName("tantargy");

            for (int i = 0; i < tantargyLista.getLength(); i++) {
                Element t = (Element) tantargyLista.item(i);
                String tkod = t.getAttribute("tkod");
                String nev = getTextContentOfChild(t, "nev");
                String kredit = getTextContentOfChild(t, "kredit");

                tantargyNevek.put(tkod, nev != null ? nev : "Ismeretlen tantargy");
                tantargyKreditek.put(tkod, kredit != null ? kredit : "-");
            }


            // =====================================================================
            // 5) OKTATAS KAPCSOLAT BEOLVASASA (tkod -> oktatoId, felev, nap)
            // =====================================================================
            Map<String, String> targyOktatoId = new HashMap<>();
            Map<String, String> targyFelev = new HashMap<>();
            Map<String, String> targyNap = new HashMap<>();
            NodeList oktatLista = doc.getElementsByTagName("oktat");

            for (int i = 0; i < oktatLista.getLength(); i++) {
                Element ok = (Element) oktatLista.item(i);
                String tkod = ok.getAttribute("tkod");
                String oktatoId = ok.getAttribute("oktatoId");
                String felev = getTextContentOfChild(ok, "felev");
                String nap = getTextContentOfChild(ok, "orarendi_nap");

                targyOktatoId.put(tkod, oktatoId);
                targyFelev.put(tkod, felev != null ? felev : "-");
                targyNap.put(tkod, nap != null ? nap : "-");
            }


            // =====================================================================
            // 6) HALLGATOK BEOLVASASA (neptun -> nev)
            // =====================================================================
            Map<String, String> hallgatoNevek = new HashMap<>();
            NodeList hallgatoLista = doc.getElementsByTagName("hallgato");

            for (int i = 0; i < hallgatoLista.getLength(); i++) {
                Element h = (Element) hallgatoLista.item(i);
                String neptun = h.getAttribute("neptun");
                String nev = getTextContentOfChild(h, "nev");
                hallgatoNevek.put(neptun, nev != null ? nev : "Ismeretlen hallgato");
            }


            // =====================================================================
            // 7) JEGYEK OSSZEKAPCSOLASA: Hallgato (neptun) -> Lista<KapottJegy>
            // =====================================================================
            Map<String, List<KapottJegy>> hallgatoiJegyek = new HashMap<>();
            // Minden hallgato alapertelmezett ures listaval indul
            for (String neptun : hallgatoNevek.keySet()) {
                hallgatoiJegyek.put(neptun, new ArrayList<>());
            }

            NodeList jegyLista = doc.getElementsByTagName("jegy");

            for (int i = 0; i < jegyLista.getLength(); i++) {
                Element j = (Element) jegyLista.item(i);

                String neptun = j.getAttribute("neptun");
                String tkod = j.getAttribute("tkod");

                String ertek = getTextContentOfChild(j, "ertek");
                String datum = getTextContentOfChild(j, "datum");

                String tantargyNev = tantargyNevek.getOrDefault(tkod, "Tantargy nev hianyzik");
                String kredit = tantargyKreditek.getOrDefault(tkod, "-");

                String oktatoId = targyOktatoId.get(tkod);
                String oktatoNev = oktatoNevek.getOrDefault(oktatoId, "Oktato nem meghatarozott");
                String felev = targyFelev.getOrDefault(tkod, "-");
                String nap = targyNap.getOrDefault(tkod, "-");

                KapottJegy jegyObj = new KapottJegy(
                        tkod,
                        tantargyNev,
                        kredit,
                        ertek != null ? ertek : "Nincs jegy",
                        datum != null ? datum : "Nincs datum",
                        felev,
                        nap,
                        oktatoNev
                );

                List<KapottJegy> lista = hallgatoiJegyek.getOrDefault(neptun, new ArrayList<>());
                lista.add(jegyObj);
                hallgatoiJegyek.put(neptun, lista);
            }


            // =====================================================================
            // 8) KONZOL KIIRATAS - HALLGATOK ES KAPOTT JEGYEIK
            // =====================================================================
            for (String neptun : hallgatoiJegyek.keySet()) {
                String hallgatoNev = hallgatoNevek.getOrDefault(neptun, "Ismeretlen hallgato");
                List<KapottJegy> lista = hallgatoiJegyek.get(neptun);

                System.out.println("--------------------------------");
                System.out.println("Hallgato: " + hallgatoNev);
                System.out.println("Neptun-kod: " + neptun);

                if (lista.isEmpty()) {
                    System.out.println("Nincs rogzitett jegy.");
                    System.out.println("--------------------------------\n");
                    continue;
                }

                System.out.println("Kapott jegyek:");
                for (KapottJegy jegy : lista) {
                    System.out.println("\n\tTantargy: " + jegy.tantargyNev + " (" + jegy.tkod + ")");
                    System.out.println("\tKredit: " + jegy.kredit);
                    System.out.println("\tErdemjegy: " + jegy.ertek);
                    System.out.println("\tDatum: " + jegy.datum);
                    System.out.println("\tFelev / Nap: " + jegy.felev + " | " + jegy.orarendiNap);
                    System.out.println("\tOktato neve: " + jegy.oktatoNev);
                }

                System.out.println("--------------------------------\n");
            }


            // =====================================================================
            // 9) KAPCSOLO LEKERDEZES - TANTARGYGONDOZAS (gondoz)
            // =====================================================================
            System.out.println("=================================");
            System.out.println("Tantargygondozasi nyilvantartas:");
            System.out.println("=================================");
            NodeList gondozLista = doc.getElementsByTagName("gondoz");
            for (int i = 0; i < gondozLista.getLength(); i++) {
                Element g = (Element) gondozLista.item(i);
                String tkod = g.getAttribute("tkod");
                String tanszekId = g.getAttribute("tanszekId");
                System.out.println("  Tantargy: " + tantargyNevek.getOrDefault(tkod, tkod)
                        + " [" + tkod + "] -> Tanszek: "
                        + tanszekNevek.getOrDefault(tanszekId, tanszekId) + " [" + tanszekId + "]");
            }

        } catch (Exception ex) {
            System.out.println("Hiba tortent XML beolvasas vagy lekerdezes soran:");
            ex.printStackTrace();
        }
    }

    /**
     * Segedfuggveny:
     * Visszaadja egy gyermek elem szoveget, ha letezik, egyebkent null.
     */
    private static String getTextContentOfChild(Element parent, String childTag) {
        NodeList nl = parent.getElementsByTagName(childTag);
        if (nl.getLength() == 0) {
            return null;
        }
        Element c = (Element) nl.item(0);
        if (c == null) {
            return null;
        }
        String txt = c.getTextContent();
        return txt != null ? txt.trim() : null;
    }
}