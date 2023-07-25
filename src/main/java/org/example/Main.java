package org.example;

import eu.sealsproject.platform.res.tool.api.ToolBridgeException;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.jena.base.Sys;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import utils.Pair;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


public class Main {


    private static final OWLDataFactory factory = new OWLDataFactoryImpl();
    static CallCanard canard;
    static CallLinkex linkex;
    static CallLogMap logMap;
    static CallAMLC amlc;

    private static File fileSource;
    private static File fileTarget;


    public static void main(String[] args) throws Exception {

        System.out.println(args[0]);
        System.out.println(args[1]);

        File fileSource = new File(args[0]);
        File fileTarget = new File(args[1]);
        String system = args[2];

        if (system.equals("1")) {
            runCanard(fileSource, fileTarget, args[3], args[4], args[5], args[6], Double.valueOf(args[7]), args[8]);
        } else if (system.equals("2")) {
            pipeLogmap(fileSource, fileTarget, args[3], args[4], args[5]);
        } else if (system.equals("3")) {
            runAMD(fileSource, fileTarget, args[3], args[4], args[5]);
        } else if (system.equals("4")) {
            runAtMatcher(fileSource, fileTarget, args[3], args[4], args[5]);
        } else if (system.equals("5")) {
            runMatcha(fileSource, fileTarget, args[3], args[4], args[5]);
        } else if (system.equals("6")) {
            runBasicMatcher(fileSource, fileTarget);
        }


    }
    private static void runBasicMatcher(File fileSource, File fileTarget) throws Exception {

        System.out.println("Piping basic label matcher has started ...");
        CallLabelMatch labelMatch=new CallLabelMatch();
        for(int i=0;i<3;i++){
            File output=new File("Output/outLB"+i+".txt");
        OWLOntology source = loadOntology(fileSource);
        OWLOntology target = loadOntology(fileTarget);

        // Convert OWL ontology to OntModel
        OntModel ontModel1 =convertOWLOntologyToOntModel(source);
        OntModel ontModel2 = convertOWLOntologyToOntModel(target);
        Set<Alignment> als= labelMatch.match(ontModel1,ontModel2, output);
        System.out.println("alignments obtained: "+als.size());
        Correspondence.saturateOntologies(source,target,als );
      //  Linkey.saturateSameAs(source,target,als);

        fileSource = new File( "output/source_tmp"+i+".xml");
        fileTarget = new File("output/target_tmp"+i+".xml");

        source.getOWLOntologyManager().saveOntology(source,  IRI.create(fileSource.toURI()));
        target.getOWLOntologyManager().saveOntology(target, IRI.create(fileTarget.toURI()));
        System.out.println("Ontologies saturated and saved");
        }
    }



    public static OntModel convertOWLOntologyToOntModel(OWLOntology ontology) {
        OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);

        // Get the ontology as an InputStream
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ontology.getOWLOntologyManager().saveOntology(ontology, outputStream);
            InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

            // Read the InputStream into the OntModel
            ontModel.read(inputStream, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ontModel;
    }


    private static void runMatcha(File fileSource, File fileTarget, String java, String matchaPath, String output) throws IOException, ToolBridgeException, OWLOntologyStorageException, ParserConfigurationException, SAXException {
        OWLOntology source = loadOntology(fileSource);
        OWLOntology target = loadOntology(fileTarget);
        for (int iter = 0; iter < 5; iter++) {

            String fs = CallMatcha.run(fileSource.getAbsolutePath(), fileTarget.getAbsolutePath(), matchaPath);

            Correspondence.saturateCorrespondenceSimple(source,target,  fs,"5");
            Linkey.saturateSameAs(source,target, fs, "4");

            fileSource = new File("test/source_tmp.ttl");
            fileTarget = new File("test/target_tmp.ttl");
            OWLOntologyManager manager1 = source.getOWLOntologyManager();
            OWLOntologyManager manager2 = target.getOWLOntologyManager();
            manager1.saveOntology(source, source.getFormat(), IRI.create(fileSource.toURI()));
            manager2.saveOntology(target, target.getFormat(), IRI.create(fileTarget.toURI()));
        }
    }

