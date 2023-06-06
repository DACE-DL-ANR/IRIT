package org.example;

import java.io.File;

public class CallCanard {

    private String canardPath;
    private String javaPath;


    public CallCanard(String canardPath) {

        this.canardPath = canardPath;
    }
    public void execute(File fileSourceI, File fileTargetI, File fileSource, File fileTarget, Double valueOfConf) {

        int t1 = fileSourceI.getName().lastIndexOf(".");
        int t2 = fileTargetI.getName().lastIndexOf(".");
        String s = "target/classes/CQAs_" + fileSourceI.getName().substring(0, t1) + "_" + fileTargetI.getName().substring(0, t2);

        StringBuilder arguments = new StringBuilder();
        arguments.append("java -jar ").append(canardPath).append(" ");
        arguments.append(fileSource).append(" ");
        arguments.append(fileTarget).append(" ");
        arguments.append(s).append(" ");
        arguments.append("--range ").append(valueOfConf);

        try {
         //   System.out.println(arguments);
           // Run.runProcess("java -jar ../canard/CanardE.jar"+" "+ fileSource+" "+fileTarget+ " "+ s + " --range "+valueOfConf);
           Run.runProcess(arguments.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
