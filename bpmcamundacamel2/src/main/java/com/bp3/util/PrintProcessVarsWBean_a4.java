package com.bp3.util;

import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Named;

import org.camunda.bpm.engine.delegate.DelegateExecution;


@Named("printProcessVarsWBean_a4")
public class PrintProcessVarsWBean_a4 {
	/**
	 * The logger
	 */
	private final Logger LOGGER = Logger.getLogger(PrintProcessVarsWBean_a4.class.getName());
	
	/**
	 * Print Camunda process variables
	 * 
	 * @param execution
	 * @throws Exception
	 */
	public void printvariables(DelegateExecution execution) throws Exception {
		
		LOGGER.info("*******************");
		LOGGER.info("*** Process ID: "+execution.getProcessDefinitionId());
		LOGGER.info("*** TASK ID: "+execution.getCurrentActivityId());
	
		Map<String, Object> processVars = execution.getVariables();
		Iterator<Map.Entry<String,Object>> entries = processVars.entrySet().iterator();
		
		if (processVars.isEmpty()) {
			LOGGER.info("*** Process Variables: EMPTY");
		} else {
			LOGGER.info("*** Process Variables: ");
		}
		
		// Print out all the variables. 
		while (entries.hasNext()) {
			Map.Entry<String, Object> thisEntry = entries.next();
			Object object = thisEntry.getValue();
			if (object != null) {
				LOGGER.info(thisEntry.getKey().toString() + " : "+object.toString()); 
			} else {
				LOGGER.info(thisEntry.getKey().toString() + " : null");
			}
		}
		java.util.Date date = new java.util.Date();
		LOGGER.info(">> Date Stamp: "+date);
		//java.util.Date date = new java.util.Date();
		LOGGER.info("*******************");
		
		
		
	}
	

}