    private static void runAtMatcher(File fileSource, File fileTarget, String java, String path, String output) throws Exception {
        System.out.println("piping Atmatcher has started");
        CallAtMatcher callAtMatcher = new CallAtMatcher(java, path);

        Runtime.getRuntime().addShutdownHook(new Thread(callAtMatcher::close));

        //System.out.println("piping Atmatcher started 2");
        for (int iter = 1; iter < 4; iter++) {


            String fs = output + "atmr" + iter + ".rdf";
            callAtMatcher.start();
            callAtMatcher.run(fileSource.getAbsolutePath(), fileTarget.getAbsolutePath(), fs);
            callAtMatcher.close();

            System.out.println("In iteration number " + iter + " atmatcher was called on files " + fileSource.getName() + " and " + fileTarget.getName() + ".");


            OWLOntology source = loadOntology(fileSource);
            //   System.out.println("the nbr of axioms is: "+source.getAxioms().size());
            OWLOntology target = loadOntology(fileTarget);
            OWLOntologyManager manager1 = source.getOWLOntologyManager();
            OWLOntologyManager manager2 = target.getOWLOntologyManager();

            Correspondence.saturateCorrespondenceSimple(source,target,fs,"1");

            Linkey.saturateSameAs(source,target, fs, "1");


            fileSource = new File(output + "source_tmp" + iter + ".xml");
            fileTarget = new File(output + "target_tmp" + iter + ".xml");

            manager1.saveOntology(source, IRI.create(fileSource.toURI()));
            manager2.saveOntology(target, IRI.create(fileTarget.toURI()));
            System.out.println("Ontologies saturated and saved");
            //  removeAnnotationProperty("output/source_tmp.xml", "output/source_tmp.xml");
            //  removeAnnotationProperty("output/target_tmp.xml", "output/target_tmp.xml");

        }
    }

    private static void pipeLogmap(File fileSource, File fileTarget, String java, String logmap, String output) throws OWLOntologyStorageException, IOException, ParserConfigurationException, SAXException {


        StopWatch pipe = new StopWatch();
        pipe.start();
        OWLOntology source = loadOntology(fileSource);
        OWLOntology target = loadOntology(fileTarget);

        logMap = new CallLogMap("/Users/khadijajradeh/Downloads/logmap-matcher-standalone-july-2021/logmap-matcher-4.0.jar");
        pipe.stop();
        runLM(fileSource, fileTarget, source, target);
    }

    private static void runLM(File fileSource, File fileTarget, OWLOntology source, OWLOntology target) throws OWLOntologyStorageException, IOException, ParserConfigurationException, SAXException {

        String output = "output";
        for (int iter = 1; iter < 3; iter++) {


            logMap.execute(fileSource.getAbsolutePath(), fileTarget.getAbsolutePath(), output);
            String fs = output + "/logmap_overestimation.txt";
            Correspondence.saturateCorrespondenceSimple(source,target,  fs, "3");

            Linkey.saturateSameAs(source, target,fs, "3");


            fileSource = new File("test/source_tmp.owl");
            fileTarget = new File("test/target_tmp.owl");
            OWLOntologyManager manager1 = source.getOWLOntologyManager();
            OWLOntologyManager manager2 = target.getOWLOntologyManager();
            manager1.saveOntology(source, new RDFXMLDocumentFormat(), IRI.create(fileSource.toURI()));
            manager2.saveOntology(target, new RDFXMLDocumentFormat(), IRI.create(fileTarget.toURI()));

        }
    }


