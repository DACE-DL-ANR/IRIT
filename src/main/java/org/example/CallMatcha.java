package org.example;

import de.uni_mannheim.informatik.dws.melt.matching_base.external.docker.MatcherDockerFile;
import eu.sealsproject.platform.res.tool.api.ToolBridgeException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

public class CallMatcha {

    public static String run(String source, String target, String matchaPath) throws IOException, ToolBridgeException {
        MatcherDockerFile matcherDockerFile = new MatcherDockerFile("test_oaei:latest", new File(matchaPath));
        URL align = matcherDockerFile.align(Paths.get(source).toUri().toURL(),
                Paths.get(target).toUri().toURL());
        matcherDockerFile.close();
        return align.getPath();
    }
}
