package org.seal.xacml;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.seal.xacml.helpers.Z3StrExpressionHelper;
import org.seal.xacml.policyUtils.PolicyLoader;
import org.seal.xacml.utils.XMLUtil;
import org.w3c.dom.Document;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.ParsingException;
import org.wso2.balana.PolicyMetaData;
import org.xml.sax.SAXException;

public class RequestGeneratorBase {
	protected String policyFilePath;
	protected Document doc;
	protected PolicyMetaData policyMetaData;
	protected List<String> requests;
	protected Z3StrExpressionHelper z3ExpressionHelper;
	protected String currentPolicyRCA;
	protected short falsifyRulesFlag; // 0 - all
									  // 1 - permit // 2 - deny
	
    protected  void init(String path) throws IOException, SAXException, ParserConfigurationException, ParsingException{
		policyFilePath = path;
		doc = PolicyLoader.getDocument(new FileInputStream(policyFilePath));
		AbstractPolicy p = PolicyLoader.loadPolicy(doc);
		PolicyMetaData policyMetaDataTmp = p.getMetaData();
		String docStr = XMLUtil.nodeToString(doc);
		int xpathVersion = policyMetaDataTmp.getXPathVersion();
       	if(docStr.contains("http://www.w3.org/TR/1999/REC-xpath-19991116")) {
       		xpathVersion = 1;
       	}
       	policyMetaData = new PolicyMetaData(policyMetaDataTmp.getXACMLVersion(), xpathVersion);
	    requests = new ArrayList<String>();
		z3ExpressionHelper = new Z3StrExpressionHelper();
		falsifyRulesFlag = 0;
		currentPolicyRCA = p.getCombiningAlg().getIdentifier().toString();
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
    
    public void removeLastRequest(){
    	 requests.set(requests.size()-1, null);
    }
    
    public  List<String> generateTests() throws ParsingException, IOException, SAXException, ParserConfigurationException{
    	return null;
    	
    	
    }
    
	public List<String> generateTests(boolean permitDeny) throws ParsingException, IOException, SAXException, ParserConfigurationException{
		return null;
	}
	
	public List<TaggedRequest> generateRequests(List<String> mutationMethods) throws IOException, ParserConfigurationException, ParsingException, SAXException, InvocationTargetException, IllegalAccessException, NoSuchMethodException{
		return null;
	}
	
}
