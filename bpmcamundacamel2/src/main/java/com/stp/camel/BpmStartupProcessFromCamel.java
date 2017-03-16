package com.stp.camel;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.Topic;

// import org.apache.activemq.command.ActiveMQQueue;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;

@Named("bpmStartupProcessFromCamel")
public class BpmStartupProcessFromCamel {
	/**
	 * logger
	 */
	private final Logger LOGGER = Logger.getLogger(BpmStartupProcessFromCamel.class.getName());
	
	/**
	 * Access to the Camunda Process API
	 */
	@Inject
	private RuntimeService runtimeService;
	
	
	
	/**
	 * Start Camunda Process
	 * 
	 * @param jMSReplyTo
	 * @param jMSCorrelationID
	 * @param processID
	 * @param inputProcessVariable
	 * @return
	 * @throws JMSException
	 */
	public String startStpBpm(Destination jMSReplyTo, 
							  String jMSCorrelationID, 
							  String processID, 
							  String inputProcessVariable)  throws JMSException {
		
		LOGGER.info("*** startStpBpm - processID: " + processID);
		LOGGER.info("*** startStpBpm - inputProcessVariable: " + inputProcessVariable);
		
		// get the name of the reply queue or topic (this could be a temporary queue!)
		String jMSReplyToName = null;
		if (jMSReplyTo instanceof Queue) {
			Queue queue = (Queue)jMSReplyTo;
			jMSReplyToName = queue.getQueueName();
		} else if(jMSReplyTo instanceof Topic) {
			Topic topic = (Topic)jMSReplyTo;
			jMSReplyToName = topic.getTopicName();
		} else {
			System.out.println("*** BpmCamundaBean - NO REPLY DESTINATION!!!");	
		}
		
		LOGGER.info("*** startStpBpm - jMSReplyToName: " + jMSReplyToName);
		
		ProcessInstanceWithVariables pVariablesInReturn = runtimeService.createProcessInstanceByKey(processID)
				.setVariable("inputProcessVariable", inputProcessVariable)
				.setVariable("jMSReplyToName", jMSReplyToName)
				.setVariable("jMSCorrelationID", jMSCorrelationID)
				.executeWithVariablesInReturn();
		
		// just get the process instance id 
		String piid = pVariablesInReturn.getProcessInstanceId();
		
		LOGGER.info("*** startStpBpm - process started piid: " + piid);
		
		return piid; // NOTE: This value should be thrown away by Camel since the reply 
					 //			is scheduled to come back later on the reply-queue (temp-queue)

	}
	
	
	
	
	
	
	

}
