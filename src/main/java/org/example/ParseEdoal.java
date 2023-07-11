package org.example;

import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import utils.Pair;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashSet;
import java.util.Set;


public class ParseEdoal {

    private static final OWLDataFactory factory = new OWLDataFactoryImpl();

    private static final PrefixManager manager = new DefaultPrefixManager("");


    public static Set<Linkey> EDOALtoLKs(File f) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Set<Linkey> lks = new HashSet<>();
        try (Reader reader = new FileReader(f)) {
            int readSize = reader.read();
            if (readSize == -1) {
                return lks;
            }
            Document doc = builder.parse(f);
            doc.getDocumentElement().normalize();
            Node item = doc.getDocumentElement().getChildNodes().item(1);
            for (int i = 0; i < item.getChildNodes().getLength(); i++) {
                if (!item.getChildNodes().item(i).getNodeName().equals("map")) continue;
                Node item1 = item.getChildNodes().item(i).getChildNodes().item(1);
                OWLClassExpression c1 = null, c2 = null;
                for (int i1 = 0; i1 < item1.getChildNodes().getLength(); i1++) {
                    Node item2 = item1.getChildNodes().item(i1);
                    switch (item2.getNodeName()) {

                        case "entity1" ->
                                c1 = factory.getOWLClass("<" + item2.getChildNodes().item(1).getAttributes().item(0).getNodeValue() + ">", manager);

                        case "entity2" ->
                                c2 = factory.getOWLClass("<" + item2.getChildNodes().item(1).getAttributes().item(0).getNodeValue() + ">", manager);
                        case "edoal:linkkey" -> {

                            Set<Pair<OWLPropertyExpression, OWLPropertyExpression>> spIn = new HashSet<>();
                            Set<Pair<OWLPropertyExpression, OWLPropertyExpression>> spEq = new HashSet<>();
                            Pair<OWLClassExpression, OWLClassExpression> c = new Pair<>(c1, c2);
                            for (int i23 = 0; i23 < item2.getChildNodes().item(1).getChildNodes().getLength(); i23++) {
                                Node item3 = item2.getChildNodes().item(1).getChildNodes().item(i23);
                                if (item3 != null) {
                                    Node item4 = item3.getChildNodes().item(i23);

                                    if (item4 != null && !item4.getNodeName().equals("#text")) {

                                        addPair(spIn, spEq, item4);

                                    }
                                }
                            }
                            Linkey lk = new Linkey();
                            lk.setPropertySetEq(spEq);
                            lk.setPropertySetIn(spIn);
                            lk.setPairsOfConcepts(c);

                            for (Pair<OWLPropertyExpression, OWLPropertyExpression> p : spEq) {
                                if (!p.first().toString().contains("owl:sameAs") && !p.second().toString().contains("owl:sameAs")) {
                                    lks.add(lk);
                                }
                            }
                        }
                    }

                }
            }
        }

        return lks;
    }

    private static void addPair(Set<Pair<OWLPropertyExpression, OWLPropertyExpression>> spIn, Set<Pair<OWLPropertyExpression, OWLPropertyExpression>> spEq, Node item4) {
        OWLPropertyExpression p1 = null, p2 = null;
        for (int i4 = 1; i4 < item4.getChildNodes().getLength(); i4++) {

            switch (item4.getChildNodes().item(i4).getNodeName()) {
                case "edoal:property1" -> {
                    if (item4.getChildNodes().item(i4).getChildNodes().item(1).getChildNodes().item(1) != null && item4.getChildNodes().item(i4).getChildNodes().item(1).getChildNodes().item(1).getNodeName().equals("edoal:inverse")) {

                        p1 = factory.getOWLObjectInverseOf(factory.getOWLObjectProperty("<" + item4.getChildNodes().item(i4).getChildNodes().item(1).getChildNodes().item(1).getChildNodes().item(1).getAttributes().item(0).getNodeValue() + ">", manager));
                    } else {

                        p1 = factory.getOWLDataProperty("<" + item4.getChildNodes().item(i4).getChildNodes().item(1).getAttributes().item(0).getNodeValue() + ">", manager);
                    }

                }
                case "edoal:property2" -> {

                    if (item4.getChildNodes().item(i4).getChildNodes().item(1).getChildNodes().item(1) != null && item4.getChildNodes().item(i4).getChildNodes().item(1).getChildNodes().item(1).getNodeName().equals("edoal:inverse")) {
                        p2 = factory.getOWLObjectInverseOf(factory.getOWLObjectProperty("<" + item4.getChildNodes().item(i4).getChildNodes().item(1).getChildNodes().item(1).getChildNodes().item(1).getAttributes().item(0).getNodeValue() + ">", manager));
                    } else if (item4.getChildNodes().item(i4).getChildNodes().item(1).getAttributes().item(0) != null) {
                        p2 = factory.getOWLDataProperty("<" + item4.getChildNodes().item(i4).getChildNodes().item(1).getAttributes().item(0).getNodeValue() + ">", manager);
                    } else {
                        p2 = factory.getOWLDataProperty("<>", manager);
                    }

                }
            }
        }
        Pair<OWLPropertyExpression, OWLPropertyExpression> p = new Pair<>(p1, p2);
        if (item4.getNodeName().equals("edoal:Equals")) {
            spEq.add(p);
        } else {
            spIn.add(p);
        }
    }


    void saveOntologies(OWLOntology ontology, File file) throws OWLOntologyStorageException, IOException {

        file.createNewFile();
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        manager.saveOntology(ontology, ontology.getFormat(), IRI.create(file.toURI()));
    }
}
