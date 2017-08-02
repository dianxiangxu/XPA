package org.seal.testGeneration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;
import org.seal.xacml.NameDirectory;
import org.seal.xacml.TestSuiteDemo;
import org.seal.xacml.coverage.RuleCoverage;
import org.seal.xacml.utils.PropertiesLoader;
import org.umu.editorXacml3.PolicyEditorPanelDemo;

public class RuleCoverageTestGeneratorTest {

	@Test
	public void testGenerateTests() throws Exception {
		Demo demo = new Demo();
		String[] policies = {"conference3","fedora-rule3","itrust3","kmarket-blue-policy","kmarket-gold-policy","kmarket-sliver-policy","obligation3","pluto3","HL7"};
		RuleCoverage requestGenerator;
		for(int l = 0; l < policies.length;l++){
			String policyPath = System.getProperty("user.dir")+"/Experiments/"+ policies[l]+"/"+ policies[l] +".xml";
			PolicyEditorPanelDemo policyEditor = new PolicyEditorPanelDemo();
			policyEditor.openFile(policyPath);
			demo.setEditorPanel(policyEditor);
			requestGenerator = new RuleCoverage(policyPath);
			TestSuiteDemo testSuite = new TestSuiteDemo(policyPath,requestGenerator.generateRequests(),NameDirectory.MUTATION_BASED_TEST);
			testSuite.save();
			
			File requestsFolder = new File(System.getProperty("user.dir")+"/src/test/resources/org/seal/policies/"+ policies[l]+"/test_suites/"+ policies[l]+"_Exclusive");
		
			File[] requests = requestsFolder.listFiles();
			for(int i = 0; i < requests.length;i++){
				File requestFile = requests[i];
				String[] tokens =requests[i].toString().split("/");
				File generatedFile =  new File(System.getProperty("user.dir")+"/Experiments/"+ policies[l] + "/" + policies[l]+ PropertiesLoader.getProperties("config").getProperty("testSuitesBaseFolderNameSuffix")+"/"+ PropertiesLoader.getProperties("config").getProperty("ruleCoverageName")+PropertiesLoader.getProperties("config").getProperty("testSuiteFolderNameSuffix") +"/"+tokens[tokens.length-1]);
				String control = new String(Files.readAllBytes(Paths.get(requestFile.toString())));
				String test = new String(Files.readAllBytes(Paths.get(generatedFile.toString())));
				XMLAssert.assertXMLEqual("generated xml not similar to control xml", control, test);    	
			}
		}
	}
}
