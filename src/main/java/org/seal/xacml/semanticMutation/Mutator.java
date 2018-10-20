package org.seal.xacml.semanticMutation;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seal.xacml.mutation.MutationMethodAbbrDirectory;
import org.seal.xacml.policyUtils.PolicyLoader;
import org.seal.xacml.policyUtils.XpathSolver;
import org.seal.xacml.utils.FileIOUtil;
import org.seal.xacml.utils.XACMLElementUtil;
import org.seal.xacml.utils.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.ParsingException;
import org.wso2.balana.PolicyMetaData;
import org.wso2.balana.TargetMatch;
import org.wso2.balana.xacml3.AllOfSelection;
import org.wso2.balana.xacml3.AnyOfSelection;
import org.xml.sax.SAXException;

/**
 * If a mutation operator only creates one mutant for a bug position, the name of resulted mutant will
 * be {base mutant name}_{mutation operator abbreviation}{bug position} if base mutant name is not an empty string, or
 * {mutation operator abbreviation}{bug position} if base mutant name is an empty string. For example CRE1_CRE0, the base
 * mutant is CRE1, the mutation operator is CRE, the bug position is 0.
 * </p>
 * If a mutation operator creates multiple mutants for a bug position, e.g. CRC, the name of resulted mutant will be
 * {base mutant name}_{mutation operator abbreviation}{bug position}_{count} if base mutant name is not an empty string,
 * or {mutation operator abbreviation}{bug position}_{count} if base mutant name is an empty string. For example
 * CRE1_CRC2_0, the base mutant is CRE1, the bug position is 2, the count is 0.
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
    private static Set<String> ruleMutationMethods = new HashSet<>();
    private static Set<String> targetMutationMethods = new HashSet<>();
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

        ruleMutationMethods.add("createRuleEffectFlippingMutants");
        ruleMutationMethods.add("createRuleTargetTrueMutants");
        ruleMutationMethods.add("createRuleConditionTrueMutants");
        ruleMutationMethods.add("createRuleTargetFalseMutants");
        ruleMutationMethods.add("createRuleConditionFalseMutants");
        ruleMutationMethods.add("createRuleChangeComparisonFunctionMutants");
        ruleMutationMethods.add("createAddNotFunctionMutants");
        ruleMutationMethods.add("createRemoveNotFunctionMutants");
        ruleMutationMethods.add("createRemoveRuleMutants");
        ruleMutationMethods.add("createAddNewRuleMutants");
        ruleMutationMethods.add("createRemoveParallelTargetElementMutants");
        targetMutationMethods.add("createRemoveParallelTargetElementMutants");

        targetMutationMethods.add("createPolicyTargetTrueMutants");
        targetMutationMethods.add("createPolicyTargetFalseMutants");
        targetMutationMethods.add("createPolicyTargetChangeComparisonFunctionMutants");
        targetMutationMethods.add("createCombiningAlgorithmMutants");
        targetMutationMethods.add("createFirstDenyRuleMutants");
        targetMutationMethods.add("createFirstPermitRuleMutants");
    }

    private List<String> xpathList;
    private Map<String, Integer> xpathMapping;
    private Document doc;
    private XPath xPath;
    private String baseMutantName;

    public Mutator(Mutant baseMutant) throws ParserConfigurationException, SAXException, IOException {
        InputStream stream = IOUtils.toInputStream(baseMutant.encode(), Charset.defaultCharset());
        doc = PolicyLoader.getDocument(stream);
        xpathList = XpathSolver.getEntryListRelativeXPath(doc);
        xpathMapping = new HashMap<>();
        for (int i = 0; i < xpathList.size(); i++) {
            xpathMapping.put(xpathList.get(i), i);
        }
        xPath = XPathFactory.newInstance().newXPath();
        baseMutantName = baseMutant.getName();
    }

    public static boolean isEmptyNode(Node node) {
        // When the target is empty, it may have one child node that contains only text "\n"; when the target is not empty,
        // it may have three nodes: "\n", AnyOf element and "\n".
        if (node == null) {
            return true;
        }
        List<Node> childNodes = getChildNodeList(node);
        for (Node child : childNodes) {
            if (child.getNodeType() == Node.ELEMENT_NODE) {
                return false;
            }
        }
        return true;
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

    static boolean isRuleXpathString(String xPathString) {
        return xPathString.contains("[local-name()='Rule'");
    }

    static boolean isTargetXpathString(String xPathString) {
        return xPathString.contains("[local-name()='Target'");
    }

    public static List<Node> getChildNodeList(Node parent) {
        List<Node> childNodes = new ArrayList<>();
        NodeList children = parent.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            childNodes.add(children.item(i));
        }
        return childNodes;
    }

    /**
     * recursively look for a node such that node.getLocalName() equals localName
     *
     * @return the node we found, or null if there's no such node
     */
    private static Node findNodeByLocalNameRecursively(Node node, String localName) {
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
     * @param bugPosition
     * @return all the mutants that can be generated at this bug position, whether can be repaired or not.
     * @throws XPathExpressionException
     * @throws ParsingException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public List<Mutant> generateAllMutants(int bugPosition) throws XPathExpressionException, ParsingException, ParserConfigurationException, SAXException, IOException {
        String xpath = xpathList.get(bugPosition);
        List<Mutant> mutants = new ArrayList<>();
        if (isRuleXpathString(xpath)) {
            mutants.addAll(createRuleEffectFlippingMutants(xpath));
            mutants.addAll(createRuleTargetTrueMutants(xpath));
            mutants.addAll(createRuleConditionTrueMutants(xpath));
            mutants.addAll(createRuleTargetFalseMutants(xpath));
            mutants.addAll(createRuleConditionFalseMutants(xpath));
            mutants.addAll(createRuleChangeComparisonFunctionMutants(xpath));
            mutants.addAll(createAddNotFunctionMutants(xpath));
            mutants.addAll(createRemoveNotFunctionMutants(xpath));
            mutants.addAll(createRemoveRuleMutants(xpath));
            mutants.addAll(createAddNewRuleMutants(xpath));
        } else if (isTargetXpathString(xpath)) {
            mutants.addAll(createPolicyTargetTrueMutants(xpath)); 
            mutants.addAll(createPolicyTargetFalseMutants(xpath)); 
            mutants.addAll(createPolicyTargetChangeComparisonFunctionMutants(xpath));
            mutants.addAll(createCombiningAlgorithmMutants(xpath));
            mutants.addAll(createFirstDenyRuleMutants(xpath));
            mutants.addAll(createFirstPermitRuleMutants(xpath));
        }
        return mutants;
    }

    @SuppressWarnings("unchecked")
    public List<Mutant> generateSelectedMutants(List<String> mutationMethods) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Class<?> cls = this.getClass();
        List<Mutant> mutants = new ArrayList<Mutant>();
        for (String xpath : xpathList) {
            for (String mutationMethod : mutationMethods) {
                if (isRuleXpathString(xpath) && ruleMutationMethods.contains(mutationMethod) ||
                        isTargetXpathString(xpath) && targetMutationMethods.contains(mutationMethod)) {
                    Method method = cls.getDeclaredMethod(mutationMethod, String.class);
                    mutants.addAll((ArrayList<Mutant>) method.invoke(this, xpath));
                }
            }
        }
        return mutants;
    }
    
    @SuppressWarnings("unchecked")
    public List<Mutant> generateSelectedMutantsAndSave(List<String> mutationMethods,String mutantsFolder) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException, IOException {
        Class<?> cls = this.getClass();
        List<Mutant> mutants = new ArrayList<Mutant>();
        for (String xpath : xpathList) {
            for (String mutationMethod : mutationMethods) {
                if (isRuleXpathString(xpath) && ruleMutationMethods.contains(mutationMethod) ||
                        isTargetXpathString(xpath) && targetMutationMethods.contains(mutationMethod)) {
                    Method method = cls.getDeclaredMethod(mutationMethod, String.class);
                    List<Mutant> muts = (ArrayList<Mutant>) method.invoke(this, xpath);
                    for(Mutant mut:muts) {
                    	FileIOUtil.saveMutant(mut,mutantsFolder);
                    	mut.setFolder(mutantsFolder);
						mut.setPolicy(null);
                    }
                    mutants.addAll(muts);
                }
            }
        }
        return mutants;
    }
    
    @SuppressWarnings("unchecked")
    public Map<String,List<Mutant>> generateMutantsCategorizedByMethods(List<String> mutationMethods) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Class<?> cls = this.getClass();
        Map<String,List<Mutant>> mutantsMap = new HashMap<String,List<Mutant>>();
    	for (String mutationMethod : mutationMethods) {
    		List<Mutant> mutants = new ArrayList<Mutant>();
    		for (String xpath : xpathList) {
    			if (isRuleXpathString(xpath) && ruleMutationMethods.contains(mutationMethod) || isTargetXpathString(xpath) && targetMutationMethods.contains(mutationMethod)) {
    				Method method = cls.getDeclaredMethod(mutationMethod, String.class);
    				mutants.addAll((ArrayList<Mutant>) method.invoke(this, xpath));
    				mutantsMap.put(mutationMethod,mutants);
    			}
    		}
    	}
        return mutantsMap;
    }
    
    @SuppressWarnings("unchecked")
    public List<Mutant> generateSelectedMutants(List<String> mutationMethods,int bugPosition) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Class<?> cls = this.getClass();
        List<Mutant> mutants = new ArrayList<>();
        String xpath = xpathList.get(bugPosition);
        
        for (String mutationMethod : mutationMethods) {
        	if (isRuleXpathString(xpath) && ruleMutationMethods.contains(mutationMethod) || isTargetXpathString(xpath) && targetMutationMethods.contains(mutationMethod)) {
        		Method method = cls.getDeclaredMethod(mutationMethod, String.class);
        		mutants.addAll((ArrayList<Mutant>) method.invoke(this, xpath));
            }
        }
        return mutants;
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
            list.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), (baseMutantName.equals("") ? "" : baseMutantName + "_") + "CRE" + faultLocation));
        }
        return list;
    }

    /**
     * Make Policy Target always true
     */
    public List<Mutant> createPolicyTargetTrueMutants(String targetXpathString) throws XPathExpressionException, ParsingException, IOException, ParserConfigurationException, SAXException {
        int faultLocation = xpathMapping.get(targetXpathString);
        return createTargetTrueMutants(targetXpathString, "PTT", faultLocation);

    }

    /**
     * Make Rule Target always true
     */
    public List<Mutant> createRuleTargetTrueMutants(String ruleXpathString) throws XPathExpressionException, ParsingException, IOException, ParserConfigurationException, SAXException {
        int faultLocation = xpathMapping.get(ruleXpathString);
        return createTargetTrueMutants(ruleXpathString + "/*[local-name()='Target' and 1]", "RTT", faultLocation);
    }

    /**
     * Make a Target always true by removing all its child nodes. According to the specification of XACML, a Target is
     * always evaluated to true if it is empty.
     * <p />
     * We remove all the child nodes of a target instead of simply deleting the target node, because according to
     * XACML 3.0 specification, a Policy or PolicySet element must have a Target element as its child node, and a Rule
     * may have a Target element as its child. By removing child nodes of a target we can deal with different kinds of
     * Target elements in a uniform way.
     * <p />
     * see http://docs.oasis-open.org/xacml/3.0/xacml-3.0-core-spec-os-en.html#_Toc325047111 Section 5.6 Element Target
     */
    private List<Mutant> createTargetTrueMutants(String targetXpathString, String mutantName, int faultLocation) throws XPathExpressionException, ParsingException, IOException, ParserConfigurationException, SAXException {
        List<Mutant> list = new ArrayList<>();
        NodeList nodes = (NodeList) xPath.evaluate(targetXpathString, doc.getDocumentElement(), XPathConstants.NODESET);
        Node node = nodes.item(0);
        if (node != null && !isEmptyNode(node)) {
            //change doc
            List<Node> children = getChildNodeList(node);
            for (Node child : children) {
                node.removeChild(child);
            }
            AbstractPolicy newPolicy = PolicyLoader.loadPolicy(doc);
            list.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), (baseMutantName.equals("") ? "" : baseMutantName + "_") + mutantName + faultLocation));
            //restore doc
            for (Node child : children) {
                node.appendChild(child);
            }
        }
        return list;
    }

    /**
     * Make Rule Condition always true
     * <p />
     * We cannot remove all child nodes of Condition as we do to policy target and rule target, because
     * Condition.getInstance() will throw a null pointer exception. So here the whole Condition node is removed from the
     * rule node.
     */
    public List<Mutant> createRuleConditionTrueMutants(String ruleXpathString) throws XPathExpressionException, ParsingException, ParserConfigurationException, SAXException, IOException {
        int faultLocation = xpathMapping.get(ruleXpathString);
        String mutantName = "RCT";
        String conditionXpathString = ruleXpathString + "/*[local-name()='Condition' and 1]";
        List<Mutant> list = new ArrayList<>();
        NodeList nodes = (NodeList) xPath.evaluate(conditionXpathString, doc.getDocumentElement(), XPathConstants.NODESET);
        Node node = nodes.item(0);
        if (!isEmptyNode(node)) {
            //change doc
            Node ruleNode = node.getParentNode();
            ruleNode.removeChild(node);
            AbstractPolicy newPolicy = PolicyLoader.loadPolicy(doc);
            list.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), (baseMutantName.equals("") ? "" : baseMutantName + "_") + mutantName + faultLocation));
            //restore doc
            ruleNode.appendChild(node);
        }
        return list;
    }

    /**
     * Make Rule Target always false
     */
    public List<Mutant> createRuleTargetFalseMutants(String ruleXpathString) throws XPathExpressionException, ParsingException {
        int faultLocation = xpathMapping.get(ruleXpathString);
        String mutantName = "RTF";
        String matchXpathString = ruleXpathString + "/*[local-name()='Target' and 1]/*[local-name()='AnyOf' and 1]/*[local-name()='AllOf' and 1]/*[local-name()='Match' and 1]";
        return createTargetFalseMutants(matchXpathString, faultLocation, mutantName);
    }

    /**
     * Make the Target of a Policy or PolicySet always false
     *
     * @param targetXpathString xpath to the Target of a Policy or PolicySet
     */
    public List<Mutant> createPolicyTargetFalseMutants(String targetXpathString) throws XPathExpressionException, ParsingException {
        int faultLocation = xpathMapping.get(targetXpathString);
        String mutantName = "PTF";
        String matchXpathString = targetXpathString + "/*[local-name()='AnyOf' and 1]/*[local-name()='AllOf' and 1]/*[local-name()='Match' and 1]";
        return createTargetFalseMutants(matchXpathString, faultLocation, mutantName);
    }

    /**
     * If Match element exists, we can make the Target always evaluate to false by adding two conflicting Match elements
     * to the parent of Match element. For example, if the Match element says role == "physician", then we add 2 Match
     * elements: role == "a" and role == "b".
     * <p />
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
        String allOfXpathString = matchXpathString.substring(0, matchXpathString.length()-37);//+ "/*[local-name()='Target' and 1]/*[local-name()='AnyOf' and 1]/*[local-name()='AllOf' and 1]/*[local-name()='Match' and 1]";
        NodeList allOfNodes = (NodeList) xPath.evaluate(allOfXpathString+"]", doc.getDocumentElement(), XPathConstants.NODESET);
        Document d = (Document) doc.cloneNode(true);
        
        for(int i = 0; i < allOfNodes.getLength();i++) {
        	String mxpath = allOfXpathString + "and position()=" + (i+1) +"]/*[local-name()='Match' and 1]";
        NodeList nodes = (NodeList) xPath.evaluate(mxpath, d, XPathConstants.NODESET);
       
        Node matchNode = nodes.item(0);
        if (matchNode != null && !isEmptyNode(matchNode)) {
            //change doc
            List<Node> clonedNodes = new ArrayList<>();
            for (int k = 0; k < 2; k++) {
                Node cloned = matchNode.cloneNode(true);
                clonedNodes.add(cloned);
                //find the AttributeValue child node
                Node attributeValueNode = findNodeByLocalNameRecursively(cloned, "AttributeValue");
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
            //restore doc by removing the two conflicting Match nodes from parent
           // for (Node cloned : clonedNodes) {
           //     matchNode.getParentNode().removeChild(cloned);
           // }
            
//            System.out.println(XpathSolver.nodeToString(matchNode.getParentNode(), false, true));
        }
        }
        if(allOfNodes.getLength()>0) {
        list.add(new Mutant(PolicyLoader.loadPolicy(d), Collections.singletonList(faultLocation), (baseMutantName.equals("") ? "" : baseMutantName + "_") + mutantName + faultLocation));
        }
        
        return list;
    }

    /**
     * Make Rule Condition always false.
     * <p />
     * First find the condition node, then remove and replace it with a condition node we built. The condition node we
     * built is always false because it has two conflicting conditions, e.g. role == "a" and role == "b".
     */
    public List<Mutant> createRuleConditionFalseMutants(String ruleXpathString) throws XPathExpressionException, ParserConfigurationException, IOException, SAXException, ParsingException {
        int faultLocation = xpathMapping.get(ruleXpathString);
        String mutantName = "RCF";
        List<Mutant> mutants = new ArrayList<>();
        String conditionXpathString = ruleXpathString + "/*[local-name()='Condition' and 1]";
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
                mutants.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), (baseMutantName.equals("") ? "" : baseMutantName + "_") + mutantName + faultLocation));
                //restore doc
                parent.removeChild(importedFalseConditionNode);
                parent.appendChild(conditionNode);
