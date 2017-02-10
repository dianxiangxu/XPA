package org.seal.combiningalgorithms;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.seal.coverage.PolicySpreadSheetTestRecord;
import org.seal.coverage.PolicySpreadSheetTestSuite;
import org.seal.gui.TestPanel;
import org.seal.gui.XPA;
import org.seal.mcdc.MCDCConditionSet;
import org.seal.mcdc.MCDC_converter2;
import org.seal.mutation.PolicyMutator;
import org.wso2.balana.Balana;
import org.wso2.balana.MatchResult;
import org.wso2.balana.Policy;
import org.wso2.balana.ParsingException;
import org.wso2.balana.PolicyTreeElement;
import org.wso2.balana.Rule;
import org.wso2.balana.TargetMatch;
import org.wso2.balana.attr.BooleanAttribute;
import org.wso2.balana.attr.IntegerAttribute;
import org.wso2.balana.attr.StringAttribute;
import org.wso2.balana.attr.xacml3.AttributeDesignator;
import org.wso2.balana.combine.CombinerElement;
import org.wso2.balana.combine.CombiningAlgorithm;
import org.wso2.balana.combine.xacml2.FirstApplicableRuleAlg;
import org.wso2.balana.combine.xacml3.DenyOverridesRuleAlg;
import org.wso2.balana.combine.xacml3.DenyUnlessPermitRuleAlg;
import org.wso2.balana.combine.xacml3.PermitOverridesRuleAlg;
import org.wso2.balana.combine.xacml3.PermitUnlessDenyRuleAlg;
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

public class PolicyX {

	private static Balana balana;
	String policyName;
	algorithm al = new algorithm();
	Call_Z3str z3 = new Call_Z3str();
	public HashMap nameMap = new HashMap();
	public HashMap typeMap = new HashMap();
	public HashMap valueMap = new HashMap();
	
	private LinkedHashMap<String, String> vMap = new LinkedHashMap<String, String>();
	private LinkedHashMap<String, String> tMap = new LinkedHashMap<String, String>();
	private LinkedHashMap<String, String> rMap = new LinkedHashMap<String, String>();
	private LinkedHashMap<String, String> funcMap = new LinkedHashMap<String, String>();
	private LinkedHashMap<String, String> catMap = new LinkedHashMap<String, String>();
	
	private boolean[][] rule_table;
	private final int TRUE_TRUE = 0;
	private final int TRUE_FALSE = 1;
	private final int FALSE_TRUE = 2;
	private final int FALSE_FALSE = 3;
	private final int IND_TRUE = 4;
	
	String int_function = "urn:oasis:names:tc:xacml:1.0:function:integer-equal";
	String str_function = "urn:oasis:names:tc:xacml:1.0:function:string-equal";
	String int_function_one_and_only = "urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only";
	String str_function_one_and_only = "urn:oasis:names:tc:xacml:1.0:function:string-one-and-only";
	String str_value = "RANDOM$@^$%#&!";
	String str_value1 = "str_A";
	String str_value2 = "str_B";
	String int_value1 = "123456789";
	String int_value2 = "-987654321";
	
	private boolean[][] fault_table;
	

