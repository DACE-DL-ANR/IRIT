package org.example;

import fr.inrialpes.exmo.align.impl.edoal.EDOALAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;


public class ParseEdoal {

    File edoal;
    private static OWLDataFactory factory = new OWLDataFactoryImpl();


    public File getEdoal() {
        return edoal;
    }

    public void setEdoal(File edoal) {
        this.edoal = edoal;
    }

    /*   private Alignment callParser(RDFParser p, Object o ) throws AlignmentException {

          if ( o instanceof URI) return p.parse( ((URI)o).toString() );
           if ( o instanceof String ) return p.parse( new ByteArrayInputStream( ((String)o).getBytes() ) );
            if ( o instanceof Reader) return p.parse((Reader)o);
            if ( o instanceof InputStream) return p.parse((InputStream)o);
            throw new AlignmentException( "AlignmentParser: RDFParser cannot parse :"+o );
       }*/
 /*  public Alignment parseString( String s ) throws AlignmentException {
       // The problem here is that InputStream are consumed by parsers
       // So they must be opened again! Like Readers...
       RDFParser r=new RDFParser();
        callParser( r,s );

       return alignment;
    }*/
    public Set<Linkey> EDOALtoLKs(File f) {
        PrefixManager manager = new DefaultPrefixManager("file:" + f.getAbsolutePath().substring(0, f.getAbsolutePath().lastIndexOf(f.getName())));
        System.out.println("Parsing the link keys");
        Set<Linkey> linkeys = new HashSet<>();

        try {
            Scanner myReader = new Scanner(f);
            String s;
            while (myReader.hasNext()) {
                s = myReader.next();
                if (s.startsWith("<Cell")) {

                    myReader.nextLine();
                    myReader.nextLine();
                    myReader.next();
                    s = myReader.next();

                    OWLClassExpression class_1, class_2;
                    ConceptPair PairOfConcepts = new ConceptPair();
                    Set<PropertyPair> propertySet = new HashSet<>();

                    if (s.startsWith("rdf:about")) {
                        int st = s.indexOf("\"");
                        int e = s.lastIndexOf("\"");
                        s = s.substring(st + 1, e);

                        class_1 = factory.getOWLClass("<" + s + ">", manager);
                        //   System.out.println("First class " + class_1);
                        PairOfConcepts.setFirstConcept(class_1);
                    }
                    myReader.nextLine();
                    myReader.next();
                    myReader.next();
                    myReader.next();
                    s = myReader.next();

                    if (s.startsWith("rdf:about")) {
                        int st = s.indexOf("\"");
                        int e = s.lastIndexOf("\"");
                        s = s.substring(st + 1, e);

                        class_2 = factory.getOWLClass("<" + s + ">", manager);

                        //  System.out.println("Second class " + class_2);
                        PairOfConcepts.setSecondConcept(class_2);

                    }


                    myReader.nextLine();
                    myReader.nextLine();
                    myReader.nextLine();
                    myReader.nextLine();
                    myReader.nextLine();
                    myReader.nextLine();
                    myReader.nextLine();
                    s = myReader.next();

                    while (!s.endsWith(" </edoal:Intersects>") && !s.endsWith("</edoal:linkkey>") && myReader.hasNext()) {
                        if (s.startsWith("<edoal:Intersects")) {

                            PropertyPair pp = new PropertyPair();
                            myReader.next();
                            myReader.next();
                            s = myReader.next();

                            if (s.startsWith("rdf:about")) {

                                int st = s.indexOf("\"");
                                int e = s.lastIndexOf("\"");
                                s = s.substring(st + 1, e);

                                OWLPropertyExpression p1 = factory.getOWLDataProperty("<" + s + ">", manager);
                                //   System.out.println("The first property: " +p1);

                                pp.setFirstProperty(p1);
                            }

                            myReader.next();
                            myReader.next();
                            myReader.next();
                            s = myReader.next();
                            //  System.out.println(s);
                            if (s.startsWith("rdf:about")) {
                                int st = s.indexOf("\"");
                                int e = s.lastIndexOf("\"");
                                s = s.substring(st + 1, e);

                                OWLPropertyExpression p2 = factory.getOWLDataProperty("<" + s + ">", manager);
                                //    System.out.println("The second property: " +p2);
                                pp.setSecondProperty(p2);
                            }

                            propertySet.add(pp);
                        }

                        myReader.nextLine();
                        myReader.nextLine();
                        myReader.nextLine();
                        myReader.nextLine();
                        myReader.nextLine();
                        myReader.nextLine();
                        myReader.nextLine();
                        s = myReader.next();

                    }

                    Linkey lk = new Linkey(PairOfConcepts, propertySet);
                    lk.setPairsOfConcepts(PairOfConcepts);
                    lk.setPropertySet(propertySet);
                    linkeys.add(lk);
                }


            }
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        return linkeys;

    }

    public Set<Correspondance> EDOALtoCCs(File f) {
        Set<Correspondance> ccs=new HashSet<>();
        AlignmentParser parser = new AlignmentParser();
        Alignment alignment;

        {
            try {
                alignment = parser.parseString(Files.readString(Paths.get("th_0_7_2.edoal")));
            } catch (AlignmentException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        alignment.forEach(cell -> {
            Correspondance c=new Correspondance();

            try {

                if(cell.getObject1AsURI()!=null&&cell.getObject2AsURI()!=null) {

                        c.setC1(factory.getOWLClass(IRI.create(cell.getObject1AsURI())));
                        c.setC2(factory.getOWLClass(IRI.create(cell.getObject2AsURI())));


                }
                //System.out.println("semantics "+cell.getSemantics());
               // System.out.println("relation "+cell.getRelation());
               // System.out.println("extension "+cell.getExtensions());
            } catch (AlignmentException e) {
                throw new RuntimeException(e);
            }
            c.setValueOfCon(cell.getStrength());
            ccs.add(c);
        });
   //  System.out.println("number of correspondances"+ ccs.size());
     return ccs;
    }
}



