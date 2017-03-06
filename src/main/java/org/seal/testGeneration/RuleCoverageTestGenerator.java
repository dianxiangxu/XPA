package org.seal.testGeneration;
import static org.seal.policyUtils.XpathSolver.policyPattern;
import static org.seal.policyUtils.XpathSolver.policysetPattern;
import static org.seal.policyUtils.XpathSolver.rulePattern;

import org.seal.combiningalgorithms.Call_Z3str;
import org.seal.combiningalgorithms.MyAttr;
import org.seal.combiningalgorithms.algorithm;
import org.seal.combiningalgorithms.function;
import org.seal.coverage.PolicySpreadSheetTestRecord;
import org.seal.coverage.PolicySpreadSheetTestSuite;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.balana.DOMHelper;
import org.wso2.balana.ParsingException;
import org.wso2.balana.PolicyMetaData;
import org.wso2.balana.PolicyTreeElement;
import org.wso2.balana.Rule;
import org.wso2.balana.TargetMatch;
import org.seal.policyUtils.PolicyLoader;
import org.seal.policyUtils.XpathSolver;
import org.seal.semanticMutation.Mutator;
import org.seal.testGeneration.TestPanelDemo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.Policy;
import org.wso2.balana.attr.IntegerAttribute;
import org.wso2.balana.attr.StringAttribute;
import org.wso2.balana.attr.xacml3.AttributeDesignator;
import org.wso2.balana.combine.CombinerElement;
import org.wso2.balana.cond.Apply;
import org.wso2.balana.cond.Condition;
import org.wso2.balana.cond.Expression;
import org.wso2.balana.xacml3.AllOfSelection;
import org.wso2.balana.xacml3.AnyOfSelection;
import org.wso2.balana.xacml3.Target;


/**tri
 * Created by roshanshrestha on 2/10/17.
 */
public class RuleCoverageTestGenerator {
	
