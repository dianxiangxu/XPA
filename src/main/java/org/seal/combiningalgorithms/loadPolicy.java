package org.seal.combiningalgorithms;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.balana.DOMHelper;
import org.wso2.balana.ParsingException;
import org.wso2.balana.Policy;
import org.wso2.balana.ctx.AbstractRequestCtx;
import org.wso2.balana.ctx.RequestCtxFactory;
import org.wso2.balana.ctx.xacml3.RequestCtx;
import org.wso2.balana.ctx.xacml3.XACML3EvaluationCtx;

public class loadPolicy {
	public Policy getPolicy(String policyFile) {

		Policy policy = null;
		InputStream stream = null;
		
		try
		{
			stream = new FileInputStream(policyFile);
		}
		catch(Exception e)
		{
			return policy;
		}

		try {
			// create the factory
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setIgnoringComments(true);
			factory.setNamespaceAware(true);
			factory.setValidating(false);

			// create a builder based on the factory & try to load the policy
			DocumentBuilder db = factory.newDocumentBuilder();
			// Test
				//System.out.println(policyFile);
			Document doc = db.parse(stream);

			// handle the policy, if it's a known type
			Element root = doc.getDocumentElement();
			String name = DOMHelper.getLocalName(root);

			if (name.equals("Policy")) {
				policy = Policy.getInstance(root);
			}
		} catch (Exception e) {
			//e.printStackTrace();
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
				}
			}
		}
		return policy;
	}

	
	public int PolicyEvaluate(Policy policy, String request) {
		RequestCtxFactory rc = new RequestCtxFactory();
		AbstractRequestCtx ar = null;
		try {
			ar = rc.getRequestCtx(request);
		} catch (ParsingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		XACML3EvaluationCtx ec;

		ec = new XACML3EvaluationCtx(new RequestCtx(ar.getAttributesSet(),
				ar.getDocumentRoot()), ReadPolicy.getPDPconfig());
		// System.out.print(request);
		return policy.evaluate(ec).getDecision();
	}
//	public static void main(String args[]) {
//		loadPolicy lp = new loadPolicy();
//		PolicyX policyx = new PolicyX(
//				lp.getPolicy("//home//nshen//xpa//branch//XPA//resources//pluto3.xml"));
//		try {
//			Vector<Vector<Object>> result = policyx
//					.generateRequestForDifferenceRCAs();
//			System.out.println(result.size() + "size");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
