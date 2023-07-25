package alignment;

import junit.framework.Assert;
import org.example.CallLinkex;
import org.example.Linkey;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.xml.sax.SAXException;

import javax.ws.rs.core.Link;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Set;

public class LinkeyTest {


    @Test
    public void test0() throws URISyntaxException, OWLOntologyCreationException, IOException, ParserConfigurationException, SAXException, OWLOntologyStorageException {
       /* URL resource = getClass().getResource("/logmap_overestimation.txt");
        URL ont1 = getClass().getResource("/swg.xml");
        URL ont2 = getClass().getResource("/swtor.xml");*/
        File f1=new File("test/cmt_100.ttl");
        File f2=new File("test/conference_100.ttl");
        //cmt_100.ttl
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology1 = manager.loadOntologyFromOntologyDocument(f1);
        OWLOntology ontology2 = manager.loadOntologyFromOntologyDocument(f2);
        CallLinkex linkex = new CallLinkex("../linkex/LinkkeyDiscovery-1.0-SNAPSHOT-jar-with-dependencies.jar");

        File f_start = new File("output/startlinkeys");

        Set<Linkey> lks=linkex.execute(f1, f2, f_start);
        System.out.println("The number of link keys obtained is: "+lks.size());
        for (Linkey lk:lks)
        lk.saturateLinkey(ontology1, ontology2);
        File fileSource = new File("test/Saturated_Datasets/"+f1.getName().substring(0,f1.getName().lastIndexOf("."))+"_"+f2.getName().substring(0,f2.getName().lastIndexOf("."))+"/"+f1.getName());
        File fileTarget = new File("test/Saturated_Datasets/"+f1.getName().substring(0,f1.getName().lastIndexOf("."))+"_"+f2.getName().substring(0,f2.getName().lastIndexOf("."))+"/"+f2.getName());
        manager.saveOntology(ontology1,  IRI.create(fileSource.toURI()));
        manager.saveOntology(ontology2,  IRI.create(fileTarget.toURI()));
        System.out.println("Finished");
      //  Assert.assertEquals(115273, ontology1.axioms().count());

    }
}
