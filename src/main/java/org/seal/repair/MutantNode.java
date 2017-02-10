package org.seal.repair;

import java.util.List;

import org.seal.mutation.PolicyMutant;

public class MutantNode implements Comparable<MutantNode> {
	private PolicyMutant mutant;
	private String testSuiteFile;
	private String faultLocalizaMethod;
	private MutantNode parent;
	private int totalRank;
	private int layer;
	private List<Boolean> testResult;
	
	MutantNode(MutantNode parent, PolicyMutant mutant, String testSuiteFile, String faultLocalizaMethod, int rank, int layer) {
		this.setMutant(mutant);
		this.testSuiteFile = testSuiteFile;
		this.faultLocalizaMethod = faultLocalizaMethod;
		this.parent = parent;
		if (parent != null)
			this.totalRank = parent.getTotalRank() + rank;
		else
			this.totalRank = rank;
		this.layer = layer;
	}
	
	int getLayer() {
		return layer;
	}

	PolicyMutant getMutant() {
		return mutant;
	}

	void setMutant(PolicyMutant mutant) {
		this.mutant = mutant;
	}
	
	List<Boolean> getTestResult() throws Exception {
		if (testResult == null)
			testResult = PolicyRepairer.getTestResults(testSuiteFile, mutant.getMutantFilePath());
		return testResult;
	}
	
	/**
	 * make sure to run tests before calling this method
	 */
	List<Integer> getSuspicionRank() throws Exception {
		if (faultLocalizaMethod.equals("random")) {
			return PolicyRepairer.getRandomSuspicionRank(mutant);
		}
		return PolicyRepairer.getSuspicionRank(mutant, faultLocalizaMethod);
	}


	@Override
	public int compareTo(MutantNode other) {
		return this.getTotalRank() - other.getTotalRank();
	}
	
	@Override
	public boolean equals(Object obj) {
		MutantNode other = (MutantNode) obj;
		return this.getTotalRank() == other.getTotalRank();
	}

	int getTotalRank() {
		return totalRank;
	}
	
	boolean isPromising() throws Exception {
		if (parent == null)
			return true;
		return failedTestsIsSubset();
	}
	
	private boolean failedTestsIsSubset() throws Exception {
		List<Boolean> result = getTestResult();
		List<Boolean> parentResult = parent.getTestResult();
		for (int i = 0; i < result.size(); i++) 
			if (!result.get(i) && parentResult.get(i))
				return false;
		return true;
	}
	
	void clear() {
		mutant.clear();
	}
}
