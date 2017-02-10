package org.seal.combiningalgorithms;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.wso2.balana.Policy;
import org.wso2.balana.PolicyTreeElement;
import org.wso2.balana.Rule;
import org.wso2.balana.combine.CombinerElement;

public class function {

	// private ArrayList<MyAttr> collectTargetAttr(Target target,
	// ArrayList<MyAttr> targets) {
	// for (MyAttr myattr1 : PolicyX.getTargetAttributes(target)) {
	// boolean EXIST = false;
	// for (MyAttr child : targets) {
	// if (myattr1.getName().equals(child.getName().toString())) {
	// EXIST = true;
	// for (String element : myattr1.getDomain()) {
	// child.addValue(element);
	// }
	// }
	// }
	// if (EXIST == false) {
	// System.out.println("HERE");
	// System.out.println(sb.toString());
	// targets.add(myattr1);
	// }
	// }
	// return targets;
	// }
	//
	// private ArrayList<MyAttr> collectConditionAttr(Condition condition,
	// ArrayList<MyAttr> conditions) {
	// for (MyAttr myattr : PolicyX.getConditionAttributes(condition
	// .getExpression())) {
	// boolean EXIST = false;
	// for (MyAttr child : conditions) {
	// if (myattr.getName().equals(child.getName().toString())) {
	// EXIST = true;
	// for (String element : myattr.getDomain()) {
	// child.addValue(element);
	// }
	// }
	// }
	// if (EXIST == false)
	// conditions.add(myattr);
	// }
	// return conditions;
	// }

	// public ArrayList<MyAttr> collectAttr(Rule rule1, Rule rule2,
	// AbstractTarget target) {
	// ArrayList<MyAttr> collections = new ArrayList<MyAttr>();
	//
	// if (target instanceof Target) {
	// collections = collectTargetAttr((Target) target, collections);
	// }
	// if (rule1.getTarget() instanceof Target) {
	// collections = collectTargetAttr((Target) rule1.getTarget(),
	// collections);
	// }
	// if (rule1.getCondition() instanceof Condition) {
	// collections = collectConditionAttr(
	// (Condition) rule1.getCondition(), collections);
	// }
	// if (rule2.getTarget() instanceof Target) {
	// collections = collectTargetAttr((Target) rule2.getTarget(),
	// collections);
	// }
	// if (rule2.getCondition() instanceof Condition) {
	// collections = collectConditionAttr(
	// (Condition) rule2.getCondition(), collections);
	// }
	//
	// return collections;
	// }

	// public String generateTTRequest(Rule first, Rule second,
	// AbstractTarget target) {
	// // al.PermitOverride_FA(target, childElements);
	// function f = new function();
	// ArrayList<MyAttr> attrs = collectAttr(first, second, target);
	// f.expandValue(attrs);
	// ArrayList<int[]> combinations = null;
	// combinations = f.combination(attrs);
	// String request = null;
	// for (int[] b : combinations) {
	// request = f.generateRequest(b, attrs);
	// RequestCtxFactory rc = new RequestCtxFactory();
	// // PDPConfig pdpConfig = balana.getPdpConfig();
	// AbstractRequestCtx ar = null;
	// ;
	// try {
	// ar = rc.getRequestCtx(request);
	// } catch (ParsingException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// XACML3EvaluationCtx ec;
	//
	// ec = new XACML3EvaluationCtx(new RequestCtx(ar.getAttributesSet(),
	// ar.getDocumentRoot()), ReadPolicy.getPDPconfig());
	// if (first.evaluate(ec).getDecision() == 1
	// && second.evaluate(ec).getDecision() == 0)
	// return request;
	// // System.out.print("first: "+first.evaluate(ec).getDecision());
	// // System.out.println("    second: "+second.evaluate(ec).getDecision());
	// }
	// return null;
	//
	// }

	public ArrayList<MyAttr> expandValue(ArrayList<MyAttr> collections) {
		ArrayList<MyAttr> newversion = new ArrayList<MyAttr>();
		for (MyAttr child : collections) {
			MyAttr temp = child;
			ArrayList<String> newlist = new ArrayList<String>();
			if (temp.getDataType().contains("XMLSchema#integer")) {
				for (String element : child.getDomain()) {
					// System.out.println(child.getDomain().size());
					String a, b, c;
					a = Integer.parseInt(element) + 1 + "";
					b = Integer.parseInt(element) - 1 + "";
					c = Integer.parseInt(element) + "";
					newlist.add(a);
					newlist.add(b);
					newlist.add(c);
					// System.out.println("end");
				}
				temp.clearDomain();
				for (String s : newlist) {
					temp.addValue(s);
				}
			}
			if (temp.getDataType().contains("XMLSchema#string")) {
				String first, last;
				last = child.getDomain().get(0);
				first = last;
				for (String element : child.getDomain()) {
					if (first.compareTo(element) > 0) {
						first = element;
					}
					if (last.compareTo(element) < 0) {
						last = element;
					}
				}
				temp.addValue(last + "a ");
				if (first.length() > 1) {
					first = first.substring(0, first.length() - 1);
				}
				temp.addValue(first);
			}

			newversion.add(temp);
		}

		return newversion;
	}

