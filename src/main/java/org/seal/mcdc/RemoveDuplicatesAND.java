package org.seal.mcdc;
//package mcdc;
import java.util.ArrayList;

public class RemoveDuplicatesAND {

	public final static char greaterThan='>';
	public final static String and="&&";
	boolean removed;
	String localExpression="",finalExpression="";
	String[] tokens; 
	ArrayList<String> removeItems=new ArrayList<String>();
	ArrayList<String> addItems=new ArrayList<String>();
	
	public String check(String expression) {
         
		removed=false;
		finalExpression="";
		removeItems.clear();
		addItems.clear();
		//System.out.println("Expression: "+expression);
		localExpression=removeDuplicates(expression);
		//System.out.println("LocalEx: "+localExpression);
		tokens=localExpression.split(and);
		outerLoop();
		
		finalExpression=removeDuplicates(localExpression);
		return finalExpression.trim();
		
	}//CHECK


	public void outerLoop()
	{
		String firstPart,variable1="",number1="",operator1="";	
	    int i;
	    	    	    
		for (int x=0; x<tokens.length; x++)
		{   
			variable1="";number1="";operator1="";
			firstPart = tokens[x].trim();
			
			for(i=0;i<firstPart.length();i++)
			{
			    if(firstPart.charAt(i)!='<' && firstPart.charAt(i)!='>' && firstPart.charAt(i)!='=' && (firstPart.charAt(i)!='!'|| i==0))
				{
					variable1+=firstPart.charAt(i);
				}
				else
				{
					operator1+=firstPart.charAt(i);
					break;
				}
			}
			for(int j=i+1;j<firstPart.length();j++)
			{
				if(firstPart.charAt(j)!='=')
					number1+=firstPart.charAt(j);
				else 
					operator1+=firstPart.charAt(j);
			}
		    //System.out.println("variable1:operator1:number1-> "+variable1+":"+operator1+":"+number1);
            innerLoop(x,variable1,operator1,number1);
	    }//FOR X
  }//OUTERLOOP

	
	
	
     public void innerLoop(int x,String variable1,String operator1,String number1)
	 {
		  String secondPart="",variable2,operator2,number2;
		  int i,operatorValue=0;
		  
		  //removeItems.clear();
		  //System.out.println("x:"+x);
		  //System.out.println("LENGTH:"+ tokens.length);
		  for (int y=x+1; y<tokens.length; y++)
		  {
			 secondPart = tokens[y].trim();
			 //System.out.println("secondPart:"+secondPart+":");
			 variable2="";number2="";operator2="";
				
			 for(i=0;i<secondPart.length();i++)
			 {
				if(secondPart.charAt(i)!='<' && secondPart.charAt(i)!='>' && secondPart.charAt(i)!='=' && (secondPart.charAt(i)!='!'|| i==0))
					{
						variable2+=secondPart.charAt(i);
					}
					else
					{
						operator2+=secondPart.charAt(i);
						break;
					}
			  }
			  for(int j=i+1;j<secondPart.length();j++)
			  {
				 if(secondPart.charAt(j)!='=')
					number2+=secondPart.charAt(j);
				 else 
					operator2+=secondPart.charAt(j);
			  }
			  //System.out.println("variable2:operator2:number2-> "+variable2+":"+operator2+":"+number2);
				
			  if(variable1.equals(variable2))
			  {		
				 if(operator1!="" && operator2!="")
				 {	 
			         //System.out.println("befor value: "+ operatorValue);
			         operatorValue=Integer.parseInt((TableForOperators.operatorsMap.get(operator1+operator2)).toString());
			         //System.out.println("value: "+ operatorValue);
			  
			         ExpresionCheckerAND ecAND=new ExpresionCheckerAND();
			         operatorValue =ecAND.checkOperands(number1,number2,operatorValue);
			         switch(operatorValue)
			         {
			           case 0: //System.out.println("Keep it");
			                   break;
			           case 1:  //System.out.println("Remove First");
	                   //System.out.println(variable1+operator1+number1);
			                    removeItems.add(variable1+operator1+number1);
	                            break;
			           case 2:
				               //System.out.println("Remove second");
		                      //System.out.println(variable2+operator2+number2);
				               removeItems.add(variable2+operator2+number2);
		                       break;
			           case 3://System.out.println("Remove Both");
	                          //System.out.println(variable1+operator1+number1);
	                           //System.out.println(variable2+operator2+number2);
			                  removeItems.add(variable1+operator1+number1);
	                          removeItems.add(variable2+operator2+number2);
	                          //System.out.println("Remove1");
	                          removed=true;
			                  break;
			           case 4://<=>= or >=<=
			        	      removeItems.add(variable1+operator1+number1);
                              removeItems.add(variable2+operator2+number2);
                              addItems.add(variable1+"=="+number1);
                              break;
			           case 5: //!=>= or >=!=
			        	      removeItems.add(variable1+operator1+number1);
                              removeItems.add(variable2+operator2+number2);
                              addItems.add(variable1+">"+number1);      
			        	      break;
			           case 6: //!=>= or >=!=
			        	      removeItems.add(variable1+operator1+number1);
                              removeItems.add(variable2+operator2+number2);
                              addItems.add(variable1+"<"+number1);
                              break;
			            default:break;
			  
			          }//SWITCH
				 }//Both are operators
				 else
					 removeItems.add(variable1);
			
			  }//IF VARIABLE1==VARIABLE2
			  else if((variable1.equals("!"+variable2)) ||(variable2.equals("!"+variable1)))
			  {
				  removeItems.add(variable1+operator1+number1);
				  removeItems.add(variable2+operator2+number2);
				  //System.out.println("Remove2");
				  removed=true;
			  }	  
		  }//FOR
		  	  
	   }//INNERLOOP
     
    
   private String removeDuplicates(String expression)
   {
	   int i=0;
	   String newExpression="";
	   String[] newTokens;
	   newTokens=expression.split(and);
	   
	   if(removed)
		 return "NO RESULT";
	   for(i=0;i<newTokens.length;i++)
	   {
		 if(!removeItems.contains(newTokens[i].trim()))
		 {		 
			  newExpression+=newTokens[i].trim()+" "+and +" ";
			  removeItems.add(newTokens[i].trim());
		 }	  
	   }	  
	   
	  // System.out.println("NewEx : "+ newExpression);
	   removeItems.clear();
	   for(i=0;i<addItems.size();i++)
	   {
		 newExpression+=addItems.get(i)+and +" ";
			 	 
	   }
	   addItems.clear();
	   
	   if(newExpression!="")
	    return newExpression.substring(0,newExpression.length()-3);
	   else return "NO RESULT";
   }//REMOVE DUPLICATE
     
}
