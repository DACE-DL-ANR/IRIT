package alignment;

import junit.framework.Assert;
import org.example.Linkey;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

public class LinkeyTest {


    @Test
    public void test0() throws URISyntaxException, OWLOntologyCreationException {
        URL resource = getClass().getResource("/logmap_overestimation.txt");
        URL ont1 = getClass().getResource("/swg.xml");
        URL ont2 = getClass().getResource("/swtor.xml");
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        OWLOntology ontology1 = manager.loadOntologyFromOntologyDocument(Paths.get(ont1.toURI()).toFile());
        OWLOntology ontology2 = manager.loadOntologyFromOntologyDocument(Paths.get(ont2.toURI()).toFile());

        Linkey.saturateSameAs(ontology1, ontology2, resource.getPath());

        Assert.assertEquals(115273, ontology1.axioms().count());

    }
}
