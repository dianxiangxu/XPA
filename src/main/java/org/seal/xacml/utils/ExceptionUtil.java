package org.seal.xacml.utils;

import org.apache.log4j.Logger;

public class ExceptionUtil {
	private static org.apache.log4j.Logger log = Logger.getLogger(ExceptionUtil.class);
	public static void handleInDefaultLevel(Exception e){
		if(PropertiesLoader.getProperties("config").get("environment").equals("development")){
    		e.printStackTrace();
    	}else{
    		log.error(e.getMessage());
    	}
	}
}
