package org.example;
import org.apache.commons.lang3.time.StopWatch;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import utils.Pair;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class Main {


    static CallCanard canard;
    static CallLinkex linkex;
    static CallLogMap logMap;
    static CallAMLC amlc;
    private static final OWLDataFactory factory = new OWLDataFactoryImpl();
    private static File fileSource;
    private static File fileTarget;

    public static void main(String[] args) throws Exception {
       /* String pathSource = "edas_100.ttl";

        String pathTarget = "conference_100.ttl";

        Double valueOfConf = 0.7;
        String system="2";*/

       Scanner scanner = new Scanner(System.in);
       System.out.println("Which system you need to integrate? 1:LOGMAP, 2:DICAP");
       String system = scanner.nextLine().trim();
       System.out.println("Please enter the source ontology");
       String pathSource = scanner.nextLine().trim();
       System.out.println("Please enter the target ontology");
       String pathTarget = scanner.nextLine().trim();
       fileSource = new File("test/" + pathSource);
       fileTarget = new File("test/" + pathTarget);


        if (system.equals("1")) {
            pipe(fileSource, fileTarget);
        }
        if (system.equals("2")) {
               System.out.println("Enter the value of confidence");
               Double valueOfConf = Double.valueOf(scanner.nextLine().trim());
               pipe(fileSource, fileTarget, fileSource, fileTarget, valueOfConf);
        }
    }

    private static void pipe(File fileSource, File fileTarget ) {
        StopWatch pipe = new StopWatch();
        pipe.start();
        OWLOntology source = loadOntology(fileSource);
        OWLOntology target = loadOntology(fileTarget);
        logMap = new CallLogMap("../logmap-matcher-master/target/logmap-matcher-4.0.jar");
        pipe.stop();
        runLM(fileSource, fileTarget);
    }

    private static void runLM(File fileSource, File fileTarget) {
        ParseEdoal pr=new ParseEdoal();
        for (int iter = 0; iter < 5; iter++) {
            System.out.println("Iteration number: "+iter++);
            logMap.execute(fileSource, fileTarget);
            fileSource = new File("test/source_temp.ttl");
            fileTarget = new File("test/target_temp.ttl");

            }

            //fs will be the path to the correspondances and instances
            // saturateOntologies(source, target, fs);
            //after we saturate ontologies we save them into files and we call logMap

    }

    static OWLOntology loadOntology(File fileSource) {

        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology = null;
        try {
            ontology = manager.loadOntologyFromOntologyDocument(fileSource);

        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        }
        return ontology;
    }

    static void pipe(File fileSourceI, File fileTargetI, File fileSource, File fileTarget, Double valueOfConf) throws Exception {
        StopWatch pipe = new StopWatch();

        pipe.start();

        OWLOntology source = loadOntology(fileSourceI);
        OWLOntology target = loadOntology(fileTargetI);

        linkex = new CallLinkex("../linkex/LinkkeyDiscovery-1.0-SNAPSHOT-jar-with-dependencies.jar");
        File f_start = new File("output/startlinkeys");
        f_start.createNewFile();
        Set<Linkey> lks = linkex.execute(fileSourceI, fileTargetI, f_start);

        canard = new CallCanard("../canard/CanardE.jar");
        canard.execute(fileSourceI, fileTargetI, fileSource, fileTarget, valueOfConf);

        int t1 = fileSource.getName().lastIndexOf(".");
        int t2 = fileTarget.getName().lastIndexOf(".");

        String fs = "output/" + fileSourceI.getName().substring(0, t1) + "_" + fileTargetI.getName().substring(0, t2) + "/th_" + valueOfConf.toString() + ".edoal";
        ParseEdoal pr = new ParseEdoal();
        Correspondence c = new Correspondence();
        run(fileSourceI, fileTargetI, valueOfConf, source, target, lks, fs, pr);
        pipe.stop();
        System.out.println("Pipe: " + pipe.getTime(TimeUnit.SECONDS));
    }

    private static void run(File fileSourceI, File fileTargetI, Double valueOfConf, OWLOntology source, OWLOntology target, Set<Linkey> lks, String fs, ParseEdoal pr) throws IOException, ParserConfigurationException, SAXException, OWLOntologyStorageException {


        int counter_lks = 0;
        int counter_lkc = 0;

        for (int iter = 0; iter < 2; iter++) {
            System.out.println("Iteration number: "+iter++);
            // int t1 = fileSource.getName().lastIndexOf(".");
            //  int t2 = fileTarget.getName().lastIndexOf(".");
            //  fs = "output/" + fileSource.getName().substring(0, t1) + "_" + fileTarget.getName().substring(0, t2) + "/th_" + valueOfConf + ".edoal";
           //saturate with the starting lks in the first round.

            fileSource = new File("test/source_temp.ttl");
            fileTarget = new File("test/target_temp.ttl");
            pr.saveOntologies(source, fileSource);
            pr.saveOntologies(target, fileTarget);

            canard.execute(fileSourceI, fileTargetI, fileSource, fileTarget, valueOfConf);
            Pair<Set<Correspondence>, Set<Correspondence>> pair = buildCorrespondences(fs);

              for (Correspondence cr : pair.first()) {
                  System.out.println();
            File f = new File("output/linkeys_simple/" + cr.getC1().toString().substring(1,cr.getC1().toString().length()-1).replace("://","_") + "_" + cr.getC2().toString().substring(1,cr.getC2().toString().length()-1).replace("://","_"));

                Set<Linkey> lks_s= linkex.execute(fileSource, fileTarget, f, cr);

               for (Linkey lk : lks_s) {

                   lk.printLk();
                    lk.saturateLinkey(source, target);
                }
        }
       for (Correspondence crc : pair.second())
        {
            Set<Linkey> lks_c= linkex.execute(fileSource, fileTarget, new File("output/linkeys_complex/" + crc.getC1().toString().substring(1,crc.getC1().toString().length()-1).replace("://","_")  + "_" + crc.getC2().toString().substring(1,crc.getC2().toString().length()-1).replace("://","_")), crc);

            for (Linkey lk : lks_c) {

                lk.printLk();
                lk.saturateLinkey(source, target);
            }
        }

        Set<Linkey> lks_w = linkex.execute(fileSource, fileTarget, new File("output/linkeys"));
        /*if (lks.size() == lks_w.size()) {
            break;
        }*/
        //
       // lks = lks_w;
            //
             saturateOntologies(source, target, lks, fs);




    }

}

    private static void saturateOntologies(OWLOntology source, OWLOntology target, Set<Linkey> lks, String fs) throws IOException, ParserConfigurationException, SAXException, OWLOntologyStorageException {

      Correspondence c=new Correspondence();
      c.saturateCorrespondence(source, target, fs);

      //  if (lks.isEmpty()) return;

        for (Linkey lk : lks) {
            lk.printLk();
            lk.saturateLinkey(source, target);
        }


    }


    private static Pair<Set<Correspondence>, Set<Correspondence>> buildCorrespondences(String fs) throws IOException, ParserConfigurationException, SAXException {
        Set<Alignment> alignments = Alignment.readAlignments(fs);


        Set<Correspondence> crs = new HashSet<>();
        Set<Correspondence> crsc = new HashSet<>();

        for (Alignment a : alignments) {

            String a1Merged = a.getElement1().toMergedForm();
            String a1Trim = a1Merged.trim();

            String a2Merged = a.getElement2().toMergedForm();
            String a2Trim = a2Merged.trim();

            addTocrs(crs, a1Merged, a1Trim, a2Merged, a2Trim);
            addTocrsc(crsc, a1Merged, a1Trim, a2Merged, a2Trim);
        }

        return new Pair<>(crs, crsc);
    }

    private static void addTocrsc(Set<Correspondence> crsc, String a1Merged, String a1Trim, String a2Merged, String a2Trim) {
        if (!a1Trim.startsWith("Relation") && !a2Trim.startsWith("Relation")
                && a1Trim.startsWith("Class") && a2Trim.startsWith("AttributeDomainRestriction")
                && !a2Trim.contains("or+") && !a2Trim.contains("inverse")
                && !a2Trim.contains("and+")) {

            crsc.add(new Correspondence(factory.getOWLClass(IRI.create(a1Merged)), factory.getOWLClass(IRI.create(a2Merged))));

        }
    }

    private static void addTocrs(Set<Correspondence> crs, String a1Merged, String a1Trim, String a2Merged, String a2Trim) {
        if (!a1Trim.startsWith("Relation") && !a2Trim.startsWith("Relation")
                && a1Trim.startsWith("Class") && a2Trim.startsWith("Class")
                && !a2Trim.contains("or+") && !a2Trim.contains("and+")) {

            crs.add(new Correspondence(factory.getOWLClass(IRI.create(a1Merged)), factory.getOWLClass(IRI.create(a2Merged))));
        }
    }

    private static int getCounterLks(File fileTarget, File fileSource, Set<Correspondence> crs) throws IOException, ParserConfigurationException, SAXException {
        int counter_lks = 0;
        for (Correspondence cc : crs) {
            File f = new File("output/" + cc);
            Set<Linkey> lqeq = linkex.execute(fileSource, fileTarget, f, cc);
            for (Linkey lk : lqeq) {
                lk.printLk();
                counter_lks++;
            }
        }
        return counter_lks;
    }


    public static double calculateAverageMeasure(String xmlFilePath) {
        try {
            File inputFile = new File(xmlFilePath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("measure");
            double sum = 0;
            for (int i = 0; i < nList.getLength(); i++) {
                Element element = (Element) nList.item(i);
                sum += Double.parseDouble(element.getTextContent());
            }
            return sum / nList.getLength();
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void removeRdfsLabels(OWLOntology ontology) {
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLAnnotationProperty rdfsLabel = factory.getRDFSLabel();

        List<RemoveAxiom> removeAxiomList = ontology.getAxioms()
                .stream()
                .filter(axiom -> axiom.getAnnotations().stream()
                        .anyMatch(annotation -> annotation.getProperty().equals(rdfsLabel)))
                .map(axiom -> new RemoveAxiom(ontology, axiom))
                .collect(Collectors.toList());

        manager.applyChanges(removeAxiomList);

    }


}
