package com.original.webcrawler;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by vasilenko on 7/13/2018.
 */
public class WebFinder {
    protected static boolean marker = false;
    protected static NamedNodeMap attributes;
    protected static List<Object[]> allExamNodes = new ArrayList<Object[]>(0);

    public static void main(String[] in) {
	//String idKey = "make-everything-ok-button";
	String idKey = in[0];
	String sampleFile = in[1];
	String targetFile = in[2];


	try {
	    learn(getXMLFromFile(sampleFile), idKey);
	    findSomeResult(getXMLFromFile(targetFile));
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (SAXException e) {
	    e.printStackTrace();
	} catch (ParserConfigurationException e) {
	    e.printStackTrace();
	}
    }

    public static void learn(Document document, String idKey)
		    throws IOException, SAXException, ParserConfigurationException {
	NodeList nlist = document.getChildNodes();
	if (nlist != null)
	    getNodesOrNull(idKey, nlist, "");
    }

    protected static NodeList getNodesOrNull(String idKey, NodeList nlist, String spaceAppender) {
	NodeList result = null;

	for (int i = 0; i < nlist.getLength(); i++) {
	    if (marker)
		return result;
	    if (nlist.item(i).getNodeType() == Node.ELEMENT_NODE) {
		Element el = (Element) nlist.item(i);
		//System.out.println(spaceAppender + el.getNodeName());
		if (el.getAttribute("id") != null && el.getAttribute("id").equals(idKey)) {
		    attributes = el.getAttributes();
		    marker = true;
		    return null;
		}
		NodeList childNodes = nlist.item(i).getChildNodes();
		if (childNodes != null) {
		    getNodesOrNull(idKey, childNodes, spaceAppender + "	");
		}
	    }
	}
	return result;
    }

    public static Document getXMLFromFile(String filePath)
		    throws IOException, ParserConfigurationException, SAXException {
	byte[] encoded = Files.readAllBytes(Paths.get(filePath));
	String xml = new String(encoded, "UTF-8");
	DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
	DocumentBuilder newDocumentBuilder = documentBuilderFactory.newDocumentBuilder();
	ByteArrayInputStream bis = new ByteArrayInputStream(xml.getBytes());
	return newDocumentBuilder.parse(bis);
    }

    public static void findSomeResult(Document document) {
	NodeList nlist = document.getChildNodes();
	if (nlist != null)
	    checkAllNodes(nlist);
	Object[] result = null;
	for (Object[] o : allExamNodes) {
	    if (result == null)
		result = o;
	    if ((Integer) o[0] > (Integer) result[0])
		result = o;
	}
	System.out.println("Result : " + result[1] + " :: " + result[2]);
    }

    public static NodeList checkAllNodes(NodeList nlist) {
	NodeList result = null;

	for (int i = 0; i < nlist.getLength(); i++) {
	    if (nlist.item(i).getNodeType() == Node.ELEMENT_NODE) {
		int factor = 0;
		String tagItem = "";
		Element el = (Element) nlist.item(i);

		for (int j = 0; j < attributes.getLength(); j++) {
		    String nodeName = attributes.item(j).getNodeName();
		    if (el.getAttribute(nodeName) != null) {
			factor++;
			if (el.getAttribute(nodeName)
					.equalsIgnoreCase(attributes.getNamedItem(nodeName).getNodeValue())) {
			    factor++;
			}
			tagItem = tagItem + " " + nodeName + "=\"" + el.getAttribute(nodeName) + "\"";
		    }
		}
		Object item[] = new Object[3];
		item[0] = factor;
		Node pn = el.getParentNode();
		List<String> spath = new ArrayList<String>(0);
		while (pn.getParentNode() != null) {
		    spath.add(pn.getNodeName());
		    pn = pn.getParentNode();
		}
		String path = "";
		for (int z = spath.size(); z > 1; z--) {
		    path += " > " + spath.get(z - 1);
		}
		path += " > " + el.getNodeName();
		item[1] = path;
		item[2] = tagItem;
		allExamNodes.add(item);
		NodeList childNodes = nlist.item(i).getChildNodes();
		if (childNodes != null) {
		    checkAllNodes(childNodes);
		}
	    }
	}
	return result;
    }
}
