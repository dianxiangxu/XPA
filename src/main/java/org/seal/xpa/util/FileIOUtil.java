package org.seal.xpa.util;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.List;

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
}
