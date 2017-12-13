package org.seal.xacml.semanticCoverage;

import org.apache.commons.io.IOUtils;
import org.seal.xacml.policyUtils.PolicyLoader;
import org.seal.xacml.policyUtils.XpathSolver;
import org.wso2.balana.AbstractPolicy;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.*;

public class PolicyCoverageFactory {
    private static List<List<Coverage>> coverageMatrix;
    private static List<Boolean> results;
    private static Map<String, Integer> mapping;

    /**
     * This method will be called by AspectJ to insert the Coverage object to the coverage matrix
     * when a policy element is touch by a test.
     * @param coverage
     * @param xPath
     */
    static void addCoverage(Coverage coverage, String xPath) {
        int index = mapping.get(xPath);
        if (index == -1)
            throw new RuntimeException("cannot find xpath: " + xPath);
        coverageMatrix.get(coverageMatrix.size() - 1).set(index, coverage); 
        // should be this coverageMatrix.get(coverageMatrix.size() - 1).add(coverage);
         // coverageMatrix Error to be fixed
        /*if(coverageMatrix.size()>0){
        	coverageMatrix.get(coverageMatrix.size() - 1).add(coverage);
        } else{
        	List<Coverage> c = new ArrayList<Coverage>();
        	c.add(coverage);
        	coverageMatrix.add(c);
        }*/
     }

    /**
     * This method will be called by AspectJ to add a new row in the coverage matrix before
     * each test runs.
     */
    public static void newRow() {
        // the TargetCoverage here is only a position occupier
        // new TargetCoverage(1) means NO_MATCH, will result a 0 in semanticCoverage matrix in the spectrum fault localizer
        coverageMatrix.add(new ArrayList<Coverage>(Collections.nCopies(mapping.size(), new TargetCoverage(1))));
    }

    /**
     * This method will be called by AspectJ to initialize coverageMatrix, results and mapping before
     * each test suite runs.
     * @param policy
     */
    public static void init(AbstractPolicy policy) {
        coverageMatrix = new ArrayList<>();
        InputStream stream = IOUtils.toInputStream(policy.encode(), Charset.defaultCharset());
        try {
            List<String> entryList = XpathSolver.getEntryListRelativeXPath(PolicyLoader.getDocument(stream));
            mapping = new HashMap<>();
            for (int i = 0; i < entryList.size(); i++)
                mapping.put(entryList.get(i), i);
        } catch (IOException | SAXException | ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<List<Coverage>> getCoverageMatrix() {
        return Collections.unmodifiableList(coverageMatrix);
    }

    public static List<Boolean> getResults() {
        return Collections.unmodifiableList(results);
    }

    static void setResults(List<Boolean> testResults) {
        results = testResults;
    }
}
