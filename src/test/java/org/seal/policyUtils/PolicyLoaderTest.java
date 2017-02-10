package org.seal.policyUtils;

import org.junit.Test;
import org.wso2.balana.AbstractPolicy;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * unit test for <code>PolicyLoader</code>
 * Created by shuaipeng on 9/7/16.
 */
public class PolicyLoaderTest {
    @org.junit.Test
    public void getPolicy() throws Exception {
        //Get file from resources folder
        String fileName = "org/seal/policies/conference3/conference3.xml";
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        assertEquals(policy.getId().toString(), "conference");
    }

    @Test
    public void getPolicySet() throws Exception {
        String fileName = "org/seal/policies/policysetExample.xml";
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        assertEquals(policy.getId().toString(), "urn:oasis:names:tc:xacml:3.0:example:policysetid:1");
    }

    @Test
    public void getPolicySetHL7() throws Exception {
        String fileName = "org/seal/policies/HL7/HL7.xml";
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        AbstractPolicy policy = PolicyLoader.loadPolicy(file);
        assertEquals(policy.getId().toString(), "http://axiomatics.com/alfa/identifier/com.axiomatics.hl7.global");
    }
}