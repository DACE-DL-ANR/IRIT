package org.example;

import de.uni_mannheim.informatik.dws.melt.matching_base.external.docker.MatcherDockerFile;
import eu.sealsproject.platform.res.tool.api.ToolBridgeException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;

public class CallMatcha {

    public static void  run(String source, String target, String matchaPath) throws IOException, ToolBridgeException {

        StringBuilder arguments1 = new StringBuilder();
        StringBuilder arguments2 = new StringBuilder();
        System.out.println(source);
      //  arguments1.append("java -cp ").append("\"" + matchaPath + ":lib/*\"").append(" "+matchaPath +"de.uni_mannheim.informatik.dws.melt.receiver_http.Main");
     //   curl -F "source=@source.xml" -F "target=@target.xml" http://127.0.0.1:8080/match > alignment.rdf
        arguments2.append("curl -F \"source=@");
        arguments2.append(source).append("\" -F \"target=@");
        arguments2.append(target).append("\" http://127.0.0.1:8080/match > alignment.rdf");
        System.out.println(arguments2);
       // curl -F "source=@/Users/khadijajradeh/Downloads/DICAPNEW/test/target-1.xml" -F "target=@/Users/khadijajradeh/Downloads/DICAPNEW/test/target-2.xml" http://127.0.0.1:8080/match > alignment.rdf
        try {
           // Run.runProcess(arguments1.toString());
            Run.runProcess(arguments2.toString());
            //      System.out.println("run successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    /*   java -cp "oaei-0.0.1-SNAPSHOT.jar:lib/*" de.uni_mannheim.informatik.dws.melt.receiver_http.Main
        MatcherDockerFile matcherDockerFile = new MatcherDockerFile("test_oaei:latest", new File(matchaPath));
        URL align = matcherDockerFile.align(Paths.get(source).toUri().toURL(),
                Paths.get(target).toUri().toURL());
        matcherDockerFile.close();
        return align.getPath();*/
    }
}
