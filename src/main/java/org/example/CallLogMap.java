package org.example;

public class CallLogMap {


    private final String logmapPath;

    public CallLogMap(String s) {

        logmapPath = s;
    }

    public void execute(String fileSource, String fileTarget, String output) {

//java -jar target/logmap-matcher-4.0.jar MATCHER source_temp.ttl  target_temp.ttl  ./output f
  //      System.out.println("logmap path "+logmapPath+" filesrc "+fileSource+" filetrgt "+fileTarget );
        StringBuilder arguments = new StringBuilder();
        arguments.append("java -jar ").append(logmapPath).append(" ");
        arguments.append("MATCHER ");
        arguments.append(fileSource).append(" ");
        arguments.append(fileTarget).append(" ");
        arguments.append(output).append(" false");

        try {

            Run.runProcess(arguments.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
