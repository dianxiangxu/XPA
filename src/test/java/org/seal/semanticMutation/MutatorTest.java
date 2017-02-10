package org.seal.semanticMutation;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.seal.policyUtils.PolicyLoader;
import org.seal.policyUtils.ReflectionUtils;
import org.seal.policyUtils.XpathSolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.ParsingException;
import org.xml.sax.SAXException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.ComparisonControllers;
import org.xmlunit.diff.Diff;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by shuaipeng on 12/1/16.
 */
public class MutatorTest {
    private XPath xPath = XPathFactory.newInstance().newXPath();
    private List<String> xpathList;
    private Mutator mutator;
    private Document doc;

    private boolean isRuleXpathString(String xPathString) {
        return xPathString.contains("[local-name()='Rule'");
    }

    private boolean isTargetXpathString(String xPathString) {
        return xPathString.contains("[local-name()='Target'");
    }

    @Before
    public void initialize() throws ParserConfigurationException, ParsingException, SAXException, IOException {
        File file = new File("src/test/resources/org/seal/policies/HL7/HL7.xml");
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        doc = PolicyLoader.getDocument(IOUtils.toInputStream(policy.encode(), Charset.defaultCharset()));
        xpathList = XpathSolver.getEntryListRelativeXPath(doc);
        mutator = new Mutator(new Mutant(policy, ""));
    }

    @After
    public void checkIfDocIsRestored() {
        // make sure the doc in Mutator is properly restored
        // compiler warnings like "" will appear, which is caused by a bug in JVM
        // there's little we can do about this
        // see http://stackoverflow.com/questions/25453042/how-to-disable-accessexternaldtd-and-entityexpansionlimit-warnings-with-logback
        isSameDoc(doc, (Document) ReflectionUtils.getField(mutator, "doc"));
    }

    private void isSameDoc(Document docBefore, Document docAfter) {
        Diff diff = DiffBuilder.compare(docBefore)
                .withTest(docAfter)
                .checkForSimilar()
                .ignoreComments()
                .ignoreWhitespace()
                .normalizeWhitespace()
                .withComparisonController(ComparisonControllers.StopWhenDifferent)
                .build();
        Assert.assertFalse(diff.toString(), diff.hasDifferences());
    }

    @Test
    public void createRuleEffectFlippingMutantsTest() throws XPathExpressionException, ParsingException, ParserConfigurationException, SAXException, IOException {
        for (String xpathString : xpathList) {
            if (isRuleXpathString(xpathString)) {
                NodeList nodes = (NodeList) xPath.evaluate(xpathString, doc.getDocumentElement(), XPathConstants.NODESET);
                Assert.assertEquals(1, nodes.getLength());
                Node node = nodes.item(0);
                String effect = node.getAttributes().getNamedItem("Effect").getTextContent();
                Assert.assertTrue(effect.equals("Permit") || effect.equals("Deny"));
                List<Mutant> mutants = mutator.createRuleEffectFlippingMutants(xpathString);
                Assert.assertEquals(1, mutants.size());
                Mutant mutant = mutants.get(0);
                Document newDoc = PolicyLoader.getDocument(IOUtils.toInputStream(mutant.encode(), Charset.defaultCharset()));
                nodes = (NodeList) xPath.evaluate(xpathString, newDoc.getDocumentElement(), XPathConstants.NODESET);
                Assert.assertEquals(1, nodes.getLength());
                node = nodes.item(0);
                String flippedEffect = node.getAttributes().getNamedItem("Effect").getTextContent();
                Assert.assertTrue(flippedEffect.equals("Permit") || flippedEffect.equals("Deny"));
                Assert.assertNotEquals(effect, flippedEffect);
            }
        }
    }

