package org.seal.coverage;
import static org.junit.Assert.*;

import org.junit.Test;

public class KmarketGeneratedTests {
	PolicyRunner policyRunner;

	public KmarketGeneratedTests(){
		try{
			policyRunner = new PolicyRunner("Experiments//conference3//mutants//conference3_ANR4.xml");
		}
		catch (Exception e){}
	}

	@Test
	public void test1()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 1", "request1.txt", "Deny"));
	}

	@Test
	public void test2()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 2", "request2.txt", "Deny"));
	}

	@Test
	public void test3()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 3", "request3.txt", "Deny"));
	}

	@Test
	public void test4()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 4", "request4.txt", "Permit"));
	}

	@Test
	public void test5()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 5", "request5.txt", "Deny"));
	}

	@Test
	public void test6()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 6", "request6.txt", "Permit"));
	}

	@Test
	public void test7()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 7", "request7.txt", "Deny"));
	}

	@Test
	public void test8()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 8", "request8.txt", "Permit"));
	}

	@Test
	public void test9()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 9", "request9.txt", "Permit"));
	}

	@Test
	public void test10()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 10", "request10.txt", "Deny"));
	}

	@Test
	public void test11()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 11", "request11.txt", "Permit"));
	}

	@Test
	public void test12()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 12", "request12.txt", "Permit"));
	}

	@Test
	public void test13()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 13", "request13.txt", "Deny"));
	}

	@Test
	public void test14()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 14", "request14.txt", "Permit"));
	}

	@Test
	public void test15()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 15", "request15.txt", "Permit"));
	}

	@Test
	public void test16()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 16", "request16.txt", "Deny"));
	}

	@Test
	public void test17()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 17", "request17.txt", "Permit"));
	}

	@Test
	public void test18()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 18", "request18.txt", "Permit"));
	}

	@Test
	public void test19()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 19", "request19.txt", "Deny"));
	}

	@Test
	public void test20()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 20", "request20.txt", "Permit"));
	}

	@Test
	public void test21()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 21", "request21.txt", "Permit"));
	}

	@Test
	public void test22()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 22", "request22.txt", "Deny"));
	}

	@Test
	public void test23()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 23", "request23.txt", "Permit"));
	}

	@Test
	public void test24()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 24", "request24.txt", "Deny"));
	}

	@Test
	public void test25()  throws Exception {
		assertTrue(policyRunner.runTestFromFile("Test 25", "request25.txt", "Permit"));
	}

}