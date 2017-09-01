package org.seal.xacml.helpers;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.xml.parsers.ParserConfigurationException;

import org.seal.xacml.Attr;
import org.seal.xacml.utils.ExceptionUtil;
import org.seal.xacml.utils.XMLUtil;
import org.w3c.dom.Node;
import org.wso2.balana.ParsingException;
import org.wso2.balana.PolicyMetaData;
import org.wso2.balana.Rule;
import org.wso2.balana.TargetMatch;
import org.wso2.balana.attr.BooleanAttribute;
import org.wso2.balana.attr.IntegerAttribute;
import org.wso2.balana.attr.StringAttribute;
import org.wso2.balana.attr.xacml3.AttributeDesignator;
import org.wso2.balana.cond.Apply;
import org.wso2.balana.cond.Condition;
import org.wso2.balana.cond.Expression;
import org.wso2.balana.cond.Function;
import org.wso2.balana.xacml3.AllOfSelection;
import org.wso2.balana.xacml3.AnyOfSelection;
import org.wso2.balana.xacml3.Target;
import org.xml.sax.SAXException;

public class Z3StrExpressionHelper {
	private Map<String,String> nameMap;
	private Map<String,String> typeMap;
	private List<Attr> collector;
	private List<Attr> currentRequestCollector;
	
	public Z3StrExpressionHelper(){
		nameMap = new HashMap<String,String>();
		typeMap = new HashMap<String,String>();
		collector = new ArrayList<Attr>();
	}
	
	public Map<String,String> getNameMap(){
		return nameMap;
	}
	
	public Map<String,String> getTypeMap(){
		return typeMap;
	}
	
	public List<Attr> getAttributeList(){
		return currentRequestCollector;
	}
	
	public StringBuffer getTrueTargetExpression(Target target) {
		StringBuffer expr = new StringBuffer();
		if (target != null) {
			for (AnyOfSelection anyofselection : target.getAnyOfSelections()) {
				StringBuilder orBuilder = new StringBuilder();
				for (AllOfSelection allof : anyofselection.getAllOfSelections()) {
					StringBuilder allBuilder = new StringBuilder();
					for (TargetMatch match : allof.getMatches()) {
						if (match.getEval() instanceof AttributeDesignator) {
							AttributeDesignator attribute = (AttributeDesignator) match.getEval();
							if(attribute.getType().toString().contains("boolean")){
								allBuilder.append(getName(attribute));
							}else{
								allBuilder.append(" (" + getOperator(match.getMatchFunction().encode()) + " " + getName(attribute) + " ");
							}
							if (attribute.getType().toString().contains("string")) {
								String value = match.getAttrValue().encode();
								value = value.replaceAll(System.lineSeparator(), "");
								value = value.trim();
								allBuilder.append("\"" + value + "\")");
							}
							if (attribute.getType().toString().contains("integer")) {
								String value = match.getAttrValue().encode();
								value = value.replaceAll(System.lineSeparator(), "");
								value.trim();
								value = value.trim();
								allBuilder.append(value + ")");
							}
							getType(attribute);
							Attr myattr = new Attr(attribute);
							if (!collector.contains(myattr)) {
								collector.add(myattr);
							}
						}
					}
					allBuilder.insert(0, " (and ");
					allBuilder.append(")");
					orBuilder.append(allBuilder);
				}
				orBuilder.insert(0, " (or ");
				orBuilder.append(")");
				expr.append(orBuilder);
			}
			expr = new StringBuffer(expr.toString().trim());
			if(!expr.toString().trim().equals("")){
				expr.insert(0, "(and ");
				expr.append(")");
			}
		}
		return expr.append(System.lineSeparator());
	}
	
	public StringBuffer getFalseTargetExpression(Target target){
		String[] lines = getTrueTargetExpression(target).toString().split("\n");
		StringBuffer expr = new StringBuffer();
		for (String s : lines) {
			if (s.isEmpty()) {
				continue;
			} else {
				StringBuffer subExpr = new StringBuffer();
				subExpr.append("(not ");
				subExpr.append(s);
				subExpr.append(")");
				expr.append(subExpr);
			}
		}
		return expr;
	}
    
	public StringBuffer getTrueConditionExpression(Condition condition) {
		StringBuffer expr = new StringBuffer("");
		if (condition != null) {
			Expression expression = condition.getExpression();
			if (expression instanceof Apply) {
				Apply apply = (Apply) expression;
				expr = ApplyStatements(apply, "", expr);
			}
		}
		return expr.append(System.lineSeparator());
	}
	
