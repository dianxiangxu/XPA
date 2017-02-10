package org.seal.mcdc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.table.DefaultTableModel;

import tcg.SAdaptor;

public class MCDCConditionSet {

	public static final String    valueZero  = "0";
	public static final String    valueOne   = "1";
	public static final Character zero       = '0';
	public static final Character one        = '1';
	
	
	private ArrayList<MCDCCondition> conditionSet;

	public MCDCConditionSet(String expression, boolean UniqueCaseMCDC) {
		if(UniqueCaseMCDC) // from Jian Zhang's group at Chinese Academy of Science
		{
			SAdaptor adp = new SAdaptor(expression);
	        HashMap<String, Boolean> testcases = adp.genTestCases();
	        this.conditionSet = new ArrayList<MCDCCondition>();
	        Iterator it = testcases.entrySet().iterator();
	        while(it.hasNext()) {
	            Map.Entry e = (Map.Entry) it.next();
	            String s = (String) e.getKey();
	            Boolean b = (Boolean) e.getValue();
	            MCDCCondition mc = new MCDCCondition(s, b);
	            this.conditionSet.add(mc);
	        }
		}else{ 	// from Xu's group at NDSU (Rajendar Gangannagari)
			generateConditionSet(expression);
		}
	}
	

	public void generateConditionSet(String expressionIn) {
		String expression  =  simplifyExpression(expressionIn);
	    if(expression != "")     {  
			ConvertToPostfix  ctp =  new ConvertToPostfix();
	    	ArrayList<String> postfix = ctp.GeneratePostfixForm(expression);
			DefaultTableModel MCDCTestCases = new TestCaseGenerator().generateTestCases(postfix);
	    	conditionSet = TestTupleGenerator.generateTestConditions(postfix,MCDCTestCases);
	    }
	}
  
	public static String simplifyExpression(String expressionIn) {
		String expression = "";
		ConvertToPostfix     ctp   =  new ConvertToPostfix();
		ConvertToExpression  cte   =  new ConvertToExpression();
		//MinimizedTable       mt    =  null;
		SimplifyExpression   se    =  new SimplifyExpression();
		ArrayList<String>    postfix   =  ctp.GeneratePostfixForm(expressionIn);
		String tempExpression   =  se.simplify(postfix);
		//System.out.println("tempExpression: "+tempExpression);
		postfix.clear();
		expression = minimize(tempExpression);
		if(expression.equals(valueZero)|| expression.equals(valueOne))
		  return expression; 
		else
		{
			expression = cte.putNots(expression);
			expression = minimize(expression);
			
			if(expression.equals(valueZero)|| expression.equals(valueOne))
		     return expression; 
//	System.out.println("expression: "+expression);
		    postfix    = ctp.GeneratePostfixForm(expression);
		    expression = se.simplify(postfix);
			if(expression.equals(valueOne))
				return valueOne;
			return expression; 
		}	
		//return expression;
	}
	public static String minimize(String expression)
	{
		ConvertToExpression  cte   =  new ConvertToExpression();
		MinimizedTable       mt    =  null;
		ConvertToPostfix     ctp   =  new ConvertToPostfix();
		
		ArrayList<String>    postfix   =  new ArrayList<String>();
		postfix  =  cte.ChangeFormat(expression.trim());
		     mt  =  new MinimizedTable(postfix);
	    String result = mt.toString();
	    
	    if( result.charAt(0) == zero || result.charAt(0) == one)
	    {
	    	return new Character(result.charAt(0)).toString();
	    }

	    postfix.clear();
	    postfix    =  cte.storeInArrayList(result);
		expression = ctp.removeDuplicates(postfix);

		if(expression == "") return valueZero;
		else return expression;
		
	}//MINIMIZE

	public ArrayList<MCDCCondition> getConditionSet() {
		return conditionSet;
	}

	public ArrayList<String> getPositiveConditions() {
		ArrayList<String> positiveConditions = new ArrayList<String>();
		for (MCDCCondition mcdcCondition: conditionSet) {
			if (mcdcCondition.isPositive())
				positiveConditions.add(mcdcCondition.getCondition());
		}
		return positiveConditions;
	}

