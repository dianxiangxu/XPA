package org.seal.mcdc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Stack;


public class MCDCGen {

	private HashMap mcdcName  ; // attribute id in z3 and mcdc
	private MCDCConditionSet mcdcSet ;
	private String z3Exp;
	private boolean orWithSameAttributeFlag;
	private boolean andOrFlag;

	public MCDCGen(String z3Exp, HashMap nameMap) {
		
		mcdcName = nameMap;
		if(z3Exp !=null) {
			String e = convert(z3Exp);
			mcdcSet = new MCDCConditionSet(e,false);
			this.z3Exp = z3Exp;
			if(e.indexOf("&") < 0){
				orWithSameAttributeFlag = true;
			} else {
				orWithSameAttributeFlag = false;
				if(e.indexOf("|")>=0) {
					andOrFlag = true;;
				}
			}
		}
	}
	
	public  void initializeAttributeMap() {
		mcdcName = new HashMap();
	}
	
	public  String getValue(String key) {
		if(mcdcName.containsKey(key)) {
			return mcdcName.get(key).toString();
		} else {
			return null;
		}
	}
	public boolean isAndOr() {
		return andOrFlag;
	}
	public boolean isMCDCFeasible() {
		if(mcdcSet.getConditionSet().size()>2) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean isOrWithSameAttributeExp() {
		return orWithSameAttributeFlag;
	}
	
	public List<String> getCases(){
		if(isOrWithSameAttributeExp()) {
			List<String> lst = new ArrayList<String>();
			for(String m:mcdcSet.getPositiveConditions()) {
				lst.add(convertToZ3(m.toString(), z3Exp));
			}
			return lst;
		}
		else {
		List<String> lst = new ArrayList<String>();
		for(MCDCCondition m:mcdcSet.getConditionSet()) {
			lst.add(convertToZ3(m.toString(), z3Exp));
		}
		return lst;
	}
	}
	
	public List<String> getConditionSet(){
		if(isOrWithSameAttributeExp()) {
			return mcdcSet.getPositiveConditions();
		} else {
			List<String> lst = new ArrayList<String>();
			for(MCDCCondition m: mcdcSet.getConditionSet()) {
				lst.add(m.toString());
			}
			return lst;
		}
	}
	
	public MCDCConditionSet getMCDCConditionSet(){
		return mcdcSet;
	}
	
	
	
	public List<String> getPositiveCases(){
		List<String> lst = new ArrayList<String>();
		for(String m:mcdcSet.getPositiveConditions()) {
			lst.add(convertToZ3(m.toString(), z3Exp));
		}
		return lst;
	}
	

	public List<String> getNegativeCases(){
		List<String> lst = new ArrayList<String>();
		for(String m:mcdcSet.getNegativeConditions()) {
			lst.add(convertToZ3(m.toString(), z3Exp));
		}
		return lst;
	}
	
	public HashMap getMap() {
		return this.mcdcName;
	}

	private String getName(String name, String expression) {
		if(mcdcName.containsValue(expression)) {
			for(Object key:mcdcName.keySet()) {
				if(mcdcName.get(key).equals(expression)) {
					return key.toString();
				}
			}
		}
		boolean has = true;
		if (!mcdcName.containsKey(name)) {
			mcdcName.put(name, expression);
			return name;
		} else {
			StringBuffer sb = new StringBuffer();
			do {
				sb = new StringBuffer();
				String base = "abcdefghijklmnopqrstuvwxyz";
				Random random = new Random();
				for (int i = 0; i < 6; i++) {
					int number = random.nextInt(base.length());

					sb.append(base.charAt(number));
				}
				if (!mcdcName.containsValue(sb.toString())) {
					has = false;
				}
			} while (has == true);
			mcdcName.put(sb.toString(), expression);
			return sb.toString();
		}
	}

	public static void test(boolean[] arr) {
		arr[0]=true;
	}
	
	public static void main(String args[]) {
		String input = "(and (or  (and  (= abcde \"IT\") (= mdiay \"HR\")) (and (= efgcg \"GH\") (= mdiay \"HR\"))))";
		
		MCDCGen m = new MCDCGen(input,new HashMap() );
		for(String s:m.getCases()) {
			System.out.println();
		}
		
		boolean [] flags = new boolean[2];
		flags[0]=flags[1]=false;
		test(flags);
		System.out.println(flags[0]);
		//		String input = "(or  (= abcde IT) (= adfdfe TT))";
//		
//		System.out.println(input);
//		MCDCGen mcdc = new MCDCGen();
//		String out = mcdc.convert(input);
//		System.out.println("result: " + out);
//
//		MCDCConditionSet mcset = new MCDCConditionSet(out,false);
//		// System.out.println(mcset.getConditionSet().size() + "result set");
//		// System.out.println(mcset.getConditionSet().get(0).toString());
//		System.out.println("positives     "
//				+ mcset.getConditionSet().get(0).toString());
//		System.out.println("positives     "
//				+ mcset.getConditionSet().get(1).toString());
//
//		String in = "!bb && !ks && sd";
//		List<String> list = mcset.getPositiveConditions();
//		for(String m:list) {
//			System.out.println(mcdc.convertToZ3(m.toString(), input));
//		}

//		String input = "(or (and (= sd  sd) (or (= sd \"127.0.0.1\") (= bb 12312))))";
//		System.out.println(input);
//		MCDC_converter2 mcdc = new MCDC_converter2();
//		System.out.println("result: " + mcdc.convert(input));
//
//		MCDCConditionSet mcset = new MCDCConditionSet(mcdc.convert(input),false);
//		// System.out.println(mcset.getConditionSet().size() + "result set");
//		// System.out.println(mcset.getConditionSet().get(0).toString());
//		System.out.println("positives     "
//				+ mcset.getConditionSet().get(0).toString());
//		System.out.println("positives     "
//				+ mcset.getConditionSet().get(1).toString());
//
//		input = "!bb && !ks && sd";
//		System.out
//				.println(mcdc
//						.reverseConvert(input,
//								"(or (and (= sd  sd) (or (= ks \"127.0.0.1\") (= bb 12312))))"));

	}

	public String convert(String input) {
		String[] inputArray = input.split(" ");
		ArrayList<String> inputList = new ArrayList<String>();
		for (int i = 0; i < inputArray.length; i++) {
			if (inputArray[i].trim().equals("")) {
				continue;
			}
			// System.out.println(inputArray[i]);
			inputList.add(inputArray[i]);
		}
		// System.out.println("size " + inputList.size());

		StringBuffer sb = new StringBuffer();
		sb = logicList(inputList);
		return sb.toString();

	}

	
	public String convertToZ3(String mcdc, String input) {
		String[] tokens = mcdc.split("\\s+");
		
		for(String token:tokens) {
			if(token.indexOf("!")>=0) {
				String key = token.replaceAll("!", "");
				String notVal = "not " + mcdcName.get(key);
				input = input.replaceAll(mcdcName.get(key).toString(), notVal);
			}
		}
		
		return input;
	}

	private StringBuffer logicList(ArrayList<String> inputs) {

		Stack<String> op = new Stack<String>();
		StringBuffer sb = new StringBuffer();
		// StringBuffer sub = new StringBuffer();
		for (int i = 0; i < inputs.size(); i++) {
			if (inputs.get(i).equals("(or")) {
				op.push("or");
				sb.append("( ");
				continue;
			} else if (inputs.get(i).equals("(and")) {
				op.push("and");
				sb.append("( ");
				continue;
			} else if (inputs.get(i).equals("(not")) {
				op.push("not");
				sb.append("( ");
				continue;
			} else if (inputs.get(i).startsWith("(")) {
				String name = inputs.get(i + 1);
				String suffix = inputs.get(i + 2);
				int ks = i + 3;
				// int p = 3;

					while (!suffix.endsWith(")") && ks < inputs.size()) {
						suffix = inputs.get(ks);
						ks = ks + 1;
					}
					StringBuffer su = new StringBuffer();
					int add = 0;
					while(ks > (i+2+add)){
						su.append(inputs.get(i+2+add));
						su.append(" ");
						add++;
					}
					suffix = su.toString();
				int num = checkNum(suffix, ')');
				// System.out.println("suffix :  " +
				// suffix.substring(0,suffix.trim().length()-num) );
				String expression = inputs.get(i) + " " + name + " "
						+ suffix.substring(0, suffix.length() - num);
				// System.out.println("Exp  :  " + expression);
				if (!expression.endsWith(")")) {
					expression = expression + ")";
				}
				while(checkNum(expression, ')')>1){
					expression = expression.substring(0,expression.length()-1);
				}
				System.out.println("expression : " + expression);
				name = getName(name, expression);

				if (!op.isEmpty()) {
					if (op.peek().equals("not")) {
						sb.append(getOperator(op.peek()) + " ");
						sb.append(name + " ");
					} else {
						sb.append(name + " ");
						if ((i + 1 < inputs.size() - 1) && num == 1) {
							sb.append(getOperator(op.peek()) + " ");
						}

					}

				}
				boolean ending = false;
				while (num - 1 > 0) {
					ending = true;
					op.pop();
					sb.append(")");
					num--;
				}
				if (!op.isEmpty() && ending) {
					sb.append(getOperator(op.peek()));
				}
			}

		}

		return sb;
	}

	private String getOperator(String input) {
		if (input.equals("not")) {
			return "!";
		} else if (input.equals("and")) {
			return "&&";
		} else if (input.equals("or")) {
			return "||";
		}

		return "";
	}

	private int checkNum(String input, char key) {
		int number = 0;
		for (int i = 0; i < input.length(); i++) {
			if (input.charAt(i) == key) {
				number++;
			}
		}
		return number;

	}

	public String reverseConvert(String mcdc, String z3input) {
		StringBuffer sb = new StringBuffer();
		String[] temp = mcdc.split(" ");
		ArrayList<String> result = new ArrayList<String>();
		for (int i = 0; i < temp.length; i++) {
			if (temp[i].trim().equals("") || temp[i].equals("&&")
					|| temp.equals("||")) {
				continue;
			} else {
				result.add(temp[i]);
			}
		}

		ArrayList<String> inputList = new ArrayList<String>();
		temp = z3input.split(" ");
		for (int i = 0; i < temp.length; i++) {
			if (temp[i].trim().equals("")) {
				continue;
			}
			// System.out.println(inputArray[i]);
			inputList.add(temp[i]);
		}

		sb.append("(and ");
		for (String s : result) { // result from mcdc
			boolean isNot = false;
			String name = "";
			if (s.startsWith("!")) {
				name = s.substring(1);
				isNot = true;
			} else {
				name = s;
			}
			//

			for (int i = 0; i < inputList.size(); i++) { // token from z3 input
				if (inputList.get(i).equals(name)) {
					if (isNot) {
						sb.append("(not ");
					}
					sb.append(inputList.get(i - 1) + " ");
					sb.append(inputList.get(i) + " ");
					sb.append(removeBracet(inputList.get(i + 1)) + " ");
					sb.append(")");
					if (isNot) {
						sb.append(")");
					}
				}
			}
		}
		sb.append(")");
		return sb.toString();
	}

	private String removeBracet(String ending) {
		int position = 0;
		for (int i = ending.length() - 1; i >= 0; i--) {
			if (ending.charAt(i) == ')') {
				continue;
			} else {
				position = i;
				break;
			}
		}
		return ending.substring(0, position + 1);
	}

	
}
