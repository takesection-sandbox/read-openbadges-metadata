package com.pigumer.logic;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataFormatImpl;
import javax.imageio.stream.ImageInputStream;
import java.io.InputStream;
import java.util.Iterator;

public class Logic {

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
        System.out.println(keyword + ": " + value);
        if (keyword.equals("openbadges")) {
            return value;
        }
        return null;
    }

    public String analyze(InputStream in) throws Exception {
        // inのイメージファイルのメタデータを読み取る
        String info = null;
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
                            info = json;
                        }
                    }
                }
            }
        }
        return info;
    }
}
