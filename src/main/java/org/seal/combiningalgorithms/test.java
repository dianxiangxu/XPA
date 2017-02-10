/*
 *  Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.seal.combiningalgorithms;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.DOMHelper;
import org.wso2.balana.Policy;
import org.wso2.balana.Balana;
import org.wso2.balana.PDP;
import org.wso2.balana.PDPConfig;
import org.wso2.balana.finder.PolicyFinder;
import org.wso2.balana.finder.PolicyFinderModule;
import org.wso2.balana.finder.impl.FileBasedPolicyFinderModule;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class test {

	public static PDPConfig pdpConfig;

	public Vector<Vector<Object>> getResult() throws IOException {
		System.out.println("start");
		initBalana();
		System.out.println("initFinish");
		Vector<Vector<Object>> result = new Vector<Vector<Object>>();
		// XU, 5-26-2014
		PolicyFinder policyFinder = pdpConfig.getPolicyFinder();
		Iterator it = policyFinder.getModules().iterator();
		System.out.println("Starts out while ");
		while (it.hasNext()) {
			System.out.println("Finish here");
			PolicyFinderModule module = (PolicyFinderModule) (it.next());
			if (module instanceof FileBasedPolicyFinderModule) {
				Map<URI, AbstractPolicy> policies = ((FileBasedPolicyFinderModule) module)
						.getPolicies();
				Set<Map.Entry<URI, AbstractPolicy>> entrySet = policies
						.entrySet();
				for (Map.Entry<URI, AbstractPolicy> entry : entrySet) {
					AbstractPolicy policy = entry.getValue();
					if (policy instanceof Policy) {
						System.out.println("Finish here" + result.size());
						PolicyX policyX = new PolicyX((Policy) policy);
						result = policyX
								.generateRequestForDifferenceRCAs();

						return result;
						//System.out.println(result.size() + "size");
//						System.out.println(result.get(0).get(1) + ""
//								+ result.get(0).get(3) + ""
//								+ result.get(1).get(3) + ""
//								+ result.get(2).get(3) + ""
//								+ result.get(3).size());
					}
				}

			}
		}
		return null;
		// XU, 5-26-2014
	}

	private void initBalana() {
		String policyLocation = "//home//nshen//xpa//branch//XPA//resources//pluto3.xml";
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

	
//	public static void main(String args[]) {
//		test t = new test();
//		try {
//			Vector<Vector<Object>> result = t.getResult();
//			//System.out.println(result.size() + "test size");
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

}