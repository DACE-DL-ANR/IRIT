package org.example;

import org.coode.owlapi.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;


public class Main {

    static boolean enter = true;
    static CallCanard canard;
    static CallLinkex linkex;
    private static OWLDataFactory factory = new OWLDataFactoryImpl();

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

    static void pipe(File fileSourceI, File fileTargetI, File fileSource, File fileTarget, Double valueOfConf) throws Exception {


        OWLOntology source;
        OWLOntology target;
        try {
            source = loadOntology(fileSourceI);
            target = loadOntology(fileTargetI);
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException("Can't load the ontologies");
        }
        linkex = new CallLinkex();
        File f_start = new File("output/startlinkeys");
        if (!f_start.exists()) {
            f_start.createNewFile();
        }
        Set<Linkey> lks;
      //  System.out.println("Extracting the link keys before launching Canard");
       // lks = linkex.execute(fileSourceI, fileTargetI, f_start);
       canard = new CallCanard();
       canard.execute(fileSourceI, fileTargetI, fileSource, fileTarget, valueOfConf);
        int t1 = fileSource.getName().lastIndexOf(".");
        int t2 = fileTarget.getName().lastIndexOf(".");

      /*  System.out.println("The average value of confidence obtained from canard with labels is: "+calculateAverageMeasure(fs));
        System.out.println("The number of alignments obtained from canard with labels is"+Alignment.readAlignments(fs).size());
        removeRdfsLabels(source);
        removeRdfsLabels(target);
        ParseEdoal pr = new ParseEdoal();
        pr.saveOntologies(source,fileSourceI);
        pr.saveOntologies(target,fileTargetI);

        int iter=0;*/
        Correspondance c=new Correspondance();
        String fs = "output/" + fileSource.getName().substring(0, t1) + "_" + fileTarget.getName().substring(0, t2) + "/th_" + valueOfConf + ".edoal";
        c.seperateCorrespondances(fs);
       // System.out.println(setcls);
        /*  System.out.println("Extracting the correspondances before saturating with linkeys");
        canard.execute(fileSourceI, fileTargetI, fileSource, fileTarget, valueOfConf);
        System.out.println("The average value of confidence without labels is: "+calculateAverageMeasure(fs));
        Set<Linkey> lks_w;
        Set<Correspondance> crs=new HashSet<>();
        Set<Correspondance> crsc=new HashSet<>();

        int counter_lks=0;
        int counter_lkc=0;
      while (enter) {
            iter++;

            c.saturateCorrespondance(source,target,fs);

            if (!lks.isEmpty()) {
                System.out.println("Saturating the graph the " + lks.size() + " link keys obtained");
                for (Linkey lk : lks) {
                    lk.printLk(lk);
                    lk.saturateLinkey(source, target, lk);
                }
                System.out.println("Saturation finished");
            } else {
                System.out.println("No link keys obtained");
            }
            fileSource = new File("test/source_temp.ttl");
            removeRdfsLabels(source);
            pr.saveOntologies(source, fileSource);
            fileTarget = new File("test/target_temp.ttl");
            removeRdfsLabels(target);
            pr.saveOntologies(target, fileTarget);
            canard.execute(fileSourceI, fileTargetI, fileSource, fileTarget, valueOfConf);
            t1 = fileSource.getName().lastIndexOf(".");
            t2 = fileTarget.getName().lastIndexOf(".");
            fs = "output/" + fileSource.getName().substring(0, t1) + "_" + fileTarget.getName().substring(0, t2) + "/th_" + valueOfConf + ".edoal";
            Set<Alignment> A=Alignment.readAlignments(fs);
            System.out.println("In iteration "+iter+" we have obtained "+A.size() +" correspondences");
            for(Alignment a:A) {

                //tryng to avoid mappings between relations

                if (!a.getElement1().toMergedForm().trim().startsWith("Relation")&&!a.getElement2().toMergedForm().trim().startsWith("Relation")&&a.getElement1().toMergedForm().trim().startsWith("Class")&&a.getElement2().toMergedForm().trim().startsWith("Class")&&!a.getElement2().toMergedForm().trim().contains("or+")&&!a.getElement2().toMergedForm().trim().contains("and+")) {

                    crs.add(new Correspondance(factory.getOWLClass(IRI.create(a.getElement1().toMergedForm())), factory.getOWLClass(IRI.create(a.getElement2().toMergedForm()))));
                }
                if(!a.getElement1().toMergedForm().trim().startsWith("Relation")&&!a.getElement2().toMergedForm().trim().startsWith("Relation")&&a.getElement1().toMergedForm().trim().startsWith("Class")&&a.getElement2().toMergedForm().trim().startsWith("AttributeDomainRestriction")&&!a.getElement2().toMergedForm().trim().contains("or+")&&!a.getElement2().toMergedForm().trim().contains("inverse")&&!a.getElement2().toMergedForm().trim().contains("and+")){
                    System.out.println("Element one: "+a.getElement1().toMergedForm());
                    System.out.println("Element two: "+a.getElement2().toMergedForm());
                    crsc.add(new Correspondance(factory.getOWLClass(IRI.create(a.getElement1().toMergedForm())), factory.getOWLClass(IRI.create(a.getElement2().toMergedForm()))));

                }
             /*   if(!a.getElement1().toMergedForm().trim().startsWith("Relation")&&!a.getElement2().toMergedForm().trim().startsWith("Relation")&&a.getElement1().toMergedForm().trim().startsWith("Class")&&a.getElement2().toMergedForm().trim().startsWith("AttributeDomainRestriction")&&a.getElement2().toMergedForm().trim().contains("or+")&&!a.getElement2().toMergedForm().trim().contains("and+")&&!a.getElement2().toMergedForm().trim().contains("inverse")){

                    crsc.add(new Correspondance(factory.getOWLClass(IRI.create(a.getElement1().toMergedForm())), factory.getOWLClass(IRI.create(a.getElement2().toMergedForm()))));
                }*/
       /*     }


            System.out.println("Extracting link keys between the "+ crs.size()+" equivalent simple classes");
            for(Correspondance cc:crs){
                File f=new File("output/"+cc);

                Set<Linkey> lqeq = linkex.execute(fileSource, fileTarget, f, cc);
                for(Linkey lk:lqeq){
                    lk.printLk(lk);
                    counter_lks++;
                 //   lk.saturateLinkey(source,target,lk);
                }

            }
            System.out.println("There are "+counter_lks+" link keys between simple classes");
            System.out.println("Extracting link keys between the (exists)" +crsc.size()+" equivalent complex classes");
            for(Correspondance cc:crsc) {
                    File f = new File("output/" + cc);
                    System.out.println("Extracting link keys between " + cc.getC1() + " and " + cc.getC2());

                    Set<Linkey> lqeq = linkex.executec(fileSource, fileTarget, f, cc);
                    System.out.println("Now saturating");
                    for (Linkey lk : lqeq) {
                        lk.printLk(lk);
                        counter_lkc++;
                        //  lk.saturateLinkey(source, target, lk);
                    }
                }

            System.out.println("There are "+counter_lkc+" link keys between complex classes");

            try {
                source = loadOntology(fileSource);
                target = loadOntology(fileTarget);
            } catch (OWLOntologyCreationException e) {
                throw new RuntimeException("Can't load the ontologies");
            }
            canard.execute(fileSourceI, fileTargetI, fileSource, fileTarget, valueOfConf);
            System.out.println("The average value of confidence in iteration "+iter+" is: "+calculateAverageMeasure(fs));
            lks_w = linkex.execute(fileSource, fileTarget, new File("output/linkeys"));

            if (lks.size() == lks_w.size()) {

                System.out.println("We have reached a stationary situation after " +iter+" iterations.");
                enter = false;
                break;

            }
            lks = lks_w;
        }*/

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
            double average = sum / nList.getLength();
            return average;
        } catch (Exception e) {
            e.printStackTrace();
            return 0; // or throw an exception, depending on your use case
        }
    }

    public static void removeRdfsLabels(OWLOntology ontology) {
        OWLOntologyManager manager = ontology.getOWLOntologyManager();
        OWLDataFactory factory = manager.getOWLDataFactory();
        OWLAnnotationProperty rdfsLabel = factory.getRDFSLabel();

        manager.applyChanges(ontology.getAxioms().stream()
                .filter(axiom -> axiom.getAnnotations().stream()
                        .anyMatch(annotation -> annotation.getProperty().equals(rdfsLabel)))
                .map(axiom -> new RemoveAxiom(ontology, axiom))
                .collect(Collectors.toList()));

        try {
            manager.saveOntology(ontology);
        } catch (OWLOntologyStorageException e) {
            // Handle exception
            e.printStackTrace();
        }
    }









    public static void main(String[] args) throws Exception {


       Scanner scanner = new Scanner(System.in);
        System.out.println("Please enter the source ontology");
        String pathSource = scanner.nextLine().trim();

        System.out.println("Please enter the target ontology");
        String pathTarget = scanner.nextLine().trim();

        System.out.println("Enter the value of confidence");
        Double valueOfConf = Double.valueOf(scanner.nextLine().trim());

        File fileSource = new File("test/"+pathSource);
        File fileTarget = new File("test/"+pathTarget);

        pipe( fileSource, fileTarget, fileSource, fileTarget, valueOfConf);
      //  lineDiff();*/
   /* Correspondance c=new Correspondance();
        String fs = "output/" + "edas_100_conference_100/th_0.7.edoal";
        c.seperateCorrespondances(fs);*/
    }
}
