package org.seal.mcdc;
//package mcdc;
import java.util.ArrayList;

public class ConvertToExpression {
	
	ArrayList<String> expression=new ArrayList<String>();
	public static String and  = "&";
	public static String or   = "|";
	public static String zero = "0";
	public static String one  = "1";
	
	public String GenerateExpression(ArrayList<String> postfix)
	{
		ArrayList<String> stack=new ArrayList<String>();
		//ArrayList<String> expression=new ArrayList<String>();
		String finalExpression="";
		
		String secondVar="";
		String firstVar="";
		
		if(postfix.size()==1)
			return postfix.get(0);
		
		for(int i=0;i<postfix.size();i++)
		{
		  //System.out.println("cte:"+postfix.get(i));	
		   if(!postfix.get(i).equals(or)&& !postfix.get(i).equals(and))
		   {
				stack.add(postfix.get(i));
		   }
		   else
		   {
			   //expression.add("(");
			   expression.clear();
			   secondVar=stack.get(stack.size()-1).trim();
			   stack.remove(stack.size()-1);
			    

			   firstVar=(stack.get(stack.size()-1)).trim();
			   stack.remove(stack.size()-1);
			   
			   if(postfix.get(i).equals(and))
			   {	   
			     if(!secondVar.equals(zero) && !secondVar.equals(one) && !firstVar.equals(zero) && !firstVar.equals(one))
			     {	 
					   finalExpression="("+firstVar+postfix.get(i)+postfix.get(i)+secondVar+")";
					   stack.add(finalExpression);
			     }
			     else if(secondVar.equals(one))
			     { 
			    	 finalExpression=firstVar;
			    	 stack.add(firstVar);
			     
			     }
			     else if(firstVar.equals(one))
			     { 
			    	 finalExpression=secondVar;
			    	 stack.add(secondVar);
			     
			     }
			     else 
			     {	 finalExpression = zero;
			    	 stack.add(zero);
			     
			     }
			   }
			   else
			   {	   
			       if(!secondVar.equals(zero)&& !secondVar.equals(one)&& !firstVar.equals(zero) && !firstVar.equals(one))
			       {
			    	   finalExpression="("+firstVar+postfix.get(i)+postfix.get(i)+secondVar+")";
					   stack.add(finalExpression);
			       } 
			       else if(secondVar.equals(zero))
			       {
				     //String newVar="("+firstVar+postfix.get(i)+secondVar+")";
			    	   finalExpression=firstVar;
			    	   stack.add(firstVar);
			       }
			       else if(firstVar.equals(zero))
			       {
				     //String newVar="("+firstVar+postfix.get(i)+secondVar+")";
			    	   finalExpression=secondVar;
			    	   stack.add(secondVar);
			       }
			       else 
			    	   
			       {
			    	   finalExpression=one;
			    	   stack.add(one);  
				   }	   
		         }	   
		    
		}	//FOR i
		}	
		//System.out.println("Expression");
		//for(int i=0;i<expression.size(); i++)
			//System.out.println(i+"."+expression.get(i));
		//System.out.println("exp: "+finalExpression);	
		return finalExpression;
  }//GENERATEEXPRESSION
		
	
	public void CheckForNegatives(ArrayList<String> postfix)
	{
		for(int i=0;i<postfix.size();i++)
		{
			if(!postfix.get(i).equals("&")&& !postfix.get(i).equals("|"))
			{
				for(int j=0;j<postfix.size();j++)
				{
					if(!postfix.get(i).equals("&")&& !postfix.get(i).equals("|"))
					{
						
					}//posftfix j
					
				}//FOR j	
				
			}//postfix i	
		}//FOR i	
		
	}//CheckForNegatives
	
