package com.stp.camel;

import javax.annotation.Resource;
import javax.ejb.Startup;
import javax.enterprise.context.ApplicationScoped;
import javax.jms.ConnectionFactory;

import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.cdi.ContextName;
import org.apache.camel.component.jms.JmsComponent; 


@ApplicationScoped
@Startup
@ContextName("bpmStpRoute2")
public class BpmStpRoute2 extends RouteBuilder {

	
	@Resource(mappedName = "java:/ConnectionFactory")
	private ConnectionFactory connectionFactory;	
	
	
	
	@Override
	public void configure() throws Exception {
		
		// create and set the jms component 
		//---------------------------- 
		JmsComponent component = new JmsComponent();
		component.setConnectionFactory(connectionFactory);
		getContext().addComponent("jmsBpm", component); 

		// Define Camel route 
		//---------------------------- 
		from("jmsBpm:queue:JmsBpmStartQueue").routeId("JmsBpmStartQueue")
			.log("*** jmsBpm:queue:JmsBpmStartQueue header JMSReplyTo: ${header.JMSReplyTo}")
			.log("*** jmsBpm:queue:JmsBpmStartQueue header JMSCorrelationID: ${header.JMSCorrelationID}")
			.setExchangePattern(ExchangePattern.InOnly).to("bean:bpmStartupProcessFromCamel?"
														 + "method=startStpBpm(${header.jMSReplyTo}, "
														 + "${header.JMSCorrelationID}, "
														 + "${header.processID}, "
														 + "${body})");
	}

}
