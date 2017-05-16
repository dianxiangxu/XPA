package org.seal.xpa.util;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;

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

import org.apache.commons.io.IOUtils;
import org.seal.policyUtils.PolicyLoader;
import org.seal.semanticMutation.Mutant;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.balana.AbstractPolicy;
import org.xml.sax.InputSource;

public class FileIOUtil {
	
	public static void writeWithNumberSuffix(List<String> contents,String path,String suffix,String extension){
		String fileName;
		for(int i = 0; i< contents.size();i++){
			try{
				fileName = path;
				if(suffix != null){
					fileName += suffix ;
				} 
				fileName += (i+1)+extension;
				FileWriter fw = new FileWriter(fileName);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(contents.get(i));
				bw.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public static void saveMutant(Mutant mutant, String mutantsFolder){
		String fileName;
		try{
			fileName = mutantsFolder + File.separator + mutant.getName()+ "." + PropertiesLoader.getProperties("config").getProperty("mutantFileExtension") ;
			FileWriter fw = new FileWriter(fileName);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(mutant.encode());
			bw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public static void writeFile(File file,String content){
		try{
			FileWriter fw = new FileWriter(file);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.close();
		}catch(Exception e){
			e.printStackTrace();
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
	
	}
