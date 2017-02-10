package org.seal.semanticMutation;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seal.policyUtils.PolicyLoader;
import org.seal.policyUtils.XpathSolver;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.ParsingException;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by shuaipeng on 9/8/16.
 */
public class Mutator {
    private static Log logger = LogFactory.getLog(Mutator.class);
    // so far only string and integer are considered.
    private static Map<String, String> equalsFunctionMap = new HashMap<>();
    private static Map<String, List<String>> unequalValuesMap = new HashMap<>();
    private static Map<String, String> oneAndOnlyFunctionMap = new HashMap<>();
    private static Map<String, List<String>> comparisonFunctionMap = new HashMap<>();
    private static List<String> ruleCombiningAlgorithms;
    private static List<String> policyCombiningAlgorithms;

    static {
        /*
          see http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html#_Toc325047233 Section 10.2.7 Data-types
         */
        equalsFunctionMap.put("http://www.w3.org/2001/XMLSchema#string", "urn:oasis:names:tc:xacml:1.0:function:string-equal");
        equalsFunctionMap.put("http://www.w3.org/2001/XMLSchema#boolean", "urn:oasis:names:tc:xacml:1.0:function:boolean-equal");
        equalsFunctionMap.put("http://www.w3.org/2001/XMLSchema#integer", "urn:oasis:names:tc:xacml:1.0:function:integer-equal");
        equalsFunctionMap.put("http://www.w3.org/2001/XMLSchema#double", "urn:oasis:names:tc:xacml:1.0:function:double-equal");
        equalsFunctionMap.put("http://www.w3.org/2001/XMLSchema#date", "urn:oasis:names:tc:xacml:1.0:function:date-equal");
        /*
          see http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html#_Toc325047234 Section 10.2.8 Functions
         */
        oneAndOnlyFunctionMap.put("http://www.w3.org/2001/XMLSchema#string", "urn:oasis:names:tc:xacml:1.0:function:string-one-and-only");
        oneAndOnlyFunctionMap.put("http://www.w3.org/2001/XMLSchema#boolean", "urn:oasis:names:tc:xacml:1.0:function:boolean-one-and-only");
        oneAndOnlyFunctionMap.put("http://www.w3.org/2001/XMLSchema#integer", "urn:oasis:names:tc:xacml:1.0:function:integer-one-and-only");
        oneAndOnlyFunctionMap.put("http://www.w3.org/2001/XMLSchema#double", "urn:oasis:names:tc:xacml:1.0:function:double-one-and-only");
        oneAndOnlyFunctionMap.put("http://www.w3.org/2001/XMLSchema#date", "urn:oasis:names:tc:xacml:1.0:function:date-one-and-only");
        /*
          see https://www.w3.org/TR/xmlschema-2/#built-in-primitive-datatypes for legal literals for different types
         */
        unequalValuesMap.put("http://www.w3.org/2001/XMLSchema#string", Arrays.asList("a", "b"));
        unequalValuesMap.put("http://www.w3.org/2001/XMLSchema#boolean", Arrays.asList("true", "false"));
        unequalValuesMap.put("http://www.w3.org/2001/XMLSchema#integer", Arrays.asList("1", "2"));
        unequalValuesMap.put("http://www.w3.org/2001/XMLSchema#double", Arrays.asList("1.0", "2.0"));
        unequalValuesMap.put("http://www.w3.org/2001/XMLSchema#date", Arrays.asList("2002-10-10+13:00", "2002-10-11+13:00"));
        /*
          see http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html#_Toc325047234 Section 10.2.8 Functions
         */
        comparisonFunctionMap.put("http://www.w3.org/2001/XMLSchema#string",
                Arrays.asList("urn:oasis:names:tc:xacml:1.0:function:string-equal",
                        "urn:oasis:names:tc:xacml:1.0:function:string-greater-than",
                        "urn:oasis:names:tc:xacml:1.0:function:string-greater-than-or-equal",
                        "urn:oasis:names:tc:xacml:1.0:function:string-less-than",
                        "urn:oasis:names:tc:xacml:1.0:function:string-less-than-or-equal"
                ));
        comparisonFunctionMap.put("http://www.w3.org/2001/XMLSchema#boolean",
                Collections.singletonList("urn:oasis:names:tc:xacml:1.0:function:boolean-equal"));
        comparisonFunctionMap.put("http://www.w3.org/2001/XMLSchema#integer",
                Arrays.asList("urn:oasis:names:tc:xacml:1.0:function:integer-equal",
                        "urn:oasis:names:tc:xacml:1.0:function:integer-greater-than",
                        "urn:oasis:names:tc:xacml:1.0:function:integer-greater-than-or-equal",
                        "urn:oasis:names:tc:xacml:1.0:function:integer-less-than",
                        "urn:oasis:names:tc:xacml:1.0:function:integer-less-than-or-equal"
                ));
        comparisonFunctionMap.put("http://www.w3.org/2001/XMLSchema#double",
                Arrays.asList("urn:oasis:names:tc:xacml:1.0:function:double-equal",
                        "urn:oasis:names:tc:xacml:1.0:function:double-greater-than",
                        "urn:oasis:names:tc:xacml:1.0:function:double-greater-than-or-equal",
                        "urn:oasis:names:tc:xacml:1.0:function:double-less-than",
                        "urn:oasis:names:tc:xacml:1.0:function:double-less-than-or-equal"
                ));
        comparisonFunctionMap.put("http://www.w3.org/2001/XMLSchema#date",
                Arrays.asList("urn:oasis:names:tc:xacml:1.0:function:date-equal",
                        "urn:oasis:names:tc:xacml:1.0:function:date-greater-than",
                        "urn:oasis:names:tc:xacml:1.0:function:date-greater-than-or-equal",
                        "urn:oasis:names:tc:xacml:1.0:function:date-less-than",
                        "urn:oasis:names:tc:xacml:1.0:function:date-less-than-or-equal"
                ));
        ruleCombiningAlgorithms = Arrays.asList("urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-overrides",
                "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-overrides",
                "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable",
                "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-deny-overrides",
                "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:ordered-permit-overrides",
                "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:deny-unless-permit",
                "urn:oasis:names:tc:xacml:3.0:rule-combining-algorithm:permit-unless-deny");
        policyCombiningAlgorithms = Arrays.asList("urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-overrides",
                "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-overrides",
                "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:first-applicable",
                "urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:only-one-applicable",
                "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:ordered-deny-overrides",
                "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:ordered-permit-overrides",
                "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:deny-unless-permit",
                "urn:oasis:names:tc:xacml:3.0:policy-combining-algorithm:permit-unless-deny");
    }

