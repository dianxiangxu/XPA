
package org.seal.faultlocalization;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpectrumBasedDiagnosisResults {
	public static enum DebuggingStyle {TOPDOWN, BOTTOMUP};
	public static enum ScoreType {COUNT, PERCENTAGE};

	public DebuggingStyle debuggingStyle = null;
//	public DebuggingStyle debuggingStyle = DebuggingStyle.TOPDOWN;
	public ScoreType scoreType = ScoreType.COUNT;
	private double score;
	private String methodName;
	private List<Integer> ruleIndexRankedBySuspicion;
	private PolicyElementCoefficient[] ruleCoefficients;
	/**
	 * @return the rule indexes ranked by their suspicion
	 */
	public List<Integer> getRuleIndexRankedBySuspicion() {
		return ruleIndexRankedBySuspicion;
	}

	private void init(String methodName, double[] s) {
		this.methodName = methodName;
		PolicyElementCoefficient[] ruleCoefficients = new PolicyElementCoefficient[s.length];
		for (int index=0; index<s.length; index++) {
			ruleCoefficients[index] = new PolicyElementCoefficient(s[index], index);
		}
		Arrays.sort(ruleCoefficients);
		rankSuspicion(ruleCoefficients);
		ruleIndexRankedBySuspicion = new ArrayList<Integer>();
		for(PolicyElementCoefficient coefficient: ruleCoefficients) {
			ruleIndexRankedBySuspicion.add(coefficient.getElementIndex());
		}
		this.ruleCoefficients = ruleCoefficients;
	}
	
	public SpectrumBasedDiagnosisResults(String methodName, double[] s){
		init(methodName, s);
	}
	/**
	 * call other methods to set the score
	 * @param methodName
	 * @param s, an array of coefficient of rules
	 * @param bugPositions
	 */
	public SpectrumBasedDiagnosisResults(String methodName, double[] s,
			int[] bugPositions) {
		init(methodName, s);
		double[] scores = new double[bugPositions.length];
		for (int i = 0; i < bugPositions.length; i++) {
			int bugPosition = bugPositions[i];
			if (bugPosition >= 0) {//how to deal with the case when bugPosition == -1?
				if (scoreType == ScoreType.PERCENTAGE)
					scores[i] = percentageOfRulesInspected(bugPosition, ruleCoefficients);
				else {
					if (debuggingStyle == null) {
						scores[i] = numberOfRulesInspected(bugPosition, ruleCoefficients);
					} else {
						if (debuggingStyle == DebuggingStyle.TOPDOWN)
							scores[i] = topdownDebuggingSaving(bugPosition, ruleCoefficients);
						else
							scores[i] = bottomupDebuggingSaving(bugPosition, ruleCoefficients);
					}
				}
			}
		}
		score = 0;
		for (double item: scores) {
			score += item;
		}
		score /= scores.length;
	}

	/**
	 * set rank for each element in ruleCoefficients
	 * note that if two element in ruleCoefficients have almost the same coefficient,
	 * set their rank as the same. For example, [0.9, 0.9, 0.8, 0.7, 0.7] will have rank
	 * [2, 2, 3, 5, 5]. The rank is not [1, 1, 3, 4, 4] because we want to know worst 
	 * case performance.
	 */
	private void rankSuspicion(PolicyElementCoefficient[] ruleCoefficients){
		//worst case ranking
		ruleCoefficients[ruleCoefficients.length-1].setRank(ruleCoefficients.length);
		for (int index=ruleCoefficients.length-2; index>=0; index--) {
			if (ruleCoefficients[index].approximateEqual(ruleCoefficients[index+1]))
				ruleCoefficients[index].setRank(ruleCoefficients[index+1].getRank());
			else 
				ruleCoefficients[index].setRank(index+1);
		}
//		//best case ranking
//		ruleCoefficients[0].setRank(1);
//		for (int index = 1; index < ruleCoefficients.length; index++) {
//			if (ruleCoefficients[index].approximateEqual(ruleCoefficients[index - 1]))
//				ruleCoefficients[index].setRank(ruleCoefficients[index - 1].getRank());
//			else
//				ruleCoefficients[index].setRank(index + 1);
//		}
	}
	/**
	 * set score for scoreType == ScoreType.PERCENTAGE
	 * @param bugPosition
	 * @return 
	 */
	private double percentageOfRulesInspected(int bugPosition, PolicyElementCoefficient[] ruleCoefficients){
		for (int index=0; index < ruleCoefficients.length; index++){
			if (ruleCoefficients[index].getElementIndex()==bugPosition) { 
				return ((double)(ruleCoefficients[index].getRank()))/ruleCoefficients.length;
			}
		}
		return 0;
	}
	/**
	 * set score for scoreType == ScoreType.COUNT and debuggingStyle ==null
	 * @param bugPosition
	 */
	private int numberOfRulesInspected(int bugPosition, PolicyElementCoefficient[] ruleCoefficients){
		for (int index=0; index < ruleCoefficients.length; index++){
			if (ruleCoefficients[index].getElementIndex()==bugPosition) { 
				return ruleCoefficients[index].getRank();
			}
		}
		return 0;
	}

	private double bottomupDebuggingSaving(int bugPosition, PolicyElementCoefficient[] ruleCoefficients){
		for (int index=0; index < ruleCoefficients.length; index++){
			if (ruleCoefficients[index].getElementIndex()==bugPosition) { 
				return (ruleCoefficients.length - bugPosition+1) - ruleCoefficients[index].getRank() ;
			}
		}
		return 0;
	}

	private double topdownDebuggingSaving(int bugPosition, PolicyElementCoefficient[] ruleCoefficients){
		for (int index=0; index < ruleCoefficients.length; index++){
			if (ruleCoefficients[index].getElementIndex()==bugPosition) { 
				return bugPosition - ruleCoefficients[index].getRank();
			}
		}
		return 0;
	}

	
/*	
	private void calculateAccuracy(int bugPosition){
		for (int index=0; index < results.length; index++){
			if (results[index].getRuleIndex()==bugPosition-1) { 
				int first = index;
				while (first>0 && approximateEqual(results[first].getCoefficient(),results[first-1].getCoefficient())){
					first--;
				}
				int last = index;
				while (last<results.length-1 && approximateEqual(results[last].getCoefficient(),results[last+1].getCoefficient())){
					last++;
				}
				accuracy += ((double)(first+last))/2.0;
//System.out.println("\nCalculated accuracy: "+accuracy);				
// an alternative: 
// accuracy = begin;				
				return;
			}
		}
	}
*/
	
	public double getScore(){
		return score;
	}
	
	public String getMethodName(){
		return methodName;
	}

	public void printCoefficients(){
		System.out.println(methodName);
		for (PolicyElementCoefficient coefficient: this.ruleCoefficients) {
			System.out.println("rule " + coefficient.getElementIndex() + " : " + coefficient.getCoefficient());
		}
		System.out.println();
	}
		
}
