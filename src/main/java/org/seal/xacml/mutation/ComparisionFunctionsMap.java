package org.seal.xacml.mutation;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ComparisionFunctionsMap {
	public static Map<String,List<String>> functionListMap;
	static{
		functionListMap = new HashMap<String,List<String>>();
		//string
		functionListMap.put("urn:oasis:names:tc:xacml:1.0:function:string-equal", 
		Arrays.asList(
		"urn:oasis:names:tc:xacml:1.0:function:string-greater-than",
		"urn:oasis:names:tc:xacml:1.0:function:string-less-than"
		));
		
		functionListMap.put("urn:oasis:names:tc:xacml:1.0:function:string-less-than", 
				Arrays.asList(
				"urn:oasis:names:tc:xacml:1.0:function:string-greater-than",
				"urn:oasis:names:tc:xacml:1.0:function:string-equal"
		));
		
		functionListMap.put("urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal", 
				Arrays.asList(
				"urn:oasis:names:tc:xacml:1.0:function:string-greater-than",
				"urn:oasis:names:tc:xacml:1.0:function:string-equal",
				"urn:oasis:names:tc:xacml:1.0:function:string-less-than"
		));
		
		functionListMap.put("urn:oasis:names:tc:xacml:1.0:function:string-greater-than", 
				Arrays.asList(
				"urn:oasis:names:tc:xacml:1.0:function:string-less-than",
				"urn:oasis:names:tc:xacml:1.0:function:string-equal"
		));
		
		functionListMap.put("urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal", 
				Arrays.asList(
				"urn:oasis:names:tc:xacml:1.0:function:string-greater-than",
				"urn:oasis:names:tc:xacml:1.0:function:string-equal",
				"urn:oasis:names:tc:xacml:1.0:function:string-less-than"
		));
		
		//integer
		functionListMap.put("urn:oasis:names:tc:xacml:1.0:function:integer-equal", 
			Arrays.asList(
			"urn:oasis:names:tc:xacml:1.0:function:integer-greater-than",
			"urn:oasis:names:tc:xacml:1.0:function:integer-less-than"
			));
		
		functionListMap.put("urn:oasis:names:tc:xacml:1.0:function:integer-less-than", 
				Arrays.asList(
				"urn:oasis:names:tc:xacml:1.0:function:integer-greater-than",
				"urn:oasis:names:tc:xacml:1.0:function:integer-equal"
		));
		
		functionListMap.put("urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal", 
				Arrays.asList(
				"urn:oasis:names:tc:xacml:1.0:function:integer-greater-than",
				"urn:oasis:names:tc:xacml:1.0:function:integer-equal",
				"urn:oasis:names:tc:xacml:1.0:function:integer-less-than"
		));
		
		functionListMap.put("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than", 
				Arrays.asList(
				"urn:oasis:names:tc:xacml:1.0:function:integer-less-than",
				"urn:oasis:names:tc:xacml:1.0:function:integer-equal"
		));
		
		functionListMap.put("urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal", 
				Arrays.asList(
				"urn:oasis:names:tc:xacml:1.0:function:integer-greater-than",
				"urn:oasis:names:tc:xacml:1.0:function:integer-equal",
				"urn:oasis:names:tc:xacml:1.0:function:integer-less-than"
		));
		//double
		functionListMap.put("urn:oasis:names:tc:xacml:1.0:function:double-equal", 
				Arrays.asList(
				"urn:oasis:names:tc:xacml:1.0:function:double-greater-than",
				"urn:oasis:names:tc:xacml:1.0:function:double-less-than"
				));
			
			functionListMap.put("urn:oasis:names:tc:xacml:1.0:function:double-less-than", 
					Arrays.asList(
					"urn:oasis:names:tc:xacml:1.0:function:double-greater-than",
					"urn:oasis:names:tc:xacml:1.0:function:double-equal"
			));
			
			functionListMap.put("urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal", 
					Arrays.asList(
					"urn:oasis:names:tc:xacml:1.0:function:double-greater-than",
					"urn:oasis:names:tc:xacml:1.0:function:double-equal",
					"urn:oasis:names:tc:xacml:1.0:function:double-less-than"
			));
			
			functionListMap.put("urn:oasis:names:tc:xacml:1.0:function:double-greater-than", 
					Arrays.asList(
					"urn:oasis:names:tc:xacml:1.0:function:double-less-than",
					"urn:oasis:names:tc:xacml:1.0:function:double-equal"
			));
			
			functionListMap.put("urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal", 
					Arrays.asList(
					"urn:oasis:names:tc:xacml:1.0:function:double-greater-than",
					"urn:oasis:names:tc:xacml:1.0:function:double-equal",
					"urn:oasis:names:tc:xacml:1.0:function:double-less-than"
			));

			//date
			functionListMap.put("urn:oasis:names:tc:xacml:1.0:function:date-equal", 
				Arrays.asList(
				"urn:oasis:names:tc:xacml:1.0:function:date-greater-than",
				"urn:oasis:names:tc:xacml:1.0:function:date-less-than"
				));
			
			functionListMap.put("urn:oasis:names:tc:xacml:1.0:function:date-less-than", 
					Arrays.asList(
					"urn:oasis:names:tc:xacml:1.0:function:date-greater-than",
					"urn:oasis:names:tc:xacml:1.0:function:date-equal"
			));
			
			functionListMap.put("urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal", 
					Arrays.asList(
					"urn:oasis:names:tc:xacml:1.0:function:date-greater-than",
					"urn:oasis:names:tc:xacml:1.0:function:date-equal",
					"urn:oasis:names:tc:xacml:1.0:function:date-less-than"
			));
			
			functionListMap.put("urn:oasis:names:tc:xacml:1.0:function:date-greater-than", 
					Arrays.asList(
					"urn:oasis:names:tc:xacml:1.0:function:date-less-than",
					"urn:oasis:names:tc:xacml:1.0:function:date-equal"
			));
			
			functionListMap.put("urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal", 
					Arrays.asList(
					"urn:oasis:names:tc:xacml:1.0:function:date-greater-than",
					"urn:oasis:names:tc:xacml:1.0:function:date-equal",
					"urn:oasis:names:tc:xacml:1.0:function:date-less-than"
			));
	}
	
    
}
