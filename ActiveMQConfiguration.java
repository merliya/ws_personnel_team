package com.jbhunt.personnel.team.configuration;

import java.util.Arrays;

import javax.jms.JMSException;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.RedeliveryPolicy;
import org.apache.activemq.camel.component.ActiveMQComponent;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

import com.jbhunt.biz.securepid.PIDCredentials;
import com.jbhunt.personnel.team.properties.TeamActiveMQProperties;

@Configuration
public class ActiveMQConfiguration {
    private final TeamActiveMQProperties teamActiveMQProperties;

    public ActiveMQConfiguration(TeamActiveMQProperties teamActiveMQProperties) {
        this.teamActiveMQProperties = teamActiveMQProperties;
    }

    /**
     * @return
     * @throws JMSException
     */
    @Bean
    public ActiveMQComponent activeMQProducer(@Qualifier("pidCredentials") PIDCredentials pidCredential) {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
        activeMQConnectionFactory.setBrokerURL(teamActiveMQProperties.getProducerBrokerUrl());
        activeMQConnectionFactory.setUserName(pidCredential.getUsername());
        activeMQConnectionFactory.setPassword(pidCredential.getPassword());
        activeMQConnectionFactory.setRedeliveryPolicy(redeliveryPolicy());
        // for posting java object not for Json data
        activeMQConnectionFactory.setTrustedPackages(Arrays.asList("com.jbhunt.personnel.team"));
        PooledConnectionFactory pooledConnectionFactory = new PooledConnectionFactory();
        pooledConnectionFactory.setConnectionFactory(activeMQConnectionFactory);
        pooledConnectionFactory.setMaxConnections(teamActiveMQProperties.getMaxConnections());
        ActiveMQComponent activeMQComponent = new ActiveMQComponent();
        activeMQComponent.setConnectionFactory(pooledConnectionFactory);
        activeMQComponent.setConcurrentConsumers(teamActiveMQProperties.getConcurrentConsumers());
        activeMQComponent.setCacheLevel(DefaultMessageListenerContainer.CACHE_CONNECTION);
        activeMQComponent.setTransacted(false);
        return activeMQComponent;
    }
    
    private RedeliveryPolicy redeliveryPolicy() {
        RedeliveryPolicy redeliveryPolicy = new RedeliveryPolicy();
        redeliveryPolicy.setBackOffMultiplier(teamActiveMQProperties.getBackOffMultiplier());
        redeliveryPolicy.setUseExponentialBackOff(true);
        redeliveryPolicy.setMaximumRedeliveryDelay(teamActiveMQProperties.getMaximumRedeliveryDelay());
        redeliveryPolicy.setMaximumRedeliveries(teamActiveMQProperties.getMaximumRedeliveryDelay());
        return redeliveryPolicy;
    }

}