    private List<String> xpathList;
    private Map<String, Integer> xpathMapping;
    private Document doc;
    private XPath xPath;

    public Mutator(Mutant baseMutant) throws ParserConfigurationException, SAXException, IOException {
        InputStream stream = IOUtils.toInputStream(baseMutant.encode(), Charset.defaultCharset());
        doc = PolicyLoader.getDocument(stream);
        xpathList = XpathSolver.getEntryListRelativeXPath(doc);
        xpathMapping = new HashMap<>();
        for (int i = 0; i < xpathList.size(); i++) {
            xpathMapping.put(xpathList.get(i), i);
        }
        xPath = XPathFactory.newInstance().newXPath();
    }

    static boolean isEmptyNode(Node node) {
        // When the target is empty, it may have one child node that contains only text "\n"; when the target is not empty,
        // it may have three nodes: "\n", AnyOf element and "\n".
        if (node == null) {
            return true;
        }
        NodeList children = node.getChildNodes();
        boolean isEmptyTarget = true;
        for (int i = 0; i < children.getLength(); i++) {
            // we don't use "if (child.getNodeName().equals("AnyOf"))" here for backward compatibility with XACML 2.0.
            if (!children.item(i).getNodeName().equals("#text")) {
                isEmptyTarget = false;
            }
        }
        return isEmptyTarget;
    }

    public static void main(String[] args) throws ParserConfigurationException, ParsingException, SAXException, IOException, XPathExpressionException {
        File file = new File("src/test/resources/org/seal/policies/HL7/HL7.xml");
        String xpathString = "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes.createNoteoo']";
        XPath xPath = XPathFactory.newInstance().newXPath();
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        Document doc = PolicyLoader.getDocument(IOUtils.toInputStream(policy.encode(), Charset.defaultCharset()));
        NodeList nodes = (NodeList) xPath.evaluate(xpathString, doc.getDocumentElement(), XPathConstants.NODESET);
        // assert only one
        Node node = nodes.item(0);
        System.out.println(XpathSolver.nodeToString(node, false, true));
        Node child = node.getFirstChild();
        node.removeChild(child);
        System.out.println(XpathSolver.nodeToString(node, false, true));
        node.appendChild(child);
        System.out.println(XpathSolver.nodeToString(node, false, true));

    }

