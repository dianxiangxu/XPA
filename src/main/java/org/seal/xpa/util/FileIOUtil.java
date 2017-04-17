package org.seal.xpa.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import org.seal.semanticMutation.Mutant;

public class FileIOUtil {
	
	public static void writeWithNumberSuffix(List<String> contents,String path,String suffix,String extension){
		String fileName;
		for(int i = 0; i< contents.size();i++){
			try{
				fileName = path;
				if(suffix != null){
					fileName += suffix ;
				} 
				fileName += (i+1)+extension;
				FileWriter fw = new FileWriter(fileName);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(contents.get(i));
				bw.close();
			}catch(Exception e){
				e.printStackTrace();
			}
		}
	}
	
	public static void saveMutant(Mutant mutant, String mutantsFolder){
		String fileName;
		try{
			fileName = mutantsFolder + File.separator + mutant.getName()+ "." + PropertiesLoader.getProperties("config").getProperty("mutantFileExtension") ;
			FileWriter fw = new FileWriter(fileName);
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(mutant.encode());
			bw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
}
