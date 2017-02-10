package org.seal.faultlocalization;

// A test result in excel cell

public class TestCellResult {
	
	boolean verdict; // true for pass, false for fail
	String literalDetail; // (Pass/Fail) + Oracle/Actual
	
	public TestCellResult() {
		verdict = false;
		literalDetail = "";
	}
	
	public boolean getVerdict() {
		return verdict;
	}
	
	public String getLiteralDetail() {
		return literalDetail;
	}
	
	public void setVerdict(boolean verdict) {
		this.verdict = verdict;
	}

	public void setLiteralDetail(String literalDetail) {
		this.literalDetail = literalDetail;
	}
	
}