    /**
     * flip rule effect
     */
    public List<Mutant> createRuleEffectFlippingMutants(String ruleXpathString) throws XPathExpressionException, ParsingException {
        List<Mutant> list = new ArrayList<>();
        NodeList nodes = (NodeList) xPath.evaluate(ruleXpathString, doc.getDocumentElement(), XPathConstants.NODESET);
        Node node = nodes.item(0);
        if (node != null) {
            //change doc
            if (node.getAttributes().getNamedItem("Effect").getTextContent().equals("Deny")) {
                node.getAttributes().getNamedItem("Effect").setTextContent("Permit");
            } else {
                node.getAttributes().getNamedItem("Effect").setTextContent("Deny");
            }
            AbstractPolicy newPolicy = PolicyLoader.loadPolicy(doc);
            //restore doc
            if (node.getAttributes().getNamedItem("Effect").getTextContent().equals("Deny")) {
                node.getAttributes().getNamedItem("Effect").setTextContent("Permit");
            } else {
                node.getAttributes().getNamedItem("Effect").setTextContent("Deny");
            }
            int faultLocation = xpathMapping.get(ruleXpathString);
            list.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), "CRE" + faultLocation));
        }
        return list;
    }

    /**
     * Make Policy Target always true
     */
    public List<Mutant> createPolicyTargetTrueMutants(String xpathString) throws XPathExpressionException, ParsingException, IOException, ParserConfigurationException, SAXException {
        int faultLocation = xpathMapping.get(xpathString);
        return createTargetTrueMutants(xpathString, "PTT", faultLocation);

    }

    /**
     * Make Rule Target always true
     */
    public List<Mutant> createRuleTargetTrueMutants(String xpathString) throws XPathExpressionException, ParsingException, IOException, ParserConfigurationException, SAXException {
        int faultLocation = xpathMapping.get(xpathString);
        return createTargetTrueMutants(xpathString + "/*[local-name()='Target' and 1]", "RTT", faultLocation);
    }

    /**
     * Make a Target always true by removing all its child nodes. According to the specification of XACML, a Target is
     * always evaluated to true if it is empty.
     */
    private List<Mutant> createTargetTrueMutants(String targetXpathString, String mutantName, int faultLocation) throws XPathExpressionException, ParsingException, IOException, ParserConfigurationException, SAXException {
        List<Mutant> list = new ArrayList<>();
        NodeList nodes = (NodeList) xPath.evaluate(targetXpathString, doc.getDocumentElement(), XPathConstants.NODESET);
        Node node = nodes.item(0);
        if (node != null && !isEmptyNode(node)) {
            //change doc
            List<Node> children = new ArrayList<>();
            while (node.hasChildNodes()) {
                Node child = node.getFirstChild();
                children.add(child);
                node.removeChild(child);
            }
            AbstractPolicy newPolicy = PolicyLoader.loadPolicy(doc);
            list.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), mutantName + faultLocation));
            //restore doc
            for (Node child : children) {
                node.appendChild(child);
            }
        }
        return list;
    }

    /**
     * Make Rule Condition always true
     *
     * We cannot remove all child nodes of Condition as we do to policy target and rule target, because
     * Condition.getInstance() will throw a null pointer exception. So here the whole Condition node is removed from the
     * rule node.
     */
    public List<Mutant> createRuleConditionTrueMutants(String xpathString) throws XPathExpressionException, ParsingException, ParserConfigurationException, SAXException, IOException {
        int faultLocation = xpathMapping.get(xpathString);
        String mutantName = "RCT";
        String conditionXpathString = xpathString + "/*[local-name()='Condition' and 1]";
        List<Mutant> list = new ArrayList<>();
        NodeList nodes = (NodeList) xPath.evaluate(conditionXpathString, doc.getDocumentElement(), XPathConstants.NODESET);
        Node node = nodes.item(0);
        if (!isEmptyNode(node)) {
            //change doc
            Node ruleNode = node.getParentNode();
            ruleNode.removeChild(node);
            AbstractPolicy newPolicy = PolicyLoader.loadPolicy(doc);
            list.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), mutantName + faultLocation));
            //restore doc
            ruleNode.appendChild(node);
        }
        return list;
    }

    /**
     * Make Rule Target always false
     */
    public List<Mutant> createRuleTargetFalseMutants(String xpathString) throws XPathExpressionException, ParsingException {
        int faultLocation = xpathMapping.get(xpathString);
        String mutantName = "RTT";
        String matchXpathString = xpathString + "/*[local-name()='Target' and 1]/*[local-name()='AnyOf' and 1]/*[local-name()='AllOf' and 1]/*[local-name()='Match' and 1]";
        return createTargetFalseMutants(matchXpathString, faultLocation, mutantName);
    }

    /**
     * Make the Target of a Policy or PolicySet always false
     *
     * @param xpathString xpath to the Target of a Policy or PolicySet
     */
    public List<Mutant> createPolicyTargetFalseMutants(String xpathString) throws XPathExpressionException, ParsingException {
        int faultLocation = xpathMapping.get(xpathString);
        String mutantName = "PTT";
        String matchXpathString = xpathString + "/*[local-name()='AnyOf' and 1]/*[local-name()='AllOf' and 1]/*[local-name()='Match' and 1]";
        return createTargetFalseMutants(matchXpathString, faultLocation, mutantName);
    }

    /**
     * If Match element exists, we can make the Target always evaluate to false by adding two conflicting Match elements
     * to the parent of Match element. For example, if the Match element says role == "physician", then we add 2 Match
     * elements: role == "a" and role == "b".
     * First make 2 clones of the Match element, for each clone: find the AttributeValue element in the Match element,
     * get the DataType attribute from the AttributeValue. Set the MatchId attribute according to DataType. And set the
     * text content of AttributeValue element according the DataType. For example, if DataType is string, we set MatchId
     * to string-equals, set the text context to "a" and "b" for the 2 clones separately.
     * element.
     *
     * @param matchXpathString the xpath to the first Match element in a Target element
     */
    private List<Mutant> createTargetFalseMutants(String matchXpathString, int faultLocation, String mutantName) throws XPathExpressionException, ParsingException {
        List<Mutant> list = new ArrayList<>();
        NodeList nodes = (NodeList) xPath.evaluate(matchXpathString, doc.getDocumentElement(), XPathConstants.NODESET);
        Node matchNode = nodes.item(0);
        if (matchNode != null && !isEmptyNode(matchNode)) {
            //change doc
            List<Node> clonedNodes = new ArrayList<>();
            for (int k = 0; k < 2; k++) {
                Node cloned = matchNode.cloneNode(true);
                clonedNodes.add(cloned);
                //find the AttributeValue child node
                List<Node> attributeValueNodes = findChildrenByLocalName(cloned, "AttributeValue");
                if (attributeValueNodes.size() == 0) {
                    throw new RuntimeException("couldn't find AttributeValue in Mathch");
                }
                Node attributeValueNode = attributeValueNodes.get(0);
                //set MatchId and AttributeValue according to DataType
                String dataType = attributeValueNode.getAttributes().getNamedItem("DataType").getNodeValue();
                if (!equalsFunctionMap.containsKey(dataType)) {
                    throw new RuntimeException("unsupported DataType: " + dataType);
                }
                cloned.getAttributes().getNamedItem("MatchId").setNodeValue(equalsFunctionMap.get(dataType));
                attributeValueNode.setTextContent(unequalValuesMap.get(dataType).get(k));
                //add two conflicting Match nodes to parent
                matchNode.getParentNode().appendChild(cloned);
            }
//            System.out.println(XpathSolver.nodeToString(matchNode.getParentNode(), false, true));
            AbstractPolicy newPolicy = PolicyLoader.loadPolicy(doc);
            list.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), mutantName + faultLocation));
            //restore doc by removing the two conflicting Match nodes from parent
            for (Node cloned : clonedNodes) {
                matchNode.getParentNode().removeChild(cloned);
            }
