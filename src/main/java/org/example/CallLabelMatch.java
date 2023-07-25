package org.example;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.util.iterator.ExtendedIterator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CallLabelMatch {
    Set<Alignment> alignment;
    OntModel ontology1;
    OntModel ontology2;


    public Set<Alignment> match(OntModel sourceOntology, OntModel targetOntology, File output) throws Exception {
        this.alignment = new HashSet<>();
        ontology1 = sourceOntology;
        ontology2 = targetOntology;
       // ExtendedIterator<OntClass> r = ontology1.listClasses();

        match(ontology1.listClasses(), ontology2.listClasses(), new File(output.getPath()+"cls"));
        match(ontology1.listDatatypeProperties(), ontology2.listDatatypeProperties(), output);
        match(ontology1.listObjectProperties(), ontology2.listObjectProperties(), output);
        match(ontology1.listDatatypeProperties(), ontology2.listDatatypeProperties(), output);
        match(ontology1.listIndividuals(), ontology2.listIndividuals(), output);
        return this.alignment;
    }
    Set<Alignment> match(ExtendedIterator<? extends OntResource> resourceIterator1,
                         ExtendedIterator<? extends OntResource> resourceIterator2, File Output) throws IOException {

        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(Output));
        HashMap<BagOfWords, String> labelToURI_1 = new HashMap<>();
    
        
        while (resourceIterator1.hasNext()) {
            OntResource r1 = resourceIterator1.next();

            // String processing
            BagOfWords label = normalize(getLabelOrFragment(r1));
            //   System.out.println("label1 "+label);
            if (label != null) {
                labelToURI_1.put(label, r1.getURI());
            }
        }

        while (resourceIterator2.hasNext()) {
            OntResource r2 = resourceIterator2.next();
            BagOfWords label2 = normalize(getLabelOrFragment(r2));
            if (label2 != null && labelToURI_1.containsKey(label2)) {
                if (labelToURI_1.get(label2) != null) {


                    Alignment al = new Alignment();

                    if (r2.getURI().contains("resource")) {
                        al.element1 = Alignment.OntologyNode.fromTxt(labelToURI_1.get(label2), "INST");
                        al.element2 = Alignment.OntologyNode.fromTxt(r2.getURI(), "INST");

                    } else {

                        al.element1 = Alignment.OntologyNode.fromTxt(labelToURI_1.get(label2), "CLS");

                        al.element2 = Alignment.OntologyNode.fromTxt(r2.getURI(), "CLS");

                    }

                    // al.relation = split[2];
                    // al.measure = Float.parseFloat(split[3]);

                    //  al.getElement1()= r1.getURI();
                    //   al.getElement1().attributes=labelToURI_1.get(labelToURI_1);
                    // alignment.add(labelToURI_1.get(label2), r2.getURI());
                    // System.out.println("elem. 1: "+al.getElement1());
                    // System.out.println("elem. 2: "+al.getElement2());
                    alignment.add(al);
                    bufferedWriter.write(al.element1.getName());
                    bufferedWriter.write("|");
                    bufferedWriter.write(al.element2.getName());
                    bufferedWriter.write("|");
                    bufferedWriter.write("=");
                    bufferedWriter.write("|");
                    bufferedWriter.write(al.element1.getTag());
                    bufferedWriter.newLine();
                }

            }
        }
        return alignment;
    }
    /**
     * Normalizes a string and returns a bag of words.
     * @param stringToBeNormalized The string that shall be normalized.
     * @return bag of words.
     */
    public static BagOfWords normalize(String stringToBeNormalized) {
        if (stringToBeNormalized == null) return null;
        stringToBeNormalized = stringToBeNormalized.replaceAll("(?<!^)(?<!\\s)(?=[A-Z][a-z])", "_"); // convert camelCase to under_score_case
        stringToBeNormalized = stringToBeNormalized.replace(" ", "_");
        stringToBeNormalized = stringToBeNormalized.toLowerCase();

        // delete non alpha-numeric characters:
        stringToBeNormalized = stringToBeNormalized.replaceAll("[^a-zA-Z\\d\\s:_]", ""); // regex: [^a-zA-Z\d\s:]

        return new BagOfWords(stringToBeNormalized.split("_"));
    }
    public static String getLabelOrFragment(OntResource resource) {
        if (resource == null){
            return null;
        }
        ExtendedIterator<RDFNode> iterator = resource.listLabels(null);
        while (iterator.hasNext()) {
            RDFNode node = iterator.next();
            return node.asLiteral().getLexicalForm();
        }
        // no label found: return local name
        if(resource.isURIResource()) {
            return resource.getURI();
        }
        return null;
    }

}
