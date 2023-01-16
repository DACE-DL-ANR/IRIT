package org.example;
import org.semanticweb.owlapi.model.*;
import org.xml.sax.SAXException;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.HashSet;
import java.util.Set;

public class Correspondance {
    OWLClassExpression c1;
    OWLClassExpression c2;
    private final OWLDataFactory factory = new OWLDataFactoryImpl();

    Double valueOfConf;

    public Double getValueOfCon() {
        return valueOfCon;
    }

    public void setValueOfCon(Double valueOfCon) {
        this.valueOfCon = valueOfCon;
    }

    Double valueOfCon;

    public OWLClassExpression getC1() {
        return c1;
    }

    public OWLClassExpression getC2() {
        return c2;
    }

    public void setC1(OWLClassExpression c1) {
        this.c1 = c1;
    }

    public void setC2(OWLClassExpression c2) {
        this.c2 = c2;
    }


    void saturateCorrespondance(OWLOntology o1, OWLOntology o2, String f) throws IOException, ParserConfigurationException, SAXException {
        //these two files should be overwritten each time
        //actually there is no need to keep them, I just I keep the final version
        OWLOntologyManager manager1 = o1.getOWLOntologyManager();
        OWLOntologyManager manager2 = o2.getOWLOntologyManager();
        File f_o1_temp = new File("test/source_temp.ttl");
        f_o1_temp.createNewFile();


        File f_o2_temp = new File("test/target_temp.ttl");
        f_o2_temp.createNewFile();
        int j = 0;
        //these files have to be created if they don't exist then they have to be passed to the new calling systems
        ParseEdoal parseEdoal = new ParseEdoal();
        Set<Alignment> alignments = Alignment.readAlignments(f);
        for (OWLIndividual i : o1.getIndividualsInSignature()) {

            for (Alignment alignment : alignments) {

               Set<OWLClassExpression> align_classes1 = alignToComplexAss(alignment.getElement1().toMergedForm());
                boolean enter = false;
               if(alignment.getElement1().toMergedForm().substring(alignment.getElement1().toMergedForm().indexOf("Class")+6).startsWith("and")){

                    for(OWLClassExpression align_class : align_classes1){
                        satisfy(o1,i,align_class );
                    }
                }
                else if(alignment.getElement1().toMergedForm().substring(alignment.getElement1().toMergedForm().indexOf("Class")+6).startsWith("or")) {
                   for (OWLClassExpression align_class : align_classes1) {
                    if(satisfy(o1,i,align_class)) {

                        //  i.getTypes(o1).add();
                        OWLClassAssertionAxiom assertion1 = factory.getOWLClassAssertionAxiom(factory.getOWLClass(IRI.create(alignment.getElement1().toMergedForm().replace("#","@"))), i);
                        manager1.addAxiom(o1, assertion1);

                        OWLClassAssertionAxiom assertion2 = factory.getOWLClassAssertionAxiom(factory.getOWLClass(IRI.create(alignment.getElement2().toMergedForm().replace("#","@"))), i);
                        manager1.addAxiom(o1, assertion2);


                    }
                   }
               }
                else{

                       for(OWLClassExpression align_class : align_classes1){
                           System.out.println( "The type of the expression is: "+align_class.getClassExpressionType());
                          if( satisfy(o1,i,align_class )) {
                              System.out.println("The number of types of i before is: " + i.getTypes(o1).size());
                              i.getTypes(o1).add(factory.getOWLClass(IRI.create(alignment.getElement1().toMergedForm())));
                              System.out.println("The number of types of i after is: " + i.getTypes(o1).size());
                          }
                       }
                   }


                if (enter) {
                    j++;
                   Set<OWLClassExpression> align_classes2 = alignToComplexAss(alignment.getElement2().toMergedForm());
                    OWLClassAssertionAxiom assertion2 = factory.getOWLClassAssertionAxiom(factory.getOWLClass(IRI.create(alignment.getElement2().toMergedForm())), i);
                    manager1.addAxiom(o1, assertion2);
                    // System.out.println("The merged form of the second side of the alignment: " + alignment.getElement2().toMergedForm());
                }
            }
        }
        for (OWLIndividual i : o2.getIndividualsInSignature()) {

            for (Alignment alignment : alignments) {
                Set<OWLClassExpression> align_classes2 = alignToComplexAss(alignment.getElement2().toMergedForm());
                boolean enter = false;
                if (alignment.getElement2().toMergedForm().startsWith("simple")) {
                    System.out.println("Simple");
                    for (OWLClassExpression align_class : align_classes2) {
                        System.out.println("under simple");
                    }
                }
                else if(alignment.getElement2().toMergedForm().startsWith("and")){
                    System.out.println("AND");
                    for (OWLClassExpression align_class : align_classes2) {
                        System.out.println("under and");
                    }

                }
                else if(alignment.getElement2().toMergedForm().startsWith("or")){
                    System.out.println("OR");
                    for (OWLClassExpression align_class : align_classes2) {
                        System.out.println("under or");
                    }
                }
                if (enter) {
                       OWLClassAssertionAxiom assertion1 = factory.getOWLClassAssertionAxiom(factory.getOWLClass(IRI.create(alignment.getElement1().toMergedForm().replace("#","@"))), i);
                       manager2.addAxiom(o2, assertion1);
                       j++;
                }
            }
        }
        try {
                System.out.println("The files have been enriched "+j+" times with assertions coming from correspondances");
                parseEdoal.saveOntologies(o1, f_o1_temp);
                parseEdoal.saveOntologies(o2, f_o2_temp);
            } catch (OWLOntologyCreationException | OWLOntologyStorageException e) {
                throw new RuntimeException(e);
            }

    }

