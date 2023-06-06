package org.example;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AlignmentOWL {

    private OWLObject entity1, entity2;
    private String relation;
    private float measure;

    public AlignmentOWL(OWLObject entity1, OWLObject entity2, String relation, float measure) {
        this.entity1 = entity1;
        this.entity2 = entity2;
        this.relation = relation;
        this.measure = measure;
    }

    private static OWLOntologyManager manager;
    private static OWLDataFactory dataFactory;


    public static Set<AlignmentOWL> parseAlignment(File path) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = builder.parse(path);
        doc.getDocumentElement().normalize();
        Node item = doc.getDocumentElement().getChildNodes().item(1);
        Set<AlignmentOWL> alignments = new HashSet<>();

        manager = OWLManager.createOWLOntologyManager();
        dataFactory = manager.getOWLDataFactory();

        for (int i = 0; i < item.getChildNodes().getLength(); i++) {
            if (!item.getChildNodes().item(i).getNodeName().equals("map")) continue;
            Node item1 = item.getChildNodes().item(i).getChildNodes().item(1);

            OWLObject entity1 = null;
            OWLObject entity2 = null;
            String relation = null;
            float measure = 0;
            for (int i1 = 0; i1 < item1.getChildNodes().getLength(); i1++) {
                Node item2 = item1.getChildNodes().item(i1);
                switch (item2.getNodeName()) {
                    case "entity1" -> entity1 = parseEntity(item2);
                    case "entity2" -> entity2 = parseEntity(item2);
                    case "relation" -> relation = item2.getChildNodes().item(0).getNodeValue();
                    case "measure" -> measure = Float.parseFloat(item2.getChildNodes().item(0).getNodeValue());

                }

            }

            alignments.add(new AlignmentOWL(entity1, entity2, relation, measure));

        }
        manager = null;
        dataFactory = null;
        return alignments;
    }

    private static OWLObject parseEntity(Node node) {
        OWLObject object = null;
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node item = node.getChildNodes().item(i);
            switch (item.getNodeName()) {
                case "edoal:Class" -> object = parseClassExpression(item);
                case "edoal:Relation" -> object = parseRelationExpression(item);
                case "edoal:AttributeDomainRestriction" -> object = parseAttributeDomainRestriction(item);
                case "edoal:AttributeOccurenceRestriction" -> object = parseAttributeOccurrenceRestriction(item);
            }

        }

        return object;
    }

    private static OWLObjectCardinalityRestriction parseAttributeOccurrenceRestriction(Node node) {


        OWLObjectPropertyExpression property = null;
        String comparator = null;
        Integer value = null;

        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            if (node.getChildNodes().item(i).getNodeName().equals("#text")) continue;

            switch (node.getChildNodes().item(i).getNodeName()) {
                case "edoal:onAttribute" -> property = parseAttribute(node.getChildNodes().item(i));
                case "edoal:comparator" -> comparator = parseComparator(node.getChildNodes().item(i));
                case "edoal:value" -> value = parseValue(node.getChildNodes().item(i));
            }
        }


        if (comparator.equals("greater-than")) {
            return dataFactory.getOWLObjectMinCardinality(value, property);
        }


        return null;
    }

    private static int parseValue(Node node) {
        return Integer.parseInt(node.getTextContent());
    }

    private static String parseComparator(Node node) {
        return node.getAttributes().item(0).getNodeValue().split("#")[1];
    }


    private static OWLNamedIndividual parseInstanceExpression(Node node) {
        OWLNamedIndividual individual = null;
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node item = node.getChildNodes().item(i);
            switch (item.getNodeName()) {
                case "edoal:Instance" -> individual = parseInstance(item);
            }

        }

        return individual;
    }

    private static OWLNamedIndividual parseInstance(Node node) {
        IRI iri = IRI.create(node.getAttributes().item(0).getNodeValue());
        return dataFactory.getOWLNamedIndividual(iri);
    }


    private static OWLClassExpression parseClassExpression(Node node) {

        switch (node.getNodeName()) {
            case "edoal:AttributeValueRestriction" -> {

                return parseAttributeValueRestriction(node);
            }
        }


        if (node.getChildNodes().getLength() == 0) {
            return parseClass(node);
        }

        OWLClassExpression expression = null;

        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node item = node.getChildNodes().item(i);

            switch (item.getNodeName()) {
                case "edoal:and" -> expression = parseClassAnd(item);
            }

        }

        return expression;
    }

    private static OWLObjectHasValue parseAttributeValueRestriction(Node node) {
        OWLObjectPropertyExpression property = null;
        String comparator = null;
        OWLNamedIndividual individual = null;

        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            if (node.getChildNodes().item(i).getNodeName().equals("#text")) continue;

            switch (node.getChildNodes().item(i).getNodeName()) {
                case "edoal:onAttribute" -> property = parseAttribute(node.getChildNodes().item(i));
                case "edoal:comparator" -> comparator = parseComparator(node.getChildNodes().item(i));
                case "edoal:value" -> individual = parseInstanceExpression(node.getChildNodes().item(i));
            }
        }


        if (comparator.equals("equals")) {
            return dataFactory.getOWLObjectHasValue(property, individual);
        }


        return null;
    }

    private static OWLObjectIntersectionOf parseClassAnd(Node node) {

        Set<OWLClassExpression> expressions = new HashSet<>();

        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node item = node.getChildNodes().item(i);
            if (item.getNodeName().equals("#text")) continue;

            expressions.add(parseClassExpression(item));
        }

        return dataFactory.getOWLObjectIntersectionOf(expressions);
    }

    private static OWLClass parseClass(Node node) {
        IRI iri = IRI.create(node.getAttributes().item(0).getNodeValue());
        return dataFactory.getOWLClass(iri);
    }

    private void parsePropertyExpression(Node node) {
        System.out.println(node.getNodeName());
    }

    private static OWLObject parseRelationExpression(Node node) {

        switch (node.getNodeName()) {
            case "edoal:RelationDomainRestriction" -> {
                return parseRelationDomainRestriction(node);
            }
            case "edoal:RelationCoDomainRestriction" -> {
                return parseRelationCoDomainRestriction(node);
            }
        }

        if (node.getChildNodes().getLength() == 0) {
            if (node.getAttributes().getLength() == 0) {
                //TODO IMPORTANT!!

                return null;
            }

            return parseRelation(node);
        }

        OWLObject expression = null;

        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node item = node.getChildNodes().item(i);

            switch (item.getNodeName()) {
                case "edoal:compose" -> expression = parseCompose(item);
                case "edoal:and" -> expression = parseRelationAnd(item);
                case "edoal:inverse" -> expression = parseInverseRelation(item);
            }

        }

        return expression;
    }

    private static OWLObject parseRelationCoDomainRestriction(Node node) {
        return null;
    }

    private static OWLObject parseRelationDomainRestriction(Node node) {
        return null;
    }

    private static OWLObjectInverseOf parseInverseRelation(Node node) {

        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node item = node.getChildNodes().item(i);
            if (item.getNodeName().equals("#text")) continue;
            OWLObject owlObjectProperty = parseRelationExpression(item);
            return dataFactory.getOWLObjectInverseOf((OWLObjectProperty) owlObjectProperty);
        }

        return null;
    }

    private static OWLSubPropertyChainOfAxiom parseRelationAnd(Node node) {
        List<OWLObjectPropertyExpression> expressions = new ArrayList<>();
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node item = node.getChildNodes().item(i);
            if (item.getNodeName().equals("#text")) continue;
            expressions.add((OWLObjectPropertyExpression) parseRelationExpression(item));
        }

        OWLObjectPropertyExpression last = expressions.get(expressions.size() - 1);
        expressions.remove(expressions.size() - 1);

        return dataFactory.getOWLSubPropertyChainOfAxiom(expressions, last);
    }

    private static OWLObject parseCompose(Node node) {

        return dataFactory.getOWLObjectProperty(IRI.create("http://www.w3.org/2002/07/owl#topObjectProperty"));
    }

    private static OWLObjectProperty parseRelation(Node node) {
        IRI iri = IRI.create(node.getAttributes().item(0).getNodeValue());
        return dataFactory.getOWLObjectProperty(iri);
    }

    private static OWLObjectPropertyDomainAxiom parseAttributeDomainRestriction(Node node) {
        OWLObjectPropertyExpression property = null;
        OWLClassExpression expression = null;
        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node item = node.getChildNodes().item(i);

            switch (item.getNodeName()) {
                case "edoal:onAttribute" -> property = parseAttribute(item);
                case "edoal:exists" -> expression = parseExists(item);
            }

        }

        return dataFactory.getOWLObjectPropertyDomainAxiom(property, expression);

    }

    private static OWLClassExpression parseExists(Node node) {

        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node item = node.getChildNodes().item(i);
            if (item.getNodeName().equals("#text")) continue;
            return parseClassExpression(item);
        }

        return null;
    }

    private static OWLObjectPropertyExpression parseAttribute(Node node) {

        for (int i = 0; i < node.getChildNodes().getLength(); i++) {
            Node item = node.getChildNodes().item(i);
            if (item.getNodeName().equals("#text")) continue;
            return (OWLObjectPropertyExpression) parseRelationExpression(item);
        }

        return null;
    }

    public OWLObject getEntity1() {
        return entity1;
    }

    public OWLObject getEntity2() {
        return entity2;
    }

    public String getRelation() {
        return relation;
    }

    public float getMeasure() {
        return measure;
    }
}