//                System.out.println(XpathSolver.nodeToString(parent, false, true));
            }
        }
        return mutants;
    }

    /**
     * Change the comparison function in the target of a policy or a policy set. For example, change "==" to ">", ">=",
     * "<" or "<=".
     *
     * @param targetXpathString xpath string to the target of a policy or a policy set
     * @return a list of mutants generated using this semanticMutation operator, or an empty list if this semanticMutation operator is
     * not applicable this rule
     */
    public List<Mutant> createPolicyTargetChangeComparisonFunctionMutants(String targetXpathString) throws XPathExpressionException, ParsingException {
        int faultLocation = xpathMapping.get(targetXpathString);
        String mutantName = "CCF";
        return createTargetChangeComparisonFunctionMutants(targetXpathString, faultLocation, mutantName, 0);
    }

    /**
     * Change the comparison functions in a rule. The changed comparison function can be in a the target or the condition
     * of the rule.
     * @param ruleXpathString xpath string to a rule element
     */
    public List<Mutant> createRuleChangeComparisonFunctionMutants(String ruleXpathString) throws XPathExpressionException, ParsingException {
        int faultLocation = xpathMapping.get(ruleXpathString);
        String mutantName = "CCF";
        List<Mutant> mutants = new ArrayList<>();
        String ruleTargetXpathString = ruleXpathString + "/*[local-name()='Target' and 1]";
        List<Mutant> tmp = createTargetChangeComparisonFunctionMutants(ruleTargetXpathString, faultLocation, mutantName, 0);
        mutants.addAll(tmp);
        mutants.addAll(createRuleConditionChangeComparisonFunctionMutants(ruleXpathString, faultLocation, mutantName, tmp.size()));
        return mutants;
    }

    /**
     * Change the first comparision function in the condition of a rule.
     * @param ruleXpathString xpath string to a rule element
     */
    private List<Mutant> createRuleConditionChangeComparisonFunctionMutants(String ruleXpathString, int faultLocation, String mutantName, int startCount) throws XPathExpressionException, ParsingException {
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
                    int count = startCount;
                    for (String comparisonFunction : comparisonFunctionMap.get(dataType)) {
                        if (!comparisonFunction.equals(originalComparisonFunction)) {
                            //change doc
                            functionNode.getAttributes().getNamedItem("FunctionId").setNodeValue(comparisonFunction);
                            AbstractPolicy newPolicy = PolicyLoader.loadPolicy(doc);
                            list.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), (baseMutantName.equals("") ? "" : baseMutantName + "_") + mutantName + faultLocation + "_" + count));
                            count++;
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
    private List<Mutant> createTargetChangeComparisonFunctionMutants(String targetXpathString, int faultLocation, String mutantName, int startCount) throws XPathExpressionException, ParsingException {
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
                int count = startCount;
                for (String comparisonFunction : comparisonFunctionMap.get(dataType)) {
                    if (!comparisonFunction.equals(orignalComparisonFunction)) {
                        matchNode.getAttributes().getNamedItem("MatchId").setNodeValue(comparisonFunction);
//                        System.out.println(XpathSolver.nodeToString(matchNode, false, true));
                        AbstractPolicy newPolicy = PolicyLoader.loadPolicy(doc);
                        mutants.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), (baseMutantName.equals("") ? "" : baseMutantName + "_") + mutantName + faultLocation + "_" + count));
                        count++;
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
     * @param ruleXpathString xpath string to a rule
     */
    public List<Mutant> createAddNotFunctionMutants(String ruleXpathString) throws XPathExpressionException, ParsingException, ParserConfigurationException, IOException, SAXException {
        int faultLocation = xpathMapping.get(ruleXpathString);
        String mutantName = "ANF";
        List<Mutant> mutants = new ArrayList<>();
        String conditionXpathString = ruleXpathString + "/*[local-name()='Condition' and 1]";
        Node conditionNode = ((NodeList) xPath.evaluate(conditionXpathString, doc.getDocumentElement(), XPathConstants.NODESET)).item(0);
        if (!isEmptyNode(conditionNode)) {
            List<Node> childNodes = getChildNodeList(conditionNode);
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
            mutants.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), (baseMutantName.equals("") ? "" : baseMutantName + "_") + mutantName + faultLocation));
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
     * @param ruleXpathString xpath string to a rule
     */
    public List<Mutant> createRemoveNotFunctionMutants(String ruleXpathString) throws XPathExpressionException, ParsingException {
        int faultLocation = xpathMapping.get(ruleXpathString);
        String mutantName = "RNF";
        List<Mutant> mutants = new ArrayList<>();
        String conditionXpathString = ruleXpathString + "/*[local-name()='Condition' and 1]";
        Node conditionNode = ((NodeList) xPath.evaluate(conditionXpathString, doc.getDocumentElement(), XPathConstants.NODESET)).item(0);
        if (!isEmptyNode(conditionNode)) {
            Node applyNode = findNodeByLocalNameRecursively(conditionNode, "Apply");
            String notFunctionString = "urn:oasis:names:tc:xacml:1.0:function:not";
            if (applyNode != null && applyNode.getAttributes().getNamedItem("FunctionId").getNodeValue().equals(notFunctionString)) {
                List<Node> childNodes = getChildNodeList(applyNode);
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
                mutants.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), (baseMutantName.equals("") ? "" : baseMutantName + "_") + mutantName + faultLocation));
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
            int count = 0;
            String originalCombiningAlgId = policyNode.getAttributes().getNamedItem(combiningAlgAttribute).getNodeValue();
            for (String combiningAlgoId : combiningAlgList) {
                if (!combiningAlgoId.equals(originalCombiningAlgId)) {
                    policyNode.getAttributes().getNamedItem(combiningAlgAttribute).setNodeValue(combiningAlgoId);
                    AbstractPolicy newPolicy = PolicyLoader.loadPolicy(doc);
                    mutants.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), (baseMutantName.equals("") ? "" : baseMutantName + "_") + mutantName + faultLocation + "_" + count));
                    count++;
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
    public List<Mutant> createRemoveRuleMutants(String ruleXpathString) throws XPathExpressionException, ParsingException {
        int faultLocation = xpathMapping.get(ruleXpathString);
        String mutantName = "RER";
        List<Mutant> mutants = new ArrayList<>();
        Node ruleNode = ((NodeList) xPath.evaluate(ruleXpathString, doc.getDocumentElement(), XPathConstants.NODESET)).item(0);
        Node parent = ruleNode.getParentNode();
        //change doc
        Node nextSibling = ruleNode.getNextSibling();
        parent.removeChild(ruleNode);
        AbstractPolicy newPolicy = PolicyLoader.loadPolicy(doc);
        mutants.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), (baseMutantName.equals("") ? "" : baseMutantName + "_") + mutantName + faultLocation));
        //restore
        //use insertBefore() instead of appendChild() because we want to restore the Rule node to the same previous index
        // as for combining algorithms like "first applicable", the order of rules matters
        parent.insertBefore(ruleNode, nextSibling);
        return mutants;
    }

    /**
     * add a new rule. The new rule is based on this rule, but its effect is flipped, or its target is always true if
     * the target of this rule is not always true.
     * <p />
     * Note that this mutation method should not be used for fault localization as it will cause the position of the
     * following policy elements to shift by 1.
     */
    public List<Mutant> createAddNewRuleMutants(String ruleXpathString) throws XPathExpressionException, ParsingException {
        int faultLocation = xpathMapping.get(ruleXpathString);
        String mutantName = "ANR";
        List<Mutant> mutants = new ArrayList<>();
        Node ruleNode = ((NodeList) xPath.evaluate(ruleXpathString, doc.getDocumentElement(), XPathConstants.NODESET)).item(0);
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
        mutants.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), (baseMutantName.equals("") ? "" : baseMutantName + "_") + mutantName + faultLocation));
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
            mutants.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), (baseMutantName.equals("") ? "" : baseMutantName + "_") + mutantName + faultLocation));
            //restore doc
            parent.removeChild(clone);
        }
        return mutants;
    }

    /**
     * If the targetXpathString points to a Policy target, and rule combining algorithm of the policy is first-applicable,
     * move a rule whose effect is deny before a rule whose effect is permit. The fault location shall be the location of
     * the target.
     * <p />
     * This mutation operator should be used for fault localization, because the positions of policy elements will be
     * changed after moving the rules.
     */
    public List<Mutant> createFirstDenyRuleMutants(String targetXpathString) throws XPathExpressionException, ParsingException {
        return createFirstPermitOrDenyRuleMutants(targetXpathString, "FDR");
    }

    /**
     * If the targetXpathString points to a Policy target, and rule combining algorithm of the policy is first-applicable,
     * move a rule whose effect is deny before a rule whose effect is permit. The fault location shall be the location of
     * the target.
     * <p/>
     * This mutation operator should be used for fault localization, because the positions of policy elements will be
     * changed after moving the rules.
     */
    public List<Mutant> createFirstPermitRuleMutants(String targetXpathString) throws XPathExpressionException, ParsingException {
        return createFirstPermitOrDenyRuleMutants(targetXpathString, "FPR");
    }


    /**
     * If the targetXpathString points to a Policy target, and rule combining algorithm of the policy is first-applicable,
     * move a rule whose effect is effectA before a rule whose effect is effectB. The fault location shall be the location of
     * the target.
     * <p/>
     * effectA and effectB must be different. When effectA == "Permit" and effectB == "Deny", generates first deny rule mutants. When effectA == "Deny" and
     * effectB == "Permit", generates first permit rule mutants.
     */
    private List<Mutant> createFirstPermitOrDenyRuleMutants(String targetXpathString, String mutantName) throws XPathExpressionException, ParsingException {
        int faultLocation = xpathMapping.get(targetXpathString);
        List<Mutant> mutants = new ArrayList<>();
        String effectA, effectB;
        switch (mutantName) {
            case "FDR":
                effectA = "Permit";
                effectB = "Deny";
                break;
            case "FPR":
                effectA = "Deny";
                effectB = "Permit";
                break;
            default:
                return mutants;
        }
        String PolicyXpathString = targetXpathString.replace("/*[local-name()='Target' and 1]", "");
        Node policyNode = ((NodeList) xPath.evaluate(PolicyXpathString, doc.getDocumentElement(), XPathConstants.NODESET)).item(0);
        String firstApplicableCombiningAlgo = "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable";
        if (policyNode.getLocalName().equals("Policy")
                && policyNode.getAttributes().getNamedItem("RuleCombiningAlgId").getNodeValue().equals(firstApplicableCombiningAlgo)) {
            List<Node> childNodes = getChildNodeList(policyNode);
            int i = 0;
            Node nodeA = null;
            for (; i < childNodes.size(); i++) {
                Node child = childNodes.get(i);
                if ("Rule".equals(child.getLocalName()) && child.getAttributes().getNamedItem("Effect").getNodeValue().equals(effectA)) {
                    nodeA = child;
                    break;
                }
            }
            if (nodeA == null) {
                return mutants;
            }
            Node nodeB = null;
            for (; i < childNodes.size(); i++) {
                Node child = childNodes.get(i);
                if ("Rule".equals(child.getLocalName()) && child.getAttributes().getNamedItem("Effect").getNodeValue().equals(effectB)) {
                    nodeB = child;
                    break;
                }
            }
            if (nodeB == null) {
                return mutants;
            }
            //change doc
            Node nodeBNext = nodeB.getNextSibling();
            policyNode.removeChild(nodeB);
            policyNode.insertBefore(nodeB, nodeA);
            AbstractPolicy newPolicy = PolicyLoader.loadPolicy(doc);
            mutants.add(new Mutant(newPolicy, Collections.singletonList(faultLocation), (baseMutantName.equals("") ? "" : baseMutantName + "_") + mutantName + faultLocation));
            //restore doc
            policyNode.removeChild(nodeB);
            policyNode.insertBefore(nodeB, nodeBNext);
        }
        return mutants;
    }

    /**
     * remove default rules
     * @throws SAXException 
     * @throws IOException 
     * @throws ParserConfigurationException 
     */
    public Mutant createRemoveDefaultRulesMutant(AbstractPolicy policy) throws XPathExpressionException, ParsingException, ParserConfigurationException, IOException, SAXException {
    	doc = XMLUtil.loadXMLDocumentFromString(policy.encode().toString());
    	PolicyMetaData policyMetaData = policy.getMetaData();
    	traverse(doc.getDocumentElement(), new ArrayList<String>(), policyMetaData);
    	Mutant mutant = new Mutant(PolicyLoader.loadPolicy(doc),MutationMethodAbbrDirectory.getAbbr("createRemoveDefaultRulesMutant"));
        return mutant;
    }
    
    /**
     */
    public List<Mutant> createRemoveParallelTargetElementMutants(String xPathString) throws XPathExpressionException, ParsingException, IOException, ParserConfigurationException, SAXException {
        int faultLocation = xpathMapping.get(xPathString);
        String targetXpathString = null;
        if(xPathString.contains("[local-name()='Target'")){
        	targetXpathString = xPathString;
        }
        else{
        	targetXpathString = xPathString + "/*[local-name()='Target' and 1]";
        }  
        	
        
    	List<Mutant> list = new ArrayList<>();
        NodeList nodes = (NodeList) xPath.evaluate(targetXpathString, doc.getDocumentElement(), XPathConstants.NODESET);
        Node node = nodes.item(0);
        PolicyMetaData md = PolicyLoader.loadPolicy(doc).getMetaData();
        int anyOfi = 0;
        int allOfi = 0;
        if (node != null && !isEmptyNode(node)) {
            //change doc
            List<Node> children = getChildNodeList(node);
            int allOfCount = 0;
            if(children.size()>1){
	            for (Node child : children) {
	            	if(child!=null && child.getLocalName() !=null && child.getLocalName().equals("AnyOf")){
	            		List<Node> childrenAllOf = getChildNodeList(child);
	            		if(childrenAllOf.size() > 2){
	            			for(Node childAllOf:childrenAllOf){
	            				if(childAllOf.getLocalName() !=null && childAllOf.getLocalName().equals("AllOf")){
	            					allOfCount++;
	            					List<Node> childrenMatchOf = getChildNodeList(childAllOf);
	        	            					
	            					if( AllOfSelection.getInstance(childAllOf,md).getMatches().size() > 1) {
	            						for(int i = 0; i < childrenMatchOf.size(); i++){
	            							Node childMatchOf = childrenMatchOf.get(i);
	    	            					
	    	            					if(childMatchOf.getLocalName() !=null && childMatchOf.getLocalName().equals("Match")){
	    	            						childAllOf.removeChild(childMatchOf);
	    	            						Node nextChild = childMatchOf.getNextSibling();
	    	            						Mutant mutant = new Mutant(PolicyLoader.loadPolicy(doc),(baseMutantName.equals("") ? "" : baseMutantName + "_") +MutationMethodAbbrDirectory.getAbbr("createRemoveParallelTargetElementMutants")+ "_" + faultLocation + "_" + 1 + "_" + i+"_"+i );
	    	            						mutant.addFaultLocationAt(faultLocation, 0);
	    	            						
	    	            						list.add(mutant);
	    	            				        childAllOf.insertBefore(childMatchOf, nextChild);  
	    	            					}
	    	            					
	    	            				}		
	            					}
	            					
	            				}
	            			}
	            			if(allOfCount > 1){
	            				for(int i = 0; i < childrenAllOf.size(); i++){
	            					Node childAllOf = childrenAllOf.get(i);
	            					if(childAllOf.getLocalName() !=null && childAllOf.getLocalName().equals("AllOf")){
	            						child.removeChild(childAllOf);
	            						Node nextChild = childAllOf.getNextSibling();
	            						Mutant mutant = new Mutant(PolicyLoader.loadPolicy(doc),(baseMutantName.equals("") ? "" : baseMutantName + "_") +MutationMethodAbbrDirectory.getAbbr("createRemoveParallelTargetElementMutants")+ "_" + faultLocation + "_" + 1 + "_" + i );
	            						mutant.addFaultLocationAt(faultLocation, 0);
	            						
	            						list.add(mutant);
	            				        child.insertBefore(childAllOf, nextChild);
	            					}
	            					
	            				}
	            			}
	            		}
	            	}
	            }
            }
        }
        return list;
    }
      
    private static void traverse(Node node, List<String> paths, PolicyMetaData policyMetaData) throws ParsingException, IOException {
    	if (XACMLElementUtil.isRule(node)) {
    		if(XACMLElementUtil.isDefaultRule(node, policyMetaData)){
    			Node parent = node.getParentNode();
    			parent.removeChild(node);
    		}
	    }
		
		if (XACMLElementUtil.isPolicy(node) || XACMLElementUtil.isPolicySet(node)) {
		    NodeList children = node.getChildNodes();
	        for (int i = 0; i < children.getLength(); i++) {
	            Node child = children.item(i);
	            if (child instanceof Element && XMLUtil.isTraversableElement(child)) {
	            	traverse(child, paths,policyMetaData);
	            }
	        }
	        if(paths.size()>0){
	        	paths.remove(paths.size() - 1);
	        }
		}	
    }
}
