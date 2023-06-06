package alignment;

import junit.framework.Assert;
import org.checkerframework.checker.units.qual.A;
import org.example.Alignment;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

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
        System.out.println(alignments);
        Assert.assertEquals(137, alignments.size());


    }

}
