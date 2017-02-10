package org.seal.mcdc;

import java.util.ArrayList;

public class SimplifyExpression {
	
	public static final String zero = "0";
	public static final String one  = "1";
	public static final String and  = "&";
	public static final String or   = "|";
	public static final String delemeter  = "*";
	
	
	public String simplify(ArrayList<String> postfix)
	{
		//System.out.println("postfix SE: "+ postfix.toString());
		ArrayList<String> stack    = new ArrayList<String>();
		ArrayList<String> expStack = new ArrayList<String>();
		String secondVar   = "";
		String firstVar    = "";
		//String variable    = "";
		String expression  = "";
		
		if(postfix.size()==1) return postfix.get(0);
		
		for(int i=0;i<postfix.size();i++)
       	{
       	   if( !postfix.get(i).equals(and) && !postfix.get(i).equals(or))
       		   stack.add(postfix.get(i));
       	   else
       	   {
       		 if( postfix.get(i).equals(and) )
             {
       			 if( stack.get(stack.size()-1).trim().equals(delemeter) )
                     secondVar = one;
                   else 
                     secondVar =  stack.get(stack.size()-1).trim();
                   
              	  stack.remove(stack.size()-1);
              	  
              	  if( stack.get(stack.size()-1).trim().equals(delemeter) )
                        firstVar = one;
                    else 
                        firstVar =  stack.get(stack.size()-1).trim();
              	stack.remove(stack.size()-1);  
          	    String result = checkForONEs(firstVar,secondVar);
          	    stack.add(delemeter);
          	    if(result!=null){
          	    if( result.contains("&&"))
                   expStack.add(result);
          	    else
          	    {
          	    	if(expStack.size()!=0){
          	    	String tempVar = expStack.get(expStack.size()-1);
          	    	expStack.remove(expStack.size()-1);
          	    	tempVar+= " && "+result;
          	    	expStack.add(tempVar);
          	    	}
          	    	else expStack.add(result);
          	    }//else
                }
          	   }// if postfix.get(i).equals(and)
      		            
               else if( postfix.get(i).equals(or) )
               {
                 if( stack.get(stack.size()-1).trim().equals(delemeter) )
                   secondVar = zero;
                 else 
                   secondVar =  stack.get(stack.size()-1).trim();
                 
            	  stack.remove(stack.size()-1);
            	  
            	  if( stack.get(stack.size()-1).trim().equals(delemeter) )
                      firstVar = zero;
                  else 
                      firstVar =  stack.get(stack.size()-1).trim();
                    
               	  stack.remove(stack.size()-1);
            	  if(!firstVar.equals(zero)|| !secondVar.equals(zero))
            	  {	  
            	    String result = checkForDuplicates(secondVar+"||"+firstVar);
            	   //System.out.println("value: "+result);
            	   if(result.contains("||"))
            	    stack.add("("+result+")");
            	   else  stack.add(result);
            	  }
            	  else {
            		  stack.add(delemeter);
            	  }
       		    }//ELSE ( postfix.get(i).equals(or) ) 
       		 
       		  }//ELSE 	  
       	     
       		       	
       	 }//FOR i	
		
       	//System.out.println("Final:" + stack.get(0));
       	
       	if(stack.get(stack.size()-1).equals(delemeter))
       		stack.remove(stack.size()-1);
       	if(expStack.size()==0)
       	{
       		if (stack.size()==0||stack.get(0).equals(one))
       			return one;
       		else return stack.get(0);
       	}
       	else
       	{
       		for(int i=0;i<expStack.size();i++)
       			expression+= expStack.get(i)+"||";
       		expression = expression.substring(0,expression.length()-2);
       		if(stack.size()!=0)
       			if(!stack.get(0).equals(one)&& !stack.get(0).equals(delemeter))
       			expression+="||"+stack.get(0);
       	}	
       	
       	return expression;
       	
	}//simplify

	private void changeValues(ArrayList<String> postfix, String result, int i, int j)
	{
		switch(operator.checkOp(result))
		{
		    case ZERO : break;
		    case ONE:   postfix.remove(i);
                        postfix.add(i,zero);
	                    break; 
		    	
		    case TWO:   postfix.remove(j);
		                postfix.add(j,zero);
		    	        break;
		    	
		    case THREE:	postfix.remove(i);
                        postfix.add(i,one);
                        postfix.remove(j);
                        postfix.add(j,one);
                        break;
		    case FOUR: 
		    case FIVE:
		    case SIX:   postfix.remove(i);
                        postfix.add(i,result);
                        postfix.remove(j);
                        postfix.add(j,zero);
                        break;
		    default:    postfix.remove(i);
                        postfix.add(i,result);
                        postfix.remove(j);
                        postfix.add(j,zero);    
		    	        break;	
		 }

		
	}//changeValues
	
