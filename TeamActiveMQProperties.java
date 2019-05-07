package com.jbhunt.personnel.team.properties;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import lombok.Data;

@Data
@Component
@RefreshScope
@ConfigurationProperties
public class TeamActiveMQProperties {
	   @Value("${jbhunt.personnel.jms.connectionFactory.activeMQ.producer.brokerURL}")
	    private String producerBrokerUrl;

	    @Value("${jbhunt.personnel.jms.connectionFactory.activeMQ.consumer.brokerURL}")
	    private String consumerBrokerUrl;

	    @Value("${jbhunt.personnel.jms.connectionFactory.activeMQ.consumer.maximumRedeliveryDelay}")
	    private int maximumRedeliveryDelay;

	    @Value("${jbhunt.personnel.jms.connectionFactory.activeMQ.consumer.maxConnections}")
	    private int maxConnections;

	    @Value("${jbhunt.personnel.jms.connectionFactory.activeMQ.consumer.concurrentConsumers}")
	    private int concurrentConsumers;
	    
	    @Value("${jbhunt.personnel.jms.connectionFactory.activeMQ.consumer.backOffMultiplier}")
	    private int backOffMultiplier;
	    
	    @Value("${jbhunt.personnel.jms.connectionFactory.activeMQ.consumer.maximumRedeliveries}")
	    private int maximumRedeliveries;
}