    @Test
    public void createPolicyTargetTrueMutantsTest() throws XPathExpressionException, ParsingException, ParserConfigurationException, SAXException, IOException {
        for (String xpathString : xpathList) {
            if (isTargetXpathString(xpathString)) {
//                System.out.println(xpathString);
                NodeList nodes = (NodeList) xPath.evaluate(xpathString, doc.getDocumentElement(), XPathConstants.NODESET);
                Assert.assertEquals(1, nodes.getLength());
                Node node = nodes.item(0);
//                System.out.println(XpathSolver.nodeToString(node, false, true));
                List<Mutant> mutants = mutator.createPolicyTargetTrueMutants(xpathString);
                if (!Mutator.isEmptyNode(node)) {
                    Assert.assertEquals(1, mutants.size());
                    Mutant mutant = mutants.get(0);
                    Document newDoc = PolicyLoader.getDocument(IOUtils.toInputStream(mutant.encode(), Charset.defaultCharset()));
                    nodes = (NodeList) xPath.evaluate(xpathString, newDoc.getDocumentElement(), XPathConstants.NODESET);
                    Assert.assertEquals(1, nodes.getLength());
                    Node newNode = nodes.item(0);
//                    System.out.println(XpathSolver.nodeToString(newNode, false, true));
                    Assert.assertTrue(Mutator.isEmptyNode(newNode));
                } else {
//                    System.out.println(XpathSolver.nodeToString(node, false, true));
                    Assert.assertEquals(0, mutants.size());
                }
//                System.out.println("===========");
            }
        }
    }

    @Test
    public void createRuleTargetTrueMutantsTest() throws XPathExpressionException, ParsingException, ParserConfigurationException, SAXException, IOException {
        for (String xpathString : xpathList) {
            if (isRuleXpathString(xpathString)) {
                String targetXpathString = xpathString + "/*[local-name()='Target' and 1]";
//                System.out.println(xpathString);
                NodeList nodes = (NodeList) xPath.evaluate(targetXpathString, doc.getDocumentElement(), XPathConstants.NODESET);
                Assert.assertEquals(1, nodes.getLength());
                Node node = nodes.item(0);
//                System.out.println(XpathSolver.nodeToString(node, false, true));
                List<Mutant> mutants = mutator.createRuleTargetTrueMutants(xpathString);
                if (!Mutator.isEmptyNode(node)) {
                    Assert.assertEquals(1, mutants.size());
                    Mutant mutant = mutants.get(0);
                    Document newDoc = PolicyLoader.getDocument(IOUtils.toInputStream(mutant.encode(), Charset.defaultCharset()));
                    nodes = (NodeList) xPath.evaluate(targetXpathString, newDoc.getDocumentElement(), XPathConstants.NODESET);
                    Assert.assertEquals(1, nodes.getLength());
                    Node newNode = nodes.item(0);
//                    System.out.println(XpathSolver.nodeToString(newNode, false, true));
                    Assert.assertTrue(Mutator.isEmptyNode(newNode));
                } else {
//                    System.out.println(XpathSolver.nodeToString(node, false, true));
                    Assert.assertEquals(0, mutants.size());
                }
//                System.out.println("===========");
            }
        }
    }

    @Test
    public void createRuleConditionTrueMutantsTest() throws XPathExpressionException, ParsingException, ParserConfigurationException, SAXException, IOException {
        for (String xpathString : xpathList) {
            if (isRuleXpathString(xpathString)) {
//                System.out.println(xpathString);
                Node ruleNode = ((NodeList) xPath.evaluate(xpathString, doc.getDocumentElement(), XPathConstants.NODESET)).item(0);
//                System.out.println(XpathSolver.nodeToString(ruleNode, false, true));
                String conditionXpathString = xpathString + "/*[local-name()='Condition' and 1]";
                Node conditionNode = ((NodeList) xPath.evaluate(conditionXpathString, doc.getDocumentElement(), XPathConstants.NODESET)).item(0);
                List<Mutant> mutants = mutator.createRuleConditionTrueMutants(xpathString);
                if (conditionNode != null && !Mutator.isEmptyNode(conditionNode)) {
                    Assert.assertEquals(1, mutants.size());
                    Mutant mutant = mutants.get(0);
                    Document newDoc = PolicyLoader.getDocument(IOUtils.toInputStream(mutant.encode(), Charset.defaultCharset()));
                    NodeList nodes = (NodeList) xPath.evaluate(conditionXpathString, newDoc.getDocumentElement(), XPathConstants.NODESET);
                    //because the way we make condition always true is to delete the Condition node
                    Assert.assertEquals(0, nodes.getLength());
                    ruleNode = ((NodeList) xPath.evaluate(xpathString, newDoc.getDocumentElement(), XPathConstants.NODESET)).item(0);
//                    System.out.println(XpathSolver.nodeToString(ruleNode, false, true));
                } else {
                    Assert.assertEquals(0, mutants.size());
//                    ruleNode = ((NodeList) xPath.evaluate(xpathString, doc.getDocumentElement(), XPathConstants.NODESET)).item(0);
//                    System.out.println(XpathSolver.nodeToString(ruleNode, false, true));
                }
//                System.out.println("===========");
            }
        }
    }

