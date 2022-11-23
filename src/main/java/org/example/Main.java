package org.example;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Set;

public class Main {
    static boolean enter = true;
    static CallCanard canard;
    static CallLinkex linkex;
    static QueryGenerator qGen;

    static ParseEdoal parser;
    static OWLOntology loadOntology(File fileSource) throws OWLOntologyCreationException {

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = null;
        try {
            ontology = manager.loadOntologyFromOntologyDocument(fileSource);

        } catch (OWLOntologyCreationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ontology;
    }

    static ArrayList<OWLOntology> pipe(File fileSource, File fileTarget, Double valueOfConf) {


        OWLOntology source = null;
        OWLOntology target = null;
        try {
            source = loadOntology(fileSource);
            target = loadOntology(fileTarget);
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException("Can't load the ontologies");
        }



        ArrayList<OWLOntology> pairsFinal = new ArrayList<>();
        while(enter)

        {
            canard = new CallCanard();
            System.out.println("The correspondances obtained before launching Linkex");
            ArrayList<Correspondance> cs = canard.execute(fileSource, fileTarget);
            System.out.println("The link keys obtained without correspondances");
            linkex = new CallLinkex();
            Set<Linkey> lks= linkex.execute(fileSource,fileTarget);
            System.out.println("We have obtained "+lks.size()+" link keys");
            System.out.println("Saturating the graph with the link keys obtained");
            for(Linkey lk:linkex.execute(fileSource,fileTarget)){
                lk.saturateLinkey(source,target);
            }

            for (Correspondance c : cs) {
                if(c.getValueOfCon()>valueOfConf) {
                    c.saturateCorrespondance(source, c.getC1(), c.getC2());
                    c.saturateCorrespondance(target, c.getC1(), c.getC2());
                    lks = linkex.execute(fileSource, fileTarget, c);
                    for (Linkey lk : lks) {
                        if(lk.getValueOfConf()>valueOfConf) {
                            lk.saturateLinkey(source, target);
                        }
                    }
                }
            }


            if (cs.isEmpty()) {
                enter = false;
            }
        }
        System.out.println("The correspondances obtained after launching Linkex");
        ArrayList<Correspondance> cs = canard.execute(fileSource, fileTarget);
        //Call saturate with correspondances
        pairsFinal.add(source);
        pairsFinal.add(target);
        return pairsFinal;
    }


    public static void main(String[] args) {


        Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter the source ontology");
        String pathSource = scanner.nextLine().trim();

        System.out.println("Please enter the target ontology");
        String pathTarget = scanner.nextLine().trim();

        System.out.println("Enter the value of confidence");
        Double valueOfConf = Double.valueOf(scanner.nextLine().trim());

        File fileSource = new File("test/"+pathSource);
        File fileTarget = new File("test/"+pathTarget);
        pipe(fileSource, fileTarget, valueOfConf);
        ParseEdoal p=new ParseEdoal();
        p.EDOALtoCCs(fileSource);
    }
}
