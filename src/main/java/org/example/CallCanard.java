package org.example;
import java.io.File;

public class CallCanard {
    Run r=new Run();
    void execute(File fileSourceI, File fileTargetI, File fileSource, File fileTarget, Double valueOfConf) {

        System.out.println("Canard starting ....");
        int t1 = fileSourceI.getName().lastIndexOf(".");
        int t2 = fileTargetI.getName().lastIndexOf(".");
        String s = "CQAs_" + fileSourceI.getName().substring(0, t1) + "_" + fileTargetI.getName().substring(0, t2);
       // System.out.println(s);
        try {

            System.out.println("**********");
           // java -jar canard.jar source target cqa --output outputDirectory --range 0.5:0.9:0.1
            r.runProcess("java -jar ../canard/CanardE.jar"+" "+ fileSource+" "+fileTarget+ " "+ s + " --range "+valueOfConf);
            System.out.println("**********");
        } catch (Exception e) {
            e.printStackTrace();
        }


    }


}
