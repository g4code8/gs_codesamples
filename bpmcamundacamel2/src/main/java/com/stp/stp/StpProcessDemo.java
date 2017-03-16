package com.stp.stp;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.cdi.ContextName;
//import org.apache.camel.model.ProcessDefinition;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.cdi.annotation.ProcessVariable;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.variable.VariableMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ObjectWriter;


@Named("stpProcessDemo")
public class StpProcessDemo {
	/**
	 * The logger
	 */
	private final Logger LOGGER = Logger.getLogger(StpProcessDemo.class.getName());
	
	
	// Camel context name 	
	@Inject
    @ContextName("bpmStpRoute2")
    private CamelContext camelContext;
	
	
	// --------------------------
	// Camunda Process Variables 
	// --------------------------	
	//@Inject @ProcessVariable("yetAnotherProcessVariable")
	//private Object yetAnotherProcessVariable;
	
	@Inject @ProcessVariable("jMSReplyToName")
	private Object jMSReplyToName;
	
	@Inject @ProcessVariable("jMSCorrelationID")
	private Object jMSCorrelationID;
	
	
	
	/**
	 * 
	 * @param execution
	 * @throws Exception
	 */
	public void addProcessVariable(DelegateExecution execution) throws Exception {
		
		LOGGER.info("*** addProcessVariable: invoked");
		
		// yetAnotherProcessVariable = "foo";
		execution.setVariable("yetAnotherProcessVariable", "foo");
		
		//LOGGER.info("*** yetAnotherProcessVariable: " + yetAnotherProcessVariable.toString());
		
		
	}
	
	/**
	 * 
	 * @param execution
	 * @throws Exception
	 */
	public void camundaSaysHello(DelegateExecution execution) {
		
		LOGGER.info("*** camundaSaysHello: invoked");
		
		execution.setVariable("camundaGreeting", "Hello from Camunda");
		
	}
	
	
	
	/**
	 * 
	 * @param execution
	 * @throws Exception
	 */
	public void returnProcessVariables2(DelegateExecution execution) throws Exception {
		
		RepositoryService repositoryService = execution.getProcessEngineServices().getRepositoryService();
		ProcessDefinition definition = repositoryService.getProcessDefinition(execution.getProcessDefinitionId());
		String processID = definition.getKey();
		
		String piid = execution.getProcessInstanceId();
		
		Map<String, Object> variableMap = execution.getProcessInstance().getVariables();
		
		// this time we build a proper JSON return value - using Jackson
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootObjectNode = mapper.createObjectNode();
		
		// set the process instance ID as the root JSON object - we then add variables as child JSON nodes
		rootObjectNode.put("processID", processID)
					  .put("processInstanceID", piid);
		
		// create the JSON node to hold the variables		
		ObjectNode processVariablesNode = mapper.createObjectNode();
		// attach to parent 
		rootObjectNode.set("processVariables", processVariablesNode);
		
		// print process variables and append to return JSON object
		variableMap.forEach((processVariableName,processVariableValue) -> 
								{
									// log values
									LOGGER.info(processVariableName.toString() + " : " + processVariableValue.toString());
									
									// build JSON return
									processVariablesNode.put(processVariableName.toString(), processVariableValue.toString());
								});
				
		// create writer and return pretty output
		ObjectWriter prettyWriter = mapper.writer().withDefaultPrettyPrinter();
		String jsonResults =  prettyWriter.writeValueAsString(rootObjectNode);
		
		Map<String, Object> camelHeaderMap = new HashMap<>();
		camelHeaderMap.put("JMSCorrelationID", jMSCorrelationID.toString());
		
		String camelUri = "jmsBpm:queue:" + jMSReplyToName.toString();
		
		ProducerTemplate producer = camelContext.createProducerTemplate();
		
		LOGGER.info("*** returnProcessVariables sending to Camel URI: " + camelUri);
		LOGGER.info("*** returnProcessVariables jsonResults: " + jsonResults);
		producer.sendBodyAndHeaders(camelUri, jsonResults, camelHeaderMap);
		

	}
	
	
	
	
}
