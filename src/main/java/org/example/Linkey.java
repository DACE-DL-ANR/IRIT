package org.example;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Linkey {

    private ConceptPair PairsOfConcepts;
    private Double valueOfConf;

    public Double getValueOfConf() {
        return valueOfConf;
    }

    public void setValueOfConf(Double valueOfConf) {
        this.valueOfConf = valueOfConf;
    }

    private static OWLDataFactory factory = new OWLDataFactoryImpl();


    private static OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private Set<PropertyPair> PropertySetIn ;
    private Set<PropertyPair> PropertySetEq ;
    public Linkey() {
    }

    public Linkey(ConceptPair pairOfConcepts, Set<PropertyPair> propertySet) {
    }


    public void setPairsOfConcepts(ConceptPair pairsOfConcepts) {
        PairsOfConcepts = pairsOfConcepts;
    }

    public void setPropertySetIn(Set<PropertyPair> propertySetIn) {
        PropertySetIn = propertySetIn;
    }

    public void setPropertySetEq(Set<PropertyPair> propertySetEq) {
        PropertySetEq = propertySetEq;
    }

    public Set<PropertyPair> getPropertySetIn() {
        return PropertySetIn;
    }

    public Set<PropertyPair> getPropertySetEq() {
        return PropertySetEq;
    }

    public ConceptPair getPairsOfConcepts() {
        return PairsOfConcepts;
    }

    public void addRoleAss(OWLOntology oa, OWLOntology ob, OWLIndividual a, OWLIndividual b, Set<OWLDataPropertyExpression> sprp) {
        // a becomes equal to b, so I need to add the axioms of b to the ontology of a
        // addRoleAss(o2, a, b, sprp1);
        for (OWLPropertyExpression pe : sprp) {

            if (pe.isDataPropertyExpression()) {
                for (OWLLiteral obj : a.getDataPropertyValues((OWLDataPropertyExpression) pe, oa)) {
                    OWLDataPropertyAssertionAxiom assertion = factory.getOWLDataPropertyAssertionAxiom((OWLDataPropertyExpression) pe, b, obj);
                    manager.addAxiom(ob, assertion);
                }
            }
            else {
                for (OWLIndividual obj : a.getObjectPropertyValues((OWLObjectPropertyExpression) pe, oa)) {
                    OWLObjectPropertyAssertionAxiom assertion = factory.getOWLObjectPropertyAssertionAxiom((OWLObjectPropertyExpression) pe, b, obj);
                    manager.addAxiom(ob, assertion);
                }
            }
        }
    }
    public void addClassAss(OWLIndividual a, OWLOntology oa, OWLIndividual b, OWLOntology ob ) {

        for (OWLClassExpression c : a.getTypes(oa)) {
            OWLClassAssertionAxiom assertion = factory.getOWLClassAssertionAxiom(c, b);
            manager.addAxiom(ob, assertion);
        }
    }


    void saturateLinkey(OWLOntology o1, OWLOntology o2, Linkey lk) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException {
        File f_o1_temp = new File("test", "source_temp.ttl");
        File f_o2_temp = new File("test", "target_temp.ttl");
        int i=0;
        f_o1_temp.createNewFile();
        f_o2_temp.createNewFile();

        IRI owlIRI = IRI.create("http://www.w3.org/2002/07/owl#");
        boolean sat1 = false;

        Set<OWLPropertyExpression> slkp1In = new HashSet<>();
        Set<OWLPropertyExpression> slkp2In = new HashSet<>();
        for (PropertyPair p : lk.getPropertySetIn()) {
            slkp1In.add(p.getFirstProperty());
            slkp2In.add(p.getSecondProperty());
        }
        Set<OWLPropertyExpression> slkp1Eq = new HashSet<>();
        Set<OWLPropertyExpression> slkp2Eq = new HashSet<>();
        for (PropertyPair p : lk.getPropertySetEq()) {
            slkp1Eq.add(p.getFirstProperty());
            slkp2Eq.add(p.getSecondProperty());
        }
        System.out.println("The size of the properties of the first side of lk: "+slkp1Eq.size());
        System.out.println("The size of the properties of the second side of lk: "+slkp2Eq.size());
        ParseEdoal pr = new ParseEdoal();
        // add equalities owl:sameAs
        //a -> a.getTypes(o1).contains(lk.getPairsOfConcepts().getFirstConcept()) && b -> b.getTypes(o2).contains(lk.getPairsOfConcepts().getSecondConcept()) &&

       Set<OWLIndividual> A = o1.getIndividualsInSignature().stream().filter( a -> a.getDataPropertyValues(o1).keySet().containsAll(slkp1Eq)||a.getObjectPropertyValues(o1).keySet().containsAll(slkp1Eq)).collect(Collectors.toSet());
       Set<OWLIndividual> B = o2.getIndividualsInSignature().stream().filter( b->b.getDataPropertyValues(o1).keySet().containsAll(slkp2Eq)||b.getObjectPropertyValues(o1).keySet().containsAll(slkp2Eq)).collect(Collectors.toSet());
        for (OWLIndividual a : A) {
            for (OWLIndividual b : B) {
                for (OWLPropertyExpression p1 : slkp1Eq) {
                    System.out.println("Here1");
                    for (OWLPropertyExpression p2 : slkp2Eq) {
                        System.out.println("Here2");
                        if(a.getDataPropertyValues(o1).get(p1)!=null &&b.getDataPropertyValues(o2).get(p2)!=null) {
                            System.out.println("Here");
                            if (a.getDataPropertyValues(o1).get(p1).contains(b.getDataPropertyValues(o2).get(p2)) || (a.getObjectPropertyValues(o1).get(p1).contains(b.getObjectPropertyValues(o2).get(p2)))) {
                                sat1 = true;
                            }
                        }
                    }
                }

                if (sat1) {
                    i++;
                    System.out.println("Adding assertions");
                    OWLObjectPropertyExpression sameAs = factory.getOWLObjectProperty(IRI.create(owlIRI + "owl:sameAs"));
                    OWLObjectPropertyAssertionAxiom sameAsAss = factory.getOWLObjectPropertyAssertionAxiom(sameAs, a, b);
                    manager.addAxiom(o1, sameAsAss);
                    manager.addAxiom(o2, sameAsAss);
                    addClassAss(b, o2, a, o1);
                    addClassAss(a, o1, b, o2);
                    addRoleAss(o1, o2, a, b, a.getDataPropertyValues(o1).keySet());
                    addRoleAss(o2, o1, b, a, b.getDataPropertyValues(o2).keySet());

                }
            }
        }
            System.out.println("The files have been saturated "+i+" by assertions coming from the saturation with link keys");
            pr.saveOntologies(o1, f_o1_temp);
            pr.saveOntologies(o2, f_o2_temp);
        }

}