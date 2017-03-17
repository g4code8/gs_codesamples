package com.example.jaxrsservices;

import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.cdi.ContextName;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.ProcessInstanceWithVariables;
import org.camunda.bpm.engine.variable.VariableMap;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.fasterxml.jackson.databind.ObjectWriter;


@RequestScoped
@Path("bpm")
@Produces({ "application/xml", "application/json","application/text" })
@Consumes({ "application/xml", "application/json","application/text" })
public class BpmServices {
	
	/**
	 * Logging 
	 */
	private final Logger LOGGER = Logger.getLogger(BpmServices.class.getName());
	
	
	/**
	 * Access to the Camunda Process API
	 */
	@Inject
	private RuntimeService runtimeService;
	
	/**
	@Inject
    @ContextName("bpmStpRoute")
    private CamelContext camelContext;
    **/
    
	
	@Inject
    //@ContextName("bpmStpRoute2")
	@ContextName("bpmStpRoute2")
    private CamelContext camelContext2; // this is required as a workaround for a minor Weld annoyance. 
	
	
	/**
	 * Simple test of our ReST, JAX-RS service infrastructure
	 * <br><br>
	 * Not yet engaging camunda
	 * 
	 * @param hello
	 * @return the String echo - in JSON
	 */
	@GET
    @Path("echo/{hello}")
	@Produces(MediaType.APPLICATION_JSON)
    public String echo(@PathParam("hello") String hello){
		
		LOGGER.info("*** echo - hello: " + hello);
		
		// assemble a basic JSON reply and return
		String echoReply = "{\"echoback\": \"" + hello + "\"}";		
		
		return echoReply;
	}
	
	
	
