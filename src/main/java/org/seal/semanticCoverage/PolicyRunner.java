package org.seal.semanticCoverage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.balana.AbstractPolicy;
import org.wso2.balana.Balana;
import org.wso2.balana.ParsingException;
import org.wso2.balana.ctx.AbstractRequestCtx;
import org.wso2.balana.ctx.AbstractResult;
import org.wso2.balana.ctx.RequestCtxFactory;
import org.wso2.balana.ctx.xacml3.RequestCtx;
import org.wso2.balana.ctx.xacml3.XACML3EvaluationCtx;

/**
 * Created by shuaipeng on 9/8/16.
 */
public class PolicyRunner {
    private static Log logger = LogFactory.getLog(PolicyRunner.class);

    static int evaluate(AbstractPolicy policy, String request) {
        logger.debug("policy: " + policy);
        logger.debug("request: " + request);
        RequestCtxFactory rc = new RequestCtxFactory();
        try {
            AbstractRequestCtx ar = rc.getRequestCtx(request);
            XACML3EvaluationCtx ec = new XACML3EvaluationCtx(new RequestCtx(ar.getAttributesSet(),
                    ar.getDocumentRoot()), Balana.getInstance().getPdpConfig());
            AbstractResult result = policy.evaluate(ec);
            return result.getDecision();
        } catch (ParsingException e) {
            logger.error(e);
            return Integer.MAX_VALUE;
        }
    }


}
