package org.seal.combiningalgorithms;
/* old version, ignore
This is a commit test... Izzat... Test
 * 
 */
import java.util.ArrayList;
import java.util.List;

import org.wso2.balana.PolicyTreeElement;
import org.wso2.balana.Rule;
import org.wso2.balana.combine.CombinerElement;

public class algorithm {

	public static final int INT = 1;
	public static final int STRING = 2;
	public static final int BOOLEAN = 3;
	public static final int DATA = 4;
	public static final int TIME = 5;
	public static final int IPADDRESS = 6;

	public static final int EQUAL = 1;
	public static final int LESS = 2;
	public static final int GREATER = 3;
	public static final int EQUAL_LESS = 4;
	public static final int EQUAL_GREATER = 5;
	public static final int STRING_IN = 6;

	public int checkType(MyAttr myattr) {
		if (myattr.getDataType().contains("string")) {
			return this.STRING;
		}
		if (myattr.getDataType().contains("integer")) {
			return this.INT;
		}
		if (myattr.getDataType().contains("boolean")) {
			return this.BOOLEAN;
		}
		if (myattr.getDataType().contains("date")) {
			return this.DATA;
		}
		if (myattr.getDataType().contains("time")) {
			return this.TIME;
		} else {
			return -1;
		}

	}
	
	public String returnFunction(String function){
		if (function.contains("equal") && !function.contains("greater")
				&& !function.contains("less")) {
			return "=";
		} else if (function.contains("greater") && !function.contains("equal")) {
			return ">";
		} else if (function.contains("less") && !function.contains("equal")) {
			return "<";
		} else if (function.contains("less") && function.contains("equal")) {
			return "<=";
		} else if (function.contains("greater") && function.contains("equal")) {
			return ">=";
		} else if (function.contains("not")){
			return "=";
		} else if (function.contains("at-least-one-member-of")){
			return "=";
		}
		
		else {
			return "=";
		}
	}

	public int checkFunction(String function) {
		if (function.contains("equal") && !function.contains("greater")
				&& !function.contains("less")) {
			return EQUAL;
		} else if (function.contains("greater") && !function.contains("equal")) {
			return GREATER;
		} else if (function.contains("less") && !function.contains("equal")) {
			return LESS;
		} else if (function.contains("less") && function.contains("equal")) {
			return EQUAL_LESS;
		} else if (function.contains("greater") && function.contains("equal")) {
			return EQUAL_GREATER;
		} else {
			return -1;
		}
	}


	public String getIndeterminate(MyAttr myattr) {
		return (myattr.getName() + "--TestIndeterminate--");
	}

