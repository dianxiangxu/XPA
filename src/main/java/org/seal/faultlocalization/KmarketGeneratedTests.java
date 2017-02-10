package org.seal.faultlocalization;
import static org.junit.Assert.*;
import org.seal.coverage.PolicyRunner;
import org.junit.Test;

public class KmarketGeneratedTests {
	PolicyRunner policyRunner;

	public KmarketGeneratedTests(){
		try{
			policyRunner = new PolicyRunner("Experiments//kmarket-blue-policy//mutants//kmarket-blue-policy_RER1.xml");
		}
		catch (Exception e){}
	}

	@Test
	public void test1()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 1", "request1_old.txt", "Permit"));
	}

	@Test
	public void test2()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 2", "request2_old.txt", "Deny"));
	}

	@Test
	public void test3()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 3", "request3_old.txt", "Deny"));
	}

	@Test
	public void test4()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 4", "request4_old.txt", "Deny"));
	}

	@Test
	public void test5()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 5", "request5_old.txt", "Deny"));
	}

}