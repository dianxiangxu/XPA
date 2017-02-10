package org.seal.semanticCoverage;

public class TargetCoverage extends Coverage {
	private TargetMatchResult matchResult;

	public TargetCoverage(int matchResult) {
		this.matchResult = TargetMatchResult.getTargetMatchResult(matchResult);
	}

	public TargetMatchResult getMatchResult() {
		return matchResult;
	}

	public enum TargetMatchResult {
		MATCH, NO_MATCH, INDETERMINATE;

		public static TargetMatchResult getTargetMatchResult(int value) {
			switch (value) {
				case 0:
					return MATCH;
				case 1:
					return NO_MATCH;
				case 2:
					return INDETERMINATE;
				default:
					throw new RuntimeException("wrong MatchResult");
			}
		}
	}

}
