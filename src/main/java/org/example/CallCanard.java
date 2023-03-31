package org.example;

import java.io.File;

public class CallCanard {

    private String canardPath;
    private String javaPath;

    public CallCanard(String javaPath, String canardPath) {
        this.javaPath = javaPath;
        this.canardPath = canardPath;
    }

    public void execute(File fileSourceI, File fileTargetI, File fileSource, File fileTarget, Double valueOfConf) {

        int t1 = fileSourceI.getName().lastIndexOf(".");
        int t2 = fileTargetI.getName().lastIndexOf(".");
        String s = "CQAs_" + fileSourceI.getName().substring(0, t1) + "_" + fileTargetI.getName().substring(0, t2);

        StringBuilder arguments = new StringBuilder();
        arguments.append(javaPath).append(" -jar ").append(canardPath).append(" ");
        arguments.append("/home/guilherme/IdeaProjects/DICAP/").append(fileSource).append(" ");
        arguments.append("/home/guilherme/IdeaProjects/DICAP/").append(fileTarget).append(" ");
        arguments.append(getClass().getResource("/").getPath() + s).append(" ");
        arguments.append("--range ").append(valueOfConf);

        try {

            Run.runProcess(arguments.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
