package com.bp3.bpm;

import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;


public class TestCamundaUtil {
	
	/**
	 * 
	 * @param fileName
	 * @return
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	public static final JsonNode readFileToJsonNode(String fileName) throws JsonProcessingException, IOException {
		
		InputStream inputStream = TestCamundaUtil.class.getClassLoader().getResourceAsStream(fileName);
		
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(inputStream);
		
		return rootNode;
		
	}
	
	
	/**
	 * 
	 * @param fileName
	 * @return
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	public static final String readFileToJsonString(String fileName) throws JsonProcessingException, IOException {
		
		JsonNode jsonNode = TestCamundaUtil.readFileToJsonNode(fileName);
		
		ObjectMapper objectMapper = new ObjectMapper();
	    ObjectWriter notPrettyWriter = objectMapper.writer();
	    String jsonString = notPrettyWriter.writeValueAsString(jsonNode);
		
		return jsonString;
	}
	
	
	
	
	/**
	 * 
	 * @param jsonString
	 * @return
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	public static final JsonNode readStringToJsonNode(String jsonString) throws JsonProcessingException, IOException{
		
		ObjectMapper objectMapper = new ObjectMapper();
		JsonNode rootNode = objectMapper.readTree(jsonString);
		
		return rootNode;
		
	}
	
	
	/**
	 * 
	 * @param jsonNode
	 * @return
	 * @throws JsonProcessingException
	 * @throws IOException
	 */
	public static final String readJsonNodeToString(JsonNode jsonNode) throws JsonProcessingException, IOException{
		
	    ObjectMapper objectMapper = new ObjectMapper();
	    ObjectWriter notPrettyWriter = objectMapper.writer();
	    String jsonString = notPrettyWriter.writeValueAsString(jsonNode);
		
		return jsonString;
		
	}
	
	
	/**
	 * 
	 * @param bpmClientTarget
	 * @param processID
	 * @param jsonString
	 * @return
	 */
	public static final Response bpmStartProcess(WebTarget bpmClientTarget, String processID, String jsonString) {
		
		String path = "process-definition/key/" + processID + "/start";
		
		Response response = bpmClientTarget
				.path(path)
				.request()
				.header("Content-Type","application/json")
				.header("Accept","application/json")
				.post(Entity.json(jsonString));
		
		//System.out.println("status: "+response.getStatus());
		//System.out.println("response "+response.readEntity(String.class));
		
		return response;
	}
	
	
	
	
}