	static Call_Z3str z3 = new Call_Z3str();
	public static HashMap nameMap = new HashMap();
	public static HashMap typeMap = new HashMap();
	static algorithm al = new algorithm();
    private static boolean debug = false;
    public static int count ;

	
    public static ArrayList<PolicySpreadSheetTestRecord> generateTests(TestPanelDemo testPanel, String policyFile){
        ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
        //List<Rule> rules = getRuleFromPolicy(policy);
        Document doc=null;
        try{
         doc = PolicyLoader.getDocument(new FileInputStream(policyFile));
        }catch(Exception e){
        	e.printStackTrace();
        }
        function f = new function();
        int ruleNo = 0;
        File file = new File(testPanel.getTestOutputDestination("_Exclusive"));
        if (!file.isDirectory() && !file.exists()) {
            file.mkdir();
        } else {
            f.deleteFile(file);
        }
        long startTime = System.currentTimeMillis();
        List<String> paths = new ArrayList<String>();
        List<String> expressions = new ArrayList<String>();
        AbstractPolicy policy=null;
        try{
        	policy = PolicyLoader.loadPolicy(doc);
        }catch(Exception e){
        	e.printStackTrace();
        }
        PolicyMetaData metaData = policy.getMetaData();
        ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
        StringBuffer sb = new StringBuffer();
        count = 0;
        try{
        dfs( doc.getDocumentElement(), paths, expressions, metaData, sb,f,testPanel,generator,null);
        }catch(Exception e){
        	e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Test generation time ： " + (endTime - startTime) + "ms");
        return generator;
    }
    
    public static ArrayList<PolicySpreadSheetTestRecord> generateTests2(TestPanelDemo testPanel, Policy policy) {
        ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
        List<Rule> rules = getRuleFromPolicy(policy);
        function f = new function();
        int count = 1;
        int ruleNo = 0;
        File file = new File(testPanel.getTestOutputDestination("_Exclusive"));
        if (!file.isDirectory() && !file.exists()) {
            file.mkdir();
        } else {
            f.deleteFile(file);
        }
        long startTime = System.currentTimeMillis();
        for (Rule rule : rules) {
            ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
            StringBuffer sb = new StringBuffer();
            if (rule.getCondition() == null && rule.getTarget() == null) {
                boolean success = generateDefaultRule(generator,
                        testPanel, ruleNo, rules, count, "_Exclusive",policy);
                if(success)
                    count++;
                continue;
            }
            sb = new StringBuffer();
            sb.append(TruePolicyTarget(policy, collector) + "\n");
            System.out.println("Policy Target: " +sb);
            sb.append(True_Target((Target) rule.getTarget(), collector) + "\n");
            System.out.println("Rule Target: " +sb);
            sb.append(True_Condition(rule.getCondition(), collector) + "\n");
            System.out.println("Rule Condition: " +sb);
            
            for (Rule fRule : rules) {
                if (fRule.getId().equals(rule.getId()))
                    break;
                if (f.isDefaultRule(fRule))
                    continue;
                sb.append(FalseTarget_FalseCondition(fRule, collector) + "\n");

           // sb.append(False_Condition(fRule, collector) + "\n");
            }
            boolean sat = z3str(sb.toString(), nameMap, typeMap);
            if (sat == true) {
                try {
                    z3.getValue(collector, nameMap);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                String request = f.print(collector);
                try {
                    String path = testPanel
                            .getTestOutputDestination("_Exclusive")
                            + File.separator + "request" + count + ".txt";

                    FileWriter fw = new FileWriter(path);
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(request);
                    bw.close();

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // generate target object
                PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
                        PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + count,
                        "request" + count + ".txt", request, "");
                generator.add(psstr);
                count++;
            }
            ruleNo++;
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Test generation time ： " + (endTime - startTime) + "ms");
        return generator;
    }
    public static List<Rule> getRuleFromPolicy(AbstractPolicy policy) {
		List<CombinerElement> childElements = policy.getChildElements();
		List<Rule> Elements = new ArrayList<Rule>();
		for (CombinerElement element : childElements) {
			PolicyTreeElement tree1 = element.getElement();
			Rule rule = null;
			if (tree1 instanceof Rule) {
				rule = (Rule) tree1;
				Elements.add(rule);
			}
		}
		return Elements;
	}
    
    private static boolean generateDefaultRule(
			ArrayList<PolicySpreadSheetTestRecord> generator,
			TestPanelDemo testPanel, int order, List<Rule> rules, int testNo,
			String coverageName, AbstractPolicy policy) {
		function f = new function();
		StringBuffer sb = new StringBuffer();
		ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
		sb.append(TruePolicyTarget(policy, collector));
		for (int i = 0; i < order; i++) {
			sb.append(FalseTarget_FalseCondition(rules.get(i), collector) + "\n");
		}
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if (sat) {
			try {
			z3.getValue(collector, nameMap);
			} catch (IOException e) {
				e.printStackTrace();
			}
			String request = f.print(collector);
			try {
				String path = testPanel.getTestOutputDestination(coverageName)
						+ File.separator + "request" + testNo + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(request);
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + testNo,
					"request" + testNo + ".txt", request, "");
			generator.add(psstr);
			return true;
		}
		return false;
	}
    
    public static StringBuffer TruePolicyTarget(AbstractPolicy policy,
			ArrayList<MyAttr> collector) {
		StringBuffer sb = new StringBuffer();
		Target target = (Target) policy.getTarget();
		if (target != null) {
			sb.append(getTargetAttribute(target, collector));
		}
		if (sb.toString().equals("(and )")) {
			return new StringBuffer();
		}
		return sb;
	}
    
    public static StringBuffer TruePolicyTarget(Target target,
			ArrayList<MyAttr> collector) {
		StringBuffer sb = new StringBuffer();
		//Target target = (Target) policy.getTarget();
		if (target != null) {
			sb.append(getTargetAttribute(target, collector));
		}
		if (sb.toString().equals("(and )")) {
			return new StringBuffer();
		}
		return sb;
	}
    
    public static StringBuffer True_Target(Target target, ArrayList<MyAttr> collector) {
		StringBuffer sb = new StringBuffer();
		sb.append(getTargetAttribute(target, collector));
		sb.append("\n");
		return sb;
	}
	
    public static StringBuffer True_Condition(Condition condition,
			ArrayList<MyAttr> collector) {
		StringBuffer sb = new StringBuffer();
		sb.append(getConditionAttribute(condition, collector));
		sb.append("\n");
		return sb;
	}
    
    public static String getConditionAttribute(Condition condition,
			ArrayList<MyAttr> collector) {
		if (condition != null) {
			Expression expression = condition.getExpression();
			StringBuffer sb = new StringBuffer();
			if (expression instanceof Apply) {
				Apply apply = (Apply) expression;
			sb = ApplyStatements(apply, "", sb, collector);
			}
			return sb.toString();
		}
		return "";
	}
    public static String getTargetAttribute(Target target,
			ArrayList<MyAttr> collector, MyAttr input) {
		StringBuffer sb = new StringBuffer();
		if (target != null) {
			for (AnyOfSelection anyofselection : target.getAnyOfSelections()) {
				StringBuilder orBuilder = new StringBuilder();
				for (AllOfSelection allof : anyofselection.getAllOfSelections()) {
					StringBuilder allBuilder = new StringBuilder();
					for (TargetMatch match : allof.getMatches()) {

						if (match.getEval() instanceof AttributeDesignator) {

							AttributeDesignator attribute = (AttributeDesignator) match
									.getEval();
							if (!attribute.getId().toString()
									.equals(input.getName())) {
								continue;
							}
						allBuilder.append(" ("
									+ al.returnFunction(match
											.getMatchFunction().encode()) + " "
									+ getName(attribute.getId().toString())
									+ " ");
							if (attribute.getType().toString()
									.contains("string")) {
								String value = match.getAttrValue().encode();
								value = value.replaceAll("\n", "");
								value = value.trim();
								allBuilder.append("\"" + value + "\")");
							}
							if (attribute.getType().toString()
									.contains("integer")) {
								String value = match.getAttrValue().encode();
								value = value.replaceAll("\n", "");
								value.trim();
								value = value.trim();
								allBuilder.append(value + ")");
							}
							getType(getName(attribute.getId().toString()),
								    								attribute.getType().toString());
							MyAttr myattr = new MyAttr(attribute.getId()
									.toString(), attribute.getCategory()
									.toString(), attribute.getType().toString());
							if (isExist(collector, myattr) == false) {
								collector.add(myattr);
							}
						}
					}
					allBuilder.insert(0, "(and ");
					allBuilder.append(")");
					orBuilder.append(allBuilder);
				}
				orBuilder.insert(0, "(or ");
				orBuilder.append(")");
				sb.append(orBuilder);
			}
			sb.insert(0, "(and ");
			sb.append(")");
			return sb.toString();
		}
		return "";
	}
    
    public static String getTargetAttribute(Target target, ArrayList<MyAttr> collector) {
		StringBuffer sb = new StringBuffer();
		if (target != null) {
			for (AnyOfSelection anyofselection : target.getAnyOfSelections()) {
				StringBuilder orBuilder = new StringBuilder();
				for (AllOfSelection allof : anyofselection.getAllOfSelections()) {
					StringBuilder allBuilder = new StringBuilder();
					for (TargetMatch match : allof.getMatches()) {

						if (match.getEval() instanceof AttributeDesignator) {

							AttributeDesignator attribute = (AttributeDesignator) match
									.getEval();
							// System.out.println("********" +
							// attribute.getId().toString());
							String attr = attribute.toString();
							String temp = getName(attribute.getId().toString());
							allBuilder.append(" ("
									+ al.returnFunction(match
											.getMatchFunction().encode()) + " "
									+ getName(attribute.getId().toString())
									+ " ");
							if (attribute.getType().toString()
									.contains("string")) {
								String value = match.getAttrValue().encode();
								value = value.replaceAll("\n", "");
								value = value.trim();
								allBuilder.append("\"" + value + "\")");
							}
							if (attribute.getType().toString()
									.contains("integer")) {
								String value = match.getAttrValue().encode();
								value = value.replaceAll("\n", "");
								value.trim();
								value = value.trim();
								allBuilder.append(value + ")");
							}
							getType(getName(attribute.getId().toString()),
									attribute.getType().toString());
							MyAttr myattr = new MyAttr(attribute.getId()
									.toString(), attribute.getCategory()
									.toString(), attribute.getType().toString());
							if (isExist(collector, myattr) == false) {
								collector.add(myattr);
							}
						}
					}
					allBuilder.insert(0, " (and");
					allBuilder.append(")");
					orBuilder.append(allBuilder);
				}
				orBuilder.insert(0, " (or ");
				orBuilder.append(")");
				sb.append(orBuilder);
			}
			sb.insert(0, "(and ");
			sb.append(")");
			return sb.toString();
		}
		return "";
	}
    
    public static StringBuffer FalseTarget_FalseCondition(Rule rule,
			ArrayList<MyAttr> collector) {
		StringBuffer targetsb = new StringBuffer();
		StringBuffer conditionsb = new StringBuffer();
		StringBuffer sb = new StringBuffer();
		Target target = (Target) rule.getTarget();
		targetsb.append(getTargetAttribute(target, collector));
		conditionsb.append(getConditionAttribute(rule.getCondition(), collector));
		sb.append("(not (and ");
		sb.append(targetsb);
		sb.append(conditionsb);
		sb.append("))");
		return sb;
	}
    
    public static StringBuffer FalseTarget_FalseCondition(Target target,Condition condition,
			ArrayList<MyAttr> collector) {
		StringBuffer targetsb = new StringBuffer();
		StringBuffer conditionsb = new StringBuffer();
		StringBuffer sb = new StringBuffer();
		
		targetsb.append(getTargetAttribute(target, collector));
		conditionsb.append(getConditionAttribute(condition, collector));
		if(targetsb.equals("")&& conditionsb.equals("")){
			System.out.println("");
		}
		sb.append("(not (and ");
		sb.append(targetsb);
		sb.append(conditionsb);
		sb.append("))");
		return sb;
	}
    
    public static boolean z3str(String input, HashMap nameMap, HashMap typeMap) {
		//System.err.println("Building z3 input");
		z3.buildZ3Input(input, nameMap, typeMap);
		z3.buildZ3Output();
		if (z3.checkConflict() == true) {
			return true;
		} else {
			return false;
		}
	}
    
	public static StringBuffer ApplyStatements(Apply apply, String function,
			StringBuffer sb, ArrayList<MyAttr> collector) {
		if (apply.getFunction().encode()
				.contains("urn:oasis:names:tc:xacml:1.0:function:and")) {
			StringBuffer newsb = new StringBuffer();
			;
			for (Object element : apply.getList()) {
				if (element instanceof Apply) {
					Apply childApply = (Apply) element;
					ApplyStatements(childApply, apply.getFunction().toString(),
							newsb, collector);
				}
			}
			newsb.insert(0, "(and ");
			newsb.append(")");
			sb.append(newsb);
			return sb;
		} else if (apply.getFunction().encode()
				.contains("urn:oasis:names:tc:xacml:1.0:function:or")) {
			StringBuffer newsb = new StringBuffer();
			for (Object element : apply.getList()) {

				if (element instanceof Apply) {
					Apply childApply = (Apply) element;
					ApplyStatements(childApply, apply.getFunction().toString(),
							newsb, collector);
				}
			}
			newsb.insert(0, "(or ");
			newsb.append(")");
			sb.append(newsb);
			return sb;
		} else if (apply.getFunction().encode()
				.contains("urn:oasis:names:tc:xacml:1.0:function:not")) {
			StringBuffer newsb = new StringBuffer();
			for (Object element : apply.getList()) {
				if (element instanceof Apply) {

					Apply childApply = (Apply) element;
					ApplyStatements(childApply, apply.getFunction().toString(),
							newsb, collector);
				}
			}
			newsb.insert(0, "(not ");
			newsb.append(")");
			sb.append(newsb);
			return sb;
		} else if (apply.getFunction().encode().contains("string-is-in")) {
			String value = "";
			value = getAttrValue(apply);
			String functionName = al.returnFunction(apply.getFunction()
					.encode());
			sb = buildAttrDesignator(sb, apply, value, functionName, collector);
			return sb;
		} else if (apply.getFunction().encode()
				.contains("string-at-least-one-member-of")) {
			String value = "";
			String functionName = al.returnFunction(apply.getFunction()
					.encode());
			for (Object element : apply.getList()) {
				if (element instanceof Apply) {
					Apply childApply = (Apply) element;
					value = getAttrValue(childApply);
				}
			}
			sb = buildAttrDesignator(sb, apply, value, functionName, collector);
			return sb;
		} else {
			for (Object element : apply.getList()) {
				String value = null;
				if (element instanceof IntegerAttribute) {
					IntegerAttribute intValue = (IntegerAttribute) element;
					value = intValue.getValue() + "";
					sb.append(value + ")");

				}
				if (element instanceof StringAttribute) {
					StringAttribute stringValue = (StringAttribute) element;
					value = stringValue.getValue() + "";
					sb.append("\"" + value + "\")");
				}
				if (element instanceof Apply) {
					Apply childApply = (Apply) element;
					ApplyStatements(childApply, apply.getFunction().encode(),
							sb, collector);
				}
				if (element instanceof AttributeDesignator) {
					AttributeDesignator attributes = (AttributeDesignator) element;
					sb.append(" (" + al.returnFunction(function) + " "
							+ getName(attributes.getId().toString()) + " ");
					getType(getName(attributes.getId().toString()), attributes
							.getType().toString());
					MyAttr myattr = new MyAttr(attributes.getId().toString(),
							attributes.getCategory().toString(), attributes
									.getType().toString());
					if (isExist(collector, myattr) == false) {
						collector.add(myattr);
					}

				}
			}
		}
		return sb;
	}
	
	private static String getName(String name) {
		boolean has = true;
		if (nameMap.containsKey(name)) {
			return nameMap.get(name).toString();
		} else {
			StringBuffer sb = new StringBuffer();
			do {
				sb = new StringBuffer();
				String base = "abcdefghijklmnopqrstuvwxyz";
				Random random = new Random();
				for (int i = 0; i < 5; i++) {
					int number = random.nextInt(base.length());

					sb.append(base.charAt(number));
				}
				if (!nameMap.containsValue(sb.toString())) {
					has = false;
				}
			} while (has == true);
			nameMap.put(name, sb.toString());
			return sb.toString();
		}
	}

	private static String getType(String name, String type) {
		if (typeMap.containsKey(name)) {
			return typeMap.get(name).toString();
		} else {
			if (type.contains("string")) {
				typeMap.put(name, "String");
			}
			if (type.contains("integer")) {
				typeMap.put(name, "Int");
			}
			return typeMap.get(name).toString();
		}
	}
	
	public static boolean isExist(ArrayList<MyAttr> generation, MyAttr childAttr) {
		if (generation == null)
			return false;
		for (MyAttr it : generation) {
			if (it.getName().equals(childAttr.getName())) {
				return true;
			}
		}
		return false;
	}

	public static String getAttrValue(Apply apply) {
		String value = "";
		for (Object element : apply.getList()) {
			if (element instanceof IntegerAttribute) {
				IntegerAttribute intValue = (IntegerAttribute) element;
				value = intValue.getValue() + ")";
				// sb.append(value + ")");

			}
			if (element instanceof StringAttribute) {
				StringAttribute stringValue = (StringAttribute) element;
				value = "\"" + stringValue.getValue() + "\")";
				// sb.append("\"" + value + "\")");
			}
		}
		return value;
	}
	
	public static StringBuffer buildAttrDesignator(StringBuffer sb, Apply apply,
			String value, String function, ArrayList<MyAttr> collector) {
		for (Object element : apply.getList()) {
			if (element instanceof AttributeDesignator) {
				AttributeDesignator attributes = (AttributeDesignator) element;
				sb.append(" (" + function + " "
						+ getName(attributes.getId().toString()) + " " + value);
				getType(getName(attributes.getId().toString()), attributes
						.getType().toString());
				MyAttr myattr = new MyAttr(attributes.getId().toString(),
						attributes.getCategory().toString(), attributes
								.getType().toString());
				if (isExist(collector, myattr) == false) {
					collector.add(myattr);
				}
			}
		}
		return sb;
	}

    

	private static void dfs(Element node, List<String> path, List<String> expressions, PolicyMetaData metaData, StringBuffer sb, function f,TestPanelDemo testPanel,ArrayList<PolicySpreadSheetTestRecord> generator,List<String> falseTargetFalseCondition) throws ParsingException {
        String name = DOMHelper.getLocalName(node);
        Target target = null;
        Condition condition = null;
        if (rulePattern.matcher(name).matches()) {
        	
            Node targetNode = findInChildNodes(node, "Target");
            if (!Mutator.isEmptyNode(targetNode)) {
                if (debug) {
                    path.add(XpathSolver.buildNodeXpath(targetNode));
                } else {
                    target = Target.getInstance(targetNode, metaData);
                    String targetExpression = buildTargetExpression(target);
                    path.add(targetExpression);
                }
            }
            Node conditionNode = findInChildNodes(node, "Condition");
            
            if (!Mutator.isEmptyNode(conditionNode)) {
                if (debug) {
                    path.add(XpathSolver.buildNodeXpath(conditionNode));
                } else {
                    condition = Condition.getInstance(conditionNode, metaData, null);
                    String conditionExpression = buildConditionExpression(condition);
                    path.add(conditionExpression);
                }
            }
            expressions.add(concatenateExpressions(path));
            if (!Mutator.isEmptyNode(targetNode)) {
                path.remove(path.size() - 1);
            }
            if (!Mutator.isEmptyNode(conditionNode)) {
                path.remove(path.size() - 1);
            }
            if (condition == null && target == null) {
            	//TODO default rule
            }
            ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
            StringBuffer ruleExpression = new StringBuffer();
            ruleExpression.append(True_Target(target, collector) + "\n");
            ruleExpression.append(True_Condition(condition, collector) + "\n");
            //TODO : need to handle this in more effective approach
            if(!falseTargetFalseCondition.equals("(not (and ))")){
            	falseTargetFalseCondition.add(FalseTarget_FalseCondition(target,condition, collector) + "\n");
            }
            StringBuffer falsifyPreviousRules = new StringBuffer();
            for(int i = 0; i< falseTargetFalseCondition.size()-1;i++){
            	falsifyPreviousRules.append(falseTargetFalseCondition.get(i));
            }
            String expresion = sb.toString()+ruleExpression+falsifyPreviousRules;
            boolean sat = z3str(expresion, nameMap, typeMap);
            if (sat == true) {
                try {
                    z3.getValue(collector, nameMap);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                String request = f.print(collector);
                try {
                	count++;
                    String filePath = testPanel
                            .getTestOutputDestination("_Exclusive")
                            + File.separator + "request" + count + ".txt";

                    FileWriter fw = new FileWriter(filePath);
                    BufferedWriter bw = new BufferedWriter(fw);
                    bw.write(request);
                    bw.close();

                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

                // generate target object
                PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
                        PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + count,
                        "request" + count + ".txt", request, "");
                generator.add(psstr);
                
            }
            return;
        }
        if (policyPattern.matcher(name).matches() || policysetPattern.matcher(name).matches()) {
            Node targetNode = findInChildNodes(node, "Target");
            if (targetNode != null) {
                if (debug) {
                    path.add(XpathSolver.buildNodeXpath(targetNode));
                } else {
                    target = Target.getInstance(targetNode, metaData);
                    String targetExpression = buildTargetExpression(target);
                    path.add(targetExpression);
                }
            }
            NodeList children = node.getChildNodes();
            falseTargetFalseCondition= null;
            if(policyPattern.matcher(name).matches()){
            	falseTargetFalseCondition = new ArrayList<String>();
            }
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                
                if (child instanceof Element) {
                	dfs((Element) child, path, expressions, metaData,sb,f,testPanel,generator,falseTargetFalseCondition);
                }
            }
            path.remove(path.size() - 1);
            ArrayList<MyAttr> collector = new ArrayList<MyAttr>();

            sb.append(TruePolicyTarget(target, collector) + "\n");
            System.out.println(sb);
        }
    }
	private static Node findInChildNodes(Node parent, String localName) {
        List<Node> childNodes = Mutator.getChildNodeList(parent);
        for (Node child : childNodes) {
            if (localName.equals(child.getLocalName())) {
                return child;
            }
        }
        return null;
    }

    private static String concatenateExpressions(List<String> path) {
        //TODO construct an expression from a list expressions
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            if (i == 0) {
                sb.append(path.get(i));
            } else {
                sb.append("\n").append(path.get(i));
            }
        }
        return sb.toString();
    }

    private static String buildTargetExpression(Target target) {
        //TODO build expression from Target
        return target.encode();
    }

    private static String buildConditionExpression(Condition condition) {
        //TODO build expression from Condition
        return condition.encode();
    }
	
}
