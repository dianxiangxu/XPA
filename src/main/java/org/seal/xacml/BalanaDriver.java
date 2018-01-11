package org.seal.xacml;

import org.wso2.balana.Balana;
import org.wso2.balana.ConfigurationStore;

public class BalanaDriver {
	private static Balana balana;
	
	public static void initBalana(String resourcesPath) {
		try {
			System.setProperty(ConfigurationStore.PDP_CONFIG_PROPERTY, resourcesPath);
			if(balana != null) {
				balana = Balana.getInstance();
			}
		} catch (Exception e) {
			System.err.println("Can not locate policy repository");
		}
	}
}
