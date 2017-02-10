package org.seal.coverage;

import java.util.Vector;

public class PolicySpreadSheetTestRecord {

	private String number;
	private String requestFile;
	private String request;
	private String oracle;
	
	public PolicySpreadSheetTestRecord(String number, String requestFile, String request, String oracle){
		this.number = number;
		this.requestFile = requestFile;
		this.request = request;
		this.oracle = oracle;
	}
	
	public String getNumber(){
		return number;
	}
		
	public String getRequestFile(){
		return requestFile;
	}
	
	public String getRequest(){
		return request;
	}
	
	public String getOracle(){
		return oracle;
	}
	
	public Vector<Object> getTestVector(){
		Vector<Object> vector = new Vector<Object>();
		vector.add("");		// sequence number
		vector.add(number);	// test name

		vector.add(requestFile);
		vector.add(oracle);
		vector.add("");	// actual response
		vector.add("");	// verdict
		vector.add(request);
		return vector;
	}
	
}
