package org.example;
import org.coode.owlapi.turtle.TurtleOntologyFormat;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
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

    public void addRoleAss(OWLOntology oa, OWLOntology ob, OWLIndividual a, OWLIndividual b, Set<OWLDataPropertyExpression> sprp, File f) throws OWLOntologyStorageException {
        Set<OWLAxiom> axiomsToAdd = new HashSet<>();
        for (OWLPropertyExpression pe : sprp) {
            if (pe.isDataPropertyExpression()) {
                for (OWLLiteral obj : a.getDataPropertyValues((OWLDataPropertyExpression) pe, oa)) {
                    OWLDataPropertyAssertionAxiom assertion = factory.getOWLDataPropertyAssertionAxiom((OWLDataPropertyExpression) pe, b, obj);
                    axiomsToAdd.add(assertion);
                }
            } else {
                for (OWLIndividual obj : a.getObjectPropertyValues((OWLObjectPropertyExpression) pe, oa)) {
                    OWLObjectPropertyAssertionAxiom assertion = factory.getOWLObjectPropertyAssertionAxiom((OWLObjectPropertyExpression) pe, b, obj);
                    axiomsToAdd.add(assertion);
                }
            }
        }

        manager.addAxioms(ob, axiomsToAdd);
        manager.saveOntology(ob, new TurtleOntologyFormat(), IRI.create(f.toURI()));
    }

    public void addClassAss(OWLIndividual a, OWLOntology oa, OWLIndividual b, OWLOntology ob, File f) throws OWLOntologyStorageException {
        Set<OWLAxiom> axiomsToAdd = new HashSet<>();

        for (OWLClassExpression c : a.getTypes(oa)) {
            OWLClassAssertionAxiom assertion = factory.getOWLClassAssertionAxiom(c, b);
            axiomsToAdd.add(assertion);
        }

        manager.addAxioms(ob, axiomsToAdd);
        manager.saveOntology(ob, new TurtleOntologyFormat(), IRI.create(f.toURI()));
    }


    void saturateLinkey(OWLOntology o1, OWLOntology o2, Linkey lk) throws OWLOntologyCreationException, IOException, OWLOntologyStorageException, URISyntaxException {
        File f_o1_temp = new File("test", "source_temp.ttl");
        File f_o2_temp = new File("test", "target_temp.ttl");
        AtomicInteger i= new AtomicInteger();
        f_o1_temp.createNewFile();
        f_o2_temp.createNewFile();

        Set<OWLPropertyExpression> slkp1In = new HashSet<>();
        Set<OWLPropertyExpression> slkp2In = new HashSet<>();
        if(lk.getPropertySetIn()!=null) {
            for (PropertyPair p : lk.getPropertySetIn()) {
                slkp1In.add(p.getFirstProperty());
                slkp2In.add(p.getSecondProperty());
            }
        }
        Set<OWLPropertyExpression> slkp1Eq = new HashSet<>();
        Set<OWLPropertyExpression> slkp2Eq = new HashSet<>();
        if(lk.getPropertySetIn()!=null) {
            for (PropertyPair p : lk.getPropertySetEq()) {
                slkp1Eq.add(p.getFirstProperty());
                slkp2Eq.add(p.getSecondProperty());
            }
        }
        //  System.out.println("The size of the properties of the first side of lk: "+slkp1Eq.size());
        //    System.out.println("The size of the properties of the second side of lk: "+slkp2Eq.size());
        ParseEdoal pr = new ParseEdoal();

        for (OWLPropertyExpression p1 : slkp1Eq) {

            for (OWLPropertyExpression p2 : slkp2Eq) {


                if (p2.toString().equals("rdfs:label") && p1.toString().equals("rdfs:label")) {
                    o1.getIndividualsInSignature().parallelStream().flatMap(a ->
                            o2.getIndividualsInSignature().parallelStream().filter(b ->
                                    b.getAnnotations(o2).size() > 0 && a.getAnnotations(o1).size() > 0
                            ).map(b -> new AbstractMap.SimpleImmutableEntry<>(a, b))
                    ).forEach(pair -> {
                        OWLNamedIndividual a = pair.getKey();
                        OWLNamedIndividual b = pair.getValue();

                        String s1 = a.getAnnotations(o1).toString().substring(a.getAnnotations(o1).toString().indexOf("\"") + 1, a.getAnnotations(o1).toString().lastIndexOf("\""));
                        String s2 = b.getAnnotations(o2).toString().substring(b.getAnnotations(o2).toString().indexOf("\"") + 1, b.getAnnotations(o2).toString().lastIndexOf("\""));

                        if (s1.contains(s2)) {

                            i.getAndIncrement();
                          //  System.out.println("Adding assertion 1");
                            manager.addAxiom(o1, factory.getOWLSameIndividualAxiom(a, b));
                            manager.addAxiom(o2, factory.getOWLSameIndividualAxiom(a, b));

                            try {
                                caller(a, o1, b, o2, f_o1_temp, f_o2_temp);
                            } catch (OWLOntologyStorageException e) {
                                throw new RuntimeException(e);
                            }
                        }

                    });

                }
                if (p2.toString().equals("rdfs:label")) {
                    // Cache frequently accessed data
                    Map<OWLIndividual, Set<OWLLiteral>> dataPropertyValues = new HashMap<>();
                    Map<OWLNamedIndividual, String> annotations = new HashMap<>();
                    for (OWLIndividual a : o1.getIndividualsInSignature().stream().filter(a -> a.getDataPropertyValues(o1).keySet().contains(p1)).collect(Collectors.toSet())) {
                        dataPropertyValues.put(a, a.getDataPropertyValues(o1).get(p1));
                    }
                    for (OWLNamedIndividual b : o2.getIndividualsInSignature().stream().filter(b -> b.getAnnotations(o2).size() > 0).collect(Collectors.toSet())) {
                        annotations.put(b, b.getAnnotations(o2).iterator().next().getValue().toString());
                    }

// Iterate over the filtered sets and perform the operation
                    for (OWLIndividual a : dataPropertyValues.keySet()) {
                        Set<OWLLiteral> propertyValues = dataPropertyValues.get(a);
                        for (OWLNamedIndividual b : annotations.keySet()) {
                            String annotationValue = annotations.get(b);

                            if (annotationValue.contains(propertyValues.toString().substring(1, propertyValues.toString().length() - 1)) ||
                                    propertyValues.toString().substring(1, propertyValues.toString().length() - 1).contains(annotationValue)) {
                                i.getAndIncrement();
                         //          System.out.println("Adding assertions 2");
                                manager.addAxiom(o1, factory.getOWLSameIndividualAxiom(a, b));
                                manager.addAxiom(o2, factory.getOWLSameIndividualAxiom(a, b));
                                caller( a, o1, b, o2, f_o1_temp, f_o2_temp);
                            }
                        }
                    }

                }

                else {
                    Map<OWLIndividual, Set<OWLLiteral>> dataPropertyValues1 = new HashMap<>();
                    Map<OWLIndividual, Set<OWLLiteral>> dataPropertyValues2 = new HashMap<>();



                            for (OWLIndividual a : o1.getIndividualsInSignature().stream().filter(a -> a.getDataPropertyValues((OWLDataPropertyExpression) p1,o1)!=null).collect(Collectors.toSet())) {
                                dataPropertyValues1.put(a, a.getDataPropertyValues((OWLDataPropertyExpression) p1,o1));
                            }
                    for (OWLIndividual a : o2.getIndividualsInSignature().stream().filter(a -> a.getDataPropertyValues((OWLDataPropertyExpression) p2,o2)!=null).collect(Collectors.toSet())) {
                        dataPropertyValues2.put(a, a.getDataPropertyValues(o2).get(p2));
                    }
                    for (OWLIndividual a : dataPropertyValues1.keySet()) {
                        Set<OWLLiteral> propertyValues1 = dataPropertyValues1.get(a);

                        dataPropertyValues2.keySet().parallelStream().forEach(b -> {
                            Set<OWLLiteral> propertyValues2 = dataPropertyValues1.get(b);
                            if (propertyValues2 != null) {
                              if(propertyValues2.toString().substring(1, propertyValues2.toString().length() - 1).length()>0&&propertyValues1.toString().substring(1, propertyValues1.toString().length() - 1).length()>0) {
                                  if (propertyValues2.toString().substring(1, propertyValues2.toString().length() - 1).contains(propertyValues1.toString().substring(1, propertyValues1.toString().length() - 1)) ||
                                          propertyValues1.toString().substring(1, propertyValues1.toString().length() - 1).contains(propertyValues2.toString().substring(1, propertyValues2.toString().length() - 1))) {
                                      i.getAndIncrement();
                                  //    System.out.println("Adding assertions 3");
                                      manager.addAxiom(o1, factory.getOWLSameIndividualAxiom(a, b));
                                      manager.addAxiom(o2, factory.getOWLSameIndividualAxiom(a, b));
                                      try {
                                          caller(a, o1, b, o2, f_o1_temp, f_o2_temp);
                                      } catch (OWLOntologyStorageException e) {
                                          throw new RuntimeException(e);
                                      }
                                  }
                              }
                            }
                        });
                    }
                }
            }
        }
        System.out.println("The files have been saturated " + i + " by assertions coming from the saturation with link keys");
        pr.saveOntologies(o1, f_o1_temp);
        pr.saveOntologies(o2, f_o2_temp);
    }
    public void caller(OWLIndividual a,OWLOntology o1, OWLIndividual b,OWLOntology o2, File f_o1_temp, File f_o2_temp) throws OWLOntologyStorageException {
        addClassAss(b, o2, a, o1, f_o1_temp);
        addClassAss(a, o1, b, o2, f_o2_temp);
        addRoleAss(o1, o2, a, b, a.getDataPropertyValues(o1).keySet(), f_o2_temp);
        addRoleAss(o2, o1, b, a, b.getDataPropertyValues(o2).keySet(), f_o1_temp);

    }

    public void printLk(Linkey lk){

        if(lk.getPropertySetEq()!=null) {
            //   System.out.println("The properties are: " + lk.getPropertySetEq().size() + " pairs.");

            int i = 0;
            for (PropertyPair pp : lk.getPropertySetEq()) {
                i++;
                System.out.println("The pair number " + i + " : first element: " + pp.getFirstProperty() + " : second element: " + pp.getSecondProperty());
            }
        }
        if(lk.PairsOfConcepts!=null)
            System.out.println("The classes of the link keys are: "+lk.PairsOfConcepts.getFirstConcept()+" and "+lk.PairsOfConcepts.getSecondConcept());
        //  System.out.println("Lk: ({"+lk.getPropertySetEq()+" }, <"+lk.PairsOfConcepts+">)" );
    }

}