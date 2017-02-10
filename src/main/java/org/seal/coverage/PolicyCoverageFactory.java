package org.seal.coverage;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.wso2.balana.ctx.AbstractResult;

public class PolicyCoverageFactory {
	public static ArrayList<PolicyCoverage> policyCoverages = new ArrayList<PolicyCoverage>();
	public static PolicyCoverage currentPolicyCoverage;
	public static String currentTestID;
	public static int currentTestOracle;
	
	public static void init(){
		policyCoverages.clear();
		currentPolicyCoverage = null;
		currentTestID = null;
	}
	
	public static void startNewPolicyCoverage(String policyID, int numberOfRules, int targetMatchResult){
		flush();
		currentPolicyCoverage = new PolicyCoverage(currentTestID, currentTestOracle, policyID, numberOfRules, targetMatchResult); 
	}
	
	public static void flush(){
		if (currentPolicyCoverage!=null){
			policyCoverages.add(currentPolicyCoverage);		
			currentPolicyCoverage = null;	// prevent repeated calls
		}
	}
	
	public static void writeCoverageToSpreadSheet(String fileName){
		flush();
		if (policyCoverages.size()==0)
			System.err.println("No policy coverage!\n");				
		HSSFWorkbook workBook = new HSSFWorkbook();
		workBook.createSheet("rule coverage");
		Sheet sheet = workBook.getSheetAt(0);
		writeFirstTitleRow(sheet, 0, policyCoverages.get(0));
		writeSecondTitleRow(sheet,1, policyCoverages.get(0));
		
		int rowIndex = 2;
		for (PolicyCoverage policyCoverage: policyCoverages){
			writePolicyCoverage(sheet, rowIndex++, policyCoverage);
		}
		
		workBook.createSheet("coverage statistics");
		int statRowIndex=0;
		Sheet statisticsSheet = workBook.getSheetAt(1);
		writeVerdictCounts(statisticsSheet, statRowIndex);
		statRowIndex +=3;
		writeDecisionCoverageCounts(statisticsSheet, statRowIndex);
		statRowIndex +=4;
		writeRuleDecisionCoverageCounts(statisticsSheet, statRowIndex);
		statRowIndex +=6;
		writeAllTargetConditionCombinationalCoverageCounts(statisticsSheet, statRowIndex);	
		try {
			FileOutputStream out = new FileOutputStream(fileName);
			workBook.write(out);
			out.close();
		}
		catch (IOException ioe){
			ioe.printStackTrace();
		}
	}

	private static void writeVerdictCounts(Sheet sheet, int rowIndex){
		int[] verdictCoverageCounts = getVerdictCoverageCounts();
		Row row = sheet.createRow(rowIndex);
		Cell passCell = row.createCell(0);
		passCell.setCellValue("PASS");
		Cell failCell = row.createCell(1);
		failCell.setCellValue("FAIL");

		row = sheet.createRow(rowIndex+1);
		Cell passCountCell = row.createCell(0);
		passCountCell.setCellValue(verdictCoverageCounts[0]);
		Cell failCountCell = row.createCell(1);
		failCountCell.setCellValue(verdictCoverageCounts[1]);
	}
	
	private static int[] getVerdictCoverageCounts(){
		int[] coverageCounts = new int[2];
		coverageCounts[0] = getVerdictCoverageCount("PASS");
		coverageCounts[1] = getVerdictCoverageCount("FAIL");
		return coverageCounts;
	}
	
	private static int getVerdictCoverageCount(String verdict){
		int count=0;
		for (PolicyCoverage policyCoverage: policyCoverages){
			if (policyCoverage.getVerdict().equalsIgnoreCase(verdict)){
				count++;
			}
		} 
		return count;
	}

	private static void writeDecisionCoverageCounts(Sheet sheet, int rowIndex){
		int[] policyDecisionCoverageCounts = getPolicyDecisionCoverageCounts();
		Row titleRow = sheet.createRow(rowIndex);
		Cell[] titleCells = new Cell[policyDecisionCoverageCounts.length];
		for (int i=0; i<titleCells.length; i++)
			titleCells[i] = titleRow.createCell(i);
		titleCells[0].setCellValue("PERMIT");
		titleCells[1].setCellValue("DENY");
		titleCells[2].setCellValue("INDETERMINATE");
		titleCells[3].setCellValue("NA");
		titleCells[4].setCellValue("ID");
		titleCells[5].setCellValue("IP");
		titleCells[6].setCellValue("IDP");
		Row dataRow = sheet.createRow(rowIndex+1);
		for (int i=0; i<titleCells.length; i++) {
			Cell dataCell = dataRow.createCell(i);
			dataCell.setCellValue(policyDecisionCoverageCounts[i]);
		}
	}
	
