package org.seal.semanticMutation;

import org.wso2.balana.*;
import org.wso2.balana.combine.CombinerElement;
import org.wso2.balana.combine.CombiningAlgorithm;
import org.wso2.balana.ctx.AbstractResult;
import org.wso2.balana.ctx.EvaluationCtx;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Created by shuaipeng on 9/8/16.
 */
public class Mutant extends AbstractPolicy {
    private String name;
    private AbstractPolicy policy;
    /**
     * unmodifiable list
     */
    private List<Integer> faultLocations;

    public Mutant(AbstractPolicy policy, String name) {
        this(policy, new ArrayList<Integer>(), name);
    }

    public Mutant(AbstractPolicy policy, List<Integer> faultLocations, String name) {
        super();
        this.policy = policy;
        this.faultLocations = Collections.unmodifiableList(faultLocations);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public List<Integer> getFaultLocations() {
        return faultLocations;
    }

    public AbstractPolicy getPolicy() {
        return policy;
    }

    @Override
    public String encode() {
        return policy.encode();
    }

    @Override
    public void encode(StringBuilder builder) {
        policy.encode(builder);
    }


    public String getSubjectPolicyValue() {
        return policy.getSubjectPolicyValue();
    }

    public void setSubjectPolicyValue(String subjectPolicyValue) {
        policy.setSubjectPolicyValue(subjectPolicyValue);
    }

    public String getResourcePolicyValue() {
        return policy.getResourcePolicyValue();
    }

    public void setResourcePolicyValue(String resourcePolicyValue) {
        policy.setResourcePolicyValue(resourcePolicyValue);
    }

    public String getActionPolicyValue() {
        return policy.getActionPolicyValue();
    }

    public void setActionPolicyValue(String actionPolicyValue) {
        policy.setActionPolicyValue(actionPolicyValue);
    }

    public String getEnvPolicyValue() {
        return policy.getEnvPolicyValue();
    }

    public void setEnvPolicyValue(String envPolicyValue) {
        policy.setEnvPolicyValue(envPolicyValue);
    }


    /**
     * Returns the id of this policy
     *
     * @return the policy id
     */
    public URI getId() {
        return policy.getId();
    }

    /**
     * Returns the version of this policy. If this is an XACML 1.x policy then this will always
     * return <code>"1.0"</code>.
     *
     * @return the policy version
     */
    public String getVersion() {
        return policy.getVersion();
    }

    /**
     * Returns the combining algorithm used by this policy
     *
     * @return the combining algorithm
     */
    public CombiningAlgorithm getCombiningAlg() {
        return policy.getCombiningAlg();
    }

    /**
     * Returns the list of input parameters for the combining algorithm. If this is an XACML 1.x
     * policy then the list will always be empty.
     *
     * @return a <code>List</code> of <code>CombinerParameter</code>s
     */
    public List getCombiningParameters() {
        return policy.getCombiningParameters();
    }

    /**
     * Returns the given description of this policy or null if there is no description
     *
     * @return the description or null
     */
    public String getDescription() {
        return policy.getDescription();
    }

    /**
     * Returns the target for this policy
     *
     * @return the policy's target
     */
    public AbstractTarget getTarget() {
        return policy.getTarget();
    }

    /**
     * Returns the XPath version to use or null if none was specified
     *
     * @return XPath version or null
     */
    public String getDefaultVersion() {
        return policy.getDefaultVersion();
    }

    /**
     * Returns the <code>List</code> of children under this node in the policy tree. Depending on
     * what kind of policy this node represents the children will either be
     * <code>AbstractPolicy</code> objects or <code>Rule</code>s.
     *
     * @return a <code>List</code> of child nodes
     */
    public List<PolicyTreeElement> getChildren() {
        return policy.getChildren();
    }

    /**
     * Returns the <code>List</code> of <code>CombinerElement</code>s that is provided to the
     * combining algorithm. This returns the same set of children that <code>getChildren</code>
     * provides along with any associated combiner parameters.
     *
     * @return a <code>List</code> of <code>CombinerElement</code>s
     */
    public List<CombinerElement> getChildElements() {
        return policy.getChildElements();
    }

    /**
     * Returns the Set of obligations for this policy, which may be empty
     *
     * @return the policy's obligations
     */
    public Set getObligationExpressions() {
        return policy.getObligationExpressions();
    }

    /**
     * Returns the Set of advice expressions for this policy, which may be empty
     *
     * @return the policy's advice expressions
     */
    public Set getAdviceExpressions() {
        return policy.getAdviceExpressions();
    }

    /**
     * Returns the meta-data associated with this policy
     */
    public PolicyMetaData getMetaData() {
        return policy.getMetaData();
    }

    /**
     * Given the input context sees whether or not the request matches this policy. This must be
     * called by combining algorithms before they evaluate a policy. This is also used in the
     * initial policy finding operation to determine which top-level policies might apply to the
     * request.
     *
     * @param context the representation of the request
     * @return the result of trying to match the policy and the request
     */
    public MatchResult match(EvaluationCtx context) {
        return policy.match(context);
    }


    /**
     * Tries to evaluate the policy by calling the combining algorithm on the given policies or
     * rules. The <code>match</code> method must always be called first, and must always return
     * MATCH, before this method is called.
     *
     * @param context the representation of the request
     * @return the result of evaluation
     */
    public AbstractResult evaluate(EvaluationCtx context) {
        return policy.evaluate(context);
    }

}
