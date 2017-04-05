package org.seal.testGeneration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;
import org.seal.gui.TestPanelDemo;
import org.umu.editorXacml3.PolicyEditorPanelDemo;

public class RuleCoverageTestGeneratorTest {

	@Test
	public void testGenerateTests() throws Exception {
		Demo demo = new Demo();
		TestPanelDemo testPanelDemo = new TestPanelDemo(demo);
		String[] policies = {"conference3","fedora-rule3","itrust3","kmarket-blue-policy","kmarket-gold-policy","kmarket-sliver-policy","obligation3","pluto3","HL7"};
		for(int l = 0; l < policies.length;l++){
			String conference3 = System.getProperty("user.dir")+"/Experiments/"+ policies[l]+"/"+ policies[l] +".xml";
			PolicyEditorPanelDemo policyEditor = new PolicyEditorPanelDemo();
			policyEditor.openFile(conference3);
			demo.setEditorPanel(policyEditor);
			RuleCoverageTestGenerator.generateTests(testPanelDemo, conference3);
			File conference3RequestsFolder = new File(System.getProperty("user.dir")+"/src/test/resources/org/seal/policies/"+ policies[l]+"/test_suites/"+ policies[l]+"_Exclusive");
		
			File[] requests = conference3RequestsFolder.listFiles();
			for(int i = 0; i < requests.length;i++){
				File requestFile = requests[i];
				String[] tokens =requests[i].toString().split("/");
				File generatedFile =  new File(System.getProperty("user.dir")+"/Experiments/"+ policies[l]+"/test_suites/"+ policies[l]+"_Exclusive/"+tokens[tokens.length-1]);
				String control = new String(Files.readAllBytes(Paths.get(requestFile.toString())));
				String test = new String(Files.readAllBytes(Paths.get(generatedFile.toString())));
				XMLAssert.assertXMLEqual("generated xml not similar to control xml", control, test);    	
			}
		}
	}
}