	// check whether the attribute name have already have value.
	public boolean isExist(ArrayList<MyAttr> generation, MyAttr childAttr) {
		for (MyAttr it : generation) {
			if (it.getName().equals(childAttr.getName())) {
				return true;
			}
		}
		return false;
	}
	
	
/*
	// to check whether 2 rules can get different values.
	public boolean checkConflict(Rule rule1, Rule rule2) {
		ArrayList<MyAttr> target1 = new ArrayList<MyAttr>();
		ArrayList<MyAttr> condition1 = new ArrayList<MyAttr>();
		ArrayList<MyAttr> target2 = new ArrayList<MyAttr>();
		ArrayList<MyAttr> condition2 = new ArrayList<MyAttr>();

		if (rule1.getTarget() instanceof Target) {
			for (MyAttr myattr1 : PolicyX.getTargetAttributes((Target) rule1.getTarget())) {
				target1.add(myattr1);
			}
		}
		for (MyAttr myattr : PolicyX.getConditionAttributes(rule1.getCondition().getExpression())) {
			condition1.add(myattr);
		}
		if (rule2.getTarget() instanceof Target) {
			for (MyAttr myattr1 : PolicyX.getTargetAttributes((Target) rule2.getTarget())) {
				target2.add(myattr1);
			}
		}
		if (rule2.getCondition() instanceof Condition) {
			for (MyAttr myattr : PolicyX.getConditionAttributes(rule2.getCondition().getExpression())) {
				condition2.add(myattr);
			}
		}
		for (MyAttr mylist : target1) { // if
			boolean allsame = true;
			for (MyAttr mylist2 : condition2) {
				boolean existsame = false;
				if (mylist.getName().equals(mylist2.getName().toString())) {
					existsame = true;
				}
				if (existsame == false) {
					allsame = false;
				}
			}
			if (allsame) {
				return true;
			}
		}

		if (condition1.equals(condition2)) {
			return true;
		}
		return false;
	}
*/
	// NING SHEN 5/19, based on DenyOverride_FA;
	/*
	public String PermitOverride_FA(AbstractTarget target,
			List<CombinerElement> childElement) {
		ArrayList<MyAttr> generation = new ArrayList<MyAttr>();
		if (target instanceof Target) {
			for (MyAttr myattr1 : PolicyX.getTargetAttributes((Target) target)) {
				if (!isExist(generation, myattr1)) {
					myattr1.setDomain(getTrueValue(myattr1));
					generation.add(myattr1);
				}
			}
		}
		if (allPermit(childElement) || allDeny(childElement)) { // all P/D, one
			// System.out.println("goes here"); // I one P/D
			if(childElement.size() == 1){
				return "Can not find request";
			}
			for (int i = 0; i < childElement.size() - 1; i++) {
				Rule rule1 = null;
				Rule rule2 = null;
				PolicyTreeElement tree1 = childElement.get(i).getElement();
				if (tree1 instanceof Rule) {
					rule1 = (Rule) tree1;
				}
				for (int j = i + 1; j < childElement.size(); j++) {
					PolicyTreeElement tree2 = childElement.get(j).getElement();
					if (tree1 instanceof Rule) {
						rule2 = (Rule) tree2;
					}
					if (!checkConflict(rule1, rule2)) {
						for (MyAttr myattr : PolicyX.getTargetAttributes((Target) rule1.getTarget())) {
							myattr.setDomain(getTrueValue(myattr));
							generation.add(myattr);
						}
						for (MyAttr myattr : PolicyX.getConditionAttributes(rule1.getCondition().getExpression())) {
							myattr.setName(getIndeterminate(myattr));
							myattr.setDomain(getTrueValue(myattr));
							generation.add(myattr);
						}
						for (MyAttr myattr : PolicyX.getTargetAttributes((Target) rule2.getTarget())) {
							myattr.setDomain(getTrueValue(myattr));
							generation.add(myattr);
						}
						for (MyAttr myattr : PolicyX.getConditionAttributes(rule2.getCondition().getExpression())) {
							myattr.setDomain(getTrueValue(myattr));
							generation.add(myattr);
						}
						print(generation);
						return "";
					}
				}

			}
		} else {
			for (int i = 0; i < childElement.size() - 1; i++) {
				Rule rule1 = null;
				Rule rule2 = null;
				PolicyTreeElement tree1 = childElement.get(i).getElement();
				if (tree1 instanceof Rule) {
					rule1 = (Rule) tree1;
				}
				if (rule1.getEffect() == 1) {
					for (int j = i + 1; j < childElement.size(); j++) { // inner
																		// loop
																		// only
																		// when
																		// first
																		// is
																		// permit
						PolicyTreeElement tree2 = childElement.get(j)
								.getElement();
						if (tree2 instanceof Rule) {
							rule2 = (Rule) tree2;
						}
						if (rule2.getEffect() == 0) { // if second is deny
							if (!checkConflict(rule1, rule2)) { // if no
																// conflict
								if (rule1.getTarget() instanceof Target) {
									for (MyAttr myattr : PolicyX.getTargetAttributes((Target) rule1.getTarget())) {
										myattr.setDomain(getTrueValue(myattr));
										generation.add(myattr);
									}
								}
								if (rule1.getCondition() instanceof Condition) {
									for (MyAttr myattr : PolicyX.getConditionAttributes(rule1.getCondition().getExpression())) {
										myattr.setDomain(getTrueValue(myattr));
										// System.out.println(myattr.getDomain().get(0));
										generation.add(myattr);
									}
								}
								if (rule2.getTarget() instanceof Target) {
									for (MyAttr myattr : PolicyX.getTargetAttributes((Target) rule2.getTarget())) {
										myattr.setDomain(getTrueValue(myattr));
										generation.add(myattr);
									}
								}
								if (rule2.getCondition() instanceof Condition) {
									for (MyAttr myattr : PolicyX.getConditionAttributes(rule2.getCondition().getExpression())){
										myattr.setDomain(getTrueValue(myattr));
										generation.add(myattr);
									}
								}
								print(generation); // meet all requirement,
													// return one
								return "";
							}

						}
					} // end for second for loop
				}
			}
		}
		// TODO
		return "Can not find";
	}

	// NING SHEN 5/15
	public String DenyOverride_FA(AbstractTarget target,
			List<CombinerElement> childElement) {
		ArrayList<MyAttr> generation = new ArrayList<MyAttr>();

		if (target instanceof Target) {
			for (MyAttr myattr1 : PolicyX.getTargetAttributes((Target) target)) {
				if (!isExist(generation, myattr1)) {
					myattr1.setDomain(getTrueValue(myattr1));
					generation.add(myattr1);
				}
			}
		}
		if (allPermit(childElement) || allDeny(childElement)) { // all P/D, one
			// System.out.println("goes here"); // I one P/D
			for (int i = 0; i < childElement.size() - 1; i++) {
				Rule rule1 = null;
				Rule rule2 = null;
				PolicyTreeElement tree1 = childElement.get(i).getElement();
				if (tree1 instanceof Rule) {
					rule1 = (Rule) tree1;
				}
				for (int j = i + 1; j < childElement.size(); j++) {
					PolicyTreeElement tree2 = childElement.get(j).getElement();
					if (tree1 instanceof Rule) {
						rule2 = (Rule) tree2;
					}
					if (!checkConflict(rule1, rule2)) {
						for (MyAttr myattr : PolicyX.getTargetAttributes((Target) rule1.getTarget())) {
							myattr.setDomain(getTrueValue(myattr));
							generation.add(myattr);
						}
						for (MyAttr myattr : PolicyX.getConditionAttributes(rule1.getCondition().getExpression())) {
							myattr.setName(getIndeterminate(myattr));
							myattr.setDomain(getTrueValue(myattr));
							generation.add(myattr);
						}
						for (MyAttr myattr : PolicyX.getTargetAttributes((Target) rule2.getTarget())) {
							myattr.setDomain(getTrueValue(myattr));
							generation.add(myattr);
						}
						for (MyAttr myattr : PolicyX.getConditionAttributes(rule1.getCondition().getExpression())) {
							myattr.setDomain(getTrueValue(myattr));
							generation.add(myattr);
						}
						print(generation);
						return "";
					}
				}

			}
		} else { // has permit and deny
			for (int i = 0; i < childElement.size() - 1; i++) {
				Rule rule1 = null;
				Rule rule2 = null;
				PolicyTreeElement tree1 = childElement.get(i).getElement();
				if (tree1 instanceof Rule) {
					rule1 = (Rule) tree1;
				}
				if (rule1.getEffect() == 0) {
					for (int j = i + 1; j < childElement.size(); j++) { // inner
																		// loop
																		// only
																		// when
																		// first
																		// is
																		// permit
						PolicyTreeElement tree2 = childElement.get(j)
								.getElement();
						if (tree2 instanceof Rule) {
							rule2 = (Rule) tree2;
						}
						if (rule2.getEffect() == 1) { // if second is deny
							if (!checkConflict(rule1, rule2)) { // if no
																// conflict
								if (rule1.getTarget() instanceof Target) {
									for (MyAttr myattr : PolicyX.getTargetAttributes((Target) rule1.getTarget())) {
										myattr.setDomain(getTrueValue(myattr));
										generation.add(myattr);
									}
								}
								if (rule1.getCondition() instanceof Condition) {
									for (MyAttr myattr : PolicyX.getConditionAttributes(rule1.getCondition().getExpression())){
										myattr.setDomain(getTrueValue(myattr));
										// System.out.println(myattr.getDomain().get(0));
										generation.add(myattr);
									}
								}
								if (rule2.getTarget() instanceof Target) {
									for (MyAttr myattr : PolicyX.getTargetAttributes((Target) rule2.getTarget())) {
										myattr.setDomain(getTrueValue(myattr));
										generation.add(myattr);
									}
								}
								if (rule2.getCondition() instanceof Condition) {
									for (MyAttr myattr : PolicyX.getConditionAttributes(rule1.getCondition().getExpression())) {
										myattr.setDomain(getTrueValue(myattr));
										generation.add(myattr);
									}
								}
								print(generation); // meet all requirement,
													// return one
								return "";
							}

						}
					} // end for second for loop
				}
			}
		}

		return " can not find";
	}

	// NING SHEN 5/17
	public String FA_Unless(AbstractTarget target,
			List<CombinerElement> childElement) {
		ArrayList<MyAttr> generation = new ArrayList<MyAttr>();
		// if only have one default?

		if (target instanceof Target) { // no matter what, satisfy target
			for (MyAttr myattr1 : PolicyX.getTargetAttributes((Target) target)) {
				if (!isExist(generation, myattr1)) {
					myattr1.setDomain(getTrueValue(myattr1));
					generation.add(myattr1);
				}
			}
		}
		if (hasDefaultRule(childElement) && childElement.size() == 1) {
			return "No way"; // Only have default rule;
		}

		CombinerElement element = childElement.get(0);
		PolicyTreeElement tree = element.getElement();
		if (tree instanceof Rule) {
			Rule myrule = (Rule) tree;
			if (myrule.getTarget() instanceof Target) {
				for (MyAttr myattr : PolicyX.getTargetAttributes((Target) myrule.getTarget())) {
					myattr.setDomain(getTrueValue(myattr));
					generation.add(myattr);
				}
			}
			for (MyAttr myattr : PolicyX.getConditionAttributes(myrule.getCondition().getExpression())) {
				myattr.setName(getIndeterminate(myattr));
				generation.add(myattr);
			}
			print(generation);
			return "";
		}
		return "error";
	}

	// NING SHEN 1/19
	public String PermitOverride_PermitUnlessDeny(AbstractTarget target,
			List<CombinerElement> childElements) {
		ArrayList<MyAttr> generation = new ArrayList<MyAttr>();// same as
																// DenyO/DenyU
		ArrayList<MyAttr> generation2 = new ArrayList<MyAttr>();

		ArrayList<String> AttributeName = new ArrayList<String>();// the
																	// collection
																	// to
																	// generate
																	// faked
																	// attributes
		if (target instanceof Target) {
			for (MyAttr myattr1 : PolicyX.getTargetAttributes((Target) target)) {
				if (!isExist(generation, myattr1)) {
					myattr1.setDomain(getTrueValue(myattr1));
					generation.add(myattr1);
					AttributeName.add(myattr1.getName());
				}
			}
		}
		if (hasDefaultRule(childElements)
				&& DefaultEffect(childElements).equals("deny")) { // pick one
																	// permit
			if (allDeny(childElements)) {
				return "No way";
			} else {

			}
		} else if (hasDefaultRule(childElements)
				&& DefaultEffect(childElements).equals("permit")) {

		} else {

		}

		return "";
	}

	public String DenyOverride_DenyUnlessPermit(AbstractTarget target,
			List<CombinerElement> childElements) {
		ArrayList<MyAttr> generation = new ArrayList<MyAttr>(); // policy
																// targets
		ArrayList<MyAttr> generation2 = new ArrayList<MyAttr>(); // except
																	// policy
																	// targets
		ArrayList<String> AttributeName = new ArrayList<String>(); // make sure
																	// mutation
																	// is unique
		if (target instanceof Target) {
			for (MyAttr myattr1 : PolicyX.getTargetAttributes((Target) target)) {
				if (!isExist(generation, myattr1)) {
					myattr1.setDomain(getTrueValue(myattr1));
					generation.add(myattr1);
					AttributeName.add(myattr1.getName());
				}
			}
		}
		if (hasDefaultRule(childElements)
				&& DefaultEffect(childElements).equals("deny")) {
			if (allDeny(childElements)) {
				return "";
			} else {
				for (CombinerElement rule : childElements) {
					PolicyTreeElement tree = rule.getElement();
					if (tree instanceof Rule) {
						Rule permitRule = (Rule) tree;
						if (permitRule.getEffect() == 0) {
							if (permitRule.getTarget() instanceof Target) {
								for (MyAttr myattr : PolicyX.getTargetAttributes((Target) permitRule.getTarget())) {
									if (!isExist(generation, myattr)) {
										myattr.setDomain(getTrueValue(myattr));
										generation.add(myattr);
									}
								}
							}
							for (MyAttr myattr : PolicyX.getConditionAttributes(permitRule.getCondition().getExpression())) {
								// System.out.println("******** COLLECT CONDITION*******");
								if (!isExist(generation, myattr)) {
									myattr.setDomain(getTrueValue(myattr));
									generation.add(myattr);
								}
							}
						}
					}
				}
			}
			print(generation);
			return "";

		} else if (hasDefaultRule(childElements)
				&& DefaultEffect(childElements).equals("permit")) {
			if (allPermit(childElements)) {
				return "No way";
			} else {
				for (CombinerElement rule : childElements) {
					PolicyTreeElement tree = rule.getElement();
					if (tree instanceof Rule) {
						Rule permitRule = (Rule) tree;
						if (permitRule.getEffect() == 1) {
							if (permitRule.getTarget() instanceof Target) {
								for (MyAttr myattr : PolicyX.getTargetAttributes((Target) permitRule.getTarget())) {
									if (!isExist(generation, myattr)) {
										myattr.setDomain(getTrueValue(myattr));
										generation.add(myattr);
									}
								}
							}
							for (MyAttr myattr : PolicyX.getConditionAttributes(permitRule.getCondition().getExpression())) {
								// System.out.println("******** COLLECT CONDITION*******");
								if (!isExist(generation, myattr)) {
									myattr.setDomain(getTrueValue(myattr));
									generation.add(myattr);
								}
							}
						}
					}
				}
			}
			print(generation);
			return "";
		} else {
			for (CombinerElement rule : childElements) {
				PolicyTreeElement tree = rule.getElement();
				if (tree instanceof Rule) {
					// System.out.println("get a rule");
					Rule myrule = (Rule) tree;
					if (myrule.getTarget() instanceof Target) {
						for (MyAttr myattr : PolicyX.getTargetAttributes((Target) myrule.getTarget())) {
							if (!isExist(generation2, myattr)) {
								myattr.setDomain(getTrueValue(myattr));
								generation2.add(myattr);
								AttributeName.add(myattr.getName());
							}
						}
						for (MyAttr myattr : PolicyX.getConditionAttributes(myrule.getCondition().getExpression())) {
							if (!isExist(generation2, myattr)) {
								myattr.setDomain(getTrueValue(myattr));
								generation2.add(myattr);
								AttributeName.add(myattr.getName());
							}
						}
					}
				}
			}

			String alias = "urn:oasis:names:tc:xacml:1.0:alias:faked-id";
			final String allChar = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
			Random random = new Random();
			for (String subs : AttributeName) {
				while (subs.equals(alias)) {
					alias = alias
							+ allChar.charAt(random.nextInt(allChar.length()));
				}
			}
			for (MyAttr newAttr : generation2) {
				newAttr.setName(alias);
			}

			StringBuilder sb = new StringBuilder();
			buildHeader(sb);
			for (MyAttr it : generation) {
				buildRequest(sb, it, 0);
			}
			for (MyAttr it : generation2) {
				buildRequest(sb, it, 0);
			}
			buildEnd(sb);
			print(generation);
			print(generation2);
			return "";
		}
	}
	
	public String DenyUnlessPermit_PermitUnlessDeny(AbstractTarget target,
			List<CombinerElement> childElements){
		ArrayList<MyAttr> generation = new ArrayList<MyAttr>(); 
		ArrayList<MyAttr> generation2 = new ArrayList<MyAttr>();
		ArrayList<String> AttributeName = new ArrayList<String>();
		
		if (target instanceof Target) {
			for (MyAttr myattr1 : PolicyX.getTargetAttributes((Target) target)) {
				if (!isExist(generation, myattr1)) {
					myattr1.setDomain(getTrueValue(myattr1));
					generation.add(myattr1);
					AttributeName.add(myattr1.getName());
				}
			}
		}
		if (hasDefaultRule(childElements) && DefaultEffect(childElements).equals("permit")){
			
		}
		return "";
	}
*/
	public boolean allPermit(List<CombinerElement> childElements) {
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

	public boolean allDeny(List<CombinerElement> childElements) {
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





	/*
	 * public String DenyOverride_FA(AbstractTarget target,
	 * List<CombinerElement> childElements) { boolean hasP = false; boolean hasD
	 * = false; ArrayList<MyAttr> generation = new ArrayList<MyAttr>(); if
	 * (target instanceof Target) { for (MyAttr myattr1 : ((Target)
	 * target).getAttribute()) { if (!isExist(generation, myattr1)) {
	 * myattr1.setDomain(getTrueValue(myattr1)); generation.add(myattr1); } } }
	 * 
	 * if (childElements.size() <= 1) { return "do nothing"; } else { for (int i
	 * = 1; i < childElements.size(); i++) { CombinerElement rule =
	 * childElements.get(i); PolicyTreeElement tree = rule.getElement(); if
	 * (tree instanceof Rule) { Rule myrule = (Rule) tree; if
	 * (myrule.getEffect() == 0) { hasP = true; } if (myrule.getEffect() == 1) {
	 * hasD = true; } } } } for (CombinerElement rule : childElements) {
	 * PolicyTreeElement tree = rule.getElement(); if (tree instanceof Rule) {
	 * Rule myrule = (Rule) tree; if (myrule.getEffect() == 0) { if (hasD ==
	 * true) {
	 * 
	 * } } if (myrule.getTarget() instanceof Target) { for (MyAttr myattr :
	 * ((Target) myrule.getTarget()) .getAttribute()) { if
	 * (!isExist(generation2, myattr)) { myattr.setDomain(getTrueValue(myattr));
	 * generation2.add(myattr); AttributeName.add(myattr.getName()); } } for
	 * (MyAttr myattr : myrule.getCondition().getAttribute()) { if
	 * (!isExist(generation2, myattr)) { myattr.setDomain(getTrueValue(myattr));
	 * generation2.add(myattr); AttributeName.add(myattr.getName()); } } } } }
	 * return ""; }
	 */
	
	/*
	public String DenyOverride_PermitOverride(AbstractTarget target,
			List<CombinerElement> childElements) {
		ArrayList<MyAttr> generation = new ArrayList<MyAttr>();
		boolean onePermit = false;
		boolean oneDeny = false;

		if (target instanceof Target) {
			// System.out.println("******** COLLECT TARGET*******");
			for (MyAttr myattr1 : PolicyX.getTargetAttributes((Target) target)) {
				if (!isExist(generation, myattr1)) {
					myattr1.setDomain(getTrueValue(myattr1));
					generation.add(myattr1);
				}
			}
		}
		for (CombinerElement rule : childElements) {
			PolicyTreeElement tree = rule.getElement();
			if (tree instanceof Rule) {
				Rule myrule = (Rule) tree;
				if (myrule.getEffect() == 0 && !onePermit) {
					// System.out.println("******** COLLECT TARGET *******" );
					if (myrule.getTarget() instanceof Target) {
						for (MyAttr myattr : PolicyX.getTargetAttributes((Target) myrule.getTarget())) {
							if (!isExist(generation, myattr)) {
								myattr.setDomain(getTrueValue(myattr));
								generation.add(myattr);
							}
						}
					}
					for (MyAttr myattr : PolicyX.getConditionAttributes(myrule.getCondition().getExpression())) {
						// System.out.println("******** COLLECT CONDITION*******");
						if (!isExist(generation, myattr)) {
							myattr.setDomain(getTrueValue(myattr));
							generation.add(myattr);
						}
					}
					onePermit = true;
				}
				if (myrule.getEffect() == 1 && !oneDeny) {
					if (myrule.getTarget() instanceof Target) {
						for (MyAttr myattr : PolicyX.getTargetAttributes((Target) myrule.getTarget())) {
							if (!isExist(generation, myattr)) {
								myattr.setDomain(getTrueValue(myattr));
								generation.add(myattr);
							}
						}
					}
					for (MyAttr myattr : PolicyX.getConditionAttributes(myrule.getCondition().getExpression())) {
						// System.out.println("******** COLLECT CONDITION*******");
						if (!isExist(generation, myattr)) {
							myattr.setDomain(getTrueValue(myattr));
							generation.add(myattr);
						}
					}
					oneDeny = true;
				}

			}
		}
		StringBuilder sb = new StringBuilder();
		buildHeader(sb);
		for (MyAttr it : generation) {
			buildRequest(sb, it, 0);
		}
		buildEnd(sb);
		// System.out.println(sb);

		return "";
	}

*/
	public String buildHeader(StringBuilder builder) {
		builder.append("<Request xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\" CombinedDecision=\"false\" ReturnPolicyIdList=\"false\">\n");
		return builder.toString();
	}

	public String buildEnd(StringBuilder builder) {
		builder.append("</Request>");
		return builder.toString();
	}

	public String buildRequest(StringBuilder builder, MyAttr myattr, int i) {
		builder.append("<Attributes Category=");
		builder.append("\"" + myattr.getCategory() + "\">\n");
		builder.append("<Attribute AttributeId=");
		builder.append("\"" + myattr.getName()
				+ "\" IncludeInResult = \"false\">\n");
		builder.append("<AttributeValue DataType=");
		builder.append("\"" + myattr.getDataType() + "\">"
				+ myattr.getDomain().get(0).toString() + "</AttributeValue>\n");
		builder.append("</Attribute>\n");
		builder.append("</Attributes>\n");
		return builder.toString();

	}

	public void print(ArrayList<MyAttr> source) {
		for (MyAttr my : source) {
			System.out.println(my.getName() + " :  ");
			System.out.println(my.getDomain().get(0));
		}
	}

	// NING SHEN 5/5
	
	/*
	public String DenyOverride_DenyUnless(AbstractTarget target,
			List<CombinerElement> childElements) {
		ArrayList<MyAttr> generation = new ArrayList<MyAttr>(); // store policy
																// target,must
																// be true
		ArrayList<MyAttr> generation2 = new ArrayList<MyAttr>();// store others,
																// be faked
		ArrayList<String> AttributeName = new ArrayList<String>();
		if (target instanceof Target) {
			for (MyAttr myattr1 : PolicyX.getTargetAttributes((Target) target)) {
				if (!isExist(generation, myattr1)) {
					myattr1.setDomain(getTrueValue(myattr1));
					generation.add(myattr1);
					AttributeName.add(myattr1.getName());
				}
			}
		}
		for (CombinerElement rule : childElements) {
			PolicyTreeElement tree = rule.getElement();
			if (tree instanceof Rule) {
				// System.out.println("get a rule");
				Rule myrule = (Rule) tree;
				if (myrule.getTarget() instanceof Target) {
					for (MyAttr myattr : PolicyX.getTargetAttributes((Target) myrule.getTarget())) {
						if (!isExist(generation2, myattr)) {
							myattr.setDomain(getTrueValue(myattr));
							generation2.add(myattr);
							AttributeName.add(myattr.getName());
						}
					}
					for (MyAttr myattr : PolicyX.getConditionAttributes(myrule.getCondition().getExpression())){
						if (!isExist(generation2, myattr)) {
							myattr.setDomain(getTrueValue(myattr));
							generation2.add(myattr);
							AttributeName.add(myattr.getName());
						}
					}
				}
			}
		}
		String alias = "urn:oasis:names:tc:xacml:1.0:alias:faked-id";
		final String allChar = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
		Random random = new Random();
		for (String subs : AttributeName) {
			while (subs.equals(alias)) {
				alias = alias
						+ allChar.charAt(random.nextInt(allChar.length()));
			}
		}
		for (MyAttr newAttr : generation2) {
			newAttr.setName(alias);
		}

		StringBuilder sb = new StringBuilder();
		buildHeader(sb);
		for (MyAttr it : generation) {
			buildRequest(sb, it, 0);
		}
		for (MyAttr it : generation2) {
			buildRequest(sb, it, 0);
		}
		buildEnd(sb);
		print(generation);
		print(generation2);
		return "";
	}
	*/
}
