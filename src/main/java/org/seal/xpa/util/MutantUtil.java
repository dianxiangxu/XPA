package org.seal.xpa.util;

import java.io.File;
import java.util.Arrays;
import java.util.Vector;

import org.seal.semanticMutation.Mutant;

import java.util.List;

public class MutantUtil {
	public static Vector<Vector<Object>> getVectorsForMutants(List<Mutant> mutants){
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		int i = 1;
		for(Mutant mutant:mutants){
			Vector<Object> vector = new Vector<Object>();
			vector.add(i++);		// sequence number
			vector.add(mutant.getName());	// mutant name
			vector.add(mutant.getName() + "." + PropertiesLoader.getProperties("config").getProperty("mutantFileExtension"));
			vector.add(Arrays.toString(mutant.getFaultLocations().toArray()));
			vector.add("");	
			data.add(vector);
		}
		return data;
	}
	
	public static Vector<Object> getVector(Mutant mutant, String policyName, String extension){
		Vector<Object> vector = new Vector<Object>();
		vector.add("");		// sequence number
		vector.add(policyName + "_" +mutant.getName());	// mutant name
		vector.add(policyName + "_" +mutant.getName() + extension);
		vector.add(Arrays.toString(mutant.getFaultLocations().toArray()));
		vector.add("");	
		return vector;
	}
	
	public static String getMutantFileName(Mutant mutant){
		return mutant.getName() + "." + PropertiesLoader.getProperties("config").getProperty("mutantFileExtension");
	}
	
	public static String getPolicyName(String mutantsSpreadSheetFile){
		String[] tokens = mutantsSpreadSheetFile.split(File.separator);
		return tokens[tokens.length-1].split("_")[0];
	}
	
	public static File getMutantsFolderForPolicyFile(File policyFile){
		return new File(policyFile.getParent()+File.separator+PropertiesLoader.getProperties("config").getProperty("mutantsFolderName"));
		
	}

}