    @Test
    public void createRuleTargetFalseMutantsTest() throws XPathExpressionException, ParsingException, ParserConfigurationException, SAXException, IOException {
        for (String xpathString : xpathList) {
            if (isRuleXpathString(xpathString)) {
//                System.out.println(xpathString);
                List<Mutant> mutants = mutator.createRuleTargetFalseMutants(xpathString);
                String targetXpathString = xpathString + "/*[local-name()='Target' and 1]";
                createTargetFalseMutantsTest(targetXpathString, mutants);
            }
        }
    }

    @Test
    public void createPolicyTargetFalseMutantsTest() throws XPathExpressionException, ParsingException, ParserConfigurationException, SAXException, IOException {
        for (String xpathString : xpathList) {
            if (isTargetXpathString(xpathString)) {
//                System.out.println(xpathString);
                createTargetFalseMutantsTest(xpathString, mutator.createPolicyTargetFalseMutants(xpathString));
            }
        }
    }

    private void createTargetFalseMutantsTest(String targetXpathString, List<Mutant> mutants) throws XPathExpressionException, ParserConfigurationException, SAXException, IOException {
        NodeList nodes = (NodeList) xPath.evaluate(targetXpathString, doc.getDocumentElement(), XPathConstants.NODESET);
        Assert.assertEquals(1, nodes.getLength());
        Node node = nodes.item(0);
//        System.out.println(XpathSolver.nodeToString(node, false, true));
        if (!Mutator.isEmptyNode(node)) {
            Assert.assertEquals(1, mutants.size());
            Mutant mutant = mutants.get(0);
            Document newDoc = PolicyLoader.getDocument(IOUtils.toInputStream(mutant.encode(), Charset.defaultCharset()));
            nodes = (NodeList) xPath.evaluate(targetXpathString, newDoc.getDocumentElement(), XPathConstants.NODESET);
            Assert.assertEquals(1, nodes.getLength());
            Node newNode = nodes.item(0);
//            System.out.println(XpathSolver.nodeToString(newNode, false, true));
        } else {
//            System.out.println(XpathSolver.nodeToString(node, false, true));
            Assert.assertEquals(0, mutants.size());
        }
//        System.out.println("===========");
    }

    @Test
    public void createTargetFalseMutantsTestExamples() throws XPathExpressionException, ParsingException, ParserConfigurationException, SAXException, IOException {
        String ruleXpathString = "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes.createNote']";
        String targetXpathString = ruleXpathString + "/*[local-name()='Target' and 1]";
        NodeList nodes = (NodeList) xPath.evaluate(targetXpathString, doc.getDocumentElement(), XPathConstants.NODESET);
        Assert.assertEquals(1, nodes.getLength());
        Node originNode = nodes.item(0);
        String expectedOriginString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Target xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\"><AnyOf><AllOf><Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">physician</AttributeValue><AttributeDesignator AttributeId=\"com.axiomatics.hl7.user.role\" Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"false\"/></Match><Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">create</AttributeValue><AttributeDesignator AttributeId=\"com.axiomatics.hl7.action.id\" Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"false\"/></Match></AllOf></AnyOf></Target>";
        Assert.assertEquals(expectedOriginString, XpathSolver.nodeToStringTrimmed(originNode, false));

        List<Mutant> mutants = mutator.createRuleTargetFalseMutants(ruleXpathString);
        Assert.assertEquals(1, mutants.size());
        Mutant mutant = mutants.get(0);
        Document newDoc = PolicyLoader.getDocument(IOUtils.toInputStream(mutant.encode(), Charset.defaultCharset()));
        nodes = (NodeList) xPath.evaluate(targetXpathString, newDoc.getDocumentElement(), XPathConstants.NODESET);
        Assert.assertEquals(1, nodes.getLength());
        Node newNode = nodes.item(0);
        String expectedNewString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Target xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\"><AnyOf><AllOf><Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">physician</AttributeValue><AttributeDesignator AttributeId=\"com.axiomatics.hl7.user.role\" Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"false\"/></Match><Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">create</AttributeValue><AttributeDesignator AttributeId=\"com.axiomatics.hl7.action.id\" Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"false\"/></Match><Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">a</AttributeValue><AttributeDesignator AttributeId=\"com.axiomatics.hl7.user.role\" Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"false\"/></Match><Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">b</AttributeValue><AttributeDesignator AttributeId=\"com.axiomatics.hl7.user.role\" Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"false\"/></Match></AllOf></AnyOf></Target>";
        Assert.assertEquals(expectedNewString, XpathSolver.nodeToStringTrimmed(newNode, false));
    }

