package org.seal.semanticMutation;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
 * Created by Brady on 3/6/2017.
 */
public class RequestGenerator {

    public static String generateRequest(JSONObject object) throws JSONException {
        StringBuilder str = new StringBuilder();
        str.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><xacml-ctx:Request ReturnPolicyIdList=\"true\" CombinedDecision=\"false\" xmlns:xacml-ctx=\"urn:oasis:names:tc:xacml:3.0:core:schema:wd-17\">\n");
        JSONObject category  = object.getJSONObject("environment");
        JSONArray attributes;
        str.append("\t<xacml-ctx:Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:environment\" >\n");
        if(category.has("attributes")) {
            attributes = category.getJSONArray("attributes");
            str.append(getAttributes(attributes));
        }
        str.append("\t</xacml-ctx:Attributes>\n");

        category  = object.getJSONObject("resource");
        str.append("\t<xacml-ctx:Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:resource\" >\n");
        if(category.has("attributes")) {
            attributes = category.getJSONArray("attributes");
            str.append(getAttributes(attributes));
        }
        str.append("\t</xacml-ctx:Attributes>\n");

        category  = object.getJSONObject("action");
        str.append("\t<xacml-ctx:Attributes Category=\"urn:oasis:names:tc:xacml:3.0:attribute-category:action\" >\n");
        if(category.has("attributes")) {
            attributes = category.getJSONArray("attributes");
            str.append(getAttributes(attributes));
        }
        str.append("\t</xacml-ctx:Attributes>\n");

        category  = object.getJSONObject("subject");
        str.append("\t<xacml-ctx:Attributes Category=\"urn:oasis:names:tc:xacml:1.0:subject-category:access-subject\" >\n");
        if(category.has("attributes")) {
            attributes = category.getJSONArray("attributes");
            str.append(getAttributes(attributes));
        }
        str.append("\t</xacml-ctx:Attributes>\n");