	public StringBuffer getFalseConditionExpression(Condition condition){
		String[] lines = getTrueConditionExpression(condition).toString().split("\n");
		StringBuffer expr = new StringBuffer();
		for (String s : lines) {
			if (s.trim().isEmpty()) {
				continue;
			} else {
				StringBuffer subExpr = new StringBuffer();
				subExpr.append("(not ");
				subExpr.append(s);
				subExpr.append(")");
				expr.append(subExpr);
			}
		}
		return expr;
	}
    
	public StringBuffer getFalseTargetFalseConditionExpression(Rule rule) {
		StringBuffer targetsb = new StringBuffer();
		StringBuffer conditionsb = new StringBuffer();
		StringBuffer sb = new StringBuffer();
		Target target = (Target) rule.getTarget();
		Condition condition = (Condition) rule.getCondition();
		if(target == null && condition == null){
			return sb;
		}
		targetsb.append(getTrueTargetExpression(target).toString().trim());
		conditionsb.append(getTrueConditionExpression(condition).toString().trim());
		if(targetsb.length() == 0 && conditionsb.length()==0){
			return sb;
		}
		sb.append("(not (and ");
		sb.append(targetsb);
		sb.append(conditionsb);
		sb.append("))");
		return sb;
	}
	
	public StringBuffer getTrueTargetTrueConditionExpression(Rule rule) {
		StringBuffer targetsb = new StringBuffer();
		StringBuffer conditionsb = new StringBuffer();
		StringBuffer sb = new StringBuffer();
		Target target = (Target) rule.getTarget();
		Condition condition = (Condition) rule.getCondition();
		if((target == null && condition == null)){
			return sb;
		}
		targetsb.append(getTrueTargetExpression(target).toString().trim());
		conditionsb.append(getTrueConditionExpression(condition).toString().trim());
		if((targetsb.length() == 0 && conditionsb.length()==0)){
			return sb;
		}
		sb.append("(and ");
		sb.append(targetsb);
		sb.append(" " + conditionsb);
		sb.append(")");
		return sb;
	}
	
	public String getName(AttributeDesignator attr) {
		String uri = attr.getId().toString();
		boolean has = true;
		if (nameMap.containsKey(uri)) {
			return nameMap.get(uri).toString();
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
			nameMap.put(uri, sb.toString());
			return sb.toString();
		}
	}
	
