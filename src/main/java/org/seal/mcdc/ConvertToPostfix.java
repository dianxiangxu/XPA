package org.seal.mcdc;
//package mcdc;

import java.util.ArrayList;

public class ConvertToPostfix {
	
	public final static String and ="&";
	public final static String or="|";
	public final static String zero="0";
	public final static String space=" ";
	
    public ArrayList<String> GeneratePostfixForm(String expressionIn) {

    	int i=0;
		String symbol;
		char c;
		String expression="",subExpression="";
		
		ArrayList<String> postfix    = new ArrayList<String>();
		ArrayList<String> stack      = new ArrayList<String>();
		ArrayList<String> postfixOut = new ArrayList<String>();
		
		for (int ki = 0 ; ki < expressionIn.length() ; ki++) {
		    c = expressionIn.charAt(ki);
		    if ( c != 32 && c != 9 ) {
		        expression+=c;
		    }
		}
		subExpression="";
		stack.add("(");
		expression+=")";

		//System.out.println(expression);
		//System.out.println(expression.length());
		
		while(!(stack.isEmpty())){
		   if(expression.charAt(i)=='('){
			   if(subExpression!="") subExpression+=expression.charAt(i);
			   else
			   stack.add(Character.toString(expression.charAt(i)));
		   }
		   else if(expression.charAt(i)==')'){
			   if(braceCount(subExpression))
			   {  
				   subExpression+=expression.charAt(i);
			   }
			   else{
			  if(i==expression.length()-1&&(subExpression.length()!=0)) {
				postfix.add(subExpression.toString());
			    subExpression="";//make buffer empty
			  }
			  symbol= stack.get(stack.size()-1);
			  stack.remove(stack.size()-1);
			  while(!symbol.equals("("))
			  {
				if(subExpression.length()!=0){
				  postfix.add(subExpression.toString());
				  subExpression="";
				}
              	postfix.add(symbol);
				symbol=stack.get(stack.size()-1);
				stack.remove(stack.size()-1);
			  }
			 }
			}
		    else if(expression.charAt(i)=='&' || expression.charAt(i)=='|'|| (expression.charAt(i)=='!'&&(expression.charAt(i+1)!='='))){
		       if(subExpression.length()!=0)
		       {   
		    	//System.out.println("Print: "+subExpression+" "+expression.charAt(i)); 
		    	postfix.add(subExpression.toString());
		    	subExpression="";
		    	}
		    	symbol=stack.get(stack.size()-1);
				stack.remove(stack.size()-1);
				while (precendence(symbol,expression.charAt(i))){
				   postfix.add(symbol);
				   symbol=stack.get(stack.size()-1);
				   stack.remove(stack.size()-1);
				}
				stack.add(symbol);
				stack.add(Character.toString(expression.charAt(i)));
				if(expression.charAt(i)!='!')
					{i++;}
			}
		    else{
		    	 subExpression+=expression.charAt(i);
			}
		   i++;
		   
		}//WHILE
		
		//System.out.println("Postfix");
		//for(i=0;i<postfix.size(); i++)
		//System.out.print(":"+postfix.get(i));
		
		postfixOut=reversePostfix(postfix);
		return postfixOut;
		
	}//CONVERTTOPOSTFIX
	
	//FUNCTIONS
    
	public boolean precendence(String symbol,char op) {
	   if (symbol.equals("(")) return false;
	   else if((op=='&')&&(symbol.equals("|"))) return false;
	   else if((op=='!')&&(symbol.equals("&")||symbol.equals("|")))return false;
	   else if ((op=='!')&&(symbol.equals("!")))return false;
	   else return true;

    }//PRECEDENCE
	public boolean braceCount(String subExpression)
	{
		int closedBrace=0;
		int openBrace=0;
		
		for(int i=0;i<subExpression.length();i++)
		{
			if(subExpression.charAt(i)=='(') openBrace++;
			else if (subExpression.charAt(i)==')') closedBrace++;
		}
		if((openBrace-closedBrace)==0)
		    return false;
		else 
			return true;
		
	}

