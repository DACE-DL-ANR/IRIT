package org.example;

import java.io.File;

public class CallAMLC {
    String AMLCPath;

    public CallAMLC(String s) {
        AMLCPath=s;
    }

    public void setLogmapPath(String logmapPath) {
        AMLCPath= logmapPath;
    }

    public void execute(File fileSource, File fileTarget) {

//java -jar target/logmap-matcher-4.0.jar MATCHER source_temp.ttl  target_temp.ttl  ./output f

        StringBuilder arguments = new StringBuilder();
        //.append(javaPath).append(" -jar ")..append("/IdeaProjects/").append("/IdeaProjects/").
        arguments.append("java -jar ").append(AMLCPath);
        arguments.append("-s "+fileSource).append(" ");
        arguments.append("-t "+fileTarget).append(" -o ../output f");

        try {

            Run.runProcess(arguments.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
