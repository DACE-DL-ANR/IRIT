package org.example;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;
import org.semanticweb.owlapi.model.parameters.Imports;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;


public class ParseEdoal {

    File edoal;
    private static OWLDataFactory factory = new OWLDataFactoryImpl();

    private static PrefixManager manager = new DefaultPrefixManager("");
    public File getEdoal() {
        return edoal;
    }

    public void setEdoal(File edoal) {
        this.edoal = edoal;
    }


    public static Set<Linkey> EDOALtoLKs(File f) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Set<Linkey> lks = new HashSet<>();
        Reader reader = new FileReader(f);
        int readSize = reader.read();
        if (readSize == -1)
        {
            return lks;
        }
        Document doc = builder.parse(f);
        doc.getDocumentElement().normalize();
        Node item = doc.getDocumentElement().getChildNodes().item(1);
        for (int i = 0; i < item.getChildNodes().getLength(); i++) {
            if (!item.getChildNodes().item(i).getNodeName().equals("map")) continue;
            //cell
            Node item1 = item.getChildNodes().item(i).getChildNodes().item(1);
            OWLClassExpression c1 = null,c2=null;
            for (int i1 = 0; i1 < item1.getChildNodes().getLength(); i1++) {
//entity1, entity2, linkkey
                Node item2 = item1.getChildNodes().item(i1);
                switch (item2.getNodeName()) {

                    case "entity1" -> {
                        c1=factory.getOWLClass("<" + item2.getChildNodes().item(1).getAttributes().item(0).getNodeValue()  + ">", manager);
                      //  System.out.println("Class 1: " + item2.getChildNodes().item(1).getAttributes().item(0).getNodeValue() );
                    }

                    case "entity2" -> {

                        c2 = factory.getOWLClass("<" + item2.getChildNodes().item(1).getAttributes().item(0).getNodeValue() + ">", manager);
                    }
                    case "edoal:linkkey" -> {

                        Set<PropertyPair> spIn = new HashSet<>();
                        Set<PropertyPair> spEq = new HashSet<>();
                        ConceptPair c = new ConceptPair(c1, c2);
                        //Linkey, bindings
                        for (int i23 = 0; i23 < item2.getChildNodes().item(1).getChildNodes().getLength(); i23++) {
                            Node item3 = item2.getChildNodes().item(1).getChildNodes().item(i23);
                            //items of bindings
                            if (item3 != null) {
                                Node item4 = item3.getChildNodes().item(i23);
                                // getChildNodes().item(i3);

                                if (item4 != null && !item4.getNodeName().equals("#text")) {

                                    OWLPropertyExpression p1 = null, p2 = null;
                                    for (int i4 = 1; i4 < item4.getChildNodes().getLength(); i4++) {

                                        switch (item4.getChildNodes().item(i4).getNodeName()) {
                                            case "edoal:property1" -> {
                                                if(item4.getChildNodes().item(i4).getChildNodes().item(1).getChildNodes().item(1)!=null&&item4.getChildNodes().item(i4).getChildNodes().item(1).getChildNodes().item(1).getNodeName().equals("edoal:inverse")){

                                                 //   System.out.println("1: "+item4.getChildNodes().item(i4).getChildNodes().item(1).getChildNodes().item(1).getChildNodes().item(1).getAttributes().item(0).getNodeValue());
                                                    p1 = factory.getOWLObjectInverseOf(factory.getOWLObjectProperty("<"+ item4.getChildNodes().item(i4).getChildNodes().item(1).getChildNodes().item(1).getChildNodes().item(1).getAttributes().item(0).getNodeValue() +">", manager));
                                                }
                                                 else{

                                                    p1 = factory.getOWLDataProperty("<" + item4.getChildNodes().item(i4).getChildNodes().item(1).getAttributes().item(0).getNodeValue() + ">", manager);
                                                }
                                                //   System.out.println("prop 1 " + item4.getChildNodes().item(i4).getChildNodes().item(1).getAttributes().item(0).getNodeValue());

                                            }
                                            case "edoal:property2" -> {

                                                if(item4.getChildNodes().item(i4).getChildNodes().item(1).getChildNodes().item(1)!=null&&item4.getChildNodes().item(i4).getChildNodes().item(1).getChildNodes().item(1).getNodeName().equals("edoal:inverse")){
                                              //   System.out.println("2: "+item4.getChildNodes().item(i4).getChildNodes().item(1).getChildNodes().item(1).getChildNodes().item(1).getAttributes().item(0).getNodeValue());
                                                    p2 = factory.getOWLObjectInverseOf(factory.getOWLObjectProperty("<"+ item4.getChildNodes().item(i4).getChildNodes().item(1).getChildNodes().item(1).getChildNodes().item(1).getAttributes().item(0).getNodeValue() +">", manager));
                                                }
                                              //  OWLObjectPropertyExpression inverseProperty = factory.getOWLObjectInverseOf(forwardProperty);
                                             else
                                                 if(item4.getChildNodes().item(i4).getChildNodes().item(1).getAttributes().item(0) != null) {
                                                        p2 = factory.getOWLDataProperty("<" + item4.getChildNodes().item(i4).getChildNodes().item(1).getAttributes().item(0).getNodeValue() + ">", manager);
                                                    } else {
                                                        p2 = factory.getOWLDataProperty("<>", manager);
                                                    }
                                                    //       System.out.println("prop 2 " + item4.getChildNodes().item(i4).getChildNodes().item(1).getAttributes().item(0).getNodeValue());

                                            }
                                        }
                                    }
                                    PropertyPair p = new PropertyPair(p1, p2);
                                    if (item4.getNodeName().equals("edoal:Equals")) {
                                        spEq.add(p);
                                    }
                                    else {
                                        spIn.add(p);
                                    }

                                }
                            }
                        }
                        Linkey lk = new Linkey();
                        lk.setPropertySetEq(spEq);
                        lk.setPropertySetIn(spIn);
                        lk.setPairsOfConcepts(c);

                        for (PropertyPair p:spEq) {
//! p.getFirstProperty().equals(p.getSecondProperty())&&
                            if (!p.getFirstProperty().toString().contains("owl:sameAs")&&!p.getSecondProperty().toString().contains("owl:sameAs")) {
                               //  System.out.println("Here");
                                lks.add(lk);
                            }
                        }
                    }
                }

            }
        }

        return lks;
    }


    Set<OWLClassExpression> corr(File f){
        Set<OWLClassExpression> classExpressions=new HashSet<>();
        try {
            OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
            OWLOntology ontology = manager.loadOntologyFromOntologyDocument(f);
            for (OWLEquivalentClassesAxiom equivAxiom : ontology.getAxioms(AxiomType.EQUIVALENT_CLASSES)) {
                classExpressions.addAll(equivAxiom.getClassExpressions());
            }
        }
        catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
    return classExpressions;
}



    void saveOntologies(OWLOntology ontology, File file) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException, URISyntaxException {

        file.createNewFile();
        OWLOntologyManager manager =  ontology.getOWLOntologyManager();
        manager.saveOntology(ontology, new TurtleOntologyFormat(), IRI.create(file.toURI()));
    }
}