//            System.out.println(XpathSolver.nodeToString(matchNode.getParentNode(), false, true));
        }
        return list;
    }

    /**
     * @return all the child nodes of input node whose local name equals to input argument localName
     */
    private List<Node> findChildrenByLocalName(Node node, String localName) {
        List<Node> matchedChildNodes = new ArrayList<>();
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node child = childNodes.item(i);
            if (localName.equals(child.getLocalName())) {
                matchedChildNodes.add(child);
            }
        }
        return matchedChildNodes;
    }

    /**
     * Make Rule Condition always false
     * First find the condition node, then remove and replace it with a condition node we built. The condition node we
     * built is always false because it has two conflicting conditions, e.g. role == "a" and role == "b".
     */
    public List<Mutant> createRuleConditionFalseMutants(String xpathString) throws XPathExpressionException, ParserConfigurationException, IOException, SAXException, ParsingException {
        int faultLocation = xpathMapping.get(xpathString);
        String mutantName = "RCF";
        List<Mutant> mutants = new ArrayList<>();
        String conditionXpathString = xpathString + "/*[local-name()='Condition' and 1]";
        Node conditionNode = ((NodeList) xPath.evaluate(conditionXpathString, doc.getDocumentElement(), XPathConstants.NODESET)).item(0);
        if (conditionNode != null && !isEmptyNode(conditionNode)) {
            Node attributeDesignator = findNodeByLocalNameRecursively(conditionNode, "AttributeDesignator");
            if (attributeDesignator != null) {
                //get DataType and attributes
                String dataType = attributeDesignator.getAttributes().getNamedItem("DataType").getNodeValue();
                String attributeId = attributeDesignator.getAttributes().getNamedItem("AttributeId").getNodeValue();
                String category = attributeDesignator.getAttributes().getNamedItem("Category").getNodeValue();
                if (!equalsFunctionMap.containsKey(dataType)) {
                    throw new RuntimeException("unsupported DataType: " + dataType);
                }
                //build false condition
                String falseCondition = "";
                falseCondition += "\t<Condition>\n";
                falseCondition += "\t\t<Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:and\">\n";
                falseCondition += "\t\t\t<Apply FunctionId=\"" + equalsFunctionMap.get(dataType) + "\">\n";
                falseCondition += "\t\t\t\t<Apply FunctionId=\"" + oneAndOnlyFunctionMap.get(dataType) + "\">\n";
                falseCondition += "\t\t\t\t\t<AttributeDesignator AttributeId=\"" + attributeId + "\" Category=\"" + category + "\" DataType=\"" + dataType + "\" MustBePresent=\"true\"/>\n";
                falseCondition += "\t\t\t\t</Apply>\n";
                falseCondition += "\t\t\t\t<AttributeValue DataType=\"" + dataType + "\">" + unequalValuesMap.get(dataType).get(0) + "</AttributeValue>\n";
                falseCondition += "\t\t\t</Apply>\n";
                falseCondition += "\t\t\t<Apply FunctionId=\"" + equalsFunctionMap.get(dataType) + "\">\n";
                falseCondition += "\t\t\t\t<Apply FunctionId=\"" + oneAndOnlyFunctionMap.get(dataType) + "\">\n";
                falseCondition += "\t\t\t\t\t<AttributeDesignator AttributeId=\"" + attributeId + "\" Category=\"" + category + "\" DataType=\"" + dataType + "\" MustBePresent=\"true\"/>\n";
                falseCondition += "\t\t\t\t</Apply>\n";
                falseCondition += "\t\t\t\t<AttributeValue DataType=\"" + dataType + "\">" + unequalValuesMap.get(dataType).get(1) + "</AttributeValue>\n";
                falseCondition += "\t\t\t</Apply>\n";
                falseCondition += "\t\t</Apply>\n";
                falseCondition += "\t</Condition>\n";
                Node falseConditionNode = DocumentBuilderFactory
                        .newInstance()
                        .newDocumentBuilder()
                        .parse(new ByteArrayInputStream(falseCondition.getBytes())).getFirstChild();
                Node importedFalseConditionNode = doc.importNode(falseConditionNode, true);
                //change doc
                Node parent = conditionNode.getParentNode();
                parent.removeChild(conditionNode);
                parent.appendChild(importedFalseConditionNode);
//                System.out.println(XpathSolver.nodeToString(parent, false, true));
                AbstractPolicy newPolicy = PolicyLoader.loadPolicy(doc);
                mutants.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), mutantName + faultLocation));
                //restore doc
                parent.removeChild(importedFalseConditionNode);
                parent.appendChild(conditionNode);