    @Test
    public void createRuleConditionFalseMutantsTest() throws ParserConfigurationException, ParsingException, SAXException, XPathExpressionException, IOException {
        for (String xpathString : xpathList) {
            if (isRuleXpathString(xpathString)) {
//                System.out.println(xpathString);
                NodeList nodes = (NodeList) xPath.evaluate(xpathString, doc.getDocumentElement(), XPathConstants.NODESET);
                Node node = nodes.item(0);
//                System.out.println(XpathSolver.nodeToString(node, false, true));
                String conditionXpathString = xpathString + "/*[local-name()='Condition' and 1]";
                nodes = (NodeList) xPath.evaluate(conditionXpathString, doc.getDocumentElement(), XPathConstants.NODESET);
                Node conditionNode = nodes.item(0);
                List<Mutant> mutants = mutator.createRuleConditionFalseMutants(xpathString);
                if (!Mutator.isEmptyNode(conditionNode)) {
                    Assert.assertEquals(1, mutants.size());
                    Mutant mutant = mutants.get(0);
                    Document newDoc = PolicyLoader.getDocument(IOUtils.toInputStream(mutant.encode(), Charset.defaultCharset()));
                    nodes = (NodeList) xPath.evaluate(xpathString, newDoc.getDocumentElement(), XPathConstants.NODESET);
                    Assert.assertEquals(1, nodes.getLength());
                    Node newNode = nodes.item(0);
//                    System.out.println(XpathSolver.nodeToString(newNode, false, true));
                } else {
//                    System.out.println(XpathSolver.nodeToString(node, false, true));
                    Assert.assertEquals(0, mutants.size());
                }
//                System.out.println("=================");
            }
        }
    }

    @Test
    public void createRuleConditionFalseMutantsTestExamples() throws XPathExpressionException, ParserConfigurationException, ParsingException, SAXException, IOException {
        String ruleXpathString = "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes.createNote']";
        NodeList nodes = (NodeList) xPath.evaluate(ruleXpathString, doc.getDocumentElement(), XPathConstants.NODESET);
        Assert.assertEquals(1, nodes.getLength());
        Node originNode = nodes.item(0);
        String expectedOriginString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Rule Effect=\"Permit\" RuleId=\"http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes.createNote\" xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\"><Description>A primary physician can create a patient's progress note</Description><Target><AnyOf><AllOf><Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">physician</AttributeValue><AttributeDesignator AttributeId=\"com.axiomatics.hl7.user.role\" Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"false\"/></Match><Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">create</AttributeValue><AttributeDesignator AttributeId=\"com.axiomatics.hl7.action.id\" Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"false\"/></Match></AllOf></AnyOf></Target><Condition><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:any-of-any\"><Function FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"/><AttributeDesignator AttributeId=\"com.axiomatics.hl7.patient.primaryPhysician\" Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"false\"/><AttributeDesignator AttributeId=\"com.axiomatics.hl7.user.requestorId\" Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"false\"/></Apply></Condition></Rule>";
        Assert.assertEquals(expectedOriginString, XpathSolver.nodeToStringTrimmed(originNode, false));
        List<Mutant> mutants = mutator.createRuleConditionFalseMutants(ruleXpathString);
        Assert.assertEquals(1, mutants.size());
        Mutant mutant = mutants.get(0);
        Document newDoc = PolicyLoader.getDocument(IOUtils.toInputStream(mutant.encode(), Charset.defaultCharset()));
        nodes = (NodeList) xPath.evaluate(ruleXpathString, newDoc.getDocumentElement(), XPathConstants.NODESET);
        Assert.assertEquals(1, nodes.getLength());
        Node newNode = nodes.item(0);
        String expectedNewString = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Rule Effect=\"Permit\" RuleId=\"http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes.createNote\" xmlns=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\"><Description>A primary physician can create a patient's progress note</Description><Target><AnyOf><AllOf><Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">physician</AttributeValue><AttributeDesignator AttributeId=\"com.axiomatics.hl7.user.role\" Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"false\"/></Match><Match MatchId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">create</AttributeValue><AttributeDesignator AttributeId=\"com.axiomatics.hl7.action.id\" Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"false\"/></Match></AllOf></AnyOf></Target><Condition><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:and\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><AttributeDesignator AttributeId=\"com.axiomatics.hl7.patient.primaryPhysician\" Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"true\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">a</AttributeValue></Apply><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-equal\"><Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:string-one-and-only\"><AttributeDesignator AttributeId=\"com.axiomatics.hl7.patient.primaryPhysician\" Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\" DataType=\"http://www.w3.org/2001/XMLSchema#string\" MustBePresent=\"true\"/></Apply><AttributeValue DataType=\"http://www.w3.org/2001/XMLSchema#string\">b</AttributeValue></Apply></Apply></Condition></Rule>";
        Assert.assertEquals(expectedNewString, XpathSolver.nodeToStringTrimmed(newNode, false));
    }