	public ArrayList<String> getNegativeConditions() {
		ArrayList<String> negativeConditions = new ArrayList<String>();
		for (MCDCCondition mcdcCondition: conditionSet) {
			if (!mcdcCondition.isPositive())
				negativeConditions.add(mcdcCondition.getCondition());
		}
		return negativeConditions;
	}

	public void printConditions(){
		for(int i=0;i<conditionSet.size(); i++)
			 System.out.println(conditionSet.get(i));
	}

	public static void main(String[] args) {
		//MCDCConditionSet set = new MCDCConditionSet("amt==100");
		//MCDCConditionSet set = new MCDCConditionSet("amt<=100");
		//MCDCConditionSet set = new MCDCConditionSet("amt>=100");
		//MCDCConditionSet set = new MCDCConditionSet("amt!=100");
		//MCDCConditionSet set = new MCDCConditionSet("amt()");
		//MCDCConditionSet set = new MCDCConditionSet("amt>100");
		
		//MCDCConditionSet set = new MCDCConditionSet("amt>100 || amt<1000");
		
		//MCDCConditionSet set = new MCDCConditionSet("amt>=0 && getBalance()-amt>=0");
		
		//MCDCConditionSet set = new MCDCConditionSet("amt>100 && amt<1000");
		//MCDCConditionSet set = new MCDCConditionSet("amt>100 || balance>1000 || interest ==5");
		//MCDCConditionSet set = new MCDCConditionSet("amt>100 && balance>1000 && interest ==5");
		//MCDCConditionSet set = new MCDCConditionSet("amt>100 && balance>1000 && interest()");
		//MCDCConditionSet set = new MCDCConditionSet("amt>100 && balance>1000 || interest()");
		//MCDCConditionSet set = new MCDCConditionSet("amt>100 && (balance>1000 || interest())");
		//MCDCConditionSet set = new MCDCConditionSet("amt>100 || balance>1000 && interest()");
		//MCDCConditionSet set = new MCDCConditionSet("amt>100 ||(balance>1000 && interest())");
		//MCDCConditionSet set = new MCDCConditionSet("(amt>100 || amt<1000) ||  (balance>1000 || interest())");
		//MCDCConditionSet set = new MCDCConditionSet("(amt>100 && amt<1000)&&  (balance>1000 && interest())");
		//MCDCConditionSet set = new MCDCConditionSet("(amt>100 && amt<1000) ||  (balance>1000 && interest())");
		//MCDCConditionSet set = new MCDCConditionSet("(amt>100 || amt<1000) &&  (balance>1000 || interest())");
		//MCDCConditionSet set = new MCDCConditionSet("amt>=0 && getBalance()-amt>=0 && getBalance()>1000");
		//set.printConditions();
			
		//MCDCConditionSet set = new MCDCConditionSet("x>=0 && (x<10 || x>=100)");
		//MCDCConditionSet set = new MCDCConditionSet("x>=0 && x<10 || x>=100");
		//MCDCConditionSet set = new MCDCConditionSet("!(a>0) && !(b>0)");
		//MCDCConditionSet set = new MCDCConditionSet("!(x<0) && !(x>=10 && x<100)");
		//MCDCConditionSet set = new MCDCConditionSet("(x>=0 && x<10) ||(x>=0 && x>=100)");
		//MCDCConditionSet set = new MCDCConditionSet("a&&!a");
		//MCDCConditionSet set = new MCDCConditionSet("getBalance()-amt<0 && getBalance()-amt>=0 || amt<0");
		//MCDCConditionSet set = new MCDCConditionSet("amt>=0 &&   getBalance()-amt>=0 ");
		//MCDCConditionSet set = new MCDCConditionSet("!(amt>=0 && getBalance()-amt>=0) && !(amt>=0 && getBalance()-amt<0)");				
		
		//MCDCConditionSet set = new MCDCConditionSet("(a || b ) && (a ||!b) && (c || d ) && (c ||!d)");				
		//MCDCConditionSet set = new MCDCConditionSet("c&&d||c&&e||a&&c||a&&b");
		
		//MCDCConditionSet set = new MCDCConditionSet("a&&b||a&&c");
		//MCDCConditionSet set = new MCDCConditionSet("a>=0&&a<0 && c ");
		//MCDCConditionSet set = new MCDCConditionSet("!(b>0)&&!(b<0&&b>-1000)");
		//MCDCConditionSet set = new MCDCConditionSet("b!=0&&b<10&&a>0&&a<0&&c");
		
		//MCDCConditionSet set = new MCDCConditionSet("x>=0 && x<10 || x>=100");
		//MCDCConditionSet set = new MCDCConditionSet("(amt>=0 || b_1>0) && (amt>=0 || B_1<=0)");				
		//MCDCConditionSet set = new MCDCConditionSet("!(amt>=0 && getBalance()-amt>=0) && !(amt>=0 && getBalance()-amt<0) && !(amt>=0 && getBalance()-amt<0)");

		MCDCConditionSet set = new MCDCConditionSet("( ( ( rhfqd && lfnao && senrn )))",false);
		set.printConditions();
		
		
         String s = "";
		 s = "a";
		 //s = "a&&b";
		 //s = "a&&b&&c";
		 //s = "!a>100";
		 //s = "a<10 && a<100";
		 //s = "a<10 && a>100";
		 //s = "!(b+amt>=0)||!(b+amt<0)";
		 //s = "!(b-amt>=0)&&!(b-amt<0&&b-amt>-1000)";
		 //s = "amt>=0 && getBalance()-amt<0 || amt>=0 && getBalance()-amt>=0";
		 //s = "(amt>=0&&getBalance()-amt<0) ||(amt>=0&&getBalance()-amt>=0)";
		 //s = "(a&&b>0||a&&b<=0||a&&c||d&&e)";
		 //s = "(amt>=0||getBalance()-amt<0)&&(amt>=0||getBalance()-amt>=0)";
		 //s = "a && !a";
		 //s = "a>10||!a>10";
		 //s = "(a>10||a>=100)&&(a>0||a>=0)";
		 //s = "a>0 || a<=0";
		 //s = "a>0||!a>0";
		 //s = "x>=0 && (x<10 || x>=100)";
		 //s = "a>0||(a>=0&&b)||a<=100)";
		 //s = "(b()>10||a>=100)&&(a>0||a>=0)";
		 //s = "a&&b<0&&b>0";
		 //s = "a&&b<0&&b<=0";
		 //s = "(c>0||c<100)&&(k>0||k<=0)&&c&&d";
	     //s = "a&&b||c>0||c<=0||k";
		 //s = "!(a>0||!a>0)";
		 //s = "!(a>0&&!a>0)";
		 //s = "(a||!a)&&(b||!b)";
		 //s = "c>0||a&&b||c<=0";
		 //s = "(c>0||c==0)&&a&&b||c<0"; 
		 //s = "(a>0||a<=0)&&(a>10||a>=100)";
		 //s = "a>0&&a<100 ||c>10&&c>=100 || e"; //testitproper
		 //s = "a>0&&a<=0 || a>10&&a>=100 || c&&d ";
	 	 //s ="(a>0&&a>100)||(a>1000&&a>10)";
		 //s = "(a&&b)||(c&&d)||(e&&f)||(g&&h)";
		 //s = "a||(b&&c)";
		 //s = "(a||b)&&c";
		 //s = "a || amt>=0 && getBalance()-amt<0 || amt>=0 && getBalance()-amt>=0";
         s = "a||b ||c&&d&&e";
         //s ="amt<0 || amt>=0 && getBalance()-amt<0 || amt>=0 && getBalance()-amt>=0";
         //s = "a||b&&c";
         //s ="amt<0||amt>=0 && getBalance()-amt<0 || amt>=0 && getBalance()-amt>=0";
		 //s ="(amt>=0 || getBalance()-amt<0) && (amt>=0 || getBalance()-amt>=0)";
		 //s = "b>0&&c>0||a>0&&a<0||x";
		 s = "a&&!a";
		 //s = "!((a&&b>0)||(a&&b<0))";
         s = "!((amt>=0 && getBalance()-amt<0) || (amt>=0 && getBalance()-amt>=0))";
         s = "amt>=0 && getBalance()-amt<0 || amt>=0 && getBalance()-amt>=0";
          s ="(amt<0 && getBalance()-amt<0) || (amt<0 && getBalance()-amt>0)";
		 System.out.println("exp: "+ simplifyExpression(s));
	}
}








