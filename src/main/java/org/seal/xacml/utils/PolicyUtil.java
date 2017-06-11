package org.seal.xacml.utils;

import java.io.File;

public class PolicyUtil {
	public static String getPolicyName(String path){
		String[] tokens = path.substring(0, path.length() - 4).split(File.separator);
		return tokens[tokens.length-1];
	}
	
	public static String getPolicyName(File file){
		return getPolicyName(file.toString());
	}
}
