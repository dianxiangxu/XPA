package org.seal.xpa.util;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XMLUtil {
	public static String nodeToString(Node node) {
		  StringWriter sw = new StringWriter();
		  try {
		    Transformer t = TransformerFactory.newInstance().newTransformer();
		    t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		    t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

	    t.transform(new DOMSource(node), new StreamResult(sw));
	  } catch (TransformerException te) {
	    System.out.println("nodeToString Transformer Exception");
	  }
	  return sw.toString();
	}
	
	public static String toPrettyString(String xml, int indent) {
	    try {
	        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(new ByteArrayInputStream(xml.getBytes("utf-8"))));
	        document.normalize();
	        XPath xPath = XPathFactory.newInstance().newXPath();
	        NodeList nodeList = (NodeList) xPath.evaluate("//text()[normalize-space()='']",document,XPathConstants.NODESET);
	        for (int i = 0; i < nodeList.getLength(); ++i) {
	            Node node = nodeList.item(i);
	            node.getParentNode().removeChild(node);
	        }
	        TransformerFactory transformerFactory = TransformerFactory.newInstance();
	        transformerFactory.setAttribute("indent-number", indent);
	        Transformer transformer = transformerFactory.newTransformer();
	        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
	        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        StringWriter stringWriter = new StringWriter();
	        transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
	        return stringWriter.toString();
	    } catch (Exception e) {
	        throw new RuntimeException(e);
	    }
	}

}
