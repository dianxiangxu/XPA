package org.seal.semanticRepair;

import org.seal.semanticCoverage.TestSuite;
import org.seal.semanticMutation.Mutant;
import org.seal.semanticMutation.Mutator;
import org.wso2.balana.ParsingException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 * Created by speng on 3/12/17.
 */
public class Repairer {

    /**
     * @param booleanList
     * @return result of logical AND on all elements of the boolean array
     */
    private static boolean booleanListAnd(List<Boolean> booleanList) {
        boolean result = true;
        for (boolean b : booleanList) {
            result = result && b;
        }
        return result;
    }

    String repair(Mutant policyToRepair, TestSuite testSuite, String scoringMethod, int maxSearchLayer) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException, ParsingException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Queue<MutantNode> queue = new PriorityQueue<>();
        queue.add(new MutantNode(null, policyToRepair, testSuite, scoringMethod, 0, 0));
        MutantNode node = null;
        boolean foundRepair = false;
        while (!queue.isEmpty()) {
            node = queue.poll();
            List<Boolean> testResults = node.getTestResult();
            if (booleanListAnd(testResults)) {
                foundRepair = true;
                break;//found a repair
            }
            if (!node.isPromising()) {
                continue;
            }
            if (node.getLayer() + 1 > maxSearchLayer)
                continue;
            List<Integer> suspicionRank = node.getSuspicionRank();
            Mutator mutator = new Mutator(node.getMutant());
            int rank = 1;
            for (int bugPosition : suspicionRank) {
                List<Mutant> mutantList = mutator.generateAllMutants(bugPosition);
                for (Mutant mutant : mutantList) {
                    queue.add(new MutantNode(node, mutant, testSuite, scoringMethod, rank, node.getLayer() + 1));
                }
                rank++;
            }
        }
        assert node != null;
        String res = node.getMutant().getName();
        if (foundRepair)
            return res;
        return null;
    }

}