	private ArrayList<String> reversePostfix(ArrayList<String> postfix)
	{
		ArrayList<String> rpostfix = new ArrayList<String>();
		
		int c=0;//=1;
		
		//System.out.println("Postfix in function");
		//for(int i=0;i<postfix.size();i++)
		//	System.out.print(": "+postfix.get(i));
//		System.out.println();
		for(int i=postfix.size()-1;i>=0;i--)
		{
			//System.out.print(": "+postfix.get(i));
			if(postfix.get(i).equals(Character.toString('!')))
			{
				if(postfix.get(i-1).equals(Character.toString('|'))||postfix.get(i-1).equals(Character.toString('&')))
				{
					
					if(postfix.get(i-1).equals(Character.toString('|')))
					   rpostfix.add("&");
					else rpostfix.add("|"); 			
					i--;	
					c=c+2;
				}
				else if(postfix.get(i-1).equals(Character.toString('!')))
				{
					i--;
				}
				else
				{
					if(c>0)
					{	
					  rpostfix.add(postfix.get(i-1));
					  c--;
					  i--;
					}
					else rpostfix.add("!"+postfix.get(--i));
				}	
			}//IF
			else
			{
				if(postfix.get(i).equals(Character.toString('|'))||postfix.get(i).equals(Character.toString('&')))
				{
				
					if(c>0)
					{
						if(postfix.get(i).equals(Character.toString('|')))
						   rpostfix.add("&");
						else rpostfix.add("|"); 			
					        c=c+2;
						
					}
					else
					{
						rpostfix.add(postfix.get(i));
					}
				}
				else{
				if(c>0)
				{	
				  rpostfix.add("!"+postfix.get(i));
				  c--;
				}
				else rpostfix.add(postfix.get(i));
				}
			}//ELSE	
			
		}//FOR	
		
		//System.out.println("REVERSE:");
		//for(int i=0;i<rpostfix.size(); i++)
		//System.out.println(i+"."+rpostfix.get(i));
		//return rpostfix;
		
		//ArrayList<String> pf=new ArrayList<String>();
				
		postfix.clear();
		
		for(int i=rpostfix.size()-1;i>=0;i--)
			postfix.add(rpostfix.get(i));
		
		DeleteNotOperator dno=new DeleteNotOperator();
		//String a=dno.deleteNot("!(a>0)");
		
		for(int i=0;i<postfix.size();i++)
		{
			if(!postfix.get(i).equals("|")&& !postfix.get(i).equals("&"))
			{
				String variable=dno.deleteNot(postfix.get(i));
				postfix.remove(i);
				postfix.add(i,variable);
			}	
		}	
		
		return postfix;
   }//REVERSE POSTFIX	

		
	public String removeDuplicates(ArrayList<String> postfix)
	{
		RemoveDuplicatesAND rd  =  new RemoveDuplicatesAND();
		String expression        =  "";
		String newExpression     =  "";
		String firstExpression   =  "";
		String secondExpression  =  "";
		
		for(int i=0;i<postfix.size();i++)
		{
		  if(postfix.get(i).equals(or) || i == postfix.size()-1)
		  {
			  if(i == postfix.size()-1) expression+=postfix.get(i);
			  //System.out.println("exp:"+expression);
			  firstExpression = rd.check(expression);
			  //System.out.println("first: "+firstExpression);
			  if(!firstExpression.equals("NO RESULT"))
				  secondExpression = rd.check(firstExpression);
			  else secondExpression = firstExpression;
			  
			  if(!secondExpression.equals("NO RESULT"))
			  {
				  newExpression+= secondExpression;
				  if(postfix.get(i).equals(or)) 
					 newExpression+= space + or + or+ space ;
			  }
			  else
			  {
				  if(postfix.get(i).equals(or)) 
						 newExpression+= space + or + or+ space ;
				  try{
				     if(newExpression!="")
				        newExpression = (newExpression.substring(0,newExpression.length()-3)).trim();
				  }
				  catch(Exception e)
				  {
					 
				  }
			  }
			  expression="";
			  
		  }	  
		  else if(postfix.get(i).equals(and))
		  {
			  expression+= and + and;
		  }
		  else
		  {
			  expression+= postfix.get(i);
		  }	  
						
		}//FOR i	
				
		return newExpression.trim();
				
	}//REMOVEDUPLICATES
	
	
}//CLASS