//                System.out.println(XpathSolver.nodeToString(parent, false, true));
            }
        }
        return mutants;
    }

    /**
     * recursively look for a node such that node.getLocalName() equals localName
     *
     * @return the node we found, or null if there's no such node
     */
    private Node findNodeByLocalNameRecursively(Node node, String localName) {
        if (localName.equals(node.getLocalName())) {
            return node;
        }
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            Node target = findNodeByLocalNameRecursively(child, localName);
            if (target != null) {
                return target;
            }
        }
        return null;
    }

    /**
     * Change the comparison function in the target of a policy or a policy set. For example, change "==" to ">", ">=",
     * "<" or "<=".
     *
     * @param xpathString xpath string to the target of a policy or a policy set
     * @return a list of mutants generated using this semanticMutation operator, or an empty list if this semanticMutation operator is
     * not applicable this rule
     */
    public List<Mutant> createPolicyTargetChangeComparisonFunctionMutants(String xpathString) throws XPathExpressionException, ParsingException {
        int faultLocation = xpathMapping.get(xpathString);
        String mutantName = "CCF";
        return createTargetChangeComparisonFunctionMutants(xpathString, faultLocation, mutantName);
    }

    /**
     * Change the comparison functions in a rule. The changed comparison function can be in a the target or the condition
     * of the rule.
     * @param xpathString xpath string to a rule element
     */
    public List<Mutant> createRuleChangeComparisonFunctionMutants(String xpathString) throws XPathExpressionException, ParsingException {
        int faultLocation = xpathMapping.get(xpathString);
        String mutantName = "CCF";
        List<Mutant> mutants = new ArrayList<>();
        String ruleTargetXpathString = xpathString + "/*[local-name()='Target' and 1]";
        mutants.addAll(createTargetChangeComparisonFunctionMutants(ruleTargetXpathString, faultLocation, mutantName));
        mutants.addAll(createRuleConditionChangeComparisonFunctionMutants(xpathString, faultLocation, mutantName));
        return mutants;
    }

    /**
     * Change the first comparision function in the condition of a rule.
     * @param ruleXpathString xpath string to a rule element
     */
    private List<Mutant> createRuleConditionChangeComparisonFunctionMutants(String ruleXpathString, int faultLocation, String mutantName) throws XPathExpressionException, ParsingException {
        String conditionXpathString = ruleXpathString + "/*[local-name()='Condition' and 1]";
        List<Mutant> list = new ArrayList<>();
        Node conditionNode = ((NodeList) xPath.evaluate(conditionXpathString, doc.getDocumentElement(), XPathConstants.NODESET)).item(0);
        if (!isEmptyNode(conditionNode)) {
            Node attributeDesignator = findNodeByLocalNameRecursively(conditionNode, "AttributeDesignator");
            if (attributeDesignator != null) {
                String dataType = attributeDesignator.getAttributes().getNamedItem("DataType").getNodeValue();
                Node applyNode = attributeDesignator.getParentNode();
                Node functionNode = findNodeByLocalNameRecursively(applyNode, "Function");
                if (functionNode != null) {
                    String originalComparisonFunction = functionNode.getAttributes().getNamedItem("FunctionId").getNodeValue();
                    for (String comparisonFunction : comparisonFunctionMap.get(dataType)) {
                        if (!comparisonFunction.equals(originalComparisonFunction)) {
                            //change doc
                            functionNode.getAttributes().getNamedItem("FunctionId").setNodeValue(comparisonFunction);
                            AbstractPolicy newPolicy = PolicyLoader.loadPolicy(doc);
                            list.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), mutantName + faultLocation));
                        }
                    }
                    //restore doc
                    functionNode.getAttributes().getNamedItem("FunctionId").setNodeValue(originalComparisonFunction);
                }
            }
        }
        return list;

    }

    /**
     * Change the first comparision function in a policy target or rule target.
     * @param targetXpathString the xpath string to a target element
     */
    private List<Mutant> createTargetChangeComparisonFunctionMutants(String targetXpathString, int faultLocation, String mutantName) throws XPathExpressionException, ParsingException {
        String matchXpathString = targetXpathString + "/*[local-name()='AnyOf' and 1]/*[local-name()='AllOf' and 1]/*[local-name()='Match' and 1]";
        Node matchNode = ((NodeList) xPath.evaluate(matchXpathString, doc.getDocumentElement(), XPathConstants.NODESET)).item(0);
        List<Mutant> mutants = new ArrayList<>();
        if (!isEmptyNode(matchNode)) {
            Node attributeDesignator = findNodeByLocalNameRecursively(matchNode, "AttributeDesignator");
            if (attributeDesignator != null) {
//                System.out.println(XpathSolver.nodeToString(matchNode, false, true));
                String dataType = attributeDesignator.getAttributes().getNamedItem("DataType").getNodeValue();
                String orignalComparisonFunction = matchNode.getAttributes().getNamedItem("MatchId").getNodeValue();
                //change doc
                for (String comparisonFunction : comparisonFunctionMap.get(dataType)) {
                    if (!comparisonFunction.equals(orignalComparisonFunction)) {
                        matchNode.getAttributes().getNamedItem("MatchId").setNodeValue(comparisonFunction);
//                        System.out.println(XpathSolver.nodeToString(matchNode, false, true));
                        AbstractPolicy newPolicy = PolicyLoader.loadPolicy(doc);
                        mutants.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), mutantName + faultLocation));
                    }
                }
                //restore doc
                matchNode.getAttributes().getNamedItem("MatchId").setNodeValue(orignalComparisonFunction);
