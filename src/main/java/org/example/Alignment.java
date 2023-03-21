package org.example;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;

public class Alignment {



    static class OntologyNode {
        public static ArrayList<String> classes=new ArrayList<>();
        public static ArrayList<String> properties=new ArrayList<>();

        public ArrayList<String> getClasses() {
            return classes;
        }

        public ArrayList<String> getProperties() {
            return properties;
        }
        private String tag, name;


        private Map<String, String> attributes = new HashMap<>();
        private final List<OntologyNode> children = new ArrayList<>();
        public final OWLDataFactory factory = new OWLDataFactoryImpl();
        public static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        private OntologyNode(){}


        public static OntologyNode fromXMLNode(Node node) {
            OntologyNode ontologyNode = new OntologyNode();
            ontologyNode.tag = node.getNodeName();

            Map<String, String> attributeMap = new HashMap<>();
            NamedNodeMap attributes = node.getAttributes();

            if (attributes != null){
                for (int i = 0; i < attributes.getLength(); i++) {
                    attributeMap.put(attributes.item(i).getNodeName(), attributes.item(i).getNodeValue());
                }
            }

            ontologyNode.attributes = attributeMap;
            if (ontologyNode.attributes.containsKey("rdf:about")){
                ontologyNode.name = ontologyNode.attributes.get("rdf:about");
            }

            return ontologyNode;
        }


        public String getTag() {
            return tag;
        }

        public String getName() {
            return name;
        }

        public Map<String, String> getAttributes() {
            return attributes;
        }

        public String toMergedForm() {
           // this.name=this.name.replace("#","@");
            String[] split = tag.split(":");
            String name = this.name == null ? "" : "_" + this.name;
            StringBuilder base = new StringBuilder(split[split.length - 1] + name);
            for (OntologyNode child : children) {
                base.append("+").append(child.toMergedForm());
            }
          //  int i=base.toString().indexOf("#");
         //   base.toString().replace("#","_");

            return base.toString().replace("#","_");
        }

    }

    private OntologyNode element1, element2;
    private String relation;
    private float measure;

    Alignment(){}


    public OntologyNode getElement1() {
        return element1;
    }

    public OntologyNode getElement2() {
        return element2;
    }

    public String getRelation() {
        return relation;
    }

    public float getMeasure() {
        return measure;
    }

    public OWLOntology separate(OWLOntology o, OWLOntology merge) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {


        OWLDataFactory factory = new OWLDataFactoryImpl();
       // ParseEdoal pr=new ParseEdoal();
        OWLOntologyManager om = o.getOWLOntologyManager();
        Set<OWLAxiom> axs=new HashSet<>();
        for (OWLIndividual i:o.getIndividualsInSignature()) {
            for (OWLObjectProperty pe : merge.getObjectPropertiesInSignature()) {
                    for (OWLIndividual obj : i.getObjectPropertyValues(pe, merge)) {
                        OWLObjectPropertyAssertionAxiom opa = factory.getOWLObjectPropertyAssertionAxiom(pe, i, obj);
                        axs.add(opa);
                    }
            }
            for (OWLDataProperty pe : merge.getDataPropertiesInSignature()) {
                for (OWLLiteral obj : i.getDataPropertyValues(pe, merge)) {
                    OWLDataPropertyAssertionAxiom opa = factory.getOWLDataPropertyAssertionAxiom( pe, i, obj);
                    axs.add(opa);
                }
            }
            for (OWLClassExpression c : i.getTypes(merge)) {
                OWLClassAssertionAxiom assertion = factory.getOWLClassAssertionAxiom(c, i);
                axs.add(assertion);
            }
        }
        for(OWLObjectProperty p:merge.getObjectPropertiesInSignature()){
            om.removeAxioms(o,o.getAxioms(p));
        }
        for(OWLDataProperty p:merge.getDataPropertiesInSignature()){
            om.removeAxioms(o,o.getAxioms(p));
        }

        for(OWLClass p:merge.getClassesInSignature()){
            om.removeAxioms(o,o.getAxioms(p));
        }
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();




        return  manager.createOntology(axs);

    }

