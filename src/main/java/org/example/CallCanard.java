package org.example;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;

public class CallCanard {
    Run r=new Run();
    ArrayList<Correspondance> execute(File source, File target){
        System.out.println("Canard starting ....");
        try {

            System.out.println("**********");
           // java -jar canard.jar source target cqa --output outputDirectory --range 0.5:0.9:0.1
            r.runProcess("java -jar ../canard/CanardE.jar"+" "+ source+" "+target+" test/accepted_paper.sparql --range 0:4");
            System.out.println("**********");
        } catch (Exception e) {
            e.printStackTrace();
        }
        ArrayList<Correspondance> lks=new ArrayList<>();
        return lks;
    }


}