	private static int[] getPolicyDecisionCoverageCounts(){
		int[] coverageCounts = new int[7];
		coverageCounts[0] = getPolicyDecisionCoverageCount(AbstractResult.DECISION_PERMIT);
		coverageCounts[1] = getPolicyDecisionCoverageCount(AbstractResult.DECISION_DENY);
		coverageCounts[2] = getPolicyDecisionCoverageCount(AbstractResult.DECISION_INDETERMINATE);
		coverageCounts[3] = getPolicyDecisionCoverageCount(AbstractResult.DECISION_NOT_APPLICABLE);
		coverageCounts[4] = getPolicyDecisionCoverageCount(AbstractResult.DECISION_INDETERMINATE_DENY);
		coverageCounts[5] = getPolicyDecisionCoverageCount(AbstractResult.DECISION_INDETERMINATE_PERMIT);
		coverageCounts[6] = getPolicyDecisionCoverageCount(AbstractResult.DECISION_INDETERMINATE_DENY_OR_PERMIT);
		return coverageCounts;
	}

	private static int getPolicyDecisionCoverageCount(int decision){
		int count=0;
		for (PolicyCoverage policyCoverage: policyCoverages){
			if (policyCoverage.getDecision()==decision){
				count++;
			}
		} 
		return count;
	}
	
	private static void writeRuleDecisionCoverageCounts(Sheet sheet, int rowIndex){
		if (policyCoverages.size()>0) {
			Row testRow = sheet.createRow(rowIndex);
			writeRulesTitleRow(testRow, policyCoverages.get(0).getRuleCoverages());
			ArrayList<int[]> allRuleDecisionCoverageCounts = getAllRuleDecisionCoverageCounts();
			writeRuleCoverageRow(sheet, rowIndex, allRuleDecisionCoverageCounts, "EFFECT", 0);
			writeRuleCoverageRow(sheet, rowIndex, allRuleDecisionCoverageCounts, "NA", 1);
			writeRuleCoverageRow(sheet, rowIndex, allRuleDecisionCoverageCounts, "INDETERMINATE", 2);	
			writeRuleCoverageTotalRow(sheet, rowIndex, allRuleDecisionCoverageCounts, 3);	
		}
	}
	
	private static void writeRulesTitleRow(Row testRow, ArrayList<RuleCoverage> rules){
		Cell testCell = testRow.createCell(0);
		testCell.setCellValue("");
		for (int i=0; i<rules.size(); i++){
			testCell = testRow.createCell(i+1);
			testCell.setCellValue(rules.get(i).getId());
		}		
	}
	
	private static void writeRuleCoverageRow(Sheet sheet, int rowIndex, ArrayList<int[]> allRuleDecisionCoverageCounts, String category, int index){
		Row row = sheet.createRow(rowIndex+1+index);		
		Cell cell = row.createCell(0);
		cell.setCellValue(category);
		for (int i=0; i<allRuleDecisionCoverageCounts.size(); i++) {
			cell = row.createCell(i+1);
			cell.setCellValue(allRuleDecisionCoverageCounts.get(i)[index]);
		}
	}

	private static void writeRuleCoverageTotalRow(Sheet sheet, int rowIndex, ArrayList<int[]> allRuleDecisionCoverageCounts, int total){
		Row row = sheet.createRow(rowIndex+1+total);		
		Cell cell = row.createCell(0);
		cell.setCellValue("TOTAL");
		for (int i=0; i<allRuleDecisionCoverageCounts.size(); i++) {
			int sum =0;
			for (int j=0; j<allRuleDecisionCoverageCounts.get(i).length; j++){
				sum+=allRuleDecisionCoverageCounts.get(i)[j];
			}
			cell = row.createCell(i+1);
			cell.setCellValue(sum);
		}
	}

	private static ArrayList<int[]> getAllRuleDecisionCoverageCounts(){
		ArrayList<int[]> counts = new ArrayList<int[]>();
		for (int ruleIndex=0; ruleIndex<totalNumberOfRules(); ruleIndex++) {
			counts.add(getRuleDecisionCoverageCounts(ruleIndex));
		}
		return counts;
	}
	
	private static int totalNumberOfRules(){
		if (policyCoverages.size()>0)
			return policyCoverages.get(0).getNumberOfRules();
		else 
			return 0;
	}
	
