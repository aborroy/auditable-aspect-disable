package org.alfresco.behaviour.disable;

import java.util.Arrays;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.node.NodeServicePolicies;
import org.alfresco.repo.policy.Behaviour.NotificationFrequency;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.policy.JavaBehaviour;
import org.alfresco.repo.policy.PolicyComponent;
import org.alfresco.repo.transaction.AlfrescoTransactionSupport;
import org.alfresco.repo.transaction.TransactionListener;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.transaction.TransactionListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Disable AUDITABLE Behaviour when adding an aspect "triggeringAspectQName".
 * 
 * "triggeringAspectQName" can be specified at alfresco-global.properties, by default:
 * 
 * triggering.aspect.qname={http://www.alfresco.org/model/content/1.0}templatable
 * 
 * Usage: By default, when a change is performed on a node having AUDITABLE ASPECT,
 *        Alfresco changes "cm:modified" and "cm:modifier" properties.
 *        When using this module, these properties remain unchanged after adding
 *        the Aspect specified in alfresco-global.properties 
 * 
 * @author aborroy
 *
 */
public class DisableAuditableBehaviour implements NodeServicePolicies.BeforeAddAspectPolicy 
{
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DisableAuditableBehaviour.class);
	
    /**
     * ThreadLocal list to store every node related with the transaction.
     */
    private static final String KEY_NODES = DisableAuditableBehaviour.class.getName() + ".nodes";
    
	/**
	 * Policy related Spring Beans
	 */
	private PolicyComponent policyComponent;
	private BehaviourFilter policyBehaviourFilter;
	
	
	/**
	 * QName (url, local name) of the aspect this behaviour is listening to.
	 */
	private String triggeringAspectQName;
	
	/**
	 * Listen to "afterCommit" event to restore the behaviour for every related node.
	 */
	private TransactionListener transactionListener;
	
	/**
	 * Binds "beforeAddAspect" behaviour associated to "triggeringAspectQName" aspect.
	 * Creates the transactionListener instance. 
	 */
	public void init()
	{
		
		this.policyComponent.bindClassBehaviour(
				NodeServicePolicies.BeforeAddAspectPolicy.QNAME,
				QName.createQName(triggeringAspectQName),
				new JavaBehaviour(this, "beforeAddAspect", NotificationFrequency.FIRST_EVENT)
				);
		this.transactionListener = new RestoreAuditableBehaviourTransactionListener();
	}

	/* (non-Javadoc)
	 * @see org.alfresco.repo.node.NodeServicePolicies.BeforeAddAspectPolicy#beforeAddAspect(org.alfresco.service.cmr.repository.NodeRef, org.alfresco.service.namespace.QName)
	 */
	@Override
	public void beforeAddAspect(NodeRef nodeRef, QName aspectQName) 
	{
		
		// Store a list of nodes involved in the transaction
		AlfrescoTransactionSupport.bindListener(transactionListener);
        List<NodeRef> currentNodes = AlfrescoTransactionSupport.getResource(KEY_NODES);
        if (currentNodes == null) {
            currentNodes = Arrays.asList(nodeRef);
        } else {
            currentNodes.add(nodeRef);
        }
        AlfrescoTransactionSupport.bindResource(KEY_NODES, currentNodes);

		
		// Disabling AUDITABLE ASPECT Behaviour for this node
        LOGGER.debug("Disabling auditable behaviour for node {}", nodeRef);
		policyBehaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);
		
	}
	
    /**
     * Listen to "afterCommit" event to restore AUDITABLE ASPECT Behavior 
     * for every related node in the transaction
     * @author aborroy
     *
     */
    private class RestoreAuditableBehaviourTransactionListener 
        extends TransactionListenerAdapter 
        implements TransactionListener 
    {
    	@Override
    	public void afterCommit()
    	{
            List<NodeRef> nodes = AlfrescoTransactionSupport.getResource(KEY_NODES);
            for (NodeRef node : nodes) 
            {
        		LOGGER.debug("Enabling auditable behaviour for node {}", node);
        		policyBehaviourFilter.enableBehaviour(node, ContentModel.ASPECT_AUDITABLE);
            }
    	}

		@Override
		public void flush() 
		{
		}

    }
    
	public void setPolicyComponent(PolicyComponent policyComponent) {
		this.policyComponent = policyComponent;
	}

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	public void setTriggeringAspectQName(String triggeringAspectQName) {
		this.triggeringAspectQName = triggeringAspectQName;
	}

}
