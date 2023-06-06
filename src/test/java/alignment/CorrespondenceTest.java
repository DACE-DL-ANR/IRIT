package alignment;

import junit.framework.Assert;
import org.example.Correspondence;
import org.junit.jupiter.api.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;

public class CorrespondenceTest {


    @Test
    public void testSaturateSimpleTxt() throws URISyntaxException, OWLOntologyCreationException {
        URL resource = getClass().getResource("/logmap_overestimation.txt");
        URL ont1 = getClass().getResource("/swg.xml");
        URL ont2 = getClass().getResource("/swtor.xml");
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

        OWLOntology ontology1 = manager.loadOntologyFromOntologyDocument(Paths.get(ont1.toURI()).toFile());
        OWLOntology ontology2 = manager.loadOntologyFromOntologyDocument(Paths.get(ont2.toURI()).toFile());

        Correspondence.saturateCorrespondenceSimple(ontology1, ontology2, resource.getPath());

        Assert.assertEquals(90, ontology1.getClassesInSignature().size());
        Assert.assertEquals(121, ontology2.getClassesInSignature().size());
    }
}