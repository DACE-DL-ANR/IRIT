package org.example;

import java.io.File;

public class CallLogMap {
    String LogmapPath;

    public CallLogMap(String s) {
        LogmapPath=s;
    }

    public void setLogmapPath(String logmapPath) {
        LogmapPath = logmapPath;
    }

    public void execute(File fileSource, File fileTarget) {

//java -jar target/logmap-matcher-4.0.jar MATCHER source_temp.ttl  target_temp.ttl  ./output f

        StringBuilder arguments = new StringBuilder();
        //.append(javaPath).append(" -jar ")..append("/IdeaProjects/").append("/IdeaProjects/").
        arguments.append("java -jar ").append(LogmapPath).append(" MATCHER");
        arguments.append(fileSource).append(" ");
        arguments.append(fileTarget).append(" ./output f");

        try {

            Run.runProcess(arguments.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