    public boolean satisfy(OWLOntology o, OWLIndividual i, OWLClassExpression exp){

        //  System.out.println();
      //  if(exp.getClassExpressionType().equals("ObjectAllValuesFrom"))
      //  {
            for(OWLObjectProperty p:exp.getObjectPropertiesInSignature()){

                for(OWLIndividual j:i.getObjectPropertyValues(p,o)) {
                    if(j.getTypes(o).containsAll(exp.getClassesInSignature())) {
                      //  System.out.println("Satisfy");
                        return true;
                    }
                }
            }
       // }

        return false;
    }

    public Set<OWLClassExpression> alignToComplexAss(String s){
        Set<OWLClassExpression> compSet = new HashSet<>();
        String cls="";
        while (s.length()>0&&s.contains("+")) {
                String oper = s.substring(0, s.indexOf("+"));
                 if (oper.equals("Class")) {
                s = s.substring(s.indexOf("Class+") + 6);
                s=s.substring(s.indexOf("+")+1);
                if(s.startsWith("or")||s.startsWith("and")) {
                    cls = s;
                }
                else{
                    cls = s;
                  compSet=  alignToComplexAss(s);
                }
                 }
                if (oper.equals("RelationDomainRestriction")) {
                    String occRel=s.substring(s.indexOf("Relation_")+9, s.indexOf("+"));
                    String occCls=s.substring(s.indexOf(occRel)+occRel.length(), s.indexOf("+"));
                    cls=occCls;
                    compSet.add(factory.getOWLObjectSomeValuesFrom(factory.getOWLObjectProperty(IRI.create(occRel)),factory.getOWLClass(IRI.create(occCls))));

                }
                else if(oper.equals("AttributeOccurenceRestriction")){
                    s=s.substring(s.indexOf("Relation_")+9);
                    String occRel=s.substring(0, s.indexOf("+"));
                    String occCls=s.substring(s.indexOf(occRel)+occRel.length(),  s.indexOf("+"));
                    cls=occCls;
                    compSet.add(factory.getOWLDataSomeValuesFrom(factory.getOWLDataProperty(IRI.create(occRel)),factory.getOWLDatatype(IRI.create(occCls))));

                }
                else if(oper.equals("AttributeDomainRestriction")){
                    System.out.println(s);
                    String domRel=s.substring(s.indexOf("Relation_")+9,s.indexOf(s.substring(s.indexOf("+Class")-7)) );
                    s=s.substring(s.indexOf("Class_"));
                    String domCls ;
                    if(s.contains("+")) {
                        domCls=s.substring(s.indexOf("Class_") + 6, s.indexOf("+"));
                    }
                    else{
                       domCls=s.substring(s.indexOf("Class_") + 6);
                    }
                    compSet.add(factory.getOWLObjectAllValuesFrom(factory.getOWLObjectProperty(IRI.create(domRel)),factory.getOWLClass(IRI.create(domCls))));
                   cls=domCls;
                }
                if(s.contains(cls)&&s.length()>0) {
                    s = s.substring(s.indexOf(cls) + cls.length() );
                }
                else{
                    s="";
                }
            }

        return compSet;
    }

}
