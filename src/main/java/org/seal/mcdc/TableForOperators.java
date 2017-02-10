package org.seal.mcdc;
//package mcdc;
import java.util.HashMap;

public class TableForOperators {
   
	
	public static final HashMap<String, String>  operatorsMap=new HashMap<String,String>();
		static{	
	        operatorsMap.put("<<", "1");
	        operatorsMap.put("<<=","2");
	        operatorsMap.put("<>", "3");
	        operatorsMap.put("<>=","4");
	        operatorsMap.put("<==","5");
	        operatorsMap.put("<!=","6");
	        
	        operatorsMap.put("<=<", "7");
	        operatorsMap.put("<=<=","8");
	        operatorsMap.put("<=>", "9");
	        operatorsMap.put("<=>=","10");
	        operatorsMap.put("<===","11");
	        operatorsMap.put("<=!=","12");
	        
	        operatorsMap.put("><", "13");
	        operatorsMap.put("><=","14");
	        operatorsMap.put(">==","15");
	        operatorsMap.put(">!=","16");
	        operatorsMap.put(">>", "17");
	        operatorsMap.put(">>=","18");
	        
	        
	        operatorsMap.put(">=<", "19");
	        operatorsMap.put(">=<=","20");
	        operatorsMap.put(">===","21");
	        operatorsMap.put(">=!=","22");
	        operatorsMap.put(">=>", "23");
	        operatorsMap.put(">=>=","24");
	        
	        
	        operatorsMap.put("==<", "25");
	        operatorsMap.put("==<=","26");
	        operatorsMap.put("====","27");
	        operatorsMap.put("==!=","28");
	        operatorsMap.put("==>", "29");
	        operatorsMap.put("==>=","30");
	        
	        
	        operatorsMap.put("!=<", "31");
	        operatorsMap.put("!=<=","32");
	        operatorsMap.put("!===","33");
	        operatorsMap.put("!=!=","34");
	        operatorsMap.put("!=>", "35");
	        operatorsMap.put("!=>=","36");
	        
	        
	        //System.out.println(opearatorsMap.size());
	}	
}

