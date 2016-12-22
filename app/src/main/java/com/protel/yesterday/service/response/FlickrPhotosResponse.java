package com.protel.yesterday.service.response;

import android.text.TextUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Created by erdemmac on 22/12/2016.
 */

public class FlickrPhotosResponse implements Serializable {

    public List<PhotoItem> photos;

    public static class PhotoItem implements Serializable {
        public String owner;
        public String id, server;
        public String farm, secret;
    }

    public void parse(String data) {
        photos = new ArrayList<>();
        if (TextUtils.isEmpty(data)) return;
        InputStream inputStream = new ByteArrayInputStream(data.getBytes(Charset.defaultCharset()));
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder;
            dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputStream);
            Element element = doc.getDocumentElement();
            element.normalize();

            NodeList photoNodeList = doc.getElementsByTagName("photos");

            for (int i = 0; i < photoNodeList.getLength(); i++) {
                Node node = photoNodeList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element2 = (Element) node;
                    NodeList nListPhotoChilds = element2.getElementsByTagName("photo");
                    for (int j = 0; j < nListPhotoChilds.getLength(); j++) {
                        Node nodePhoto = nListPhotoChilds.item(j);
                        if (nodePhoto.getNodeType() == Node.ELEMENT_NODE) {
                            FlickrPhotosResponse.PhotoItem photoItem = new FlickrPhotosResponse.PhotoItem();
                            Element elementPhoto = (Element) nodePhoto;
                            photoItem.owner = elementPhoto.getAttribute("owner");
                            photoItem.farm = elementPhoto.getAttribute("farm");
                            photoItem.id = elementPhoto.getAttribute("id");
                            photoItem.secret = elementPhoto.getAttribute("secret");
                            photoItem.server = elementPhoto.getAttribute("server");
                            photos.add(photoItem);
                        }
                    }
                }
            }

        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