	public String getType(AttributeDesignator attr) {
		String name = getName(attr);
		String type = attr.getType().toString();
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
	
	public String getOperator(String function){
		if (function.contains("equal") && !function.contains("greater") && !function.contains("less")) {
			if(function.contains("boolean")){
				return "";
			} else{
				return "=";
			}
		} else if (function.contains("greater") && !function.contains("equal")) {
			return ">";
		} else if (function.contains("less") && !function.contains("equal")) {
			return "<";
		} else if (function.contains("less") && function.contains("equal")) {
			return "<=";
		} else if (function.contains("greater") && function.contains("equal")) {
			return ">=";
		} else if (function.contains("notcurrentRequestCollector")){
			return "=";
		} else if (function.contains("at-least-one-member-of")){
			return "=";
		} else {
			return "=";
		}
	}
	
	public StringBuffer ApplyStatements(Apply apply, String function, StringBuffer sb) {
		if (apply.getFunction().encode().contains("urn:oasis:names:tc:xacml:1.0:function:and")) {
			StringBuffer newsb = new StringBuffer();
			for (Object element : apply.getList()) {
				if (element instanceof Apply) {
					Apply childApply = (Apply) element;
					ApplyStatements(childApply, apply.getFunction().toString(),newsb);
				}
			}
			newsb.insert(0, "(and ");
			newsb.append(")");
			sb.append(newsb);
			return sb;
		} else if (apply.getFunction().encode().contains("urn:oasis:names:tc:xacml:1.0:function:or")) {
			StringBuffer newsb = new StringBuffer();
			for (Object element : apply.getList()) {
				if (element instanceof Apply) {
					Apply childApply = (Apply) element;
					ApplyStatements(childApply, apply.getFunction().toString(),newsb);
				}
			}
			newsb.insert(0, "(or ");
			newsb.append(")");
			sb.append(newsb);
			return sb;
		} else if (apply.getFunction().encode().contains("urn:oasis:names:tc:xacml:1.0:function:not")) {
			StringBuffer newsb = new StringBuffer();
			for (Object element : apply.getList()) {
				if (element instanceof Apply) {
					Apply childApply = (Apply) element;
					ApplyStatements(childApply, apply.getFunction().toString(),newsb);
				}
			}
			newsb.insert(0, "(not ");
			newsb.append(")");
			sb.append(newsb);
			return sb;
		} else if (apply.getFunction().encode().contains("string-is-in")) {
			String value = "";
			value = getAttrValue(apply);
			String functionName = getOperator(apply.getFunction().encode());
			sb = buildAttrDesignator(sb, apply, value, functionName);
			return sb;
		} else if (apply.getFunction().encode().contains("string-at-least-one-member-of")) {
			String value = "";
			String functionName = getOperator(apply.getFunction().encode());
			for (Object element : apply.getList()) {
				if (element instanceof Apply) {
					Apply childApply = (Apply) element;
					value = getAttrValue(childApply);
				}
			}
			sb = buildAttrDesignator(sb, apply, value, functionName);
			return sb;
		} else {
			int consecutiveAttrDs = 0;
			boolean first = true;
			String postPart="";
			for (Object element : apply.getList()) {
				String value = null;
				if(element instanceof Function){
					function = ((Function)element).encode();
				}else if (element instanceof IntegerAttribute) {
					IntegerAttribute intValue = (IntegerAttribute) element;
					value = intValue.getValue() + "";
					if(first){
						postPart =  value + ")";
						first = false;
					} else{
						sb.append(value + ")");
					}
					sb.append(value + ")");
					consecutiveAttrDs--;
				}else if (element instanceof StringAttribute) {
					StringAttribute stringValue = (StringAttribute) element;
					value = stringValue.getValue() + "";
					if(first){
						postPart =  "\"" + value + "\")";
						first = false;
					} else{
						sb.append("\"" + value + "\")");
					}
					consecutiveAttrDs--;
				}else if (element instanceof BooleanAttribute) {
					BooleanAttribute booleanValue = (BooleanAttribute) element;
					value = booleanValue.getValue() + "";
					if(first){
						if(value.equals("false")){
							sb.append("not ");
						}
						first = false;
					} else{
						sb = new StringBuffer("not " + sb);
					}
					consecutiveAttrDs--;
				}else if (element instanceof Apply) {
					Apply childApply = (Apply) element;
					ApplyStatements(childApply, apply.getFunction().encode(),sb);
					consecutiveAttrDs--;
				}else if (element instanceof AttributeDesignator) {
					consecutiveAttrDs++;
					AttributeDesignator attribute = (AttributeDesignator) element;
					if(consecutiveAttrDs == 2){
						consecutiveAttrDs = 0;
						sb.append(" " + getName(attribute) + ") ");
					} else{
						if(function.contains("boolean")){
							sb.append(" " + getName(attribute));
						} else {
							sb.append(" (" + getOperator(function) + " " + getName(attribute) + " ");
							if(!first){
								sb.append(postPart);
								first = true;
							}
						}
					}
					getType(attribute);
					Attr attr = new Attr(attribute);
					if (!collector.contains(attr)) {
						collector.add(attr);
					}
				}
			}
		}
		return sb;
	}
	
	private String getAttrValue(Apply apply) {
		String value = "";
		for (Object element : apply.getList()) {
			if (element instanceof IntegerAttribute) {
				IntegerAttribute intValue = (IntegerAttribute) element;
				value = intValue.getValue() + ")";
			}
			if (element instanceof StringAttribute) {
				StringAttribute stringValue = (StringAttribute) element;
				value = "\"" + stringValue.getValue() + "\")";
			}
		}
		return value;
	}
	
	private StringBuffer buildAttrDesignator(StringBuffer sb, Apply apply, String value, String function) {
		for (Object element : apply.getList()) {
			if (element instanceof AttributeDesignator) {
				AttributeDesignator attribute = (AttributeDesignator) element;
				sb.append(" (" + function + " " + getName(attribute) + " " + value);
				getType(attribute);
				Attr attr = new Attr(attribute);
				if (!collector.contains(attr)) {
					collector.add(attr);
				}
			}
		}
		return sb;
	}
	
	public void updateColletor() {
	    List<Attr> tempRequestCollector = new ArrayList<Attr>();
	    try{
	        FileReader fr = new FileReader("./Z3_output");
	        BufferedReader br = new BufferedReader(fr);
	        String s;
	        List<String> currentNameMap = new ArrayList<String>();
	        currentRequestCollector = new ArrayList<Attr>();
	        while ((s = br.readLine()) != null) {
	            String[] data = s.split(" ");
	            int preValueIndex = 0;
	            while (preValueIndex<data.length && !data[preValueIndex].equals("->")) {
	                preValueIndex++;
	            }
	            if (data.length > preValueIndex+1) {
	                Iterator<Map.Entry<String, String>> iter = nameMap.entrySet().iterator();
	                while (iter.hasNext()) {
	                    Map.Entry<String,String> entry = (Map.Entry<String,String>) iter.next();
	                    
	                    if (data[0].equals(entry.getValue())) {
	                    	currentNameMap.add(data[0]);
	                    	for (Attr attr : collector) {
	                            if (attr.getName().equals(entry.getKey())) {
	                                String value = "";
	                                for (int l = preValueIndex+1; l < data.length -1 ; l++) {
	                                    value = value + data[l].toString() + " ";
	                                }
	                                value = value + data[data.length-1].toString();
	                                value = value.replaceAll("\"", "");
	                                attr.setDomain(value);
	                                tempRequestCollector.add(attr);
	                            }
	                        }
	                    }
	                }
	            }
	        }
	        fr.close();
		} catch(Exception e){
			ExceptionUtil.handleInDefaultLevel(e);
		}
        // value should be added after checking type
        for (Attr attr : collector) {
        		if (attr.getDomain().isEmpty()) {
        			attr.addValue("0");
        		}
        		if(tempRequestCollector.contains(attr)){
        			currentRequestCollector.add(attr);
        		}
        }
    }
	
	/*
	public List<ExpressionWithTruthValue> getMCDCExpssions(Target target){
		List<ExpressionWithTruthValue> expressions = new ArrayList<ExpressionWithTruthValue>();
		if (target != null) {
			for (AnyOfSelection anyofselection : target.getAnyOfSelections()) {
				int n = anyofselection.getAllOfSelections().size() + 1;
				for(int i = 0; i< n;i++){import org.seal.xacml.components.ExpressionWithTruthValue;

					int k = 0;
					StringBuilder orBuilder = new StringBuilder();
					for (AllOfSelection allof : anyofselection.getAllOfSelections()) {
						StringBuilder allBuilder = new StringBuilder();
						for (TargetMatch match : allof.getMatches()) {
							if (match.getEval() instanceof AttributeDesignator) {
								AttributeDesignator attribute = (AttributeDesignator) match.getEval();
								if(attribute.getType().toString().contains("boolean")){
									allBuilder.append(getName(attribute));
								}else{
									allBuilder.append(" (" + getOperator(match.getMatchFunction().encode()) + " " + getName(attribute) + " ");
								}
								if (attribute.getType().toString().contains("string")) {
									String value = match.getAttrValue().encode();
									value = value.replaceAll(System.lineSeparator(), "");
									value = value.trim();
									allBuilder.append("\"" + value + "\")");
								}
								if (attribute.getType().toString().contains("integer")) {
									String value = match.getAttrValue().encode();
									value = value.replaceAll(System.lineSeparator(), "");
									value.trim();
									value = value.trim();
									allBuilder.append(value + ")");
								}
								
								getType(attribute);
								Attr myattr = new Attr(attribute);
								if (!collector.contains(myattr)) {
									collector.add(myattr);
								}
							}
						}
						allBuilder.insert(0, " (and ");
						allBuilder.append(")");
						if(i > 0 && k==(i-1)){
							orBuilder.append(allBuilder);
						} else{
							orBuilder.append("(not " + allBuilder + ")");
						}
						k++;
					}
					orBuilder.insert(0, " (or ");
					orBuilder.append(")");
					expressions.add(new ExpressionWithTruthValue(orBuilder.toString(),true));
				}
			}
		}
		return expressions;
	} */
	
	public List<String> getMCDCExpssions(Target target){
		List<String> expressions = new ArrayList<String>();
		if (target != null) {
			for (AnyOfSelection anyofselection : target.getAnyOfSelections()) {
				int n = anyofselection.getAllOfSelections().size() + 1;
				for(int i = 0; i< n;i++){
					List<StringBuilder> orBuilders = new ArrayList<StringBuilder>();
					
					int k = 0;
					StringBuilder orBuilder = new StringBuilder();
					for (AllOfSelection allof : anyofselection.getAllOfSelections()) {
						int m = allof.getMatches().size() + 1;
						List<StringBuilder> allBuilders = new ArrayList<StringBuilder>();
						for(int l = 0; l < m; l++){
							StringBuilder allBuilder = new StringBuilder();
							int p = 0;
							for (TargetMatch match : allof.getMatches()) {
								if (match.getEval() instanceof AttributeDesignator) {
									AttributeDesignator attribute = (AttributeDesignator) match.getEval();
									StringBuilder nb = new StringBuilder();
									if(attribute.getType().toString().contains("boolean")){
										nb.append(getName(attribute));
									}else{
										nb.append(" (" + getOperator(match.getMatchFunction().encode()) + " " + getName(attribute) + " ");
									}
									if (attribute.getType().toString().contains("string")) {
										String value = match.getAttrValue().encode();
										value = value.replaceAll(System.lineSeparator(), "");
										value = value.trim();
										nb.append("\"" + value + "\")");
									}
									if (attribute.getType().toString().contains("integer")) {
										String value = match.getAttrValue().encode();
										value = value.replaceAll(System.lineSeparator(), "");
										value.trim();
										value = value.trim();
										nb.append(value + ")");
									}
									
									if(l > 0 && p ==(l-1)){
										nb.insert(0,"(not ");
										nb.append(")");
									} 
									allBuilder.append(nb);
									getType(attribute);
									Attr myattr = new Attr(attribute);
									if (!collector.contains(myattr)) {
										collector.add(myattr);
									}
								}
								p++;
							}
							
							allBuilder.insert(0, " (and ");
							allBuilder.append(")");
							allBuilders.add(allBuilder);
						}
						List<StringBuilder> oldBuilders = new ArrayList<StringBuilder>();
						oldBuilders.addAll(orBuilders);
						orBuilders = new ArrayList<StringBuilder>();
						if(oldBuilders.size() > 0){
							for(StringBuilder ob:oldBuilders){
								
								for(StringBuilder ab:allBuilders){
									StringBuilder sb = new StringBuilder(ob.toString());
									if(i > 0 && k==(i-1)){
										sb.append(ab);
									} else{
										sb.append("(not " + ab + ")");
									}
									orBuilders.add(sb);
								}
							}
						} else{
							for(StringBuilder ab:allBuilders){
								StringBuilder ob = new StringBuilder();
								if(i > 0 && k==(i-1)){
									ob.append(ab);
								} else{
									ob.append("(not " + ab + ")");
								}
								orBuilders.add(ob);
							}
						}
						k++;
					}
					for(StringBuilder ob:orBuilders){
						ob.insert(0, " (or ");
						ob.append(")");
						expressions.add(ob.toString());
					}
					
				}
			}
		}
		return expressions;
	}
	
	public List<String> getRPTEExpression(Target target,PolicyMetaData policyMetaData) throws ParsingException, IOException, SAXException, ParserConfigurationException{
		String targetStr = null;
		if(target!=null){
			StringBuilder targetSb = new StringBuilder();
			target.encode(targetSb);
			targetStr = targetSb.toString();
		}
		List<String> expressions = new ArrayList<String>();
    	if(targetStr!=null){
    		Node targetNode = XMLUtil.loadXMLDocumentFromString(targetStr);

    		if (targetNode != null) {
	            List<Node> children = XMLUtil.getChildNodeList(targetNode.getChildNodes().item(0));
	            
	            int allOfCount = 0;
	            if(children.size()>0){
		            for (Node child : children) {
		            	if(child!=null && child.getLocalName() !=null && child.getLocalName().equals("AnyOf")){
		            		List<Node> childrenAllOf = XMLUtil.getChildNodeList(child);
		            		if(childrenAllOf.size() > 0){
		            			for(Node childAllOf:childrenAllOf){
		            				if(childAllOf.getLocalName() !=null && childAllOf.getLocalName().equals("AllOf")){
		            					allOfCount++;
		            				}
		            			}
		            			if(allOfCount > 1){
		            				for(int i = 0; i < childrenAllOf.size(); i++){
		            					Node childAllOf = childrenAllOf.get(i);
		            					if(childAllOf.getLocalName() !=null && childAllOf.getLocalName().equals("AllOf")){
		            						StringBuilder currentPreExpression = new StringBuilder();
		            						child.removeChild(childAllOf);
		            						Node nextChild = childAllOf.getNextSibling();
		            						currentPreExpression.append(getFalseTargetExpression(target));
		            						List<Node> childrenAllOfCopy = new ArrayList<Node>();
		            						childrenAllOfCopy.addAll(XMLUtil.getChildNodeList(child));
		            						
		            						for(Node c:childrenAllOfCopy){
		            							child.removeChild(c);
		            						}
		            						child.appendChild(childAllOf);
		            						Target targetTrue = Target.getInstance(targetNode, policyMetaData);
		            						currentPreExpression.append(System.lineSeparator()).append(getTrueTargetExpression(targetTrue));
		            						child.removeChild(childAllOf);
		            						for(Node c:childrenAllOfCopy){ 
		            							child.appendChild(c);
		            						}
		            						child.insertBefore(childAllOf, nextChild);
		            						expressions.add(currentPreExpression.toString());
		            					}
		            					
		            				}
		            			}
		            		}
		            	}
		            }
	            }
	        }
		}
    	return expressions;
    }
	
	
}
