package org.example;

import org.apache.commons.lang3.time.StopWatch;
import org.semanticweb.owlapi.apibinding.OWLManager;
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
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class Main {


    static CallCanard canard;
    static CallLinkex linkex;
    private static final OWLDataFactory factory = new OWLDataFactoryImpl();

    public static void main(String[] args) throws IOException, OWLOntologyStorageException, ParserConfigurationException, SAXException {

        String pathSource = "cmt_0.ttl";

        String pathTarget = "conference_0.ttl";

        Double valueOfConf = 0.5;
        File fileSource = new File("test/" + pathSource);
        File fileTarget = new File("test/" + pathTarget);

        pipe(fileSource, fileTarget, fileSource, fileTarget, valueOfConf);
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

    static void pipe(File fileSourceI, File fileTargetI, File fileSource, File fileTarget, Double valueOfConf) throws IOException, OWLOntologyStorageException, ParserConfigurationException, SAXException {
        StopWatch pipe = new StopWatch();
        pipe.start();
        OWLOntology source = loadOntology(fileSourceI);
        OWLOntology target = loadOntology(fileTargetI);
        linkex = new CallLinkex("java", "/home/guilherme/IdeaProjects/DICAP/linkex/LinkkeyDiscovery-1.0-SNAPSHOT-jar-with-dependencies.jar");

        File f_start = new File("output/startlinkeys");

        if (!f_start.exists()) {
            f_start.createNewFile();
        }

        Set<Linkey> lks = linkex.execute(fileSourceI, fileTargetI, f_start);

        canard = new CallCanard("/home/guilherme/.jdks/openjdk-18/bin/java", "/home/guilherme/IdeaProjects/DICAP/canard/CanardE.jar");
        canard.execute(fileSourceI, fileTargetI, fileSource, fileTarget, valueOfConf);

        int t1 = fileSource.getName().lastIndexOf(".");
        int t2 = fileTarget.getName().lastIndexOf(".");

        String fs = "output/" + fileSource.getName().substring(0, t1) + "_" + fileTarget.getName().substring(0, t2) + "/th_" + valueOfConf.toString().replace(".", "_") + ".edoal";


        removeRdfsLabels(source);
        removeRdfsLabels(target);

        ParseEdoal pr = new ParseEdoal();
        pr.saveOntologies(source, fileSourceI);
        pr.saveOntologies(target, fileTargetI);

        Correspondence c = new Correspondence();


        canard.execute(fileSourceI, fileTargetI, fileSource, fileTarget, valueOfConf);

        pipe.stop();
        System.out.println("Pipe: " + pipe.getTime(TimeUnit.SECONDS));
        run(fileSourceI, fileTargetI, valueOfConf, source, target, lks, fs, pr, c);

    }

    private static void run(File fileSourceI, File fileTargetI, Double valueOfConf, OWLOntology source, OWLOntology target, Set<Linkey> lks, String fs, ParseEdoal pr, Correspondence c) throws IOException, ParserConfigurationException, SAXException, OWLOntologyStorageException {


        int counter_lks = 0;
        int counter_lkc = 0;

        for (int iter = 0; iter < 5; iter++) {
            saturateOntologies(source, target, lks, fs, c);

            removeRdfsLabels(source);
            removeRdfsLabels(target);

            File fileSource = new File("test/source_temp.ttl");
            File fileTarget = new File("test/target_temp.ttl");

            pr.saveOntologies(source, fileSource);
            pr.saveOntologies(target, fileTarget);

            canard.execute(fileSourceI, fileTargetI, fileSource, fileTarget, valueOfConf);

            int t1 = fileSource.getName().lastIndexOf(".");
            int t2 = fileTarget.getName().lastIndexOf(".");
            fs = "output/" + fileSource.getName().substring(0, t1) + "_" + fileTarget.getName().substring(0, t2) + "/th_" + valueOfConf.toString().replace(".", "_") + ".edoal";


            Set<Linkey> lks_w = linkex.execute(fileSource, fileTarget, new File("output/linkeys"));

            if (lks.size() == lks_w.size()) {

                break;

            }
            lks = lks_w;
        }

    }

    private static void saturateOntologies(OWLOntology source, OWLOntology target, Set<Linkey> lks, String fs, Correspondence c) throws IOException, ParserConfigurationException, SAXException, OWLOntologyStorageException {
        c.saturateCorrespondence(source, target, fs);

        if (lks.isEmpty()) return;

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