    private static void runAMD(File fileSource, File fileTarget, String python, String amdPath, String output) throws OWLOntologyStorageException, IOException, InterruptedException, ParserConfigurationException, SAXException {

        CallAMD amd = new CallAMD(python, amdPath);
        for (int iter = 1; iter < 4; iter++) {

            amd.run(fileSource.getAbsolutePath(), fileTarget.getAbsolutePath(), output);
            String fs = output + "/out.txt";
            OWLOntology source = loadOntology(fileSource);
            OWLOntology target = loadOntology(fileTarget);
            Correspondence.saturateCorrespondenceSimple(source, target,fs, "4");
            Linkey.saturateSameAs(source, target,fs, "4");

            fileSource = new File(output + "/source_tmp.owl");
            fileTarget = new File(output + "/target_tmp.owl");
            OWLOntologyManager manager1 = source.getOWLOntologyManager();
            OWLOntologyManager manager2 = target.getOWLOntologyManager();
            manager1.saveOntology(source, new RDFXMLDocumentFormat(), IRI.create(fileSource.toURI()));
            manager2.saveOntology(target, new RDFXMLDocumentFormat(), IRI.create(fileTarget.toURI()));
        }
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

    static void pipeAMLC(File fileSourceI, File fileTargetI, File fileSource, File fileTarget, Double valueOfConf) throws Exception {
        StopWatch pipe = new StopWatch();

        pipe.start();
        OWLOntology source = loadOntology(fileSourceI);
        OWLOntology target = loadOntology(fileTargetI);
//        linkex = new CallLinkex("../linkex/LinkkeyDiscovery-1.0-SNAPSHOT-jar-with-dependencies.jar");

        File f_start = new File("output/startlinkeys");

        if (!f_start.exists()) {
            f_start.createNewFile();
        }

        Set<Linkey> lks = linkex.execute(fileSourceI, fileTargetI, f_start);

        System.out.println(valueOfConf);
        amlc = new CallAMLC("../AML/bin/AML.jar");
        amlc.execute(fileSource, fileTarget);


        int t1 = fileSource.getName().lastIndexOf(".");
        int t2 = fileTarget.getName().lastIndexOf(".");

        String fs = "output/" + fileSource.getName().substring(0, t1) + "_" + fileTarget.getName().substring(0, t2) + "/th_" + valueOfConf.toString() + ".edoal";


        // removeRdfsLabels(source);
        //  removeRdfsLabels(target);

        ParseEdoal pr = new ParseEdoal();
        // pr.saveOntologies(source, fileSourceI);
        //  pr.saveOntologies(target, fileTargetI);

        Correspondence c = new Correspondence();
        //   Correspondence.separateCorrespondences(fs);


        // canard.execute(fileSourceI, fileTargetI, fileSource, fileTarget, valueOfConf);
        amlc.execute(fileSource, fileTarget);

        pipe.stop();
        System.out.println("Pipe: " + pipe.getTime(TimeUnit.SECONDS));
        runAMLC(fileSourceI, fileTargetI, valueOfConf, source, target, lks, fs, pr, c);

    }


    private static void runAMLC(File fileSourceI, File fileTargetI, Double valueOfConf, OWLOntology source, OWLOntology target, Set<Linkey> lks, String fs, ParseEdoal pr, Correspondence c) throws IOException, ParserConfigurationException, SAXException, OWLOntologyStorageException {

        for (int iter = 0; iter < 3; iter++) {
            System.out.println("Iteration number: " + iter++);
            // int t1 = fileSource.getName().lastIndexOf(".");
            //  int t2 = fileTarget.getName().lastIndexOf(".");
            //  fs = "output/" + fileSource.getName().substring(0, t1) + "_" + fileTarget.getName().substring(0, t2) + "/th_" + valueOfConf + ".edoal";
            //saturate with the starting lks in the first round.

            fileSource = new File("test/source_temp.ttl");
            fileTarget = new File("test/target_temp.ttl");
            pr.saveOntologies(source, fileSource);
            pr.saveOntologies(target, fileTarget);


//            canard.execute(fileSourceI, fileTargetI, fileSource, fileTarget, valueOfConf);
            Pair<Set<Correspondence>, Set<Correspondence>> pair = buildCorrespondences(fs);

            for (Correspondence cr : pair.first()) {
                System.out.println();
                File f = new File("output/linkeys_simple/" + cr.getC1().toString().substring(1, cr.getC1().toString().length() - 1).replace("://", "_") + "_" + cr.getC2().toString().substring(1, cr.getC2().toString().length() - 1).replace("://", "_"));

                Set<Linkey> lks_s = linkex.execute(fileSource, fileTarget, f, cr);

                for (Linkey lk : lks_s) {


                    lk.printLk();
                    lk.saturateLinkey(source, target);
                }


                Set<Linkey> lks_w = linkex.execute(fileSource, fileTarget, new File("output/linkeys"));

                if (lks.size() == lks_w.size()) {
                    break;
                }
                lks = lks_w;
            }

        }
    }


    private static void runCanard(File fileSourceI, File fileTargetI, String javaPath, String linkexPath, String canardPath, String cqas, Double valueOfConf, String output) throws Exception {
        fileSource = fileSourceI;
        fileTarget = fileTargetI;
        StopWatch pipe = new StopWatch();

        pipe.start();
        linkex = new CallLinkex(linkexPath, javaPath);
        canard = new CallCanard(canardPath, javaPath);

        for (int iter = 1; iter < 3; iter++) {
            System.out.println("Iteration: " + iter);
            OWLOntology source = loadOntology(fileSourceI);
            OWLOntology target = loadOntology(fileTargetI);


            File f = new File(output + "/linkeys" + iter);
            f.createNewFile();
            Set<Linkey> lks = linkex.execute(fileSource, fileTarget, f);
            System.out.println("Linkex Called!");

            canard.execute(fileSource, fileTarget, valueOfConf, cqas, output);

            System.out.println("Canard Called!");
            String[] fn = output.split("/");

            String fs = output + "/" + fn[fn.length-1] + "/th_" + valueOfConf.toString().replaceAll("\\.", "_") + ".edoal";
            //    System.out.println("fs name: "+fs);

            fileSource = new File(output + "/source_temp.ttl");
            fileTarget = new File(output + "/target_temp.ttl");
            //  int t1 = fileSource.getName().lastIndexOf(".");
            //   int t2 = fileTarget.getName().lastIndexOf(".");
            // Correspondence.separateCorrespondences(fs);

            saturateOntologies(source, target, lks, fs);
            System.out.println("Ontologies saturated!");
            OWLOntologyManager manager1 = source.getOWLOntologyManager();
            OWLOntologyManager manager2 = target.getOWLOntologyManager();
            manager1.saveOntology(source, IRI.create(fileSource.toURI()));
            manager2.saveOntology(target, IRI.create(fileTarget.toURI()));

            System.out.println("Ontologies saved!");


        }
        pipe.stop();
        System.out.println("Pipe: " + pipe.getTime(TimeUnit.SECONDS));
    }

    private static void saturateOntologies(OWLOntology source, OWLOntology target, Set<Linkey> lks, String fs) throws IOException, ParserConfigurationException, SAXException, OWLOntologyStorageException {

        //    Correspondence.saturateCorrespondence(source, target, fs);
        if (lks.isEmpty()) return;
        for (Linkey lk : lks) {
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
            //System.out.println(a.getElement1());

            String a2Merged = a.getElement2().toMergedForm();
            String a2Trim = a2Merged.trim();
            //System.out.println(a.getElement2());

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

    public static void removeAnnotationProperty(String filePath, String outputFilePath) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new File(filePath));

        NodeList nodeList = document.getElementsByTagName("owl:AnnotationProperty");
        for (int i = nodeList.getLength() - 1; i >= 0; i--) {
            Element element = (Element) nodeList.item(i);
            Node parent = element.getParentNode();
            parent.removeChild(element);
        }


        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

        Result output = new StreamResult(new File(outputFilePath));
        Source input = new DOMSource(document);
        transformer.transform(input, output);
    }


}
