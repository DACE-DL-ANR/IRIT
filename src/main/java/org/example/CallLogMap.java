package org.example;

public class CallLogMap {

    private final String javaPath;
    private final String logmapPath;

    public CallLogMap(String javaPath, String s) {
        this.javaPath = javaPath;
        logmapPath = s;
    }

    public void execute(String fileSource, String fileTarget, String output) {

//java -jar target/logmap-matcher-4.0.jar MATCHER source_temp.ttl  target_temp.ttl  ./output f
        StringBuilder arguments = new StringBuilder();
        arguments.append(javaPath).append(" -jar ").append(logmapPath).append(" ");
        arguments.append("MATCHER ");
        arguments.append("file://").append(fileSource).append(" ");
        arguments.append("file://").append(fileTarget).append(" ");
        arguments.append("file://").append(output).append(" false");

        try {

            Run.runProcess(arguments.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
