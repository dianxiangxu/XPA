package org.seal.xacml.utils;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

public class PropertiesLoader {
	private static HashMap<String,Properties> properties = new HashMap<String,Properties>();
	public static Properties getProperties(String name){
		Properties props = properties.get(name);
		if(props!=null){
			return props;
		} else{
			String resourceName = name + ".properties"; 
			ClassLoader loader = Thread.currentThread().getContextClassLoader();
			props = new Properties();
			try(InputStream resourceStream = loader.getResourceAsStream(resourceName)) {
			    props.load(resourceStream);
			}catch(Exception e){
				e.printStackTrace();
			}
			properties.put(name, props);
			return props;
		}
	}
}
