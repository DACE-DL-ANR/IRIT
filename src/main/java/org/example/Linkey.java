package org.example;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import utils.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Linkey {

    private Pair<OWLClassExpression, OWLClassExpression> pairsOfConcepts;

    private static final OWLDataFactory factory = new OWLDataFactoryImpl();


    private static final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
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


    void saturateLinkey(OWLOntology o1, OWLOntology o2) throws OWLOntologyStorageException {


        AtomicInteger i = new AtomicInteger();


        Set<OWLPropertyExpression> slkp1Eq = new HashSet<>();
        Set<OWLPropertyExpression> slkp2Eq = new HashSet<>();

        if (getPropertySetIn() != null) {
            for (Pair<OWLPropertyExpression, OWLPropertyExpression> p : getPropertySetEq()) {
                slkp1Eq.add(p.first());
                slkp2Eq.add(p.second());
            }
        }

        for (OWLPropertyExpression p1 : slkp1Eq) {
            for (OWLPropertyExpression p2 : slkp2Eq) {


                if (p2.toString().equals("rdfs:label") && p1.toString().equals("rdfs:label")) {

                    case1(o1, o2, i);
                }
                if (p2.toString().equals("rdfs:label")) {

                    case2(o1, o2, i, p1);

                }
                else {

                    case3(o1, o2, i, (OWLDataPropertyExpression) p1, (OWLDataPropertyExpression) p2);
                }
            }
        }


    }


    private void case3(OWLOntology o1, OWLOntology o2, AtomicInteger i, OWLDataPropertyExpression p1, OWLDataPropertyExpression p2) {

        Map<OWLNamedIndividual, String> dataPropertyValues1 = getOwlNamedIndividualSetMap(o1, p1);

        Map<OWLNamedIndividual, String> dataPropertyValues2 = getOwlNamedIndividualSetMap(o2, p2);


        for (Map.Entry<OWLNamedIndividual, String> a : dataPropertyValues1.entrySet()) {

            String substring1 = a.getValue();


            for (Map.Entry<OWLNamedIndividual, String> b : dataPropertyValues2.entrySet()) {

                String substring2 = b.getValue();
                if (calculateDistance(substring2,substring1)>0.7) {
                    i.getAndIncrement();
                    //add the sameAs assertion

                    manager.addAxiom(o1, factory.getOWLSameIndividualAxiom(a.getKey(), b.getKey()));
                    manager.addAxiom(o2, factory.getOWLSameIndividualAxiom(a.getKey(), b.getKey()));

                    // Retrieve the value of the rdfs:label property for the specified instance
                    OWLDataFactory factory = manager.getOWLDataFactory();
                    OWLAnnotationProperty labelProperty = factory.getRDFSLabel();


                    OWLAnnotation labelAnnotation = o1.getAnnotationAssertionAxioms(a.getKey().getIRI())
                            .stream()
                            .findFirst()
                            .map(OWLAnnotationAssertionAxiom::getAnnotation)
                            .orElse(null);

                    OWLAnnotationAssertionAxiom axiom2=factory.getOWLAnnotationAssertionAxiom(factory.getRDFSLabel(), b.getKey().getIRI(), factory.getOWLLiteral(b.getValue()));

                    manager.addAxiom(o2,axiom2);




                    if (labelAnnotation != null) {
                        OWLLiteral labelValue = (OWLLiteral) labelAnnotation.getValue();
                        String label = labelValue.getLiteral();


                        OWLAnnotationAssertionAxiom axiom1=factory.getOWLAnnotationAssertionAxiom(factory.getRDFSLabel(), b.getKey().getIRI(), factory.getOWLLiteral(label));
                        manager.addAxiom(o2,axiom1);

                    }
                    OWLAnnotation labelAnnotation2 = o2.getAnnotationAssertionAxioms(b.getKey().getIRI())
                            .stream()
                            .findFirst()
                            .map(OWLAnnotationAssertionAxiom::getAnnotation)
                            .orElse(null);

                    if (labelAnnotation2 != null) {
                        OWLLiteral labelValue = (OWLLiteral) labelAnnotation2.getValue();
                        String label = labelValue.getLiteral();

                    }

                }
            }

                    // Add the annotation assertion to the ontology

                   // caller(a.getKey(), o1, b.getKey(), o2);
                }
        System.out.println("We have added "+i+" sameAs in case 3.");
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

    private static Map<OWLNamedIndividual, String> getOwlNamedIndividualSetMap(OWLOntology o1, OWLDataPropertyExpression p1) {
        return o1.getIndividualsInSignature()
                .stream()
                .map(a -> Map.entry(a, EntitySearcher.getDataPropertyValues(a, p1, o1).collect(Collectors.toSet())))
                .filter(a -> a.getValue().size() > 0)
                .map(a -> Map.entry(a.getKey(), a.getValue().toString().substring(1, a.getValue().toString().length() - 1)))
                .filter(a -> a.getValue().length() > 0)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void case2(OWLOntology o1, OWLOntology o2, AtomicInteger i, OWLPropertyExpression p1) {
        Map<OWLIndividual, Set<OWLLiteral>> dataPropertyValues = new HashMap<>();
        Map<OWLNamedIndividual, String> annotations = new HashMap<>();

        o1.getIndividualsInSignature()
                .stream()
                .filter(a -> EntitySearcher.getDataPropertyValues(a, o1).containsKey(p1))
                .forEach(a -> dataPropertyValues.put(a, new HashSet<>(EntitySearcher.getDataPropertyValues(a, o1).get((OWLDataPropertyExpression) p1))));

        o2.getIndividualsInSignature().stream()
                .filter(b -> EntitySearcher.getAnnotations(b, o2).toList().size() > 0)
                .forEach(b -> annotations.put(b, EntitySearcher.getAnnotations(b, o2).iterator().next().getValue().toString()));


        for (OWLIndividual a : dataPropertyValues.keySet()) {
            Set<OWLLiteral> propertyValues = dataPropertyValues.get(a);
            for (OWLNamedIndividual b : annotations.keySet()) {
                String annotationValue = annotations.get(b);

                String substring = propertyValues.toString().substring(1, propertyValues.toString().length() - 1);

                if (calculateDistance(annotationValue, substring) > 0.7) {
                    i.getAndIncrement();
                    manager.addAxiom(o1, factory.getOWLSameIndividualAxiom(a, b));
                    manager.addAxiom(o2, factory.getOWLSameIndividualAxiom(a, b));
                    // caller(a, o1, b, o2);


                    Stream<OWLAnnotation> literal = EntitySearcher.getAnnotations((OWLEntity) a, o1);
                    String label = literal.toList().get(0).getValue().toString();
                    EntitySearcher.getAnnotations(b, o2);
                    // Create the label annotation
                    OWLAnnotationProperty labelProperty = factory.getRDFSLabel();

                    OWLLiteral labelValue = factory.getOWLLiteral(label);
                    // Create the annotation assertion
                    OWLAnnotationAssertionAxiom annotationAssertion =
                            factory.getOWLAnnotationAssertionAxiom(labelProperty, b.getIRI(), labelValue);

                    // Add the annotation assertion to the ontology
                    //    AddAxiom addAxiom = new AddAxiom(o2, annotationAssertion);
                    manager.addAxiom(o2, annotationAssertion);
                }

            }
        }

System.out.println("We have added "+i+" sameAs in case 2");

    }

    private void case1(OWLOntology o1, OWLOntology o2, AtomicInteger i) throws OWLOntologyStorageException {
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

                i.getAndIncrement();
                manager.addAxiom(o1, factory.getOWLSameIndividualAxiom(a, b));
                manager.addAxiom(o2, factory.getOWLSameIndividualAxiom(a, b));

                //  OWLAnnotationProperty labelProperty = factory.getRDFSLabel();
                // Set<OWLAnnotation> annotation=o1.getAnnotations();


                OWLLiteral literal = (OWLLiteral) EntitySearcher.getAnnotations(a, o1);
                String label = literal.getLiteral();
                EntitySearcher.getAnnotations(b, o2);
                // Create the label annotation
                OWLAnnotationProperty labelProperty = factory.getRDFSLabel();

                OWLLiteral labelValue = factory.getOWLLiteral(label);
                // Create the annotation assertion
                OWLAnnotationAssertionAxiom annotationAssertion =
                        factory.getOWLAnnotationAssertionAxiom(labelProperty, b.getIRI(), labelValue);

                // Add the annotation assertion to the ontology
                manager.addAxiom(o2, annotationAssertion);
            }
            System.out.println("We have added "+i+" sameAs in case 1");
            //caller(a, o1, b, o2);
        }
    }

    public static void caller(OWLIndividual a, OWLOntology o1, OWLIndividual b, OWLOntology o2) {
        addClassAss(b, o2, a, o1);
        addClassAss(a, o1, b, o2);
        addRoleAss(o1, o2, a, b, EntitySearcher.getDataPropertyValues(a, o1).keySet());
        addRoleAss(o2, o1, b, a, EntitySearcher.getDataPropertyValues(b, o2).keySet());
    }

    public void setPairsOfConcepts(Pair<OWLClassExpression, OWLClassExpression> pairsOfConcepts) {
        this.pairsOfConcepts = pairsOfConcepts;
    }

    public void setPropertySetIn(Set<Pair<OWLPropertyExpression, OWLPropertyExpression>> propertySetIn) {
        this.propertySetIn = propertySetIn;
    }

    public void setPropertySetEq(Set<Pair<OWLPropertyExpression, OWLPropertyExpression>> propertySetEq) {
        this.propertySetEq = propertySetEq;
    }

    public Set<Pair<OWLPropertyExpression, OWLPropertyExpression>> getPropertySetIn() {
        return propertySetIn;
    }

    public Set<Pair<OWLPropertyExpression, OWLPropertyExpression>> getPropertySetEq() {
        return propertySetEq;
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