    @Test
    public void createPolicyTargetChangeComparisonFunctionMutantsTest() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException, ParsingException {
        for (String xpathString : xpathList) {
            if (isTargetXpathString(xpathString)) {
//                System.out.println(xpathString);
                NodeList nodes = (NodeList) xPath.evaluate(xpathString, doc.getDocumentElement(), XPathConstants.NODESET);
                Assert.assertEquals(1, nodes.getLength());
                Node node = nodes.item(0);
//                System.out.println(XpathSolver.nodeToString(node, false, true));
                List<Mutant> mutants = mutator.createPolicyTargetChangeComparisonFunctionMutants(xpathString);
                if (!Mutator.isEmptyNode(node)) {
                    for (Mutant mutant : mutants) {
                        Document newDoc = PolicyLoader.getDocument(IOUtils.toInputStream(mutant.encode(), Charset.defaultCharset()));
                        nodes = (NodeList) xPath.evaluate(xpathString, newDoc.getDocumentElement(), XPathConstants.NODESET);
                        Assert.assertEquals(1, nodes.getLength());
                        Node newNode = nodes.item(0);
//                        System.out.println(XpathSolver.nodeToString(newNode, false, true));
                    }
                } else {
//                    System.out.println(XpathSolver.nodeToString(node, false, true));
                    Assert.assertEquals(0, mutants.size());
                }
//                System.out.println("===========");
            }
        }
    }

    @Test
    public void createRuleChangeComparisonFunctionMutantsTest() throws XPathExpressionException, ParserConfigurationException, SAXException, IOException, ParsingException {
        for (String xpathString : xpathList) {
            if (isRuleXpathString(xpathString)) {
//                System.out.println(xpathString);
                NodeList nodes = (NodeList) xPath.evaluate(xpathString, doc.getDocumentElement(), XPathConstants.NODESET);
                Assert.assertEquals(1, nodes.getLength());
                Node node = nodes.item(0);
//                System.out.println(XpathSolver.nodeToString(node, false, true));
                List<Mutant> mutants = mutator.createRuleChangeComparisonFunctionMutants(xpathString);
                if (!Mutator.isEmptyNode(node)) {
                    for (Mutant mutant : mutants) {
                        Document newDoc = PolicyLoader.getDocument(IOUtils.toInputStream(mutant.encode(), Charset.defaultCharset()));
                        nodes = (NodeList) xPath.evaluate(xpathString, newDoc.getDocumentElement(), XPathConstants.NODESET);
                        Assert.assertEquals(1, nodes.getLength());
                        Node newNode = nodes.item(0);
//                        System.out.println(XpathSolver.nodeToString(newNode, false, true));
                    }
                } else {
//                    System.out.println(XpathSolver.nodeToString(node, false, true));
                    Assert.assertEquals(0, mutants.size());
                }
//                System.out.println("===========");
            }
        }
    }