	public ArrayList<int[]> combination(ArrayList<MyAttr> newversion) {
		int size = newversion.size();
		int input[] = new int[size];
		int i = 0;
		for (MyAttr child : newversion) {
			input[i] = child.getDomain().size();
			i++;
		}

		AllCombinations ac = new AllCombinations();
		return ac.getCombinations(input);
	}

	public String generateRequest(int combine[], ArrayList<MyAttr> newversion) {
		// each request is generated based on combine[];
		ArrayList<MyAttr> selected = new ArrayList<MyAttr>();
		// print2(newversion);
		ArrayList<String> value = new ArrayList<String>();
		for (int i = 0; i < combine.length; i++) {
			MyAttr temp = newversion.get(i);
			value.add(newversion.get(i).getDomain().get(combine[i]));
			selected.add(temp);
		}
		return print(selected, value);
	}

	// INT parameter for the combinations.
	public String print(ArrayList<MyAttr> newversion, ArrayList<String> value) {
		// newversion for name, category... value for value;
		// requests need to be generated after group by category;
		StringBuilder sb = new StringBuilder();
		buildHeader(sb);
		// ArrayList<MyAttr> group = null; // for print
		ArrayList<String> log = new ArrayList<String>(); // log categories which
															// already exists
		for (int j = 0; j < newversion.size(); j++) {
			if (exist(newversion.get(j).getCategory(), log)) {
				continue;
			} else {
				log.add(newversion.get(j).getCategory().toString());
				buildCategoryHead(sb, newversion.get(j)); // category header
			}
			for (int k = j; k < newversion.size(); k++) {
				if (newversion.get(k).getCategory()
						.equals(newversion.get(j).getCategory())) {
					// group = new ArrayList<MyAttr>();
					// group.add(newversion.get(k));
					buildRequest(sb, newversion.get(k), value.get(k));
				}
			}

			buildCategoryEnd(sb);
		}

		// System.out.println("----------------------------- ");
		buildEnd(sb);
		// System.out.println(sb.toString());
		return sb.toString();
	}

	public String print(ArrayList<MyAttr> newversion) {
		StringBuilder sb = new StringBuilder();
		buildHeader(sb);
		ArrayList<String> log = new ArrayList<String>();
		for (int j = 0; j < newversion.size(); j++) {
			if (exist(newversion.get(j).getCategory(), log)) {
				continue;
			} else {
				log.add(newversion.get(j).getCategory().toString());
				buildCategoryHead(sb, newversion.get(j)); // category header
			}
			for (int k = j; k < newversion.size(); k++) {
				if (newversion.get(k).getCategory()
						.equals(newversion.get(j).getCategory())) {
					buildRequest(sb, newversion.get(k), newversion.get(k)
							.getDomain().get(0));
				}
			}
			buildCategoryEnd(sb);
		}
		buildEnd(sb);
		return sb.toString();
	}
	
	public String print(ArrayList<MyAttr> newversion, boolean start, boolean end) {
		StringBuilder sb = new StringBuilder();
		if(start)
			buildHeader(sb);
		ArrayList<String> log = new ArrayList<String>();
		for (int j = 0; j < newversion.size(); j++) {
			if (exist(newversion.get(j).getCategory(), log)) {
				continue;
			} else {
				log.add(newversion.get(j).getCategory().toString());
				buildCategoryHead(sb, newversion.get(j)); // category header
			}
			for (int k = j; k < newversion.size(); k++) {
				if (newversion.get(k).getCategory()
						.equals(newversion.get(j).getCategory())) {
					buildRequest(sb, newversion.get(k), newversion.get(k)
							.getDomain().get(0));
				}
			}
			buildCategoryEnd(sb);
		}
		if(end)
			buildEnd(sb);
		return sb.toString();
	}

