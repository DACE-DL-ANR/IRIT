package org.example;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.xml.sax.SAXException;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Correspondence {
    OWLClassExpression c1;
    OWLClassExpression c2;
    private static final OWLDataFactory factory = new OWLDataFactoryImpl();

    public Correspondence() {
    }

    public Correspondence(OWLClassExpression cls1, OWLClassExpression cls2) {
        this.c1 = cls1;
        this.c2 = cls2;
    }

    public OWLClassExpression getC1() {
        return c1;
    }

    public OWLClassExpression getC2() {
        return c2;
    }


    public void saturateCorrespondence(OWLOntology o1, OWLOntology o2, String f) throws IOException, ParserConfigurationException, SAXException {
        Set<Alignment> alignments = Alignment.readAlignments(f)
                .stream()
                .filter(alignment -> !alignment.getElement1().toMergedForm().startsWith("Relation"))
                .collect(Collectors.toSet());

        Map<String, OWLClass> classMap = getStringOWLClassMap(alignments);

        int j = getJ(o1, o2, alignments, classMap);

    }

    private int getJ(OWLOntology o1, OWLOntology o2, Set<Alignment> alignments, Map<String, OWLClass> classMap) {
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


    public boolean satisfy(OWLNamedIndividual a, OWLOntology o1, OWLClass cls1) {

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
