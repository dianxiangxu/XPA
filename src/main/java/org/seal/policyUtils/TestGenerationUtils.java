package org.seal.policyUtils;

import org.seal.semanticMutation.Mutator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.DOMHelper;
import org.wso2.balana.ParsingException;
import org.wso2.balana.PolicyMetaData;
import org.wso2.balana.cond.Condition;
import org.wso2.balana.xacml2.Target;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.seal.policyUtils.XpathSolver.*;

/**
 * Created by shuaipeng on 3/1/17.
 */
public class TestGenerationUtils {
    private static boolean debug = true;

    public static void main(String[] args) throws IOException, ParserConfigurationException, SAXException, ParsingException {
        File file = new File("src/test/resources/org/seal/policies/HL7/HL7.xml");
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        PolicyMetaData metaData = policy.getMetaData();
        Document doc = PolicyLoader.getDocument(new FileInputStream(file));
        List<String> expressions = new ArrayList<>();
        dfs(doc.getDocumentElement(), new ArrayList<String>(), expressions, metaData);
        for (String expression : expressions) {
            System.out.println(expression);
            System.out.println("\n\n\n");
        }
    }

    private static void dfs(Element node, List<String> path, List<String> expressions, PolicyMetaData metaData) throws ParsingException {
        String name = DOMHelper.getLocalName(node);
        if (rulePattern.matcher(name).matches()) {
            Node targetNode = findInChildNodes(node, "Target");
            if (!Mutator.isEmptyNode(targetNode)) {
                if (debug) {
                    path.add(XpathSolver.buildNodeXpath(targetNode));
                } else {
                    Target target = Target.getInstance(targetNode, metaData);
                    String targetExpression = buildTargetExpression(target);
                    path.add(targetExpression);
                }
            }
            Node conditionNode = findInChildNodes(node, "Condition");
            if (!Mutator.isEmptyNode(conditionNode)) {
                if (debug) {
                    path.add(XpathSolver.buildNodeXpath(conditionNode));
                } else {
                    Condition condition = Condition.getInstance(conditionNode, metaData, null);
                    String conditionExpression = buildConditionExpression(condition);
                    path.add(conditionExpression);
                }
            }
            expressions.add(concatenateExpressions(path));
            if (!Mutator.isEmptyNode(targetNode)) {
                path.remove(path.size() - 1);
            }
            if (!Mutator.isEmptyNode(conditionNode)) {
                path.remove(path.size() - 1);
            }
            return;
        }
        if (policyPattern.matcher(name).matches() || policysetPattern.matcher(name).matches()) {
            Node targetNode = findInChildNodes(node, "Target");
            if (targetNode != null) {
                if (debug) {
                    path.add(XpathSolver.buildNodeXpath(targetNode));
                } else {
                    Target target = Target.getInstance(targetNode, metaData);
                    String targetExpression = buildTargetExpression(target);
                    path.add(targetExpression);
                }
            }
            NodeList children = node.getChildNodes();
            for (int i = 0; i < children.getLength(); i++) {
                Node child = children.item(i);
                if (child instanceof Element) {
                    dfs((Element) child, path, expressions, metaData);
                }
            }
            path.remove(path.size() - 1);
        }
    }

    private static Node findInChildNodes(Node parent, String localName) {
        List<Node> childNodes = Mutator.getChildNodeList(parent);
        for (Node child : childNodes) {
            if (localName.equals(child.getLocalName())) {
                return child;
            }
        }
        return null;
    }

    private static String concatenateExpressions(List<String> path) {
        //TODO construct an expression from a list expressions
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            if (i == 0) {
                sb.append(path.get(i));
            } else {
                sb.append("\n").append(path.get(i));
            }
        }
        return sb.toString();
    }

    private static String buildTargetExpression(Target target) {
        //TODO build expression from Target
        return target.encode();
    }

    private static String buildConditionExpression(Condition condition) {
        //TODO build expression from Condition
        return condition.encode();
    }
}
