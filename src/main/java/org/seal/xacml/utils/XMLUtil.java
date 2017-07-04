package org.seal.xacml.utils;

import static org.seal.policyUtils.XpathSolver.policyPattern;
import static org.seal.policyUtils.XpathSolver.policysetPattern;
import static org.seal.policyUtils.XpathSolver.rulePattern;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.seal.policyUtils.PolicyLoader;
import org.seal.semanticMutation.Mutator;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.balana.AbstractPolicy;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

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
	
	public static String getElementByXPath(String xPathString, File file){
		XPath xPath = XPathFactory.newInstance().newXPath();
		String elementString = "";
		try{
			AbstractPolicy policy = PolicyLoader.loadPolicy(file);
			Document doc = PolicyLoader.getDocument(IOUtils.toInputStream(policy.encode(), Charset.defaultCharset()));
			NodeList nodes = (NodeList) xPath.evaluate(xPathString, doc.getDocumentElement(), XPathConstants.NODESET);
	        for(int i = 0; i< nodes.getLength();i++){
	        	elementString += XMLUtil.toPrettyString(XMLUtil.nodeToString(nodes.item(i)),4);
	        }
		}catch(Exception e){
			e.printStackTrace();
		}
        return elementString;
	}
	
	public static boolean isTraversableElement(Node e){
		if(rulePattern.matcher(e.getLocalName()).matches()||policysetPattern.matcher(e.getLocalName()).matches() || policyPattern.matcher(e.getLocalName()).matches()){
			return true;
		} else{
			return false;
		}
	}
    
	public static Node findInChildNodes(Node parent, String localName) {
	    List<Node> childNodes = Mutator.getChildNodeList(parent);
	    for (Node child : childNodes) {
	        if (localName.equals(child.getLocalName())) {
	            return child;
	        }
	    }
	    return null;
	}
	
	public static boolean isEmptyNode(Node node) {
        if (node == null) {
            return true;
        }
        List<Node> childNodes = getChildNodeList(node);
        for (Node child : childNodes) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                return false;
            }
        }
        return true;
    }
	
	public static List<Node> getChildNodeList(Node parent) {
        List<Node> childNodes = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            childNodes.add(children.item(i));
        }
        return childNodes;
    }
	
	public static Document loadXMLDocumentFromString(String xml) throws ParserConfigurationException, IOException, SAXException{
	    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    factory.setNamespaceAware(true);
	    DocumentBuilder builder = factory.newDocumentBuilder();
	    return builder.parse(new ByteArrayInputStream(xml.getBytes()));
	}

}
