package org.seal.policyUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.balana.*;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by shuaipeng on 10/20/16.
 */
public class XpathSolver {
    private static Log logger = LogFactory.getLog(XpathSolver.class);
    private static Pattern policyPattern = Pattern.compile("(?:\\w+:)*Policy");
    private static Pattern policysetPattern = Pattern.compile("(?:\\w+:)*PolicySet");
    private static Pattern rulePattern = Pattern.compile("(?:\\w+:)*Rule");
    private static Pattern targetPattern = Pattern.compile("(?:\\w+:)*Target");

    /**
     * get the string representation of a DOM node, used for debugging
     *
     * @param node
     * @param omitXmlDeclaration
     * @param prettyPrint
     * @return string representation of a DOM node
     */
    public static String nodeToString(Node node, boolean omitXmlDeclaration, boolean prettyPrint) {
        if (node == null) {
            throw new IllegalArgumentException("node is null.");
        }

        try {
            // Remove unwanted whitespaces
            XPath xpath = XPathFactory.newInstance().newXPath();
            XPathExpression expr = xpath.compile("//text()[normalize-space()='']");
            NodeList nodeList = (NodeList) expr.evaluate(node, XPathConstants.NODESET);

            for (int i = 0; i < nodeList.getLength(); ++i) {
                Node nd = nodeList.item(i);
                nd.getParentNode().removeChild(nd);
            }

            // Create and setup transformer
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            if (omitXmlDeclaration) {
                transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            }

            if (prettyPrint) {
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            }

            // Turn the node into a string
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(node), new StreamResult(writer));
            return writer.toString();
        } catch (TransformerException | XPathExpressionException e) {
            throw new RuntimeException(e);
        }
    }


    public static String nodeToStringTrimmed(Node node, boolean omitXmlDeclaration) {
        Node cloned = node.cloneNode(true);
        trimWhitespace(cloned);
        return nodeToString(cloned, omitXmlDeclaration, false);
    }

    private static void trimWhitespace(Node node) {
        NodeList children = node.getChildNodes();
        for (int i = 0; i < children.getLength(); ++i) {
            Node child = children.item(i);
            if (child.getNodeType() == Node.TEXT_NODE) {
                child.setTextContent(child.getTextContent().trim());
            }
            trimWhitespace(child);
        }
    }

    private static void buildEntryListAbsoluteXPath(List<String> entries, String parentXPath, Element parent) {
        String name = DOMHelper.getLocalName(parent);
        //here we use regex match instead of simply String.equals() because some XACML may use namespace, in such case
        // the local name is "namespace:Policy" instead of "Policy".
        if (rulePattern.matcher(name).matches()
                || targetPattern.matcher(name).matches() && !rulePattern.matcher(DOMHelper.getLocalName(parent.getParentNode())).matches())
            entries.add(parentXPath);

        if (policyPattern.matcher(name).matches() || policysetPattern.matcher(name).matches()) {
            NodeList children = parent.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child instanceof Element) {
                    String identifier = getNodeIdentifier(child);
                    buildEntryListAbsoluteXPath(entries, parentXPath + "/" + identifier, (Element) child);
                }
            }
        }
    }

    /**
     * get the absolute xpathes of all rule and policy target and policyset target elements.
     *
     * @param doc
     * @return a list of absolute xpathes
     */
    public static List<String> getEntryListAbsoluteXPath(Document doc) {
        ArrayList<String> entries = new ArrayList<>();
        Element root = doc.getDocumentElement();
        buildEntryListAbsoluteXPath(entries, "/" + getNodeIdentifier(root), root);
        return entries;
    }

    private static void buildEntryListRelativeXPath(List<String> entries, Element node) {
        String name = DOMHelper.getLocalName(node);
        //here we use regex match instead of simply String.equals() because some XACML may use namespace, in such case
        // the local name is "namespace:Policy" instead of "Policy".
        if (rulePattern.matcher(name).matches()) {
            entries.add("//" + getNodeIdentifier(node));
        } else if (targetPattern.matcher(name).matches() && !rulePattern.matcher(DOMHelper.getLocalName(node.getParentNode())).matches()) {
            entries.add("//" + getNodeIdentifier(node.getParentNode()) + "/" + getNodeIdentifier(node));
        }

        if (policyPattern.matcher(name).matches() || policysetPattern.matcher(name).matches()) {
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child instanceof Element) {
                    buildEntryListRelativeXPath(entries, (Element) child);
                }
            }
        }
    }

    /**
     * get the relative xpathes of all rule, policy target and policyset target elements
     *
     * @param doc
     * @return a list of relative xpathes
     */
    public static List<String> getEntryListRelativeXPath(Document doc) {
        ArrayList<String> entries = new ArrayList<>();
        Element root = doc.getDocumentElement();
        buildEntryListRelativeXPath(entries, root);
        return entries;
    }

    /**
     * given a node, get the absolute xpath of this node by recursively tracing back its ancestors
     *
     * @param node
     * @return
     */
    static String buildNodeXpath(Node node) {
        Node parent = node.getParentNode();
        String parentXpath = "";
        if (parent != null && parent.getParentNode() != null)
            parentXpath = buildNodeXpath(parent);
        return parentXpath + "/" + getNodeIdentifier(node);
    }

    /**
     * get the relative xpath of a <code>Rule</code>.
     * <p>
     * Note that it's impossible to get absolute xpath of any balana elements, including <code>Policy</code> ,
     * <code>Policyset</code>, <code>Rule</code>, <code>Target</code>, because there's no getParent() method in those classes.
     *
     * @param rule
     * @return
     */
    public static String buildRuleXpath(Rule rule) {
        return String.format("//*[local-name()='%s' and @%s='%s']", "Rule", "RuleId", rule.getId());
    }

    /**
     * get the relative xpath of a policy target or a policyset target
     *
     * @param policy
     * @return
     */
    public static String buildTargetXpath(AbstractPolicy policy) {
        String policyString;
        if (policy instanceof Policy)
            policyString = "Policy";
        else if (policy instanceof PolicySet)
            policyString = "PolicySet";
        else
            throw new RuntimeException(policy.getId().toString() + " is neither Policy or PolicySet");
        return String.format("//*[local-name()='%s' and @%s='%s']/*[local-name()='Target' and 1]", policyString, policyString + "Id", policy.getId().toString());
    }

    /**
     * get the identifier of a node in an XACML document, e.g. "Rule[@RuleId='rule13']". A target doesn't have an id
     * attribute, so simply return a "Target[1]".
     * <p>
     * Note that for policy, policyset and rule, the uniqueness of the identifier replies on the uniqueness of their id
     * attribute.
     *
     * @param node a node in an XACML document
     * @return
     */
    private static String getNodeIdentifier(Node node) {
        String nodeName = node.getNodeName();
        String idAttr = "";
        String idValue = "";
        if (policyPattern.matcher(nodeName).matches() || policysetPattern.matcher(nodeName).matches() || rulePattern.matcher(nodeName).matches()) {
            if (policyPattern.matcher(nodeName).matches()) {
                idAttr = "PolicyId";
            } else if (policysetPattern.matcher(nodeName).matches()) {
                idAttr = "PolicySetId";
            } else if (rulePattern.matcher(nodeName).matches()) {
                idAttr = "RuleId";
            }
            idValue = ((Element) node).getAttribute(idAttr);
        }
        String identifier;
        if (StringUtils.isEmpty(idValue))
            identifier = String.format("*[local-name()='%s' and 1]", nodeName);
        else
            identifier = String.format("*[local-name()='%s' and @%s='%s']", nodeName, idAttr, idValue);
        return identifier;
    }
}
