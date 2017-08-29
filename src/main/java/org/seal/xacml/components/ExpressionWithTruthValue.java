package org.seal.xacml.components;

public class ExpressionWithTruthValue{
	private String expression;
	private boolean tv;
	public ExpressionWithTruthValue(String expression, boolean tv){
		this.expression = expression;
		this.tv = tv;
	}

	public String getExpression(){
		return expression;
	}
	
	public boolean getTV(){
		return this.tv;
	}
}