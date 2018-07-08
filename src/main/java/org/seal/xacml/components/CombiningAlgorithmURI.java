package org.seal.xacml.components;

import java.util.Map;
import java.util.HashMap;

public class CombiningAlgorithmURI {
	 public static Map<String,String> map = new HashMap<String,String>();
	 static {
		 
	 map.put("DO", "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides");
	 map.put("PO", "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides");
	 map.put("FA", "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable");
     map.put("ODO", "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-deny-overrides");
     map.put("OPO", "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-permit-overrides");
     map.put("DUP", "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit");
     map.put("PUD", "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-unless-deny");
	 }
	
}
