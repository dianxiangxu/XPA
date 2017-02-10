package org.seal.mcdc;

import java.util.ArrayList;
import javax.swing.table.DefaultTableModel;

public class TestTupleGenerator {
	
	    public final static char operatorAnd ='&';
		public final static char operatorOr='|';
		public final static char operatorEquals='=';
		public final static char operatorNot='!';
		public final static char greaterThan='>';
		public final static char lessThan='<';
		public final static char ENTER = 9;
		public final static char SPACE = ' ';
		public final static String LogicalAnd="&&";
		public final static char leftParanthesis='(';
		public final static char rightParanthesis=')';
				
		public static ArrayList<MCDCCondition> generateTestConditions(ArrayList<String> expressionIn, DefaultTableModel MCDCTestCases)	{
			
			ArrayList<MCDCCondition> testTuples= new ArrayList<MCDCCondition>();
			ArrayList<MCDCCondition> simplifiedTestTuples= new ArrayList<MCDCCondition>();
			ArrayList<String> expressionList= new ArrayList<String>();
			
			String condition="";
		    int column;
		    boolean result;
		    	    
		    DeleteNotOperator DNO=new DeleteNotOperator();
		    RemoveDuplicatesAND RD=new RemoveDuplicatesAND();

		    //expression=removeSpaces(expressionIn);
		    System.out.println("TESTCASES:\n");
           		    
		    for(int i=0;i<expressionIn.size();i++)
		    {
		    	if(!(expressionIn.get(i).equals(Character.toString('|'))||expressionIn.get(i).equals(Character.toString('&'))))
		    	{
		    		expressionList.add(expressionIn.get(i));
		    	}
		    }	
		    	
		    for(int row=0; row<MCDCTestCases.getRowCount();row++){
			    column = 0;
				condition="";
				result=false;
				 
			    for(int count=0;count<expressionList.size();count++)
			    {	 
				    if(MCDCTestCases.getValueAt(row, column)=="T"){
				       result=true;	 
				       //System.out.println(expressionList.get(count));
				       condition += removeBraces(expressionList.get(count),result); 
					   condition += " " + LogicalAnd + " ";
					   column++;
				    }
				    else {
					   result=false;
					   condition+=removeBraces(expressionList.get(count),result); 
					   condition += " " + LogicalAnd + " ";
					   column++;
				    }
				    
			     }//COUNT  
			    
				//column++;
			    //System.out.println("cnod:" + condition);
			  condition=condition.substring(0,condition.length()-3);
			  			   
			   boolean truthValue = MCDCTestCases.getValueAt(row, column) == "T"? true: false;
			   testTuples.add(new MCDCCondition(condition, truthValue));
			   String newCondition=DNO.deleteNot(condition);
			   System.out.println(newCondition+"->"+truthValue);
			   String simplifiedCondition = RD.check(newCondition);
			   if(!simplifiedCondition.equals("NO RESULT"))
			   simplifiedTestTuples.add(new MCDCCondition(simplifiedCondition, truthValue));
		   
		   } //ROW
			
		   System.out.println("\nSimplified Test cases:");	
		   return simplifiedTestTuples;
			//return testTuples;

		}

  public static String checkOperator(char tempChar,char nextTempChar) {
	 	 String value;
		 switch(tempChar)
		 {
			  case greaterThan: return value = nextTempChar==operatorEquals? "<":"<=";
			  case lessThan: return value = nextTempChar==operatorEquals? ">":">=";
			  case operatorEquals: return value = nextTempChar==operatorEquals? "!=":"";
			  case operatorNot: return value = nextTempChar==operatorEquals? "==":"";
			  default: value = "";
		 }
		 return value;
			
  }//GENERATETESTCONDITIONS

  /*
  private static String removeSpaces(String expressionIn){
	   String expression="";
	   for (int i = 0 ; i < expressionIn.length() ; i++) {
		  if ( expressionIn.charAt(i) != 32 && expressionIn.charAt(i) != 9 ) {
		     expression+=Character.toString(expressionIn.charAt(i));
		  }
		}
	    return expression;
  }
*/		
  private static String removeBraces(String variable,boolean result){
	
	  String firstVar="", secondVar="";
	  int i,braceCount=0,leftBrace=0,rightBrace=0;
	  
	  int startPosition=result&&variable.charAt(0)==operatorNot?1:0;
	  //System.out.println("var: "+variable);
	  for(i=startPosition;i<variable.length();i++)
	  {
		if(variable.charAt(i)==leftParanthesis)
		   braceCount++;
		else
		   break;
	  }	
	  firstVar=variable.substring(braceCount+startPosition);
	  braceCount=0;
	  for(i=firstVar.length()-1;i>0;i--)
	  {
		if(firstVar.charAt(i)==rightParanthesis)
		   braceCount++;
		else
		   break;
	   }	
	   secondVar=firstVar.substring(0,firstVar.length()-braceCount);
	   //braceCount=0;
	   for(i=0;i<secondVar.length();i++)
	   {
		 if(secondVar.charAt(i)==leftParanthesis)
		    leftBrace++;
		 else if(secondVar.charAt(i)==rightParanthesis)
		    rightBrace++;
		}
	   if(leftBrace>rightBrace)
	   {	   
		 for(i=0;i<leftBrace-rightBrace;i++)
		  secondVar+=rightParanthesis;
	   }
	   else if(leftBrace<rightBrace)
	   {
		  for(i=0;i<rightBrace-leftBrace;i++)
		    secondVar=leftParanthesis+secondVar;
	   }
	   //System.out.println("SV: "+secondVar);
	   if(result)
	   {
		 return variable.charAt(0)==operatorNot?operatorNot+secondVar:secondVar;  
	   }
	   else
		   return variable.charAt(0)==operatorNot?secondVar.substring(1,secondVar.length()):operatorNot+secondVar;
  }//REMOVEBRACES

}//CLASS

