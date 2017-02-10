package org.seal.mcdc;
//package mcdc;
public class DeleteNotOperator {
	
	String finalExpression;
	public String deleteNot(String localExpression)
	{
		int i;
		String[] tokens;
		String subToken;
		boolean operatorNot;
		tokens=localExpression.split(TestTupleGenerator.LogicalAnd);
		finalExpression="";
		
		for(i=0;i<tokens.length;i++)
		{
			subToken=tokens[i].trim();
			operatorNot=false;
			if(subToken.charAt(0)==TestTupleGenerator.operatorNot)
			{ 
			  operatorNot=true;
			  removeNot(removeBraces(subToken.substring(1),operatorNot),operatorNot);	
			}	
			
			else
			{	
				finalExpression+=removeBraces(subToken,operatorNot)+" && ";
			}
		}
		//System.out.println(finalExpression.substring(0,finalExpression.length()-3));
		return (finalExpression.substring(0,finalExpression.length()-3));
    }//DELETE NOT
	
	private String removeBraces(String subToken,boolean operatorNot)
	{
		int i,braceCount=0;
		//System.out.println("OPe: "+operatorNot);
		//k=operatorNot==true?1:0;
		//System.out.println(k);
		for(i=0;i<subToken.length();i++)
		{
			if(subToken.charAt(i)==TestTupleGenerator.leftParanthesis)
			  braceCount++;
			else 
			  break;
		}
		String tempSubToken=subToken.substring(braceCount);
		//System.out.println("BC: "+braceCount);
		//System.out.println("Temp: "+tempSubToken);
		String newSubToken=tempSubToken.substring(0,(tempSubToken.length()-braceCount));
		//System.out.println("NEW: "+newSubToken);
		return newSubToken;
		
	}//REMOVE BRACES
	
	private void removeNot(String subToken,boolean operatorNot)
	{
		boolean operator=false;
		String variable ="";
		int i;
		
		for(i=0;i<subToken.length();i++)
		{
			if(subToken.charAt(i)!=TestTupleGenerator.greaterThan && subToken.charAt(i)!=TestTupleGenerator.lessThan 
					&& subToken.charAt(i)!=TestTupleGenerator.operatorEquals && subToken.charAt(i)!=TestTupleGenerator.operatorNot)
			{
				variable +=subToken.charAt(i);
			}
			else
			{
				operator=true;
				TestTupleGenerator gtt=new TestTupleGenerator();
				variable+= gtt.checkOperator(subToken.charAt(i),subToken.charAt(i+1));
			    break;	
			}	
		}
		for(int j=i+1;j<subToken.length();j++)
		{
			if(subToken.charAt(j)!=TestTupleGenerator.operatorEquals)
			{	
		       variable+=subToken.charAt(j);
			}
		}	
		finalExpression+=operatorNot==true && operator==true?variable :TestTupleGenerator.operatorNot+ variable;
	    finalExpression+=" "+TestTupleGenerator.LogicalAnd+" ";
	
	}//REMOVE NOT
	
}