        str.append("</xacml-ctx:Request>");
        return str.toString();
    }

    public static String getAttributes(JSONArray attributes) throws JSONException {
        StringBuilder str = new StringBuilder();
        if(attributes.length() != 0) {
            for (int i = 0; i < attributes.length(); i++) {
                JSONObject obj = (JSONObject) attributes.get(i);
                str.append("\t\t<xacml-ctx:Attribute AttributeId=\"" + obj.get("AttributeId") + "\" IncludeInResult=\"true\">\n");
                str.append("\t\t\t<xacml-ctx:AttributeValue DataType=\"" + obj.get("DataType") + "\">" + obj.get("value") + "</xacml-ctx:AttributeValue>\n");
                str.append("\t\t</xacml-ctx:Attribute>\n");
            }
        }

        return str.toString();
    }

    public JSONObject createRequestObject(String[] environment, String[] resource, String[] actionString, String[] subjectString) throws JSONException {
        JSONObject obj = new JSONObject();

        JSONObject env = new JSONObject();
        if(environment != null) {
            JSONArray envAtts = new JSONArray();
            for(String att : environment) {
                JSONObject first = new JSONObject();
                first.put("AttributeId", "com.axiomatics.hl7.object.objectType");
                first.put("DataType", "http://www.w3.org/2001/XMLSchema#string");
                first.put("value", att);
                envAtts.put(first);
            }
            env.put("attributes", envAtts);
        }
        obj.put("environment", env);

        JSONObject res = new JSONObject();
        if(resource != null) {
            JSONArray atts = new JSONArray();

            for(int i = 0; i < resource.length; i=i+2) {
                JSONObject next = new JSONObject();
                next.put("AttributeId", resource[i]);
                next.put("DataType", "http://www.w3.org/2001/XMLSchema#string");
                next.put("value", resource[i+1]);
                atts.put(next);
            }
            res.put("attributes", atts);
        }
        obj.put("resource", res);

        JSONObject action = new JSONObject();
        if(actionString != null) {
            JSONObject two = new JSONObject();
            JSONArray atts2 = new JSONArray();
            for(String att: actionString) {
                two.put("AttributeId", "com.axiomatics.hl7.action.id");
                two.put("DataType", "http://www.w3.org/2001/XMLSchema#string");
                two.put("value", att);
                atts2.put(two);
            }
            action.put("attributes", atts2);
        }
        obj.put("action", action);

        JSONObject subject = new JSONObject();
        if(subjectString != null) {
            JSONObject three = new JSONObject();
            JSONArray atts3 = new JSONArray();
            for(int i = 0; i < subjectString.length; i=i+2) {
                three.put("AttributeId", subjectString[i]);
                three.put("DataType", "http://www.w3.org/2001/XMLSchema#string");
                three.put("value", subjectString[i+1]);
                atts3.put(three);
            }
            subject.put("attributes", atts3);
        }
        obj.put("subject", subject);
        return obj;
    }

   @Test
    public void createRemoveRuleMutantsTest() throws IOException, SAXException, ParserConfigurationException, ParsingException, XPathExpressionException, JSONException {
        File file = new File("src/test/resources/org/seal/policies/HL7/HL7.xml");
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        Mutator mutator = new Mutator(new Mutant(policy, ""));
        String ruleXpathString = "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.clinicalObjectAccess.clinicalObjectAccess']";
        List<Mutant> mutants = mutator.createRemoveRuleMutants(ruleXpathString);
        Assert.assertEquals(1, mutants.size());
        Mutant mutant = mutants.get(0);
        String[] environment = null;
        String[] resource = new String[2];
        resource[0] = "com.axiomatics.hl7.object.objectType";
        resource[1] = "Clinical Object";
        String[] actionString = new String[1];
        actionString[0] = "read";
        String[] subjectString = new String[2];
        subjectString[0] = "com.axiomatics.hl7.user.role";
        subjectString[1] = "physician";
        JSONObject obj = createRequestObject(environment, resource, actionString, subjectString);
        String request = generateRequest(obj);
        TestSuite testSuite = new TestSuite(Collections.singletonList(""), Collections.singletonList(request), Collections.singletonList("Deny"));
        List<Boolean> results = testSuite.runTests(mutant);
        for (Boolean res : results) {
            Assert.assertTrue(res);
        }
    }

    @Test
    public void createAddNotFunctionMutantsTest() throws IOException, SAXException, ParserConfigurationException, ParsingException, XPathExpressionException, JSONException {
        File file = new File("src/test/resources/org/seal/policies/HL7/HL7.xml");
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        Mutator mutator = new Mutator(new Mutant(policy, ""));
        String ruleXpathString = "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes.createNote']";
        List<Mutant> mutants = mutator.createAddNotFunctionMutants(ruleXpathString);
        Assert.assertEquals(1, mutants.size());
        Mutant mutant = mutants.get(0);
        String[] environment = null;
        String[] resource = new String[4];
        resource[0] = "com.axiomatics.hl7.patient.primaryPhysician";
        resource[1] = "123";
        resource[2] = "com.axiomatics.hl7.object.objectType";
        resource[3] = "progress note";
        String[] actionString = new String[1];
        actionString[0] = "create";
        String[] subjectString = new String[4];
        subjectString[0] = "com.axiomatics.hl7.user.role";
        subjectString[1] = "physician";
        subjectString[2] = "com.axiomatics.hl7.user.requestorId";
        subjectString[3] = "123";
        JSONObject obj = createRequestObject(environment, resource, actionString, subjectString);
        String request = generateRequest(obj);
        TestSuite testSuite = new TestSuite(Collections.singletonList(""), Collections.singletonList(request), Collections.singletonList("Deny"));
        List<Boolean> results = testSuite.runTests(mutant);
        for (Boolean res : results) {
            Assert.assertTrue(res);
        }
    }

    @Test
    public void createRuleConditionTrueMutants() throws IOException, SAXException, ParserConfigurationException, ParsingException, XPathExpressionException, JSONException {
        File file = new File("src/test/resources/org/seal/policies/HL7/HL7.xml");
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        Mutator mutator = new Mutator(new Mutant(policy, ""));
        String ruleXpathString = "//*[local-name()='Rule' and @RuleId='http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global.progressNotes.createNote']";
        List<Mutant> mutants = mutator.createRuleConditionTrueMutants(ruleXpathString);
        Assert.assertEquals(1, mutants.size());
        Mutant mutant = mutants.get(0);
        String[] environment = null;
        String[] resource = new String[4];
        resource[0] = "com.axiomatics.hl7.patient.primaryPhysician";
        resource[1] = "123";
        resource[2] = "com.axiomatics.hl7.object.objectType";
        resource[3] = "progress note";
        String[] actionString = new String[1];
        actionString[0] = "create";
        String[] subjectString = new String[4];
        subjectString[0] = "com.axiomatics.hl7.user.role";
        subjectString[1] = "physician";
        subjectString[2] = "com.axiomatics.hl7.user.requestorId";
        subjectString[3] = "123";
        JSONObject obj = createRequestObject(environment, resource, actionString, subjectString);
        String request = generateRequest(obj);
        TestSuite testSuite = new TestSuite(Collections.singletonList(""), Collections.singletonList(request), Collections.singletonList("Deny"));
        List<Boolean> results = testSuite.runTests(mutant);
        for (Boolean res : results) {
            Assert.assertTrue(res);
        }
    }

}