	private static int[] getRuleDecisionCoverageCounts(int ruleIndex){
		int[] coverageCounts = new int[3];
		coverageCounts[0] = getRuleDecisionCoverageCount(ruleIndex, RuleCoverage.RuleDecisionCoverage.EFFECT);
		coverageCounts[1] = getRuleDecisionCoverageCount(ruleIndex, RuleCoverage.RuleDecisionCoverage.NA);
		coverageCounts[2] = getRuleDecisionCoverageCount(ruleIndex, RuleCoverage.RuleDecisionCoverage.INDETERMINATE);
		return coverageCounts;
	}
	
	// precondition ruleIndex>= 0 
	private static int getRuleDecisionCoverageCount(int ruleIndex, RuleCoverage.RuleDecisionCoverage ruleDecision){
		int count=0;
		for (PolicyCoverage policyCoverage: policyCoverages){
			ArrayList<RuleCoverage> ruleCoverages = policyCoverage.getRuleCoverages();
			if (ruleIndex<ruleCoverages.size() && ruleCoverages.get(ruleIndex).getRuleDecisionCoverage() == ruleDecision){
				count++;
			}
		} 		
		return count;
	}

	private static void writeAllTargetConditionCombinationalCoverageCounts(Sheet sheet, int rowIndex){
		if (policyCoverages.size()>0) {
			Row testRow = sheet.createRow(rowIndex);
			writeRulesTitleRow(testRow, policyCoverages.get(0).getRuleCoverages());
			ArrayList<int[]> allTargetConditionCombinationCoverageCounts = getAllTargetConditionCombinationalCoverageCounts();
			writeRuleCoverageRow(sheet, rowIndex, allTargetConditionCombinationCoverageCounts, "BOTHTRUE", 0);
			writeRuleCoverageRow(sheet, rowIndex, allTargetConditionCombinationCoverageCounts, "FALSETARGET", 1);
			writeRuleCoverageRow(sheet, rowIndex, allTargetConditionCombinationCoverageCounts, "FALSECONDITION", 2);	
			writeRuleCoverageRow(sheet, rowIndex, allTargetConditionCombinationCoverageCounts, "ERRORTARGET", 3);	
			writeRuleCoverageRow(sheet, rowIndex, allTargetConditionCombinationCoverageCounts, "ERRORCONDITION", 4);	
			writeRuleCoverageTotalRow(sheet, rowIndex, allTargetConditionCombinationCoverageCounts, 5);	
		}
	}

	private static ArrayList<int[]> getAllTargetConditionCombinationalCoverageCounts(){
		ArrayList<int[]> counts = new ArrayList<int[]>();
		for (int ruleIndex=0; ruleIndex<totalNumberOfRules(); ruleIndex++) {
			counts.add(getTargetConditionCombinationalCoverageCounts(ruleIndex));
		}
		return counts;
	}

	// precondition ruleIndex>= 0 
	private static int[] getTargetConditionCombinationalCoverageCounts(int ruleIndex){
		int[] coverageCounts = new int[5];
		coverageCounts[0] = getTargetConditionCombinationalCoverageCount(ruleIndex, RuleCoverage.TargetConditionCombinationalCoverage.BOTHTRUE);
		coverageCounts[1] = getTargetConditionCombinationalCoverageCount(ruleIndex, RuleCoverage.TargetConditionCombinationalCoverage.FALSETARGET);
		coverageCounts[2] = getTargetConditionCombinationalCoverageCount(ruleIndex, RuleCoverage.TargetConditionCombinationalCoverage.FALSECONDITION);
		coverageCounts[3] = getTargetConditionCombinationalCoverageCount(ruleIndex, RuleCoverage.TargetConditionCombinationalCoverage.ERRORTARGET);
		coverageCounts[4] = getTargetConditionCombinationalCoverageCount(ruleIndex, RuleCoverage.TargetConditionCombinationalCoverage.ERRORCONDITION);
		return coverageCounts;
	}
	
	// precondition ruleIndex>= 0 
	private static int getTargetConditionCombinationalCoverageCount(int ruleIndex, RuleCoverage.TargetConditionCombinationalCoverage coverage){
		int count=0;
		for (PolicyCoverage policyCoverage: policyCoverages){
			ArrayList<RuleCoverage> ruleCoverages = policyCoverage.getRuleCoverages();
			if (ruleIndex<ruleCoverages.size() && ruleCoverages.get(ruleIndex).getTargetConditionCombinationalCoverage() == coverage){
				count++;
			}
		} 		
		return count;
	}

