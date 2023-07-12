package org.example;
import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class Filter {
    public static void main(String[] args) {
        String filePath = "output/atmr5.rdf2";

        try {
            File file = new File(filePath);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(file);

            NodeList mapList = document.getElementsByTagName("map");
            int counter=0;
            for (int i = 0; i < mapList.getLength(); i++) {
                NodeList cellList = mapList.item(i).getChildNodes();
                for (int j = 0; j < cellList.getLength(); j++) {
                    if (cellList.item(j).getNodeName().equals("Cell")) {
                        NodeList entityList = cellList.item(j).getChildNodes();
                        boolean removeCell = false;
                        for (int k = 0; k < entityList.getLength(); k++) {
                            if (entityList.item(k).getNodeName().startsWith("entity")) {
                                String entityValue = entityList.item(k).getAttributes().getNamedItem("rdf:resource").getNodeValue();
                                // System.out.println(entityValue);
                                if (entityValue.contains(".jpg")||entityValue.contains(".png")||entityValue.contains(".svg")) {

                                    counter++;
                                    removeCell = true;
                                    break;
                                }
                            }
                        }

                            if (removeCell) {
                                mapList.item(i).getParentNode().removeChild(mapList.item(i));
                            }
                        }
                    }

            }

            // TODO: Save the modified document back to the file

            System.out.println("Cells with specific entities removed successfully.");
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(filePath));
            transformer.transform(source, result);

            System.out.println("Modified XML file saved successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

