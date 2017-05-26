package org.seal.testGeneration;

import static org.seal.policyUtils.XpathSolver.policyPattern;
import static org.seal.policyUtils.XpathSolver.policysetPattern;
import static org.seal.policyUtils.XpathSolver.rulePattern;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import org.seal.combiningalgorithms.Call_Z3str;
import org.seal.combiningalgorithms.MyAttr;
import org.seal.combiningalgorithms.algorithm;
import org.seal.combiningalgorithms.function;
import org.seal.coverage.PolicySpreadSheetTestRecord;
import org.seal.coverage.PolicySpreadSheetTestSuite;
import org.seal.gui.TestPanelDemo;
import org.seal.mcdc.MCDC_converter2;
import org.seal.policyUtils.PolicyLoader;
import org.seal.policyUtils.XpathSolver;
import org.seal.semanticMutation.Mutator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.Balana;
import org.wso2.balana.DOMHelper;
import org.wso2.balana.MatchResult;
import org.wso2.balana.ParsingException;
import org.wso2.balana.Policy;
import org.wso2.balana.PolicyMetaData;
import org.wso2.balana.PolicySet;
import org.wso2.balana.PolicyTreeElement;
import org.wso2.balana.Rule;
import org.wso2.balana.TargetMatch;
import org.wso2.balana.attr.BooleanAttribute;
import org.wso2.balana.attr.IntegerAttribute;
import org.wso2.balana.attr.StringAttribute;
import org.wso2.balana.attr.xacml3.AttributeDesignator;
import org.wso2.balana.combine.CombinerElement;
import org.wso2.balana.cond.Apply;
import org.wso2.balana.cond.Condition;
import org.wso2.balana.cond.EvaluationResult;
import org.wso2.balana.cond.Expression;
import org.wso2.balana.ctx.AbstractRequestCtx;
import org.wso2.balana.ctx.RequestCtxFactory;
import org.wso2.balana.ctx.xacml3.RequestCtx;
import org.wso2.balana.ctx.xacml3.XACML3EvaluationCtx;
import org.wso2.balana.xacml3.AllOfSelection;
import org.wso2.balana.xacml3.AnyOfSelection;
import org.wso2.balana.xacml3.Target;

public class DecisionCoverageTestGenerator {
	static AbstractPolicy policy;
	static Call_Z3str z3 = new Call_Z3str();
	public static HashMap nameMap = new HashMap();
	public static HashMap typeMap = new HashMap();
	static algorithm al = new algorithm();
	private static Balana balana;
	private static ArrayList<PolicySpreadSheetTestRecord> generator;
	 private static function f;
	 private static  PolicyMetaData metaData;
	 private static boolean debug = false;   
	 private static int count =0;
	 private static TestPanelDemo testPanel ;
	    
	
	 private static void initDependencies(TestPanelDemo testPanel, AbstractPolicy policy,String fileName,Balana b){
		 DecisionCoverageTestGenerator.testPanel = testPanel;
		 DecisionCoverageTestGenerator.policy = policy;
		 DecisionCoverageTestGenerator.generator = new ArrayList<PolicySpreadSheetTestRecord>();
         f = new function();
         metaData = policy.getMetaData();
         File file = new File(testPanel.getTestOutputDestination(fileName));
         if (!file.isDirectory() && !file.exists()) {
             file.mkdirs();
         } else {
             f.deleteFile(file);
         }
         balana = b;
    }
	 
