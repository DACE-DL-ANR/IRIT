package org.example;
import org.semanticweb.owlapi.model.OWLClass;
import org.xml.sax.SAXException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Set;

public class CallLinkex {


    Set<Linkey> execute(File source, File target, File result) throws IOException, ParserConfigurationException, SAXException {

        if(!result.exists()) {
            result.createNewFile();
        }
        Set<Linkey> lks;
        System.out.println("Linkex starting ....");
        String s="*********";
        try {
            System.out.println(s);
            Run.runProcess("java  -Xmx10012M -jar  ../linkex/LinkkeyDiscovery-1.0-SNAPSHOT-jar-with-dependencies.jar -o output/"+result.getName()+" -t eq -f edoal -c1 " + "http://www.w3.org/2002/07/owl#Thing " +source+" "+target );
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
            Run.runProcess("java  -Xmx10012M -jar  ../linkex/LinkkeyDiscovery-1.0-SNAPSHOT-jar-with-dependencies.jar -d 0.4 -s 0.4 -o output/"+result.getName()+" -t eq -f edoal -c1 "+ cls+ " " +source+" "+target );
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
        try {
            System.out.println(s);
            Run.runProcess("java  -Xmx10012M -jar  ../linkex/LinkkeyDiscovery-1.0-SNAPSHOT-jar-with-dependencies.jar -d 0.4 -s 0.4 -o output/"+result.getName()+" -t eq -f edoal -c1 "+ "http://"+c.getC1().toString().substring(1,c.getC1().toString().length()-1).replace("#","@")+ " -c2 "+"http://"+c.getC2().toString().substring(1,c.getC2().toString().length()-1).replace("#","@")+ " " + source+ " "+target);
            System.out.println(s);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Set<Linkey> lks;
        ParseEdoal parser=new ParseEdoal();
        lks=parser.EDOALtoLKs(result);
        return lks;
    }
}