	public ArrayList<String> ChangeFormat(String expressionIn)
	{
		ConvertToPostfix ctp=new ConvertToPostfix();
		ArrayList<String> postfix =new ArrayList<String>(); 
		String subVar="";
		String expression = "";
		boolean notOp=false;
		
		for (int ki = 0 ; ki < expressionIn.length() ; ki++) {
		    char c = expressionIn.charAt(ki);
		    if ( c != 32 && c != 9 ) {
		        expression+=c;
		    }
	   }
        
//		System.out.println("Expression in cte: "+expression);
		for(int i=0;i<expression.length();i++)
		{
			if(expression.charAt(i)=='(')
			{
			    if(subVar=="")
				  postfix.add(new Character(expression.charAt(i)).toString());
			    else subVar+=expression.charAt(i);
			}//IF
			else if(expression.charAt(i)=='!')
			{
				if(subVar=="")
				notOp=true;
				else subVar+=expression.charAt(i);
			}
			else if(expression.charAt(i)==')')
			{
				if(ctp.braceCount(subVar))
				{
					subVar+=expression.charAt(i);
					if(i==expression.length()-1)
					if(notOp)
					{
						notOp=false;
						if(subVar!="")postfix.add(subVar);
						postfix.add("!");
						subVar="";
					}
					else
					{
						if(subVar!="")postfix.add(subVar);
						subVar="";
						
					}
				}
				else
				{
					if(notOp)
					{
						notOp=false;
						if(subVar!="")postfix.add(subVar);
						postfix.add("!");
						postfix.add(")");
						subVar="";
					}
					else
					{
						if(subVar!="")postfix.add(subVar);
						postfix.add(")");
						subVar="";
						
					}	
				}	
			}
			else if(expression.charAt(i)=='|'|| expression.charAt(i)=='&')
			{
				i++;
				if(notOp)
				{
					notOp=false;
					if(subVar!="")postfix.add(subVar);
					postfix.add("!");
					subVar="";
				}
				else
				{
					if(subVar!="")
					{
						postfix.add(subVar);
						//postfix.add(new Character(expression.charAt(i)));
					}
					subVar="";
					
				}
//				System.out.println("i: " + i);
				postfix.add(new Character(expression.charAt(i)).toString());
				
			}
			else if(i==expression.length()-1)
			{
				if(notOp)
				{
					subVar+=expression.charAt(i);
					  postfix.add(subVar);
					  postfix.add("!");
					  subVar="";
				}
				else{
				  subVar+=expression.charAt(i);
				  postfix.add(subVar);
				  subVar="";
				}
			}
			else
			{
				subVar+=expression.charAt(i);
			}	
				
			
		}//FOR i	
		
		//for(int i=0;i<postfix.size();i++)
			//System.out.println(postfix.get(i));
		
		return postfix;
		
	}//CheckForNegatives

	public ArrayList<String> storeInArrayList(String expression)
	{
		ArrayList<String> tempPostfix =new ArrayList<String>();
		ArrayList<String> postfix =new ArrayList<String>();
		String subVar="";
		String variable="";
		
		for(int i=0;i<expression.length();i++)
		{
			if(expression.charAt(i)==',')
			{
			  tempPostfix.add(subVar);
			  subVar="";
			}
			else if(expression.charAt(i)=='!')
			{
				if(subVar!="") subVar+=expression.charAt(i);
				else{
				variable=tempPostfix.get(tempPostfix.size()-1);
				tempPostfix.remove(tempPostfix.size()-1);
				tempPostfix.add("!"+variable);
				}
			}	
			else if(expression.charAt(i)=='|')
			{
				tempPostfix.add("|");
			}
			else if(i==expression.length()-1)
			{  
				subVar+=expression.charAt(i);
				subVar+=expression.charAt(i);
				tempPostfix.add(subVar);
				subVar="";
			}
			else
			{
				subVar+=expression.charAt(i);
				
			}	
			
		}//FOR i	
			
		for(int i=0;i<tempPostfix.size()-1;i++)
		{
			if(!tempPostfix.get(i).equals("|")){
			   if(!tempPostfix.get(i+1).equals("|"))
			   {
				 postfix.add(tempPostfix.get(i));
				 postfix.add("&");
			   }
			   else
			   {
				 postfix.add(tempPostfix.get(i));
			   }	
			}
			else postfix.add(tempPostfix.get(i));
		}
		postfix.add(tempPostfix.get(tempPostfix.size()-1));
		//for(int i=0;i<postfix.size();i++)
			//System.out.print(": "+postfix.get(i));
		
		return postfix;
	}//STOREINARRAYLIST
	