	/**
	 * Starts the process identified by processID and loads the hello message as a process variable. 
	 * 
	 * @param processID
	 * @param hello
	 * @return
	 */
	@GET
    @Path("echoBpm/{processID}/{hello}")
	@Produces(MediaType.APPLICATION_JSON)
    public String echoBpm(@PathParam("processID") String processID, 
    				    @PathParam("hello") String hello ){
		
		LOGGER.info("*** echoBpm - processID: " + processID);
		LOGGER.info("*** echoBpm - hello: " + hello);
		
		ProcessInstanceWithVariables pVariablesInReturn = runtimeService.createProcessInstanceByKey(processID)
												.setVariable("hello", hello)
												.executeWithVariablesInReturn();
		
		String piid = pVariablesInReturn.getProcessInstanceId();
		
		return "{\"processInstanceID\": \"" + piid + "\"}";	
		// String returnVal = producerTemplate.requestBodyAndHeader("direct:startIbmBpmProcess", "msg_body_in_value", "case_id",case_id).toString();
		

	}
	
	
	/**
	 * Simple Camunda process invocation - 
	 * <br>
	 * 	setting one variable at startup 
	 * <br> 
	 *  and the greeting during a java-task implementation. 
	 * 
	 * @param processID
	 * @param hello
	 * @return
	 */
	@GET
    @Path("helloBpm/{processID}/{hello}")
	@Produces(MediaType.APPLICATION_JSON)
    public String helloBpm(@PathParam("processID") String processID, 
    				       @PathParam("hello") String hello ){
		
		LOGGER.info("*** echoBpm - processID: " + processID);
		LOGGER.info("*** echoBpm - hello: " + hello);
		
		ProcessInstanceWithVariables pVariablesInReturn = runtimeService.createProcessInstanceByKey(processID)
												.setVariable("hello", hello)
												.executeWithVariablesInReturn();
		
		String piid = pVariablesInReturn.getProcessInstanceId();
		String camundaGreeting =  pVariablesInReturn.getVariables().get("camundaGreeting").toString();
		
		return "{\"processInstanceID\": \"" + piid + "\", \"camundaGreeting\": \"" + camundaGreeting + "\"}";	

	}	
	
	
	
	
	
	
	
	
	/**
	 * 
	 * @param processID
	 * @param inputProcessVariable
	 * @return
	 * @throws JsonProcessingException 
	 */
	@GET
    @Path("startStpBpm/{processID}/{inputProcessVariable}")
	@Produces(MediaType.APPLICATION_JSON)
    public String startStpBpm(@PathParam("processID") String processID, 
    				    	  @PathParam("inputProcessVariable") String inputProcessVariable ) throws JsonProcessingException{
		
		LOGGER.info("*** startStpBpm - processID: " + processID);
		LOGGER.info("*** startStpBpm - inputProcessVariable: " + inputProcessVariable);
		
		ProcessInstanceWithVariables pVariablesInReturn = runtimeService.createProcessInstanceByKey(processID)
												.setVariable("inputProcessVariable", inputProcessVariable)
												.executeWithVariablesInReturn();
		
		String piid = pVariablesInReturn.getProcessInstanceId();
		boolean isEnded = pVariablesInReturn.isEnded();
		VariableMap variableMap = pVariablesInReturn.getVariables();
		
		// Evaluate the variables after starting the process
		
		LOGGER.info("*** Started: " + processID + " variables: ");
		
		// this time we build a proper JSON return value - using Jackson
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootObjectNode = mapper.createObjectNode();
		
		// set the process instance ID as the root JSON object - we then add variables as child JSON nodes
		rootObjectNode.put("processID", processID)
					  .put("processInstanceID", piid)
					  .put("isEnded", isEnded);
						
		
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
		return prettyWriter.writeValueAsString(rootObjectNode);
					
		// String returnVal = producerTemplate.requestBodyAndHeader("direct:startIbmBpmProcess", "msg_body_in_value", "case_id",case_id).toString();
		

	}
	
	
	/**
	 * 
	 * @param processID
	 * @param inputProcessVariable
	 * @return
	 * @throws JsonProcessingException
	 */
	/**
	@GET
    @Path("startStpBpmViaCamel/{processID}/{inputProcessVariable}")
	@Produces(MediaType.APPLICATION_JSON)
    public String startStpBpmViaCamel(@PathParam("processID") String processID, 
    				    	  		  @PathParam("inputProcessVariable") String inputProcessVariable ) throws JsonProcessingException{
			
		LOGGER.info("*** startStpBpmViaCamel - processID: " + processID);
		LOGGER.info("*** startStpBpmViaCamel - inputProcessVariable: " + inputProcessVariable);
		
		ProducerTemplate producer = camelContext.createProducerTemplate();
		ConsumerTemplate consumer = camelContext.createConsumerTemplate();
		
		//producer.sendBodyAndHeader("direct:startBpmStp2", inputProcessVariable, "processID", processID);
		
		// String response = producer.requestBodyAndHeader("direct:startBpmStp2", inputProcessVariable, "processID", processID,String.class);
		String response = producer.requestBodyAndHeader("jmsBpmStp2:queue:BpmStpQueue2", inputProcessVariable, "processID", processID,String.class);
		
		//consumer.  sendBodyAndHeader("direct:startBpmStp2", inputProcessVariable, "processID", processID);
		
		
		return response;
		// return "foo";
	}
	**/
	
	
	/**
	 * Start a BPM Process via Apache Camel Artemis: Event Orchestration
	 * 
	 * @param camelUri
	 * @param processID
	 * @param inputProcessVariable
	 * @return
	 * @throws JsonProcessingException
	 */
	@GET
    @Path("startStpBpmViaCamel/{camelUri}/{processID}/{inputProcessVariable}")
	@Produces(MediaType.APPLICATION_JSON)
    public String startStpBpmViaCamel(@PathParam("camelUri") String camelUri, 
    								  @PathParam("processID") String processID, 
    				    	  		  @PathParam("inputProcessVariable") String inputProcessVariable ) { 
		
		LOGGER.info("*** startStpBpmViaCamel - camelUri: " + camelUri);
		LOGGER.info("*** startStpBpmViaCamel - processID: " + processID);
		LOGGER.info("*** startStpBpmViaCamel - inputProcessVariable: " + inputProcessVariable);
		
		ProducerTemplate producer = camelContext2.createProducerTemplate(); 
		
		String response = producer.requestBodyAndHeader(camelUri, inputProcessVariable, "processID", processID,String.class);
		
		return response;
	}
	
	
	
	
	
	/**
	 * 
	 * @param hello
	 * @return
	 * @throws JsonProcessingException
	 */
	/**
	@GET
    @Path("WildFlyCamelQueue/{hello}")
	@Produces(MediaType.APPLICATION_JSON)
    public String WildFlyCamelQueue(@PathParam("hello") String hello) throws JsonProcessingException {
			
		LOGGER.info("*** WildFlyCamelQueue - hello: " + hello);
				
		ProducerTemplate producer = camelContext.createProducerTemplate();

		String response = producer.requestBody("jmsBpmStp2:queue:WildFlyCamelQueue", hello, String.class);
		
		return "{\"response\": \"" + response + "\"}";		
	}
	**/

	
	
	
	

}