    @Test
    public void createAddNotFunctionMutantsTest() throws XPathExpressionException, ParserConfigurationException, ParsingException, SAXException, IOException {
        for (String xpathString : xpathList) {
            if (isRuleXpathString(xpathString)) {
//                System.out.println(xpathString);
                NodeList nodes = (NodeList) xPath.evaluate(xpathString, doc.getDocumentElement(), XPathConstants.NODESET);
                Assert.assertEquals(1, nodes.getLength());
                Node node = nodes.item(0);
//                System.out.println(XpathSolver.nodeToString(node, false, true));
                List<Mutant> mutants = mutator.createAddNotFunctionMutants(xpathString);
                if (!Mutator.isEmptyNode(node)) {
                    for (Mutant mutant : mutants) {
                        Document newDoc = PolicyLoader.getDocument(IOUtils.toInputStream(mutant.encode(), Charset.defaultCharset()));
                        nodes = (NodeList) xPath.evaluate(xpathString, newDoc.getDocumentElement(), XPathConstants.NODESET);
                        Assert.assertEquals(1, nodes.getLength());
                        Node newNode = nodes.item(0);
//                        System.out.println(XpathSolver.nodeToString(newNode, false, true));
                    }
                } else {
//                    System.out.println(XpathSolver.nodeToString(node, false, true));
                    Assert.assertEquals(0, mutants.size());
                }
//                System.out.println("===========");
            }
        }
    }

    @Test
    public void createRemoveNotFunctionMutantsTest() throws XPathExpressionException, ParserConfigurationException, ParsingException, SAXException, IOException {
        //use HL7.notFunction.xml instead of HL7.xml because the later doesn't have not function in Condition elements
        File file = new File("src/test/resources/org/seal/policies/HL7/HL7.notFunction.xml");
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        //note that a local doc and mutator variable is used
        Document doc = PolicyLoader.getDocument(IOUtils.toInputStream(policy.encode(), Charset.defaultCharset()));
        Mutator mutator = new Mutator(new Mutant(policy, ""));
        for (String xpathString : xpathList) {
            if (isRuleXpathString(xpathString)) {
//                System.out.println(xpathString);
                //check if there the first Apply element's FunctionId is not function
                String notFunctionXpathString = xpathString + "/*[local-name()='Condition' and 1]/*[local-name()='Apply' and @FunctionId='urn:oasis:names:tc:xacml:1.0:function:not']";
                NodeList nodes = (NodeList) xPath.evaluate(xpathString, doc.getDocumentElement(), XPathConstants.NODESET);
//                Assert.assertEquals(1, nodes.getLength());
                Node node = nodes.item(0);
//                System.out.println(XpathSolver.nodeToString(node, false, true));
                List<Mutant> mutants = mutator.createRemoveNotFunctionMutants(xpathString);
                if (!Mutator.isEmptyNode(node)) {
                    for (Mutant mutant : mutants) {
                        Document newDoc = PolicyLoader.getDocument(IOUtils.toInputStream(mutant.encode(), Charset.defaultCharset()));
                        nodes = (NodeList) xPath.evaluate(xpathString, newDoc.getDocumentElement(), XPathConstants.NODESET);
                        Assert.assertEquals(1, nodes.getLength());
                        Node newNode = nodes.item(0);
//                        System.out.println(XpathSolver.nodeToString(newNode, false, true));
                    }
                } else {
//                    System.out.println(XpathSolver.nodeToString(node, false, true));
                    Assert.assertEquals(0, mutants.size());
                }
//                System.out.println("===========");
            }
        }
    }