	public String putNots(String expressionIn)
	{
	  ArrayList<String> tokens = new ArrayList<String>();
	  int    index     = 0;
      String expressionOut = "";
      String expression    = "";
	  String variable  = "";
	  String firstVar  = "";
	  String secondVar = "";
	  String firstNum  = "";
	  String secondNum = "";
	  String three     = "3";
	  char or  = '|';
	  char and = '&'; 
	  char not = '!';
	  
	  for (int ki = 0 ; ki < expressionIn.length() ; ki++) {
		    char c = expressionIn.charAt(ki);
		    if ( c != 32 && c != 9 ) {
		        expression+=c;
		    }
	  }
	  
	  for(int i=0;i<expression.length();i++)
	  {
		 if(expression.charAt(i)==or || expression.charAt(i)==and )
		 {
			 tokens.add(variable.trim());
			 tokens.add(new Character(expression.charAt(i)).toString()+new Character(expression.charAt(i)).toString());
		     variable = "";
		     i++;
		 }
		 else if(i == expression.length()-1)
		 {
			 variable+=expression.charAt(i);
			 tokens.add(variable.trim());
			 variable = "";
		 }
		 else  
			 variable+=expression.charAt(i);
		 
	  }//FOR i 

	  RemoveDuplicatesOR rdOR = new RemoveDuplicatesOR();
	  
      for(int i =0 ; i<tokens.size(); i+=2)
      {
    	  for(int j = i+2; j<tokens.size(); j+=2)
    	  {
    		  firstVar  = tokens.get(i);
    		  secondVar = tokens.get(j);
    		  firstNum  = "";
    		  secondNum = "";
    		  if(firstVar.charAt(index)!= not && secondVar.charAt(index)!= not )
    		  {
    			//System.out.println(rdOR.check(firstVar+or+or+secondVar));
    			firstNum  = getNumber(firstVar);
    			secondNum = getNumber(secondVar);
    			if(firstNum!="" && secondNum!="" && firstNum.equals(secondNum))
    			{	
    	          if((rdOR.check(firstVar+or+or+secondVar)).equals(three))
    	          {
    	        	tokens.remove(j);
    	        	tokens.add(j,"!"+tokens.get(i));
    	        	
    	          }//rdOR.check()	
    			}//IF firstNum,secondNum
    		  }	// if(firstvar.charAt(index))  
    		  
    	  }//FOR j
    	  
      }//FOR i	  
      
      for(int i = 0;i<tokens.size();i++)
    	  expressionOut+= tokens.get(i);
 
      //System.out.println(tokens.toString());     
      return expressionOut;
      
  }//putNots
	
	public String getNumber(String variable)
	{
		String localVar = "";
		String number   = "";
		String operator = "";	
	    int i;
	    
	    for(i=0;i<variable.length();i++)
		{
		   if(variable.charAt(i)!='<' && variable.charAt(i)!='>' && variable.charAt(i)!='=' && (variable.charAt(i)!='!'|| i==0))
		   {
			  localVar+=variable.charAt(i);
		   }
		   else
		   {
			  operator+=variable.charAt(i);
			  break;
		   }
		 }//FOR i
	    
		 for(int j=i+1;j<variable.length();j++)
		 {
			if(variable.charAt(j)!='=')
				number+=variable.charAt(j);
			else 
				operator+=variable.charAt(j);
		 }//FOR j
		    //System.out.println("variable1:operator1:number1-> "+variable1+":"+operator1+":"+number1);
         
		 return number;      
  
  }//getNumber
	
  	
}//CLASS
