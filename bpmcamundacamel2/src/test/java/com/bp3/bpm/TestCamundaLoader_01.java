package com.bp3.bpm;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.dataset.SimpleDataSet;
import org.apache.camel.component.http4.HttpMethods;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.impl.SimpleRegistry;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.apache.http.client.utils.URIBuilder;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestCamundaLoader_01 extends CamelTestSupport  {
	
	
	// THESE NEED TO BE SET TO YOUR OWN ENVIRONMENT
	// -----------------------------------------------------
	// Base URI
	static String BASE_URI = "http://centosw02esx"; 
	// Port
	static int PORT = 8080;
	// Base Path - NOTE: leaving off the trailing "/" (always getting confused if it's on/off
	static String BASE_PATH = "/engine-rest";
	
	// Setup bpmServices URL (note: using constants - not required but helps manage details
	static String bpmServices = BASE_URI + ":" + PORT + BASE_PATH;
	

	// String bpmProcessName = "simple_print_inputs_w_service_task_02";
	// String processID = "simple_process_started_by_camel_pid";
	// String processID = "simple_process_example_pid";
	String processID = "print_process_variables_pid";
	
	//String processID = "straight_through_process_pid";

	
	String inputFileName = "start_process_variables.json";
	
	final int testBatchSize = 10;
	
	
	@Override
    protected CamelContext createCamelContext() throws Exception {
        
        // Input data: This drives the number of times we call on our leading rout "start" 
        // -------------------
        SimpleDataSet inputDataSet = new SimpleDataSet();
        inputDataSet.setDefaultBody("foo");
        inputDataSet.setSize(testBatchSize);
        
        // -------------------
        
        // Expected output: This keeps track and verifies that the number out matches the number in
        // -------------------
        SimpleDataSet expectedOutputDataSet = new SimpleDataSet();
        expectedOutputDataSet.setDefaultBody(null);
        expectedOutputDataSet.setSize(testBatchSize);
        // -------------------

        // Set the registry with our data sets
        // -------------------
        SimpleRegistry registry = new SimpleRegistry();
        registry.put("input", inputDataSet);
        registry.put("expectedOutput", expectedOutputDataSet);
        // -------------------
        
        return new DefaultCamelContext(registry);
    }
	
	
	@Override
    protected RouteBuilder createRouteBuilder() throws Exception {
		
    	return new RouteBuilder() {
    		
    		public void configure() throws Exception {

    			String jsonStringPayload = TestCamundaUtil.readFileToJsonString(inputFileName); // <<< Read in some JSON    			
    	    	
    	    	
    	    	// Build up the HTTP parameters
    	    	//		NOTE: 	These don't include the Camel HTTP4 specific options. 
    	    	//				Just creating "normal" parameters expected by IBM-BPM ReST API
    	    	// ------------------------------
    			URIBuilder uRIBuilder = new URIBuilder(bpmServices + "/process-definition/key/" + processID + "/start");

    			// ------------------------------
    			// Top-level Camel route - this is where we start
    			from("dataset:input?produceDelay=-1")
    				.setHeader(Exchange.HTTP_METHOD, constant(HttpMethods.POST)) // set to POST method
    				.setHeader(Exchange.CONTENT_TYPE, constant("application/json"))
    				.setHeader(Exchange.HTTP_URI, constant(uRIBuilder.toString())) // add in the IBM-BPM ReST parms
    				.setBody(simple(jsonStringPayload))
    				.to("seda:input"); // send the HTTP4 exchange configs' to our async receiver (like an in-memory queue)
    				
    			// ---------------------------------

    			// This is where we make the actual ReST calls to BPM - "seda" is an in-memory queue
    			//	NOTE: this has NO error catching - "continue" is "true" (even with errors)
    			from("seda:input?concurrentConsumers=100") // <<< set concurrent, parallel processors - sort-of like batching
    				// here we pass in our Camel HTTP4 component options. The "foo" route is ignored, but required (a bug?)
    				.to("http4://foo?&throwExceptionOnFailure=false&maxTotalConnections=200&connectionsPerRoute=100")
					.to("direct:setbodynull"); // clear the body so we can match in/out expectations
    		
    			// ---------------------------------
    			// setting body to null to satisfy assertion - will figure this out later...
    			from("direct:setbodynull")
	    			.process(new Processor() {
	    				@Override
	    				public void process(Exchange exchange) throws Exception {
	    					// set the body to null for now - just need to match expected output at this point. 
	    					exchange.getOut().setBody(null);										
	    				}				
	    			})    			
					.to("dataset:expectedOutput");
    			
    			// ---------------------------------

    		}
    	};    	
    }
	
	
	@Test
	public void A_testDataLoad() throws Exception {
		
        MockEndpoint expectedOutput = getMockEndpoint("dataset:expectedOutput");
        expectedOutput.setResultWaitTime(9000);
        //expectedOutput.setResultWaitTime(90000000); // This extra wait time is helpful when stepping through debug - or huge tests
        expectedOutput.assertIsSatisfied();		
	}
	
	
	
	
	
	
	
}
