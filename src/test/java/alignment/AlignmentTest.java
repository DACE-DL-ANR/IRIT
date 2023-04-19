package alignment;

import fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import junit.framework.Assert;
import org.example.Alignment;
import org.example.AlignmentOWL;
import org.junit.jupiter.api.Test;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.rdf.rdfxml.renderer.RDFXMLRenderer;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class AlignmentTest {



    @Test
    public void test0() throws IOException, ParserConfigurationException, SAXException {
        URL resource = getClass().getResource("/th_0_4.edoal");
        Set<Alignment> alignments = Alignment.readAlignments(resource.getPath());
    }

    @Test
    public void test1() throws URISyntaxException {
        URL resource = getClass().getResource("/example1.txt");
        Path path = Paths.get(resource.toURI());
        Set<Alignment> alignments = Alignment.readAlignmentsTxt(path);

        Assert.assertEquals(137, alignments.size());


    }




    @Test
    public void edoal2owlapi() throws URISyntaxException, ParserConfigurationException, IOException, SAXException {
        URL resource = getClass().getResource("/th_0.7.edoal");
        Path path = Paths.get(resource.toURI());

//        Set<AlignmentOWL> alignments = AlignmentOWL.parseAlignment(path.toFile());
//
//        for (AlignmentOWL alignment : alignments) {
//            Assert.assertNotNull(alignment.getEntity1());
//            Assert.assertNotNull(alignment.getEntity2());
//        }

    }

    static class SwitchWriter extends PrintWriter {
        private boolean enabled = false;


        public SwitchWriter(Writer out) {
            super(out);
        }

        public void toggle(){
            enabled = !enabled;
        }

        public void enable(){
            enabled = true;
        }


        @Override
        public void print(String s) {
            if(enabled){
                super.print(s);
            }
        }
    }

    @Test
    public void test2() throws Exception {
        AlignmentParser parser = new AlignmentParser(0);


        Set<Path> collect = Files.walk(Paths.get("/home/guilherme/IdeaProjects/ComplexAlignmentGenerator/output/conference/test_0.8/"), 1)
                .filter(path -> !Files.isDirectory(path)).collect(Collectors.toSet());

        int total = 0;
        int renderedTotal = 0;
        int notRenderedTotal = 0;

        System.out.println("EDOAL Alignments from https://framagit.org/IRIT_UT2J/ComplexAlignmentGenerator/-/tree/master/output/conference/data_100");
        System.out.println("Align API from https://gitlab.inria.fr/moex/alignapi");
        System.out.println();
        for (Path path : collect) {
            System.out.println(path.getFileName());
            OWLAxiomsRendererVisitor rendererVisitor = new OWLAxiomsRendererVisitor(new PrintWriter(OutputStream.nullOutputStream()));
            BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get("/home/guilherme/Documents/dicap/edoal/" + path.getFileName()));
            SwitchWriter switchWriter = new SwitchWriter(new PrintWriter(bufferedWriter));
            RDFRendererVisitor rdfRendererVisitor = new RDFRendererVisitor(switchWriter);
            org.semanticweb.owl.align.Alignment parse = parser.parse(path.toUri());
            System.out.println("Total: " + parse.nbCells());
            total += parse.nbCells();
            try {
                parse.render(rdfRendererVisitor);
                parse.render(rendererVisitor);
            } catch (Exception ignored){

            }
            switchWriter.enable();
            switchWriter.print("<?xml version='1.0' encoding='utf-8' standalone='no'?>\n" +
                    "<rdf:RDF xmlns='http://knowledgeweb.semanticweb.org/heterogeneity/alignment#'\n" +
                    "         xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'\n" +
                    "         xmlns:xsd='http://www.w3.org/2001/XMLSchema#'\n" +
                    "         xmlns:alext='http://exmo.inrialpes.fr/align/ext/1.0/'\n" +
                    "         xmlns:align='http://knowledgeweb.semanticweb.org/heterogeneity/alignment#'\n" +
                    "         xmlns:edoal='http://ns.inria.org/edoal/1.0/#'>\n" +
                    "<Alignment>\n" +
                    "  <xml>yes</xml>\n" +
                    "  <level>2EDOAL</level>\n" +
                    "  <type>**</type>\n" +
                    "  ");
            int rendered = 0;
            int notRendered = 0;
            for (Cell a : parse) {

                try {
                    rendererVisitor.visit(a);
                    rendered++;
                } catch (Exception e){
                    rdfRendererVisitor.visit(a);
                    notRendered++;
                }

            }
            System.out.println("Rendered: " + rendered);
            System.out.println("Not rendered: " + notRendered);
            System.out.println("-----------------------------");
            switchWriter.print("</Alignment>\n" +
                    "</rdf:RDF>");
            switchWriter.flush();
            switchWriter.close();
            renderedTotal += rendered;
            notRenderedTotal += notRendered;
        }

        System.out.println("Global");
        System.out.println("Total: " + total);
        System.out.println("Rendered: " + renderedTotal);
        System.out.println("Not rendered: " + notRenderedTotal);
        // not rendered percentage rounded to 2 decimal places
        System.out.println("Not rendered percentage: " + Math.round((notRenderedTotal * 100.0 / total) * 100.0) / 100.0 + "%");

    }


    @Test
    public void test3() throws Exception {

    }


}
