package com.pigumer;

import javax.imageio.*;
import javax.imageio.metadata.*;
import javax.imageio.stream.ImageInputStream;

import java.io.*;
import java.util.*;
import org.w3c.dom.*;

public class App 
{
    private String getOpenBadges(NamedNodeMap textEntries) {
        String keyword = null;
        String value = null;
        for (int i = 0; i < textEntries.getLength(); i++) {
            Node attribute = textEntries.item(i);
            if (attribute.getNodeName().equals("keyword")) {
                keyword = attribute.getNodeValue();
            }
            if (attribute.getNodeName().equals("value")) {
                value = attribute.getNodeValue();
            }
        }
        if (keyword.equals("openbadges")) {
            return value;
        }
        return null;
    }
    
    public String analyze(InputStream in) throws Exception {
        // inのイメージファイルのメタデータを読み取る
        try (ImageInputStream is = ImageIO.createImageInputStream(in)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(is);
            ImageReader reader = readers.next();
            reader.setInput(is);
            IIOImage image = reader.readAll(0, null);
            IIOMetadata meta = image.getMetadata();
            Node root = meta.getAsTree(IIOMetadataFormatImpl.standardMetadataFormatName);
            NodeList nl = root.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node node = nl.item(i);
                if (node.getNodeName().equalsIgnoreCase("text")) {
                    NodeList textNodes = node.getChildNodes();
                    for (int j = 0; j < textNodes.getLength(); j++) {
                        Node item = textNodes.item(j);
                        NamedNodeMap map = item.getAttributes();
                        String json = getOpenBadges(map);
                        if (json != null) {
                            return json;
                        }
                    }
                }
            }
        }
        return null;
    }

    public static void main( String[] args ) throws Exception
    {
        App app = new App();
        System.out.println("Credly:");
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("aws-certified-machine-learning-engineer-associate.png")) {
            String json = app.analyze(in);
            if (json != null) {
                System.out.println(json);
            }
        }
        System.out.println("---");
        System.out.println("Scrum:");
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("sa-csm-600.png")) {
            String json = app.analyze(in);
            if (json != null) {
                System.out.println(json);
            }
        }
    }
}
