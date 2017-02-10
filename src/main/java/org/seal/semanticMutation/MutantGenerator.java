package org.seal.semanticMutation;

import org.apache.commons.io.IOUtils;
import org.seal.policyUtils.PolicyLoader;
import org.seal.policyUtils.XpathSolver;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.ParsingException;
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
import java.util.List;

/**
 * Created by shuaipeng on 10/31/16.
 */
//public class MutantGenerator implements Iterable<Mutant> {
public class MutantGenerator {

    public MutantGenerator(Mutant baseMutant) {

    }

//    @Override
//    public Iterator<Mutant> iterator() {
//        return new Iterator<Mutant>() {
//            @Override
//            public boolean hasNext() {
//                return false;
//            }
//
//            @Override
//            public Mutant next() {
//                return null;
//            }
//        };
//    }


    public static void main(String[] args) throws ParserConfigurationException, ParsingException, SAXException, IOException, XPathExpressionException {
//        String fileName = "org/seal/policies/HL7/HL7.xml";
//        ClassLoader classLoader = MutantGenerator.class.getClassLoader();
//        File file = new File(classLoader.getResource(fileName).getFile());
        File file = new File("src/test/resources/org/seal/policies/HL7/HL7.xml");
        // by load the policy and then encode it back to string, we replace the namespace declaration with default namespace declaration
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        InputStream stream = IOUtils.toInputStream(policy.encode(), Charset.defaultCharset());
        System.out.println(policy.encode());
        // TODO why?
        Document doc = PolicyLoader.getDocument(stream);


//        File file = new File("src/test/resources/org/seal/policies/HL7/HL7.encoded.xml");
//        InputStream stream = new FileInputStream(file);
//        Document doc = PolicyLoader.getDocument(stream, true);

        List<String> list = XpathSolver.getEntryListAbsoluteXPath(doc);
//        AbstractPolicy newPolicy = PolicyLoader.loadPolicy(doc);

        int faultLocation = 2;
        String xpathString = list.get(faultLocation);
//        String xpathString = "//*[local-name()='PolicySet' and @PolicySetId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global']/*[local-name()='Target' and 1]";
//        String xpathString = "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes.createNote']";

        //Evaluate XPath against Document itself
        XPath xPath = XPathFactory.newInstance().newXPath();
        // get node by xpath string
        NodeList nodes = (NodeList) xPath.evaluate(xpathString,
                doc.getDocumentElement(), XPathConstants.NODESET);
        // the xpath should identify a unique node
        Node node = nodes.item(0);
        System.out.println(node.getAttributes().getNamedItem("RuleId"));
        System.out.println(node.getAttributes().getNamedItem("Effect"));
        node.getAttributes().getNamedItem("Effect").setTextContent("Deny");
        System.out.println(node.getAttributes().getNamedItem("Effect").getNodeName());
        System.out.println(node.getAttributes().getNamedItem("Effect").getNodeValue());
        System.out.println(XpathSolver.nodeToString(node, false, true));
        AbstractPolicy newPolicy = PolicyLoader.loadPolicy(doc);
        System.out.println(newPolicy.encode());



    }


}
