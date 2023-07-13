package org.example;

import org.apache.jena.base.Sys;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import javax.xml.parsers.DocumentBuilder;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Correspondence {
    private static final OWLDataFactory factory = new OWLDataFactoryImpl();
    OWLClassExpression c1;
    OWLClassExpression c2;

    public Correspondence() {
    }

    public Correspondence(OWLClassExpression cls1, OWLClassExpression cls2) {
        this.c1 = cls1;
        this.c2 = cls2;
    }

    private static Map<String, OWLClass> getStringOWLClassMap(Set<Alignment> alignments) {
        Map<String, OWLClass> classMap = new HashMap<>();
        for (Alignment alignment : alignments) {

            String e1MergedForm = alignment.getElement1().toMergedForm();
            String e2MergedForm = alignment.getElement2().toMergedForm();

            if (e1MergedForm.startsWith("Class") && e2MergedForm.startsWith("Class")) {
                String uri1 = e1MergedForm.substring(6).replace("_", "#");
                String uri2 = e2MergedForm.substring(6).replace("_", "#");
                OWLClass cls1 = factory.getOWLClass(IRI.create(uri1));
                OWLClass cls2 = factory.getOWLClass(IRI.create(uri2));
                classMap.put(uri1, cls1);
                classMap.put(uri2, cls2);
            }
            if (e1MergedForm.startsWith("Class") && e2MergedForm.startsWith("AttributeDomainRestriction")) {
                String uri1 = e1MergedForm.substring(6).replace("_", "#");
                String uri2 = e2MergedForm.replace("_", "+");
                OWLClass cls1 = factory.getOWLClass(IRI.create(uri1));
                OWLClass cls2 = factory.getOWLClass(IRI.create("http://" + uri2));
                classMap.put(uri1, cls1);
                classMap.put(uri2, cls2);
            }
            if (e1MergedForm.startsWith("AttributeDomainRestriction") && e2MergedForm.startsWith("Class")) {
                String uri1 = e1MergedForm.replace("_", "+");
                String uri2 = e2MergedForm.substring(6).replace("_", "#");
                OWLClass cls1 = factory.getOWLClass(IRI.create("http://" + uri1));
                OWLClass cls2 = factory.getOWLClass(IRI.create(uri2));
                classMap.put(uri1, cls1);
                classMap.put(uri2, cls2);
            }
        }
        return classMap;
    }

    public static Set<Correspondence> extractClassPairs(String f) {
        Set<Correspondence> classPairs = new HashSet<>();

        try {
            // Load the XML document
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(f);
            doc.getDocumentElement().normalize();

            // Find all 'Cell' elements
            NodeList cellList = doc.getElementsByTagName("Cell");
            for (int i = 0; i < cellList.getLength(); i++) {
                Element cellElement = (Element) cellList.item(i);
                System.out.println("cellElement: " + cellElement);

                // Get the entity1 and entity2 elements
                Element entity1Element = (Element) cellElement.getElementsByTagName("entity1").item(0);
                Element entity2Element = (Element) cellElement.getElementsByTagName("entity2").item(0);
                System.out.println("Entity 11: " + cellElement.getElementsByTagName("entity1").item(1));
                System.out.println("Entity 21: " + cellElement.getElementsByTagName("entity1").item(1));
                System.out.println("Entity 1: " + entity1Element);
                System.out.println("Entity 2: " + entity2Element);

                // Get the RDF about attribute of the entity1 and entity2 elements
                String entity1About = entity1Element.getElementsByTagNameNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "about").item(0).getTextContent();
                String entity2About = entity2Element.getElementsByTagNameNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "about").item(0).getTextContent();

                // Extract the class names from the RDF about attribute values
                String class1 = entity1About.substring(entity1About.lastIndexOf('#') + 1);
                String class2 = entity2About.substring(entity2About.lastIndexOf('#') + 1);
                OWLClass cls1 = factory.getOWLClass(IRI.create("http://" + class1));
                OWLClass cls2 = factory.getOWLClass(IRI.create("http://" + class2));
                Correspondence c = new Correspondence();
                c.setC1(cls1);
                c.setC2(cls2);
                // Add the class pair to the list
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return classPairs;
    }



    public static void saturateCorrespondenceSimple(OWLOntology ontology1, OWLOntology ontology2, String fs) throws IOException, ParserConfigurationException, SAXException {
        //  Path path = Paths.get(fs);

        Set<Alignment> alignments = Alignment.readAlignmentsAt(fs)
                .stream()
                .filter(alignment ->alignment.getElement1().toString().contains("class"))
                .collect(Collectors.toSet());
        System.out.println("size: "+alignments.size());
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        for (Alignment al : alignments) {

            String name1 = "<" + al.getElement1().attributes.get("rdf:resource") + ">";

            for (OWLNamedIndividual e1 : ontology1.getIndividualsInSignature()) {
                Set<String> collect = EntitySearcher.getTypes(e1, ontology1).map(Objects::toString).collect(Collectors.toSet());
                if (collect.contains(name1)) {
                    OWLClassAssertionAxiom owlClassAssertionAxiom = factory.getOWLClassAssertionAxiom(factory.getOWLClass(al.getElement2().attributes.get("rdf:resource")), e1);
                    manager.addAxiom(ontology1, owlClassAssertionAxiom);
                }
            }

        /*    for (OWLNamedIndividual e2 : ontology2.getIndividualsInSignature()) {
                Set<String> collect = EntitySearcher.getTypes(e2, ontology2).map(Objects::toString).collect(Collectors.toSet());
                if (collect.contains(name2)) {
                    OWLClassAssertionAxiom owlClassAssertionAxiom = factory.getOWLClassAssertionAxiom(factory.getOWLClass(al.getElement1().getName()), e2);
                    manager.addAxiom(ontology2, owlClassAssertionAxiom);
                }
            }*/
        }
    }

    public static void separateCorrespondences(String xmlFilePath) throws Exception {
        try {
            // Create a DocumentBuilderFactory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new File(xmlFilePath));
            Document doc_simple = builder.newDocument();
            NodeList nodes = doc.getElementsByTagName("edoal:Relation");
            for (int i = 0; i < nodes.getLength(); i++) {
                Node node = nodes.item(i);
                String parent = node.getParentNode().getNodeName();
                if (node.getParentNode() != null && (parent.equals("entity1") || parent.equals("entity2"))) {
                    //remove simple
                    node.getParentNode().getParentNode().getParentNode().getParentNode().removeChild(node.getParentNode().getParentNode().getParentNode());
                    //cell
                    node.getParentNode().getParentNode().getParentNode().removeChild(node.getParentNode().getParentNode());
                    //entity1
                    NodeList childNodes = node.getChildNodes();
                    Node n = node.getParentNode();
                    for (int j = 0; j < childNodes.getLength(); j++) {
                        Node childNode = childNodes.item(j);
                        node.removeChild(childNode);
                    }
                    node.getParentNode().removeChild(node);
                    n.getParentNode().removeChild(n);
                }

            }

            NodeList nodesimple = doc.getElementsByTagName("Cell");
            File f_simple = new File(xmlFilePath.concat("_simple"));
            f_simple.createNewFile();

            Element root = doc_simple.createElement("rdf");
            root.setAttribute("xmlns", "http://knowledgeweb.semanticweb.org/heterogeneity/alignment#");
            root.setAttribute("xmlns:edoal", "http://ns.inria.org/edoal/1.0/#");
            root.setAttribute("xmlns:rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
            root.setAttribute("xmlns:xsd", "http://www.w3.org/2001/XMLSchema#");
            root.setAttribute("xmlns:align", "http://knowledgeweb.semanticweb.org/heterogeneity/alignment#");
            root.setAttribute("xmlns:alext", "http://exmo.inrialpes.fr/align/ext/1.0/");
            doc_simple.appendChild(root);

            for (int j = 0; j < nodesimple.getLength(); j++) {
                Node n = nodesimple.item(j);
                Node entity1 = n.getChildNodes().item(1);
                Node entity2 = n.getChildNodes().item(3);
                if (entity1.getChildNodes().item(1)!=null&&entity2.getChildNodes().item(1)!=null&&entity1.getChildNodes().item(1).getChildNodes().getLength() == 0 && entity2.getChildNodes().item(1).getChildNodes().getLength() == 0) {

                    Element newElement_1 = doc_simple.createElement(n.getParentNode().getNodeName());
                    root.appendChild(newElement_1);

                    Element newElement_2 = doc_simple.createElement(n.getNodeName());
                    newElement_1.appendChild(newElement_2);

                    Element newElement_3 = doc_simple.createElement(entity1.getNodeName());
                    newElement_2.appendChild(newElement_3);

                    Element newElement_4 = (Element) entity1.getChildNodes().item(1);
                    Element imported_4 = (Element) doc_simple.importNode(newElement_4, true);
                    newElement_3.appendChild(imported_4);

                    Element newElement_5 = doc_simple.createElement(entity2.getNodeName());

                    newElement_2.appendChild(newElement_5);

                    Element newElement_6 = (Element) entity2.getChildNodes().item(1);
                    Element imported_6 = (Element) doc_simple.importNode(newElement_6, true);
                    newElement_5.appendChild(imported_6);

                    newElement_5.appendChild(doc_simple.importNode(n.getChildNodes().item(5), true));
                    newElement_5.appendChild(doc_simple.importNode(n.getChildNodes().item(7), true));

                    entity1.getParentNode().getParentNode().getParentNode().removeChild(entity1.getParentNode().getParentNode());
                    entity1.getParentNode().getParentNode().removeChild(entity1.getParentNode());
                    entity1.getParentNode().removeChild(entity1);


                }
            }


            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            DOMSource source_simple = new DOMSource(doc_simple);
            Path xmlPath = Paths.get(xmlFilePath);

            StreamResult result_simple = new StreamResult(f_simple);
            transformer.transform(source_simple, result_simple);

            try (OutputStream out = new FileOutputStream(xmlPath.toFile())) {

                StreamResult result = new StreamResult(out);
                transformer.transform(source, result);


            }
        } catch (IOException | TransformerException e) {
            e.printStackTrace();
        }

    }

    public OWLClassExpression getC1() {
        return c1;
    }

    public void setC1(OWLClassExpression c1) {
        this.c1 = c1;
    }

    // create a reasoner for the ontology

    public OWLClassExpression getC2() {
        return c2;
    }

    public void setC2(OWLClassExpression c2) {
        this.c2 = c2;
    }

    public static void saturateCorrespondence(OWLOntology o1, OWLOntology o2, String f) throws IOException, ParserConfigurationException, SAXException {


    }

    private static int getJ(OWLOntology o1, OWLOntology o2, Set<Alignment> alignments, Map<String, OWLClass> classMap) {
        int j = 0;
        for (Alignment alignment : alignments) {
            String uri1 = "", uri2 = "";

            String e1MergedForm = alignment.getElement1().toMergedForm();
            String e2MergedForm = alignment.getElement2().toMergedForm();

            if (e1MergedForm.startsWith("Class") && e2MergedForm.startsWith("Class")) {
                uri1 = e1MergedForm.substring(6).replace("_", "#");
                uri2 = e2MergedForm.substring(6).replace("_", "#");
            }

            if (e1MergedForm.startsWith("Class") && e2MergedForm.startsWith("AttributeDomainRestriction")) {
                uri1 = e1MergedForm.substring(6).replace("_", "#");
                uri2 = e2MergedForm.replace("_", "+");
            }

            if (e1MergedForm.startsWith("AttributeDomainRestriction") && e2MergedForm.startsWith("Class")) {
                uri1 = e1MergedForm.substring(6).replace("_", "#");
                uri2 = e2MergedForm.replace("_", "+");
            }

            OWLClass cls1 = classMap.get(uri1);
            OWLClass cls2 = classMap.get(uri2);

            Set<OWLIndividual> indSatisfy1 = o1.getIndividualsInSignature()
                    .stream()
                    .filter(a -> !EntitySearcher.getTypes(a, o1).toList().isEmpty() && satisfy(a, o1, cls1))
                    .collect(Collectors.toSet());

            Set<OWLIndividual> indSatisfy2 = o2.getIndividualsInSignature()
                    .stream()
                    .filter(a -> !EntitySearcher.getTypes(a, o2).toList().isEmpty() && satisfy(a, o2, cls2))
                    .collect(Collectors.toSet());

            Set<OWLIndividual> individuals = new HashSet<>(indSatisfy1);
            individuals.addAll(indSatisfy2);
            j += individuals.size();

            for (OWLIndividual i : individuals) {
                OWLClassAssertionAxiom assertion1 = factory.getOWLClassAssertionAxiom(cls1, i);
                OWLClassAssertionAxiom assertion2 = factory.getOWLClassAssertionAxiom(cls2, i);
                o1.getOWLOntologyManager().addAxiom(o1, assertion1);
                o2.getOWLOntologyManager().addAxiom(o2, assertion2);
            }
        }

        return j;
    }

    public static boolean satisfy(OWLNamedIndividual a, OWLOntology o1, OWLClass cls1) {

        if (cls1 == null) {
            return false;
        }

        boolean satisfy = false;

        if (cls1.toString().startsWith("AttributeDomainRestriction") && !cls1.toString().contains("inverse")) {
            String p = cls1.toString().substring(cls1.toString().indexOf("Relation") + 8, cls1.toString().indexOf("Class") - 1);
            String c = cls1.toString().substring(cls1.toString().indexOf("Class") + 6);
            OWLObjectProperty property = factory.getOWLObjectProperty(IRI.create(p));
            OWLClass cls = factory.getOWLClass(IRI.create(c));
            for (OWLIndividual v : EntitySearcher.getObjectPropertyValues(a, property, o1).toList()) {
                satisfy = EntitySearcher.getTypes(v, o1).toList().contains(cls);
            }
        } else {

            return EntitySearcher.getTypes(a, o1).toList().contains(cls1);

        }

        return satisfy;
    }
}



