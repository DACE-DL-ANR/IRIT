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

public class Linkey {

    private Pair<OWLClassExpression, OWLClassExpression> pairsOfConcepts;

    private static final OWLDataFactory factory = new OWLDataFactoryImpl();


    private static final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private Set<Pair<OWLPropertyExpression, OWLPropertyExpression>> propertySetIn;
    private Set<Pair<OWLPropertyExpression, OWLPropertyExpression>> propertySetEq;

    public Linkey() {
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

                } else {
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

                if (substring2.contains(substring1) || substring1.contains(substring2)) {
                    i.getAndIncrement();
                    //add the sameAs assertion
                    manager.addAxiom(o1, factory.getOWLSameIndividualAxiom(a.getKey(), b.getKey()));
                    manager.addAxiom(o2, factory.getOWLSameIndividualAxiom(a.getKey(), b.getKey()));
                    caller(a.getKey(), o1, b.getKey(), o2);
                }
            }
        }
    }

    // saturateSameAs, saturateCorrespandences.
    private static void saturateSameAs(OWLOntology o1, OWLOntology o2){
   //Alignment.readAlignmentsTxt("");
        Set<Alignment> alignments = Alignment.readAlignmentsTxt(Paths.get(""));
        Set<Alignment> instAl = alignments.stream().filter(alignment -> alignment.getElement1().getTag().equals("INST")).collect(Collectors.toSet());
        for(Alignment al:instAl) {
           // o1.getIndividualsInSignature().stream().filter(ind->ind.equals(al.getElement1().getName())).collect(Collectors.toSet());
            caller((OWLIndividual) al.getElement1(), o1, (OWLIndividual) al.getElement2(), o2);
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
                if (annotationValue.contains(substring) || substring.contains(annotationValue)) {
                    i.getAndIncrement();
                    manager.addAxiom(o1, factory.getOWLSameIndividualAxiom(a, b));
                    manager.addAxiom(o2, factory.getOWLSameIndividualAxiom(a, b));
                    caller(a, o1, b, o2);
                }
            }
        }



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

            if (!s1.contains(s2)) {
                continue;
            }
            i.getAndIncrement();
            manager.addAxiom(o1, factory.getOWLSameIndividualAxiom(a, b));
            manager.addAxiom(o2, factory.getOWLSameIndividualAxiom(a, b));
            caller(a, o1, b, o2);
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