	public enum operator
	{
	    ZERO,ONE,TWO,THREE,FOUR,FIVE, SIX,NOVALUE;

	    public static operator checkOp(String str)
	    {
	        try {
	        	if (str.equals("0"))
		               return valueOf("ZERO");
	        	else if (str.equals("1"))
	               return valueOf("ONE");
	        	else if (str.equals("2"))
	        	   return valueOf("TWO");
	        	else if(str.equals("3"))
	        		return valueOf("THREE");
	        	else if(str.equals("4"))
	        		return valueOf("FOUR");
	        	else if(str.equals("5"))
	        		return valueOf("FIVE");
	        	else if(str.equals("6"))
	        		return valueOf("SIX");
	        	else 
	        		return valueOf("NOVALUE");
	        } 
	        catch (Exception ex) {
	            return NOVALUE;
	        }
	    }   
	}//ENUM

	private String checkForDuplicates(String variable)
	{
		ArrayList<String>  expStack = new ArrayList<String>();
		RemoveDuplicatesOR rdOR     = new RemoveDuplicatesOR();
		String[] tokens;
		String or ="\\|\\|";
		
		//System.out.println("var: " + variable);
		tokens=variable.split(or);
		
		for (int x=0; x<tokens.length; x++)
		{
			expStack.add(checkForBraces(tokens[x]));
			expStack.add("||");
		}
		expStack.remove(expStack.size()-1);
		
		for(int i=0;i<expStack.size();i+=2)
		 for(int j=i+2;j<expStack.size();j+=2)
		 {
			 if( !expStack.get(i).equals(zero) && !expStack.get(i).equals(one)
					 && !expStack.get(j).equals(zero) && !expStack.get(j).equals(one) )
			 {
				//System.out.println(expStack.get(i)+"||"+expStack.get(j)); 
				String result = rdOR.check(expStack.get(i)+"||"+expStack.get(j));
			    //System.out.println("result:" + result );
				changeValues(expStack, result,i,j);
			 }//IF	
			 
		 }//FOR J	
		variable = "";
			
		if (expStack.contains(one))
			return one;
		
		return removeZeros(expStack);
		
	}//CHECKFORDUPLICATES
	
	private String checkForONEs(String firstVar, String secondVar)
	{
		if(!firstVar.equals(one) && !secondVar.equals(one))
			return  firstVar+" "+and+and+" "+secondVar;
		else if(!firstVar.equals(one))
			return firstVar;
		else if(!secondVar.equals(one))return secondVar;
		//else return one;
		return null;
	}//checkForONEs
	
	private String checkForBraces(String variable)
	{
		int count = braceCount(variable);
		if(count>0)
		{	
		 if(variable.charAt(0)=='(')
			 return variable.substring(count,variable.length());
		 else return variable.substring(0,variable.length()-count);
		}
		else return variable;
		//return null;
	}//String
	
	public int braceCount(String subExpression)
	{
		int closedBrace = 0;
		int openBrace   = 0;
		
		for(int i=0;i<subExpression.length();i++)
		{
			if(subExpression.charAt(i)=='(') openBrace++;
			else if (subExpression.charAt(i)==')') closedBrace++;
		}
		return Math.abs(openBrace-closedBrace);
		
	}
	
	public String removeZeros(ArrayList<String> expStack)
	{
		String variable = "";
		for(int i=0; i<expStack.size(); i++)
		{
			if(expStack.get(i).equals(zero))
			{
				expStack.remove(i);
				
				if(expStack.size()==i+1){
						expStack.remove(i-1);
						i--;
				}
				else if(expStack.size()!= 0)
				{
					if(i!= 0)
						{expStack.remove(i-1);i--;}
					else{
						expStack.remove(i);i--;}
				}
			}
		}
		for(int i =0 ;i<expStack.size();i++)
		{
			if(expStack.get(i).equals("|"))
			{
				variable+=" || ";
			}
			else 
			variable+=expStack.get(i);
		}	
		return variable;
	}
}