	private static void writeFirstTitleRow(Sheet sheet, int rowIndex, PolicyCoverage policyCoverage){
		Row row = sheet.createRow(rowIndex);
		Cell testCell = row.createCell(0);
		testCell.setCellValue("");
		Cell verdictCell = row.createCell(1);
		verdictCell.setCellValue("");
		Cell oracleCell = row.createCell(2);
		oracleCell.setCellValue("");
		Cell policyCell = row.createCell(3);
		policyCell.setCellValue("");
		Cell numberOfRulesCell = row.createCell(4);
		numberOfRulesCell.setCellValue("");
		Cell coveredRulesCell = row.createCell(5);
		coveredRulesCell.setCellValue("");
		Cell decisionCell = row.createCell(6);
		decisionCell.setCellValue("");
		Cell targetMatchResultCell = row.createCell(7);
		targetMatchResultCell.setCellValue("");		
		int columnIndex = 7;
		for (RuleCoverage ruleCoverage: policyCoverage.getRuleCoverages()){
			Cell resultCell = row.createCell(columnIndex++);
			resultCell.setCellValue(ruleCoverage.getId());
			Cell targetCell = row.createCell(columnIndex++);
			targetCell.setCellValue("");
			Cell conditionCell = row.createCell(columnIndex++);
			conditionCell.setCellValue("");
		}
	}
	
	private static void writeSecondTitleRow(Sheet sheet, int rowIndex, PolicyCoverage policyCoverage){
		Row row = sheet.createRow(rowIndex);
		Cell testCell = row.createCell(0);
		testCell.setCellValue("test");
		Cell verdictCell = row.createCell(1);
		verdictCell.setCellValue("verdict");
		Cell oracleCell = row.createCell(2);
		oracleCell.setCellValue("oracle");
		Cell policyCell = row.createCell(3);
		policyCell.setCellValue("policy");
		Cell numberOfRulesCell = row.createCell(4);
		numberOfRulesCell.setCellValue("#rules");
		Cell coveredRulesCell = row.createCell(5);
		coveredRulesCell.setCellValue("executed");
		Cell decisionCell = row.createCell(6);
		decisionCell.setCellValue("decision");
		Cell targetMatchResultCell = row.createCell(7);
		targetMatchResultCell.setCellValue("policy target match");		
		int columnIndex = 8;
		for (RuleCoverage ruleCoverage: policyCoverage.getRuleCoverages()){
			Cell resultCell = row.createCell(columnIndex++);
			resultCell.setCellValue("effect");
			Cell targetCell = row.createCell(columnIndex++);
			targetCell.setCellValue("target");
			Cell conditionCell = row.createCell(columnIndex++);
			conditionCell.setCellValue("condition");
		}
		
	}

	private static void writePolicyCoverage(Sheet sheet, int rowIndex, PolicyCoverage policyCoverage){
		sheet.createRow(rowIndex);
		Row row = sheet.createRow(rowIndex);
		Cell testCell = row.createCell(0);
		testCell.setCellValue(policyCoverage.getTestID());
		Cell verdictCell = row.createCell(1);
		verdictCell.setCellValue(policyCoverage.getVerdict());			
		Cell oracleCell = row.createCell(2);
		oracleCell.setCellValue(policyCoverage.getOracle());
		Cell policyCell = row.createCell(3);
		policyCell.setCellValue(policyCoverage.getPolicyID());
		Cell numberOfRulesCell = row.createCell(4);
		numberOfRulesCell.setCellValue(policyCoverage.getNumberOfRules());
		Cell coveredRulesCell = row.createCell(5);
		coveredRulesCell.setCellValue(policyCoverage.getRuleCoverages().size());
		Cell decisionCell = row.createCell(6);
		decisionCell.setCellValue(policyCoverage.getDecision());
		Cell targetMatchResultCell = row.createCell(7);
		targetMatchResultCell.setCellValue(policyCoverage.getTargetMatchResult());				
		int columnIndex = 8;
		for (RuleCoverage ruleCoverage: policyCoverage.getRuleCoverages()){
			Cell resultCell = row.createCell(columnIndex++);
			resultCell.setCellValue(ruleCoverage.getRuleResult());
			Cell targetCell = row.createCell(columnIndex++);
			targetCell.setCellValue(ruleCoverage.getTargetResult().toString());
			Cell conditionCell = row.createCell(columnIndex++);
			conditionCell.setCellValue(ruleCoverage.getConditionResult().toString());
		}
	}
	
}
