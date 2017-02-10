package org.seal.combiningalgorithms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.balana.Balana;
import org.wso2.balana.DOMHelper;
import org.wso2.balana.PDP;
import org.wso2.balana.PDPConfig;
import org.wso2.balana.Policy;
import org.wso2.balana.finder.impl.FileBasedPolicyFinderModule;

public class PolicyEvaluation {

	public PDPConfig pdpConfig;
	String backup; // store

//	public String evaluation(String request) {
//		String result = null;
//		initBalana();
//		// XU, 5-26-2014
//		PolicyFinder policyFinder = pdpConfig.getPolicyFinder();
//		Iterator it = policyFinder.getModules().iterator();
//		while (it.hasNext()) {
//			PolicyFinderModule module = (PolicyFinderModule) (it.next());
//			if (module instanceof FileBasedPolicyFinderModule) {
//				Map<URI, AbstractPolicy> policies = ((FileBasedPolicyFinderModule) module)
//						.getPolicies();
//				Set<Map.Entry<URI, AbstractPolicy>> entrySet = policies
//						.entrySet();
//				for (Map.Entry<URI, AbstractPolicy> entry : entrySet) {
//					AbstractPolicy policy = entry.getValue();
//					if (policy instanceof Policy) {
//						PolicyX policyX = new PolicyX((Policy) policy);
//						result = policyX
//								.PolicyEvaluate(policyX.policy, request) + "";
//
//					}
//				}
//
//			}
//		}
//		return result;
//	}
	
	public String evaluation(String request) {
		PolicyX policyx = new PolicyX(loadPolicy("resources//pluto3.xml"));
		String result = policyx.PolicyEvaluate(policyx.policy, request) + "";
		return result;
	}


	private void initBalana() {
		String policyLocation = "resources//pluto3.xml";
		System.setProperty(FileBasedPolicyFinderModule.POLICY_DIR_PROPERTY,
				policyLocation);
		Balana balana = Balana.getInstance();
		pdpConfig = balana.getPdpConfig();
		new PDP(pdpConfig);
	}

	public PDPConfig getPDPconfig() {
		return pdpConfig;
	}

	public Policy loadPolicy(String policyFile) {

		Policy policy = null;
		InputStream stream = null;

		try {
			// create the factory
			DocumentBuilderFactory factory = DocumentBuilderFactory
					.newInstance();
			factory.setIgnoringComments(true);
			factory.setNamespaceAware(true);
			factory.setValidating(false);

			// create a builder based on the factory & try to load the policy
			DocumentBuilder db = factory.newDocumentBuilder();
			stream = new FileInputStream(policyFile);
			Document doc = db.parse(stream);

			// handle the policy, if it's a known type
			Element root = doc.getDocumentElement();
			String name = DOMHelper.getLocalName(root);

			if (name.equals("Policy")) {
				policy = Policy.getInstance(root);
			}
		} catch (Exception e) {
			e.printStackTrace();
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

	public void modifyAlg(String newAlg) {
		try {
//			File file = new File("//home//nshen//xpa//branch//XPA//GenTests//pluto3.xml");
//			String fName[] = file.list();
//			String name = "";
//			if (fName != null && fName.length > 0)
//				name = fName[0].toString();
			String path = "resources//pluto3.xml";
			FileReader fr = new FileReader(path);
			BufferedReader br = new BufferedReader(fr);
			String strLine;
			StringBuffer sb = new StringBuffer();

			while ((strLine = br.readLine()) != null) {
				if (strLine.contains("rule-combining-algorithm:")) {
					strLine = strLine.replace(
							"urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides",
							newAlg);
					strLine = strLine.replace(
							"urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides",
							newAlg);
					strLine = strLine.replace(
							"urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit",
							newAlg);
					strLine = strLine.replace(
							"urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-unless-deny",
							newAlg);
					strLine = strLine.replace(
							"urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable",
							newAlg);
				}
				sb.append(strLine);
				sb.append("\n");
			}

			FileWriter fw = new FileWriter(path);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(sb.toString());
			bw.close();
			br.close();

		} catch (Exception e) {
			System.err.println("Error: " + e.getMessage());
		}
	}
}
