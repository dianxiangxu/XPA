package org.seal.xacml.utils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;

import org.seal.xacml.helpers.Z3StrExpressionHelper;

public class Z3StrUtil {
	private static String XPA_HOME = System.getProperty("user.dir");;
	
	
	public static void buildZ3Input(String input, Map<String,String> nameMap, Map<String,String> typeMap)  throws IOException{
		StringBuffer z3input = new StringBuffer();
		String[] lines = input.split("\n");
		for (String s : lines) {
			if (!s.trim().equals("")) {
				StringBuffer sb = new StringBuffer();
				sb.append(s);
				sb.insert(0, "(assert ");
				sb.append(")" + "\n");
				z3input.append(sb);
			}
		}
		Iterator<Map.Entry<String, String>> iter = nameMap.entrySet().iterator();
		StringBuilder getValueExpr = new StringBuilder("(get-value (");
		while (iter.hasNext()) {
			Map.Entry<String,String> entry = (Map.Entry<String,String>) iter.next();
			String name = entry.getValue().toString();
			String type = typeMap.get(entry.getValue().toString()).toString();
			z3input.insert(0, "(declare-const " + name + " " + type + ")\n");
			getValueExpr.append(" "+name);
		}
		z3input.append("(check-sat)" + "\n");
		getValueExpr.append(")) " + System.lineSeparator());
		z3input.append(getValueExpr.toString());
		FileIOUtil.writeFile(new File(XPA_HOME + "/Z3_input"),z3input.toString());
	}
	
	public static void buildZ3Input(String input, Z3StrExpressionHelper helper) throws IOException{
		StringBuffer z3input = new StringBuffer();
		String[] lines = input.split(System.lineSeparator());
		for (String s : lines) {
			if (!s.trim().equals("")) {
				StringBuffer sb = new StringBuffer();
				sb.append(s);
				sb.insert(0, "(assert ");
				sb.append(")" + System.lineSeparator());
				z3input.append(sb);
			}
		}
		Iterator<Map.Entry<String,String>> iter = helper.getNameMap().entrySet().iterator();
		StringBuilder getValueExpr = new StringBuilder("(get-value (");
		
		while (iter.hasNext()) {
			Map.Entry<String,String> entry = (Map.Entry<String,String>) iter.next();
			String name = entry.getValue().toString();
			String type = helper.getTypeMap().get(entry.getValue().toString()).toString();
			z3input.insert(0, "(declare-const " + name + " " + type + ")" + System.lineSeparator());
			if(input.indexOf(name)!=-1) {
				getValueExpr.append(" "+name);
			}
		}
		z3input.append("(check-sat)" + System.lineSeparator());
		getValueExpr.append("))" + System.lineSeparator());
		z3input.append(getValueExpr.toString());
		FileIOUtil.writeFile(new File(XPA_HOME + "/Z3_input"),z3input.toString());
	}
	
	public static boolean processExpression(String input, Z3StrExpressionHelper helper) throws IOException {
		Z3StrUtil.buildZ3Input(input, helper);
		Z3StrUtil.buildZ3Output();
		if (Z3StrUtil.checkConflict() == true) {
			helper.updateColletor();
			return true;
		} else {
			return false;
		}
	}
    
	public static void buildZ3Output() throws IOException{ 
		Runtime run = Runtime.getRuntime();
		Process p = run.exec(XPA_HOME + "/z3/build/z3 smt.string_solver=z3str3 -smt2 ./Z3_input");
		BufferedInputStream in = new BufferedInputStream(p.getInputStream());
		BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
		StringBuffer tmpTrack = new StringBuffer();
		String lineStr;
		FileWriter fw = new FileWriter(XPA_HOME + "/Z3_output");
		while ((lineStr = inBr.readLine()) != null) {
			fw.write(lineStr + "\n");
			tmpTrack.append(lineStr + "\n");
		}
		fw.close();
	
	}

	public static boolean checkConflict() throws IOException {
		FileReader fr = new FileReader(XPA_HOME + "/Z3_output");
		BufferedReader br = new BufferedReader(fr);
		String s;
		if ((s = br.readLine()) != null) {
			if (s.equals("sat")) {
				fr.close();
				return true;
			}
		}
		fr.close();
		return false;
	}
}