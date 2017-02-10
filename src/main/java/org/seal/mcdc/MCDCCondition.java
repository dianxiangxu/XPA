package org.seal.mcdc;
//package mcdc;

public class MCDCCondition {

	private String condition;
	private boolean positive;

	public MCDCCondition(String condition, boolean positive) {
		this.condition = condition;
		this.positive = positive;
	}

	public String getCondition(){
		return condition;

	}

	public boolean isPositive() {
		return positive;
	}

//	public String toString(){
//		return condition + " -> " + positive;
//	}
	// edited by NING SHEN , 11/17/2014
	public String toString(){
		return condition ;
	}
}
