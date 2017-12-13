package org.seal.xacml.utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.seal.xacml.semanticMutation.Mutant;

public class FileIOUtil {
	static private String encoding;
	
	static {
		encoding = PropertiesLoader.getProperties("config").getProperty("encoding");
	}

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
	
	public static void saveMutant(Mutant mutant, String mutantsFolder) throws IOException{
		String fileName = mutantsFolder + File.separator + mutant.getName()+ "." + PropertiesLoader.getProperties("config").getProperty("mutantFileExtension");
		FileUtils.writeStringToFile(new File(fileName), mutant.encode(), Charset.forName(encoding));
	}
	
	public static void writeFile(File file,String content) throws IOException{
		FileUtils.writeStringToFile(file, content, Charset.forName(encoding));
	}
	
	public static void writeFile(String filePath,String content) throws IOException{
		writeFile(new File(filePath),content);
	}
	
	public static String readFile(File file) throws IOException{
		return FileUtils.readFileToString(file,Charset.forName(encoding));
	}
	
	public static String readFile(String filePath) throws IOException{
		return readFile(new File(filePath));
	}
}
