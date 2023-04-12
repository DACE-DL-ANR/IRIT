package alignment;

import junit.framework.Assert;
import org.example.Alignment;
import org.example.AlignmentOWL;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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

        Set<AlignmentOWL> alignments = AlignmentOWL.parseAlignment(path.toFile());

        for (AlignmentOWL alignment : alignments) {
            Assert.assertNotNull(alignment.getEntity1());
            Assert.assertNotNull(alignment.getEntity2());
        }

    }





}
