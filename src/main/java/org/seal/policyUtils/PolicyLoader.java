package org.seal.policyUtils;

import com.opencsv.CSVReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.seal.semanticMutation.Mutant;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.wso2.balana.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * this class loads {@link Policy}, {@link PolicySet} or {@link Mutant} from files.
 * Created by shuaipeng on 9/7/16.
 */
public class PolicyLoader {
    private static Log logger = LogFactory.getLog(PolicyLoader.class);

    /**
     * read XML from inputStream, and turn it into a Document.
     * @param inputStream
     * @return
     * @throws IOException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public static Document getDocument(InputStream inputStream) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setIgnoringComments(true);
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        DocumentBuilder db = factory.newDocumentBuilder();
        return db.parse(inputStream);
    }

    /**
     * read a file and try to get a <code>Policy</code> or <code>PolicySet</code> object from it
     *
     * @param file XACML file to read
     * @return a  <code>Policy</code> or <code>PolicySet</code> object, or null if an error occurred.
     */
    public static AbstractPolicy loadPolicy(File file) throws ParserConfigurationException, SAXException, IOException, ParsingException {
        try (InputStream stream = new FileInputStream(file)) {
            // set namespaceAware to true because some XACML files have namespcace declaration
            Document doc = getDocument(stream);
            return loadPolicy(doc);
        }
    }

    public static AbstractPolicy loadPolicy(Document doc) throws ParsingException {
        Element root = doc.getDocumentElement();
            if (DOMHelper.getLocalName(root).equals("Policy"))
                return Policy.getInstance(root);
            else if (DOMHelper.getLocalName(root).equals("PolicySet"))
                return PolicySet.getInstance(root);
            else
                throw new ParsingException("Cannot create Policy or PolicySet from root of type "
                        + DOMHelper.getLocalName(root));
    }

    public static List<Mutant> readMutantsCSVFile(File mutantsCSVFile) throws IOException {
        CSVReader reader = new CSVReader(new FileReader(mutantsCSVFile));
        List<Mutant> mutantList = new ArrayList<>();
        for (String[] entry : reader) {
            String mutantName = entry[0];
            String fileName = mutantsCSVFile.getParent() + File.separator + entry[1];
            List<Integer> bugPositions = StringToIntList(entry[2]);
            try {
                mutantList.add(new Mutant(PolicyLoader.loadPolicy(new File(fileName)), bugPositions, mutantName));
            } catch (ParserConfigurationException | SAXException | ParsingException e) {
                logger.error(e);
            }
        }
        reader.close();
        return mutantList;
    }

    private static List<Integer> StringToIntList(String str) {
        List<Integer> results = new ArrayList<>();
        if (StringUtils.isEmpty(str))
            return results;
        String[] strs = str.replace("[", "").replace("]", "").split(",");
        for (String str1 : strs) {
            results.add(Integer.parseInt(str1.trim()));
        }
        return results;
    }

}
