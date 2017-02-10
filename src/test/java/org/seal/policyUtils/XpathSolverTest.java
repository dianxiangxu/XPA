package org.seal.policyUtils;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.balana.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

/**
 * tests {@link XpathSolver}
 * Created by shuaipeng on 10/20/16.
 */
public class XpathSolverTest {
    @Test
    public void getEntryListAbsoluteXPathTest() throws ParserConfigurationException, IOException, SAXException, ParsingException {
//        String fileName = "org/seal/policies/conference3/conference3.xml";
        String fileName = "org/seal/policies/HL7/HL7.xml";
        ClassLoader classLoader = XpathSolver.class.getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        // by load the policy and then encode it back to string, we replace the namespace declaration with default namespace declaration
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        InputStream stream = IOUtils.toInputStream(policy.encode(), Charset.defaultCharset());
        Document doc = PolicyLoader.getDocument(stream);
        List<String> list = XpathSolver.getEntryListAbsoluteXPath(doc);
//        for (String entry: list)
//            System.out.println("\"" + entry + "\",");

        String[] expected = new String[]{
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='Target' and 1]",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes']/*[local-name()='Target' and 1]",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes']/*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes.createNote']",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes']/*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes.updateNote']",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes']/*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes.safetyHarness']",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.assessments']/*[local-name()='Target' and 1]",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.assessments']/*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.assessments.createAssessment']",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.assessments']/*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.assessments.safetyHarness']",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.paymentHistory']/*[local-name()='Target' and 1]",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.paymentHistory']/*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.paymentHistory.readPaymentHistory']",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.paymentHistory']/*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.paymentHistory.safetyHarness']",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords']/*[local-name()='Target' and 1]",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.physiciansAccessMedicalRecords']/*[local-name()='Target' and 1]",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.physiciansAccessMedicalRecords']/*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.physiciansAccessMedicalRecords.readMedicalRecord']",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.physiciansAccessMedicalRecords']/*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.physiciansAccessMedicalRecords.explicitDenyAccess']",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.admissionClerkMedicalRecordAccess']/*[local-name()='Target' and 1]",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.admissionClerkMedicalRecordAccess']/*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.admissionClerkMedicalRecordAccess.withAdditionalAuthority']",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.healthRelatedProfessionalAccess']/*[local-name()='Target' and 1]",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.healthRelatedProfessionalAccess']/*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.healthRelatedProfessionalAccess.accessMedicalRecord']",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess']/*[local-name()='Target' and 1]",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess']/*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess.alternateRead']",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess']/*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess.alternateUpdate']",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess']/*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess.safetyHarness']",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.updateCarePlan']/*[local-name()='Target' and 1]",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.updateCarePlan']/*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.updateCarePlan.Id_10']",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.updateCarePlan']/*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.updateCarePlan.safetyHarness']",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess']/*[local-name()='Target' and 1]",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess']/*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess.clinicalObjectAccess']",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess']/*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess.billingStatementAccess']",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess']/*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess.safetyHarness']"
        };

        Assert.assertArrayEquals(list.toArray(), expected);
    }

    @Test
    public void getEntryListRelativeXPathTestConference3() throws ParserConfigurationException, IOException, SAXException, ParsingException {
        String fileName = "org/seal/policies/conference3/conference3.xml";

        String[] expected = new String[]{
                "//*[local-name()='Policy' and @PolicyId='conference']/*[local-name()='Target' and 1]",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule0']",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule1']",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule2']",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule3']",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule4']",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule5']",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule6']",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule7']",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule8']",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule9']",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule10']",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule11']",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule12']",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule13']",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule14']"
        };

        getEntryListRelativeXPathTest(fileName, expected);
    }

    @Test
    public void getEntryListRelativeXPathTestHL7() throws ParserConfigurationException, IOException, SAXException, ParsingException {
        String fileName = "org/seal/policies/HL7/HL7.xml";

        String[] expected = new String[]{
                "//*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='Target' and 1]",
                "//*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes']/*[local-name()='Target' and 1]",
                "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes.createNote']",
                "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes.updateNote']",
                "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes.safetyHarness']",
                "//*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.assessments']/*[local-name()='Target' and 1]",
                "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.assessments.createAssessment']",
                "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.assessments.safetyHarness']",
                "//*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.paymentHistory']/*[local-name()='Target' and 1]",
                "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.paymentHistory.readPaymentHistory']",
                "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.paymentHistory.safetyHarness']",
                "//*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords']/*[local-name()='Target' and 1]",
                "//*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.physiciansAccessMedicalRecords']/*[local-name()='Target' and 1]",
                "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.physiciansAccessMedicalRecords.readMedicalRecord']",
                "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.physiciansAccessMedicalRecords.explicitDenyAccess']",
                "//*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.admissionClerkMedicalRecordAccess']/*[local-name()='Target' and 1]",
                "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.admissionClerkMedicalRecordAccess.withAdditionalAuthority']",
                "//*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.healthRelatedProfessionalAccess']/*[local-name()='Target' and 1]",
                "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.medicalRecords.healthRelatedProfessionalAccess.accessMedicalRecord']",
                "//*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess']/*[local-name()='Target' and 1]",
                "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess.alternateRead']",
                "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess.alternateUpdate']",
                "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.alternatePrivilegedHealthcareProfessionalAccess.safetyHarness']",
                "//*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.updateCarePlan']/*[local-name()='Target' and 1]",
                "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.updateCarePlan.Id_10']",
                "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.updateCarePlan.safetyHarness']",
                "//*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess']/*[local-name()='Target' and 1]",
                "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess.clinicalObjectAccess']",
                "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess.billingStatementAccess']",
                "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess.safetyHarness']"
        };

        getEntryListRelativeXPathTest(fileName, expected);
    }

    private void getEntryListRelativeXPathTest(String fileName, String[] expected) throws ParserConfigurationException, IOException, SAXException, ParsingException {
        ClassLoader classLoader = XpathSolver.class.getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        // by load the policy and then encode it back to string, we replace the namespace declaration with default namespace declaration
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        InputStream stream = IOUtils.toInputStream(policy.encode(), Charset.defaultCharset());
        Document doc = PolicyLoader.getDocument(stream);
        List<String> list = XpathSolver.getEntryListRelativeXPath(doc);
//        for (String entry: list)
//            System.out.println("\"" + entry + "\",");
        Assert.assertArrayEquals(list.toArray(), expected);
    }

    @Test
    public void evaluateXpathTestConference3() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, ParsingException {
        String fileName = "org/seal/policies/conference3/conference3.xml";
        List<String> xpathList = Arrays.asList(
                "//*[local-name()='Policy' and @PolicyId='conference']/*[local-name()='Target' and 1]",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule0']",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule1']",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule2']",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule3']",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule4']",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule5']",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule6']",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule7']",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule8']",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule9']",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule10']",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule11']",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule12']",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule13']",
                "//*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule14']"
        );
        List<String> expectedXpathList = Arrays.asList(
                "/*[local-name()='Policy' and @PolicyId='conference']/*[local-name()='Target' and 1]",
                "/*[local-name()='Policy' and @PolicyId='conference']/*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule0']",
                "/*[local-name()='Policy' and @PolicyId='conference']/*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule1']",
                "/*[local-name()='Policy' and @PolicyId='conference']/*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule2']",
                "/*[local-name()='Policy' and @PolicyId='conference']/*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule3']",
                "/*[local-name()='Policy' and @PolicyId='conference']/*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule4']",
                "/*[local-name()='Policy' and @PolicyId='conference']/*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule5']",
                "/*[local-name()='Policy' and @PolicyId='conference']/*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule6']",
                "/*[local-name()='Policy' and @PolicyId='conference']/*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule7']",
                "/*[local-name()='Policy' and @PolicyId='conference']/*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule8']",
                "/*[local-name()='Policy' and @PolicyId='conference']/*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule9']",
                "/*[local-name()='Policy' and @PolicyId='conference']/*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule10']",
                "/*[local-name()='Policy' and @PolicyId='conference']/*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule11']",
                "/*[local-name()='Policy' and @PolicyId='conference']/*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule12']",
                "/*[local-name()='Policy' and @PolicyId='conference']/*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule13']",
                "/*[local-name()='Policy' and @PolicyId='conference']/*[local-name()='Rule' and @RuleId='urn:oasis:names:tc:xacml:1.0:Rule14']"
        );
        evaluateXpathTest(fileName, xpathList, expectedXpathList);
    }

    @Test
    public void evaluateXpathTestHL7() throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, ParsingException {
        String fileName = "org/seal/policies/HL7/HL7.xml";
        List<String> xpathList = Arrays.asList(
                "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess.billingStatementAccess']",
                "//*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes']/*[local-name()='Target' and 1]"
        );
        List<String> expectedXpathList = Arrays.asList(
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess']/*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess.billingStatementAccess']",
                "/*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='Policy' and @PolicyId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes']/*[local-name()='Target' and 1]"
        );
        evaluateXpathTest(fileName, xpathList, expectedXpathList);
    }

    private void evaluateXpathTest(String fileName, List<String> xpathList, List<String> expectedXpathList) throws ParserConfigurationException, IOException, SAXException, XPathExpressionException, ParsingException {
        ClassLoader classLoader = XpathSolver.class.getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        // by load the policy and then encode it back to string, we replace the namespace declaration with default namespace declaration
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        InputStream stream = IOUtils.toInputStream(policy.encode(), Charset.defaultCharset());
        Document doc = PolicyLoader.getDocument(stream);

        //Evaluate XPath against Document itself
        XPath xPath = XPathFactory.newInstance().newXPath();
        for (int i = 0; i < xpathList.size(); i++) {
            String xpathString = xpathList.get(i);
            String expectedXPathString = expectedXpathList.get(i);
            // get node by xpath string
            NodeList nodes = (NodeList) xPath.evaluate(xpathString,
                    doc.getDocumentElement(), XPathConstants.NODESET);
            // the xpath should identify a unique node
            Assert.assertEquals(1, nodes.getLength());
            Node node = nodes.item(0);
//            System.out.println(XpathSolver.nodeToString(node, false, true));
            // get xpath of node
//            System.out.println("\"" + XpathSolver.buildNodeXpath(node) + "\",");
            Assert.assertEquals(expectedXPathString, XpathSolver.buildNodeXpath(node));
        }
    }

    @Test
    public void buildXPathForRuleTest() throws ParserConfigurationException, IOException, SAXException, ParsingException {
        String ruleString = "<?xml version='1.0' encoding='UTF-8'?><Rule Effect='Deny' RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess.billingStatementAccess'>\n" +
                "    <Description>Deny Billing Statement access</Description>\n" +
                "    <Target>\n" +
                "        <AnyOf>\n" +
                "            <AllOf>\n" +
                "                <Match MatchId='urn:oasis:names:tc:xacml:1.0:function:string-equal'>\n" +
                "                    <AttributeValue DataType='http://www.w3.org/2001/XMLSchema#string'>Billing Statement</AttributeValue>\n" +
                "                    <AttributeDesignator AttributeId='com.axiomatics.hl7.object.objectType' Category='urn:oasis:names:tc:xacml:3.0:attribute-category:resource' DataType='http://www.w3.org/2001/XMLSchema#string' MustBePresent='false'/>\n" +
                "                </Match>\n" +
                "            </AllOf>\n" +
                "        </AnyOf>\n" +
                "    </Target>\n" +
                "</Rule>";
        InputStream stream = IOUtils.toInputStream(ruleString, Charset.defaultCharset());
        Document doc = PolicyLoader.getDocument(stream);
        Rule rule = Rule.getInstance(doc.getDocumentElement(), new PolicyMetaData(XACMLConstants.XACML_1_0_IDENTIFIER,
                null), null);
        String xpathString = XpathSolver.buildRuleXpath(rule);
        String expected = "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess.billingStatementAccess']";
        Assert.assertEquals(expected, xpathString);
    }
}