    @Test
    public void createCombiningAlgorithmMutantsTest() throws XPathExpressionException, ParserConfigurationException, ParsingException, SAXException, IOException {
        for (String xpathString : xpathList) {
            if (isTargetXpathString(xpathString)) {
                String PolicyXpathString = xpathString.replace("/*[local-name()='Target' and 1]", "");
//                System.out.println(PolicyXpathString);
                Node node = ((NodeList) xPath.evaluate(PolicyXpathString, doc.getDocumentElement(), XPathConstants.NODESET)).item(0);
//                Node attributeNode = node.getAttributes().getNamedItem("PolicyCombiningAlgId") == null ? node.getAttributes().getNamedItem("RuleCombiningAlgId") : node.getAttributes().getNamedItem("PolicyCombiningAlgId");
//                System.out.println(attributeNode);
                List<Mutant> mutants = mutator.createCombiningAlgorithmMutants(xpathString);
                if (!Mutator.isEmptyNode(node)) {
                    for (Mutant mutant : mutants) {
                        Document newDoc = PolicyLoader.getDocument(IOUtils.toInputStream(mutant.encode(), Charset.defaultCharset()));
                        NodeList nodes = (NodeList) xPath.evaluate(PolicyXpathString, newDoc.getDocumentElement(), XPathConstants.NODESET);
                        Assert.assertEquals(1, nodes.getLength());
                        Node newNode = nodes.item(0);
//                        attributeNode = newNode.getAttributes().getNamedItem("PolicyCombiningAlgId") == null ? newNode.getAttributes().getNamedItem("RuleCombiningAlgId") : newNode.getAttributes().getNamedItem("PolicyCombiningAlgId");
//                        System.out.println(attributeNode);
                    }
                } else {
//                    attributeNode = node.getAttributes().getNamedItem("PolicyCombiningAlgId") == null ? node.getAttributes().getNamedItem("RuleCombiningAlgId") : node.getAttributes().getNamedItem("PolicyCombiningAlgId");
//                    System.out.println(attributeNode);
                    Assert.assertEquals(0, mutants.size());
                }
//                System.out.println("===========");
            }
        }
    }

    @Test
    public void createRemoveRuleMutantsTest() throws XPathExpressionException, ParsingException, ParserConfigurationException, SAXException, IOException {
        for (String xpathString : xpathList) {
            if (isRuleXpathString(xpathString)) {
//                System.out.println(xpathString);
                NodeList nodes = (NodeList) xPath.evaluate(xpathString, doc.getDocumentElement(), XPathConstants.NODESET);
                Assert.assertEquals(1, nodes.getLength());
                Node node = nodes.item(0);
//                System.out.println(XpathSolver.nodeToString(node, false, true));
                List<Mutant> mutants = mutator.createRemoveRuleMutants(xpathString);
                for (Mutant mutant : mutants) {
                    Document newDoc = PolicyLoader.getDocument(IOUtils.toInputStream(mutant.encode(), Charset.defaultCharset()));
                    nodes = (NodeList) xPath.evaluate(xpathString, newDoc.getDocumentElement(), XPathConstants.NODESET);
                    Assert.assertEquals(0, nodes.getLength());
                    Node newNode = nodes.item(0);
//                    System.out.println(newNode);
                }
//                System.out.println("===========");
            }
        }
    }

    @Test
    public void createAddNewRuleMutantsTest() throws XPathExpressionException, ParsingException, ParserConfigurationException, SAXException, IOException {
        for (String xpathString : xpathList) {
            if (isRuleXpathString(xpathString)) {
//                System.out.println(xpathString);
                NodeList nodes = (NodeList) xPath.evaluate(xpathString, doc.getDocumentElement(), XPathConstants.NODESET);
                Assert.assertEquals(1, nodes.getLength());
                Node node = nodes.item(0);
//                System.out.println(XpathSolver.nodeToString(node, false, true));
                List<Mutant> mutants = mutator.createAddNewRuleMutants(xpathString);
                for (Mutant mutant : mutants) {
                    Document newDoc = PolicyLoader.getDocument(IOUtils.toInputStream(mutant.encode(), Charset.defaultCharset()));
                    nodes = (NodeList) xPath.evaluate(xpathString, newDoc.getDocumentElement(), XPathConstants.NODESET);
                    Assert.assertEquals(1, nodes.getLength());
                    Node newNode = nodes.item(0);
                    Node prev = newNode.getPreviousSibling();
                    while (!(prev instanceof Element)) {
                        prev = prev.getPreviousSibling();
                    }
//                    System.out.println(XpathSolver.nodeToString(prev, false, true));
                }
//                System.out.println("===========");
            }
        }
    }
}
