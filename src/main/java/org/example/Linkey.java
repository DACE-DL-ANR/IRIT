package org.example;
import com.google.common.base.Optional;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.util.Set;

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
    private Set<PropertyPair> PropertySet ;

    public Linkey(ConceptPair pairOfConcepts, Set<PropertyPair> propertySet) {
    }


    public void setPairsOfConcepts(ConceptPair pairsOfConcepts) {
        PairsOfConcepts = pairsOfConcepts;
    }

    public void setPropertySet(Set<PropertyPair> propertySet) {
        PropertySet = propertySet;
    }

    public ConceptPair getPairsOfConcepts() {
        return PairsOfConcepts;
    }

    public Set<PropertyPair> getPropertySet() {
        return PropertySet;
    }




    void saturateLinkey(OWLOntology o1, OWLOntology o2){
        // add equalities owl:sameAs
        IRI ontologyIRI = o1.getOntologyID().getOntologyIRI();
        for(OWLIndividual a:o1.getIndividualsInSignature()){
            for(OWLIndividual b:o2.getIndividualsInSignature()){
                if(a.getTypes(o1).contains(this.getPairsOfConcepts().getFirstConcept())){
                    if(b.getTypes(o2).contains(this.getPairsOfConcepts().getSecondConcept())) {
                        for(PropertyPair p:this.getPropertySet()) {

                          if(a.getDataPropertyValues(o1).containsKey((OWLDataPropertyExpression) p.getFirstProperty())&&b.getDataPropertyValues(o2).containsKey((OWLDataPropertyExpression) p.getSecondProperty())) {
                              System.out.println("**********************");
                              System.out.println(p.getFirstProperty());
                              System.out.println(p.getSecondProperty());
                              System.out.println(a.getDataPropertyValues(o1).get((OWLDataPropertyExpression) p.getFirstProperty()).toString());
                              System.out.println(b.getDataPropertyValues(o2).get((OWLDataPropertyExpression) p.getSecondProperty()).toString());
                              System.out.println("**********************");
                              if (a.getDataPropertyValues(o1).get((OWLDataPropertyExpression) p.getFirstProperty()).toString().contains(b.getDataPropertyValues(o2).get((OWLDataPropertyExpression) p.getSecondProperty()).toString())) {

                                  //    .containsKey(p.getFirstProperty())
//&&b.getDataPropertyValues(o2).containsKey(p.getSecondProperty())
                                  System.out.println("The entities share values");
                                  //  if ((a.getObjectPropertiesInSignature().contains(p.getFirstProperty()) && b.getObjectPropertiesInSignature().contains(p.getSecondProperty()))||(a.getDataPropertiesInSignature().contains(p.getFirstProperty()) && b.getDataPropertiesInSignature().contains(p.getSecondProperty()))){
                                  // they satisfy the link key condition
                                  // so first we add the new sameAs property
                                  OWLObjectPropertyExpression sameAs = factory.getOWLObjectProperty(IRI.create(ontologyIRI + "owl:sameAs"));
                                  OWLObjectPropertyAssertionAxiom sameAsAss = factory.getOWLObjectPropertyAssertionAxiom(sameAs, a, b);
                                  manager.addAxiom(o1, sameAsAss);
                                  manager.addAxiom(o2, sameAsAss);

                                  // add new class expressions
                                  for (OWLClassExpression c : b.getTypes(o2)) {
                                      OWLClassAssertionAxiom assertion = factory.getOWLClassAssertionAxiom(c, a);
                                      manager.addAxiom(o1, assertion);
                                  }

                                  for (OWLClassExpression c : a.getTypes(o1)) {
                                      OWLClassAssertionAxiom assertion = factory.getOWLClassAssertionAxiom(c, b);
                                      manager.addAxiom(o2, assertion);
                                  }

                                  // add new property expressions
                                  for (OWLDataPropertyExpression pe : a.getDataPropertiesInSignature()) {
                                      Set<OWLDatatype> objs = pe.getDatatypesInSignature();
                                      for (OWLDatatype obj : objs) {
                                          OWLDataPropertyAssertionAxiom assertion = factory.getOWLDataPropertyAssertionAxiom(pe, b, (OWLLiteral) obj);
                                          manager.addAxiom(o2, assertion);
                                      }

                                  }

                                  for (OWLObjectPropertyExpression pe : a.getObjectPropertiesInSignature()) {
                                      Set<OWLNamedIndividual> objs = pe.getIndividualsInSignature();
                                      for (OWLNamedIndividual obj : objs) {
                                          OWLObjectPropertyAssertionAxiom assertion = factory.getOWLObjectPropertyAssertionAxiom(pe, b, obj);
                                          manager.addAxiom(o2, assertion);
                                      }

                                  }

                                  for (OWLDataPropertyExpression pe : b.getDataPropertiesInSignature()) {
                                      Set<OWLDatatype> objs = pe.getDatatypesInSignature();
                                      for (OWLDatatype obj : objs) {
                                          OWLDataPropertyAssertionAxiom assertion = factory.getOWLDataPropertyAssertionAxiom(pe, a, (OWLLiteral) obj);
                                          manager.addAxiom(o1, assertion);
                                      }

                                  }

                                  for (OWLObjectPropertyExpression pe : b.getObjectPropertiesInSignature()) {
                                      Set<OWLNamedIndividual> objs = pe.getIndividualsInSignature();
                                      for (OWLNamedIndividual obj : objs) {
                                          OWLObjectPropertyAssertionAxiom assertion = factory.getOWLObjectPropertyAssertionAxiom(pe, b, obj);
                                          manager.addAxiom(o1, assertion);
                                      }
                                  }
                              }
                          }
                                    // add transitivity
                                    //OWLSameIndividualAxiom ax;
                                    // if owl:sameAs
                                }

                            }

                        }
                    }
                }

    }
}