package org.example;

import com.clarkparsia.pellet.BranchEffectTracker;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.xml.sax.SAXException;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import utils.Pair;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Linkey {

    private static final OWLDataFactory factory = new OWLDataFactoryImpl();
    private static final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private Pair<OWLClassExpression, OWLClassExpression> pairsOfConcepts;
    private Set<Pair<OWLPropertyExpression, OWLPropertyExpression>> propertySetIn;
    private Set<Pair<OWLPropertyExpression, OWLPropertyExpression>> propertySetEq;

    public Linkey() {
    }
    public static int calculateDistance(String word1, String word2) {
        int[][] dp = new int[word1.length() + 1][word2.length() + 1];

        for (int i = 0; i <= word1.length(); i++) {
            dp[i][0] = i;
        }

        for (int j = 0; j <= word2.length(); j++) {
            dp[0][j] = j;
        }

        for (int i = 1; i <= word1.length(); i++) {
            for (int j = 1; j <= word2.length(); j++) {
                if (word1.charAt(i - 1) == word2.charAt(j - 1)) {
                    dp[i][j] = dp[i - 1][j - 1];
                } else {
                    int deletion = dp[i - 1][j] + 1;
                    int insertion = dp[i][j - 1] + 1;
                    int substitution = dp[i - 1][j - 1] + 1;

                    dp[i][j] = Math.min(deletion, Math.min(insertion, substitution));
                }
            }
        }

        return dp[word1.length()][word2.length()];
    }



    public static void addRoleAss(OWLOntology oa, OWLOntology ob, OWLIndividual a, OWLIndividual b, Set<OWLDataPropertyExpression> sprp) {

        Set<OWLAxiom> axiomsToAdd = new HashSet<>();
        for (OWLDataPropertyExpression pe : sprp) {
            if (pe.isDataPropertyExpression()) {

                for (OWLLiteral obj : EntitySearcher.getDataPropertyValues(a, pe, oa).toList()) {
                    OWLDataPropertyAssertionAxiom assertion = factory.getOWLDataPropertyAssertionAxiom(pe, b, obj);
                    axiomsToAdd.add(assertion);
                }
            } else {
                for (OWLIndividual obj : EntitySearcher.getObjectPropertyValues(a, (OWLObjectPropertyExpression) pe, oa).toList()) {
                    OWLObjectPropertyAssertionAxiom assertion = factory.getOWLObjectPropertyAssertionAxiom((OWLObjectPropertyExpression) pe, b, obj);
                    axiomsToAdd.add(assertion);
                }
            }
        }

        manager.addAxioms(ob, axiomsToAdd);
    }

    public static void addClassAss(OWLIndividual a, OWLOntology oa, OWLIndividual b, OWLOntology ob) {
        Set<OWLAxiom> axiomsToAdd = new HashSet<>();

        for (OWLClassExpression c : EntitySearcher.getTypes(a, oa).toList()) {
            OWLClassAssertionAxiom assertion = factory.getOWLClassAssertionAxiom(c, b);
            axiomsToAdd.add(assertion);
        }

        manager.addAxioms(ob, axiomsToAdd);
    }

    public static void saturateSameAs(OWLOntology o1, OWLOntology o2,String path, String number) throws IOException, ParserConfigurationException, SAXException, OWLOntologyStorageException {
        Set<Alignment> alignments = new HashSet<>();
        OWLOntologyManager manager = o1.getOWLOntologyManager();

       //System.out.println("size: "+instAl.size());
        if (number == "1"){
            alignments = Alignment.readAlignmentsEdoal(path).stream().filter(alignment -> alignment.getElement1().getTag().equals("INST")).collect(Collectors.toSet());
        }
        else if(number == "2") {
            alignments = Alignment.readAlignments(path).stream().filter(alignment -> alignment.getElement1().getTag().equals("INST")).collect(Collectors.toSet());

        }
        else {
            alignments = Alignment.readAlignmentsTxt(Path.of(path)).stream().filter(alignment -> alignment.getElement1().getTag().equals("INST")).collect(Collectors.toSet());

        }
      //  System.out.println(alignments.size());
      //  alignments = alignments.stream().filter(alignment -> alignment.getElement1().getTag().equals("INST")).collect(Collectors.toSet());
        Set<OWLAxiom> axiomsToAdd=new HashSet<>();
        Set<OWLAxiom> axiomsToAdd2=new HashSet<>();
        System.out.println(alignments.size());
        for (Alignment al : alignments) {

            OWLNamedIndividual a = factory.getOWLNamedIndividual(al.getElement1().attributes.get("rdf:resource"));

            OWLNamedIndividual b = factory.getOWLNamedIndividual(al.getElement2().attributes.get("rdf:resource"));

            if(!o1.containsAxiom(factory.getOWLSameIndividualAxiom(a,b))&&a.toString().contains("resource")&&b.toString().contains("resource")) {
                {

                   axiomsToAdd.add(factory.getOWLSameIndividualAxiom(a, b));

                    for (OWLAnnotationAssertionAxiom axiom : o2.getAnnotationAssertionAxioms(b.getIRI())) {

                        if (axiom.getProperty().isLabel()) {

                            OWLLiteral label = (OWLLiteral) axiom.getValue();
                           // String labelText = label.getLiteral();

                            axiomsToAdd.add(factory.getOWLAnnotationAssertionAxiom(axiom.getProperty(),a.getIRI() , label));
                            axiomsToAdd.add(factory.getOWLAnnotationAssertionAxiom(axiom.getProperty(),a.getEntityType().getIRI() , label));
                            axiomsToAdd2.add(factory.getOWLAnnotationAssertionAxiom(axiom.getProperty(),b.getEntityType().getIRI() , label));
                          //  System.out.println("Label of the individual: " + labelText);
                        }
                    }
               }
            }

        }
        manager.addAxioms(o2,axiomsToAdd2);
        manager.addAxioms(o1,axiomsToAdd);

       // manager.saveOntology(o1,o1.getFormat(), IRI.create(new File("output/source_tmp.xml").toURI()));
    }

    private static Map<OWLNamedIndividual, String> getOwlNamedIndividualSetMap(OWLOntology o1, OWLDataPropertyExpression p1) {
        return o1.getIndividualsInSignature()
                .stream()
                .map(a -> Map.entry(a, EntitySearcher.getDataPropertyValues(a, p1, o1).collect(Collectors.toSet())))
                .filter(a -> a.getValue().size() > 0)
                .map(a -> Map.entry(a.getKey(), a.getValue().toString().substring(1, a.getValue().toString().length() - 1)))
                .filter(a -> a.getValue().length() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static void caller(OWLIndividual a, OWLOntology o1, OWLIndividual b, OWLOntology o2) {
        addClassAss(b, o2, a, o1);
        addClassAss(a, o1, b, o2);
        addRoleAss(o1, o2, a, b, EntitySearcher.getDataPropertyValues(a, o1).keySet());
        addRoleAss(o2, o1, b, a, EntitySearcher.getDataPropertyValues(b, o2).keySet());
    }

    void saturateLinkey(OWLOntology o1, OWLOntology o2) throws OWLOntologyStorageException {
        Set<Pair<OWLNamedIndividual, OWLNamedIndividual>> s1=new HashSet<>(),s2=new HashSet<>(),s3=new HashSet<>();

        int i = 0;


        Set<OWLPropertyExpression> slkp1Eq = new HashSet<>();
        Set<OWLPropertyExpression> slkp2Eq = new HashSet<>();

        if (getPropertySetIn() != null) {
            for (Pair<OWLPropertyExpression, OWLPropertyExpression> p : getPropertySetEq()) {
                slkp1Eq.add(p.first());
                slkp2Eq.add(p.second());
            }
        }
        Set<Pair<OWLNamedIndividual, OWLNamedIndividual>> intersection = new HashSet<>();

        for (OWLPropertyExpression p1 : slkp1Eq) {

            for (OWLPropertyExpression p2 : slkp2Eq) {


                if (p2.toString().equals("rdfs:label") && p1.toString().equals("rdfs:label")) {
                    s1 = case1(o1, o2);
                    if(!intersection.isEmpty())intersection.retainAll( s1);
                    else intersection.addAll(s1);
                 //   System.out.println("s1: "+s1.size());
                }
                if (p2.toString().equals("rdfs:label")) {
                    s2 = case2(o1, o2, p1);
                    if(!intersection.isEmpty())intersection.retainAll( s2);
                    else intersection.addAll(s2);
                 //   System.out.println("s2: "+s2.size());
                } else {
                    s3 = case3(o1, o2, (OWLDataPropertyExpression) p1, (OWLDataPropertyExpression) p2);
                    if(!intersection.isEmpty())intersection.retainAll( s3);
                    else intersection.addAll(s3);
                //    System.out.println("s3: "+s3.size());
                }
            }
        }
             //   System.out.println("The size of the intersection is: "+intersection.size());

                for(Pair<OWLNamedIndividual, OWLNamedIndividual> p:intersection){
                    i++;
                    //  o1.getOWLOntologyManager().addAxiom(o1, factory.getOWLSameIndividualAxiom( p.first(), p.second()));
                    if(!o2.containsAxiom(factory.getOWLSameIndividualAxiom(p.first(), p.second()))) {
                     o2.add( factory.getOWLSameIndividualAxiom(p.first(), p.second()));
                    }
                    Stream<OWLAnnotation> literal = EntitySearcher.getAnnotations( p.first(), o1);
                    String label = literal.toList().get(0).getValue().toString();
                    EntitySearcher.getAnnotations(p.second(), o2);
                    // Create the label annotation
                    OWLAnnotationProperty labelProperty = factory.getRDFSLabel();

                    OWLLiteral labelValue = factory.getOWLLiteral(label);
                    // Create the annotation assertion
                    OWLAnnotationAssertionAxiom annotationAssertion =
                            factory.getOWLAnnotationAssertionAxiom(labelProperty, p.second().getIRI(), labelValue);
                    if(!o2.containsAxiom(annotationAssertion)) {
                        manager.addAxiom(o2, annotationAssertion);
                       // System.out.println(manager.addAxiom(o2, annotationAssertion));
                    }
                }
              //  System.out.println("We have added: "+i+" sameAs.");
            }


    private Set<Pair<OWLNamedIndividual, OWLNamedIndividual>> case3(OWLOntology o1, OWLOntology o2, OWLDataPropertyExpression p1, OWLDataPropertyExpression p2) {

        Set<Pair<OWLNamedIndividual, OWLNamedIndividual>> pei=new HashSet<>();
        Map<OWLNamedIndividual, String> dataPropertyValues1 = getOwlNamedIndividualSetMap(o1, p1);

        Map<OWLNamedIndividual, String> dataPropertyValues2 = getOwlNamedIndividualSetMap(o2, p2);
      //  System.out.println("This property " +p2+" has the following nbr of values: "+p2.getIndividualsInSignature());
      //  System.out.println("This property " +p2+" has the following nbr of values: "+dataPropertyValues2.size());
        for (Map.Entry<OWLNamedIndividual, String> a : dataPropertyValues1.entrySet()) {


            String substring1 = a.getValue();


            for (Map.Entry<OWLNamedIndividual, String> b : dataPropertyValues2.entrySet().stream().filter(d -> calculateDistance(substring1, d.getValue()) > 0.7).toList()) {
                Pair<OWLNamedIndividual, OWLNamedIndividual> p = new Pair<>(a.getKey(), b.getKey());
                pei.add(p);
            }

        }
            return pei;
            }



    private static void saturateSameAs(OWLOntology o1, OWLOntology o2){
   //Alignment.readAlignmentsTxt("");
        Set<Alignment> alignments = Alignment.readAlignmentsTxt(Paths.get(""));
        Set<Alignment> instAl = alignments.stream().filter(alignment -> alignment.getElement1().getTag().equals("INST")).collect(Collectors.toSet());
        for(Alignment al:instAl) {
            OWLNamedIndividual a= factory.getOWLNamedIndividual(al.getElement1().getName());
            OWLNamedIndividual b= factory.getOWLNamedIndividual(al.getElement2().getName());
           // o1.getIndividualsInSignature().stream().filter(ind->ind.equals(al.getElement1().getName())).collect(Collectors.toSet());
            caller(a, o1, b, o2);
            manager.addAxiom(o1, factory.getOWLSameIndividualAxiom(a, b));
            manager.addAxiom(o2, factory.getOWLSameIndividualAxiom(a, b));
        }
        //
    }



    private Set<Pair<OWLNamedIndividual, OWLNamedIndividual>> case2(OWLOntology o1, OWLOntology o2, OWLPropertyExpression p1) {
        Set<Pair<OWLNamedIndividual, OWLNamedIndividual>> pei=new HashSet<>();
        for (OWLNamedIndividual a : o1.getIndividualsInSignature()) {

            for (OWLAnnotationAssertionAxiom assertion : o1.getAnnotationAssertionAxioms(a.getIRI()).stream().filter(ass->ass.getProperty().toString().equals(p1.toString())).toList()) {

                        String aValue = assertion.getValue().toString();

                        for (OWLNamedIndividual b : o2.getIndividualsInSignature().stream().filter(b -> EntitySearcher.getAnnotations(b, o2) != null).toList()) {

                            String annotationValue = EntitySearcher.getAnnotations(b, o2).toList().toString();
                            if (calculateDistance(annotationValue, aValue) > 0.7||annotationValue.contains(aValue)) {

                                Pair<OWLNamedIndividual, OWLNamedIndividual> p = new Pair<>(a, b);
                                pei.add(p);
                            }
                        }

                }
            }

return pei;

    }

    private Set<Pair<OWLNamedIndividual, OWLNamedIndividual>> case1(OWLOntology o1, OWLOntology o2) throws OWLOntologyStorageException {
        Set<Pair<OWLNamedIndividual, OWLNamedIndividual>> pei=new HashSet<>();
        List<Pair<OWLNamedIndividual, OWLNamedIndividual>> collect = o1.getIndividualsInSignature()
                .parallelStream()
                .flatMap(a -> o2.getIndividualsInSignature()
                        .parallelStream()
                        .filter(b -> EntitySearcher.getAnnotations(b, o2).toList().size() > 0 && EntitySearcher.getAnnotations(a, o1).toList().size() > 0
                        ).map(b -> new Pair<>(a, b))
                ).toList();


        for (Pair<OWLNamedIndividual, OWLNamedIndividual> pair : collect) {
            OWLNamedIndividual a = pair.first();
            OWLNamedIndividual b = pair.second();

            String o1Annotations = EntitySearcher.getAnnotations(a, o1).toString();
            String o2Annotations = EntitySearcher.getAnnotations(b, o2).toString();

            String s1 = o1Annotations.substring(o1Annotations.indexOf("\"") + 1, o1Annotations.lastIndexOf("\""));
            String s2 = o2Annotations.substring(o2Annotations.indexOf("\"") + 1, o2Annotations.lastIndexOf("\""));

            if (calculateDistance(s1,s2)>0.7) {

                Pair<OWLNamedIndividual, OWLNamedIndividual> p = new Pair<>(a, b);
                pei.add(p);
            }}
return pei;
    }

    public void setPairsOfConcepts(Pair<OWLClassExpression, OWLClassExpression> pairsOfConcepts) {
        this.pairsOfConcepts = pairsOfConcepts;
    }

    public Set<Pair<OWLPropertyExpression, OWLPropertyExpression>> getPropertySetIn() {
        return propertySetIn;
    }

    public void setPropertySetIn(Set<Pair<OWLPropertyExpression, OWLPropertyExpression>> propertySetIn) {
        this.propertySetIn = propertySetIn;
    }

    public Set<Pair<OWLPropertyExpression, OWLPropertyExpression>> getPropertySetEq() {
        return propertySetEq;
    }

    public void setPropertySetEq(Set<Pair<OWLPropertyExpression, OWLPropertyExpression>> propertySetEq) {
        this.propertySetEq = propertySetEq;
    }

    public void printLk() {

        if (getPropertySetEq() != null) {

            int i = 0;
            for (Pair<OWLPropertyExpression, OWLPropertyExpression> pp : getPropertySetEq()) {
                i++;

            }
        }
        if (pairsOfConcepts != null) ;
    }

}