	//
	// public void test(List<CombinerElement> childElements, AbstractTarget
	// target) {
	// // System.out.println("*****************");
	// for (int i = 0; i < childElements.size(); i++) {
	// CombinerElement element1 = childElements.get(i);
	// PolicyTreeElement tree1 = element1.getElement();
	// Rule first = null;
	// if (tree1 instanceof Rule) {
	// first = (Rule) tree1;
	// } else {
	// continue;
	// }
	// for (int j = i; j < childElements.size(); j++) {
	// CombinerElement element2 = childElements.get(j);
	// PolicyTreeElement tree2 = element2.getElement();
	// Rule second = null;
	// if (tree2 instanceof Rule) {
	// second = (Rule) tree2;
	// } else {
	// continue;
	// }
	// //
	// ArrayList<MyAttr> attrs = collectAttr(first, second, target);
	// expandValue(attrs);
	// ArrayList<int[]> combinations = null;
	// combinations = combination(attrs);
	// String request = null;
	// for (int[] b : combinations) {
	// request = generateRequest(b, attrs);
	// RequestCtxFactory rc = new RequestCtxFactory();
	// AbstractRequestCtx ar = null;
	// ;
	// try {
	// ar = rc.getRequestCtx(request);
	// } catch (ParsingException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// XACML3EvaluationCtx ec;
	//
	// ec = new XACML3EvaluationCtx(new RequestCtx(
	// ar.getAttributesSet(), ar.getDocumentRoot()),
	// ReadPolicy.getPDPconfig());
	//
	// if (first.evaluate(ec).getDecision() != 3
	// && second.evaluate(ec).getDecision() != 3) {
	// // System.out.println(request);
	// //
	// }
	//
	// }
	// }
	// }
	// }

	public boolean allPermitRule(Policy policy) {
		List<CombinerElement> childElements = policy.getChildElements();
		for (CombinerElement rule : childElements) {
			PolicyTreeElement tree = rule.getElement();
			if (tree instanceof Rule) {
				Rule lastRule = (Rule) tree;
				if (lastRule.getEffect() == 1) {
					return false;
				}
			}
		}
		return true;
	}

	public boolean allDenyRule(Policy policy) {
		List<CombinerElement> childElements = policy.getChildElements();
		for (CombinerElement rule : childElements) {
			PolicyTreeElement tree = rule.getElement();
			if (tree instanceof Rule) {
				Rule lastRule = (Rule) tree;
				if (lastRule.getEffect() == 0) {
					return false;
				}
			}
		}
		return true;
	}

	// NING SHEN 4/30 if the last rule is default rule
	public boolean checkDefaultRule(Policy policy) {
		List<CombinerElement> childElements = policy.getChildElements();
		int size = childElements.size();
		CombinerElement lastOne = childElements.get(size - 1);
		PolicyTreeElement tree = lastOne.getElement();
		if (tree instanceof Rule) {
			Rule lastRule = (Rule) tree;
			if (lastRule.getCondition() == null && lastRule.getTarget() == null) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	
	public boolean isDefaultRule(Rule rule){
		if(rule.getCondition() == null && rule.getTarget() == null){
			return true;
		}else{
			return false;
		}
	}

	// NING SHEN 4/30 RETURN THE EFFECT OF LAST RULE(DEFAULT)
	public String DefaultEffect(Policy policy) {
		List<CombinerElement> childElements = policy.getChildElements();
		int size = childElements.size();
		CombinerElement lastOne = childElements.get(size - 1);
		PolicyTreeElement tree = lastOne.getElement();
		if (tree instanceof Rule) {
			Rule lastRule = (Rule) tree;
			if (lastRule.getEffect() == 0) {
				return "permit";
			} else {
				return "deny";
			}
		}
		return "";
	}

	public String buildHeader(StringBuilder builder) {
		builder.append("<Request xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" CombinedDecision=\"false\" ReturnPolicyIdList=\"false\">\n");
		return builder.toString();
	}

	public String buildEnd(StringBuilder builder) {
		builder.append("</Request>");
		return builder.toString();
	}

	public String buildCategoryHead(StringBuilder builder, MyAttr myattr) {
		builder.append("<Attributes Category=");
		builder.append("\"" + myattr.getCategory() + "\">\n");
		return builder.toString();
	}

	public String buildCategoryEnd(StringBuilder builder) {
		builder.append("</Attributes>\n");
		return builder.toString();
	}

	public String buildRequest(StringBuilder builder, MyAttr myattr,
			String value) {

		builder.append("<Attribute AttributeId=");
		builder.append("\"" + myattr.getName()
				+ "\" IncludeInResult = \"false\">\n");
		builder.append("<AttributeValue DataType=");
		builder.append("\"" + myattr.getDataType() + "\">" + value
				+ "</AttributeValue>\n");
		builder.append("</Attribute>\n");
		return builder.toString();

	}

	public void print2(ArrayList<MyAttr> source) {
		for (MyAttr my : source) {
			System.out.println(my.getName() + " :  ");
			for (String s : my.getDomain()) {
				System.out.println(s);
			}
		}
	}

	private boolean exist(String category, ArrayList<String> log) {
		for (String s : log) {
			if (category.equals(s)) {
				return true;
			}
		}
		return false;
	}

	public void deleteFile(File file) {
		File[] files = file.listFiles();
		if (files != null) {
			for (File deleteFile : files) {
				if (deleteFile.isDirectory()) {
					continue;
				} else {
					deleteFile.delete();
				}
			}
		}
	}
}