	private String getName(String name) {
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

	private String getType(String name, String type) {
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

	Policy policy;

	public PolicyX(Policy policy) {
		this.policy = policy;
		this.policyName = policy.getId().toString();
		this.rule_table = new boolean[getRuleFromPolicy(policy).size()][7];
		this.fault_table = new boolean[getRuleFromPolicy(policy).size()][2];
		// System.out.println(policyName);
	}

	public Vector<Vector<Object>> generateRequestForDifferenceRCAs()
			throws IOException {
		function f = new function();
		System.out.println(f.checkDefaultRule(policy) + "default rule");
		Vector<Vector<Object>> result = new Vector<Vector<Object>>();
		if (policy.getCombiningAlg() instanceof DenyOverridesRuleAlg) {
			result = generateRequestsForDenyOverrides();
		} else if (policy.getCombiningAlg() instanceof PermitOverridesRuleAlg) {
			result = generateRequestsForPermitOverrides();
		} else if (policy.getCombiningAlg() instanceof DenyUnlessPermitRuleAlg) {
			result = generateRequestsForDenyUnlessPermit();
		} else if (policy.getCombiningAlg() instanceof PermitUnlessDenyRuleAlg) {
			result = generateRequestsForPermitUnlessDeny();
		} else if (policy.getCombiningAlg() instanceof FirstApplicableRuleAlg) {
			result = generateRequestsForFirstApplicable();
		}

		for (Vector<Object> child : result) {
			if (child.get(1).toString().equals("0")) {
				child.set(1, "Pemrit");
			} else if (child.get(1).toString().equals("1")) {
				child.set(1, "Deny");
			} else if (child.get(1).toString().equals("2")) {
				child.set(1, "Indeterminate");
			} else if (child.get(1).toString().equals("3")) {
				child.set(1, "NotApplicable");
			} else if (child.get(1).toString().equals("4")) {
				child.set(1, "ID");
			} else if (child.get(1).toString().equals("5")) {
				child.set(1, "IP");
			} else if (child.get(1).toString().equals("6")) {
				child.set(1, "IDP");
			}

			if (child.get(3).toString().equals("0")) {
				child.set(3, "Pemrit");
			} else if (child.get(3).toString().equals("1")) {
				child.set(3, "Deny");
			} else if (child.get(3).toString().equals("2")) {
				child.set(3, "Indeterminate");
			} else if (child.get(3).toString().equals("3")) {
				child.set(3, "NotApplicable");
			} else if (child.get(3).toString().equals("4")) {
				child.set(3, "ID");
			} else if (child.get(3).toString().equals("5")) {
				child.set(3, "IP");
			} else if (child.get(3).toString().equals("6")) {
				child.set(3, "IDP");
			}
		}

		return result;
	}

	private Vector<Object> createVector(String alg1, String result1,
			String alg2, String result2, String request) {
		Vector<Object> data1 = new Vector<Object>();
		data1.add(alg1);
		data1.add(result1);
		data1.add(alg2);
		data1.add(result2);
		data1.add(request);
		System.out.println(request + " request");
		return data1;
	}

	public Vector<Vector<Object>> generateRequestsForDenyOverrides()
			throws IOException {
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		PolicyEvaluation pe = new PolicyEvaluation();
		// ArrayList<QueryForDifference> requests = new
		// ArrayList<QueryForDifference>();
		String request1 = Deny_Permit_Override();
		if (request1 != "") {

			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides");
			Vector<Object> data1 = createVector("DenyOverrides",
					PolicyEvaluate(policy, request1) + "", "PermitOverrides",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("DenyOverrides", "null",
					"PermitOverrides", "null", "");
			data.add(data1);
		}

		request1 = DenyOverride_DenyUnlessPermit();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit");
			Vector<Object> data1 = createVector("DenyOverrides",
					PolicyEvaluate(policy, request1) + "", "DenyUnlessPermit",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("DenyOverrides", "null",
					"DenyUnlessPermit", "null", "");
			data.add(data1);
		}

		request1 = DenyOverride_PermitUnlessDeny();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-unless-deny");
			Vector<Object> data1 = createVector("DenyOverrides",
					PolicyEvaluate(policy, request1) + "", "PermitUnlessDeny",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("DenyOverrides", "null",
					"PemritUnlessDeny", "null", "");
			data.add(data1);
		}

		request1 = DenyOverride_FirstApplicable();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable");
			Vector<Object> data1 = createVector("DenyOverrides",
					PolicyEvaluate(policy, request1) + "", "FirstApplicable",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("DenyOverrides", "null",
					"FirstApplicable", "null", "");
			data.add(data1);
		}

		pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides");
		return data;
	}

	public Vector<Vector<Object>> generateRequestsForPermitOverrides()
			throws IOException {
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		PolicyEvaluation pe = new PolicyEvaluation();
		// ArrayList<QueryForDifference> requests = new
		// ArrayList<QueryForDifference>();
		String request1 = Deny_Permit_Override();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides");
			Vector<Object> data1 = createVector("PermitOverrides",
					PolicyEvaluate(policy, request1) + "", "DenyOverrides",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("PermitOverrides", "null",
					"DenyOverrides", "null", "");
			data.add(data1);
		}
		request1 = PermitOverride_DenyUnlessPermit();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit");
			Vector<Object> data1 = createVector("PermitOverrides",
					PolicyEvaluate(policy, request1) + "", "DenyUnlessPermit",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("PermitOverrides", "null",
					"DenyUnlessPermit", "null", "");
			data.add(data1);
		}

		request1 = PermitOverride_PermitUnlessDeny();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-unless-deny");
			Vector<Object> data1 = createVector("PermitOverrides",
					PolicyEvaluate(policy, request1) + "", "PermitUnlessDeny",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("PermitOverrides", "null",
					"PermitUnlessDeny", "null", "");
			data.add(data1);
		}
		request1 = PermitOverride_FirstApplicable();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable");
			Vector<Object> data1 = createVector("PermitOverrides",
					PolicyEvaluate(policy, request1) + "", "FirstApplicable",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("PermitOverrides", "null",
					"FirstApplicalbe", "null", "");
			data.add(data1);
		}
		pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides");
		return data;
	}

	public Vector<Vector<Object>> generateRequestsForDenyUnlessPermit()
			throws IOException {
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		PolicyEvaluation pe = new PolicyEvaluation();
		// ArrayList<QueryForDifference> requests = new
		// ArrayList<QueryForDifference>();
		String request1 = DenyOverride_DenyUnlessPermit();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides");
			Vector<Object> data1 = createVector("DenyUnlessPermit",
					PolicyEvaluate(policy, request1) + "", "DenyOverrides",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("DenyUnlessPermit", "null",
					"DenyOverrides", "null", "");
			data.add(data1);
		}
		request1 = PermitOverride_DenyUnlessPermit();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides");
			Vector<Object> data1 = createVector("DenyUnlessPermit",
					PolicyEvaluate(policy, request1) + "", "PermitOverrides",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("DenyUnlessPermit", "null",
					"PermitOverrides", "null", "");
			data.add(data1);
		}
		request1 = DenyUnlessPermit_PermitUnlessDeny();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-unless-deny");
			Vector<Object> data1 = createVector("DenyUnlessPermit",
					PolicyEvaluate(policy, request1) + "", "PermitUnlessDeny",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("DenyUnlessPermit", "null",
					"PermitUnlessDeny", "null", "");
			data.add(data1);
		}
		request1 = DenyUnlessPermit_FirstApplicable();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable");
			Vector<Object> data1 = createVector("DenyUnlessPermit",
					PolicyEvaluate(policy, request1) + "", "FirstApplicable",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("DenyUnlessPermit", "null",
					"FirstApplicable", "null", "");
			data.add(data1);
		}
		pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit");
		return data;
	}

	public Vector<Vector<Object>> generateRequestsForPermitUnlessDeny()
			throws IOException {
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		PolicyEvaluation pe = new PolicyEvaluation();
		// ArrayList<QueryForDifference> requests = new
		// ArrayList<QueryForDifference>();
		String request1 = DenyOverride_PermitUnlessDeny();
		// System.out.println("this is test" + PolicyEvaluate(policy, ""));
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides");
			Vector<Object> data1 = createVector("PermitUnlessDeny",
					PolicyEvaluate(policy, request1) + "", "DenyOverrides",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("PermitUnlessDeny", "null",
					"DenyOverrides", "null", "");
			data.add(data1);
		}
		request1 = PermitOverride_PermitUnlessDeny();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides");
			Vector<Object> data1 = createVector("PermitUnlessDeny",
					PolicyEvaluate(policy, request1) + "", "PermitOverrides",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("PermitUnlessDeny", "null",
					"PermitOverrides", "null", "");
			data.add(data1);
		}
		request1 = DenyUnlessPermit_PermitUnlessDeny();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit");
			Vector<Object> data1 = createVector("PermitUnlessDeny",
					PolicyEvaluate(policy, request1) + "", "DenyUnlessPermit",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("PermitUnlessDeny", "null",
					"DenyUnlessPermit", "null", "");
			data.add(data1);
		}
		request1 = PermitUnlessDeny_FirstApplicable();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable");
			Vector<Object> data1 = createVector("PermitUnlessDeny",
					PolicyEvaluate(policy, request1) + "", "FirstApplicable",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("PermitUnlessDeny", "null",
					"FirstApplicalbe", "null", "");
			data.add(data1);
		}
		pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-unless-deny");
		return data;
	}

	public Vector<Vector<Object>> generateRequestsForFirstApplicable()
			throws IOException {
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		PolicyEvaluation pe = new PolicyEvaluation();
		// ArrayList<QueryForDifference> requests = new
		// ArrayList<QueryForDifference>();
		System.out.println("DF START");
		String request1 = DenyOverride_FirstApplicable();
		System.out.println("DF DONE");
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides");
			Vector<Object> data1 = createVector("FirstApplicable",
					PolicyEvaluate(policy, request1) + "", "DenyOverrides",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("FirstApplicable", "null",
					"DenyOverrides", "null", "");
			data.add(data1);
			System.out.println("*********************************");
		}
		request1 = PermitOverride_FirstApplicable();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides");
			Vector<Object> data1 = createVector("FirstApplicable",
					PolicyEvaluate(policy, request1) + "", "PermitOverrides",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("FirstApplicable", "null",
					"PermitOverrides", "null", "");
			data.add(data1);
		}
		request1 = DenyUnlessPermit_FirstApplicable();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit");
			Vector<Object> data1 = createVector("FirstApplicable",
					PolicyEvaluate(policy, request1) + "", "DenyUnlessPermit",
					pe.evaluation(request1), request1);
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("FirstApplicable", "null",
					"DenyUnlessPermit", "null", "");
			data.add(data1);
		}
		request1 = PermitUnlessDeny_FirstApplicable();
		if (request1 != "") {
			pe.modifyAlg("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-unless-deny");
			System.out.println("modify to pud here");
			Vector<Object> data1 = createVector("FirstApplicable",
					PolicyEvaluate(policy, request1) + "", "PermitUnlessDeny",
					pe.evaluation(request1), request1);
			System.out.println(data1.size());
			data.add(data1);
		} else {
			Vector<Object> data1 = createVector("FirstApplicable", "null",
					"PermitUnlessPermit", "null", "");
			data.add(data1);
		}
		pe.modifyAlg("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable");
		System.out.println(data.size() + "data size");
		return data;
	}

	ArrayList<MyAttr> attributes = new ArrayList<MyAttr>();

	public void generateTest(TestPanel testPanel) {
		PolicySpreadSheetTestSuite byOneTestSuite = new PolicySpreadSheetTestSuite(
				generate_OneTrue(testPanel), "GenTests/");
		byOneTestSuite.writeToExcelFile(testPanel
				.getTestsuiteXLSfileName("OneTrue"));

		PolicySpreadSheetTestSuite OnetrueOtherFalse = new PolicySpreadSheetTestSuite(
				generate_OneTrueOtherFalse(testPanel), "GenTests/");
		OnetrueOtherFalse.writeToExcelFile(testPanel
				.getTestsuiteXLSfileName("OneTrueOtherFalse"));

		PolicySpreadSheetTestSuite ByTwo = new PolicySpreadSheetTestSuite(
				generate_ByTwo(testPanel), "GenTests/");
		ByTwo.writeToExcelFile(testPanel.getTestsuiteXLSfileName("ByTwo"));

		PolicySpreadSheetTestSuite ByDenyPermit = new PolicySpreadSheetTestSuite(
				generate_ByDenyPermit(testPanel), "GenTests/");
		ByDenyPermit.writeToExcelFile(testPanel
				.getTestsuiteXLSfileName("PermitDenyCombine"));

	}

	public ArrayList<PolicySpreadSheetTestRecord> generate_OneTrue(
			TestPanel testPanel) {

		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
		List<Rule> rules = getRuleFromPolicy(policy);
		function f = new function();
		int count = 1;
		int ruleNo = 0;
		File file = new File(testPanel.getTestOutputDestination("_Basic"));
		if (!file.isDirectory() && !file.exists()) {
			file.mkdir();
		} else {
			f.deleteFile(file);
		}

		for (Rule rule : rules) {
			if (rule.getCondition() == null && rule.getTarget() == null) {
				boolean success = generateDefaultRule(generator,
						testPanel, ruleNo, rules, count, "_Basic");
				if(success)
					count++;
				continue;
			}
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			StringBuffer sb = new StringBuffer();
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			sb.append(True_Target((Target) rule.getTarget(), collector) + "\n");
			sb.append(True_Condition(rule.getCondition(), collector) + "\n");
			boolean sat = z3str(sb.toString(), nameMap, typeMap);
			if (sat == true) {
				System.out.println(nameMap.size() + " map size");
				try {
					z3.getValue(collector, nameMap);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(collector.size() + "   collector size");
				String request = f.print(collector);
				try {
					String path = testPanel.getTestOutputDestination("_Basic")
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

		return generator;
	}

	public ArrayList<PolicySpreadSheetTestRecord> generate_OneTrueOtherFalse(
			TestPanel testPanel) {
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
						testPanel, ruleNo, rules, count, "_Exclusive");
				if(success)
					count++;
				continue;
			}
			sb = new StringBuffer();
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			sb.append(True_Target((Target) rule.getTarget(), collector) + "\n");
			sb.append(True_Condition(rule.getCondition(), collector) + "\n");
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

	public ArrayList<PolicySpreadSheetTestRecord> generate_ByTwo(
			TestPanel testPanel) {
		// two true, how about others?
		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();

		List<Rule> rules = getRuleFromPolicy(policy);
		function f = new function();
		int count = 1;

		File file = new File(testPanel.getTestOutputDestination("_Pair"));
		if (!file.isDirectory() && !file.exists()) {
			file.mkdir();
		} else {
			f.deleteFile(file);
		}
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < rules.size() ; i++) {
			Rule rule1 = rules.get(i);
			for (int j  = i; j <rules.size() ; j++) {
				Rule rule2 = rules.get(j);
				if (rule1.getId().toString().equals(rule2.getId().toString())) {
					continue;
				}
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				StringBuffer sb = new StringBuffer();
				sb.append(TruePolicyTarget(policy, collector) + "\n");
				sb.append(True_Target((Target) rule1.getTarget(), collector)
						+ "\n");
				sb.append(True_Condition(rule1.getCondition(), collector)
						+ "\n");
				sb.append(True_Target((Target) rule2.getTarget(), collector)
						+ "\n");
				sb.append(True_Condition(rule2.getCondition(), collector)
						+ "\n");
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
								.getTestOutputDestination("_Pair")
								+ File.separator + "request" + count + ".txt";

						FileWriter fw = new FileWriter(path);
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(request);
						bw.close();

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
							PolicySpreadSheetTestSuite.TEST_KEYWORD + " "
									+ count, "request" + count + ".txt",
							request, "");
					generator.add(psstr);
					count++;
				}
			}

		}
		long endTime = System.currentTimeMillis();
		System.out.println("Test generation time： " + (endTime - startTime) + "ms");
		return generator;
	}

	public ArrayList<PolicySpreadSheetTestRecord> generate_ByDenyPermit(
			TestPanel testPanel) {
		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
		List<Rule> pRules = getPermitRuleFromPolicy(policy);
		List<Rule> dRules = getDenyRuleFromPolicy(policy);
		function f = new function();
		int count = 1;

		File file = new File(testPanel.getTestOutputDestination("_PDpair"));
		if (!file.isDirectory() && !file.exists()) {
			file.mkdir();
		} else {
			f.deleteFile(file);
		}
		long startTime = System.currentTimeMillis();
		for (Rule prule : pRules) {
			for (Rule drule : dRules) {
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				StringBuffer sb = new StringBuffer();
				sb.append(TruePolicyTarget(policy, collector) + "\n");
				sb.append(True_Target((Target) prule.getTarget(), collector)
						+ "\n");
				sb.append(True_Condition(prule.getCondition(), collector)
						+ "\n");
				sb.append(True_Target((Target) drule.getTarget(), collector)
						+ "\n");
				sb.append(True_Condition(drule.getCondition(), collector)
						+ "\n");
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
								.getTestOutputDestination("_PDpair")
								+ File.separator + "request" + count + ".txt";

						FileWriter fw = new FileWriter(path);
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(request);
						bw.close();

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
							PolicySpreadSheetTestSuite.TEST_KEYWORD + " "
									+ count, "request" + count + ".txt",
							request, "");
					generator.add(psstr);
					count++;
				}
			}
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Test generation time： " + (endTime - startTime) + "ms");
		return generator;
	}


	public String getTargetAttribute(Target target,
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

	public String getTargetAttribute(Target target, ArrayList<MyAttr> collector) {
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
	
	private ArrayList<String> getNegativeTargetAttributes(Target target)
	{
		ArrayList<String> attributes = new ArrayList<String>();
		if(target != null)
		{
			List<AnyOfSelection> anyOf = target.getAnyOfSelections();
			for(AnyOfSelection any : anyOf)
			{
				List<AllOfSelection> allOf = any.getAllOfSelections();
				for(AllOfSelection all : allOf)
				{
					StringBuffer sb = new StringBuffer();
					List<TargetMatch> matches = all.getMatches();
					for(TargetMatch match : matches)
					{
						if(match.getEval() instanceof AttributeDesignator)
						{
							AttributeDesignator attr = (AttributeDesignator)match.getEval();
							sb.append(" (not ("
									+ al.returnFunction(match.getMatchFunction().encode())
									+ " " + getName(attr.getId().toString())
									+ " ");
							if(attr.getType().toString().contains("string"))
							{
								String val = match.getAttrValue().encode();
								val = val.replaceAll("\n", "");
								val = val.trim();
								sb.append("\"" + val + "\"))");
							}
							if(attr.getType().toString().contains("integer"))
							{
								String val = match.getAttrValue().encode();
								val = val.replaceAll("\n", "");
								val = val.trim();
								sb.append(val + "))");
							}
							attributes.add(sb.toString());
						}
					}
				}
			}
		}
		return attributes; 
	}
	
	private ArrayList<String> getTargetAttributes(Target target)
	{
		ArrayList<String> attributes = new ArrayList<String>();
		if(target != null)
		{
			List<AnyOfSelection> anyOf = target.getAnyOfSelections();
			for(AnyOfSelection any : anyOf)
			{
				List<AllOfSelection> allOf = any.getAllOfSelections();
				for(AllOfSelection all : allOf)
				{
					StringBuffer sb = new StringBuffer();
					List<TargetMatch> matches = all.getMatches();
					for(TargetMatch match : matches)
					{
						if(match.getEval() instanceof AttributeDesignator)
						{
							AttributeDesignator attr = (AttributeDesignator)match.getEval();
							sb.append(" ("
									+ al.returnFunction(match.getMatchFunction().encode())
									+ " " + getName(attr.getId().toString())
									+ " ");
							if(attr.getType().toString().contains("string"))
							{
								String val = match.getAttrValue().encode();
								val = val.replaceAll("\n", "");
								val = val.trim();
								sb.append("\"" + val + "\")");
							}
							if(attr.getType().toString().contains("integer"))
							{
								String val = match.getAttrValue().encode();
								val = val.replaceAll("\n", "");
								val = val.trim();
								sb.append(val + ")");
							}
							attributes.add(sb.toString());
						}
					}
				}
			}
		}
		return attributes; 
	}
	
	//Grab specified target attribute value and negate it
	public String getTargetAttribute_NegateValue(Target target, int alindex, int atindex, ArrayList<MyAttr> collector)
	{
		StringBuffer sb = new StringBuffer();
		if(target != null)
		{
			List<AnyOfSelection> anyOf = target.getAnyOfSelections();
			for(int i = 0; i < anyOf.size(); i++)
			{
				AnyOfSelection any = anyOf.get(i);
				StringBuffer orBuilder = new StringBuffer();
				List<AllOfSelection> allOf = any.getAllOfSelections();
				for(int j = 0; j < allOf.size(); j++)
				{
					if(j != alindex)
						continue;
					AllOfSelection all = allOf.get(j);
					StringBuffer allBuilder = new StringBuffer();
					List<TargetMatch> matches = all.getMatches();
					for(int k = 0; k < matches.size(); k++)
					{
						TargetMatch match = matches.get(k);
						if(match.getEval() instanceof AttributeDesignator)
						{
							AttributeDesignator attr = (AttributeDesignator)match.getEval();
							//String rand_id = randomAttribute();
							//String rand_cat = randomAttribute();
							if(k == atindex)
							{
								allBuilder.append(" ("
										+ al.returnFunction(match.getMatchFunction().encode())
										+ " " + getName(attr.getId().toString())
										+ " ");
								if(attr.getType().toString().contains("string"))
									allBuilder.append("\"" + "r@nd0m" + "\")");
								if(attr.getType().toString().contains("integer"))
									allBuilder.append(1234567890 + ")");
							}
							else
							{
								allBuilder.append(" ("
										+ al.returnFunction(match.getMatchFunction().encode())
										+ " " + getName(attr.getId().toString())
										+ " ");
								if(attr.getType().toString().contains("string"))
								{
									String val = match.getAttrValue().encode();
									val = val.replaceAll("\n", "");
									val = val.trim();
									allBuilder.append("\"" + val + "\")");
								}
								if(attr.getType().toString().contains("integer"))
								{
									String val = match.getAttrValue().encode();
									val = val.replaceAll("\n", "");
									val = val.trim();
									allBuilder.append(val + ")");
								}
							}
							getType(getName(attr.getId().toString()),
									attr.getType().toString());
							MyAttr myattr = new MyAttr(attr.getId().toString(), attr.getCategory().toString(), attr.getType().toString());
							if (isExist(collector, myattr) == false) 
								collector.add(myattr);
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
	
	//If we are at the last Match statement, ensure this statement is reachable within the rule
	//make this last Match statement evaluate to false by using a value that is always false
	//this forces an N/A evaluation for the policy and RE for the mutant.
	//If we are not the last Match statement, only need to ensure the statement is reachable
	//all other statements after that don't matter. This forces an N/A eval for the policy and IND
	//eval for the mutant. However, if we are the first Match statement, we only make the statement immediately
	//following eval to True.
	//Only used when PCA = First-Applicable
	public String getTargetAttribute_FALSEIND(Target target, int alindex, int atindex, ArrayList<MyAttr> collector)
	{
		StringBuffer sb = new StringBuffer();
		if(target != null)
		{
			List<AnyOfSelection> anyOf = target.getAnyOfSelections();
			for(int i = 0; i < anyOf.size(); i++)
			{
				AnyOfSelection any = anyOf.get(i);
				StringBuffer orBuilder = new StringBuffer();
				List<AllOfSelection> allOf = any.getAllOfSelections();
				for(int j = 0; j < allOf.size(); j++)
				{
					if(j != alindex)
						continue;
					AllOfSelection all = allOf.get(j);
					StringBuffer allBuilder = new StringBuffer();
					List<TargetMatch> matches = all.getMatches();
					if(atindex == matches.size() - 1)
					{
						for(int k = 0; k < matches.size(); k++)
						{
							TargetMatch match = matches.get(k);
							if(match.getEval() instanceof AttributeDesignator)
							{
								AttributeDesignator attr = (AttributeDesignator)match.getEval();
								if(k == atindex)
								{
									allBuilder.append(" ("
											+ al.returnFunction(match.getMatchFunction().encode())
											+ " " + getName(attr.getId().toString())
											+ " ");
									if(attr.getType().toString().contains("string"))
										allBuilder.append("\"" + "r@nd0m" + "\")");
									if(attr.getType().toString().contains("integer"))
										allBuilder.append(1234567890 + ")");
								}
								else
								{
									allBuilder.append(" ("
											+ al.returnFunction(match.getMatchFunction().encode())
											+ " " + getName(attr.getId().toString())
											+ " ");
									if(attr.getType().toString().contains("string"))
									{
										String val = match.getAttrValue().encode();
										val = val.replaceAll("\n", "");
										val = val.trim();
										allBuilder.append("\"" + val + "\")");
									}
									if(attr.getType().toString().contains("integer"))
									{
										String val = match.getAttrValue().encode();
										val = val.replaceAll("\n", "");
										val = val.trim();
										allBuilder.append(val + ")");
									}
								}
								getType(getName(attr.getId().toString()),
										attr.getType().toString());
								MyAttr myattr = new MyAttr(attr.getId().toString(), attr.getCategory().toString(), attr.getType().toString());
								if (isExist(collector, myattr) == false) 
									collector.add(myattr);
							}
						}
						allBuilder.insert(0, " (and");
						allBuilder.append(")");
						orBuilder.append(allBuilder);
					}
					else
					{
						boolean reachable = false;
						for(int k = 0; k < matches.size(); k++)
						{
							TargetMatch match = matches.get(k);
							if(match.getEval() instanceof AttributeDesignator)
							{
								AttributeDesignator attr = (AttributeDesignator)match.getEval();
								if(k == atindex)
								{
									allBuilder.append(" ("
											+ al.returnFunction(match.getMatchFunction().encode())
											+ " " + getName(attr.getId().toString())
											+ " ");
									if(attr.getType().toString().contains("string"))
										allBuilder.append("\"" + "r@nd0m" + "\")");
									if(attr.getType().toString().contains("integer"))
										allBuilder.append(1234567890 + ")");
								}
								else if(!reachable)
								{
									allBuilder.append(" ("
											+ al.returnFunction(match.getMatchFunction().encode())
											+ " " + getName(attr.getId().toString())
											+ " ");
									if(attr.getType().toString().contains("string"))
									{
										String val = match.getAttrValue().encode();
										val = val.replaceAll("\n", "");
										val = val.trim();
										allBuilder.append("\"" + val + "\")");
									}
									if(attr.getType().toString().contains("integer"))
									{
										String val = match.getAttrValue().encode();
										val = val.replaceAll("\n", "");
										val = val.trim();
										allBuilder.append(val + ")");
									}
									reachable = !reachable;
								}
								else
									continue;
								getType(getName(attr.getId().toString()),
										attr.getType().toString());
								MyAttr myattr = new MyAttr(attr.getId().toString(), attr.getCategory().toString(), attr.getType().toString());
								if (isExist(collector, myattr) == false) 
									collector.add(myattr);
							}
						}
						allBuilder.insert(0, " (and");
						allBuilder.append(")");
						orBuilder.append(allBuilder);
					}
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
	
	//grab specified AnyOf selection and negate the values it contains
	public String getTargetAnyOf_NegateValues(Target target, int anindex, ArrayList<MyAttr> collector)
	{
		StringBuffer sb = new StringBuffer();
		if(target != null)
		{
			List<AnyOfSelection> anyOf = target.getAnyOfSelections();
			for(int i = 0; i < anyOf.size(); i++)
			{
				StringBuffer orBuilder = new StringBuffer();
				AnyOfSelection any = anyOf.get(i);
				List<AllOfSelection> allOf = any.getAllOfSelections();
				if(i == anindex)
				{
					for(AllOfSelection all : allOf)
					{
						StringBuffer allBuilder = new StringBuffer();
						List<TargetMatch> matches = all.getMatches();
						for(TargetMatch match : matches)
						{
							if(match.getEval() instanceof AttributeDesignator)
							{
								AttributeDesignator attr = (AttributeDesignator)match.getEval();
								if(i != anindex)
								{
									allBuilder.append(" ("
											+ al.returnFunction(match.getMatchFunction().encode())
											+ " " + getName(attr.getId().toString())
											+ " ");
									if(attr.getType().toString().contains("string"))
									{
										String val = match.getAttrValue().encode();
										val = val.replaceAll("\n", "");
										val = val.trim();
										allBuilder.append("\"" + "r@nd0m" + "\")");
									}
									if(attr.getType().toString().contains("integer"))
									{
										String val = match.getAttrValue().encode();
										val = val.replaceAll("\n", "");
										val = val.trim();
										allBuilder.append(123456789 + ")");
									}
								}
								getType(getName(attr.getId().toString()),
										attr.getType().toString());
								MyAttr myattr = new MyAttr(attr.getId()
										.toString(), attr.getCategory()
										.toString(), attr.getType().toString());
								if (isExist(collector, myattr) == false) 
									collector.add(myattr);
							}
						}
						allBuilder.insert(0, " (and");
						allBuilder.append(")");
						orBuilder.append(allBuilder);
					}
				}
				else
				{
				for(AllOfSelection all : allOf)
				{
					StringBuffer allBuilder = new StringBuffer();
					List<TargetMatch> matches = all.getMatches();
					for(TargetMatch match : matches)
					{
						if(match.getEval() instanceof AttributeDesignator)
						{
							AttributeDesignator attr = (AttributeDesignator)match.getEval();
							if(i != anindex)
							{
								allBuilder.append(" ("
										+ al.returnFunction(match.getMatchFunction().encode())
										+ " " + getName(attr.getId().toString())
										+ " ");
								if(attr.getType().toString().contains("string"))
								{
									String val = match.getAttrValue().encode();
									val = val.replaceAll("\n", "");
									val = val.trim();
									allBuilder.append("\"" + val + "\")");
								}
								if(attr.getType().toString().contains("integer"))
								{
									String val = match.getAttrValue().encode();
									val = val.replaceAll("\n", "");
									val = val.trim();
									allBuilder.append(val + ")");
								}
							}
							getType(getName(attr.getId().toString()),
									attr.getType().toString());
							MyAttr myattr = new MyAttr(attr.getId()
									.toString(), attr.getCategory()
									.toString(), attr.getType().toString());
							if (isExist(collector, myattr) == false) 
								collector.add(myattr);
						}
					}
					allBuilder.insert(0, " (and");
					allBuilder.append(")");
					orBuilder.append(allBuilder);
				}
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
	
	//grab specified AllOf and negate the values it contains
	public String getTargetAllOf_NegateValues(Target target, int anindex, int alindex, ArrayList<MyAttr> collector)
	{
		StringBuffer sb = new StringBuffer();
		if(target != null)
		{
			List<AnyOfSelection> anyOf = target.getAnyOfSelections();
			for(int j = 0; j < anyOf.size(); j++)
			{
				if(j != anindex)
					continue;
				AnyOfSelection any = anyOf.get(j);
				StringBuffer orBuilder = new StringBuffer();
				List<AllOfSelection> allOf = any.getAllOfSelections();
				for(int i = 0; i < allOf.size(); i++)
				{
					AllOfSelection all = allOf.get(i);
					StringBuffer allBuilder = new StringBuffer();
					List<TargetMatch> matches = all.getMatches();
					if(i != alindex)
						continue;
					for(TargetMatch match : matches)
					{
						if(match.getEval() instanceof AttributeDesignator)
						{
							AttributeDesignator attr = (AttributeDesignator)match.getEval();
							if(i == alindex)
							{
								allBuilder.append(" ("
										+ al.returnFunction(match.getMatchFunction().encode())
										+ " " + getName(attr.getId().toString())
										+ " ");
								if(attr.getType().toString().contains("string"))
								{
									String val = match.getAttrValue().encode();
									val = val.replaceAll("\n", "");
									val = val.trim();
									allBuilder.append("\"" + val + "\")");
								}
								if(attr.getType().toString().contains("integer"))
								{
									String val = match.getAttrValue().encode();
									val = val.replaceAll("\n", "");
									val = val.trim();
									allBuilder.append(val + ")");
								}
							}
							getType(getName(attr.getId().toString()),
									attr.getType().toString());
							MyAttr myattr = new MyAttr(attr.getId()
									.toString(), attr.getCategory()
									.toString(), attr.getType().toString());
							if (isExist(collector, myattr) == false) 
								collector.add(myattr);
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
	
	public String getTargetAllOf(Target target, int alindex)
	{
		StringBuffer sb = new StringBuffer();
		StringBuffer allBuilder = new StringBuffer();
		int i = 0;
		if(target != null)
		{
			List<AnyOfSelection> anyOf = target.getAnyOfSelections();
			for(AnyOfSelection any : anyOf)
			{
				StringBuffer orBuilder = new StringBuffer();
				List<AllOfSelection> allOf = any.getAllOfSelections();
				for(i = 0; i < allOf.size(); i++)
				{
					if(i == alindex)
					{
						List<TargetMatch> matches = allOf.get(i).getMatches();
						for(TargetMatch match : matches)
						{
							if(match.getEval() instanceof AttributeDesignator)
							{
								AttributeDesignator attr = (AttributeDesignator)match.getEval();
								allBuilder.append( " ("
										+ al.returnFunction(match.getMatchFunction().encode())
										+ " " + getName(attr.getId().toString())
										+ " ");
								if(attr.getType().toString().contains("string"))
								{
									String val = match.getAttrValue().encode();
									val = val.replaceAll("\n", "");
									val = val.trim();
									allBuilder.append("\"" + val + "\")");
								}
								if(attr.getType().toString().contains("integer"))
								{
									String val = match.getAttrValue().encode();
									val = val.replaceAll("\n", "");
									val = val.trim();
									allBuilder.append(val + ")");
								}
							}
						}
						allBuilder.insert(0, " (and");
						allBuilder.append(")");
						sb.append(allBuilder);
					}
				}
			}
			return sb.toString();
		}
		return "";
	}
	
	public String getTargetAnyOf(Target target, int anyindex)
	{
		StringBuffer sb = new StringBuffer();
		if(target != null)
		{
			List<AnyOfSelection> anyOf = target.getAnyOfSelections();
			for(int i = 0; i < anyOf.size(); i++)
			{
				if(i == anyindex)
				{
					List<AllOfSelection> allOf = anyOf.get(i).getAllOfSelections();
					for(AllOfSelection all : allOf)
					{
						List<TargetMatch> matches = all.getMatches();
						for(TargetMatch match : matches)
						{
							if(match.getEval() instanceof AttributeDesignator)
							{
								AttributeDesignator attr = (AttributeDesignator)match.getEval();
								sb.append(" ("
										+ al.returnFunction(match.getMatchFunction().encode())
										+ " " + getName(attr.getId().toString())
										+ " ");
								if(attr.getType().toString().contains("string"))
								{
									String val = match.getAttrValue().encode();
									val = val.replaceAll("\n", "");
									val = val.trim();
									sb.append("\"" + val + "\")");
								}
								if(attr.getType().toString().contains("integer"))
								{
									String val = match.getAttrValue().encode();
									val = val.replaceAll("\n", "");
									val = val.trim();
									sb.append(val + ")");
								}
							}
						}
					}
				}
			}
			return sb.toString();
		}
		return "";
	}

	public StringBuffer ApplyStatements(Apply apply, String function,
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

	public String getAttrValue(Apply apply) {
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

	public StringBuffer buildAttrDesignator(StringBuffer sb, Apply apply,
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

	public String getConditionAttribute(Condition condition,
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

	public boolean isExist(ArrayList<MyAttr> generation, MyAttr childAttr) {
		if (generation == null)
			return false;
		for (MyAttr it : generation) {
			if (it.getName().equals(childAttr.getName())) {
				return true;
			}
		}
		return false;
	}

	public void mergeAttribute(ArrayList<MyAttr> Globalattributes,
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

	public void printAttribute(ArrayList<MyAttr> globalattributes) {
		for (MyAttr myattr : globalattributes) {
			System.out.println(myattr.toString());
		}
	}

	public String Deny_Permit_Override() throws IOException {
		ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
		TruePolicyTarget(policy, collector);
		function f = new function();
		if (f.allDenyRule(policy) || f.allPermitRule(policy)) {
			return "";
		} else {
			List<Rule> permitRule = getPermitRuleFromPolicy(policy);
			List<Rule> denyRule = getDenyRuleFromPolicy(policy);
			for (Rule pRule : permitRule) {
				for (Rule dRule : denyRule) {
					StringBuffer sb = new StringBuffer();
					ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
					// condition 1: P & D
					sb.append(TruePolicyTarget(policy, localcollector) + "\n");
					sb.append(TrueTarget_TrueCondition(pRule, localcollector)
							+ "\n");
					sb.append(TrueTarget_TrueCondition(dRule, localcollector)
							+ "\n");
					boolean sat = z3str(sb.toString(), nameMap, typeMap);
					if (sat == true) {
						z3.getValue(localcollector, nameMap);
						String request = f.print(localcollector);
//						//System.out.println(request);
//						System.out.println(PolicyEvaluate(policy, request));
						return request;
					}
				}
			}

			// Condition 2, P & ID
			for (Rule pRule : permitRule) {
				for (Rule dRule : denyRule) {
					boolean ind = checkIndeterminate(dRule, pRule);
					if (dRule.getCondition() == null) {
						continue;
					}
					if (ind == false) {
						continue;
					} else {
						StringBuffer sb = new StringBuffer();
						ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
						sb.append(TruePolicyTarget(policy, localcollector)
								+ "\n");
						sb.append(TrueTarget_TrueCondition(pRule,
								localcollector) + "\n");
						boolean sat = z3str(sb.toString(), nameMap, typeMap);
						if (sat == true) {
							// for indeterminate
							z3.getValue(localcollector, nameMap);
							String request = f.print(localcollector);
//							System.out.println(PolicyEvaluate(policy, request));
							return request;
						}
					}
				}
			}

			// Condition 3, IP & D
			for (Rule pRule : permitRule) {
				for (Rule dRule : denyRule) {
					boolean ind = checkIndeterminate(pRule, dRule);
					if (pRule.getCondition() == null) {
						continue;
					}
					if (ind == false) {
						continue;
					} else {
						StringBuffer sb = new StringBuffer();
						ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
						sb.append(TruePolicyTarget(policy, localcollector)
								+ "\n");
						sb.append(TrueTarget_TrueCondition(dRule,
								localcollector) + "\n");
						boolean sat = z3str(sb.toString(), nameMap, typeMap);
						System.out.println(sat);
						if (sat == true) {
							// localcollector.add(ind);
							z3.getValue(localcollector, nameMap);
							String request = f.print(localcollector);
//							System.out.println(PolicyEvaluate(policy, request));
							return request;
						}
					}
				}
			}
		}
		return "";
	}

	public String DenyOverride_DenyUnlessPermit() throws IOException {
		function f = new function();
		if (f.checkDefaultRule(policy) && f.allDenyRule(policy)) {
			return "";
		}
		if (f.checkDefaultRule(policy) && f.allPermitRule(policy)) {
			return "";
		}

		// Condition1,first permit rule
		if (f.checkDefaultRule(policy)
				&& f.DefaultEffect(policy).equals("deny")) {
			List<Rule> permitRule = getPermitRuleFromPolicy(policy);
			for (Rule pRule : permitRule) {
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
				sb.append(TruePolicyTarget(policy, localcollector) + "\n");
				sb.append(TrueTarget_TrueCondition(pRule, localcollector)
						+ "\n");
				boolean sat = z3str(sb.toString(), nameMap, typeMap);
				if (sat == true) {
					z3.getValue(localcollector, nameMap);
					String request = f.print(localcollector);
					//System.out.println(PolicyEvaluate(policy, request));
					return request;
				}
			}
		}
		// Condition2, first deny rule
		if (f.checkDefaultRule(policy)
				&& f.DefaultEffect(policy).equals("permit")) {
			List<Rule> denyRule = getDenyRuleFromPolicy(policy);
			for (Rule dRule : denyRule) {
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
				sb.append(TruePolicyTarget(policy, localcollector) + "\n");
				sb.append(TrueTarget_TrueCondition(dRule, localcollector)
						+ "\n");
				boolean sat = z3str(sb.toString(), nameMap, typeMap);
				if (sat == true) {
					z3.getValue(localcollector, nameMap);
					String request = f.print(localcollector);
					//System.out.println(PolicyEvaluate(policy, request));
					return request;
				}
			}
		}
		// otherwise, all indeterminate
		// TODO how to make sure can make indeterminate
		boolean ind = allIndeterminate(policy);
		if (ind) {
			StringBuffer sb = new StringBuffer();
			ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
			sb.append(TruePolicyTarget(policy, localcollector) + "\n");
			boolean sat = z3str(sb.toString(), nameMap, typeMap);
			if (sat == true) {
				z3.getValue(localcollector, nameMap);
				localcollector.add(invalidAttr()); // at least one attribute
				String request = f.print(localcollector);
				//System.out.println(PolicyEvaluate(policy, request));
				return request;
			}
		}
		return "";
	}

	public String DenyOverride_PermitUnlessDeny() throws IOException {
		function f = new function();
		// theorem 7
		if (f.checkDefaultRule(policy)
				&& f.DefaultEffect(policy).equals("deny")) {
			return "";
		}
		// theorem 8
		if (f.checkDefaultRule(policy)
				&& f.DefaultEffect(policy).equals("permit")) {
			if (f.allPermitRule(policy)) {
				return "";
			} else {
				List<Rule> denyRule = getDenyRuleFromPolicy(policy);
				for (Rule dRule : denyRule) {
					ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
					StringBuffer sb = new StringBuffer();
					sb.append(TruePolicyTarget(policy, localcollector) + "\n");
					sb.append(True_Target((Target) dRule.getTarget(),
							localcollector) + "\n");
					boolean ind = oneRuleIndeterminate(dRule, policy);
					if (ind) {
						boolean sat = z3str(sb.toString(), nameMap, typeMap);
						if (sat == true) {
							// localcollector.add(ind);
							z3.getValue(localcollector, nameMap);
							String request = f.print(localcollector);
							//System.out.println(PolicyEvaluate(policy, request));
							return request;
						}
					}
				}
			}
		}
		// otherwise, all indeterminate
		// TODO how to make sure can make indeterminate
		boolean ind = allIndeterminate(policy);
		if (ind) {
			StringBuffer sb = new StringBuffer();
			ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
			sb.append(TruePolicyTarget(policy, localcollector) + "\n");
			boolean sat = z3str(sb.toString(), nameMap, typeMap);
			if (sat == true) {
				z3.getValue(localcollector, nameMap);
				localcollector.add(invalidAttr());
				String request = f.print(localcollector);
				//System.out.println(PolicyEvaluate(policy, request));
				return request;
			}
		}
		return "";
	}

	public String DenyOverride_FirstApplicable() throws IOException {
		// TODO should have "or" relation here; tagged as "***"
		function f = new function();
		// theorem 9, all permit, i = ip, j = p
		if (f.allPermitRule(policy)) {
			System.out.println("All permit");
			List<Rule> permitRule = getPermitRuleFromPolicy(policy);
			for (int i = 0; i < permitRule.size(); i++) {
				for (int j = permitRule.size() - 1; j > i; j--) {
					boolean ind = checkIndeterminate(permitRule.get(i),
							permitRule.get(j));
					if (ind == false) {
						continue;
					} else {
						ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
						StringBuffer sb = new StringBuffer();
						sb.append(TruePolicyTarget(policy, localcollector)
								+ "\n");
						sb.append(TrueTarget_TrueCondition(permitRule.get(j),
								localcollector) + "\n");
						// TODO can not evaluate?
						// sb.append(True_Target(permitRule.get(i),
						// localcollector) + "\n");
						boolean sat = z3str(sb.toString(), nameMap, typeMap);
						if (sat == true) {
							// localcollector.add(ind);
							z3.getValue(localcollector, nameMap);
							String request = f.print(localcollector);
							//System.out.println(request);
							return request;
						}
					}
				}
			}
		} else {
			// theorem 10; at least have one deny rule;
			// condition a;
			List<Rule> Rule = getRuleFromPolicy(policy);
			for (int i = 0; i < Rule.size(); i++) {
				// if first rule is deny rule;
				if (Rule.get(i).getEffect() != 0) {
					for (int j = Rule.size() - 1; j > i; j--) {
						// no matter j is P or D rule, try id,p/d first
						ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
						StringBuffer sb = new StringBuffer();
						// if second rule is deny, ID&D

						boolean ind = checkIndeterminate(Rule.get(i),
								Rule.get(j));
						if (ind == false) {
							continue;
						}
						sb.append(TruePolicyTarget(policy, localcollector)
								+ "\n");
						sb.append(TrueTarget_TrueCondition(Rule.get(j),
								localcollector) + "\n");
						for (int k = 0; k < i; k++) {
							// TODO should not be "and" here
							sb.append(False_Target((Target) Rule.get(k)
									.getTarget(), localcollector)
									+ "\n");
							sb.append(False_Condition(Rule.get(k)
									.getCondition(), localcollector)
									+ "\n");
						}
						boolean sat = z3str(sb.toString(), nameMap, typeMap);
						if (sat == true) {
							if (f.isDefaultRule(Rule.get(j)))
								localcollector.add(invalidAttr());
							z3.getValue(localcollector, nameMap);
							String request = f.print(localcollector);
							//System.out.println(PolicyEvaluate(policy, request));
							return request;
						}
					}
					for (int j = Rule.size() - 1; j > i; j--) {
						// if failed, and j is P rule, try, id, ip

						if (Rule.get(j).getCondition() == null) {
							continue;
						}

						if (Rule.get(j).getEffect() == 0) {
							ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
							StringBuffer sb = new StringBuffer();
							boolean ind2 = oneRuleIndeterminate(Rule.get(j),
									policy);
							boolean ind = oneRuleIndeterminate(Rule.get(i),
									policy);
							if (ind2 == false || ind == false) {
								continue;
							}
							sb = new StringBuffer();
							localcollector = new ArrayList<MyAttr>();
							sb.append(TruePolicyTarget(policy, localcollector)
									+ "\n");
							for (int k = 0; k < i; k++) {
								// TODO should not be "and" here
								sb.append(False_Target((Target) Rule.get(k)
										.getTarget(), localcollector)
										+ "\n");
								sb.append(False_Condition(Rule.get(k)
										.getCondition(), localcollector)
										+ "\n");
							}
							boolean sat = z3str(sb.toString(), nameMap, typeMap);
							if (sat == true) {
								localcollector.add(invalidAttr()); // in case it
																	// is empty
								z3.getValue(localcollector, nameMap);
								String request = f.print(localcollector);
//								System.out.println(PolicyEvaluate(policy,
//										request));
								return request;
							}

						}

					}

				} else {
					// first permit
					for (int j = Rule.size() - 1; j > i; j--) { // j for deny //
																// rule
						if (Rule.get(j).getEffect() != 1) {
							continue;
						} else {
							// condition 1, P&D
							ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
							StringBuffer sb = new StringBuffer();
							sb.append(TruePolicyTarget(policy, localcollector)
									+ "\n");
							sb.append(TrueTarget_TrueCondition(Rule.get(j),
									localcollector) + "\n");
							sb.append(TrueTarget_TrueCondition(Rule.get(i),
									localcollector) + "\n");
							for (int k = 0; k < i; k++) {
								sb.append(False_Target((Target) Rule.get(k)
										.getTarget(), localcollector)
										+ "\n");
								sb.append(False_Condition(Rule.get(k)
										.getCondition(), localcollector)
										+ "\n");
							}
							boolean sat = z3str(sb.toString(), nameMap, typeMap);
							if (sat == true) {
								z3.getValue(localcollector, nameMap);
								String request = f.print(localcollector);
								return request;
								// TODO here, need a test?
							} else {
								// condition 2 P&ID

								localcollector = new ArrayList<MyAttr>();
								sb = new StringBuffer();

								boolean ind = checkIndeterminate(Rule.get(j),
										Rule.get(i));
								if (ind == false) {
									continue;
								}
								sb.append(TruePolicyTarget(policy,
										localcollector) + "\n");
								sb.append(TrueTarget_TrueCondition(Rule.get(i),
										localcollector) + "\n");
								sat = z3str(sb.toString(), nameMap, typeMap);
								if (sat == true) {
									String request = f.print(localcollector);
									return request;
								}
							}
						}
					}
					// first IP;
					if (Rule.get(i).getEffect() == 0) {
						for (int j = Rule.size() - 1; j > i; j--) {
							if (Rule.get(j).getEffect() == 0) {
								continue;
							}

							boolean ind = oneRuleIndeterminate(Rule.get(i),
									policy);
							boolean ind2 = oneRuleIndeterminate(Rule.get(j),
									policy);
							if (ind == false || ind2 == false) {
								continue;
							}
							// IP & ID
							ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
							StringBuffer sb = new StringBuffer();
							sb.append(TruePolicyTarget(policy, localcollector)
									+ "\n");
							boolean sat = z3str(sb.toString(), nameMap, typeMap);
							if (sat == true) {
								localcollector.add(invalidAttr()); // in case is
																	// empty
								z3.getValue(localcollector, nameMap);
								String request = f.print(localcollector);
								return request;
							} else {
								// IP & D;
								boolean ind3 = checkIndeterminate(Rule.get(i),
										Rule.get(j));
								if (ind3 == false) {
									continue;
								}
								localcollector = new ArrayList<MyAttr>();
								sb = new StringBuffer();
								sb.append(TruePolicyTarget(policy,
										localcollector) + "\n");
								sb.append(TrueTarget_TrueCondition(Rule.get(j),
										localcollector) + "\n");
								sat = z3str(sb.toString(), nameMap, typeMap);
								if (sat == true) {
									if (f.isDefaultRule(Rule.get(j)))
										localcollector.add(invalidAttr());
									z3.getValue(localcollector, nameMap);
									String request = f.print(localcollector);
									
									return request;
								}
							}
						}
					}
				}
			}

		}
		return "";
	}

	public String PermitOverride_DenyUnlessPermit() throws IOException {
		function f = new function();
		// theorem 7
		if (f.checkDefaultRule(policy)
				&& f.DefaultEffect(policy).equals("permit")) {
			return "";
		}
		// theorem 8
		if (f.checkDefaultRule(policy)
				&& f.DefaultEffect(policy).equals("deny")) {
			if (f.allDenyRule(policy)) {
				return "";
			} else {
				List<Rule> permitRule = getPermitRuleFromPolicy(policy);
				for (Rule pRule : permitRule) {
					ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
					StringBuffer sb = new StringBuffer();
					sb.append(TruePolicyTarget(policy, localcollector) + "\n");
					// sb.append(True_Target(dRule, localcollector) + "\n");
					boolean ind = oneRuleIndeterminate(pRule, policy);
					if (ind) {
						boolean sat = z3str(sb.toString(), nameMap, typeMap);
						if (sat == true) {
							// localcollector.add(ind);
							z3.getValue(localcollector, nameMap);
							if (localcollector.size() == 0) {
								localcollector.add(invalidAttr());
							}
							String request = f.print(localcollector);
							return request;
						}
					}
				}
			}
		}
		// otherwise, all indeterminate
		// TODO how to make sure can make indeterminate
		boolean ind = allIndeterminate(policy);
		if (ind) {
			StringBuffer sb = new StringBuffer();
			ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
			sb.append(TruePolicyTarget(policy, localcollector) + "\n");
			boolean sat = z3str(sb.toString(), nameMap, typeMap);
			if (sat == true) {
				z3.getValue(localcollector, nameMap);
				localcollector.add(invalidAttr());
				String request = f.print(localcollector);
//				System.out.println();
				return request;
			}
		}
		return "";
	}

	public String PermitOverride_PermitUnlessDeny() throws IOException {
		function f = new function();
		if (f.checkDefaultRule(policy) && f.allDenyRule(policy)) {
			return "";
		}
		if (f.checkDefaultRule(policy) && f.allPermitRule(policy)) {
			return "";
		}

		// Condition1,first permit rule
		if (f.checkDefaultRule(policy)
				&& f.DefaultEffect(policy).equals("deny")) {
			List<Rule> permitRule = getPermitRuleFromPolicy(policy);
			for (Rule pRule : permitRule) {
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
				sb.append(TruePolicyTarget(policy, localcollector) + "\n");
				sb.append(TrueTarget_TrueCondition(pRule, localcollector)
						+ "\n");
				boolean sat = z3str(sb.toString(), nameMap, typeMap);
				if (sat == true) {
					z3.getValue(localcollector, nameMap);
					String request = f.print(localcollector);
					return request;
				}
			}
		}
		// Condition2, first deny rule
		if (f.checkDefaultRule(policy)
				&& f.DefaultEffect(policy).equals("permit")) {
			List<Rule> denyRule = getDenyRuleFromPolicy(policy);
			for (Rule dRule : denyRule) {
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
				sb.append(TruePolicyTarget(policy, localcollector) + "\n");
				sb.append(TrueTarget_TrueCondition(dRule, localcollector)
						+ "\n");
				boolean sat = z3str(sb.toString(), nameMap, typeMap);
				if (sat == true) {
					z3.getValue(localcollector, nameMap);
					String request = f.print(localcollector);
					return request;
				}
			}
		}
		// otherwise, all indeterminate
		// TODO how to make sure can make indeterminate
		boolean ind = allIndeterminate(policy);
		if (ind) {
			StringBuffer sb = new StringBuffer();
			ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
			sb.append(TruePolicyTarget(policy, localcollector) + "\n");
			boolean sat = z3str(sb.toString(), nameMap, typeMap);
			if (sat == true) {
				z3.getValue(localcollector, nameMap);
				localcollector.add(invalidAttr());
				String request = f.print(localcollector);
				return request;
			}
		}
		return "";
	}

	public String PermitOverride_FirstApplicable() throws IOException {
		function f = new function();
		// theorem 9, all deny, i = id, j = d
		if (f.allDenyRule(policy)) {
			List<Rule> denyRule = getDenyRuleFromPolicy(policy);
			for (int i = 0; i < denyRule.size(); i++) {

				for (int j = denyRule.size() - 1; j > 0; j--) {
					boolean ind = checkIndeterminate(denyRule.get(i),
							denyRule.get(j));
					if (ind == false) {
						continue;
					} else {
						ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
						StringBuffer sb = new StringBuffer();
						sb.append(TruePolicyTarget(policy, localcollector)
								+ "\n");
						sb.append(TrueTarget_TrueCondition(denyRule.get(j),
								localcollector) + "\n");
						boolean sat = z3str(sb.toString(), nameMap, typeMap);
						if (sat == true) {
							// localcollector.add(ind);
							z3.getValue(localcollector, nameMap);
							String request = f.print(localcollector);
							return request;
						}
					}
				}
			}
		} else {
			// theorem 10; at least have one permit rule;
			// condition a;
			List<Rule> Rule = getRuleFromPolicy(policy);
			for (int i = 0; i < Rule.size(); i++) {
				// if first rule is permit rule;
				if (Rule.get(i).getEffect() == 0) {
					if (f.isDefaultRule(Rule.get(i))) {
						continue;
					}
					for (int j = Rule.size() - 1; j > i; j--) {

						// no matter j is P or D rule, try ip,p/d first
						ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
						StringBuffer sb = new StringBuffer();
						// if second rule is deny, ID&D
						boolean ind = checkIndeterminate(Rule.get(i),
								Rule.get(j));
						if (ind == false) {
							continue;
						}
						sb.append(TruePolicyTarget(policy, localcollector)
								+ "\n");
						sb.append(TrueTarget_TrueCondition(Rule.get(j),
								localcollector) + "\n");
						for (int k = 0; k < i; k++) {
							// TODO should not be "and" here
							sb.append(False_Target((Target) Rule.get(k)
									.getTarget(), localcollector)
									+ "\n");
							sb.append(False_Condition(Rule.get(k)
									.getCondition(), localcollector)
									+ "\n");
						}
						boolean sat = z3str(sb.toString(), nameMap, typeMap);
						if (sat == true) {
							// localcollector.add(ind);
							if (f.isDefaultRule(Rule.get(j))) {
								localcollector.add(invalidAttr());
							}
							z3.getValue(localcollector, nameMap);
							String request = f.print(localcollector);
							return request;
						}
					}
					for (int j = Rule.size() - 1; j > i; j--) {
						// if failed, and j is Deny rule, try, ip, id
						if (Rule.get(j).getEffect() == 1) {
							if (f.isDefaultRule(Rule.get(j))) {
								continue;
							}
							ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
							StringBuffer sb = new StringBuffer();
							boolean ind2 = oneRuleIndeterminate(Rule.get(j),
									policy);
							boolean ind = oneRuleIndeterminate(Rule.get(i),
									policy);
							// boolean same = sameAttributes(Rule.get(i),
							// Rule.get(j));
							if (ind2 == false || ind == false) {
								continue;
							}

							sb = new StringBuffer();
							localcollector = new ArrayList<MyAttr>();
							sb.append(TruePolicyTarget(policy, localcollector)
									+ "\n");
							for (int k = 0; k < i; k++) {
								// TODO should not be "and" here
								sb.append(False_Target((Target) Rule.get(k)
										.getTarget(), localcollector)
										+ "\n");
								sb.append(False_Condition(Rule.get(k)
										.getCondition(), localcollector)
										+ "\n");
							}
							boolean sat = z3str(sb.toString(), nameMap, typeMap);
							if (sat == true) {
								localcollector.add(invalidAttr());
								z3.getValue(localcollector, nameMap);
								localcollector.add(invalidAttr());
								String request = f.print(localcollector);
								return request;
							}

						}

					}

				} else {
					// first deny
					for (int j = Rule.size() - 1; j > i; j--) { // j for deny //
																// rule
						if (Rule.get(j).getEffect() == 1) {
							continue;
						} else {
							// condition 1, D&P
							ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
							StringBuffer sb = new StringBuffer();
							sb.append(TruePolicyTarget(policy, localcollector)
									+ "\n");
							sb.append(TrueTarget_TrueCondition(Rule.get(j),
									localcollector) + "\n");
							sb.append(TrueTarget_TrueCondition(Rule.get(i),
									localcollector) + "\n");
							for (int k = 0; k < i; k++) {
								sb.append(False_Target((Target) Rule.get(k)
										.getTarget(), localcollector)
										+ "\n");
								sb.append(False_Condition(Rule.get(k)
										.getCondition(), localcollector)
										+ "\n");
							}
							boolean sat = z3str(sb.toString(), nameMap, typeMap);
							if (sat == true) {
								z3.getValue(localcollector, nameMap);
								String request = f.print(localcollector);
								return request;
								// TODO here, need a test?
							} else {
								// condition 2 D&IP
								if (f.isDefaultRule(Rule.get(j))) {
									continue;
								}
								localcollector = new ArrayList<MyAttr>();
								sb = new StringBuffer();

								boolean ind = checkIndeterminate(Rule.get(j),
										Rule.get(i));
								if (ind == false) {
									continue;
								}
								sb.append(TruePolicyTarget(policy,
										localcollector) + "\n");
								sb.append(TrueTarget_TrueCondition(Rule.get(i),
										localcollector) + "\n");
								sat = z3str(sb.toString(), nameMap, typeMap);
								if (sat == true) {
									if (f.isDefaultRule(Rule.get(i)))
										localcollector.add(invalidAttr());
									z3.getValue(localcollector, nameMap);
									String request = f.print(localcollector);
									return request;
								}
							}
						}
					}
				}
				// first ID;
				if (Rule.get(i).getEffect() == 1) {
					for (int j = Rule.size() - 1; j > i; j--) {
						if (Rule.get(j).getEffect() == 1) {
							continue;
						}

						boolean ind = oneRuleIndeterminate(Rule.get(i), policy);
						boolean ind2 = oneRuleIndeterminate(Rule.get(j), policy);
						if (ind == false || ind2 == false) {
							continue;
						}
						// ID & IP
						if (f.isDefaultRule(Rule.get(j))) {
							continue;
						} else {
							ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
							StringBuffer sb = new StringBuffer();
							sb.append(TruePolicyTarget(policy, localcollector)
									+ "\n");
							boolean sat = z3str(sb.toString(), nameMap, typeMap);
							if (sat == true) {
								System.out
										.println("here ---------------------");
								localcollector.add(invalidAttr());
								z3.getValue(localcollector, nameMap);
								String request = f.print(localcollector);
								return request;
							} else {
								// ID & P;

								boolean ind3 = checkIndeterminate(Rule.get(i),
										Rule.get(j));
								if (ind3 == false) {
									continue;
								}
								localcollector = new ArrayList<MyAttr>();
								sb = new StringBuffer();
								sb.append(TruePolicyTarget(policy,
										localcollector) + "\n");
								sb.append(TrueTarget_TrueCondition(Rule.get(j),
										localcollector) + "\n");
								sat = z3str(sb.toString(), nameMap, typeMap);
								if (sat == true) {
									if (f.isDefaultRule(Rule.get(j)))
										localcollector.add(invalidAttr());
									z3.getValue(localcollector, nameMap);
									String request = f.print(localcollector);
									return request;
								}
							}
						}

					}
				}
			}
		}

		return "";
	}

	public String DenyUnlessPermit_PermitUnlessDeny() throws IOException {

		function f = new function();
		if (f.checkDefaultRule(policy)
				&& f.DefaultEffect(policy).equals("permit")) {
			if (f.allPermitRule(policy)) {
				return "";
			} else {
				List<Rule> denyRule = getDenyRuleFromPolicy(policy);
				for (Rule rule : denyRule) {
					if (f.isDefaultRule(rule))
						continue;
					ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
					StringBuffer sb = new StringBuffer();
					sb.append(TruePolicyTarget(policy, localcollector) + "\n");
					sb.append(TrueTarget_TrueCondition(rule, localcollector)
							+ "\n");
					boolean sat = z3str(sb.toString(), nameMap, typeMap);
					if (sat) {
						z3.getValue(localcollector, nameMap);
						String request = f.print(localcollector);
						return request;
					}
				}
			}
		} else if (f.checkDefaultRule(policy)
				&& f.DefaultEffect(policy).equals("deny")) {
			if (f.allDenyRule(policy)) {
				return "";
			} else {
				List<Rule> permitRule = getPermitRuleFromPolicy(policy);
				for (Rule rule : permitRule) {
					if (f.isDefaultRule(rule))
						continue;
					ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
					StringBuffer sb = new StringBuffer();
					sb.append(TruePolicyTarget(policy, localcollector) + "\n");
					sb.append(TrueTarget_TrueCondition(rule, localcollector)
							+ "\n");
					boolean sat = z3str(sb.toString(), nameMap, typeMap);
					if (sat) {
						z3.getValue(localcollector, nameMap);
						String request = f.print(localcollector);
						return request;
					}
				}
			}
		} else {
			ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
			StringBuffer sb = new StringBuffer();
			sb.append(TruePolicyTarget(policy, localcollector) + "\n");
			boolean sat = z3str(sb.toString(), nameMap, typeMap);
			if (sat) {
				if (localcollector.isEmpty())
					localcollector.add(invalidAttr());
				z3.getValue(localcollector, nameMap);
				String request = f.print(localcollector);
				return request;
			}
		}
		return "";
	}

	public String DenyUnlessPermit_FirstApplicable() throws IOException {
		function f = new function();
		List<Rule> rules = getRuleFromPolicy(policy);
		for (Rule rule : rules) {
			boolean ind = oneRuleIndeterminate(rule, policy);
			if (ind == false) {
				// System.out.println("condition one rule ind " + ind);
				continue;
			}
			StringBuffer sb = new StringBuffer();
			ArrayList<MyAttr> localcollector = new ArrayList<MyAttr>();
			sb.append(TruePolicyTarget(policy, localcollector) + "\n");
			boolean sat = z3str(sb.toString(), nameMap, typeMap);
			// System.out.println(sat + "condition sat");
			if (sat) {
				if (localcollector.size() == 0) {
					localcollector.add(invalidAttr());
				}
				z3.getValue(localcollector, nameMap);
				String request = f.print(localcollector);
				return request;
			}
		}
		return "";
	}

	public String PermitUnlessDeny_FirstApplicable() throws IOException {
		return DenyUnlessPermit_FirstApplicable();
	}

	public void initBalana(XPA xpa) {

		try {
			// using file based policy repository. so set the policy location as
			// system property
			String policyLocation = (new File(".")).getCanonicalPath()
					+ File.separator + "resources";
			System.setProperty(xpa.getWorkingPolicyFilePath(), policyLocation);
		} catch (IOException e) {
			System.err.println("Can not locate policy repository");
		}
		// create default instance of Balana
		balana = Balana.getInstance();
	}

	private XACML3EvaluationCtx getEvaluationCtx(String request) {
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

	public int TargetEvaluate(Target target, String request) {
		// 0 = match, 1 = no match, 2 = ind
		MatchResult match = null;

		XACML3EvaluationCtx ec;
		ec = getEvaluationCtx(request);
		
		match = target.match(ec);
		//System.err.println("Target match result: " + match.getResult());
		return match.getResult();

	}

	public int ConditionEvaluate(Condition condition, String request) {
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

	public ArrayList<Integer> MatchOfTarget(Target target, String request) {
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

	public int RuleEvaluate(Rule rule, String request) {
		XACML3EvaluationCtx ec;
		ec = getEvaluationCtx(request);
		rule.evaluate(ec);
		return rule.evaluate(ec).getDecision();
	}

	public int PolicyEvaluate(Policy policy, String request) {
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
				ar.getDocumentRoot()), ReadPolicy.getPDPconfig());
		// System.out.print(request);
		return policy.evaluate(ec).getDecision();
	}

	public List<Rule> getRuleFromPolicy(Policy policy) {
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

	public List<Rule> getPermitRuleFromPolicy(Policy policy) {
		List<CombinerElement> childElements = policy.getChildElements();
		List<Rule> permitElements = new ArrayList<Rule>();
		for (CombinerElement element : childElements) {
			PolicyTreeElement tree1 = element.getElement();
			Rule rule = null;
			if (tree1 instanceof Rule) {
				rule = (Rule) tree1;
				if (rule.getEffect() == 0)
					permitElements.add(rule);
			}
		}
		return permitElements;
	}

	public List<Rule> getDenyRuleFromPolicy(Policy policy) {
		List<CombinerElement> childElements = policy.getChildElements();
		List<Rule> permitElements = new ArrayList<Rule>();
		for (CombinerElement element : childElements) {
			PolicyTreeElement tree1 = element.getElement();
			Rule rule = null;
			if (tree1 instanceof Rule) {
				rule = (Rule) tree1;
				if (rule.getEffect() == 1)
					permitElements.add(rule);
			}
		}
		return permitElements;
	}

	public StringBuffer TruePolicyTarget(Policy policy,
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

	public StringBuffer TrueTarget_TrueCondition(Rule rule,
			ArrayList<MyAttr> collector) {
		StringBuffer sb = new StringBuffer();
		sb.append(True_Target((Target) rule.getTarget(), collector));
		sb.append(True_Condition(rule.getCondition(), collector));
		return sb;
	}

	public StringBuffer TrueTarget_FalseCondition(Rule rule,
			ArrayList<MyAttr> collector) {
		StringBuffer sb = new StringBuffer();
		sb.append(True_Target((Target) rule.getTarget(), collector));
		sb.append(False_Condition(rule.getCondition(), collector));
		return sb;
	}

	public StringBuffer False_Target(Target target, ArrayList<MyAttr> collector) {
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
	
	public StringBuffer Negate_OneTargetAttribute(Target target, ArrayList<MyAttr> local, int attribute)
	{
		String[] lines = getTargetAttribute(target, local).split("\n");
		StringBuffer output = new StringBuffer();
		if(attribute < 0 || attribute >= lines.length)
			return output;
		else
		{
			String attr = lines[attribute];
			StringBuffer sb = new StringBuffer();
			sb.append("(not ");
			sb.append(attr);
			sb.append(")");
			for(String s : lines)
			{
				if(s.compareTo(attr) != 0)
					output.append(s);
				else
					output.append(sb);
			}
			return output;
		}
	}

	public StringBuffer FalseTarget_FalseCondition(Rule rule,
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

	public StringBuffer True_Target(Target target, ArrayList<MyAttr> collector) {
		StringBuffer sb = new StringBuffer();
		sb.append(getTargetAttribute(target, collector));
		sb.append("\n");
		return sb;
	}

	public StringBuffer Ind_Target(Rule rule, ArrayList<MyAttr> collector,
			MyAttr attr) {
		StringBuffer sb = new StringBuffer();
		Target target = (Target) rule.getTarget();
		sb.append(getTargetAttribute(target, collector, attr));
		sb.append("\n");
		return sb;
	}

	public StringBuffer False_Condition(Condition condition,
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

	public StringBuffer True_Condition(Condition condition,
			ArrayList<MyAttr> collector) {
		StringBuffer sb = new StringBuffer();
		sb.append(getConditionAttribute(condition, collector));
		sb.append("\n");
		return sb;
	}

	private boolean checkIndeterminate(Rule rule1, Rule rule2) {

		ArrayList<MyAttr> base = new ArrayList<MyAttr>();
		ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
		ArrayList<MyAttr> target = new ArrayList<MyAttr>();
		TrueTarget_TrueCondition(rule1, collector);
		TrueTarget_TrueCondition(rule2, base);
		TruePolicyTarget(policy, target);
		for (MyAttr c : collector) {
			for (MyAttr b : base) {
				if (c.getName().toString().equals(b.getName().toString())) {
					return false;
				} else {
					for (MyAttr t : target) {
						if (c.getName().toString()
								.equals(t.getName().toString())) {
							return false;
						}
					}
				}
			}
		}

		return true;
	}

	public boolean oneRuleIndeterminate(Rule rule, Policy policy) {
		// TODO
		// problem here, what if there is only one condition attr in this rule?
		// Indeterminate -> NA
		// if (rule.getCondition() == null) {
		// return false;
		// }

		ArrayList<MyAttr> temp = new ArrayList<MyAttr>();
		ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
		TrueTarget_TrueCondition(rule, temp);
		TruePolicyTarget(policy, collector);
		if (collector.size() == 0) {
			return true;
		}
		for (MyAttr c : collector) {
			for (MyAttr t : temp) {
				if (c.getName().toString().equals(t.getName().toString())) {
					return false;
				}
			}

		}
		return true;
	}

	private MyAttr invalidAttr() {
		MyAttr myattr = new MyAttr(randomAttribute(), randomAttribute(),
				"http://www.w3.org/2001/XMLSchema#string");
		myattr.addValue("Indeterminate");
		return myattr;
	}

	private String randomAttribute() {
		String base = "abcdefghijklmnopqrstuvwxyz0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < 10; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}

	public boolean z3str(String input, HashMap nameMap, HashMap typeMap) {
		//System.err.println("Building z3 input");
		z3.buildZ3Input(input, nameMap, typeMap);
		z3.buildZ3Output();
		if (z3.checkConflict() == true) {
			return true;
		} else {
			return false;
		}
	}

	public boolean allIndeterminate(Policy policy) {
		// return one unique attribute that not existing in any rules
		List<CombinerElement> childElements = policy.getChildElements();
		List<Rule> ruleElements = new ArrayList<Rule>();
		ArrayList<MyAttr> attributes = new ArrayList<MyAttr>();
		ArrayList<MyAttr> temps = new ArrayList<MyAttr>(); // store existing
															// attributes

		for (CombinerElement element : childElements) {
			PolicyTreeElement tree = element.getElement();
			Rule rule = null;
			if (tree instanceof Rule) {
				rule = (Rule) tree;
				ruleElements.add(rule);
			}
		}
		ArrayList<MyAttr> target = new ArrayList<MyAttr>();
		TruePolicyTarget(policy, target);
		for (Rule rule : ruleElements) {
			ArrayList<MyAttr> child = new ArrayList<MyAttr>();
			TrueTarget_TrueCondition(rule, child);

			outer: for (MyAttr c : temps) {
				for (MyAttr t : target) {
					if (!c.getName().toString().equals(t.getName().toString())) {
						break outer;
					}
					return false;
				}
			}
		}
		return true;
	}

	private void removeFromArray(ArrayList<MyAttr> collector, MyAttr myattr) {
		for (MyAttr coll : collector) {
			if (myattr.getName().equals(coll.getName().toString())) {
				collector.remove(coll);
			}
		}
	}

	public class ConRecord {
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

	public class TarRecord {
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

	public class RuleRecord {
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
	
	public class MatchRecord
	{
		
	}
	
	public class AnyOfRecord
	{
		
	}
	
	public class AllOfRecord
	{
		
	}

	public class PolicyTable {
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
	
	private class MatchTable
	{
		private ArrayList<MatchRecord> expressions;
	}
	
	private class AnyOfTable
	{
		
	}
	
	private class AllOfTable
	{
		
	}

	public PolicyTable buildDecisionCoverage(Policy policy) {
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
	
	public PolicyTable buildDecisionCoverage_NoId(Policy policy){
		PolicyTable policytable = new PolicyTable();
		List<Rule> rules = getRuleFromPolicy(policy);
		Target target = (Target) policy.getTarget(); // get policy target
		if (target != null) {
			TarRecord record ;
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
				TarRecord tar ;
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
				ConRecord con ;

				con = new ConRecord(1, 0);
				rulerecord.addCondition(con);

				con = new ConRecord(0, 0);
				rulerecord.addCondition(con);
			}
			policytable.addRule(rulerecord);
		}
		return policytable;
	}

	public ArrayList<PolicySpreadSheetTestRecord> generate_DecisionCoverage(
			TestPanel testPanel, PolicyTable policytable, String fileName) {
		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
		function f = new function();
		List<Rule> rules = getRuleFromPolicy(policy);
		ArrayList<TarRecord> trecord = policytable.getTarget();

		File file = new File(
				testPanel.getTestOutputDestination(fileName));
		if (!file.isDirectory() && !file.exists()) {
			file.mkdir();
		} else {
			f.deleteFile(file);
		}

		long startTime = System.currentTimeMillis();

		int count = 1;
		for (TarRecord ptarget : trecord) {
			if (ptarget.getEffect() == 2 && ptarget.getCovered() == 0) {
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				sb.append(TruePolicyTarget(policy, collector) + "\n");
				if (collector.size() > 0){
					collector.remove(0);
				collector.add(invalidAttr());
				}
				// in case is empty
				else{
					continue;
				}
				boolean sat = z3str(sb.toString(), nameMap, typeMap);
				if (sat) {
					try {
						z3.getValue(collector, nameMap);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ptarget.setCovered(1);
					String request = f.print(collector);
					//System.out.println(request);
					try {
						String path = testPanel
								.getTestOutputDestination(fileName)
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
							PolicySpreadSheetTestSuite.TEST_KEYWORD + " "
									+ count, "request" + count + ".txt",
							request, "");
					generator.add(psstr);
					ptarget.setCovered(1);
					count++;

				}
			} else if (ptarget.getEffect() == 1 && ptarget.getCovered() == 0) {
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				sb.append(False_Target((Target) policy.getTarget(), collector));
				boolean sat = z3str(sb.toString(), nameMap, typeMap);
				if (sat) {
					try {
						z3.getValue(collector, nameMap);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					ptarget.setCovered(1);
					System.out.println("I am here");
					String request = f.print(collector);
					//System.out.println(request);
					try {
						String path = testPanel
								.getTestOutputDestination(fileName)
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
							PolicySpreadSheetTestSuite.TEST_KEYWORD + " "
									+ count, "request" + count + ".txt",
							request, "");
					generator.add(psstr);
					ptarget.setCovered(1);
					count++;

				}

			} else if (ptarget.getEffect() == 0) {
				// if policy target is true
				for (int i = rules.size() - 1; i >= 0; i--) {
					if (isDefaultRule(rules.get(i))) {
						boolean success = generateDefaultRule(generator,
								testPanel, i, rules, count, fileName);
						if(success)
							count++;
						continue;
					}

					ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
					StringBuffer prefix = new StringBuffer();
					for (int k = 0; k < i; k++) {
						prefix.append(FalseTarget_FalseCondition(rules.get(k),
								collector) + "\n");
					}
					RuleRecord ruleRecord = policytable.getRules().get(i);
					for (TarRecord rtarget : ruleRecord.getTarget()) {
						if (rtarget.getEffect() == 2
								&& rtarget.getCovered() == 0) { // rule target
																// is ind
							boolean getOne = generateIndTarget(generator,
									testPanel, policytable, rules.get(i),
									collector, count, i, prefix,
									fileName, rtarget, true);
							if (getOne)
								count++;
						} else if (rtarget.getEffect() == 1
								&& rtarget.getCovered() == 0) {
							StringBuffer sb = new StringBuffer();
							// ArrayList<MyAttr> collector = new
							// ArrayList<MyAttr>();
							System.out.println("false target for rule: " + i);
							sb.append(TruePolicyTarget(policy, collector)
									+ "\n");
							sb.append(False_Target((Target) rules.get(i)
									.getTarget(), collector)
									+ "\n");
							sb.append(prefix);
							boolean sat = z3str(sb.toString(), nameMap, typeMap);
							System.out.println(sat);
							if (sat) {
								try {
									z3.getValue(collector, nameMap);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								rtarget.setCovered(1);
								String request = f.print(collector);
								updateDecisionTable(policy, policytable,
										request, i);
								//System.out.println(request);
								try {
									String path = testPanel
											.getTestOutputDestination(fileName)
											+ File.separator
											+ "request"
											+ count + ".txt";
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
										PolicySpreadSheetTestSuite.TEST_KEYWORD
												+ " " + count, "request"
												+ count + ".txt", request, "");
								generator.add(psstr);
								count++;
							}

						} else if (rtarget.getEffect() == 0) {
							// if rule target is true
							if (ruleRecord.getCondition().size() == 0) {
								StringBuffer sb = new StringBuffer();
								sb.append(True_Target((Target) rules.get(i)
										.getTarget(), collector)
										+ "\n");
								sb.append(TruePolicyTarget(policy, collector)
										+ "\n");
								sb.append(prefix);
								boolean sat = z3str(sb.toString(), nameMap,
										typeMap);
								if (sat) {
									try {
										z3.getValue(collector, nameMap);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									rtarget.setCovered(1);
									String request = f.print(collector);
									//System.out.println(request);
									System.out.println(i);
									updateDecisionTable(policy, policytable,
											request, i);
									try {
										String path = testPanel
												.getTestOutputDestination(fileName)
												+ File.separator
												+ "request"
												+ count + ".txt";
										FileWriter fw = new FileWriter(path);
										BufferedWriter bw = new BufferedWriter(
												fw);
										bw.write(request);
										bw.close();

									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									// generate target object
									PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
											PolicySpreadSheetTestSuite.TEST_KEYWORD
													+ " " + count, "request"
													+ count + ".txt", request,
											"");
									generator.add(psstr);
									count++;
								}

							} else {
								for (ConRecord rcondition : ruleRecord
										.getCondition()) {
									if (rcondition.getEffect() == 2
											&& rcondition.getCovered() == 0) {
										// TODO
										boolean getOne = generateIndCondition(
												generator, testPanel,
												policytable, rules.get(i),
												collector, count, i, prefix,
												fileName,
												rcondition, true);
										if (getOne)
											count++;
									} else if (rcondition.getEffect() == 1
											&& rcondition.getCovered() == 0) {
										StringBuffer sb = new StringBuffer();
										// ArrayList<MyAttr> collector = new
										// ArrayList<MyAttr>();
										sb.append(TruePolicyTarget(policy,
												collector) + "\n");
										sb.append(True_Target((Target) rules
												.get(i).getTarget(), collector)
												+ "\n");
										sb.append(False_Condition(rules.get(i)
												.getCondition(), collector)
												+ "\n");
										sb.append(prefix + "\n");

										boolean sat = z3str(sb.toString(),
												nameMap, typeMap);
										if (sat) {
											try {
												z3.getValue(collector, nameMap);
											} catch (IOException e) {
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											}
											rcondition.setCovered(1);
											String request = f.print(collector);
											updateDecisionTable(policy,
													policytable, request, i);
											try {
												String path = testPanel
														.getTestOutputDestination(fileName)
														+ File.separator
														+ "request"
														+ count
														+ ".txt";
												FileWriter fw = new FileWriter(
														path);
												BufferedWriter bw = new BufferedWriter(
														fw);
												bw.write(request);
												bw.close();

											} catch (IOException e) {
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											}
											// generate target object
											PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
													PolicySpreadSheetTestSuite.TEST_KEYWORD
															+ " " + count,
													"request" + count + ".txt",
													request, "");
											generator.add(psstr);
											count++;
										}

									} else if (rcondition.getEffect() == 0
											&& rcondition.getCovered() == 0) {
										StringBuffer sb = new StringBuffer();
										// ArrayList<MyAttr> collector = new
										// ArrayList<MyAttr>();
										sb.append(TruePolicyTarget(policy,
												collector) + "\n");
										sb.append(True_Target((Target) rules
												.get(i).getTarget(), collector)
												+ "\n");
										sb.append(True_Condition(rules.get(i)
												.getCondition(), collector)
												+ "\n");
										sb.append(prefix + "\n");
										boolean sat = z3str(sb.toString(),
												nameMap, typeMap);
										if (sat) {
											try {
												z3.getValue(collector, nameMap);
											} catch (IOException e) {
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											}
											rcondition.setCovered(1);
											String request = f.print(collector);
											// updateDecisionTable(policy,
											// policytable, request, i);
											try {
												String path = testPanel
														.getTestOutputDestination(fileName)
														+ File.separator
														+ "request"
														+ count
														+ ".txt";
												FileWriter fw = new FileWriter(
														path);
												BufferedWriter bw = new BufferedWriter(
														fw);
												bw.write(request);
												bw.close();

											} catch (IOException e) {
												// TODO Auto-generated catch
												// block
												e.printStackTrace();
											}
											// generate target object
											PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
													PolicySpreadSheetTestSuite.TEST_KEYWORD
															+ " " + count,
													"request" + count + ".txt",
													request, "");
											generator.add(psstr);
											count++;
										}
									}
								}
								rtarget.setCovered(1);
							}
						}
					}
				}
				ptarget.setCovered(1);
			}
		}
		long endTime = System.currentTimeMillis();
		System.out.println("Test generation time： " + (endTime - startTime) + "ms");

		for (TarRecord ttrecord : policytable.getTarget()) {
			if (ttrecord.covered == 0) {
				System.out.println("PolicyTarget not covered: "
						+ ttrecord.getEffect());
			}
		}
		int ii = 1;
		for (RuleRecord rrrecord : policytable.getRules()) {
			for (TarRecord ttrecord : rrrecord.getTarget()) {
				if (ttrecord.covered == 0) {
					System.out.println("RuleTarget not covered: " + ii
							+ " effect: " + ttrecord.getEffect());
				}
			}
			for (ConRecord ccrecord : rrrecord.getCondition()) {
				if (ccrecord.covered == 0) {
					System.out.println("RuleCondition not covered: " + ii
							+ " effect: " + ccrecord.getEffect());
				}
			}
			ii++;
		}
		return generator;
	}

	private StringBuffer makeAvailability(List<Rule> rules, int position,
			StringBuffer sb, ArrayList<MyAttr> collector) {
		for (int i = 0; i < position; i++) {
			Rule rule = rules.get(i);
			if (rule.getEffect() == rules.get(position).getEffect()) {
				if (rule.getTarget() != null) {
					sb.append(False_Target((Target) rule.getTarget(), collector));
					sb.append("\n");
					continue;
				} else if (rule.getCondition() != null) {
					sb.append(False_Condition(rule.getCondition(), collector));
					sb.append("\n");
					continue;
				}
			}
		}
		return sb;
	}

	public void updateDecisionTable(Policy policy, PolicyTable policytable,
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

	private boolean arrayMatch(ArrayList<Integer> arry1,
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

	private void printarray(ArrayList<Integer> arr) {
		for (int i = 0; i < arr.size(); i++) {
			System.out.print(arr.get(i));
		}
		System.out.println();
	}

	private void updateMCDCTable(Policy policy, PolicyTable policytable,
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
	
	private void updateFaultTable_RPTE(List<Rule> rules, String request, int start)
	{
		
		Rule starting = rules.get(start);
		ArrayList<Integer> t1 = new ArrayList<Integer>();
		int c = -1;
		if(starting.getTarget() != null)
			t1 = MatchOfTarget((Target)starting.getTarget(), request);
		if(starting.getCondition() != null)
			c = ConditionEvaluate(starting.getCondition(), request);
		if((t1 != null && t1.size() != 0) || c >= 0)
			rule_table[start][FALSE_TRUE] = true;
		
		for(int i = start + 1; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			ArrayList<Integer> t2 = new ArrayList<Integer>();
			int c2 = -1;
			if(r.getTarget() != null)
				t2 = MatchOfTarget((Target)starting.getTarget(), request);
			if(r.getCondition() != null)
				c2 = ConditionEvaluate(r.getCondition(), request);
			if(arrayMatch(t1, t2) && c2 == c)
				rule_table[i][FALSE_TRUE] = true;
		}
	}
	
	private void updateFaultTable(List<Rule> rules, boolean[][] fault_table, String request, int start)
	{
		//fault_table[X][0] -> target coverage
		//fault_table[X][1] -> condition coverage
		Rule starting = rules.get(start);
		ArrayList<Integer> t1 = new ArrayList<Integer>();
		int t2 = -1;
		int c = -1;
		if(starting.getTarget() != null)
		{
			t1 = MatchOfTarget((Target)starting.getTarget(), request);
			t2 = TargetEvaluate((Target)starting.getTarget(), request);
		}
		if(starting.getCondition() != null)
			c = ConditionEvaluate(starting.getCondition(), request);
		for(int i = start + 1; i < rules.size(); i++)
		{
			Rule rule = rules.get(i);
			if(isDefaultRule(rule))
				continue;
			ArrayList<Integer> tresult1 = new ArrayList<Integer>();
			int tresult2 = -1;
			int cresult = -1;
			if(rule.getTarget() != null)
			{
				tresult1 = MatchOfTarget((Target)rule.getTarget(), request);
				tresult2 = TargetEvaluate((Target)rule.getTarget(), request);
			}
			if(rule.getCondition() != null)
				cresult = ConditionEvaluate(rule.getCondition(), request);
			if(rule.getTarget() == null)
			{
				if(cresult == c && !fault_table[i][1])
					fault_table[i][1] = true;
			}
			else
			{
				if(fault_table[i][0])
				{
					if(cresult == c && !fault_table[i][1])
						fault_table[i][1] = true;
					else
						continue;
				}
				if((tresult2 == t2 || arrayMatch(tresult1, t1)) && !fault_table[i][0])
					fault_table[i][0] = true;
				if(cresult == c && !fault_table[i][1])
					fault_table[i][1] = true;
			}
		}
	}

	public boolean isDefaultRule(Rule rule) {
		if (rule.getCondition() == null && rule.getTarget() == null) {
			return true;
		} else {
			return false;
		}
	}

	public PolicyTable buildMCDC_Table(Policy policy, MCDC_converter2 converter, boolean isUnique) {
		PolicyTable policytable = new PolicyTable();
		List<Rule> rules = getRuleFromPolicy(policy);
		// Target target = (Target) policy.getTarget();

		if ((Target) policy.getTarget() != null) {
			policytable.addTarget(new TarRecord(2, 0)); // add ind manually

			ArrayList<String> order = new ArrayList<String>();
			StringBuffer sb = new StringBuffer();
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			sb.append(TruePolicyTarget(policy, collector));
			String mcdc_input = converter.convert(sb.toString());

			String[] s = mcdc_input.split(" ");
			for (int p = 0; p < s.length; p++) {
				if (!s[p].endsWith("(") && !s[p].endsWith(")")
						&& !s[p].endsWith("&&"))
					order.add(s[p]);
			}

			if (!mcdc_input.trim().equals("")) {
				MCDCConditionSet mcdcset = new MCDCConditionSet(mcdc_input, isUnique);
				ArrayList<String> positives = mcdcset.getPositiveConditions();
				ArrayList<String> negatives = mcdcset.getNegativeConditions();
				for (String result : negatives) {
					System.out.println("result :" + result);
					policytable.addTarget(new TarRecord(ConvertToArray(result,
							order), getOrder(order), 1, 0));
				}
				for (String result : positives) {
					System.out.println("result :" + result);
					policytable.addTarget(new TarRecord(ConvertToArray(result,
							order), getOrder(order), 0, 0));
				}
			}
			policytable.addTarget(new TarRecord(0, 0));
			// add an dumb target to policy target
		}

		for (Rule rule : rules) {
			if (isDefaultRule(rule)) {
				continue;
			}
			RuleRecord ruleRecord = new RuleRecord();
			// first target
			if (rule.getTarget() != null) {
				ruleRecord.addTarget(new TarRecord(2, 0)); // add ind

				ArrayList<String> order = new ArrayList<String>();

				Target rTarget = (Target) rule.getTarget();
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				sb.append(True_Target(rTarget, collector));
				String mcdc_input = converter.convert(sb.toString());
				System.out.println("mcdc input here: " + mcdc_input);
				String[] s = mcdc_input.split(" ");
				for (int p = 0; p < s.length; p++) {
					if (!s[p].endsWith("(") && !s[p].endsWith(")")
							&& !s[p].endsWith("&&"))
						order.add(s[p]);
				}
				System.out.println(printArray(order, 1));
				MCDCConditionSet mcdcset = new MCDCConditionSet(mcdc_input, isUnique);
				ArrayList<String> positives = mcdcset.getPositiveConditions();
				ArrayList<String> negatives = mcdcset.getNegativeConditions();
				for (String result : negatives) {
					System.out.println("result :" + result);

					ruleRecord.addTarget(new TarRecord(ConvertToArray(result,
							order), getOrder(order), 1, 0));
				}
				for (String result : positives) {
					System.out.println("result :" + result);
					ruleRecord.addTarget(new TarRecord(ConvertToArray(result,
							order), getOrder(order), 0, 0));
				}

			} else {
				ruleRecord.addTarget(new TarRecord(0, 0));
				// need to go on
			}

			if (rule.getCondition() != null) {
				ConRecord con = new ConRecord(2, 0);
				ruleRecord.addCondition(con);

				con = new ConRecord(1, 0);
				ruleRecord.addCondition(con);

				con = new ConRecord(0, 0);
				ruleRecord.addCondition(con);
			} else {
				ConRecord con = new ConRecord(0, 0);
				ruleRecord.addCondition(con);
			}

			policytable.addRule(ruleRecord);
		}

		return policytable;
	}
	
	
	public PolicyTable buildMCDC_Table_NoId(Policy policy, MCDC_converter2 converter, boolean isUnique) {
		PolicyTable policytable = new PolicyTable();
		List<Rule> rules = getRuleFromPolicy(policy);
		// Target target = (Target) policy.getTarget();

		if ((Target) policy.getTarget() != null) {
			//policytable.addTarget(new TarRecord(2, 0)); // add ind manually

			ArrayList<String> order = new ArrayList<String>();
			StringBuffer sb = new StringBuffer();
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			sb.append(TruePolicyTarget(policy, collector));
			String mcdc_input = converter.convert(sb.toString());

			String[] s = mcdc_input.split(" ");
			for (int p = 0; p < s.length; p++) {
				if (!s[p].endsWith("(") && !s[p].endsWith(")")
						&& !s[p].endsWith("&&"))
					order.add(s[p]);
			}

			if (!mcdc_input.trim().equals("")) {
				MCDCConditionSet mcdcset = new MCDCConditionSet(mcdc_input, isUnique);
				ArrayList<String> positives = mcdcset.getPositiveConditions();
				ArrayList<String> negatives = mcdcset.getNegativeConditions();
				for (String result : negatives) {
					System.out.println("result :" + result);
					policytable.addTarget(new TarRecord(ConvertToArray(result,
							order), getOrder(order), 1, 0));
				}
				for (String result : positives) {
					System.out.println("result :" + result);
					policytable.addTarget(new TarRecord(ConvertToArray(result,
							order), getOrder(order), 0, 0));
				}
			}
			policytable.addTarget(new TarRecord(0, 0));
			// add an dumb target to policy target
		}

		for (Rule rule : rules) {
			if (isDefaultRule(rule)) {
				continue;
			}
			RuleRecord ruleRecord = new RuleRecord();
			// first target
			if (rule.getTarget() != null) {
				//ruleRecord.addTarget(new TarRecord(2, 0)); // add ind

				ArrayList<String> order = new ArrayList<String>();

				Target rTarget = (Target) rule.getTarget();
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				sb.append(True_Target(rTarget, collector));
				String mcdc_input = converter.convert(sb.toString());
				System.out.println("mcdc input here: " + mcdc_input);
				String[] s = mcdc_input.split(" ");
				for (int p = 0; p < s.length; p++) {
					if (!s[p].endsWith("(") && !s[p].endsWith(")")
							&& !s[p].endsWith("&&"))
						order.add(s[p]);
				}
				System.out.println(printArray(order, 1));
				MCDCConditionSet mcdcset = new MCDCConditionSet(mcdc_input, false);
				ArrayList<String> positives = mcdcset.getPositiveConditions();
				ArrayList<String> negatives = mcdcset.getNegativeConditions();
				for (String result : negatives) {
					System.out.println("result :" + result);

					ruleRecord.addTarget(new TarRecord(ConvertToArray(result,
							order), getOrder(order), 1, 0));
				}
				for (String result : positives) {
					System.out.println("result :" + result);
					ruleRecord.addTarget(new TarRecord(ConvertToArray(result,
							order), getOrder(order), 0, 0));
				}

			} else {
				ruleRecord.addTarget(new TarRecord(0, 0));
				// need to go on
			}

			if (rule.getCondition() != null) {
				ConRecord con ;
				//ruleRecord.addCondition(con);

				con = new ConRecord(1, 0);
				ruleRecord.addCondition(con);

				con = new ConRecord(0, 0);
				ruleRecord.addCondition(con);
			} else {
				ConRecord con = new ConRecord(0, 0);
				ruleRecord.addCondition(con);
			}

			policytable.addRule(ruleRecord);
		}

		return policytable;
	}
	

	public ArrayList<PolicySpreadSheetTestRecord> generate_MCDCCoverage(
			TestPanel testPanel, PolicyTable policytable, String foldName, MCDC_converter2 converter) {
		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
		int count = 1;
		//MCDC_converter2 converter = new MCDC_converter2();
		//PolicyTable policytable = buildMCDC_Table(policy, converter);
		// System.out.println(policytable.getRules().size() + "size here");
		function f = new function();

		List<Rule> rules = getRuleFromPolicy(policy);
		ArrayList<TarRecord> trecord = policytable.getTarget();

		File file = new File(
				testPanel.getTestOutputDestination(foldName));
		if (!file.isDirectory() && !file.exists()) {
			file.mkdir();
		} else {
			f.deleteFile(file);
		}
		int ruleNo = 1;
		long startTime = System.currentTimeMillis();
		for (TarRecord ptarget : trecord) {
			if (ptarget.getEffect() == 2 && ptarget.covered == 0) {
				// policy target ind
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				StringBuffer sb = new StringBuffer();
				sb.append(TruePolicyTarget(policy, collector));
				if (collector.size() > 0) {
					collector.remove(0);
					collector.add(invalidAttr());
				} else {
					continue;
				}
				boolean sat = z3str(sb.toString(), nameMap, typeMap);
				if (sat) {
					try {
						z3.getValue(collector, nameMap);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					String request = f.print(collector);
					try {
						String path = testPanel
								.getTestOutputDestination(foldName)
								+ File.separator + "request" + count + ".txt";
						FileUtils.forceMkdir(new File(FilenameUtils
								.getFullPath(path)));
						FileWriter fw = new FileWriter(path);
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(request);
						bw.close();

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
							PolicySpreadSheetTestSuite.TEST_KEYWORD + " "
									+ count, "request" + count + ".txt",
							request, "");
					generator.add(psstr);
					count++;
					ptarget.setCovered(1);
				}
			} else if (ptarget.getEffect() == 1 && ptarget.covered == 0) {
				// false policy target
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				StringBuffer z3_input = new StringBuffer();
				TruePolicyTarget(policy, collector);
				System.out.println(ptarget.getArray().size() + "size");
				z3_input.append(ptarget.buildZ3(converter) + "\n");
				System.out.println("request : " + z3_input.toString());

				boolean sat = z3str(z3_input.toString(), nameMap, typeMap);
				if (sat) {
					try {
						z3.getValue(collector, nameMap);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					String request = f.print(collector);
					try {
						String path = testPanel
								.getTestOutputDestination(foldName)
								+ File.separator + "request" + count + ".txt";
						FileWriter fw = new FileWriter(path);
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(request);
						bw.close();

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
							PolicySpreadSheetTestSuite.TEST_KEYWORD + " "
									+ count, "request" + count + ".txt",
							request, "");
					generator.add(psstr);
					count++;
					ptarget.setCovered(1);
				}
			} else if (ptarget.getEffect() == 0 && ptarget.covered == 0) {
				// is permit policy target
				for (int i = rules.size() - 1; i >= 0; i--) {
					if (isDefaultRule(rules.get(i))) {
						boolean success = generateDefaultRule(generator,
								testPanel, i, rules, count, foldName);
						if(success)
							count++;
						continue;
					}
					
					ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
					StringBuffer prefix = new StringBuffer();
					for (int k = 0; k < i; k++) {
						prefix.append(FalseTarget_FalseCondition(rules.get(k),
								collector) + "\n");
					}
					RuleRecord ruleRecord = policytable.getRules().get(i);
					for (TarRecord rtarget : ruleRecord.getTarget()) {
						if (rtarget.getEffect() == 2 && rtarget.covered == 0) { // ind
							boolean getOne = generateIndTarget(generator,
									testPanel, policytable, rules.get(i),
									collector, count, i, prefix,
									foldName, rtarget, false);
							if (getOne)
								count++;
						}

						if (rtarget.getEffect() == 1 && rtarget.covered == 0) {
							TruePolicyTarget(policy, collector);
							True_Target((Target) rules.get(i).getTarget(),
									collector);
							// printTrack2("should wrong once");
							StringBuffer z3_input = new StringBuffer();
							z3_input.append(ptarget.buildZ3(converter) + "\n");
							z3_input.append(rtarget.buildZ3(converter) + "\n");
							// z3_input.append(prefix + "\n");
							z3_input.append(TruePolicyTarget(policy, collector) + "\n");
							z3_input.append(prefix);
							boolean sat = z3str(z3_input.toString(), nameMap,
									typeMap);
							if (sat) {
								try {
									z3.getValue(collector, nameMap);
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

								String request = f.print(collector);
								updateMCDCTable(policy, policytable, request, i);
								try {
									String path = testPanel
											.getTestOutputDestination(foldName)
											+ File.separator
											+ "request"
											+ count + ".txt";
									FileWriter fw = new FileWriter(path);
									BufferedWriter bw = new BufferedWriter(fw);
									bw.write(request);
									bw.close();

								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
										PolicySpreadSheetTestSuite.TEST_KEYWORD
												+ " " + count, "request"
												+ count + ".txt", request, "");
								generator.add(psstr);
								count++;
								rtarget.setCovered(1);
							}

						} else if (rtarget.getEffect() == 0
								&& rtarget.covered == 0) {
							// rule target permit

							for (ConRecord rcondition : ruleRecord
									.getCondition()) {
								if (rcondition.getEffect() == 2
										&& rcondition.getCovered() == 0) {
									boolean getOne = generateIndCondition(
											generator, testPanel, policytable,
											rules.get(i), collector, count, i,
											prefix, foldName,
											rcondition, false);
									if (getOne)
										count++;
								} else if (rcondition.getEffect() == 1
										&& rcondition.getCovered() == 0) {
									// ArrayList<MyAttr> collector = new
									// ArrayList<MyAttr>();

									True_Target((Target) rules.get(i)
											.getTarget(), collector);

									StringBuffer z3_input = new StringBuffer();
									z3_input.append(ptarget.buildZ3(converter)
											+ "\n");
									z3_input.append(rtarget.buildZ3(converter)
											+ "\n");

									z3_input.append(False_Condition(rules
											.get(i).getCondition(), collector)
											+ "\n");
									// z3_input.append(prefix + "\n");
									z3_input.append(TruePolicyTarget(policy,
											collector) + "\n");
									z3_input.append(prefix);
									boolean sat = z3str(z3_input.toString(),
											nameMap, typeMap);
									if (sat) {
										try {
											z3.getValue(collector, nameMap);
										} catch (IOException e) {
											// TODO Auto-generated catch
											// block
											e.printStackTrace();
										}

										String request = f.print(collector);
										if (!policy
												.getCombiningAlg()
												.getIdentifier()
												.equals("urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable"))
											updateMCDCTable(policy,
													policytable, request, i);
										try {
											String path = testPanel
													.getTestOutputDestination(foldName)
													+ File.separator
													+ "request"
													+ count
													+ ".txt";
											FileWriter fw = new FileWriter(path);
											BufferedWriter bw = new BufferedWriter(
													fw);
											bw.write(request);
											bw.close();

										} catch (IOException e) {
											// TODO Auto-generated catch
											// block
											e.printStackTrace();
										}
										PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
												PolicySpreadSheetTestSuite.TEST_KEYWORD
														+ " " + count,
												"request" + count + ".txt",
												request, "");
										generator.add(psstr);
										count++;
										// rcondition.setCovered(1);
									}
								} else if (rcondition.getEffect() == 0
										&& rcondition.getCovered() == 0) {
									// ArrayList<MyAttr> collector = new
									// ArrayList<MyAttr>();
									TruePolicyTarget(policy, collector);
									True_Target((Target) rules.get(i)
											.getTarget(), collector);

									StringBuffer z3_input = new StringBuffer();
									z3_input.append(ptarget.buildZ3(converter)
											+ "\n");
									z3_input.append(rtarget.buildZ3(converter)
											+ "\n");
									// z3_input.append(prefix + "\n");
									z3_input.append(True_Condition(rules.get(i)
											.getCondition(), collector));
									z3_input.append(TruePolicyTarget(policy,
											collector) + "\n");
									z3_input.append(prefix);
									System.out.println("request : "
											+ z3_input.toString());
									boolean sat = z3str(z3_input.toString(),
											nameMap, typeMap);
									System.out.println(sat + ":   "
											+ z3_input.toString());
									if (sat) {
										try {
											z3.getValue(collector, nameMap);
										} catch (IOException e) {
											// TODO Auto-generated catch
											// block
											e.printStackTrace();
										}

										String request = f.print(collector);
										updateMCDCTable(policy, policytable,
												request, i);
										try {
											String path = testPanel
													.getTestOutputDestination(foldName)
													+ File.separator
													+ "request"
													+ count
													+ ".txt";
											FileUtils
													.forceMkdir(new File(
															FilenameUtils
																	.getFullPath(path)));
											FileWriter fw = new FileWriter(path);
											BufferedWriter bw = new BufferedWriter(
													fw);
											bw.write(request);
											bw.close();

										} catch (IOException e) {
											// TODO Auto-generated catch
											// block
											e.printStackTrace();
										}
										PolicySpreadSheetTestRecord psstr = new PolicySpreadSheetTestRecord(
												PolicySpreadSheetTestSuite.TEST_KEYWORD
														+ " " + count,
												"request" + count + ".txt",
												request, "");
										generator.add(psstr);
										count++;
										// rcondition.setCovered(1);
									}
								}
							}
							rtarget.setCovered(1);
						}
					}
					ruleNo++;
				}
				ptarget.setCovered(1);
			}

		}

		long endTime = System.currentTimeMillis();
		System.out.println("Test generation time： " + (endTime - startTime) + "ms");
		//checkMcdcTable(policytable);
		return generator;
	}

	private void checkMcdcTable(PolicyTable policytable) {
		System.out.println("Number of policy target : "
				+ policytable.getTarget().size());
		for (TarRecord trecord : policytable.getTarget()) {
			if (trecord.covered == 0 && trecord.getArray() != null) {
				System.out.println("Policy target not covered : ");
			}
		}
		int i = 1;
		for (RuleRecord rrecord : policytable.getRules()) {
			System.out.println("  " + i + " rule : ");
			for (TarRecord trecord : rrecord.getTarget()) {
				if (trecord.getArray() != null && trecord.covered == 0) {
					System.out.println("Rule Target not covered : "
							+ printArray(trecord.getArray()));
				}
				if (trecord.getArray() != null && trecord.covered == 1) {
					System.out.println("Rule Target  covered : "
							+ printArray(trecord.getArray()));
				}
			}
			for (ConRecord crecord : rrecord.getCondition()) {
				if (crecord.covered == 0) {
					System.out.println("Rule Condition not covered : "
							+ crecord.getEffect());
				}
			}
			i++;
		}
	}

	private String printArray(ArrayList<Integer> list) {
		StringBuffer sb = new StringBuffer();
		for (Integer i : list) {
			sb.append(i + ",");
		}
		sb.append("\n");
		return sb.toString();
	}

	private String printArray(ArrayList<String> list, int a) {
		StringBuffer sb = new StringBuffer();
		for (String i : list) {
			sb.append(i + ",");
		}
		sb.append("\n");
		return sb.toString();
	}

	// put the mcdc output into 0, 1 array in target

	private ArrayList<Integer> ConvertToArray(String input,
			ArrayList<String> order) {
		ArrayList<Integer> result = new ArrayList<Integer>();
		String[] token = input.split("&&");
		// System.out.println("token input: " + input);
		// System.out.println(order.get(0));
		for (String s : order) {
			System.out.println("Start " + s);
			for (int i = 0; i < token.length; i++) {
				System.out.println(s + "-->" + token[i].trim());
				if (token[i].trim().startsWith("!"))
					if (token[i].trim().substring(1).equals(s)) {
						result.add(1);
						System.out.println("add 1");
					}
				if (token[i].trim().equals(s)) {
					result.add(0);
					System.out.println("add 0 ");
				}
			}
			System.out.println("End");
		}
		System.out.println(printArray(result));
		return result;

	}

	private ArrayList<String> getTokens(String inputs) {
		ArrayList<String> result = new ArrayList<String>();
		String[] token = inputs.split("&&");
		for (String s : token) {
			// System.out.println("token : " + s);
			s = s.trim();
			if (s.startsWith("!")) {
				s = s.substring(1);
			}
			result.add(s);
		}
		return result;
	}

	// print to track.txt to track z3_str
	static void printTrack2(String request) {
		try {
			FileWriter fw = new FileWriter("./track.txt", true);
			fw.write(request);
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private ArrayList<String> getOrder(ArrayList<String> order) {
		ArrayList<String> result = new ArrayList<String>();
		for (String s : order) {
			result.add(s);
		}
		return result;
	}

	private MyAttr getDifferentAttribute(ArrayList<MyAttr> globle,
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

	private boolean generateIndTarget(
			ArrayList<PolicySpreadSheetTestRecord> generator,
			TestPanel testPanel, PolicyTable policytable, Rule targetRule,
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

	private boolean generateIndCondition(
			ArrayList<PolicySpreadSheetTestRecord> generator,
			TestPanel testPanel, PolicyTable policytable, Rule targetRule,
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

	private boolean generateDefaultRule(
			ArrayList<PolicySpreadSheetTestRecord> generator,
			TestPanel testPanel, int order, List<Rule> rules, int testNo,
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
	
	//Turner Lehmbecker WIP
	//Generate PTT request for PTT mutations
	public PolicySpreadSheetTestRecord generate_PolicyTargetTrue(TestPanel t)
	{
		PolicySpreadSheetTestRecord ptr = null;
		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
		function f = new function();
		if(!policy.isTargetEmpty())
		{
			Target policyTarget = (Target)policy.getTarget();
			List<AnyOfSelection> anyOf = policyTarget.getAnyOfSelections();
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			StringBuffer sb = new StringBuffer();
			if(anyOf.size() != 0)
			{
				sb.append(False_Target(policyTarget, collector) + "\n");
				boolean sat = z3str(sb.toString(), nameMap, typeMap);
				if(sat)
				{
					System.out.println(nameMap.size() + " map size");
					try
					{	
						z3.getValue(collector, nameMap);
						
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					System.out.println(collector.size() + " collector size");
					String request = f.print(collector);
					try
					{
						String path = t.getTestOutputDestination("_MutationTests") 
								+ File.separator + "requestPTT1.txt";
						FileWriter fw = new FileWriter(path);
						BufferedWriter bw = new BufferedWriter(fw);
						bw.write(request);
						bw.close();
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					ptr = new PolicySpreadSheetTestRecord(PolicySpreadSheetTestSuite.TEST_KEYWORD
							+ " PTT1", "requestPTT1.txt", request, "");
					generator.add(ptr);
				}
			}
		}
		return ptr;
	}
	
		//Turner Lehmbecker WIP
		//Generate PTF request for PTF mutations
		public PolicySpreadSheetTestRecord generate_PolicyTargetFalse(TestPanel t)
		{
			PolicySpreadSheetTestRecord ptr = null;
			ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
			function f = new function();
			if(!policy.isTargetEmpty() || policy.isTargetEmpty())
			{
				Target policyTarget = (Target)policy.getTarget();
				List<AnyOfSelection> anyOf = policyTarget.getAnyOfSelections();
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				StringBuffer sb = new StringBuffer();
				List<Rule> rules = getRuleFromPolicy(policy);
				Rule r = rules.get(0);
				sb.append(TruePolicyTarget(policy, collector) + "\n");
				sb.append(TrueTarget_TrueCondition(r, collector) + "\n");
				boolean sat = z3str(sb.toString(), nameMap, typeMap);
				if(sat)
				{
					System.out.println(nameMap.size() + " map size");
					try
					{
						z3.getValue(collector, nameMap);
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					System.out.println(collector.size() + " collection size");
					
					String request = f.print(collector);
					try
					{
						String path = t.getTestOutputDestination("_MutationTests")
								+ File.separator + "requestPTF1.txt";
						BufferedWriter bw = new BufferedWriter(new FileWriter(path));
						bw.write(request);
						bw.close();
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
					ptr = new PolicySpreadSheetTestRecord(
							PolicySpreadSheetTestSuite.TEST_KEYWORD + " PTF1",
							"requestPTF1.txt", request, "");
				}
			}
			return ptr;
		}
	
	//Turner Lehmbecker
	//WIP: Generate RTT requests and z3-str input
	public ArrayList<PolicySpreadSheetTestRecord> generate_RuleTargetTrue(TestPanel t, boolean opt)
	{
		//PolicySpreadSheetTestRecord ptr = null;
		//function f = new function();
		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
		CombiningAlgorithm cmbAlg = policy.getCombiningAlg();
		List<Rule> rules = getRuleFromPolicy(policy);
		function f = new function();
		int count = 1;
		//Step 0: Find default rule, if it exists
		Rule def = isDefaultRule(rules.get(rules.size() - 1)) ? rules.get(rules.size() - 1) : null;
		
		//CA Condition 1: rule combining algorithm is permit-overrides
		if(cmbAlg instanceof PermitOverridesRuleAlg)
		{
			//Condition 1: Check if default rule exists
			//Result 1: Default rule exists in policy
			if(def != null)
			{
				//Condition 1: Default rule exists and effect is "Permit"
				//Result: Test cannot be generated as policy will always return "Permit"
				
				if(def.getEffect() == 0)
				{
					//System.err.println("Test cannot be generated for given policy");
					//System.err.println("The policy contains a default \"Permit\" rule with permit-overrides"
							//+ " combining algorithm");
					return generator;
				}
				//Condition 2: Default rule exists but effect is "Deny"
				//Result: Continue test generation up to rule n - 1
				else
				{
					//Step 2 of Condition 2: Generate tests for each rule and ensure reachability to each rule
					System.out.println("Default rule exists, but tests can be generated");
					if(allDenyRules(rules, rules.size() - 1))
						buildRTTRequests_override(rules, generator, t, 1, opt);
					else
					{
						//Condition 2, Step 2: Mixture of rules for policy
						//Result: Normal test generation
						
						//Step 1 of Condition 2: Generate requests for rules 0 -> n-1
						buildRTTRequests_override(rules, generator, t, 0, opt);
						
					}
				}
			}
			//Condition 1: Check if default rule exists
			//Result 2: Default rule does not exist
			else
			{
				System.out.println("No default rule exists, generating tests as normal...");
				if(allDenyRules(rules, rules.size()))
					buildRTTRequests_override(rules, generator, t, 1, opt);
				else
					//Condition 2 of Result 2: Policy contains a mixture of rules
					//Result: Generate requests as normal
					buildRTTRequests_override(rules, generator, t, 0, opt);
			}
			return generator;
		}
		//CA condition 2: CA is deny-overrides
		else if(cmbAlg instanceof DenyOverridesRuleAlg)
		{
			//Step 1: Check if default rule exists
			if(def != null)
			{
				//Condition 1 of Step 1: default rule exists
				if(def.getEffect() == 1)
				{
					//Result 1 of Condition 1, Step 1: default rule effect is "Deny"
					//Effect: test cannot be generated
					//System.err.println("Test cannot be generated for given policy");
					//System.err.print("Policy contains a default \"Deny\" rule with deny-overrides"
							//+ " combining algorithm");
					return generator;
				}
				else
				{
					//Result 2 of Condition 1, Step 1: default rule effect is "Permit"
					//Effect: tests can be generated as normal up to rule n-1
					System.out.println("Default rule exists, generate tests up to rule n-1");
					//Step 2 of Condition 1: Generate requests up to n-1
					if(allPermitRules(rules, rules.size() -1))
						buildRTTRequests_override(rules, generator, t, 0, opt);
					else
					{
						//Condition 2, Step 2: Mixture of rules
						//Result: Generate requests as normal
						
						//Step 1, Condition 2: Generate requests for rules
						buildRTTRequests_override(rules, generator, t, 1, opt);
					}
				}
			}
			else
			{
				//Condition 2 of Step 1: no default rule exists
				System.out.println("No default rule exists, test generation will continue as normal...");
				if(f.allPermitRule(policy))
					buildRTTRequests_override(rules, generator, t, 0, opt);
				else
					//Condition 2 of Step 1, Condition 2: Policy contains a mixture of rules
					//Result: Generate requests as normal
					buildRTTRequests_override(rules, generator, t, 1, opt);
			}
		}
		else if(cmbAlg instanceof DenyUnlessPermitRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 0)
				{
					//System.err.println("Test cannot be generated for given policy");
					//System.err.println("Policy contains a default \"Permit\" rule with" +
					//" deny-unless-permit combining algorithm");
					return generator;
				}
				else
				{
					if(f.allDenyRule(policy))
					{
						//System.err.println("Test cannot be generated for given policy");
						//System.err.println("Policy contains nothing but \"Deny\" rules " +
						//with deny-unless-permit combining algorithm");
						//System.err.println("Please consider writing a better policy and using a different combining algorithm");
						return generator;
					}
					else
						buildRTTRequests_unless(rules, generator, t, 0, opt);
				}
			}
			else if(f.allDenyRule(policy))
			{
				//System.err.println("Test cannot be generated for given policy");
				//System.err.println("Policy contains nothing but \"Deny\" rules " +
				//"with deny-unless-permit combining algorithm");
				//System.err.println("Please consider writing a better policy and using a different combining algorithm");
				return generator;
			}
			else
				buildRTTRequests_unless(rules, generator, t, 0, opt);
			return generator;
		}
		else if(cmbAlg instanceof PermitUnlessDenyRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 1)
				{
					//System.err.println("Test cannot be generated for given policy");
					//System.err.println("Policy contains a default \"Deny\" rule with" +
					//" permit-unless-deny combining algorithm");
					return generator;
				}
				else
				{
					if(f.allPermitRule(policy))
					{
						//System.err.println("Test cannot be generated for given policy");
						//System.err.println("Policy contains nothing but \"Permit\" rules " +
						//"with permit-unless-deny combining algorithm");
						//System.err.println("Please consider writing a better policy and using a different combining algorithm");
						return generator;
					}
					else
					{
						buildRTTRequests_unless(rules, generator, t, 1, opt);
					}
				}
			}
			else if(f.allPermitRule(policy))
			{
				//System.err.println("Test cannot be generated for given policy");
				//System.err.println("Policy contains nothing but \"Permit\" rules " +
				//"with permit-unless-deny combining algorithm");
				//System.err.println("Please consider writing a better policy and using a different combining algorithm");
				return generator;
			}
			else
				buildRTTRequests_unless(rules, generator, t, 1, opt);
			return generator;
		}
		else if(cmbAlg instanceof FirstApplicableRuleAlg)
		{
			if(def != null)
			{
				//If default exists, find first rule with differing effect
				//Set that rule to target false, condition true
				//so that mutant returns that rule's effect, policy returns N/A
				//Set all other rules with same effect as default to false
				//Those rules with different effect, set to target true, condition false
				build_DefaultRTTRequests_FA(rules, def, generator, t, opt);
			}
			else if(f.allDenyRule(policy) || f.allPermitRule(policy))
			{
				build_allOne_RequestsFalse(rules, generator, t, "RTT", opt);
			}
			else
				buildRTTRequests_FA(rules, generator, t, opt);
			return generator;
		}
		//else
			//System.err.println("Given policy's combining algorithm not currently supported.");
		return generator;
	}
	
	public ArrayList<PolicySpreadSheetTestRecord> generate_RemoveParallelTargetElement(TestPanel t, boolean opt)
	{
		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
		CombiningAlgorithm cmbAlg = policy.getCombiningAlg();
		List<Rule> rules = getRuleFromPolicy(policy);
		Rule def = isDefaultRule(rules.get(rules.size() - 1)) ? rules.get(rules.size() - 1) : null;
		function f = new function();
		
		if(cmbAlg instanceof PermitOverridesRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 0)
				{
					//System.err.println("Test cannot be generated");
					return generator;
				}
				else
				{
					buildRPTERequests_override(rules, generator, t, opt);
				}
			}
			else
				buildRPTERequests_override(rules, generator, t, opt);
			return generator;
		}
		else if(cmbAlg instanceof DenyOverridesRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 1)
				{
					//System.err.println("Test cannot be generated");
					return generator;
				}
				else
					buildRPTERequests_override(rules, generator, t, opt);
			}
			else
				buildRPTERequests_override(rules, generator, t, opt);
		}
		else if(cmbAlg instanceof PermitUnlessDenyRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 1)
				{
					//System.err.println("Test cannot be generated");
					return generator;
				}
				else if(f.allPermitRule(policy))
				{
					//System.err.println("Test cannot be generated");
					return generator;
				}
				else
					buildRPTERequests_unless(rules, generator, t, 1, opt);
			}
			else if(f.allPermitRule(policy))
			{
				//System.err.println("Test cannot be generated");
				return generator;
			}
			else
				buildRPTERequests_unless(rules, generator, t, 1, opt);
			return generator;
		}
		else if(cmbAlg instanceof DenyUnlessPermitRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 0)
				{
					//System.err.println("Test cannot be gnerated");
					return generator;
				}
				else if(f.allDenyRule(policy))
				{
					//System.err.println("Test cannot be generated");
					return generator;
				}
				else
					buildRPTERequests_unless(rules, generator, t, 0, opt);
			}
			else if(f.allDenyRule(policy))
			{
				//System.err.println("Test cannot be generated");
				return generator;
			}
			else
				buildRPTERequests_unless(rules, generator, t, 0, opt);
			return generator;
		}
		else if(cmbAlg instanceof FirstApplicableRuleAlg)
		{
			if(def != null)
				buildDefaultRPTERequests(rules, def, generator, t, opt);
			else if(f.allPermitRule(policy) || f.allDenyRule(policy))
				buildRPTERequests_override(rules, generator, t, opt);
			else
				buildRPTERequests_FA(rules, generator, t, opt);
		}
		//else
			//System.err.println("Combining algorithm not currently supported");
		
		return generator;
	}
	
	//Turner Lehmbecker
	//WIP: Generate RTF requests and RTF z3-str input
	public ArrayList<PolicySpreadSheetTestRecord> generate_RuleTargetFalse(TestPanel t, PolicyMutator m, boolean opt)
	{
		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
		CombiningAlgorithm cmbAlg = policy.getCombiningAlg();
		List<Rule> rules = getRuleFromPolicy(policy);
		Rule def = isDefaultRule(rules.get(rules.size() -1)) ? rules.get(rules.size() -1) : null;
		function f = new function();
		if(cmbAlg instanceof PermitOverridesRuleAlg)
		{
			if(def != null)
			{
				
				System.out.println("Default rule exists, but tests can be generated");
				if(allDenyRules(rules, rules.size() - 1))
				{
					buildRTFRequests_override(rules, generator, t, 1, opt);
				}
				//else if(allPermitRules(rules, rules.size() -1))
					//build_OnlyOne_Request_true(rules, generator, t, 0, count, "RTF");
				else
				{
					buildRTFRequests_override(rules, generator, t, 0, opt);
				}
			}
			else
			{
				if(allDenyRules(rules,rules.size()))
				{
					buildRTFRequests_override(rules, generator, t, 1, opt);
				}
				//else if(f.allPermitRule(policy))
					//build_OnlyOne_Request_false(rules, generator, t, 0, count, "RTF");
				else
					buildRTFRequests_override(rules, generator, t, 0, opt);
			}
			return generator;
		}
		else if(cmbAlg instanceof DenyOverridesRuleAlg)
		{
			if(def != null)
			{
				System.out.println("Default rule exists, but tests can be generated");
				if(allPermitRules(rules, rules.size() - 1))
				{
					buildRTFRequests_override(rules, generator, t, 0, opt);
				}
				//else if(allDenyRules(rules, rules.size() - 1))
					//build_OnlyOne_Request_false(rules, generator, t, 1, count, "RTF");
				else
				{
					buildRTFRequests_override(rules, generator, t, 1, opt);
				}
			}
			else
			{
				if(allPermitRules(rules, rules.size()))
					buildRTFRequests_override(rules, generator, t, 0, opt);
				//else if(f.allDenyRule(policy))
					//build_OnlyOne_Request_false(rules, generator, t, 1, count, "RTF");
				else
					buildRTFRequests_override(rules, generator, t, 1, opt);
			}
			return generator;
		}
		else if(cmbAlg instanceof DenyUnlessPermitRuleAlg)
		{
			if(f.allDenyRule(policy))
			{
				//System.err.println("Test cannot be generated for given policy");
				//System.err.println("Policy contains nothing but \"Deny\" rules " +
				//"with deny-unless-permit combining algorithm");
				//System.err.println("Please consider writing a better policy and using a different combining algorithm");
				return generator;
			}
			else
				buildRTFRequests_unless(rules, generator, t, 0, opt);
			return generator;
		}
		else if(cmbAlg instanceof PermitUnlessDenyRuleAlg)
		{
			if(f.allPermitRule(policy))
			{
				//System.err.println("Test cannot be generated for given policy");
				//System.err.println("Policy contains nothing but \"Permit\" rules " +
				//"with permit-unless-deny combining algorithm");
				//System.err.println("Please consider writing a better policy and using a different combining algorithm");
				return generator;
			}
			else
				buildRTFRequests_unless(rules, generator, t, 1, opt);
			return generator;
		}
		else if(cmbAlg instanceof FirstApplicableRuleAlg)
		{
			if(def != null)
			{
				build_DefaultRTFRequests_FA(rules, def, generator, t, opt);
			}
			else if(f.allDenyRule(policy) || f.allPermitRule(policy))
				build_allOne_RequestsTrue(rules, generator, t, "RTF", opt);
			else
				buildRTFRequests_FA(rules, generator, t, opt);
			return generator;
		}
		else
			System.err.print("Policy's rule combining algorithm not currently supported");
		return generator;
	}
	
	public ArrayList<PolicySpreadSheetTestRecord> generate_RuleConditionTrue(TestPanel t, boolean opt)
	{
		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
		CombiningAlgorithm cmbAlg = policy.getCombiningAlg();
		List<Rule> rules = getRuleFromPolicy(policy);
		int count = 1;
		Rule def = isDefaultRule(rules.get(rules.size() - 1)) ? rules.get(rules.size() - 1) : null;
		function f = new function();
		
		if(cmbAlg instanceof PermitOverridesRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 0)
				{
					//System.err.println("Test cannot be generated for given policy");
					//System.err.println("The policy contains a default \"Permit\" rule with permit-overrides"
							//+ " combining algorithm");
					return generator;
				}
				else
				{
					System.out.println("Default rule exists, but tests can be generated");
					if(allDenyRules(rules, rules.size()-1))
						buildRCTRequests_override(rules, generator, t, 1, opt);
					else
					{
						buildRCTRequests_override(rules, generator, t, 0, opt);
					}
				}
			}
			else if(f.allDenyRule(policy))
				buildRCTRequests_override(rules, generator, t, 1, opt);
			else
				buildRCTRequests_override(rules, generator, t, 0, opt);
			return generator;
		}
		else if(cmbAlg instanceof DenyOverridesRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 1)
				{
					//System.err.println("Test cannot be generated");
					return generator;
				}
				else if(allPermitRules(rules, rules.size() - 1))
					buildRCTRequests_override(rules, generator, t, 0, opt);
				else
				{
					buildRCTRequests_override(rules, generator, t, 1, opt);
				}
					
			}
			else if(f.allPermitRule(policy))
				buildRCTRequests_override(rules, generator, t, 0, opt);
			else
			{
				buildRCTRequests_override(rules, generator, t, 1, opt);
			}
			return generator;
		}
		else if(cmbAlg instanceof DenyUnlessPermitRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 0)
				{
					//System.err.println("Test cannot be generated");
					return generator;
				}
				else
					buildRCTRequests_unless(rules, generator, t, 0, opt);
			}
			else if(f.allDenyRule(policy))
			{
				//System.err.println("Test cannot be generated");
				return generator;
			}
			else
				buildRCTRequests_unless(rules, generator, t, 0, opt);
			return generator;
		}
		else if(cmbAlg instanceof PermitUnlessDenyRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 0)
				{
					//System.err.println("Test cannot be generated");
					return generator;
				}
				else
					buildRCTRequests_unless(rules, generator, t, 1, opt);
			}
			else if(f.allPermitRule(policy))
			{
				//System.err.println("Test cannot be generated");
				return generator;
			}
			else
				buildRCTRequests_unless(rules, generator, t, 1, opt);
			return generator;
		}
		else if(cmbAlg instanceof FirstApplicableRuleAlg)
		{
			if(def != null)
				build_DefaultRCTRequests_FA(rules, def, generator, t, opt);
			else if(f.allPermitRule(policy) || f.allDenyRule(policy))
				build_AllOne_ConditionRequestsFalse(rules, generator, t, "RCT", opt);
			else
				buildRCTRequests_FA(rules, generator, t, opt);
			return generator;	
		}
		else
			//System.err.println("Combining algorithm not currently supported");
		return generator;
	}
	
	
	public ArrayList<PolicySpreadSheetTestRecord> generate_RuleConditionFalse(TestPanel t, boolean opt)
	{
		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
		CombiningAlgorithm cmbAlg = policy.getCombiningAlg();
		List<Rule> rules = getRuleFromPolicy(policy);
		Rule def = isDefaultRule(rules.get(rules.size() - 1)) ? rules.get(rules.size() - 1) : null;
		function f = new function();
		if(cmbAlg instanceof PermitOverridesRuleAlg)
		{
			if(f.allDenyRule(policy))
			{
				buildRCFRequests_override(rules, generator, t, 1, opt);
			}
			else
				buildRCFRequests_override(rules, generator, t, 0, opt);
			return generator;
		}
		else if(cmbAlg instanceof DenyOverridesRuleAlg)
		{
			if(f.allPermitRule(policy))
			{
				buildRCFRequests_override(rules, generator, t, 0, opt);
			}
			else
				buildRCFRequests_override(rules, generator, t, 1, opt);
			return generator;
		}
		else if(cmbAlg instanceof PermitUnlessDenyRuleAlg)
		{
			if(f.allPermitRule(policy))
			{
				//System.err.println("Test cannot be generated");
				return generator;
			}
			else
				buildRCFRequests_unless(rules, generator, t, 1, opt);
			return generator;
		}
		else if(cmbAlg instanceof DenyUnlessPermitRuleAlg)
		{
			if(f.allDenyRule(policy))
			{
				//System.err.println("Test cannot be generated");
				return generator;
			}
			else
				buildRCFRequests_unless(rules, generator, t, 0, opt);
			return generator;
		}
		else if(cmbAlg instanceof FirstApplicableRuleAlg)
		{
			if(def != null)	
				build_DefaultRCFRequests_FA(rules, def, generator, t, opt);
			else if(f.allDenyRule(policy) || f.allPermitRule(policy))
				build_AllOne_ConditionRequestsTrue(rules, generator, t, "RCF", opt);
			else
				buildRCFRequests_FA(rules, generator, t, opt);
			return generator;
		}
		return generator;
	}
	
	public ArrayList<PolicySpreadSheetTestRecord> generate_RemoveNotFunction(TestPanel t, boolean opt)
	{
		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
		CombiningAlgorithm cmbAlg = policy.getCombiningAlg();
		List<Rule> rules = getRuleFromPolicy(policy);
		Rule def = isDefaultRule(rules.get(rules.size() - 1)) ? rules.get(rules.size() - 1) : null;
		function f = new function();
		if(cmbAlg instanceof PermitOverridesRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 0)
				{
					//System.err.println("Test cannot be generated");
					return generator;
				}
				else if(f.allDenyRule(policy))
					buildRNFRequests_override(rules, generator, t, 1, opt);
				else
					buildRNFRequests_override(rules, generator, t, 0, opt);
			}
			else if(f.allDenyRule(policy))
				buildRNFRequests_override(rules, generator, t, 1, opt);
			else
				buildRNFRequests_override(rules, generator, t, 0, opt);
		}
		else if(cmbAlg instanceof DenyOverridesRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 1)
				{
					//System.err.println("Test cannot be generated");
					return generator;
				}
				else if(f.allPermitRule(policy))
					buildRNFRequests_override(rules, generator, t, 0, opt);
				else
					buildRNFRequests_override(rules, generator, t, 1, opt);
			}
			else if(f.allPermitRule(policy))
				buildRNFRequests_override(rules, generator, t, 0, opt);
			else
			{
				buildRNFRequests_override(rules, generator, t, 1, opt);
			}
		}
		else if(cmbAlg instanceof PermitUnlessDenyRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 1)
				{
					System.err.println("Test cannot be generated");
					return generator;
				}
				else if(f.allPermitRule(policy))
				{
					System.err.println("Test cannot be generated");
					return generator;
				}
				else
					buildRNFRequests_unless(rules, generator, t, 1, opt);
			}
			else if(f.allPermitRule(policy))
			{
				System.err.println("Test cannot be generated");
				return generator;
			}
			else
				buildRNFRequests_unless(rules, generator, t, 1, opt);
		}
		else if(cmbAlg instanceof DenyUnlessPermitRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 0)
				{
					System.err.println("Test cannot be generated");
					return generator;
				}
				else if(f.allDenyRule(policy))
				{
					System.err.println("Test cannot be generated");
					return generator;
				}
				else
					buildRNFRequests_unless(rules, generator, t, 0, opt);
			}
			else if(f.allDenyRule(policy))
			{
				System.err.println("Test cannot be generated");
				return generator;
			}
			else
				buildRNFRequests_unless(rules, generator, t, 0, opt);
		}
		else if(cmbAlg instanceof FirstApplicableRuleAlg)
		{
			if(def != null)
				buildRNFRequests_default(rules, def, generator, t, opt);
			else if(f.allDenyRule(policy) || f.allPermitRule(policy))
				build_AllOne_ConditionRequestsTrue(rules, generator, t, "RNF", opt);
			else
				buildRNFRequests_FA(rules, generator, t, opt);
		}
		else
			System.err.println("Combining algorithm not current supported");
		return generator;
	}
	
	public ArrayList<PolicySpreadSheetTestRecord> generate_FlipRuleEffect(TestPanel t, boolean opt)
	{
		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
		List<Rule> rules = getRuleFromPolicy(policy);
		CombiningAlgorithm cmbAlg = policy.getCombiningAlg();
		Rule def = isDefaultRule(rules.get(rules.size() - 1)) ? rules.get(rules.size() - 1) : null;
		if(cmbAlg instanceof PermitOverridesRuleAlg || cmbAlg instanceof DenyUnlessPermitRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 0)
				{
					if(rule_table[rule_table.length - 1][TRUE_TRUE] == true)
						return generator;
					ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
					StringBuffer sb = new StringBuffer();
					PolicySpreadSheetTestRecord ptr = null;
					sb.append(TruePolicyTarget(policy, collector) + "\n");
					ptr = buildRequest_true(rules, def, sb, collector, 1, t, rules.size(), "CRE");
					if(ptr != null)
					{
						if(opt)
							checkForCoverage(rules, def, ptr.getRequest(), rules.size() - 1, TRUE_TRUE);
						generator.add(ptr);
					}
					return generator;
				}
			}
		}
		if(cmbAlg instanceof DenyOverridesRuleAlg || cmbAlg instanceof PermitUnlessDenyRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 1)
				{
					if(rule_table[rule_table.length - 1][TRUE_TRUE] == true)
						return generator;
					ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
					StringBuffer sb = new StringBuffer();
					PolicySpreadSheetTestRecord ptr = null;
					sb.append(TruePolicyTarget(policy, collector) + "\n");
					ptr = buildRequest_true(rules, def, sb, collector, 1, t, rules.size() - 1, "CRE");
					if(ptr != null)
					{
						if(opt)
							checkForCoverage(rules, def, ptr.getRequest(), rules.size() - 1, TRUE_TRUE);
						generator.add(ptr);
					}
					return generator;
				}
			}
		}
		int count = 1;
		for(int i = 0; i < rules.size(); i++)
		{
			if(rule_table[i][TRUE_TRUE] == true)
				continue;
			Rule r = rules.get(i);
			if(isDefaultRule(r))
				continue;
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			StringBuffer sb = new StringBuffer();
			PolicySpreadSheetTestRecord ptr = null;
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			ptr = buildRequest_true(rules, r, sb, collector, count, t, rules.size(), "CRE");
			if(ptr != null)
			{
				if(opt)
					checkForCoverage(rules, r, ptr.getRequest(), i, TRUE_TRUE);
				generator.add(ptr);
				count++;
			}
		}
		return generator;
	}
	
	public ArrayList<PolicySpreadSheetTestRecord> generate_RemoveOneRule(TestPanel t, boolean opt)
	{
		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
		List<Rule> rules = getRuleFromPolicy(policy);
		CombiningAlgorithm cmbAlg = policy.getCombiningAlg();
		function f = new function();
		boolean allDeny = false;
		boolean allPermit = false;
		boolean defaultRule = isDefaultRule(rules.get(rules.size() - 1));
		if(defaultRule)
		{
			allDeny = allDenyRules(rules, rules.size() - 1);
			if(!allDeny)
				allPermit = allPermitRules(rules, rules.size() - 1);
		}
		else
		{
			allDeny = f.allDenyRule(policy);
			if(!allDeny)
				allPermit = f.allPermitRule(policy);
		}
		int count = 1, tpos = TRUE_TRUE;
		for(int i = 0; i < rules.size(); i++)
		{
			if(rule_table[i][tpos] == true)
				continue;
			Rule r = rules.get(i);
			PolicySpreadSheetTestRecord ptr = null;
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			StringBuffer sb = new StringBuffer();
			if(cmbAlg instanceof DenyUnlessPermitRuleAlg)
			{
				if(allDeny)
				{
					System.err.println("Test cannot be generated");
					System.err.println("Policy contains all deny rules");
					System.err.println("COMBINING ALGORITHM: " + cmbAlg.toString());
					return generator;
				}
				else if(r.getEffect() == 0)
					ptr = buildRequest_true(rules, r, sb, collector, count, t, rules.size(), "RER");
				else
				{
					tpos = FALSE_TRUE;
					buildRequest_false(rules, r, sb, collector, count, t, rules.size(), "RER");
				}
			}
			else if(cmbAlg instanceof PermitUnlessDenyRuleAlg)
			{
				if(allPermit)
				{
					System.err.println("Test cannot be generated");
					System.err.println("Policy contains all permit rules");
					System.err.println("COMBINING ALGORITHM: " + cmbAlg.toString());
					return generator;
				}
				else if(r.getEffect() == 1)
					ptr = buildRequest_true(rules, r, sb, collector, count, t, rules.size(), "RER");
				else
				{
					tpos = FALSE_TRUE;
					buildRequest_false(rules, r, sb, collector, count, t, rules.size(), "RER");
				}
			}
			else
			{
				sb.append(TruePolicyTarget(policy, collector) + "\n");
				ptr = buildRequest_true(rules, r, sb, collector, count, t, rules.size(), "RER");
			}
			if(ptr != null)
			{
				if(opt)
					checkForCoverage(rules, r, ptr.getRequest(), i, tpos);
				generator.add(ptr);
				count++;
			}
			tpos = TRUE_TRUE;
		}
		return generator;
	}
	
	public ArrayList<PolicySpreadSheetTestRecord> generate_AddNotFunction(TestPanel t, boolean opt)
	{
		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
		List<Rule> rules = getRuleFromPolicy(policy);
		CombiningAlgorithm cmbAlg = policy.getCombiningAlg();
		function f = new function();
		Rule def = isDefaultRule(rules.get(rules.size() - 1)) ? rules.get(rules.size() - 1) : null;
		if(cmbAlg instanceof PermitOverridesRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 0)
				{
					System.err.println("Test cannot be generated");
					return generator;
				}
				else
					buildANFRequests_override(rules, generator, t, opt);
			}
			else
				buildANFRequests_override(rules, generator, t, opt);
			return generator;
		}
		else if(cmbAlg instanceof DenyOverridesRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 1)
				{
					System.err.println("Test cannot be generated");
					return generator;
				}
				else
					buildANFRequests_override(rules, generator, t, opt);
			}
			else
				buildANFRequests_override(rules, generator, t, opt);
		}
		else if(cmbAlg instanceof PermitUnlessDenyRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 1)
				{
					System.err.println("Test cannot be generated");
					return generator;
				}
				else if(f.allPermitRule(policy))
				{
					System.err.println("Test cannot be generated");
					return generator;
				}
				else
					buildANFRequests_unless(rules, generator, t, 1, opt);
			}
			else if(f.allPermitRule(policy))
			{
				System.err.println("Test cannot be generated");
				return generator;
			}
			else
				buildANFRequests_unless(rules, generator, t, 1, opt);
			return generator;
		}
		else if(cmbAlg instanceof DenyUnlessPermitRuleAlg)
		{
			if(def != null)
			{
				if(def.getEffect() == 0)
				{
					System.err.println("Test cannot be generated");
					return generator;
				}
				else if(f.allDenyRule(policy))
				{
					System.err.println("Test cannot be generated");
					return generator;
				}
				else
					buildANFRequests_unless(rules, generator, t, 0, opt);
			}
			else if(f.allDenyRule(policy))
			{
				System.err.println("Test cannot be generated");
				return generator;
			}
			else
				buildANFRequests_unless(rules, generator, t, 0, opt);
		}
		else if(cmbAlg instanceof FirstApplicableRuleAlg)
		{
			if(def != null)
				buildANFRequests_default(rules, def, generator, t, opt);
			if(f.allDenyRule(policy) || f.allPermitRule(policy))
				build_AllOne_ConditionRequestsFalse(rules, generator, t, "ANF", opt);
			else
				buildANFRequests_FA(rules, generator, t, opt);
		}
		else
			System.err.println("Combining algorithm not currently supported");
		return generator;
	}
	
	public ArrayList<PolicySpreadSheetTestRecord> generate_FirstDenyRule(TestPanel t, boolean opt)
	{
		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
		CombiningAlgorithm cmbAlg = policy.getCombiningAlg();
		List<Rule> rules = getRuleFromPolicy(policy);
		function f = new function();
		int count = 1;
		if(cmbAlg instanceof FirstApplicableRuleAlg)
		{
			if(f.allDenyRule(policy) || f.allPermitRule(policy))
			{
				System.err.println("Test cannot be generated");
				return generator;
			}
			else
			{
				for(int i = 0; i < rules.size(); i++)
				{
					Rule r = rules.get(i);
					StringBuffer sb = new StringBuffer();
					ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
					PolicySpreadSheetTestRecord ptr = null;
					if(r.getEffect() == 0)
					{
						ptr = buildRequest_trueFA(rules, r, sb, collector, count, t, "FDR");
						if(rule_table[i][TRUE_TRUE] == true)
							break;
					}
					if(ptr != null)
					{
						if(opt)
							checkForCoverage(rules, r, ptr.getRequest(), i, TRUE_TRUE);
						generator.add(ptr);
						break;
					}
				}
			}
		}
		else
			System.err.println("Test can only be generated for first-applicable combining algorithm");
		return generator;
	}
	
	public ArrayList<PolicySpreadSheetTestRecord> generate_FirstPermitRule(TestPanel t, boolean opt)
	{
		ArrayList<PolicySpreadSheetTestRecord> generator = new ArrayList<PolicySpreadSheetTestRecord>();
		CombiningAlgorithm cmbAlg = policy.getCombiningAlg();
		List<Rule> rules = getRuleFromPolicy(policy);
		function f = new function();
		int count = 1;
		if(cmbAlg instanceof FirstApplicableRuleAlg)
		{
			if(f.allDenyRule(policy) || f.allPermitRule(policy))
			{
				System.err.println("Test cannot be generated");
				return generator;
			}
			else
			{
				for(int i = 0; i < rules.size(); i++)
				{
					Rule r = rules.get(i);
					StringBuffer sb = new StringBuffer();
					ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
					PolicySpreadSheetTestRecord ptr = null;
					if(r.getEffect() == 1)
					{
						ptr = buildRequest_trueFA(rules, r, sb, collector, count, t, "FDR");
						if(rule_table[i][TRUE_TRUE] == true)
							break;
					}
					if(ptr != null)
					{
						if(opt)
							checkForCoverage(rules, r, ptr.getRequest(), i, TRUE_TRUE);
						generator.add(ptr);
						break;
					}
				}
			}
		}
		else
			System.err.println("Test can only be generated for first-applicable combining algorithm");
		return generator;
	}
	
	private void buildANFRequests_override(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, boolean opt)
	{
		int count = 1;
		for(int i = 0; i < rules.size(); i++)
		{
			if(rule_table[i][TRUE_FALSE] == true)
				continue;
			Rule r = rules.get(i);
			PolicySpreadSheetTestRecord ptr = null;
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			StringBuffer sb = new StringBuffer();
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			if(!r.isConditionEmpty())
				ptr = buildConditionRequest_false(rules, r, sb, collector, count, t, rules.size(), "ANF");
			if(ptr != null)
			{
				if(opt)
					checkForCoverage(rules, r, ptr.getRequest(), i, TRUE_FALSE);
				generator.add(ptr);
				count++;
			}
		}
	}
	
	private void buildRCTRequests_override(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, int effect, boolean opt)
	{
		int count = 1;
		for(int i = 0; i < rules.size(); i++)
		{
			if(rule_table[i][TRUE_FALSE] == true)
				continue;
			Rule r = rules.get(i);
			PolicySpreadSheetTestRecord ptr = null;
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			StringBuffer sb = new StringBuffer();
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			if(!r.isConditionEmpty())
			{
				ptr = buildConditionRequest_false(rules, r, sb, collector, count, t, rules.size(), "RCT");
			}
			//else
				//System.err.println( does not contain a condition");
			if(ptr != null)
			{
				if(opt)
					checkForCoverage(rules, r, ptr.getRequest(), i, TRUE_FALSE);
				generator.add(ptr);
				count++;
			}
		}
	}
	
	private void buildRTTRequests_override(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, int effect, boolean opt)
	{
		int count = 1;
		for(int i = 0; i < rules.size(); i++)
		{
			//System.err.println("LOOP");
			if(rule_table[i][FALSE_TRUE] == true)
				continue;
			Rule temp = rules.get(i);
			//System.err.println("Grabbed rule");
			PolicySpreadSheetTestRecord ptr = null;
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			StringBuffer sb = new StringBuffer();
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			if(!temp.isTargetEmpty())
				ptr = buildRequest_false(rules, temp, sb, collector, count, t, rules.size(), "RTT");
			//System.err.println("Made a request");
			if(ptr != null)
			{
				//System.err.println("Checking for coverage");
				String request = ptr.getRequest();
				if(opt)
					checkForCoverage(rules, temp, request, i, FALSE_TRUE);
				generator.add(ptr);
				count++;
			}
		}
	}
	
	private void buildRPTERequests_override(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, boolean opt)
	{
		int count = 1, ind_count = 1;
		int index = 0;
		//boolean[][] alls = buildAllOfTable(rules);
		//boolean[][] anys = buildAnyOfTable(rules);
		//boolean[][] mats = buildMatchTable(rules);
	for(Rule r : rules)
	{
		
		if(!r.isTargetEmpty())
		{
			Target rt = (Target)r.getTarget();
			List<AnyOfSelection> anyOf = rt.getAnyOfSelections();
			if(anyOf.size() != 0)
			{
				if(anyOf.size() > 1)
				{
					for(int i = 0; i < anyOf.size(); i++)
					{
						//if(anys[index][i] == true)
							//continue;
						ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
						PolicySpreadSheetTestRecord ptr = null;
						StringBuffer sb = new StringBuffer();
						sb.append(TruePolicyTarget(policy, collector) + "\n");
						ptr = buildRPTERequest1(rules, r, sb, collector, count, t, i);
						if(ptr != null)
						{
							//if(opt)
								//updateFaultTable_RPTE(rules, ptr.getRequest(), index);
							generator.add(ptr);
							count++;
						}
					}
				}
				for(int k = 0; k < anyOf.size(); k++)
				{
					AnyOfSelection any = anyOf.get(k);
					List<AllOfSelection> allOf = any.getAllOfSelections();
					if(allOf.size() != 0)
					{
						if(allOf.size() > 1)
						{
							for(int i = 0; i < allOf.size(); i++)
							{
								//if(alls[index][i] == true)
									//continue;
								ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
								PolicySpreadSheetTestRecord ptr = null;
								StringBuffer sb = new StringBuffer();
								sb.append(TruePolicyTarget(policy, collector) + "\n");
								ptr = buildRPTERequest2(rules, r, sb, collector, count, t, i, k);
								if(ptr != null)
								{
									//if(opt)
										//updateFaultTable_RPTE(rules, ptr.getRequest(), index);
									generator.add(ptr);
									count++;
								}
							}
						}
						for(int j = 0; j < allOf.size(); j++)
						{
							AllOfSelection all = allOf.get(j);
							List<TargetMatch> matches = all.getMatches();
							if(matches.size() != 0)
							{
								if(matches.size() > 1)
								{
									for(int i = 0; i < matches.size(); i++)
									{
										//if(mats[index][i] == true)
											//continue;
										ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
										PolicySpreadSheetTestRecord ptr = null;
										StringBuffer sb = new StringBuffer();
										sb.append(TruePolicyTarget(policy, collector) + "\n");
										ptr = buildRPTERequest3(rules, r, sb, collector, count, t, i, j);
										if(ptr != null)
										{
											//if(opt)
												//updateFaultTable_RPTE(rules, ptr.getRequest(), index);
											generator.add(ptr);
											count++;
										}
									}
								}
							}
						}
					}
				}
			}
		}
		index++;
	}
}
	
	private void buildRTFRequests_override(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, int effect, boolean opt)
	{
		int count = 1, tpos = TRUE_TRUE;
		for(int i = 0; i < rules.size(); i++)
		{
			if(rule_table[i][tpos] == true)
				continue;
			Rule temp = rules.get(i);
			StringBuffer sb = new StringBuffer();
			PolicySpreadSheetTestRecord ptr = null;
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			if(temp.getEffect() == effect)
				ptr = buildRequest_true(rules, temp, sb, collector, count, t, rules.size(), "RTF");
			else
			{
				if(!temp.isTargetEmpty())
				{
					if(temp.isConditionEmpty())
						tpos = TRUE_TRUE;
					else
						tpos = FALSE_TRUE;
				}
				else
					tpos = TRUE_TRUE;
				ptr = buildRequest_false2(rules, temp, sb, collector, count, t, rules.size(), "RTF");
			}
			if(ptr != null)
			{
				if(opt)
					checkForCoverage(rules, temp, ptr.getRequest(), i, tpos);
				generator.add(ptr);
				count++;
			}
			tpos = TRUE_TRUE; //reset table position
		}
	}
	
	private void buildRCFRequests_override(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, int effect, boolean opt)
	{
		int count = 1, tpos = TRUE_TRUE;
		for(int i = 0; i < rules.size(); i++)
		{
			if(rule_table[i][tpos] == true)
				continue;
			Rule r = rules.get(i);
			StringBuffer sb = new StringBuffer();
			PolicySpreadSheetTestRecord ptr = null;
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			if(r.getEffect() == effect && !r.isConditionEmpty())
				ptr = buildRequest_true(rules, r, sb, collector, count, t, rules.size(), "RCF");
			else if(r.getEffect() != effect && !r.isConditionEmpty())
			{
				ptr = buildConditionRequest_false(rules, r, sb, collector, count, t, rules.size(), "RCF");
				tpos = TRUE_FALSE;
			}
			else
				ptr = buildRequest_true(rules, r, sb, collector, count, t, rules.size(), "RCF");
			if(ptr != null)
			{
				if(opt)
					checkForCoverage(rules, r, ptr.getRequest(), i, tpos);
				generator.add(ptr);
				count++;
			}
			tpos = TRUE_TRUE; //important: reset table position to TRUE_TRUE;
		}
	}
	
	private void buildRNFRequests_override(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, int effect, boolean opt)
	{
		int count = 1, tpos = TRUE_TRUE;
		for(int i = 0; i < rules.size(); i++)
		{
			if(rule_table[i][tpos] == true)
				continue;
			Rule r = rules.get(i);
			StringBuffer sb = new StringBuffer();
			PolicySpreadSheetTestRecord ptr = null;
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			if(r.getEffect() == effect && !r.isConditionEmpty() && containsNot(r))
				ptr = buildRequest_true(rules, r, sb, collector, count, t, rules.size(), "RNF");
			else if(r.getEffect() != effect && !r.isConditionEmpty() && containsNot(r))
			{
				tpos = TRUE_FALSE;
				ptr = buildConditionRequest_false(rules, r, sb, collector, count, t, rules.size(), "RNF");
			}
			if(ptr != null)
			{
				if(opt)
					checkForCoverage(rules, r, ptr.getRequest(), i, tpos);
				generator.add(ptr);
				count++;
			}
			tpos = TRUE_TRUE;
		}
	}
	
	private PolicySpreadSheetTestRecord buildRequest_false(List<Rule> rules, Rule rule, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, int stop, String type)
	{
		//System.err.println("Building request");
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		//Make current rule evaluate to false
		sb.append(False_Target((Target)rule.getTarget(), collector) + "\n");
		sb.append(True_Condition(rule.getCondition(), collector) + "\n");
		System.err.println("False target true condition");
		//Ensure rules before and following current evaluate to false (NotApplicable)
		for(int i = 0; i < stop; i++)
		{
			//System.err.println("LOOP");
			Rule temp = rules.get(i);
			if(temp.getId().equals(rule.getId()) || isDefaultRule(temp))
				continue;
			else
				sb.append(FalseTarget_FalseCondition(temp, collector) + "\n");
		}
		//System.err.println("exited loop");
		//System.out.println("Here is the z3-str input: \n" + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		//System.err.println("Sent to z3");
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			//System.out.println(request);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" + type + count + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + type + count,
					"request" + type + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildRPTERequest3(List<Rule> rules, Rule rule, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, int atindex, int alindex)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		if(!(policy.getCombiningAlg() instanceof FirstApplicableRuleAlg))
			sb.append(getTargetAttribute_NegateValue((Target)rule.getTarget(), alindex, atindex, collector) + "\n");
		else
			sb.append(getTargetAttribute_FALSEIND((Target)rule.getTarget(), alindex, atindex, collector) + "\n");
		sb.append(True_Condition(rule.getCondition(), collector) + "\n");
		
		for(Rule r : rules)
		{
			if(r.getId().equals(rule.getId()) || isDefaultRule(r))
				continue;
			else
				sb.append(False_Condition(r.getCondition(), collector) + "\n");
		}
		//System.out.println("Here is the z3-str input: \n" + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			//System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			//System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "requestRPTE" + count + ".txt";
				BufferedWriter bw = new BufferedWriter(new FileWriter(path));
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " RPTE" + count,
					"requestRPTE" + count + ".txt", request, "");
		}
		else
			System.err.println("UNSAT");
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildRPTERequest3IND(List<Rule> rules, Rule rule, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, int atindex, int alindex)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		sb.append(getTargetAttribute_NegateValue((Target)rule.getTarget(), alindex, atindex, collector) + "\n");
		sb.append(True_Condition(rule.getCondition(), collector) + "\n");
		
		for(Rule r : rules)
		{
			if(r.getId().equals(rule.getId()) || isDefaultRule(r))
				continue;
			else
				sb.append(False_Condition(r.getCondition(), collector) + "\n");
		}
		//System.out.println("Here is the z3-str input: \n" + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "requestRPTE_IND" + count + ".txt";
				BufferedWriter bw = new BufferedWriter(new FileWriter(path));
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " RPTE" + count,
					"requestRPTE_IND" + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildRPTERequest1(List<Rule> rules, Rule rule, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, int anindex)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		sb.append(getTargetAnyOf_NegateValues((Target)rule.getTarget(), anindex, collector) + "\n");
		sb.append(True_Condition(rule.getCondition(), collector) + "\n");
		
		for(Rule r : rules)
		{
			if(r.getId().equals(rule.getId()) || isDefaultRule(r))
				continue;
			else
				sb.append(False_Condition(r.getCondition(), collector) + "\n");
		}
		//System.out.println("Here is the z3-str input: \n" + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "requestRPTE" + count + ".txt";
				BufferedWriter bw = new BufferedWriter(new FileWriter(path));
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " RPTE" + count,
					"requestRPTE" + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildRPTERequest1IND(List<Rule> rules, Rule rule, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, int anindex)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		sb.append(getTargetAnyOf_NegateValues((Target)rule.getTarget(), anindex, collector) + "\n");
		sb.append(True_Condition(rule.getCondition(), collector) + "\n");
		
		for(Rule r : rules)
		{
			if(r.getId().equals(rule.getId()) || isDefaultRule(r))
				continue;
			else
				sb.append(False_Condition(r.getCondition(), collector) + "\n");
		}
		//System.out.println("Here is the z3-str input: \n" + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "requestRPTE_IND" + count + ".txt";
				BufferedWriter bw = new BufferedWriter(new FileWriter(path));
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " RPTE" + count,
					"requestRPTE_IND" + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildRPTERequest2(List<Rule> rules, Rule rule, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, int alindex, int anindex)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		sb.append(getTargetAllOf_NegateValues((Target)rule.getTarget(), anindex, alindex, collector) + "\n");
		sb.append(True_Condition(rule.getCondition(), collector) + "\n");
		
		for(Rule r : rules)
		{
			if(r.getId().equals(rule.getId()) || isDefaultRule(r))
				continue;
			else
				sb.append(False_Condition(r.getCondition(), collector) + "\n");
		}
		//System.out.println("Here is the z3-str input: \n" + sb.toString());
		//System.err.println("nameMap null? " + nameMap == null);
		//System.err.println("typeMap null? " + typeMap == null);
		//System.err.println("sb null? " + sb == null);
		//System.err.println("sb tostring null? " + sb.toString() == null);
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "requestRPTE" + count + ".txt";
				BufferedWriter bw = new BufferedWriter(new FileWriter(path));
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " RPTE" + count,
					"requestRPTE" + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildRPTERequest2IND(List<Rule> rules, Rule rule, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, int alindex, int anindex)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		sb.append(getTargetAllOf_NegateValues((Target)rule.getTarget(), anindex, alindex, collector) + "\n");
		sb.append(True_Condition(rule.getCondition(), collector) + "\n");
		
		for(Rule r : rules)
		{
			if(r.getId().equals(rule.getId()) || isDefaultRule(r))
				continue;
			else
				sb.append(False_Condition(r.getCondition(), collector) + "\n");
		}
		//System.out.println("Here is the z3-str input: \n" + sb.toString());
		//System.err.println("nameMap null? " + nameMap == null);
		//System.err.println("typeMap null? " + typeMap == null);
		//System.err.println("sb null? " + sb == null);
		//System.err.println("sb tostring null? " + sb.toString() == null);
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "requestRPTE_IND" + count + ".txt";
				BufferedWriter bw = new BufferedWriter(new FileWriter(path));
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " RPTE" + count,
					"requestRPTE_IND" + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildRequest_false2(List<Rule> rules, Rule rule, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, int stop, String type)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		//Make current rule evaluate to false
		if(!rule.isTargetEmpty())
		{
			if(rule.isConditionEmpty())
				sb.append(True_Target((Target)rule.getTarget(), collector) + "\n");
			else
			{
				sb.append(False_Target((Target)rule.getTarget(), collector) + "\n");
				sb.append(True_Condition(rule.getCondition(), collector) + "\n");
			}
		}
		else
			sb.append(True_Condition(rule.getCondition(), collector) + "\n");
		
		//Ensure rules before and following current evaluate to false (NotApplicable)
		for(int i = 0; i < stop; i++)
		{
			Rule temp = rules.get(i);
			if(temp.getId().equals(rule.getId()) || isDefaultRule(temp))
				continue;
			else
				sb.append(FalseTarget_FalseCondition(temp, collector) + "\n");
		}
		//System.out.println("Here is the z3-str input: \n" + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			//System.out.println(request);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" + type + count + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + type + count,
					"request" + type + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildConditionRequest_false(List<Rule> rules, Rule rule, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, int stop, String type)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		sb.append(TrueTarget_FalseCondition(rule, collector) + "\n");
		
		for(int i = 0; i < stop; i++)
		{
			Rule r = rules.get(i);
			if(r.getId().equals(rule.getId()) || isDefaultRule(r))
				continue;
			else
				sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
		}
		//System.out.println("Here is the z3-str input: \n" + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" + type + count + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + type + count,
					"request" + type + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildConditionRequest_true(List<Rule> rules, Rule rule, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, int stop, String type)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		sb.append(TrueTarget_TrueCondition(rule, collector) + "\n");
		
		for(int i = 0; i < stop; i++)
		{
			Rule r = rules.get(i);
			if(r.getId().equals(rule.getId()) || isDefaultRule(r))
				continue;
			else
				sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
		}
		//System.out.println("Here is the z3-str input: \n" + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" + type + count + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + type + count,
					"request" + type + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildRequest_Allfalse(List<Rule> rules, Rule current, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, int stop, String type, int effect)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		Rule firstDiff = null;
		//For a policy containing a default rule
		//we must ensure that the policy never returns the default rule's effect 
		//and the mutant always returns the default rule's effect
		//To do this, we find the first rule regardless of the position in the policy
		//that has a different effect than the default rule and make sure that it evals to true
		//therefore returning either the opposite of the default rule's effect or ID
		//this method is only generates a test when there is at least 1 rule with a different effect than the default rule
		//otherwise, a different method is called
		int diffIndex = -1;
		int currentIndex = rules.indexOf(current);
		for(int i = currentIndex + 1; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(r.getEffect() != effect)
			{
				firstDiff = r;
				diffIndex = i;
				break;
			}
		}

		//if the index of the differing rule is 0 or -1 (i.e. the first rule or a differing rule could not be found)
		//then there is no possible way to generate a valid test
		if(diffIndex < 0)
			return ptr;
		
		else
		{
			sb.append(False_Target((Target)current.getTarget(), collector) + "\n");
			sb.append(True_Condition(current.getCondition(), collector) + "\n");
			sb.append(TrueTarget_TrueCondition(firstDiff, collector) + "\n");
		}
		//Ensure rules before and following current evaluate to false (NotApplicable)
		for(int i = 0; i < stop; i++)
		{	
			Rule temp = rules.get(i);
			if(isDefaultRule(temp) || temp.getId().equals(current.getId()) || temp.getId().equals(firstDiff.getId()))
				continue;
			else
				sb.append(FalseTarget_FalseCondition(temp, collector) + "\n");
		}
		//System.out.println("Here is the z3-str input: \n" + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			//System.out.println(request);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" + type + count + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + type + count,
					"request" + type + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildRPTERequest_AllFalse3(List<Rule> rules, Rule current, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, int effect, int atindex, int alindex)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		Rule firstDiff = null;
		int diffIdx = -1;
		int currentIndex = rules.indexOf(current);
		for(int i = currentIndex + 1; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(r.getEffect() != effect)
			{
				firstDiff = r;
				diffIdx = i;
				break;
			}
		}
		
		if(diffIdx < 0 || firstDiff == null)
			return ptr;
		else
		{
			sb.append(getTargetAttribute_NegateValue((Target)current.getTarget(), alindex, atindex, collector) + "\n");
			sb.append(True_Condition(current.getCondition(), collector) + "\n");
			sb.append(TrueTarget_TrueCondition(firstDiff, collector) + "\n");
		}
		
		for(Rule r : rules)
		{
			if(r.getId().equals(current.getId()) || isDefaultRule(r) || r.getId().equals(firstDiff.getId()))
				continue;
			else
				sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
		}
		//System.out.println("Here is the z3-str input: \n" + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size()+ " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "requestRPTE" + count + ".txt";
				BufferedWriter bw = new BufferedWriter(new FileWriter(path));
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " RPTE" + count,
					"requestRPTE" + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildRPTERequest_AllFalse2(List<Rule> rules, Rule current, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, int effect, int alindex, int anindex)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		Rule firstDiff = null;
		int diffIdx = -1;
		int currentIndex = rules.indexOf(current);
		for(int i = currentIndex + 1; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(r.getEffect() != effect)
			{
				firstDiff = r;
				diffIdx = i;
				break;
			}
		}
		
		if(diffIdx < 0 || firstDiff == null)
			return ptr;
		else
		{
			sb.append(getTargetAllOf_NegateValues((Target)current.getTarget(), anindex, alindex, collector) + "\n");
			sb.append(True_Condition(current.getCondition(), collector) + "\n");
			sb.append(TrueTarget_TrueCondition(firstDiff, collector) + "\n");
		}
		
		for(Rule r : rules)
		{
			if(r.getId().equals(current.getId()) || isDefaultRule(r) || r.getId().equals(firstDiff.getId()))
				continue;
			else
				sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
		}
		//System.out.println("Here is the z3-str input: \n" + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size()+ " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "requestRPTE" + count + ".txt";
				BufferedWriter bw = new BufferedWriter(new FileWriter(path));
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " RPTE" + count,
					"requestRPTE" + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildRPTERequest_AllFalse1(List<Rule> rules, Rule current, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, int effect, int anindex)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		Rule firstDiff = null;
		int diffIdx = -1;
		int currentIndex = rules.indexOf(current);
		for(int i = currentIndex + 1; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(r.getEffect() != effect)
			{
				firstDiff = r;
				diffIdx = i;
				break;
			}
		}
		
		if(diffIdx < 0 || firstDiff == null)
			return ptr;
		else
		{
			sb.append(getTargetAnyOf_NegateValues((Target)current.getTarget(), anindex, collector) + "\n");
			sb.append(True_Condition(current.getCondition(), collector) + "\n");
			sb.append(TrueTarget_TrueCondition(firstDiff, collector) + "\n");
		}
		
		for(Rule r : rules)
		{
			if(r.getId().equals(current.getId()) || isDefaultRule(r) || r.getId().equals(firstDiff.getId()))
				continue;
			else
				sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
		}
		//System.out.println("Here is the z3-str input: \n" + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size()+ " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "requestRPTE" + count + ".txt";
				BufferedWriter bw = new BufferedWriter(new FileWriter(path));
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " RPTE" + count,
					"requestRPTE" + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildConditionRequest_AllFalse(List<Rule> rules, Rule current, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, int stop, String type, int effect)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		Rule firstDiff = null;
		int diffIndex = -1;
		for(int i = 0; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(r.getEffect() != effect)
			{
				if(!r.isConditionEmpty())
				{
					diffIndex = i;
					firstDiff = r;
					break;
				}
			}
		}
		
		if(diffIndex <= 0)
			return ptr;
		
		else
		{
			sb.append(True_Target((Target)current.getTarget(), collector) + "\n");
			sb.append(False_Condition(current.getCondition(), collector) + "\n");
			sb.append(TrueTarget_TrueCondition(firstDiff, collector) + "\n");
		}
		
		for(int i = 0; i < stop; i++)
		{
			Rule temp = rules.get(i);
			if(isDefaultRule(temp) || temp.getId().equals(current.getId()) || temp != null ? temp.getId().equals(firstDiff.getId()) : false)
				continue;
			else
				sb.append(FalseTarget_FalseCondition(temp, collector) + "\n");
		}
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" + type + count + ".txt";
				BufferedWriter bw = new BufferedWriter(new FileWriter(path));
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + type + count,
					"request" + type + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildRequest_true(List<Rule> rules, Rule rule, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, int stop, String type)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		
		//Ensure that current rule evaluates to true
		sb.append(TrueTarget_TrueCondition(rule, collector) + "\n");
		
		//ensure rules before and following current evaluate to false
		for(int i = 0; i < stop; i++)
		{
			Rule temp = rules.get(i);
			if(temp.getId().equals(rule.getId()) || isDefaultRule(temp))
				continue;
			else
				sb.append(FalseTarget_FalseCondition(temp, collector) + "\n");
		}
		
		//System.out.println("Here is the z3-str input: \n" + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			if(collector.size() == 0)
				return ptr;
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			////System.out.println(request);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" + type + count + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + type + count,
					"request" + type + count + ".txt", request, "");
		}
		return ptr;
	}
	
	//Turner Lehmbecker
	//Helper method to check if rules from 0 to a specified index are all permit rules 
	//returns true if all rules from 0 to index are permit
	//false if at least one rule from 0 to index is deny
	//NEEDED IN SOME CASES
	private boolean allPermitRules(List<Rule> rules, int index)
	{
		int stop = (index < 0 || index > rules.size()) ? (index < 0 ? 0 : (index > rules.size() ? rules.size() : index)) : index;
		if(stop == 0)
		{
			return rules.get(stop).getEffect() == 0;
		}
		boolean allPermit = true;
		int i = 0;
		while(i < stop && allPermit)
		{
			allPermit = rules.get(i).getEffect() == 0;
			i++;
		}
		return allPermit;
	}
	
	//Turner Lehmbecker
	//Helper method to check if rules from 0 to a specified index are all deny rules 
	//returns true if all rules from 0 to index are deny
	//false if at least one rule from 0 to index is permit
	//NEEDED IN SOME CASES
	private boolean allDenyRules(List<Rule> rules, int index)
	{
		int stop = (index < 0 || index > rules.size()) ? (index < 0 ? 0 : (index > rules.size() ? rules.size() : index)) : index;
		if(stop == 0)
		{
			return rules.get(stop).getEffect() == 1;
		}
		boolean allDeny = true;
		int i = 0;
		while(i < stop && allDeny)
		{
			allDeny = rules.get(i).getEffect() == 1;
			i++;
		}
		return allDeny;
	}
	
	private void build_OnlyOne_Request_false(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, int effect, int count, String type, boolean opt)
	{
		PolicySpreadSheetTestRecord ptr = null;
		StringBuffer sb = new StringBuffer();
		ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
		function f = new function();
		
		if(!policy.isTargetEmpty())
			sb.append(TruePolicyTarget(policy, collector) + "\n");
		for(int i = 0; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(!isDefaultRule(r))
			{
				if(r.getEffect() == effect)
				{
					if(!r.isTargetEmpty() && !r.isConditionEmpty())
					{
						sb.append(False_Target((Target)r.getTarget(), collector) + "\n");
						sb.append(True_Condition(r.getCondition(), collector) + "\n");
					}
					else if(r.isTargetEmpty() && !r.isConditionEmpty())
						sb.append(False_Condition(r.getCondition(), collector) + "\n");
					else if(!r.isTargetEmpty() && r.isConditionEmpty())
						sb.append(False_Target((Target)r.getTarget(), collector) + "\n");
					else
						continue;
				}
				else
					sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
			}
		}
		//System.out.println("Z3: " + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" + type + count + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + type + count,
					"request" + type + count + ".txt", request, "");
			if(ptr != null)
				generator.add(ptr);
		}
		else
		{
			if(type.compareTo("RTT") == 0)
				buildRTTRequests_override(rules, generator, t, effect, opt);
			else if(type.compareTo("RTF") == 0)
				buildRTFRequests_override(rules, generator, t, effect, opt);
		}
	}
	
	private void build_OnlyOne_ConditionRequest_false(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, int effect, int count, String type, boolean opt)
	{
		System.err.println("Generating only one test");
		PolicySpreadSheetTestRecord ptr = null;
		StringBuffer sb = new StringBuffer();
		ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
		function f = new function();
		sb.append(TruePolicyTarget(policy, collector) + "\n");
		for(int i = 0; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(!isDefaultRule(r))
			{
				if(r.getEffect() == effect)
				{
					if(!r.isTargetEmpty() && !r.isConditionEmpty())
						sb.append(TrueTarget_FalseCondition(r, collector) + "\n");
					else if(r.isTargetEmpty() && !r.isConditionEmpty())
						sb.append(False_Condition(r.getCondition(), collector) + "\n");
					else
						sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
				}
				else
					sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
			}
		}
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" + type + count + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + type + count,
					"request" + type + count + ".txt", request, "");
			if(ptr != null)
			{
				generator.add(ptr);
				count++;
			}
		}
		else
		{
			if(type.compareTo("RCT") == 0)
				buildRCTRequests_override(rules, generator, t, effect, opt);
			else if(type.compareTo("RCF") == 0)
				buildRCFRequests_override(rules, generator, t, effect, opt);
		}
	}
	
	private PolicySpreadSheetTestRecord buildConditionRequest_unlessFalse(List<Rule> rules, Rule firstMatch, StringBuffer sb, TestPanel t, int count, int effect, String type)
	{
		PolicySpreadSheetTestRecord ptr = null;
		ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
		function f = new function();
		sb.append(TruePolicyTarget(policy, collector) + "\n");
		sb.append(TrueTarget_FalseCondition(firstMatch, collector) + "\n");
		int firstIndex = rules.indexOf(firstMatch);
		for(int i = 0; i < firstIndex; i++)
		{
			sb.append(FalseTarget_FalseCondition(rules.get(i), collector) + "\n");
		}
		
		for(int i = firstIndex + 1; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(r.getEffect() == effect)
				sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
		}
		
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" + type + count + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + type + count,
					"request" + type + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildRequest_unlessFalse(List<Rule> rules, Rule firstMatch, StringBuffer sb, TestPanel t,  int index, int count, int effect, String type)
	{
		PolicySpreadSheetTestRecord ptr = null;
		ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
		System.out.println("Found a match!");
		function f = new function();
		sb.append(TruePolicyTarget(policy, collector) + "\n");
		
		if(!firstMatch.isTargetEmpty())
			sb.append(False_Target((Target)firstMatch.getTarget(), collector) + "\n");
		if(!firstMatch.isConditionEmpty())
			sb.append(True_Condition(firstMatch.getCondition(), collector) + "\n");
		
		for(int i = 0; i < index; i++)
			sb.append(FalseTarget_FalseCondition(rules.get(i), collector) + "\n");
			
		for(int j = index + 1; j < rules.size(); j++)
		{
			Rule temp = rules.get(j);
			if(temp.getEffect() == effect)
				sb.append(FalseTarget_FalseCondition(temp, collector) + "\n");
		}
		//System.out.println("Z3-input: " + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" +type+ count + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bmw = new BufferedWriter(fw);
				bmw.write(request);
				bmw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + type + count,
					"request" + type + count + ".txt", request, "");
		}
		else
		{
			System.err.println("Z3 input does not meet boolean satisfiability requirements");
			System.err.println("Test could not be generated");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildRPTERequest_unless3(List<Rule> rules, Rule rule, StringBuffer sb, TestPanel t, int index, int count, int effect, int atindex, int alindex)
	{
		PolicySpreadSheetTestRecord ptr = null;
		ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
		function f = new function();
		sb.append(TruePolicyTarget(policy, collector) + "\n");
		sb.append(getTargetAttribute_NegateValue((Target)rule.getTarget(), alindex, atindex, collector) + "\n");
		sb.append(True_Condition(rule.getCondition(), collector) + "\n");
		
		for(int i = 0; i < index; i++)
			sb.append(False_Condition(rules.get(i).getCondition(), collector) + "\n");
		for(int i = index + 1; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(isDefaultRule(r))
				continue;
			if(r.getEffect() == effect)
				sb.append(False_Condition(r.getCondition(), collector) + "\n");
		}
		//System.out.println("Here is the z3-str input: \n" + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "requestRPTE" + count + ".txt";
				BufferedWriter bw = new BufferedWriter(new FileWriter(path));
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " RPTE" + count,
					"requestRPTE" + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildRPTERequest_unless1(List<Rule> rules, Rule rule, StringBuffer sb, TestPanel t, int index, int count, int effect, int anindex)
	{
		PolicySpreadSheetTestRecord ptr = null;
		ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
		function f = new function();
		sb.append(TruePolicyTarget(policy, collector) + "\n");
		sb.append(getTargetAnyOf_NegateValues((Target)rule.getTarget(), anindex, collector) + "\n");
		sb.append(True_Condition(rule.getCondition(), collector) + "\n");
		
		for(int i = 0; i < index; i++)
			sb.append(False_Condition(rules.get(i).getCondition(), collector) + "\n");
		for(int i = index + 1; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(isDefaultRule(r))
				continue;
			if(r.getEffect() == effect)
				sb.append(False_Condition(r.getCondition(), collector) + "\n");
		}
		//System.out.println("Here is the z3-str input: \n" + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "requestRPTE" + count + ".txt";
				BufferedWriter bw = new BufferedWriter(new FileWriter(path));
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " RPTE" + count,
					"requestRPTE" + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildRPTERequest_unless2(List<Rule> rules, Rule rule, StringBuffer sb, TestPanel t, int index, int count, int effect, int alindex, int anindex)
	{
		PolicySpreadSheetTestRecord ptr = null;
		ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
		function f = new function();
		sb.append(TruePolicyTarget(policy, collector) + "\n");
		sb.append(getTargetAllOf_NegateValues((Target)rule.getTarget(), anindex, alindex, collector) + "\n");
		sb.append(True_Condition(rule.getCondition(), collector) + "\n");
		
		for(int i = 0; i < index; i++)
			sb.append(False_Condition(rules.get(i).getCondition(), collector) + "\n");
		for(int i = index + 1; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(isDefaultRule(r))
				continue;
			if(r.getEffect() == effect)
				sb.append(False_Condition(r.getCondition(), collector) + "\n");
		}
		//System.out.println("Here is the z3-str input: \n" + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "requestRPTE" + count + ".txt";
				BufferedWriter bw = new BufferedWriter(new FileWriter(path));
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " RPTE" + count,
					"requestRPTE" + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private void buildRTTRequests_unless(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, int effect, boolean opt)
	{
		int count = 1, index = 0;
		for(Rule temp : rules)
		{
			if(rule_table[index][FALSE_TRUE] == true)
			{
				index++;
				continue;
			}
			StringBuffer sb = new StringBuffer();
			PolicySpreadSheetTestRecord ptr = null;
			if(temp.getEffect() == effect  && !temp.isTargetEmpty())
				ptr = buildRequest_unlessFalse(rules, temp, sb, t, index, count, effect, "RTT");
			if(ptr != null)
			{
				String request = ptr.getRequest();
				if(opt)
					checkForCoverage(rules, temp, request, index, FALSE_TRUE);
				generator.add(ptr);
				count++;
			}
			index++;
		}
	}
	
	private void buildRPTERequests_unless(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, int effect, boolean opt)
	{
		int count = 1, index = 0;
		for(Rule r : rules)
		{
			if(!r.isTargetEmpty())
			{
				if(r.getEffect() == effect)
				{
					Target rt = (Target)r.getTarget();
					List<AnyOfSelection> anyOf = rt.getAnyOfSelections();
					if(anyOf.size() != 0)
					{
						if(anyOf.size() > 1)
						{
							for(int i = 0; i < anyOf.size(); i++)
							{
								//if(anys[index][i] == true)
									//continue;
								StringBuffer sb = new StringBuffer();
								PolicySpreadSheetTestRecord ptr = null;
								ptr = buildRPTERequest_unless1(rules, r, sb, t, index, count, effect, i);
								if(ptr != null)
								{
									//if(opt)
										//checkAnyOfCoverage_unless(rules, ptr.getRequest(), index, i, effect, anys);
									generator.add(ptr);
									count++;
								}
							}
						}
						for(int k = 0; k < anyOf.size(); k++)
						{
							AnyOfSelection any = anyOf.get(k);
							List<AllOfSelection> allOf = any.getAllOfSelections();
							if(allOf.size() != 0)
							{
								if(allOf.size() > 1)
								{
									for(int i = 0; i < allOf.size(); i++)
									{
										//if(alls[index][i] == true)
											//continue;
										StringBuffer sb = new StringBuffer();
										PolicySpreadSheetTestRecord ptr = null;
										ptr = buildRPTERequest_unless2(rules, r, sb, t, index, count, effect, i, k);
										if(ptr != null)
										{
											//if(opt)
												//checkAllOfCoverage_unless(rules, ptr.getRequest(), index, i, effect, mats);
											generator.add(ptr);
											count++;
										}
									}
								}
								for(int j = 0; j < allOf.size(); j++)
								{
									AllOfSelection all = allOf.get(j);
									List<TargetMatch> matches = all.getMatches();
									if(matches.size() != 0)
									{
										if(matches.size() > 1)
										{
											for(int i = 0; i < matches.size(); i++)
											{
												//if(mats[index][i] == true)
													//continue;
												StringBuffer sb = new StringBuffer();
												PolicySpreadSheetTestRecord ptr = null;
												ptr = buildRPTERequest_unless3(rules, r, sb, t, index, count, effect, i, j);
												if(ptr != null)
												{
													//if(opt)
														//checkMatchCoverage_unless(rules, ptr.getRequest(), index, i, effect, mats);
													generator.add(ptr);
													count++;
												}
											}
										}
									}
								}
							}
						}
						index++;
						continue;
					}
					else
					{
						index++;
						continue;
					}
				}
				else
				{
					index++;
					continue;
				}
			}
			else
			{
				index++;
				continue;
			}
		}
	}
	
	private void buildRCTRequests_unless(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, int effect, boolean opt)
	{
		int count = 1;
		for(int i = 0; i < rules.size(); i++)
		{
			if(rule_table[i][TRUE_FALSE] == true)
				continue;
			Rule r = rules.get(i);
			StringBuffer sb = new StringBuffer();
			PolicySpreadSheetTestRecord ptr = null;
			if(r.getEffect() == effect && !r.isConditionEmpty())
				ptr = buildConditionRequest_unlessFalse(rules, r, sb, t, count, effect, "RCT");
			if(ptr != null)
			{
				if(opt)
					checkForCoverage(rules, r, ptr.getRequest(), i, TRUE_FALSE);
				generator.add(ptr);
				count++;
			}
		}
	}
	
	private void buildANFRequests_unless(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, int effect, boolean opt)
	{
		int count = 1;
		for(int i = 0; i < rules.size(); i++)
		{
			if(rule_table[i][TRUE_FALSE] == true)
				continue;
			Rule r = rules.get(i);
			StringBuffer sb = new StringBuffer();
			PolicySpreadSheetTestRecord ptr = null;
			if(r.getEffect() == effect && !r.isConditionEmpty())
				ptr = buildConditionRequest_unlessFalse(rules, r, sb, t, count, effect, "ANF");
			if(ptr != null)
			{
				if(opt)
					checkForCoverage(rules, r, ptr.getRequest(), i, TRUE_FALSE);
				generator.add(ptr);
				count++;
			}
		}
	}
	
	private void buildRTFRequests_unless(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, int effect, boolean opt)
	{
		int count = 1, tpos = TRUE_TRUE;
		boolean[] skip = new boolean[rules.size()];
		for(int i = 0; i < rules.size(); i++)
		{
			if(rule_table[i][tpos] == true)
				continue;
			Rule temp = rules.get(i);
			StringBuffer sb = new StringBuffer();
			PolicySpreadSheetTestRecord ptr = null;
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			if(temp.getEffect() == effect)
				ptr = buildRequest_true(rules, temp, sb, collector, count, t, rules.size(), "RTF");
			else
			{
				ptr = buildRequest_false(rules, temp, sb, collector, count, t, rules.size(), "RTF");
				tpos = FALSE_TRUE;
			}
			if(ptr != null)
			{
				if(opt)
					checkForCoverage(rules, temp, ptr.getRequest(), i, tpos);
				generator.add(ptr);
				count++;
			}
			tpos = TRUE_TRUE;
		}
	}
	
	private void buildRCFRequests_unless(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, int effect, boolean opt)
	{
		int count = 1;
		for(int i = 0; i < rules.size(); i++)
		{
			if(rule_table[i][TRUE_TRUE] == true)
				continue;
			Rule r = rules.get(i);
			StringBuffer sb = new StringBuffer();
			PolicySpreadSheetTestRecord ptr = null;
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			if((r.getEffect() == effect && !r.isConditionEmpty()) || !isDefaultRule(r))
				ptr = buildRequest_true(rules, r, sb, collector, count, t, rules.size(), "RCF");
			if(ptr != null)
			{
				if(opt)
					checkForCoverage(rules, r, ptr.getRequest(), i, TRUE_TRUE);
				generator.add(ptr);
				count++;
			}
		}
	}
	
	private void buildRNFRequests_unless(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, int effect, boolean opt)
	{
		int count = 1;
		for(int i = 0; i < rules.size(); i++)
		{
			if(rule_table[i][TRUE_TRUE] == true)
				continue;
			Rule r = rules.get(i);
			StringBuffer sb = new StringBuffer();
			PolicySpreadSheetTestRecord ptr = null;
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			if(r.getEffect() == effect && !r.isConditionEmpty() && containsNot(r))
				ptr = buildRequest_true(rules, r, sb, collector, count, t, rules.size(), "RNF");
			if(ptr != null)
			{
				if(opt)
					checkForCoverage(rules, r, ptr.getRequest(), i, TRUE_TRUE);
				generator.add(ptr);
				count++;
			}
		}
	}
	
	
	private void buildRTTRequests_FA(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, boolean opt)
	{
		if(rules.size() != 0)
		{
			int count = 1;
			for(int i = 0; i < rules.size(); i++)
			{
				if(rule_table[i][FALSE_TRUE] == true)
					continue;
				Rule temp = rules.get(i);
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				PolicySpreadSheetTestRecord ptr = null;
				sb.append(TruePolicyTarget(policy, collector) + "\n");
				if(!temp.isTargetEmpty())
					ptr = buildRequest_falseFA(rules, temp, sb, collector, count, t, "RTT");
				if(ptr != null)
				{
					if(opt)
						checkForCoverage(rules, temp, ptr.getRequest(), i, FALSE_TRUE);
					generator.add(ptr);
					count++;
				}
				
			}
		}
		else
			System.err.println("Must have at least rule to generate a test");
	}
	
	private void buildRPTERequests_FA(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, boolean opt)
	{
		int count = 1, index = 0;
		
		for(Rule r : rules)
		{
			if(!r.isTargetEmpty())
			{
				Target rt = (Target)r.getTarget();
				List<AnyOfSelection> anyOf = rt.getAnyOfSelections();
				if(anyOf.size() != 0)
				{
					if(anyOf.size() > 1)
					{
						for(int i = 0; i < anyOf.size(); i++)
						{
							//if(anys[index][i] == true)
								//continue;
							ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
							PolicySpreadSheetTestRecord ptr = null;
							StringBuffer sb = new StringBuffer();
							sb.append(TruePolicyTarget(policy, collector) + "\n");
							ptr = buildRPTERequest_FA1(rules, r, sb, collector, count, t, i);
							if(ptr != null)
							{
								//if(opt)
									//checkAnyOfCoverage_fa(rules, ptr.getRequest(), index, i, r.getEffect(), anys);
								generator.add(ptr);
								count++;
							}
						}
					}
					for(int k = 0; k < anyOf.size(); k++)
					{
						AnyOfSelection any = anyOf.get(k);
						List<AllOfSelection> allOf = any.getAllOfSelections();
						if(allOf.size() != 0)
						{
							if(allOf.size() > 1)
							{
								for(int i = 0; i < allOf.size(); i++)
								{
									//if(alls[index][i] == true)
										//continue;
									ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
									PolicySpreadSheetTestRecord ptr = null;
									StringBuffer sb = new StringBuffer();
									sb.append(TruePolicyTarget(policy, collector) + "\n");
									ptr = buildRPTERequest_FA2(rules, r, sb, collector, count, t, i, k);
									if(ptr != null)
									{
										//if(opt)
											//checkAllOfCoverage_fa(rules, ptr.getRequest(), index, i, r.getEffect(), alls);
										generator.add(ptr);
										count++;
									}
								}
							}
							for(int j = 0; j < allOf.size(); j++)
							{
								AllOfSelection all = allOf.get(j);
								List<TargetMatch> matches = all.getMatches();
								for(int i = 0; i < matches.size(); i++)
								{
									//if(mats[index][i] == true)
										//continue;
									ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
									PolicySpreadSheetTestRecord ptr = null;
									StringBuffer sb = new StringBuffer();
									sb.append(TruePolicyTarget(policy, collector) + "\n");
									ptr = buildRPTERequest_FA3(rules, r, sb, collector, count, t, i, j);
									if(ptr != null)
									{
										//if(opt)
											//checkMatchCoverage_fa(rules, ptr.getRequest(), index, i, r.getEffect(), mats);
										generator.add(ptr);
										count++;
									}
								}
							}
						}
					}
					index++;
					continue;
				}
				else
				{
					index++;
					continue;
				}
			}
			else
			{
				index++;
				continue;
			}
		}
	}
	
	private void buildRCTRequests_FA(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, boolean opt)
	{
		int count = 1;
		for(int i = 0; i < rules.size(); i++)
		{
			if(rule_table[i][TRUE_FALSE] == true)
				continue;
			Rule r = rules.get(i);
			StringBuffer sb = new StringBuffer();
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			PolicySpreadSheetTestRecord ptr = null;
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			if(!r.isConditionEmpty())
				ptr = buildConditionRequest_falseFA(rules, r, sb, collector, count, t, "RCT");
			if(ptr != null)
			{
				if(opt)
					checkForCoverage(rules, r, ptr.getRequest(), i, TRUE_FALSE);
				generator.add(ptr);
				count++;
			}
		}
	}
	
	private void buildANFRequests_FA(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, boolean opt)
	{
		int count = 1;
		for(int i = 0; i < rules.size(); i++)
		{
			if(rule_table[i][TRUE_FALSE] == true)
				continue;
			Rule r = rules.get(i);
			StringBuffer sb = new StringBuffer();
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			PolicySpreadSheetTestRecord ptr = null;
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			if(!r.isConditionEmpty())
				ptr = buildConditionRequest_falseFA(rules, r, sb, collector, count, t, "ANF");
			if(ptr != null)
			{
				if(opt)
					checkForCoverage(rules, r, ptr.getRequest(), i, TRUE_FALSE);
				generator.add(ptr);
				count++;
			}
		}
	}
	
	private void buildRCFRequests_FA(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, boolean opt)
	{
		int count = 1;
		for(int i = 0; i < rules.size(); i++)
		{
			if(rule_table[i][TRUE_TRUE] == true)
				continue;
			Rule r = rules.get(i);
			StringBuffer sb = new StringBuffer();
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			PolicySpreadSheetTestRecord ptr = null;
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			ptr = buildConditionRequest_trueFA(rules, r, sb, collector, count, t, "RCF");
			if(ptr != null)
			{
				if(opt)
					checkForCoverage(rules, r, ptr.getRequest(), i, TRUE_TRUE);
				generator.add(ptr);
				count++;
			}
		}
	}
	
	private void buildRNFRequests_FA(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, boolean opt)
	{
		int count = 1;
		for(int i = 0; i < rules.size(); i++)
		{
			if(rule_table[i][TRUE_TRUE] == true)
				continue;
			Rule r = rules.get(i);
			StringBuffer sb = new StringBuffer();
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			PolicySpreadSheetTestRecord ptr = null;
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			if(!r.isConditionEmpty() && containsNot(r))
				ptr = buildConditionRequest_trueFA(rules, r, sb, collector, count, t, "RNF");
			if(ptr != null)
			{
				if(opt)
					checkForCoverage(rules, r, ptr.getRequest(), i, TRUE_TRUE);
				generator.add(ptr);
				count++;
			}
		}
	}
	
	private void buildRTFRequests_FA(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, boolean opt)
	{
		if(rules.size() != 0)
		{
			int count = 1;
			for(int i = 0; i < rules.size(); i++)
			{
				if(rule_table[i][TRUE_TRUE] == true)
					continue;
				Rule temp = rules.get(i);
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				PolicySpreadSheetTestRecord ptr = null;
				sb.append(TruePolicyTarget(policy, collector) + "\n");
				ptr = buildRequest_trueFA(rules, temp, sb, collector, count, t, "RTF");
				if(ptr != null)
				{
					if(opt)
						checkForCoverage(rules, temp, ptr.getRequest(), i, TRUE_TRUE);
					generator.add(ptr);
					count++;
				}
				
			}
		}
		else
			System.err.println("Must have at least rule to generate a test");
	}
	
	private void build_DefaultRTTRequests_FA(List<Rule> rules, Rule defaultRule, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, boolean opt)
	{
		if(rules.size() != 0)
		{
			int count = 1;
			int effect = defaultRule.getEffect();
			function checker = new function();
			if(checker.allDenyRule(policy) || checker.allPermitRule(policy))
			{
				build_allOne_RequestsFalse(rules, generator, t, "RTT", opt);
				checker = null;
			}
			else
			{
				for(int i = 0; i < rules.size(); i++)
				{
					if(rule_table[i][FALSE_TRUE] == true)
						continue;
					Rule r = rules.get(i);
					StringBuffer sb = new StringBuffer();
					ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
					PolicySpreadSheetTestRecord ptr = null;
					sb.append(TruePolicyTarget(policy, collector) + "\n");
					if(r.getEffect() == effect && !r.isTargetEmpty())
						ptr = buildRequest_Allfalse(rules, r, sb, collector, count, t, rules.size(), "RTT", effect);
					else if(!r.isTargetEmpty())
						ptr = buildRequest_false(rules, r, sb, collector, count, t, rules.size(), "RTT");
					if(ptr != null)
					{
						String request = ptr.getRequest();
						if(opt)
							checkForCoverage(rules, r, request, i, FALSE_TRUE);
						generator.add(ptr);
						count++;
					}
				}
			}
		}
	}
	
	private void buildDefaultRPTERequests(List<Rule> rules, Rule defRule, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, boolean opt)
	{
		function checker = new function();
		if(checker.allDenyRule(policy) || checker.allPermitRule(policy))
			buildRPTERequests_override(rules, generator, t, opt);
		else
		{
			int count = 1, index = 0;
			int effect = defRule.getEffect();
			for(Rule r : rules)
			{
				if(!r.isTargetEmpty())
				{
					if(r.getEffect() == effect)
					{
						Target rt = (Target)r.getTarget();
						List<AnyOfSelection> anyOf = rt.getAnyOfSelections();
						if(anyOf.size() != 0)
						{
							if(anyOf.size() > 1)
							{
								for(int i = 0; i < anyOf.size(); i++)
								{
									//if(anys[index][i] == true)
										//continue;
									ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
									PolicySpreadSheetTestRecord ptr = null;
									StringBuffer sb = new StringBuffer();
									sb.append(TruePolicyTarget(policy, collector) + "\n");
									ptr = buildRPTERequest_AllFalse1(rules, r, sb, collector, count, t, effect, i);
									if(ptr != null)
									{
										//if(opt)
											//checkAnyOfCoverage_fa(rules, ptr.getRequest(), index, i, effect, anys);
										generator.add(ptr);
										count++;
									}
								}
							}
							for(int i = 0; i < anyOf.size(); i++)
							{
								AnyOfSelection any = anyOf.get(i);
								List<AllOfSelection> allOf = any.getAllOfSelections();
								if(allOf.size() != 0)
								{
									if(allOf.size() > 1)
									{
										for(int j = 0; j < allOf.size(); j++)
										{
											//if(alls[index][j] == true)
												//continue;
											ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
											PolicySpreadSheetTestRecord ptr = null;
											StringBuffer sb = new StringBuffer();
											sb.append(TruePolicyTarget(policy, collector) + "\n");
											ptr = buildRPTERequest_AllFalse2(rules, r, sb, collector, count, t, effect, j, i);
											if(ptr != null)
											{
												//if(opt)
													//checkAllOfCoverage_fa(rules, ptr.getRequest(), index, j, effect, alls);
												generator.add(ptr);
												count++;
											}
										}
									}
									for(int j = 0; j < allOf.size(); j++)
									{
										AllOfSelection all = allOf.get(j);
										List<TargetMatch> matches = all.getMatches();
										if(matches.size() != 0)
										{
											if(matches.size() > 1)
											{
												for(int k = 0; k < matches.size(); k++)
												{
													//if(mats[index][k] == true)
														//continue;
													ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
													PolicySpreadSheetTestRecord ptr = null;
													StringBuffer sb = new StringBuffer();
													sb.append(TruePolicyTarget(policy, collector) + "\n");
													ptr = buildRPTERequest_AllFalse3(rules, r, sb, collector, count, t, effect, k, j);
													if(ptr != null)
													{
														//if(opt)
															//checkMatchCoverage_fa(rules, ptr.getRequest(), index, k, effect, mats);
														generator.add(ptr);
														count++;
													}
												}
											}
										}
									}
								}
							}
							index++;
							continue;
						}
						else
						{
							index++;
							continue;
						}
					}
					else
					{
						Target rt = (Target)r.getTarget();
						List<AnyOfSelection> anyOf = rt.getAnyOfSelections();
						if(anyOf.size() != 0)
						{
							if(anyOf.size() > 1)
							{
								for(int i = 0; i < anyOf.size(); i++)
								{
									//if(anys[index][i] == true)
										//continue;
									ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
									PolicySpreadSheetTestRecord ptr = null;
									StringBuffer sb = new StringBuffer();
									sb.append(TruePolicyTarget(policy, collector) + "\n");
									ptr = buildRPTERequest1(rules, r, sb, collector, count, t, i);
									if(ptr != null)
									{
										//if(opt)
											//checkAnyOfCoverage_fa(rules, ptr.getRequest(), index, i, r.getEffect(), anys);
										generator.add(ptr);
										count++;
									}
								}
							}
							for(int i = 0; i < anyOf.size(); i++)
							{
								AnyOfSelection any = anyOf.get(i);
								List<AllOfSelection> allOf = any.getAllOfSelections();
								if(allOf.size() != 0)
								{
									if(anyOf.size() > 1)
									{
										for(int j = 0; j < allOf.size(); j++)
										{
											//if(alls[index][j] == true)
												//continue;
											ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
											PolicySpreadSheetTestRecord ptr = null;
											StringBuffer sb = new StringBuffer();
											sb.append(TruePolicyTarget(policy, collector) + "\n");
											ptr = buildRPTERequest2(rules, r, sb, collector, count, t, j, i);
											if(ptr != null)
											{
												//if(opt)
													//checkAllOfCoverage_fa(rules, ptr.getRequest(), index, j, r.getEffect(), alls);
												generator.add(ptr);
												count++;
											}
										}
									}
									for(int j = 0; j < allOf.size(); j++)
									{
										AllOfSelection all = allOf.get(j);
										List<TargetMatch> matches = all.getMatches();
										if(matches.size() != 0)
										{
											if(matches.size() > 1)
											{
												for(int k = 0; k < matches.size(); k++)
												{
													//if(mats[index][k] == true)
														//continue;
													ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
													PolicySpreadSheetTestRecord ptr = null;
													StringBuffer sb = new StringBuffer();
													sb.append(TruePolicyTarget(policy, collector) + "\n");
													ptr = buildRPTERequest3(rules, r, sb, collector, count, t, k, j);
													if(ptr != null)
													{
														//if(opt)
															//checkMatchCoverage_fa(rules, ptr.getRequest(), index, k, r.getEffect(), mats);
														generator.add(ptr);
														count++;
													}
												}
											}
										}
									}
								}
							}
							index++;
							continue;
						}
						else
						{
							index++;
							continue;
						}
					}
				}
				else
				{
					index++;
					continue;
				}
			}
		}
	}

	private void build_DefaultRCTRequests_FA(List<Rule> rules, Rule defaultRule, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, boolean opt)
	{
		int count = 1;
		int effect = defaultRule.getEffect();
		function checker = new function();
		boolean[] skip = new boolean[rules.size()];
		if(checker.allDenyRule(policy) || checker.allPermitRule(policy))
		{
			build_AllOne_ConditionRequestsFalse(rules, generator, t, "RCT", opt);
			checker = null;
		}
		else
		{
			for(int i = 0; i < rules.size(); i++)
			{
				if(rule_table[i][TRUE_FALSE] == true)
					continue;
				Rule r = rules.get(i);
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				PolicySpreadSheetTestRecord ptr = null;
				sb.append(TruePolicyTarget(policy, collector) + "\n");
				if(r.getEffect() == effect && !r.isConditionEmpty())
					ptr = buildConditionRequest_AllFalse(rules, r, sb, collector, count, t, rules.size(), "RCT", effect);
				else if(r.getEffect() != effect && !r.isConditionEmpty())
					ptr = buildConditionRequest_false(rules, r, sb, collector, count, t, rules.size(), "RCT");
				if(ptr != null)
				{
					if(opt)
						checkForCoverage(rules, r, ptr.getRequest(), i, TRUE_FALSE);
					generator.add(ptr);
					count++;
				}
			}
		}
	}
	
	private void buildANFRequests_default(List<Rule> rules, Rule defaultRule, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, boolean opt)
	{
		function checker = new function();
		if(checker.allDenyRule(policy) || checker.allPermitRule(policy))
			build_AllOne_ConditionRequestsFalse(rules, generator, t, "ANF", opt);
		else
		{
			int count = 1, effect = defaultRule.getEffect();
			for(int i = 0; i < rules.size(); i++)
			{
				if(rule_table[i][TRUE_FALSE] == true)
					continue;
				Rule r = rules.get(i);
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				PolicySpreadSheetTestRecord ptr = null;
				sb.append(TruePolicyTarget(policy, collector) + "\n");
				if(r.getEffect() == effect && !r.isConditionEmpty())
					ptr = buildConditionRequest_AllFalse(rules, r, sb, collector, count, t, rules.size(), "ANF", effect);
				else if(r.getEffect() != effect && !r.isConditionEmpty())
					ptr = buildConditionRequest_false(rules, r, sb, collector, count, t, rules.size(), "ANF");
				if(ptr != null)
				{
					if(opt)
						checkForCoverage(rules, r, ptr.getRequest(), i, TRUE_FALSE);
					generator.add(ptr);
					count++;
				}
			}
		}
	}
	
	private void build_DefaultRCFRequests_FA(List<Rule> rules, Rule defaultRule, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, boolean opt)
	{
		int count = 1;
		int effect = defaultRule.getEffect();
		function checker = new function();
		boolean[] skip = new boolean[rules.size()];
		if(checker.allDenyRule(policy) || checker.allPermitRule(policy))
			build_AllOne_ConditionRequestsTrue(rules, generator, t, "RCF", opt);
		else
		{
			int tpos = TRUE_TRUE;
			for(int i = 0; i < rules.size(); i++)
			{
				if(rule_table[i][tpos] == true)
					continue;
				Rule r = rules.get(i);
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				PolicySpreadSheetTestRecord ptr = null;
				sb.append(TruePolicyTarget(policy, collector) + "\n");
				if(r.getEffect() == effect && !r.isConditionEmpty())
					ptr = buildConditionRequest_true(rules, r, sb, collector, count, t, rules.size(), "RCF");
				else if(r.getEffect() != effect && !r.isConditionEmpty())
				{
					ptr = buildConditionRequest_AllFalse(rules, r, sb, collector, count, t, rules.size(), "RCF", effect);
					tpos = FALSE_TRUE;
				}
				else
				{
					ptr = buildConditionRequest_AllFalse(rules, r, sb, collector, count, t, rules.size(), "RCF", effect);
					tpos = FALSE_TRUE;
				}
				if(ptr != null)
				{
					if(opt)
						checkForCoverage(rules, r, ptr.getRequest(), i, tpos);
					generator.add(ptr);
					count++;
				}
				tpos = TRUE_TRUE;
			}
		}
	}
	
	private void buildRNFRequests_default(List<Rule> rules, Rule defRule, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, boolean opt)
	{
		function checker = new function();
		if(checker.allDenyRule(policy) || checker.allPermitRule(policy))
			build_AllOne_ConditionRequestsTrue(rules, generator, t, "RNF", opt);
		else
		{
			int count = 1, tpos = TRUE_TRUE;
			int effect = defRule.getEffect();
			for(int i = 0; i < rules.size(); i++)
			{
				if(rule_table[i][tpos] == true)
					continue;
				Rule r = rules.get(i);
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				PolicySpreadSheetTestRecord ptr = null;
				if(r.getEffect() == effect && !r.isConditionEmpty() && containsNot(r))
					ptr = buildConditionRequest_true(rules, r, sb, collector, count, t, rules.size(), "RNF");
				else if(r.getEffect() != effect && !r.isConditionEmpty() && containsNot(r))
				{
					tpos = TRUE_FALSE;
					ptr = buildConditionRequest_AllFalse(rules, r, sb, collector, count, t, rules.size(), "RNF", effect);
				}
				if(ptr != null)
				{
					if(opt)
						checkForCoverage(rules, r, ptr.getRequest(), i, tpos);
					generator.add(ptr);
					count++;
				}
				tpos = TRUE_TRUE;
			}
		}
	}
	
	private void build_DefaultRTFRequests_FA(List<Rule> rules, Rule defaultRule, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, boolean opt)
	{
		if(rules.size() != 0)
		{
			int count = 1;
			int effect = defaultRule.getEffect();
			function checker = new function();
			if(checker.allDenyRule(policy) || checker.allPermitRule(policy))
			{
				build_allOne_RequestsTrue(rules, generator, t, "RTF", opt);
				checker = null;
			}
			else
			{
				int tpos = TRUE_TRUE;
				for(int i = 0; i < rules.size(); i++)
				{
					if(rule_table[i][tpos] == true)
						continue;
					Rule r = rules.get(i);
					StringBuffer sb = new StringBuffer();
					ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
					PolicySpreadSheetTestRecord ptr = null;
					sb.append(TruePolicyTarget(policy, collector) + "\n");
					if(r.getEffect() == effect)
						ptr = buildRequest_true(rules, r, sb, collector, count, t, rules.size(), "RTF");
					else
					{
						ptr = buildRequest_Allfalse(rules, r, sb, collector, count, t, rules.size(), "RTF", effect);
						tpos = FALSE_TRUE;
					}
					if(ptr != null)
					{
						if(opt)
							checkForCoverage(rules, r, ptr.getRequest(), i, tpos);
						generator.add(ptr);
						count++;
					}
					tpos = TRUE_TRUE;
				}
			}
		}
	}
	
	private void build_allOne_RequestsFalse(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, String type, boolean opt)
	{
		if(rules.size() != 0)
		{
			int count = 1;
			for(int i = 0; i < rules.size(); i++)
			{
				if(rule_table[i][FALSE_TRUE] == true)
					continue;
				Rule r = rules.get(i);
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				PolicySpreadSheetTestRecord ptr = null;
				sb.append(TruePolicyTarget(policy, collector) + "\n");
				if(!r.isTargetEmpty())
					ptr = buildRequest_false(rules, r, sb, collector, count, t, rules.size(), type);
				if(ptr != null)
				{
					String request = ptr.getRequest();
					if(opt)
						checkForCoverage(rules, r, request, i, FALSE_TRUE);
					generator.add(ptr);
					count++;
				}
			}
		}
	}
	
	private void build_AllOne_ConditionRequestsFalse(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, String type, boolean opt)
	{
		int count = 1;

		for(int i = 0; i < rules.size(); i++)
		{
			if(rule_table[i][TRUE_FALSE] == true)
				continue;
			Rule r = rules.get(i);
			StringBuffer sb = new StringBuffer();
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			PolicySpreadSheetTestRecord ptr = null;
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			if(!r.isConditionEmpty())
				ptr = buildConditionRequest_false(rules, r, sb, collector, count, t, rules.size(), type);
			if(ptr != null)
			{
				if(opt)
					checkForCoverage(rules, r, ptr.getRequest(), i, TRUE_FALSE);
				generator.add(ptr);
				count++;
			}
		}
	}
	
	private void build_AllOne_ConditionRequestsTrue(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, String type, boolean opt)
	{
		int count = 1;
		boolean[] skip = new boolean[rules.size()];
		for(int i = 0; i < rules.size(); i++)
		{
			if(rule_table[i][TRUE_TRUE] == true)
				continue;
			Rule r = rules.get(i);
			if(isDefaultRule(r))
				continue;
			StringBuffer sb = new StringBuffer();
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			PolicySpreadSheetTestRecord ptr = null;
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			if(type.compareTo("RNF") == 0)
			{
				if(!r.isConditionEmpty() && containsNot(r))
					ptr = buildRequest_true(rules, r, sb, collector, count, t, rules.size(), type);
				else
					continue;
			}
			else
				ptr = buildRequest_true(rules, r, sb, collector, count, t, rules.size(), type);
			if(ptr != null)
			{
				if(opt)
					checkForCoverage(rules, r, ptr.getRequest(), i, TRUE_TRUE);
				generator.add(ptr);
				count++;
			}
		}
	}
	
	private void build_allOne_RequestsTrue(List<Rule> rules, ArrayList<PolicySpreadSheetTestRecord> generator, TestPanel t, String type, boolean opt)
	{
		if(rules.size() != 0)
		{
			int count = 1;
			boolean[] skip = new boolean[rules.size()];
			for(int i = 0; i < rules.size(); i++)
			{
				if(rule_table[i][TRUE_TRUE] == true)
					continue;
				Rule r = rules.get(i);
				StringBuffer sb = new StringBuffer();
				ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
				PolicySpreadSheetTestRecord ptr = null;
				sb.append(TruePolicyTarget(policy, collector) + "\n");
				ptr = buildRequest_true(rules, r, sb, collector, count, t, rules.size(), type);
				if(ptr != null)
				{
					if(opt)
						checkForCoverage(rules, r, ptr.getRequest(), i, TRUE_TRUE);
					generator.add(ptr);
					count++;
				}
			}
		}
	}
	
	private PolicySpreadSheetTestRecord buildRequest_falseFA(List<Rule> rules, Rule current, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, String type)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		sb.append(False_Target((Target)current.getTarget(), collector) + "\n");
		sb.append(True_Condition(current.getCondition(), collector) + "\n");
		int currentIndex = rules.indexOf(current);
		int firstDifferent = -1;
		for(int i = 0; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(r.getId().equals(current.getId()) || isDefaultRule(r))
				continue;
			else if(i > currentIndex)
			{
				if(r.getEffect() == current.getEffect())
					sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
				else if(firstDifferent < 0 && r.getEffect() != current.getEffect())
					firstDifferent = i;
				else
					continue;
			}
			else
				sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
		}
		if(firstDifferent > 0)
		{
			sb.append(TrueTarget_TrueCondition(rules.get(firstDifferent), collector) + "\n");
		}
		//System.out.println("Z3-input: " + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" + type + count + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + type + count,
					"request" + type + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildRPTERequest_FA3(List<Rule> rules, Rule current, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, int atindex, int alindex)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		sb.append(getTargetAttribute_NegateValue((Target)current.getTarget(), alindex, atindex, collector) + "\n");
		sb.append(True_Condition(current.getCondition(), collector) + "\n");
		int cur = rules.indexOf(current);
		Rule diff = null;
		for(int i = 0; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(r.getId().equals(current.getId()) || isDefaultRule(r))
				continue;
			if(i > cur)
			{
				if(r.getEffect() ==  current.getEffect())
					sb.append(False_Condition(r.getCondition(), collector) + "\n");
				else if(diff == null && r.getEffect() != current.getEffect())
					diff = r;
				else
					continue;
			}
			else
				sb.append(False_Condition(r.getCondition(), collector) + "\n");
		}
		if(diff != null)
		{
			sb.append(TrueTarget_TrueCondition(diff, collector) + "\n");
		}
		//System.out.println("Z3-input: " + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "requestRPTE" + count + ".txt";
				BufferedWriter bw = new BufferedWriter(new FileWriter(path));
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " RPTE" + count,
					"requestRPTE" + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildRPTERequest_FA2(List<Rule> rules, Rule current, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, int alindex, int anindex)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		sb.append(getTargetAllOf_NegateValues((Target)current.getTarget(), anindex, alindex, collector) + "\n");
		sb.append(True_Condition(current.getCondition(), collector) + "\n");
		int cur = rules.indexOf(current);
		Rule diff = null;
		for(int i = 0; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(r.getId().equals(current.getId()) || isDefaultRule(r))
				continue;
			if(i > cur)
			{
				if(r.getEffect() ==  current.getEffect())
					sb.append(False_Condition(r.getCondition(), collector) + "\n");
				else if(diff == null && r.getEffect() != current.getEffect())
					diff = r;
				else
					continue;
			}
			else
				sb.append(False_Condition(r.getCondition(), collector) + "\n");
		}
		if(diff != null)
		{
			sb.append(TrueTarget_TrueCondition(diff, collector) + "\n");
		}
		//System.out.println("Z3-input: " + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "requestRPTE" + count + ".txt";
				BufferedWriter bw = new BufferedWriter(new FileWriter(path));
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " RPTE" + count,
					"requestRPTE" + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildRPTERequest_FA1(List<Rule> rules, Rule current, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, int anindex)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		sb.append(getTargetAnyOf_NegateValues((Target)current.getTarget(), anindex, collector) + "\n");
		sb.append(True_Condition(current.getCondition(), collector) + "\n");
		int cur = rules.indexOf(current);
		Rule diff = null;
		for(int i = 0; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(r.getId().equals(current.getId()) || isDefaultRule(r))
				continue;
			if(i > cur)
			{
				if(r.getEffect() ==  current.getEffect())
					sb.append(False_Condition(r.getCondition(), collector) + "\n");
				else if(diff == null && r.getEffect() != current.getEffect())
					diff = r;
				else
					continue;
			}
			else
				sb.append(False_Condition(r.getCondition(), collector) + "\n");
		}
		if(diff != null)
		{
			sb.append(TrueTarget_TrueCondition(diff, collector) + "\n");
		}
		//System.out.println("Z3-input: " + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "requestRPTE" + count + ".txt";
				BufferedWriter bw = new BufferedWriter(new FileWriter(path));
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " RPTE" + count,
					"requestRPTE" + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildConditionRequest_falseFA(List<Rule> rules, Rule current, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, String type)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		sb.append(TrueTarget_FalseCondition(current, collector) + "\n");
		int currentIndex = rules.indexOf(current);
		int diffIndex = -1;
		for(int i = 0; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(r.getId().equals(current.getId()) || isDefaultRule(r))
				continue;
			else if(i > currentIndex)
			{
				if(r.getEffect() == current.getEffect())
					sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
				else if(diffIndex < 0 && r.getEffect() != current.getEffect() && !r.isConditionEmpty())
					diffIndex = i;
				else
					continue;
			}
			else
				sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
		}
		if(diffIndex > 0)
			sb.append(TrueTarget_TrueCondition(rules.get(diffIndex), collector) + "\n");
		//System.out.println("Z3: " + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" + type + count + ".txt";
				BufferedWriter bw = new BufferedWriter(new FileWriter(path));
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + type + count,
					"request" + type + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildConditionRequest_trueFA(List<Rule> rules, Rule current, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, String type)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		sb.append(TrueTarget_TrueCondition(current, collector) + "\n");
		int currentIndex = rules.indexOf(current);
		int diffIndex = -1;
		for(int i = 0; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(r.getId().equals(current.getId()) || isDefaultRule(r))
				continue;
			else if(i > currentIndex)
			{
				if(r.getEffect() == current.getEffect())
					sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
				else if(diffIndex < 0 && r.getEffect() != current.getEffect() && !r.isConditionEmpty())
					diffIndex = i;
				else
					continue;
			}
			else
				sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
		}
		if(diffIndex > 0)
			sb.append(TrueTarget_TrueCondition(rules.get(diffIndex), collector) + "\n");
		//System.out.println("Z3: " + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" + type + count + ".txt";
				BufferedWriter bw = new BufferedWriter(new FileWriter(path));
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + type + count,
					"request" + type + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private PolicySpreadSheetTestRecord buildRequest_trueFA(List<Rule> rules, Rule current, StringBuffer sb, ArrayList<MyAttr> collector, int count, TestPanel t, String type)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		sb.append(TrueTarget_TrueCondition(current, collector) + "\n");
		int currentIndex = rules.indexOf(current);
		int firstDifferent = -1;
		for(int i = 0; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(r.getId().equals(current.getId()) || isDefaultRule(r))
				continue;
			else if(i > currentIndex)
			{
				if(r.getEffect() == current.getEffect())
					sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
				else if(firstDifferent < 0 && r.getEffect() != current.getEffect())
					firstDifferent = i;
				else
					continue;
			}
			else
				sb.append(FalseTarget_FalseCondition(r, collector) + "\n");
		}
		if(firstDifferent > 0)
		{
			sb.append(TrueTarget_TrueCondition(rules.get(firstDifferent), collector) + "\n");
		}
		//System.out.println("Z3-input: " + sb.toString());
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		if(sat)
		{
			System.out.println(nameMap.size() + " map size");
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			System.out.println(collector.size() + " collection size");
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" + type + count + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + type + count,
					"request" + type + count + ".txt", request, "");
		}
		return ptr;
	}
	
	private boolean atLeastOneCondition(List<Rule> rules)
	{
		for(Rule r : rules)
		{
			if(r.isConditionEmpty())
				return false;
		}
		return true;
	}
	
	private void findDuplicateAttributeValues(ArrayList<String> values, Set<String> duplicates)
	{
		Set<String> bag = new HashSet<String>();
		for(String s : values)
		{
			if(bag.contains(s))
				duplicates.add(s);
			else
				bag.add(s);
		}
	}
	
	private boolean containsNot(Rule r)
	{
		if(r.isConditionEmpty())
			return false;
		Expression e = r.getCondition().getExpression();
		ArrayList<MyAttr> local = new ArrayList<MyAttr>();
		if(e instanceof Apply)
		{
			Apply app = (Apply)e;
			if(app.encode().contains("urn:oasis:names:tc:xacml:1.0:function:not"))
				return true;
		}
		return false;
	}
	
	private void updateRequestList(ArrayList<PolicySpreadSheetTestRecord> generator, String request, int stop)
	{
		List<Rule> rules = getRuleFromPolicy(policy);
		Rule cur = rules.get(stop);
		for(int i = 0; i < stop; i++)
		{
			Rule rule = rules.get(i);
			if(isDefaultRule(rule))
				continue;
			else
			{
				if(RuleEvaluate(rule, request) == RuleEvaluate(cur, request))
					generator.remove(i);
			}
		}
	}
	
	private void checkForCoverage(List<Rule> rules, Rule rule, String request, int start, int table_pos)
	{
		int rt = -1;
		int rc = -1;
		if(rule.getTarget() != null)
			rt = TargetEvaluate((Target)rule.getTarget(), request);
		if(rule.getCondition() != null)
			rc = ConditionEvaluate(rule.getCondition(), request);
		
		if(rt >= 0 || rc >= 0)
			rule_table[start][table_pos] = true;
		for(int i = start + 1; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			int tres = -1;
			int cres = -1;
			if(r.getTarget() != null)
				tres = TargetEvaluate((Target)r.getTarget(), request);
			if(r.getCondition() != null)
				cres = ConditionEvaluate(r.getCondition(), request);
			
			if(tres == rt && rc == cres)
				rule_table[i][table_pos] = true;
		}
	}
	
	private void checkAnyOfCoverage_override(List<Rule> rules, List<AnyOfSelection> anyOf, Rule rule, String request, int start, boolean[] skip)
	{
		int rt = -1;//assume no target
		int rc = -1;//assume no condition
		if(rule.getTarget() != null)
			rt = TargetEvaluate((Target)rule.getTarget(), request);
		if(rule.getCondition() != null)
			rc = ConditionEvaluate(rule.getCondition(), request);
		for(int i = start + 1; i < anyOf.size(); i++)
		{
			function f = new function();
			StringBuffer sb = new StringBuffer();
			ArrayList<MyAttr> local = new ArrayList<MyAttr>();
			sb.append(TruePolicyTarget(policy, local) + "\n");
			sb.append(getTargetAnyOf_NegateValues((Target)rule.getTarget(), i, local) + "\n");
			sb.append(True_Condition(rule.getCondition(), local) + "\n");
			for(Rule r : rules)
			{
				if(r.getId().equals(rule.getId()) || isDefaultRule(r))
					continue;
				else
					sb.append(False_Condition(r.getCondition(), local) + "\n");
			}
			boolean sat = z3str(sb.toString(), nameMap, typeMap);
			if(sat)
			{
				try
				{
					z3.getValue(local, nameMap);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				String request2 = f.print(local);
				int tres = -1;
				int cres = -1;
				if(rule.getTarget() != null)
					tres = TargetEvaluate((Target)rule.getTarget(), request2);
				if(rule.getCondition() != null)
					cres = ConditionEvaluate(rule.getCondition(), request2);
				if(tres >= 0)
					if(tres == rt && cres == rc)
						skip[i] = true;
			}
		}
	}
	
	private void checkAllOfCoverage_override(List<Rule> rules, List<AllOfSelection> allOf, Rule rule, String request, int anyindex, int index, boolean[] skip)
	{
		int rt = -1;
		int rc = -1;
		if(rule.getTarget() != null)
			rt = TargetEvaluate((Target)rule.getTarget(), request);
		if(rule.getCondition() != null)
			rc = ConditionEvaluate(rule.getCondition(), request);
		for(int i = index + 1; i < allOf.size(); i++)
		{
			function f = new function();
			StringBuffer sb = new StringBuffer();
			ArrayList<MyAttr> local = new ArrayList<MyAttr>();
			sb.append(TruePolicyTarget(policy, local) + "\n");
			sb.append(getTargetAllOf_NegateValues((Target)rule.getTarget(), anyindex, i, local) + "\n");
			sb.append(True_Condition(rule.getCondition(), local) + "\n");
			for(Rule r : rules)
			{
				if(r.getId().equals(rule.getId()) || isDefaultRule(r))
					continue;
				else
					sb.append(False_Condition(r.getCondition(), local) + "\n");
			}
			boolean sat = z3str(sb.toString(), nameMap, typeMap);
			if(sat)
			{
				try
				{
					z3.getValue(local, nameMap);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				String request2 = f.print(local);
				int tres = -1;
				int cres = -1;
				if(rule.getTarget() != null)
					tres = TargetEvaluate((Target)rule.getTarget(), request2);
				if(rule.getCondition() != null)
					cres = ConditionEvaluate(rule.getCondition(), request2);
				if(tres >= 0)
					if(tres == rt && cres == rc)
						skip[i] = true;
			}
		}
	}
	
	private void checkElementCoverage_override(List<Rule> rules, List<TargetMatch> matches, Rule rule, String request, int allindex, int index, boolean[] skip)
	{
		int rt = -1;
		int rc = -1;
		if(rule.getTarget() != null)
			rt = TargetEvaluate((Target)rule.getTarget(), request);
		if(rule.getCondition() != null)
			rc = ConditionEvaluate(rule.getCondition(), request);
		for(int i = index + 1; i < matches.size(); i++)
		{
			function f = new function();
			ArrayList<MyAttr> local = new ArrayList<MyAttr>();
			StringBuffer sb = new StringBuffer();
			sb.append(TruePolicyTarget(policy, local) + "\n");
			sb.append(getTargetAttribute_NegateValue((Target)rule.getTarget(), allindex, i, local) + "\n");
			sb.append(True_Condition(rule.getCondition(), local) + "\n");
			for(Rule r : rules)
			{
				if(r.getId().equals(rule.getId()) || isDefaultRule(r))
					continue;
				else
					sb.append(FalseTarget_FalseCondition(r, local) + "\n");
			}
			boolean sat = z3str(sb.toString(), nameMap, typeMap);
			if(sat)
			{
				try
				{
					z3.getValue(local, nameMap);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				String request2 = f.print(local);
				int tres = -1;
				int cres = -1;
				if(rule.getTarget() != null)
					tres = TargetEvaluate((Target)rule.getTarget(), request2);
				if(rule.getCondition() != null)
					cres = ConditionEvaluate(rule.getCondition(), request2);
				if(tres >= 0)
					if(tres == rt && cres == rc)
						skip[i] = true;
			}
		}
	}
	
	private void checkAnyOfCoverage_over(List<Rule> rules, String request, int start, int cur, boolean[][] skip)
	{
		skip[start][cur] = true;
		for(int i = start + 1; i < rules.size(); i++)
		{
			Rule rule2 = rules.get(i);
			if(rule2.getTarget() != null)
			{
				Target rt = (Target)rule2.getTarget();
				List<AnyOfSelection> anyOf = rt.getAnyOfSelections();
				if(anyOf.size() != 0)
				{
					if(anyOf.size() > 1)
					{
						for(int j = 0; j < anyOf.size(); j++)
						{
							function f = new function();
							StringBuffer sb = new StringBuffer();
							ArrayList<MyAttr> local = new ArrayList<MyAttr>();
							sb.append(TruePolicyTarget(policy, local) + "\n");
							sb.append(getTargetAnyOf_NegateValues(rt, j, local) + "\n");
							sb.append(True_Condition(rule2.getCondition(), local) + "\n");
							for(Rule r : rules)
							{
								if(r.getId().equals(rule2.getId()) || isDefaultRule(r))
									continue;
								else
									sb.append(False_Condition(r.getCondition(), local) + "\n");
							}
							boolean sat = z3str(sb.toString(), nameMap, typeMap);
							if(sat)
							{
								try
								{
									z3.getValue(local, nameMap);
								}
								catch(Exception e)
								{
									e.printStackTrace();
								}
								String request2 = f.print(local);
								if(request2.compareTo(request) == 0)
									skip[i][j] = true;
							}
						}
					}
					else
						continue;
				}
				else
					continue;
			}
			else
				continue;
		}
		for(int i = 0; i < start; i++)
		{
			Rule rule2 = rules.get(i);
			if(rule2.getTarget() != null)
			{
				Target rt = (Target)rule2.getTarget();
				List<AnyOfSelection> anyOf = rt.getAnyOfSelections();
				if(anyOf.size() != 0)
				{
					if(anyOf.size() > 1)
					{
						for(int j = 0; j < anyOf.size(); j++)
						{
							function f = new function();
							StringBuffer sb = new StringBuffer();
							ArrayList<MyAttr> local = new ArrayList<MyAttr>();
							sb.append(TruePolicyTarget(policy, local) + "\n");
							sb.append(getTargetAnyOf_NegateValues(rt, j, local) + "\n");
							sb.append(True_Condition(rule2.getCondition(), local) + "\n");
							for(Rule r : rules)
							{
								if(r.getId().equals(rule2.getId()) || isDefaultRule(r))
									continue;
								else
									sb.append(False_Condition(r.getCondition(), local) + "\n");
							}
							boolean sat = z3str(sb.toString(), nameMap, typeMap);
							if(sat)
							{
								try
								{
									z3.getValue(local, nameMap);
								}
								catch(Exception e)
								{
									e.printStackTrace();
								}
								String request2 = f.print(local);
								if(request2.compareTo(request) == 0)
									skip[i][j] = true;
							}
						}
					}
					else
						continue;
				}
				else
					continue;
			}
			else
				continue;
		}
	}
	
	private void checkAllOfCoverage_over(List<Rule> rules, String request, int start, int cur, boolean[][] skip)
	{
		skip[start][cur] = true;
		for(int i = start + 1; i < rules.size(); i++)
		{
			Rule rule = rules.get(i);
			if(rule.getTarget() != null)
			{
				Target rt = (Target)rule.getTarget();
				List<AnyOfSelection> anyOf = rt.getAnyOfSelections();
				if(anyOf.size() != 0)
				{
					for(int j = 0; j < anyOf.size(); j++)
					{
						AnyOfSelection any = anyOf.get(j);
						List<AllOfSelection> allOf = any.getAllOfSelections();
						if(allOf.size() != 0)
						{
							if(allOf.size() > 1)
							{
								for(int k = 0; k < allOf.size(); k++)
								{
									function f = new function();
									StringBuffer sb = new StringBuffer();
									ArrayList<MyAttr> local = new ArrayList<MyAttr>();
									sb.append(TruePolicyTarget(policy, local) + "\n");
									sb.append(getTargetAllOf_NegateValues(rt, j, k, local) + "\n");
									sb.append(True_Condition(rule.getCondition(), local) + "\n");
									for(Rule r : rules)
									{
										if(r.getId().equals(rule.getId()) || isDefaultRule(r))
											continue;
										else
											sb.append(False_Condition(r.getCondition(), local) + "\n");
									}
									boolean sat = z3str(sb.toString(), nameMap, typeMap);
									if(sat)
									{
										try
										{
											z3.getValue(local, nameMap);
										}
										catch(Exception e)
										{
											e.printStackTrace();
										}
										String request2 = f.print(local);
										if(request2.compareTo(request) == 0)
											skip[i][k] = true;
									}
								}
							}
							else
								continue;
						}
						else
							continue;
					}
				}
				else
					continue;
			}
			else
				continue;
		}
	}
	
	private void checkMatchCoverage_over(List<Rule> rules, String request, int start, int cur, boolean[][] skip)
	{
		skip[start][cur] = true;
		for(int i = start + 1; i < rules.size(); i++)
		{
			Rule rule = rules.get(i);
			if(rule.getTarget() != null)
			{
				Target rt = (Target)rule.getTarget();
				List<AnyOfSelection> anyOf = rt.getAnyOfSelections();
				if(anyOf.size() != 0)
				{
					for(int j = 0; j < anyOf.size(); j++)
					{
						AnyOfSelection any = anyOf.get(j);
						List<AllOfSelection> allOf = any.getAllOfSelections();
						if(allOf.size() != 0)
						{
							for(int k = 0; k < allOf.size(); k++)
							{
								AllOfSelection all = allOf.get(k);
								List<TargetMatch> matches = all.getMatches();
								if(matches.size() != 0)
								{
									if(matches.size() > 1)
									{
										for(int x = 0; x < matches.size(); x++)
										{
											function f = new function();
											StringBuffer sb = new StringBuffer();
											ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
											sb.append(TruePolicyTarget(policy, collector) + "\n");
											sb.append(getTargetAttribute_NegateValue(rt, k, x, collector) + "\n");
											sb.append(True_Condition(rule.getCondition(), collector) + "\n");
											for(Rule r : rules)
											{
												if(r.getId().equals(rule.getId()) || isDefaultRule(r))
													continue;
												else
													sb.append(False_Condition(r.getCondition(), collector) + "\n");
											}
											boolean sat = z3str(sb.toString(), nameMap, typeMap);
											if(sat)
											{
												try
												{
													z3.getValue(collector, nameMap);
												}
												catch(Exception e)
												{
													e.printStackTrace();
												}
												String request2 = f.print(collector);
												if(request2.compareTo(request) == 0)
													skip[i][x] = true;
											}
										}
									}
									else
										continue;
								}
								else
									continue;
							}
						}
						else
							continue;
					}
				}
				else
					continue;
			}
			else
				continue;
		}
	}
	
	private void checkAnyOfCoverage_unless(List<Rule> rules, String request, int start, int cur, int effect, boolean[][] skip)
	{
		skip[start][cur] = true;
		for(int i = start + 1; i < rules.size(); i++)
		{
			Rule rule = rules.get(i);
			if(rule.getEffect() != effect)
				continue;
			if(rule.getTarget() != null)
			{
				Target rt = (Target)rule.getTarget();
				List<AnyOfSelection> anyOf = rt.getAnyOfSelections();
				if(anyOf.size() != 0)
				{
					if(anyOf.size() > 1)
					{
						for(int j = 0; j < anyOf.size(); j++)
						{
							function f = new function();
							StringBuffer sb = new StringBuffer();
							ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
							sb.append(TruePolicyTarget(policy, collector) + "\n");
							sb.append(getTargetAnyOf_NegateValues(rt, j, collector) + "\n");
							sb.append(True_Condition(rule.getCondition(), collector) + "\n");
							for(int k = 0; k < rules.size(); k++)
							{
								Rule r = rules.get(k);
								if(k == i || isDefaultRule(r) || k == start)
									continue;
								if(k < i)
									sb.append(False_Condition(r.getCondition(), collector) + "\n");
								if(k > i && r.getEffect() == effect)
									sb.append(False_Condition(r.getCondition(), collector) + "\n");
							}
							boolean sat = z3str(sb.toString(), nameMap, typeMap);
							if(sat)
							{
								try
								{
									z3.getValue(collector, nameMap);
								}
								catch(Exception e)
								{
									e.printStackTrace();
								}
								String request2 = f.print(collector);
								if(request2.compareTo(request) == 0)
									skip[i][j] = true;
							}
						}
					}
					else
						continue;
				}
				else
					continue;
			}
			else
				continue;
		}
	}
	
	private void checkAllOfCoverage_unless(List<Rule> rules, String request, int start, int cur, int effect, boolean[][] skip)
	{
		skip[start][cur] = true;
		for(int i = start + 1; i < rules.size(); i++)
		{
			Rule rule = rules.get(i);
			if(rule.getEffect() != effect)
				continue;
			if(rule.getTarget() != null)
			{
				Target rt = (Target)rule.getTarget();
				List<AnyOfSelection> anyOf = rt.getAnyOfSelections();
				if(anyOf.size() != 0)
				{
					for(int j = 0; j < anyOf.size(); j++)
					{
						AnyOfSelection any = anyOf.get(j);
						List<AllOfSelection> allOf = any.getAllOfSelections();
						if(allOf.size() != 0)
						{
							if(allOf.size() > 1)
							{
								for(int k = 0; k < allOf.size(); k++)
								{
									function f = new function();
									StringBuffer sb = new StringBuffer();
									ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
									sb.append(TruePolicyTarget(policy, collector) + "\n");
									sb.append(getTargetAllOf_NegateValues(rt, j, k, collector) + "\n");
									sb.append(True_Condition(rule.getCondition(), collector) + "\n");
									for(int x = 0; x < rules.size(); x++)
									{
										Rule r = rules.get(x);
										if(x == i || isDefaultRule(r) || x == start)
											continue;
										if(x < i)
											sb.append(False_Condition(r.getCondition(), collector) + "\n");
										if(x > i && r.getEffect() == effect)
											sb.append(False_Condition(r.getCondition(), collector) + "\n");
									}
									boolean sat = z3str(sb.toString(), nameMap, typeMap);
									if(sat)
									{
										try
										{
											z3.getValue(collector, nameMap);
										}
										catch(Exception e)
										{
											e.printStackTrace();
										}
										String request2 = f.print(collector);
										if(request2.compareTo(request) == 0)
											skip[i][k] = true;
									}
								}
							}
							else
								continue;
						}
						else
							continue;
					}
				}
				else
					continue;
			}
			else
				continue;
		}
	}
	
	private void checkMatchCoverage_unless(List<Rule> rules, String request, int start, int cur, int effect, boolean[][] skip)
	{
		skip[start][cur] = true;
		for(int i = start + 1; i < rules.size(); i++)
		{
			Rule rule = rules.get(i);
			if(rule.getEffect() != effect)
				continue;
			if(rule.getTarget() != null)
			{
				Target rt = (Target)rule.getTarget();
				List<AnyOfSelection> anyOf = rt.getAnyOfSelections();
				if(anyOf.size() != 0)
				{
					for(int j = 0; j < anyOf.size(); j++)
					{
						AnyOfSelection any = anyOf.get(j);
						List<AllOfSelection> allOf = any.getAllOfSelections();
						if(allOf.size() != 0)
						{
							for(int k = 0; k < allOf.size(); k++)
							{
								AllOfSelection all = allOf.get(k);
								List<TargetMatch> matches = all.getMatches();
								if(matches.size() != 0)
								{
									if(matches.size() > 1)
									{
										for(int x = 0; x < matches.size(); x++)
										{
											function f = new function();
											StringBuffer sb = new StringBuffer();
											ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
											sb.append(TruePolicyTarget(policy, collector) + "\n");
											sb.append(getTargetAttribute_NegateValue(rt, k, x, collector) + "\n");
											sb.append(True_Condition(rule.getCondition(), collector) + "\n");
											for(int y = 0; y < rules.size(); y++)
											{
												Rule r = rules.get(y);
												if(y == i || isDefaultRule(r) || y == start)
													continue;
												if( y < i)
													sb.append(False_Condition(r.getCondition(), collector) + "\n");
												if(y > i && r.getEffect() == effect)
													sb.append(False_Condition(r.getCondition(), collector) + "\n");
											}
											boolean sat = z3str(sb.toString(), nameMap, typeMap);
											if(sat)
											{
												try
												{
													z3.getValue(collector, nameMap);
												}
												catch(Exception e)
												{
													e.printStackTrace();
												}
												String request2 = f.print(collector);
												if(request2.compareTo(request) == 0)
													skip[i][x] = true;
											}
										}
									}
									else
										continue;
								}
								else
									continue;
							}
						}
						else
							continue;
					}
				}
				else
					continue;
			}
			else
				continue;
		}
	}
	
	private void checkAnyOfCoverage_fa(List<Rule> rules, String request, int start, int cur, int effect, boolean[][] skip)
	{
		skip[start][cur] = true;
		for(int i = start + 1; i < rules.size(); i++)
		{
			Rule rule = rules.get(i);
			if(rule.getTarget() != null)
			{
				Target rt = (Target)rule.getTarget();
				List<AnyOfSelection> anyOf = rt.getAnyOfSelections();
				if(anyOf.size() != 0)
				{
					if(anyOf.size() > 1)
					{
						for(int j = 0; j < anyOf.size(); j++)
						{
							function f = new function();
							StringBuffer sb = new StringBuffer();
							ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
							sb.append(TruePolicyTarget(policy, collector) + "\n");
							sb.append(getTargetAnyOf_NegateValues(rt, j, collector) + "\n");
							sb.append(True_Condition(rule.getCondition(), collector) + "\n");
							int diff = -1;
							for(int k = 0; k < rules.size(); k++)
							{
								Rule r = rules.get(k);
								if(k == i || isDefaultRule(r) || k == start)
									continue;
								if(k > i)
								{
									if(r.getEffect() == effect)
										sb.append(False_Condition(r.getCondition(), collector) + "\n");
									if(r.getEffect() != effect && diff < 0)
										diff = k;
									else
										continue;
								}
								else
									sb.append(False_Condition(r.getCondition(), collector) + "\n");
							}
							if(diff > i)
								sb.append(TrueTarget_TrueCondition(rules.get(diff), collector) + "\n");
							boolean sat = z3str(sb.toString(), nameMap, typeMap);
							if(sat)
							{
								try
								{
									z3.getValue(collector, nameMap);
								}
								catch(Exception e)
								{
									e.printStackTrace();
								}
								String request2 = f.print(collector);
								if(request2.compareTo(request) == 0)
									skip[i][j] = true;
							}
						}
					}
					else
						continue;
				}
				else
					continue;
			}
			else
				continue;
		}
	}
	
	private void checkAllOfCoverage_fa(List<Rule> rules, String request, int start, int cur, int effect, boolean[][] skip)
	{
		skip[start][cur] = true;
		for(int i =  start + 1; i < rules.size(); i++)
		{
			Rule rule = rules.get(i);
			if(rule.getTarget() != null)
			{
				Target rt = (Target)rule.getTarget();
				List<AnyOfSelection> anyOf = rt.getAnyOfSelections();
				if(anyOf.size() != 0)
				{
					for(int j = 0; j < anyOf.size(); j++)
					{
						AnyOfSelection any = anyOf.get(j);
						List<AllOfSelection> allOf = any.getAllOfSelections();
						if(allOf.size() != 0)
						{
							if(allOf.size() > 1)
							{
								for(int k = 0; k < allOf.size(); k++)
								{
									function f = new function();
									StringBuffer sb = new StringBuffer();
									ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
									sb.append(TruePolicyTarget(policy, collector) + "\n");
									sb.append(getTargetAllOf_NegateValues(rt, j, k, collector) + "\n");
									sb.append(True_Condition(rule.getCondition(), collector) + "\n");
									int diff = -1;
									for(int x = 0; x < rules.size(); x++)
									{
										Rule r = rules.get(x);
										if(x == i || isDefaultRule(r) || x == start)
											continue;
										if(x > i)
										{
											if(r.getEffect() == effect)
												sb.append(False_Condition(r.getCondition(), collector) + "\n");
											if(r.getEffect() != effect && diff < 0)
												diff = x;
											else
												continue;
										}
										else
											sb.append(False_Condition(r.getCondition(), collector) + "\n");
									}
									if(diff > i)
										sb.append(TrueTarget_TrueCondition(rules.get(diff), collector) + "\n");
									boolean sat = z3str(sb.toString(), nameMap, typeMap);
									if(sat)
									{
										try
										{
											z3.getValue(collector, nameMap);
										}
										catch(Exception e)
										{
											e.printStackTrace();
										}
										String request2 = f.print(collector);
										if(request2.compareTo(request) == 0)
											skip[i][k] = true;
									}
								}
							}
							else
								continue;
						}
						else
							continue;
					}
				}
				else
					continue;
			}
			else
				continue;
		}
	}
	
	private void checkMatchCoverage_fa(List<Rule> rules, String request, int start, int cur, int effect, boolean[][] skip)
	{
		 skip[start][cur] = true;
		 for(int i = start + 1; i < rules.size(); i++)
		 {
			 Rule rule = rules.get(i);
			 if(rule.getTarget() != null)
			 {
				 Target rt = (Target)rule.getTarget();
				 List<AnyOfSelection> anyOf = rt.getAnyOfSelections();
				 if(anyOf.size() != 0)
				 {
					 for(int j = 0; j < anyOf.size(); j++)
					 {
						 AnyOfSelection any = anyOf.get(j);
						 List<AllOfSelection> allOf = any.getAllOfSelections();
						 if(allOf.size() != 0)
						 {
							 for(int k = 0; k < allOf.size(); k++)
							 {
								 AllOfSelection all = allOf.get(k);
								 List<TargetMatch> matches = all.getMatches();
								 if(matches.size() != 0)
								 {
									 if(matches.size() > 1)
									 {
										 for(int x = 0; x < matches.size(); x++)
										 {
											 function f = new function();
											 StringBuffer sb = new StringBuffer();
											 ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
											 sb.append(TruePolicyTarget(policy, collector) + "\n");
											 sb.append(getTargetAttribute_NegateValue(rt, k, x, collector) + "\n");
											 sb.append(True_Condition(rule.getCondition(), collector) + "\n");
											 int diff = -1;
											 for(int y = 0; y < rules.size(); y++)
											 {
												 Rule r = rules.get(y);
												 if(y == i || isDefaultRule(r) || y == start)
													 continue;
												 if(y > i)
												 {
													 if(r.getEffect() == effect)
														 sb.append(False_Condition(r.getCondition(), collector) + "\n");
													 if(r.getEffect() != effect && diff < 0)
														 diff = y;
													 else
														 continue;
												 }
												 else
													 sb.append(False_Condition(r.getCondition(), collector) + "\n");
											 }
											 if(diff > i)
												 sb.append(TrueTarget_TrueCondition(rules.get(diff), collector) + "\n");
											 boolean sat = z3str(sb.toString(), nameMap, typeMap);
											 if(sat)
											 {
												 try
												 {
													 z3.getValue(collector, nameMap);
												 }
												 catch(Exception e)
												 {
													 e.printStackTrace();
												 }
												 String request2 = f.print(collector);
												 if(request2.compareTo(request) == 0)
													 skip[i][x] = true;
											 }
										 }
									 }
									 else
										 continue; 
								 }
								 else
									 continue;
							 }
						 }
						 else
							 continue;
					 }
				 }
				 else
					 continue;
			 }
			 else
				 continue;
		 }
	}
	
	private boolean[][] buildAnyOfTable(List<Rule> rules)
	{
		boolean[][] any_skip = new boolean[rules.size()][0];
		for(int i = 0; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(r.getTarget() != null)
			{
				Target rt = (Target)r.getTarget();
				any_skip[i] = new boolean[rt.getAnyOfSelections().size()];
			}
			else
				any_skip[i] = new boolean[0];
		}
		return any_skip;
	}
	
	private boolean[][] buildAllOfTable(List<Rule> rules)
	{
		boolean[][] all_skip = new boolean[rules.size()][0];
		int allOf = 0;
		for(int i = 0; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(r.getTarget() != null)
			{
				Target rt = (Target)r.getTarget();
				List<AnyOfSelection> anyOf = rt.getAnyOfSelections();
				for(AnyOfSelection any : anyOf)
				{
					allOf += any.getAllOfSelections().size();
				}
				all_skip[i] = new boolean[allOf];
			}
		}
		return all_skip;
	}
	
	private boolean[][] buildMatchTable(List<Rule> rules)
	{
		boolean[][] match_skip = new boolean[rules.size()][0];
		int matches = 0;
		for(int i = 0; i < rules.size(); i++)
		{
			Rule r = rules.get(i);
			if(r.getTarget() != null)
			{
				Target rt = (Target)r.getTarget();
				List<AnyOfSelection> anyOf = rt.getAnyOfSelections();
				for(AnyOfSelection any : anyOf)
				{
					List<AllOfSelection> allOf = any.getAllOfSelections();
					for(AllOfSelection all : allOf)
					{
						List<TargetMatch> match = all.getMatches();
						for(TargetMatch m : match)
							matches++;
					}
				}
				match_skip[i] = new boolean[matches];
			}
		}
		return match_skip;
	}
	
	private LinkedHashMap<String, String> getValues(List<Rule> rules)
	{
		LinkedHashMap<String, String> value_map = new LinkedHashMap<String, String>();
		for(Rule R : rules)
		{
			if(R.getTarget() != null)
			{
				Target rt = (Target)R.getTarget();
				for(AnyOfSelection any : rt.getAnyOfSelections())
				{
					for(AllOfSelection all : any.getAllOfSelections())
					{
						for(TargetMatch match : all.getMatches())
						{
							if(match.getEval() instanceof AttributeDesignator)
							{
								AttributeDesignator attr = (AttributeDesignator)match.getEval();
								if(value_map.get(match.getAttrValue().encode() + " " + attr.getId().toString()) == null)
								{
									value_map.put(match.getAttrValue().encode() + " " + attr.getId().toString(), match.getAttrValue().encode());
									vMap.put(match.getAttrValue().encode() + " " + attr.getId().toString(), match.getAttrValue().encode());
									tMap.put(match.getAttrValue().encode() + " " + attr.getId().toString(), attr.getType().toString());
									rMap.put(match.getAttrValue().encode() + " " + attr.getId().toString(), attr.getId().toString());
									funcMap.put(match.getAttrValue().encode() + " " + attr.getId().toString(), match.getMatchFunction().encode());
									catMap.put(match.getAttrValue().encode() + " " + attr.getId().toString(), attr.getCategory().toString());
								}
								else
									continue;
							}
						}
					}
				}
			}
			else
				continue;
		}
		return value_map;
	}
	
	private String getUniqueAttribute(Target rt, int anIndex, int alIndex, int atIndex, ArrayList<MyAttr> collector)
	{
		StringBuffer sb = new StringBuffer();
		if(rt != null)
		{
			for(int i = 0; i < rt.getAnyOfSelections().size(); i++)
			{
				if(i != anIndex)
					continue;
				StringBuilder orBuilder = new StringBuilder();
				AnyOfSelection any = rt.getAnyOfSelections().get(i);
				for(int j = 0; j < any.getAllOfSelections().size(); j++)
				{
					if(j != alIndex)
						continue;
					StringBuilder andBuilder = new StringBuilder();
					AllOfSelection all = any.getAllOfSelections().get(j);
					for(int k = 0; k < all.getMatches().size(); k++)
					{
						TargetMatch match = all.getMatches().get(k);
						if(match.getEval() instanceof AttributeDesignator)
						{
							AttributeDesignator attr = (AttributeDesignator)match.getEval();
							if(k == atIndex)
							{ 
								continue;
							}
							else
							{
								andBuilder.append(" ("
										+ al.returnFunction(match.getMatchFunction().encode())
										+ " " + getName(attr.getId().toString())
										+ " ");
								if(attr.getType().toString().contains("string"))
								{
									String val = match.getAttrValue().encode();
									val = val.replaceAll("\n", "");
									val = val.trim();
									andBuilder.append("\"" + val + "\")");
								}
								if(attr.getType().toString().contains("integer"))
								{
									String val = match.getAttrValue().encode();
									val = val.replaceAll("\n", "");
									val = val.trim();
									andBuilder.append(val + ")");
								}
							}
							getType(getName(attr.getId().toString()),
									attr.getType().toString());
							MyAttr myattr = new MyAttr(attr.getId().toString(), attr.getCategory().toString(), attr.getType().toString());
							if (isExist(collector, myattr) == false) 
								collector.add(myattr);
						}
					}
					andBuilder.insert(0, " (and");
					andBuilder.append(")");
					orBuilder.append(andBuilder);
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
	
	private String getAttributeFromKey(String key, ArrayList<MyAttr> collector)
	{
		StringBuffer sb = new StringBuffer();
		StringBuilder orBuilder = new StringBuilder();
		StringBuilder andBuilder = new StringBuilder();
		String val = vMap.get(key);
		String type = tMap.get(key);
		String ID = rMap.get(key);
		String func = funcMap.get(key);
		String cat = catMap.get(key);
		andBuilder.append(" ("
				+  al.returnFunction(func)
				+ " " + getName(ID)
				+ " ");
		if(type.contains("string"))
		{
			val = val.replaceAll("\n", "");
			val = val.trim();
			andBuilder.append("\"" + val + "\")");
		}
		if(type.contains("integer"))
		{
			val = val.replaceAll("\n", "");
			val = val.trim();
			andBuilder.append(val + ")");
		}
		getType(getName(ID), type);
		MyAttr mattr = new MyAttr(ID, cat, type);
		if(!isExist(collector, mattr))
			collector.add(mattr);
		andBuilder.insert(0, " (and");
		andBuilder.append(")");
		orBuilder.append(andBuilder);
		orBuilder.insert(0, " (or ");
		orBuilder.append(")");
		sb.append(orBuilder);
		sb.insert(0, "(and ");
		sb.append(")");
		return sb.toString();
	}
	
	private String getNonUniqueAttributes(Target rt, String unique, ArrayList<MyAttr> collector)
	{
		StringBuffer sb = new StringBuffer();
		if(rt != null)
		{
			for(AnyOfSelection any : rt.getAnyOfSelections())
			{
				StringBuilder orBuilder = new StringBuilder();
				for(AllOfSelection all : any.getAllOfSelections())
				{
					StringBuilder andBuilder = new StringBuilder();
					for(TargetMatch match : all.getMatches())
					{
						if(match.getEval() instanceof AttributeDesignator)
						{
							AttributeDesignator attr = (AttributeDesignator)match.getEval();
							if(attr.getId().toString().compareTo(unique) == 0)
								continue;
							else
							{
								andBuilder.append(" ("
										+ al.returnFunction(match.getMatchFunction().encode())
										+ " " + getName(attr.getId().toString())
										+ " ");
								if(attr.getType().toString().contains("string"))
									andBuilder.append("\"" + match.getAttrValue().encode().replaceAll("\n", "").trim() + "\")");
								if(attr.getType().toString().contains("integer"))
									andBuilder.append(match.getAttrValue().encode().replaceAll("\n", "").trim() + ")");
								getType(getName(attr.getId().toString()), attr.getType().toString());
								MyAttr mine = new MyAttr(attr.getId().toString(), attr.getCategory().toString(), attr.getType().toString());
								if(!isExist(collector, mine))
									collector.add(mine);
							}
						}
					}
					andBuilder.insert(0, " (and");
					andBuilder.append(")");
					orBuilder.append(andBuilder);
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
	
	private ArrayList<String> getNonUniqueTargetIds(List<Rule> rules)
	{
		ArrayList<String> target_ids = new ArrayList<String>();
		String min_unique = "";
		String max_unique = "";
		int min = Integer.MAX_VALUE;
		int max = 0;
		ArrayList<MyAttr> ids = new ArrayList<MyAttr>();
		for(Rule r : rules)
			True_Target((Target)r.getTarget(), ids);
		for(MyAttr attr : ids)
		{
			String current = attr.getName();
			HashSet<String> values = new HashSet<String>();
			for(Rule r : rules)
			{
				if(r.getTarget() != null)
				{
					Target rtemp = (Target)r.getTarget();
					for(AnyOfSelection any : rtemp.getAnyOfSelections())
					{
						for(AllOfSelection all : any.getAllOfSelections())
						{
							for(TargetMatch match : all.getMatches())
							{
								if(match.getEval() instanceof AttributeDesignator)
								{
									AttributeDesignator temp = (AttributeDesignator)match.getEval();
									if(temp.getId().toString().compareTo(current) == 0)
										values.add(match.getAttrValue().encode());
								}
							}
						}
					}
				}
			}
			if(values.size() < min)
			{
				target_ids.remove(min_unique);
				target_ids.add(current);
				min_unique = current;
				min = values.size();
			}
			else if(values.size() > max)
			{
				target_ids.remove(max_unique);
				target_ids.add(current);
				max_unique = current;
				max = values.size();
			}
		}
		return target_ids;
	}
	
	private PolicySpreadSheetTestRecord buildRequest_ind(List<Rule> rules, Rule rule, StringBuffer sb, ArrayList<MyAttr> collector, int count, int i, int j, int k, TestPanel t, HashMap<String, String> value_map, String type)
	{
		PolicySpreadSheetTestRecord ptr = null;
		function f = new function();
		sb.append(getUniqueAttribute((Target)rule.getTarget(), i, j, k, collector) + "\n");
		sb.append(True_Condition(rule.getCondition(), collector) + "\n");
		for(Rule r : rules)
		{
			if(r.getId().equals(rule.getId()) || isDefaultRule(r))
				continue;
			else
				sb.append(False_Condition(r.getCondition(), collector) + "\n");
		}
		
		boolean sat = z3str(sb.toString(), nameMap, typeMap);
		System.err.println(sb.toString());
		if(sat)
		{
			try
			{
				z3.getValue(collector, nameMap);
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			if(collector.size() == 0)
				return ptr;
			String request = f.print(collector);
			try
			{
				String path = t.getTestOutputDestination("_MutationTests")
						+ File.separator + "request" + type + count + ".txt";
				FileWriter fw = new FileWriter(path);
				BufferedWriter bw = new BufferedWriter(fw);
				bw.write(request);
				bw.close();
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
			ptr = new PolicySpreadSheetTestRecord(
					PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + type + count,
					"request" + type + count + ".txt", request, "");
		}
		else 
		{
			System.err.println(sb.toString());
		}
		return ptr;
	}
	
	private void buildRequest_ind2(TestPanel t, String type, ArrayList<PolicySpreadSheetTestRecord> generator)
	{
		function f = new function();
		int count = 1;
		for(String key : vMap.keySet())
		{
			PolicySpreadSheetTestRecord ptr = null;
			ArrayList<MyAttr> collector = new ArrayList<MyAttr>();
			StringBuffer sb = new StringBuffer();
			sb.append(TruePolicyTarget(policy, collector) + "\n");
			sb.append(getAttributeFromKey(key, collector) + "\n");
			boolean sat = z3str(sb.toString(), nameMap, typeMap);
			if(sat)
			{
				try
				{
					z3.getValue(collector, nameMap);
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				String request = f.print(collector);
				try
				{
					String path = t.getTestOutputDestination("_MutationTests")
							+ File.separator + "request" + type + count + ".txt";
					BufferedWriter bw = new BufferedWriter(new FileWriter(path));
					bw.write(request);
					bw.close();
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
				ptr = new PolicySpreadSheetTestRecord(
						PolicySpreadSheetTestSuite.TEST_KEYWORD + " " + type + count,
						"request" + type + count + ".txt", request, "");
			}
			if(ptr != null)
			{
				generator.add(ptr);
				count++;
			}
		}
	}
	
	private ArrayList<MyAttr> getAttributes(List<Rule> rules)
	{
		ArrayList<MyAttr> attributes = new ArrayList<MyAttr>();
		for(Rule r : rules)
		{
			getTargetAttribute((Target)r.getTarget(), attributes);
			getConditionAttribute(r.getCondition(), attributes);
		}
		return attributes;
	}
}
