package org.seal.xacml.semanticCoverage;

import com.opencsv.CSVReader;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seal.xacml.semanticMutation.Mutant;
import org.seal.xacml.utils.XACMLElementUtil;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.ParsingException;
import org.wso2.balana.ctx.AbstractResult;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shuaipeng on 9/8/16.
 */
public class TestSuite {
    private static Log logger = LogFactory.getLog(TestSuite.class);
    /**
     * having a note for each request can ease debugging process.
     */
    private List<String> requestNotes;
    private List<String> requests;
    private List<String> oracles;

    public TestSuite(List<String> requestNotes, List<String> requests, List<String> oracles) {
        this.requestNotes = requestNotes;
        this.requests = requests;
        this.oracles = oracles;
    }

    public static TestSuite loadTestSuite(File csvFile) {
        try (CSVReader reader = new CSVReader(new FileReader(csvFile))) {
            List<String[]> myEntries = reader.readAll();
            List<String> requestFileNames = new ArrayList<>();
            List<String> requests = new ArrayList<>();
            List<String> oracles = new ArrayList<>();
            for (String[] entry : myEntries) {
                File requestFile = new File(csvFile.getParent() + File.separator + entry[0]);
                requestFileNames.add(entry[0]);
                requests.add(FileUtils.readFileToString(requestFile, StandardCharsets.UTF_8));
                oracles.add(entry[1]);
            }
            return new TestSuite(requestFileNames, requests, oracles);
        } catch (IOException e) {
            logger.error(e);
            throw new RuntimeException("error loading test suite " + csvFile);
        }
    }

    /**
     * a pointercut in aspectJ
     */
    private static boolean runTest(AbstractPolicy policy, String request, String oracleString) {
        int oracle = balanaFinalDecision(oracleString);
        int response = PolicyRunner.evaluate(policy, request);
//        System.out.println(oracleString + ", " + decisionToString(response));
        return response == oracle;
    }
    
    private static boolean isKilledBy(AbstractPolicy policy, String request, String oracleString) throws ParsingException{
        int oracle = balanaFinalDecision(oracleString);
        int response = XACMLElementUtil.evaluateRequestForPolicy(policy, request);
        
//        System.out.println(oracleString + ", " + decisionToString(response));
        return response == oracle;
    }
    
    public static int runTestWithoutOracle(AbstractPolicy policy, String request) {
        int response = PolicyRunner.evaluate(policy, request);
        return response;
    }

    private static int balanaFinalDecision(String decisionString) {
        if (decisionString.equalsIgnoreCase("Permit"))
            return AbstractResult.DECISION_PERMIT;
        else if (decisionString.equalsIgnoreCase("Deny"))
            return AbstractResult.DECISION_DENY;
        else if (decisionString.equalsIgnoreCase("NA") || decisionString.equalsIgnoreCase("NotApplicable")) // new pattern 11/13/14
            return AbstractResult.DECISION_NOT_APPLICABLE;
        else if (decisionString.equalsIgnoreCase("INDETERMINATE"))
            return AbstractResult.DECISION_INDETERMINATE;
        else if (decisionString.equalsIgnoreCase("IP"))
            return AbstractResult.DECISION_INDETERMINATE_PERMIT;
        else if (decisionString.equalsIgnoreCase("ID"))
            return AbstractResult.DECISION_INDETERMINATE_DENY;
        else if (decisionString.equalsIgnoreCase("IDP"))
            return AbstractResult.DECISION_INDETERMINATE_DENY_OR_PERMIT;
        return AbstractResult.DECISION_INDETERMINATE;
    }

    private static String decisionToString(int decision) {
        switch (decision) {
            case AbstractResult.DECISION_PERMIT:
                return "Permit";
            case AbstractResult.DECISION_DENY:
                return "Deny";
            case AbstractResult.DECISION_NOT_APPLICABLE:
                return "NA";
            case AbstractResult.DECISION_INDETERMINATE:
                return "INDETERMINATE";
            case AbstractResult.DECISION_INDETERMINATE_PERMIT:
                return "IP";
            case AbstractResult.DECISION_INDETERMINATE_DENY:
                return "ID";
            case AbstractResult.DECISION_INDETERMINATE_DENY_OR_PERMIT:
                return "IDP";
            default:
                return "INDETERMINATE";
        }
    }

    /**
     * a pointercut in aspectJ
     */
    public List<Boolean> runTests(AbstractPolicy policy) {
        List<Boolean> results = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            results.add(runTest(policy, requests.get(i), oracles.get(i)));
        }
        return results;
    }
    
    public boolean isKilled(AbstractPolicy policy) throws ParsingException {
        boolean flag = false;
    	for (int i = 0; i < requests.size(); i++) {
            if(!isKilledBy(policy, requests.get(i), oracles.get(i))) {
            	flag = true;
            	break;
            }
        }
        return flag;
    }

    public List<Boolean> runTests(Mutant mutant) {
        return runTests(mutant.getPolicy());
    }
    
    public int getSize(){
    	return requests.size();
    }

}
