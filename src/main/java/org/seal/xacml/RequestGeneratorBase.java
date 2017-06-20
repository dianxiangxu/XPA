package org.seal.xacml;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.seal.policyUtils.PolicyLoader;
import org.seal.xacml.helpers.Z3StrExpressionHelper;
import org.w3c.dom.Document;
import org.wso2.balana.ParsingException;
import org.wso2.balana.PolicyMetaData;
import org.xml.sax.SAXException;

public class RequestGeneratorBase {
	protected String policyFilePath;
	protected Document doc;
	protected PolicyMetaData policyMetaData;
	private List<String> requests;
	protected Z3StrExpressionHelper z3ExpressionHelper;
	
    protected  void init(String path) throws IOException, SAXException, ParserConfigurationException, ParsingException{
		policyFilePath = path;
		doc = PolicyLoader.getDocument(new FileInputStream(policyFilePath));
	    policyMetaData = PolicyLoader.loadPolicy(doc).getMetaData();
		requests = new ArrayList<String>();
		z3ExpressionHelper = new Z3StrExpressionHelper();
    }
    
    public void setRequests(List<String> reqs){
    	requests = reqs;
    }
    
    public void addRequest(String req){
    	requests.add(req);
    }
    
    public List<String> getRequests(){
    	return requests;
    }
}