	 private static void dfs(Element node, List<String> path, StringBuilder preExpression,List<Rule> previousRules,  ArrayList<MyAttr> rootCollector) throws ParsingException {
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
		        if(condition==null && target==null){
		        	return;
		        }
		        
	            if (!Mutator.isEmptyNode(targetNode)) {
	                    path.remove(path.size() - 1);
	                }
	                if (!Mutator.isEmptyNode(conditionNode)) {
	                    path.remove(path.size() - 1);
	                }
	                StringBuffer falsifyPreviousRules = new StringBuffer();
	                for(Rule rule:previousRules){
	    				falsifyPreviousRules.append(FalseTarget_FalseCondition(rule, rootCollector) + "\n");
	                }
	                ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
	                collector.addAll(rootCollector);
	                StringBuffer ruleExpression = new StringBuffer();
	                StringBuffer ruleNotConditionExpression = new StringBuffer();
	                StringBuffer errorConditionExpression = new StringBuffer();
			            
	                ruleExpression.append(True_Target(target, collector) + "\n");
	                if(target!=null){
	                	writeRequest(preExpression.toString()+False_Target(target,collector)+"\n"+falsifyPreviousRules,collector);
	                	IndTarget(target,collector,preExpression.toString());
	                	
	                	ruleNotConditionExpression.append(True_Target(target,collector));
	                	errorConditionExpression.append(True_Target(target,collector));
	                }
	                ruleExpression.append(True_Condition(condition, collector) + "\n");
	                ruleNotConditionExpression.append(False_Condition(condition, collector));
	                String expresion = preExpression.toString()+ruleExpression+"\n"+falsifyPreviousRules;
	                String notExpresion = preExpression.toString()+ruleNotConditionExpression+"\n"+falsifyPreviousRules;
	                String errorExpresion = preExpression.toString()+errorConditionExpression+"\n"+falsifyPreviousRules;
	                
	                writeRequest(expresion,collector);
	                if(condition!=null){
	                	writeRequest(notExpresion,collector);
	                	IndCondition(condition,collector, preExpression.toString() + falsifyPreviousRules);
	                }
	                previousRules.add(Rule.getInstance(node, metaData, null));
	                return;
	            }
	            if (policyPattern.matcher(name).matches() || policysetPattern.matcher(name).matches()) {
	                Node targetNode = findInChildNodes(node, "Target");
	                if (targetNode != null) {
	                    if (debug) {
	                        path.add(XpathSolver.buildNodeXpath(targetNode));
	                    } else {
	                        target = Target.getInstance(targetNode, metaData);
	                        //String targetExpression = buildTargetExpression(target);
	                        StringBuilder str = new StringBuilder();
	                        if(target.getAnyOfSelections().size()>0){
	                        /*
	                         * Here
	                         */
	                        StringBuffer expresion = False_Target(target,rootCollector);
	                        boolean sat = z3str(expresion.toString(), nameMap, typeMap);
	                        ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
	    	                collector.addAll(rootCollector);
	    	               
	    	                writeRequest(expresion.toString(),collector);


	    	                IndTarget(target,collector,preExpression.toString());
	                        
	    	                preExpression.append(True_Target(target, rootCollector) + "\n");
	                        
	                        }
	                        //path.add(targetExpression);
	                    }
	                }
	                
	                NodeList children = node.getChildNodes();
	                previousRules = null;
	                if(policyPattern.matcher(name).matches()){
	                	previousRules = new ArrayList<Rule>();
	                }
	                for (int i = 0; i < children.getLength(); i++) {
	                    Node child = children.item(i);
	                    StringBuilder preExpressionCurrent = new StringBuilder(preExpression.toString());
	                         
	                    if (child instanceof Element && isTraversableElement(child)) {
	                    	dfs((Element) child, path, preExpressionCurrent,previousRules,rootCollector);
	                    }
	                }
	                if(path.size()>0){
	                	path.remove(path.size() - 1);
	                }
	            }
	        }

	 private static void writeRequest(String expresion,ArrayList<MyAttr> collector){
		 boolean sat = z3str(expresion, nameMap, typeMap);
         if (sat == true) {
             try {
             	z3.getValue(collector, nameMap);
             } catch (IOException e) {
                 e.printStackTrace();
             }
             String request = f.print(collector);
             try {
             	count++;
                 String filePath = testPanel.getTestOutputDestination("_DecisionCoverage") + File.separator + "request" + count + ".txt";
                 FileWriter fw = new FileWriter(filePath);
                 BufferedWriter bw = new BufferedWriter(fw);
                 bw.write(request);
                 bw.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }

             PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + count, "request" + count + ".txt", request, "");
             generator.add(psstr);
         }

	 }
   
	 public static ArrayList<PolicySpreadSheetTestRecord> generateTests(TestPanelDemo testPanel,String fileName,String file,Balana b) {
	    	try{
	    	 policy = PolicyLoader.loadPolicy(new File(file));
	    	}catch(Exception e){
	    		e.printStackTrace();
	    	}
	    	initDependencies(testPanel,policy,fileName,b);
	    	count=0;
	    	Document doc=null;
	    	ArrayList<MyAttr> rootCollector = new ArrayList<MyAttr>();
	        StringBuilder preExpression = new StringBuilder();
	        long startTime = System.currentTimeMillis();
	        List<String> paths = new ArrayList<String>();
	        
	        try{
	         doc = PolicyLoader.getDocument(new FileInputStream(file));
	        }catch(Exception e){
	        	e.printStackTrace();
	        }
	    	try{
	        	dfs( doc.getDocumentElement(), paths, preExpression,null,rootCollector);
	        }catch(Exception e){
	        	e.printStackTrace();
	        }
	    	return generator;
	 }
       
  public static List<Rule> getRuleFromPolicy(AbstractPolicy policy) {
		List<CombinerElement> childElements = new ArrayList<CombinerElement>(); 
				childElements.addAll(policy.getChildElements());
		List<Rule> Elements = new ArrayList<Rule>();
		//for (CombinerElement element : childElements) {
		for(int i=0;i<childElements.size();i++){
		PolicyTreeElement tree1 = childElements.get(i).getElement();
			Rule rule = null;
			if (tree1 instanceof Rule) {
				rule = (Rule) tree1;
				Elements.add(rule);
			} else if(tree1 instanceof Policy || tree1 instanceof PolicySet ){
				
				childElements.addAll(i+1, ((AbstractPolicy)tree1).getChildElements());
			}
		}
		return Elements;
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
    
    private static MyAttr invalidAttr() {
		MyAttr myattr = new MyAttr(randomAttribute(), randomAttribute(),
				"http://www.w3.org/2001/XMLSchema#string");
		myattr.addValue("Indeterminate");
		return myattr;
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

    public static StringBuffer False_Target(Target target, ArrayList<MyAttr> collector) {
		StringBuffer sb = new StringBuffer();
		sb.append(getTargetAttribute(target, collector));
		String[] lines = sb.toString().split("\n");
		StringBuffer output = new StringBuffer();
		for (String s : lines) {
			if (s.isEmpty()) {
				continue;
			} else {
				StringBuffer subsb = new StringBuffer();
				subsb.append("(not ");
				subsb.append(s);
				subsb.append(")");
				output.append(subsb);
			}
		}
		return output;
	}
	
    private static boolean generateDefaultRule(
			ArrayList<PolicySpreadSheetTestRecord> generator,
			TestPanelDemo testPanel, int order, List<Rule> rules, int testNo,
			String coverageName) {
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
	
    public static boolean isDefaultRule(Rule rule) {
		if (rule.getCondition() == null && rule.getTarget() == null) {
			return true;
		} else {
			return false;
		}
	}
    
    public static StringBuffer FalseTarget_FalseCondition(Rule rule,
			ArrayList<MyAttr> collector) {
		StringBuffer targetsb = new StringBuffer();
		StringBuffer conditionsb = new StringBuffer();
		StringBuffer sb = new StringBuffer();
		Target target = (Target) rule.getTarget();
		targetsb.append(getTargetAttribute(target, collector));
		conditionsb
				.append(getConditionAttribute(rule.getCondition(), collector));
		sb.append("(not (and ");
		sb.append(targetsb);
		sb.append(conditionsb);
		sb.append("))");
		return sb;
	}
    
    private static boolean generateIndTarget(
			ArrayList<PolicySpreadSheetTestRecord> generator,
			TestPanelDemo testPanel, PolicyTable policytable, Rule targetRule,
			ArrayList<MyAttr> collector, int testNo, int ruleNo,
			StringBuffer prefix, String coverageName, TarRecord rtarget,
			boolean isDC) {
		function f = new function();
		StringBuffer sb = new StringBuffer();
		ArrayList<MyAttr> temp = new ArrayList<MyAttr>();
		if (policy
				.getCombiningAlg()
				.getIdentifier()
				.toString()
				.equals("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable")) {

			sb.append(True_Target((Target) targetRule.getTarget(), temp) + "\n");
			MyAttr unique = getDifferentAttribute(collector, temp);
			if (unique == null) {
				return false;
			}
			temp.add(invalidAttr());
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			mergeAttribute(temp, collector);
			temp.remove(unique);
			sb.append(prefix);
		} else {
			sb.append(True_Target((Target) targetRule.getTarget(), temp) + "\n");
			temp.add(invalidAttr());
			sb.append(TruePolicyTarget(policy, collector) + " \n");
			mergeAttribute(temp, collector);
			temp.remove(0);
			sb.append(prefix);
		}
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if (sat) {
			try {
				z3.getValue(temp, nameMap);
			} catch (IOException e) {
				e.printStackTrace();
			}
			rtarget.setCovered(1);
			String request = f.print(temp);
			if (isDC)
				updateDecisionTable(policy, policytable, request, ruleNo);
			else
				updateMCDCTable(policy, policytable, request, ruleNo);
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

    private static boolean IndTarget(
			Target target,ArrayList<MyAttr> collector,String prefix) {
		function f = new function();
		StringBuffer sb = new StringBuffer();
		ArrayList<MyAttr> temp = new ArrayList<MyAttr>();
		if (policy
				.getCombiningAlg()
				.getIdentifier()
				.toString()
				.equals("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable")) {

			sb.append(True_Target(target, temp) + "\n");
			MyAttr unique = getDifferentAttribute(collector, temp);
			if (unique == null) {
				return false;
			}
			temp.add(invalidAttr());
			mergeAttribute(temp, collector);
			temp.remove(unique);
			sb.append(prefix);
		} else {
			sb.append(True_Target(target, temp) + "\n");
			temp.add(invalidAttr());
			mergeAttribute(temp, collector);
			temp.remove(0);
			sb.append(prefix);
		}
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if (sat) {
			try {
				z3.getValue(temp, nameMap);
			} catch (IOException e) {
				e.printStackTrace();
			}
			String request = f.print(temp);
			try {
				String path = testPanel.getTestOutputDestination("_DecisionCoverage") + File.separator + "request" + ++count + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(request);
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + count,
					"request" + count + ".txt", request, "");
			generator.add(psstr);
			return true;
		}
		return false;
	}

    private static boolean IndCondition(
			Condition condition,ArrayList<MyAttr> collector,String prefix) {
		function f = new function();
		StringBuffer sb = new StringBuffer();
		ArrayList<MyAttr> temp = new ArrayList<MyAttr>();
		if (policy
				.getCombiningAlg()
				.getIdentifier()
				.toString()
				.equals("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable")) {

			sb.append(True_Condition(condition, temp) + "\n");
			MyAttr unique = getDifferentAttribute(collector, temp);
			if (unique == null) {
				return false;
			}
			temp.add(invalidAttr());
			mergeAttribute(temp, collector);
			temp.remove(unique);
			sb.append(prefix);
		} else {
			sb.append(True_Condition(condition, temp) + "\n");
			temp.add(invalidAttr());
			mergeAttribute(temp, collector);
			temp.remove(0);
			sb.append(prefix);
		}
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if (sat) {
			try {
				z3.getValue(temp, nameMap);
			} catch (IOException e) {
				e.printStackTrace();
			}
			String request = f.print(temp);
			try {
				String path = testPanel.getTestOutputDestination("_DecisionCoverage") + File.separator + "request" + ++count + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(request);
				bw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + count,
					"request" + count + ".txt", request, "");
			generator.add(psstr);
			return true;
		}
		return false;
	}


    public static void updateDecisionTable(AbstractPolicy policy, PolicyTable policytable,
			String request, int start) {
		List<Rule> rules = getRuleFromPolicy(policy);
		for (int i = 0; i < start; i++) {
			if (isDefaultRule(rules.get(i))) {
				continue;
			}
			Rule rule = rules.get(i);
			int tResult = -1;
			int cResult = -1;
			if (rule.getTarget() != null) {
				tResult = TargetEvaluate((Target) rule.getTarget(), request);
			}
			if (rule.getCondition() != null) {
				cResult = ConditionEvaluate(rule.getCondition(), request);
			}
			// get rule record
			RuleRecord record = policytable.getRules().get(i);
			// first doesn't have target
			if (rule.getTarget() == null) {
				for (ConRecord conRecord : record.getCondition()) {
					if (conRecord.getEffect() == cResult
							&& conRecord.getCovered() == 0) {
						conRecord.setCovered(1);
					}
				}
			} else { // has target
				for (TarRecord tarRecord : record.getTarget()) {
					if (tarRecord.getEffect() == tResult
							&& tarRecord.getCovered() == 0) {
						if (tResult != 0) {
							tarRecord.setCovered(1);

						} else {
							for (ConRecord conRecord : record.getCondition()) {
								if (conRecord.getEffect() == cResult
										&& conRecord.getCovered() == 0) {
									conRecord.setCovered(1);
								}
							}
						}
					}
				}
			}
		}
	}

    public static StringBuffer True_Target(Target target, ArrayList<MyAttr> collector) {
		StringBuffer sb = new StringBuffer();
		sb.append(getTargetAttribute(target, collector));
		sb.append("\n");
		return sb;
	}
    
    
    
    private static boolean generateIndCondition(
			ArrayList<PolicySpreadSheetTestRecord> generator,
			TestPanelDemo testPanel, PolicyTable policytable, Rule targetRule,
			ArrayList<MyAttr> collector, int testNo, int ruleNo,
			StringBuffer prefix, String coverageName, ConRecord rcondition,
			boolean isDC) {
		function f = new function();
		StringBuffer sb = new StringBuffer();
		ArrayList<MyAttr> temp = new ArrayList<MyAttr>();
		if (policy
				.getCombiningAlg()
				.getIdentifier()
				.toString()
				.equals("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable")) {
			sb.append(True_Target((Target) targetRule.getTarget(), collector)
					+ "\n");
			sb.append(True_Condition(targetRule.getCondition(), temp) + "\n");
			MyAttr unique = getDifferentAttribute(collector, temp);

			if (unique == null) {
				return false;
			}
			temp.add(invalidAttr());
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			mergeAttribute(temp, collector);
			temp.remove(unique);
			sb.append(prefix);
		} else {

			sb.append(True_Target((Target) targetRule.getTarget(), collector)
					+ "\n");
			sb.append(True_Condition(targetRule.getCondition(), temp) + "\n");
			temp.add(invalidAttr());
			sb.append(TruePolicyTarget(policy, collector) + " \n");
			mergeAttribute(temp, collector);
			temp.remove(0);
			sb.append(prefix);
		}
		boolean sat = z3str(sb.toString(), nameMap, typeMap);

		if (sat) {
			try {
				z3.getValue(temp, nameMap);
			} catch (IOException e) {
				e.printStackTrace();
			}
			rcondition.setCovered(1);
			String request = f.print(temp);
			if (isDC)
				updateDecisionTable(policy, policytable, request, ruleNo);
			else
				updateMCDCTable(policy, policytable, request, ruleNo);
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

    public static StringBuffer False_Condition(Condition condition,
			ArrayList<MyAttr> collector) {
		StringBuffer sb = new StringBuffer();
		sb.append(getConditionAttribute(condition, collector));
		String[] lines = sb.toString().split("\n");
		StringBuffer output = new StringBuffer();
		for (String s : lines) {
			if (s.isEmpty()) {
				continue;
			} else {
				StringBuffer subsb = new StringBuffer();
				subsb.append("(not ");
				subsb.append(s);
				subsb.append(")");
				output.append(subsb);
			}
		}
		return output;
	}

    public static StringBuffer True_Condition(Condition condition,
			ArrayList<MyAttr> collector) {
		StringBuffer sb = new StringBuffer();
		sb.append(getConditionAttribute(condition, collector));
		sb.append("\n");
		return sb;
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
			if (type.contains("boolean")) {
				typeMap.put(name, "bool");
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

	private static String randomAttribute() {
		String base = "abcdefghijklmnopqrstuvwxyz0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 10; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
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

	private static MyAttr getDifferentAttribute(ArrayList<MyAttr> globle,
			ArrayList<MyAttr> local) {
		out: for (MyAttr l : local) {
			for (MyAttr g : globle) {
				if (g.getName().equals(l.getName())) {
					continue out;
				}
			}
			return l;
		}
		return null;
	}
	
	public static void mergeAttribute(ArrayList<MyAttr> Globalattributes,
			ArrayList<MyAttr> Localattributes) {
		for (MyAttr localmyattr : Localattributes) {
			boolean found = false;
			for (MyAttr globalmyattr : Globalattributes) {
				if (localmyattr.getName().equals(globalmyattr.getName())) {
					found = true;
					break;
				}
			}
			if (!found) {
				Globalattributes.add(localmyattr);
			}
		}
	}

	private static void updateMCDCTable(AbstractPolicy policy, PolicyTable policytable,
			String request, int start) {
		List<Rule> rules = getRuleFromPolicy(policy);
		for (int i = 0; i < start; i++) {
			if (isDefaultRule(rules.get(i))) {
				continue;
			}
			Rule rule = rules.get(i);
			ArrayList<Integer> tResult = new ArrayList<Integer>();
			int tResult2 = -1; // for ind
			int cResult = -1;
			if (rule.getTarget() != null) {
				tResult = MatchOfTarget((Target) rule.getTarget(), request);

				// printarray(tResult);
				tResult2 = TargetEvaluate((Target) rule.getTarget(), request);
			}
			if (rule.getCondition() != null) {
				cResult = ConditionEvaluate(rule.getCondition(), request);
			}
			// get rule record
			RuleRecord record = policytable.getRules().get(i);
			// first doesn't have target
			if (rule.getTarget() == null) {
				for (ConRecord conRecord : record.getCondition()) {
					if (conRecord.getEffect() == cResult
							&& conRecord.getCovered() == 0) {
						conRecord.setCovered(1);
					}
				}
			} else { // has target
				for (TarRecord tarRecord : record.getTarget()) {
					if (tarRecord.getCovered() == 1)
						continue;
					if ((tarRecord.effect == tResult2) && tarRecord.effect == 2) {
						tarRecord.setCovered(1);
					}
					if (tarRecord.getArray() != null
							&& arrayMatch(tResult, tarRecord.getArray())) {
						if (tarRecord.getEffect() == 1) {
							tarRecord.setCovered(1);
						} else {
							for (ConRecord conRecord : record.getCondition()) {
								if (conRecord.getEffect() == cResult
										&& conRecord.getCovered() == 0) {
									//conRecord.setCovered(1);
								}
							}
						}
					}
				}
			}
		}
	}
	
	public static int TargetEvaluate(Target target, String request) {
		// 0 = match, 1 = no match, 2 = ind
		MatchResult match = null;

		XACML3EvaluationCtx ec;
		ec = getEvaluationCtx(request);
		
		match = target.match(ec);
		//System.err.println("Target match result: " + match.getResult());
		return match.getResult();

	}
	
	public static int ConditionEvaluate(Condition condition, String request) {
		XACML3EvaluationCtx ec;
		ec = getEvaluationCtx(request);
		EvaluationResult result = condition.evaluate(ec);
		if (result.indeterminate()) {
			return 2;
		} else {
			BooleanAttribute bool = (BooleanAttribute) (result
					.getAttributeValue());
			if (bool.getValue()) {
				// if any obligations or advices are defined, evaluates them and
				// return
				return 0;
			} else {
				return 1;
			}
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

	
	public static ArrayList<Integer> MatchOfTarget(Target target, String request) {
		XACML3EvaluationCtx ec;
		ec = getEvaluationCtx(request);
		ArrayList<Integer> result = new ArrayList<Integer>();

		List<AnyOfSelection> anyOfSelections = target.getAnyOfSelections();
		if (anyOfSelections != null) {
			for (AnyOfSelection anyof : anyOfSelections) {
				List<AllOfSelection> allOfSelections = anyof
						.getAllOfSelections();
				if (allOfSelections != null) {
					for (AllOfSelection allof : allOfSelections) {
						for (TargetMatch targetmatch : allof.getMatches()) {
							MatchResult match = null;
							match = targetmatch.match(ec);
							result.add(match.getResult());
						}
					}
				}
			}
		}
		return result;
	}

	private static boolean arrayMatch(ArrayList<Integer> arry1,
			ArrayList<Integer> arry2) {
		if (arry1.size() != arry2.size() || arry1 == null || arry2 == null) {
			return false;
		} else {
			for (int i = 0; i < arry1.size(); i++) {
				if (!arry1.get(i).equals(arry2.get(i))) {
					return false;
				}
			}
		}
		return true;
	}

	private static XACML3EvaluationCtx getEvaluationCtx(String request) {
		RequestCtxFactory rc = new RequestCtxFactory();
		AbstractRequestCtx ar = null;
		try {
			ar = rc.getRequestCtx(request);
		} catch (ParsingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		XACML3EvaluationCtx ec;
		ec = new XACML3EvaluationCtx(new RequestCtx(ar.getAttributesSet(),
				ar.getDocumentRoot()), balana.getPdpConfig());
		return ec;
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
	
	public static PolicyTable buildDecisionCoverage(AbstractPolicy policy) {
		PolicyTable policytable = new PolicyTable();
		List<Rule> rules = getRuleFromPolicy(policy);
		Target target = (Target) policy.getTarget(); // get policy target
		if (target != null) {
			TarRecord record = new TarRecord(2, 0);
			policytable.addTarget(record);
			record = new TarRecord(1, 0);
			policytable.addTarget(record);
			record = new TarRecord(0, 0);
			policytable.addTarget(record);
		}
		for (Rule rule : rules) {
			if (isDefaultRule(rule)) {
				continue;
			}
			RuleRecord rulerecord = new RuleRecord();
			target = (Target) rule.getTarget();
			Condition condition = (Condition) rule.getCondition();
			if (target != null) {
				TarRecord tar = new TarRecord(2, 0);
				rulerecord.addTarget(tar);
				tar = new TarRecord(1, 0);
				rulerecord.addTarget(tar);
				tar = new TarRecord(0, 0);
				rulerecord.addTarget(tar);
			} else {
				// even is empty, need to go next;
				TarRecord tar = new TarRecord(0, 0);
				rulerecord.addTarget(tar);
			}
			if (condition != null) {
				ConRecord con = new ConRecord(2, 0);
				rulerecord.addCondition(con);

				con = new ConRecord(1, 0);
				rulerecord.addCondition(con);

				con = new ConRecord(0, 0);
				rulerecord.addCondition(con);
			}
			policytable.addRule(rulerecord);
		}
		return policytable;
	}

	public static class PolicyTable {
		private ArrayList<TarRecord> target;
		private ArrayList<RuleRecord> ruleRecord;

		public PolicyTable() {
			ruleRecord = new ArrayList<RuleRecord>();
			target = new ArrayList<TarRecord>();
		}

		public void addTarget(TarRecord record) {
			target.add(record);
		}

		public void addRule(RuleRecord record) {
			ruleRecord.add(record);
		}

		public ArrayList<TarRecord> getTarget() {
			return this.target;
		}

		public ArrayList<RuleRecord> getRules() {
			return this.ruleRecord;
		}
	}
	
	public static class TarRecord {
		private ArrayList<Integer> basics;
		private ArrayList<String> tokens;
		public int effect; // 0=T, 1=F, 2=E;
		public int covered; // 0 = not covered, 1 = covered

		public TarRecord(int effect, int covered) {
			this.effect = effect;
			this.covered = covered;
		}

		public TarRecord(ArrayList<Integer> basics, ArrayList<String> tokens,
				int effect, int covered) {
			this(effect, covered);
			this.basics = basics;
			this.tokens = tokens;
			// this.effect = -1;
		}

		public int getCovered() {
			return this.covered;
		}

		public void setCovered(int covered) {
			this.covered = covered;
		}

		public int getEffect() {
			return this.effect;
		}

		public ArrayList<Integer> getArray() {
			return this.basics;
		}

		public ArrayList<String> getTokens() {
			return this.tokens;
		}

		public void setEffect(int effect) {
			this.effect = effect;
		}

		// build z3_input from the arrailists
		public String buildZ3(MCDC_converter2 converter) {
			StringBuffer sb = new StringBuffer();
			if (tokens != null && tokens.size() > 0) {
				sb.append("(and ");
				for (int i = 0; i < tokens.size(); i++) {
					System.out.println(getTokens().get(i).toString());
					if (getArray().get(i).equals(1)) {
						sb.append("(not ");
						sb.append(converter.getMap().get(getTokens().get(i))
								.toString()
								+ " )");
					} else {
						sb.append(converter.getMap().get(getTokens().get(i))
								.toString()
								+ " ");
					}
				}
				sb.append(")");
			}
			return sb.toString();
		}
	}

	public static class RuleRecord {
		// if target or condition is null, the size is 0;
		private ArrayList<TarRecord> target;
		private ArrayList<ConRecord> condition;

		public RuleRecord() {
			target = new ArrayList<TarRecord>();
			condition = new ArrayList<ConRecord>();
		}

		public void addTarget(TarRecord tar) {
			this.target.add(tar);
		}

		public ArrayList<TarRecord> getTarget() {
			return this.target;
		}

		public void addCondition(ConRecord con) {
			this.condition.add(con);
		}

		public ArrayList<ConRecord> getCondition() {
			return this.condition;
		}
	}

	public static class ConRecord {
		public int effect;
		public int covered;

		public ConRecord(int effect, int covered) {
			this.effect = effect;
			this.covered = covered;
		}

		public int getEffect() {
			return this.effect;
		}

		public int getCovered() {
			return this.covered;
		}

		public void setCovered(int covered) {
			this.covered = covered;
		}
	}


	
	

	private static String buildTargetExpression(Target target) {
        return target.encode();
    }

    private static String buildConditionExpression(Condition condition) {
        return condition.encode();
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

	private static boolean isTraversableElement(Node e){
		if(rulePattern.matcher(e.getLocalName()).matches()||policysetPattern.matcher(e.getLocalName()).matches() || policyPattern.matcher(e.getLocalName()).matches()){
			return true;
		} else{
			return false;
		}
	}
}
