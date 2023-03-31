package org.example;
import org.semanticweb.HermiT.ReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import java.io.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Correspondance {
    OWLClassExpression c1;
    OWLClassExpression c2;
    OWLOntology ontology;
    private static final OWLDataFactory factory = new OWLDataFactoryImpl();
    Double valueOfCon;
    public Correspondance() {
    }
    public Correspondance(OWLClassExpression cls1, OWLClassExpression cls2) {
        this.c1=cls1;
        this.c2=cls2;
    }

    public Double getValueOfCon() {
        return valueOfCon;
    }

    public void setValueOfCon(Double valueOfCon) {
        this.valueOfCon = valueOfCon;
    }

    public OWLClassExpression getC1() {
        return c1;
    }

    public OWLClassExpression getC2() {
        return c2;
    }

    public void setC1(OWLClassExpression c1) {
        this.c1 = c1;
    }

    public void setC2(OWLClassExpression c2) {
        this.c2 = c2;
    }

    // The method adds OWL class assertions to the first ontology (o1) based on the correspondences defined in the alignment file,
// and saves the updated ontologies to temporary files.
    public void saturateCorrespondance(OWLOntology o1, OWLOntology o2, String f) throws IOException, ParserConfigurationException, SAXException {
        Set<Alignment> alignments = Alignment.readAlignments(f)
                .stream()
                .filter(alignment -> !alignment.getElement1().toMergedForm().startsWith("Relation")
                       )
                .collect(Collectors.toSet());

        Map<String, OWLClass> classMap = new HashMap<>();
        for (Alignment alignment : alignments) {
            if( alignment.getElement1().toMergedForm().startsWith("Class") && alignment.getElement2().toMergedForm().startsWith("Class")) {
                String uri1 = alignment.getElement1().toMergedForm().substring(6).replace("_", "#");
                String uri2 = alignment.getElement2().toMergedForm().substring(6).replace("_", "#");
                OWLClass cls1 = factory.getOWLClass(IRI.create(uri1));
                OWLClass cls2 = factory.getOWLClass(IRI.create(uri2));
                classMap.put(uri1, cls1);
                classMap.put(uri2, cls2);
            }
            if( alignment.getElement1().toMergedForm().startsWith("Class") && alignment.getElement2().toMergedForm().startsWith("AttributeDomainRestriction")){
                String uri1 = alignment.getElement1().toMergedForm().substring(6).replace("_", "#");
                OWLClass cls1 = factory.getOWLClass(IRI.create(uri1));
                String uri2 = alignment.getElement2().toMergedForm().replace("_","+");
                OWLClass cls2 = factory.getOWLClass(IRI.create("http://"+uri2));
                classMap.put(uri1, cls1);
                classMap.put(uri2, cls2);
            }
            if( alignment.getElement2().toMergedForm().startsWith("Class") && alignment.getElement1().toMergedForm().startsWith("AttributeDomainRestriction")){
                String uri1 = alignment.getElement1().toMergedForm().replace("_", "+");
                OWLClass cls1 = factory.getOWLClass(IRI.create("http://"+uri1));
                String uri2 = alignment.getElement2().toMergedForm().substring(6).replace("_", "#");
                OWLClass cls2 = factory.getOWLClass(IRI.create(uri2));
                classMap.put(uri1, cls1);
                classMap.put(uri2, cls2);
            }
        }

        int j = 0;
        for (Alignment alignment : alignments) {
            String uri1 = "", uri2="";
            OWLClass cls1;
            OWLClass cls2;
            if( alignment.getElement1().toMergedForm().startsWith("Class") && alignment.getElement2().toMergedForm().startsWith("Class")) {
               uri1 = alignment.getElement1().toMergedForm().substring(6).replace("_", "#");
               uri2 = alignment.getElement2().toMergedForm().substring(6).replace("_", "#");

            }
            if(alignment.getElement1().toMergedForm().startsWith("Class") && alignment.getElement2().toMergedForm().startsWith("AttributeDomainRestriction")){
                uri1 = alignment.getElement1().toMergedForm().substring(6).replace("_", "#");
                uri2 = alignment.getElement2().toMergedForm().replace("_", "+");


            }
            if(alignment.getElement2().toMergedForm().startsWith("Class") && alignment.getElement1().toMergedForm().startsWith("AttributeDomainRestriction")){
                uri1 = alignment.getElement1().toMergedForm().substring(6).replace("_", "#");
                uri2 = alignment.getElement2().toMergedForm().replace("_", "+");


            }
            cls1 = classMap.get(uri1);
            cls2 = classMap.get(uri2);
          //  System.out.println("cls1: "+cls1);
           // System.out.println("cls2: "+cls2);

            Set<OWLIndividual> indSatisfy1 = o1.getIndividualsInSignature()
                    .stream()
                    .filter(a ->  !a.getTypes(o1).isEmpty() && satisfy(a, o1, cls1))
                    .collect(Collectors.toSet());


            Set<OWLIndividual> indSatisfy2 = o2.getIndividualsInSignature()
                    .stream()
                    .filter(a ->  !a.getTypes(o2).isEmpty() &&  satisfy(a, o2, cls2))
                    .collect(Collectors.toSet());
            Set<OWLIndividual> individuals = new HashSet<>(indSatisfy1);
            individuals.addAll(indSatisfy2);

            for (OWLIndividual i : individuals) {
                OWLClassAssertionAxiom assertion1 = factory.getOWLClassAssertionAxiom(cls1, i);
                OWLClassAssertionAxiom assertion2 = factory.getOWLClassAssertionAxiom(cls2, i);
                o1.getOWLOntologyManager().addAxiom(o1, assertion1);
                o2.getOWLOntologyManager().addAxiom(o2, assertion2);
                j++;
            }
        }

        ParseEdoal parseEdoal=new ParseEdoal();


        File f_o1_temp = new File("test/source_temp.ttl");

        File f_o2_temp = new File("test/target_temp.ttl");

        System.out.println("The files have been enriched " + j + " times with assertions coming from correspondances");

        try {
            parseEdoal.saveOntologies(o1, f_o1_temp);
            parseEdoal.saveOntologies(o2, f_o2_temp);
        } catch (OWLOntologyCreationException | OWLOntologyStorageException e) {
            throw new RuntimeException(e);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean satisfy(OWLNamedIndividual a, OWLOntology o1, OWLClass cls1) {
        boolean satisfy = false;
if(cls1!=null) {
    if (cls1.toString().startsWith("AttributeDomainRestriction") && !cls1.toString().contains("inverse")) {
        System.out.println(cls1);
       // AttributeDomainRestriction+onAttribute+Relation+inverse+Relation+http://ekaw+heldIn+exists+Class+http://ekaw+Conference
        String p = cls1.toString().substring(cls1.toString().indexOf("Relation")+8, cls1.toString().indexOf("Class")-1);
        System.out.println(p);
        String c = cls1.toString().substring(cls1.toString().indexOf("Class") + 6);
        OWLObjectProperty property = factory.getOWLObjectProperty(IRI.create(p));
        OWLClass cls = factory.getOWLClass(IRI.create(c));
        for (OWLIndividual v : a.getObjectPropertyValues(property, o1)) {
            if (v.getTypes(o1).contains(cls))
                satisfy = true;
            else
                satisfy = false;
        }
    } else {

        satisfy = a.getTypes(o1).contains(cls1);

    }
}


        return satisfy;
    }

    // create a reasoner for the ontology





    public static Set<Correspondance> extractClassPairs(String f) {
        Set<Correspondance> classPairs = new HashSet<>();

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
                System.out.println("cellElement: "+cellElement);

                // Get the entity1 and entity2 elements
                Element entity1Element = (Element) cellElement.getElementsByTagName("entity1").item(0);
                Element entity2Element = (Element) cellElement.getElementsByTagName("entity2").item(0);
                System.out.println("Entity 11: "+(Element) cellElement.getElementsByTagName("entity1").item(1));
                System.out.println("Entity 21: "+(Element) cellElement.getElementsByTagName("entity1").item(1));
                System.out.println("Entity 1: "+entity1Element);
                System.out.println("Entity 2: "+entity2Element);

                // Get the RDF about attribute of the entity1 and entity2 elements
                String entity1About = entity1Element.getElementsByTagNameNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "about").item(0).getTextContent();
                String entity2About = entity2Element.getElementsByTagNameNS("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "about").item(0).getTextContent();

                // Extract the class names from the RDF about attribute values
                String class1 = entity1About.substring(entity1About.lastIndexOf('#') + 1);
                String class2 = entity2About.substring(entity2About.lastIndexOf('#') + 1);
                OWLClass cls1 = factory.getOWLClass(IRI.create("http://"+class1));
                OWLClass cls2 = factory.getOWLClass(IRI.create("http://"+class2));
                Correspondance c=new Correspondance();
                c.setC1(cls1);
                c.setC2(cls2);
                // Add the class pair to the list
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return classPairs;
    }

    /*public OWLClassExpression alignToComplexAss(String s){
        Set<OWLClassExpression> compSet = new HashSet<>();
        String cls="";
        while (s.length()>0&&s.contains("+")) {
            String oper = s.substring(0, s.indexOf("+"));

            if (oper.equals("RelationDomainRestriction")) {
                String occRel=s.substring(s.indexOf("Relation_")+9, s.indexOf("+"));
                String occCls=s.substring(s.indexOf(occRel)+occRel.length(), s.indexOf("+"));
                cls=occCls;
                compSet.add(factory.getOWLObjectSomeValuesFrom(factory.getOWLObjectProperty(IRI.create(occRel)),factory.getOWLClass(IRI.create(occCls))));

            }
            else if(oper.equals("AttributeOccurenceRestriction")){
                s=s.substring(s.indexOf("Relation_")+9);
                String occRel=s.substring(0, s.indexOf("+"));
                String occCls=s.substring(s.indexOf(occRel)+occRel.length(),  s.indexOf("+"));
                cls=occCls;
                compSet.add(factory.getOWLDataSomeValuesFrom(factory.getOWLDataProperty(IRI.create(occRel)),factory.getOWLDatatype(IRI.create(occCls))));
                System.out.println(s);
            }
            else if(oper.equals("AttributeDomainRestriction")){

                String domRel=s.substring(s.indexOf("Relation_")+9,s.indexOf(s.substring(s.indexOf("+Class")-7)) );
                s=s.substring(s.indexOf("Class_"));
                String domCls ;
                if(s.contains("+")) {
                    domCls=s.substring(s.indexOf("Class_") + 6, s.indexOf("+"));
                }
                else{
                    domCls=s.substring(s.indexOf("Class_") + 6);
                }
                compSet.add(factory.getOWLObjectAllValuesFrom(factory.getOWLObjectProperty(IRI.create(domRel)),factory.getOWLClass(IRI.create(domCls))));
                cls=domCls;
                System.out.println(cls);
            }
            if(s.contains(cls)&&s.length()>0) {
                s = s.substring(s.indexOf(cls) + cls.length() );
            }
            else{
                s="";
            }
        }

        return cls;
    }*/
    // This function allows to check if an instance satisfy class assertion in this case we can saturate this instance by
    // class expressions from the correspondance
    //Pellet crashing pb with datatypes
    //Hermit not working pb with datatypes
    // Fact++ pbs with libraries
    public static void seperateCorrespondances(String xmlFilePath) throws Exception {
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
            //Now  have to save simple correspondances in a seperate file, then remove them.
            NodeList nodesimple = doc.getElementsByTagName("Cell");
            File f_simple = new File(xmlFilePath.concat("_simple"));
            f_simple.createNewFile();

            Element root = doc_simple.createElement("rdf");
            root.setAttribute("xmlns", "http://knowledgeweb.semanticweb.org/heterogeneity/alignment#");
            root.setAttribute( "xmlns:edoal", "http://ns.inria.org/edoal/1.0/#");
            root.setAttribute( "xmlns:rdf","http://www.w3.org/1999/02/22-rdf-syntax-ns#");
            root.setAttribute( "xmlns:xsd","http://www.w3.org/2001/XMLSchema#");
            root.setAttribute( "xmlns:align", "http://knowledgeweb.semanticweb.org/heterogeneity/alignment#");
            root.setAttribute("xmlns:alext", "http://exmo.inrialpes.fr/align/ext/1.0/");
            doc_simple.appendChild(root);
            for (int j = 0; j < nodesimple.getLength(); j++) {
                Node n = nodesimple.item(j);
                Node entity1 = n.getChildNodes().item(1);
                Node entity2 = n.getChildNodes().item(3);
                if (entity1.getChildNodes().item(1).getChildNodes().getLength() == 0 && entity2.getChildNodes().item(1).getChildNodes().getLength() == 0) {

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

                    newElement_5.appendChild(doc_simple.importNode(n.getChildNodes().item(5),true));
                    newElement_5.appendChild(doc_simple.importNode(n.getChildNodes().item(7),true));

                    // remove them
                    entity1.getParentNode().getParentNode().getParentNode().removeChild(entity1.getParentNode().getParentNode());
                    entity1.getParentNode().getParentNode().removeChild(entity1.getParentNode());
                    entity1.getParentNode().removeChild(entity1);


                }
            }



        // Write the updated document back to the file
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


        }}
    catch ( IOException | TransformerException e) {
            e.printStackTrace();
        }

    }
    public static String toString(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.getBuffer().toString();
    }

}








