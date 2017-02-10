package org.seal.combiningalgorithms;

public class QueryForDifference {
	String query;
	String combiningAlgMutant;
	int decision;
	
	public QueryForDifference(String query, String alg){
		this.query = query;
		this.combiningAlgMutant = alg;
	}
	
	public void setDecision(int decision){
		this.decision = decision;
	}
	
	public String getQuery(){
		return this.query;
	}
	
	public String getCombiningAlgMutant(){
		return this.combiningAlgMutant;
	}
	
	public int getDecision(){
		return this.decision;
	}
}
