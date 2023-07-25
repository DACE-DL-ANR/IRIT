package org.example;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.Set;

public class CallLinkex {


    private static String linkexPath;


    public CallLinkex( String linkexPath) {
    //    this.javaPath = javaPath;
        this.linkexPath = linkexPath;
    }

    public static Set<Linkey> execute(File source, File target, File result) throws IOException, ParserConfigurationException, SAXException {

        if(!result.exists()) {
            result.createNewFile();
        }



        String s = "*********";


        StringBuilder arguments = new StringBuilder();
        arguments.append("java -jar ").append(linkexPath).append(" ");
        arguments.append("-i -c -o output/").append(result.getName()).append(" ");
        arguments.append("-t eq -f edoal -c1 http://www.w3.org/2002/07/owl#Thing ").append(source).append(" ").append(target);

        try {
            Run.runProcess(arguments.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ParseEdoal.EDOALtoLKs(result);
    }

    Set<Linkey> execute(File source, File target, File result, Correspondence c) throws IOException, ParserConfigurationException, SAXException {
        if(!result.exists()) {
            result=new File(result.getPath());
            result.createNewFile();
        }
        StringBuilder arguments = new StringBuilder();
        arguments.append("java -jar ").append(linkexPath).append(" ");
        arguments.append("-d 0.4 -s 0.4 -i -c -o output/").append(result.getName()).append(" ");
      //  System.out.println( c.getC1().toString().substring(7, c.getC1().toString().length() - 1).replace("_", "#"));
      //  System.out.println(c.getC2().toString().substring(7, c.getC2().toString().length() - 1).replace("_", "#"));
        arguments.append("-t eq -f edoal -c1 ").append(c.getC1().toString().substring(7, c.getC1().toString().length() - 1).replace("_", "#")).append(" ");
        arguments.append("-c2 ").append(c.getC2().toString().substring(7, c.getC2().toString().length() - 1).replace("_", "#")).append(" ");
        arguments.append(source).append(" ").append(target);

        try {
            Run.runProcess(arguments.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ParseEdoal.EDOALtoLKs(result);
    }

}
