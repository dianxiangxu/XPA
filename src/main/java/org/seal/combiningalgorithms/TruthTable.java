package org.seal.combiningalgorithms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.seal.gui.XPA;
import org.wso2.balana.Balana;
import org.wso2.balana.MatchResult;
import org.wso2.balana.ParsingException;
import org.wso2.balana.Policy;
import org.wso2.balana.PolicyTreeElement;
import org.wso2.balana.Rule;
import org.wso2.balana.attr.BooleanAttribute;
import org.wso2.balana.combine.CombinerElement;
import org.wso2.balana.cond.Condition;
import org.wso2.balana.cond.EvaluationResult;
import org.wso2.balana.ctx.AbstractRequestCtx;
import org.wso2.balana.ctx.RequestCtxFactory;
import org.wso2.balana.ctx.xacml3.RequestCtx;
import org.wso2.balana.ctx.xacml3.XACML3EvaluationCtx;
import org.wso2.balana.xacml3.AllOfSelection;
import org.wso2.balana.xacml3.AnyOfSelection;
import org.wso2.balana.xacml3.Target;

public class TruthTable {
	private static Balana balana;

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
		public int[] basics;
		public int effect; // 0=T, 1=F, 2=E;
		public int covered; // 0 = not covered, 1 = covered

		public TarRecord(int effect, int covered) {
			this.effect = effect;
			this.covered = covered;
		}

		public TarRecord(int[] basics, int effect, int covered) {
			this(effect, covered);
			this.basics = basics;
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

		public void setEffect(int effect) {
			this.effect = effect;
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

	public PolicyTable buildDecisonCoverage(Policy policy) {
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

	public void DecisionCoverage(Policy policy, PolicyTable policytable) {
		List<Rule> rules = getRuleFromPolicy(policy);
		ArrayList<TarRecord> trecord = policytable.getTarget();
		for (TarRecord ptarget : trecord) {
			if (ptarget.getEffect() == 2 && ptarget.getCovered() == 0) {
				StringBuffer sb = new StringBuffer();
				
			} else if (ptarget.getEffect() == 1 && ptarget.getCovered() == 0) {

			} else if (ptarget.getEffect() == 0) {
				// if policy target is true
				for (int i = 0; i < rules.size(); i++) {
					RuleRecord ruleRecord = policytable.getRules().get(i);
					for (TarRecord rtarget : ruleRecord.getTarget()) {
						if (rtarget.getEffect() == 0) {

						} else if (rtarget.getEffect() == 1) {

						} else if (rtarget.getEffect() == 2) {
							// if rule target is true
							for (ConRecord rcondition : ruleRecord
									.getCondition()) {
								if (rcondition.getEffect() == 0) {

								} else if (rcondition.getEffect() == 1) {

								} else if (rcondition.getEffect() == 2) {

								}
							}

						}
					}
				}
			}
		}
	}

	public void upTable(Policy policy, PolicyTable policytable, String request,
			int start) {
		List<Rule> rules = getRuleFromPolicy(policy);
		for (int i = start; i < rules.size(); i++) {
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
			if(record.getTarget().size() == 0){
				for(ConRecord conRecord : record.getCondition()){
					if(conRecord.getEffect() == cResult && conRecord.getCovered()==0){
						conRecord.setCovered(1);
					}
				}
			}else{ // has target
				for(TarRecord tarRecord : record.getTarget()){
					if(tarRecord.getEffect() == tResult && tarRecord.getCovered()==0){
						if(tResult != 0){
							tarRecord.setCovered(1);
						}else{
							for(ConRecord conRecord : record.getCondition()){
								if(conRecord.getEffect() == cResult && conRecord.getCovered()==0){
									conRecord.setCovered(1);
								}
							}
						}
					}
				}
			}
		}
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
						MatchResult match = null;
						match = allof.match(ec);
						result.add(match.getResult());
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
		XACML3EvaluationCtx ec;
		ec = getEvaluationCtx(request);
		// System.out.print(request);ReadPolicy.getPDPconfig()
		return policy.evaluate(ec).getDecision();
	}
	

}
