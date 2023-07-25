package org.example;

import java.io.File;

public class CallCanard {

    private final String canardPath;
    private final String javaPath;


    public CallCanard(String canardPath, String javaPath) {
        this.javaPath = javaPath;
        this.canardPath = canardPath;
    }

    public void execute(File fileSource, File fileTarget, Double valueOfConf, String cqasPath, String output) {

        int t1 = fileSource.getName().lastIndexOf(".");
        int t2 = fileTarget.getName().lastIndexOf(".");

        StringBuilder arguments = new StringBuilder();
        arguments.append(javaPath);
        arguments.append(" -jar ").append(canardPath).append(" ");
        arguments.append(fileSource).append(" ");
        arguments.append(fileTarget).append(" ");
        arguments.append(cqasPath).append(" ");
        arguments.append("--output").append(" ").append(output).append(" ");
        arguments.append("--range ").append(valueOfConf);
        System.out.println("==============================================");
        System.out.println(arguments);
        try {

            //   System.out.println(arguments);
            // Run.runProcess("java -jar ../canard/CanardE.jar"+" "+ fileSource+" "+fileTarget+ " "+ s + " --range "+valueOfConf);

            Run.runProcess(arguments.toString());


//            Run.runProcess(arguments.toString());


        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
