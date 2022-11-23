package org.example;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLOntology;

public class Correspondance {
    OWLClassExpression c1;
    OWLClassExpression c2;

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


    void saturateCorrespondance(OWLOntology o, OWLClassExpression c1, OWLClassExpression c2){
        for(OWLIndividual i: o.getIndividualsInSignature()){
            if(i.getTypes(o).contains(c1))
                i.getTypes(o).add((OWLClass) c2);
        }
    }
}
