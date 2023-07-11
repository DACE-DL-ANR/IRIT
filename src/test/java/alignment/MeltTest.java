package alignment;

import de.uni_mannheim.informatik.dws.melt.matching_base.external.docker.MatcherDockerFile;
import eu.sealsproject.platform.res.tool.api.ToolBridgeException;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

public class MeltTest {

    @Test
    public void test0() throws IOException, ToolBridgeException {
        MatcherDockerFile matcherDockerFile = new MatcherDockerFile("test_oaei:latest", new File("/home/guilherme/IdeaProjects/DICAP/matcha/Matcha.tar.gz"));
        URL align = matcherDockerFile.align(Paths.get("/home/guilherme/Documents/kg/knowledge/swg.xml").toUri().toURL(),
                Paths.get("/home/guilherme/Documents/kg/knowledge/swtor.xml").toUri().toURL());
        System.out.println(align);
        matcherDockerFile.close();
    }

    @Test
    public void test1() throws IOException, InterruptedException {

    }
}
