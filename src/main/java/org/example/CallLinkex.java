package org.example;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class CallLinkex {
    Run r=new Run();

    Set<Linkey> execute(File source, File target){
        Set<Linkey> lks=new HashSet<>();
        System.out.println("Linkex starting ....");
        try {
            System.out.println("**********");
            r.runProcess("java -jar  ../linkex/LinkkeyDiscovery-1.0-SNAPSHOT-jar-with-dependencies.jar -o output/linkeys -f edoal "+source+" "+target );
            System.out.println("**********");
        } catch (Exception e) {
            e.printStackTrace();
        }
        ParseEdoal parser=new ParseEdoal();
        lks=parser.EDOALtoLKs(new File("output/linkeys"));
        return lks;
    }
    Set<Linkey> execute(File source, File target, Correspondance c){
        try {
            r.runProcess("pwd");
            System.out.println("**********");
            r.runProcess("java -jar  target/LinkkeyDiscovery-1.0-SNAPSHOT-jar-with-dependencies.jar -f edoal "+ source+ " "+target+ " "+ c.getC1()+ " "+c.getC2());
            System.out.println("**********");
        } catch (Exception e) {
            e.printStackTrace();
        }
        String c1 = c.getC1().toString();
        String c2 = c.getC2().toString();

        try {
            r.runProcess(c1.concat(c2));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        Set<Linkey> lks=new HashSet<>();
        ParseEdoal parser=new ParseEdoal();
        lks=parser.EDOALtoLKs(new File("output/linkeys"));
        return lks;
    }
}
