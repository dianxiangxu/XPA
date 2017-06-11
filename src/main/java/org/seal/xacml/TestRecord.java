package org.seal.xacml;

public class TestRecord {
	private String request;
	private String oracle;
	private String name;
	
	public TestRecord(String request, String oracle,String name){
		this.request = request;
		this.oracle = oracle;
		this.name = name;
	}
	
	public String getRequest(){
		return request;
	}
	
	public String getOracle(){
		return oracle;
	}

	public String getName() {
		return name;
	}
}
