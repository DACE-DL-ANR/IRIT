package org.example;
import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxOntologyFormat;
import org.semanticweb.owlapi.formats.FunctionalSyntaxDocumentFormat;
import org.semanticweb.owlapi.io.OWLOntologyDocumentTarget;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLOntologyManagerImpl;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.HashSet;
import java.util.Set;


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
// <map>
  //  <Cell>
    //  <entity1>
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

                        c2 = factory.getOWLClass("<" +item2.getChildNodes().item(1).getAttributes().item(0).getNodeValue() + ">", manager);
                      //  System.out.println("Class 2: " + item2.getChildNodes().item(1).getAttributes().item(0).getNodeValue() );
                    }
              /*       <edoal:linkkey>
        <edoal:Linkkey>
          <edoal:binding>
            <edoal:Equals>
              <edoal:property1>
                <edoal:Property rdf:about="http://www.w3.org/2002/07/owl#sameAs"/>
              </edoal:property1>
              <edoal:property2>
                <edoal:Property rdf:about="http://ekaw#topicCoveredBy"/>
              </edoal:property2>
            </edoal:Equals>
          </edoal:binding>
        </edoal:Linkkey>
      </edoal:linkkey>*/
                    case "edoal:linkkey" -> {

                        Set<PropertyPair> spIn = new HashSet<>();
                        Set<PropertyPair> spEq = new HashSet<>();
                        ConceptPair c = new ConceptPair(c1, c2);
                        //Linkey, bindings
                        for (int i23 = 0; i23 < item2.getChildNodes().item(1).getChildNodes().getLength(); i23++) {
                            Node item3 = item2.getChildNodes().item(1).getChildNodes().item(i23);
                        //    System.out.println("item3: " + item3.getNodeName() +" "+item3.getNodeValue());
                           // for (int i3 = 0; i3 < item2.getChildNodes().item(1).getChildNodes().getLength(); i3++) {

                                //  OWLPropertyExpression p1 = null, p2 = null;

                                //items of bindings
                                if (item3 != null) {
                                    Node item4 = item3.getChildNodes().item(i23);
                                    // getChildNodes().item(i3);

                                    if (item4 != null && !item4.getNodeName().equals("#text")) {
                                     //   System.out.println("item4: " + item4.getNodeName());


                                        //  System.out.println("child of inter or equal: "+item3.getChildNodes().item(i3).getChildNodes().item(1).getNodeName());

                                        //  System.out.println("child of inter or equal att of 0: "+item3.getChildNodes().item(i3).getChildNodes().item(1).getAttributes().item(0).getNodeValue());

                                        OWLPropertyExpression p1 = null, p2 = null;
                                        for (int i4 = 1; i4 < item4.getChildNodes().getLength(); i4++) {

                                            switch (item4.getChildNodes().item(i4).getNodeName()) {
                                                case "edoal:property1" -> {

                                                    p1 = factory.getOWLDataProperty("<" + item4.getChildNodes().item(i4).getChildNodes().item(1).getAttributes().item(0).getNodeValue() + ">", manager);

                                                      //   System.out.println("prop 1 " + item4.getChildNodes().item(i4).getChildNodes().item(1).getAttributes().item(0).getNodeValue());

                                                }
                                                case "edoal:property2" -> {
                                                    p2 = factory.getOWLDataProperty("<" + item4.getChildNodes().item(i4).getChildNodes().item(1).getAttributes().item(0).getNodeValue() + ">", manager);
                                                //       System.out.println("prop 2 " + item4.getChildNodes().item(i4).getChildNodes().item(1).getAttributes().item(0).getNodeValue());
                                                }
                                            }
                                        }
                                        PropertyPair p = new PropertyPair(p1, p2);
                                        //  System.out.println("Node name: "+ item4.getNodeName());
                                        if (item4.getNodeName().equals("edoal:Equals")) {
                                            spEq.add(p);
                                        } else {
                                            spIn.add(p);
                                        }

                                    }
                                }
                            }
                        //    System.out.println("Adding a new link key");
                            Linkey lk = new Linkey();
                            //
                            lk.setPropertySetEq(spEq);
                            lk.setPropertySetIn(spIn);
                            lk.setPairsOfConcepts(c);
                      //  System.out.println("The size of eq prop is: " +lk.getPropertySetEq().size());
                      //  System.out.println("The size of in prop is: " +lk.getPropertySetIn().size());
                            lks.add(lk);
                        }
                }

            }
          //  System.out.println("The size of the parsed link keys is: " +lks.size());
        }

        return lks;
    }

    void saveOntologies(OWLOntology ontology, File file) throws OWLOntologyCreationException, OWLOntologyStorageException, IOException {

        file.createNewFile();
        OWLOntologyManager manager =  ontology.getOWLOntologyManager();

        OWLOntologyFormat format = manager.getOntologyFormat(ontology);

        manager.saveOntology(ontology, format, IRI.create(file.toURI()));

    }
}