//                System.out.println(XpathSolver.nodeToString(matchNode, false, true));
            }
        }
        return mutants;
    }

    /**
     * add a not function in the Condition element
     *
     * @param xpathString xpath string to a rule
     */
    public List<Mutant> createAddNotFunctionMutants(String xpathString) throws XPathExpressionException, ParsingException, ParserConfigurationException, IOException, SAXException {
        int faultLocation = xpathMapping.get(xpathString);
        String mutantName = "ANF";
        List<Mutant> mutants = new ArrayList<>();
        String conditionXpathString = xpathString + "/*[local-name()='Condition' and 1]";
        Node conditionNode = ((NodeList) xPath.evaluate(conditionXpathString, doc.getDocumentElement(), XPathConstants.NODESET)).item(0);
        if (!isEmptyNode(conditionNode)) {
            List<Node> childNodes = new ArrayList<>();
            NodeList children = conditionNode.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                childNodes.add(children.item(i));
            }
            //change doc
            for (Node child : childNodes) {
                conditionNode.removeChild(child);
            }
            String notFunctionString = "\t<Apply FunctionId=\"urn:oasis:names:tc:xacml:1.0:function:not\">\n\t</Apply>\n";
            Node notFunctionNode = DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(new ByteArrayInputStream(notFunctionString.getBytes())).getFirstChild();
            Node importedNotFunctionNode = doc.importNode(notFunctionNode, true);
            for (Node child : childNodes) {
                importedNotFunctionNode.appendChild(child);
            }
            conditionNode.appendChild(importedNotFunctionNode);
            AbstractPolicy newPolicy = PolicyLoader.loadPolicy(doc);
            mutants.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), mutantName + faultLocation));
            //restore doc
            for (Node child : childNodes) {
                importedNotFunctionNode.removeChild(child);
            }
            conditionNode.removeChild(importedNotFunctionNode);
            for (Node child : childNodes) {
                conditionNode.appendChild(child);
            }
        }
        return mutants;
    }

    /**
     * remove the outmost not function in the Condition element, if there is such
     *
     * @param xpathString xpath string to a rule
     */
    public List<Mutant> createRemoveNotFunctionMutants(String xpathString) throws XPathExpressionException, ParsingException {
        int faultLocation = xpathMapping.get(xpathString);
        String mutantName = "RNF";
        List<Mutant> mutants = new ArrayList<>();
        String conditionXpathString = xpathString + "/*[local-name()='Condition' and 1]";
        Node conditionNode = ((NodeList) xPath.evaluate(conditionXpathString, doc.getDocumentElement(), XPathConstants.NODESET)).item(0);
        if (!isEmptyNode(conditionNode)) {
            Node applyNode = findNodeByLocalNameRecursively(conditionNode, "Apply");
            String notFunctionString = "urn:oasis:names:tc:xacml:1.0:function:not";
            if (applyNode != null && applyNode.getAttributes().getNamedItem("FunctionId").getNodeValue().equals(notFunctionString)) {
                List<Node> childNodes = new ArrayList<>();
                NodeList children = applyNode.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    childNodes.add(children.item(i));
                }
                //change doc
                for (Node child : childNodes) {
                    applyNode.removeChild(child);
                }
                Node parent = applyNode.getParentNode();
                parent.removeChild(applyNode);
                for (Node child : childNodes) {
                    parent.appendChild(child);
                }
                AbstractPolicy newPolicy = PolicyLoader.loadPolicy(doc);
                mutants.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), mutantName + faultLocation));
                //restore doc
                for (Node child : childNodes) {
                    parent.removeChild(child);
                }
                for (Node child : childNodes) {
                    applyNode.appendChild(child);
                }
                parent.appendChild(applyNode);
            }
        }
        return mutants;
    }

    /**
     * Replaces the existing combining algorithm with another combining algorithm, works for both Policy and PolicySet.
     */
    public List<Mutant> createCombiningAlgorithmMutants(String targetXpathString) throws XPathExpressionException, ParsingException {
        int faultLocation = xpathMapping.get(targetXpathString);
        String mutantName = "CRC";
        List<Mutant> mutants = new ArrayList<>();
        String PolicyXpathString = targetXpathString.replace("/*[local-name()='Target' and 1]", "");
        Node policyNode = ((NodeList) xPath.evaluate(PolicyXpathString, doc.getDocumentElement(), XPathConstants.NODESET)).item(0);
        List<String> combiningAlgList = null;
        String combiningAlgAttribute = null;
        if (policyNode.getLocalName().equals("PolicySet")) {
            combiningAlgList = policyCombiningAlgorithms;
            combiningAlgAttribute = "PolicyCombiningAlgId";
        } else if (policyNode.getLocalName().equals("Policy")) {
            combiningAlgList = ruleCombiningAlgorithms;
            combiningAlgAttribute = "RuleCombiningAlgId";
        }
        if (combiningAlgList != null) {
            //change doc
            String originalCombiningAlgId = policyNode.getAttributes().getNamedItem(combiningAlgAttribute).getNodeValue();
            for (String combiningAlgoId : combiningAlgList) {
                if (!combiningAlgoId.equals(originalCombiningAlgId)) {
                    policyNode.getAttributes().getNamedItem(combiningAlgAttribute).setNodeValue(combiningAlgoId);
                    AbstractPolicy newPolicy = PolicyLoader.loadPolicy(doc);
                    mutants.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), mutantName + faultLocation));
                }
            }
            //restore doc
            policyNode.getAttributes().getNamedItem(combiningAlgAttribute).setNodeValue(originalCombiningAlgId);
        }
        return mutants;
    }

    /**
     * remove the rule.
     * Note that this mutation method should not be used for fault localization as it will cause the position of the
     * following policy elements to shift by 1.
     */
    public List<Mutant> createRemoveRuleMutants(String xpathString) throws XPathExpressionException, ParsingException {
        int faultLocation = xpathMapping.get(xpathString);
        String mutantName = "RER";
        List<Mutant> mutants = new ArrayList<>();
        Node ruleNode = ((NodeList) xPath.evaluate(xpathString, doc.getDocumentElement(), XPathConstants.NODESET)).item(0);
        Node parent = ruleNode.getParentNode();
        //change doc
        Node nextSibling = ruleNode.getNextSibling();
        parent.removeChild(ruleNode);
        AbstractPolicy newPolicy = PolicyLoader.loadPolicy(doc);
        mutants.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), mutantName + faultLocation));
        //restore
        //use insertBefore() instead of appendChild() because we want to restore the Rule node to the same previous index
        // as for combining algorithms like "first applicable", the order of rules matters
        parent.insertBefore(ruleNode, nextSibling);
        return mutants;
    }

    /**
     * add a new rule. The new rule is based on this rule, but its effect is flipped, or its target is always true if
     * the target of this rule is not always true.
     * Note that this mutation method should not be used for fault localization as it will cause the position of the
     * following policy elements to shift by 1.
     */
    public List<Mutant> createAddNewRuleMutants(String xpathString) throws XPathExpressionException, ParsingException {
        int faultLocation = xpathMapping.get(xpathString);
        String mutantName = "ANR";
        List<Mutant> mutants = new ArrayList<>();
        Node ruleNode = ((NodeList) xPath.evaluate(xpathString, doc.getDocumentElement(), XPathConstants.NODESET)).item(0);
        Node parent = ruleNode.getParentNode();
        //make a clone of the rule, flip the Effect of the clone, and insert it before the rule
        Node clone = ruleNode.cloneNode(true);
        String originEffect = clone.getAttributes().getNamedItem("Effect").getNodeValue();
        String flippedEffect = originEffect.equals("Permit") ? "Deny" : "Permit";
        clone.getAttributes().getNamedItem("Effect").setNodeValue(flippedEffect);
        String newRuleId = UUID.randomUUID().toString();
        clone.getAttributes().getNamedItem("RuleId").setNodeValue(newRuleId);
        //change doc
        parent.insertBefore(clone, ruleNode);
        AbstractPolicy newPolicy = PolicyLoader.loadPolicy(doc);
        mutants.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), mutantName + faultLocation));
        //restore doc
        parent.removeChild(clone);
        //if the rule has a Target, make a clone of the rule, make the target of the clone always true, and insert it before the rule.
        clone = ruleNode.cloneNode(true);
        Node targetNode = findNodeByLocalNameRecursively(clone, "Target");
        if (!isEmptyNode(targetNode)) {
            while (targetNode.getFirstChild() != null) {
                targetNode.removeChild(targetNode.getFirstChild());
            }
            newRuleId = UUID.randomUUID().toString();
            clone.getAttributes().getNamedItem("RuleId").setNodeValue(newRuleId);
            //change doc
            parent.insertBefore(clone, ruleNode);
            newPolicy = PolicyLoader.loadPolicy(doc);
            mutants.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), mutantName + faultLocation));
            //restore doc
            parent.removeChild(clone);
        }
        return mutants;
    }
}
