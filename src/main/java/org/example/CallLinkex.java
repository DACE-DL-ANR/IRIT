package org.example;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Set;

public class CallLinkex {


    Set<Linkey> execute(File source, File target, File result) throws IOException, ParserConfigurationException, SAXException {

        //  if(!result.exists()) {
        result.createNewFile();
        //  }
        Set<Linkey> lks;
        System.out.println("Linkex starting ....");
        String s="*********";
        try {
            System.out.println(s);
            Run.runProcess("java  -Xmx10012M -jar  ../linkex/LinkkeyDiscovery-1.0-SNAPSHOT-jar-with-dependencies-inv.jar -i -c -o output/"+result.getName()+" -t eq -f edoal -c1 " + "http://www.w3.org/2002/07/owl#Thing " +source+" "+target );
            System.out.println(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        lks=ParseEdoal.EDOALtoLKs(result);
        return lks;
    }
    Set<Linkey> execute(File source, File target, File result,  String cls) throws IOException, ParserConfigurationException, SAXException {
        String s="*********";
        try {
            System.out.println(s);
            Run.runProcess("java  -Xmx10012M -jar  ../linkex/LinkkeyDiscovery-1.0-SNAPSHOT-jar-with-dependencies-inv.jar -c -i -o output/"+result.getName()+" -t eq -f edoal -c1 "+ cls+ " " +source+" "+target );
            System.out.println(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Set<Linkey> lks;
        ParseEdoal parser=new ParseEdoal();
        lks=parser.EDOALtoLKs(result);
        return lks;
    }
    Set<Linkey> execute(File source, File target, File result, Correspondance c) throws IOException, ParserConfigurationException, SAXException {
        String s="*********";
        result.createNewFile();
        try {
            System.out.println(s);
            Run.runProcess("java  -Xmx10012M -jar  ../linkex/LinkkeyDiscovery-1.0-SNAPSHOT-jar-with-dependencies-inv.jar -d 0.4 -s 0.4 -i -c -o output/"+result.getName()+" -t eq -f edoal -c1 "+ c.getC1().toString().substring(7,c.getC1().toString().length()-1).replace("_","#")+ " -c2 "+ c.getC2().toString().substring(7,c.getC2().toString().length()-1).replace("_","#")+ " " + source+ " "+target);
            System.out.println(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Set<Linkey> lks;
        ParseEdoal parser=new ParseEdoal();
        lks=parser.EDOALtoLKs(result);
        return lks;
    }

    public Set<Linkey> executec(File source, File target, File result, Correspondance cc) throws IOException, ParserConfigurationException, SAXException {
        String s="*********";

        try {
            System.out.println(s);
            Run.runProcess("java  -Xmx10012M -jar  ../linkex/LinkkeyDiscovery-1.0-SNAPSHOT-jar-with-dependencies-inv.jar -d 0.4 -s 0.4 -i -c -o output/"+result.getName()+" -t eq -f edoal -c1 "+ cc.getC1().toString().substring(7,cc.getC1().toString().length()-1).replace("_","#")+ " -c2 "+ "http://"+cc.getC2().toString().substring(1, cc.getC2().toString().length()-1).replace("_","+")+ " " + source+ " "+target);
            System.out.println(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        result.createNewFile();
        Set<Linkey> lks;
        ParseEdoal parser=new ParseEdoal();
        lks=parser.EDOALtoLKs(result);
        return lks;
    }
}