    public static Set<Alignment> readAlignments(String path) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(new File(path));
        doc.getDocumentElement().normalize();
        Node item = doc.getDocumentElement().getChildNodes().item(1);
        Set<Alignment> alignments = new HashSet<>();
        for (int i = 0; i < item.getChildNodes().getLength(); i++) {
            if (!item.getChildNodes().item(i).getNodeName().equals("map")) continue;
            Node item1 = item.getChildNodes().item(i).getChildNodes().item(1);

            Alignment alignment = new Alignment();
            for (int i1 = 0; i1 < item1.getChildNodes().getLength(); i1++) {
                Node item2 = item1.getChildNodes().item(i1);
                //System.out.println("Here: "+item2.getAttributes());
                switch (item2.getNodeName()) {
                    case "entity1" -> {
                        alignment.element1 = parseNode(item2.getChildNodes().item(1));

                    }
                    case "entity2" -> alignment.element2 = parseNode(item2.getChildNodes().item(1));
                    case "relation" -> alignment.relation = item2.getChildNodes().item(0).getNodeValue();
                    case "measure" ->
                             alignment.measure = Float.parseFloat(item2.getChildNodes().item(0).getNodeValue());
                }

            }
            alignments.add(alignment);

          //  System.out.println(alignment.getElement1().getName());
        }

        return alignments;
    }


    public static OntologyNode parseNode(Node node){

        OntologyNode ontologyNode = OntologyNode.fromXMLNode(node);

        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node item = node.getChildNodes().item(i);
            if (item.getNodeName().equals("#text")) continue;
            OntologyNode node1 = parseNode(item);
            ontologyNode.children.add(node1);
        }

        return ontologyNode;
    }
    public OWLOntology merge(File source, File target) throws OWLOntologyCreationException {
        OWLOntology merge;
        IRI mergedOntologyIRI = IRI.create("http://semanticweb.org/mergedontology/");

        Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
       // axioms.addAll(o1.getAxioms());
       // axioms.addAll(o2.getAxioms());
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology1 =  manager.loadOntologyFromOntologyDocument(source);
        OWLOntology ontology2 =   manager.loadOntologyFromOntologyDocument(target);
       // OWLOntologyMerger merger = new OWLOntologyMerger(manager);
       // merge=merger.createMergedOntology(manager,mergedOntologyIRI);
        OWLOntology mergedOntology = manager.createOntology();
        manager.addAxioms(mergedOntology, ontology1.getAxioms());
        manager.addAxioms(mergedOntology, ontology2.getAxioms());
        for (OWLEntity entity : ontology1.getSignature()) {
            IRI newIRI = IRI.create(mergedOntology.getOntologyID().getOntologyIRI() + "#" + entity.getIRI().getFragment());
            manager.applyChange(new SetOntologyID(mergedOntology, new OWLOntologyID(newIRI, mergedOntology.getOntologyID().getVersionIRI())));
        }
        for (OWLEntity entity : ontology2.getSignature()) {
            IRI newIRI = IRI.create(mergedOntology.getOntologyID().getOntologyIRI() + "#" + entity.getIRI().getFragment());
            manager.applyChange(new SetOntologyID(mergedOntology, new OWLOntologyID(newIRI, mergedOntology.getOntologyID().getVersionIRI())));
        }


        //  manager.addAxioms(merge, axioms);
        return mergedOntology;

    }


    public OWLOntology reason(OWLOntology o){

        OWLOntologyManager manager = o.getOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        IRI owlIRI = IRI.create("http://www.w3.org/2002/07/owl#");
      //  Ontology = manager.createOntology();
        OWLReasonerFactory reasoner = new StructuralReasonerFactory();
       // Ontology = manager.loadOntologyFromOntologyDocument(IRI.create("http://www.cs.ox.ac.uk/isg/ontologies/lib/RobertsFamily/2009-09-03/00775.owl"));
       // PelletReasoner reasoner = PelletReasonerFactory.getInstance().createNonBufferingReasoner(o);
        OWLReasoner r= reasoner.createReasoner(o);
        // added the derived sameAs individuals
        for(OWLIndividual i:o.getIndividualsInSignature()) {
            OWLObjectPropertyExpression sameAs = factory.getOWLObjectProperty(IRI.create(owlIRI + "owl:sameAs"));
            for(OWLIndividual i_: r.getSameIndividuals((OWLNamedIndividual) i)) {
                OWLObjectPropertyAssertionAxiom sameAsAss = factory.getOWLObjectPropertyAssertionAxiom(sameAs, i, i_);
                manager.addAxiom(o, sameAsAss);
            }
        }
        // added the derived equivalent individuals
        for(OWLClass c:o.getClassesInSignature()){
            for(OWLClass c_:r.getEquivalentClasses(c)) {
               // OWLObjectPropertyAssertionAxiom sameAsAss = factory.getOWLObjectPropertyAssertionAxiom(sameAs, i, i_);
                manager.addAxiom(o, factory.getOWLEquivalentClassesAxiom(c,c_));
            }
        }
        return o;

    }

}
