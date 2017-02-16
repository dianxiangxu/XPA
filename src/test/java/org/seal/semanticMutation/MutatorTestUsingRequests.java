package org.seal.semanticMutation;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.seal.policyUtils.PolicyLoader;
import org.seal.semanticCoverage.TestSuite;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.ParsingException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;

/**
 * Unit tests in this class shall test the mutation operators by examining their response to requests instead of inspecting
 * the elements in their XML documents.
 * Created by shuaipeng on 2/16/17.
 */
public class MutatorTestUsingRequests {
    /**
     * Given the correct policy HL7.xml, XACML engine will return a decision "Permit". We apply the RulEffectFlipping
     * mutation operator on the rule clinicalObjectAccess, creating a mutant. Given this mutant as input policy, the
     * XACML engine should return "Deny".
     */
    @Test
    public void createRuleEffectFlippingMutantsTest() throws IOException, SAXException, ParserConfigurationException, ParsingException, XPathExpressionException {
        File file = new File("src/test/resources/org/seal/policies/HL7/HL7.xml");
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        Mutator mutator = new Mutator(new Mutant(policy, ""));
        String ruleXpathString = "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess.clinicalObjectAccess']";
        List<Mutant> mutants = mutator.createRuleEffectFlippingMutants(ruleXpathString);
        Assert.assertEquals(1, mutants.size());
        Mutant mutant = mutants.get(0);
        //TODO after writing a util method to generate requests, we can generate requests on the fly instead of reading from hard drive
        String requestFilePath = "org/seal/policies/HL7/test_suites/manual/clinicalObjectAccess.xml";
        ClassLoader classLoader = getClass().getClassLoader();
        String request = IOUtils.toString(classLoader.getResourceAsStream(requestFilePath), Charset.defaultCharset());
        TestSuite testSuite = new TestSuite(Collections.singletonList(""), Collections.singletonList(request), Collections.singletonList("Deny"));
        List<Boolean> results = testSuite.runTests(mutant);
        for (Boolean res : results) {
            Assert.assertTrue(res);
        }
    }
}
