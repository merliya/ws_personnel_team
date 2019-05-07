package com.jbhunt.personnel.team.configuration;

import java.time.LocalDateTime;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jbhunt.biz.securepid.PIDCredentials;
import com.jbhunt.personnel.team.converter.LocalDateTimeConverter;
import com.jbhunt.personnel.team.properties.TeamElasticProperties;

import io.searchbox.client.JestClient;
import io.searchbox.client.JestClientFactory;
import io.searchbox.client.config.HttpClientConfig;

@Configuration
public class JestConfiguration {
    private final PIDCredentials pidCredentials;
    private final TeamElasticProperties teamElasticProperties;

    public JestConfiguration(PIDCredentials pidCredentials, TeamElasticProperties teamElasticProperties) {
        this.pidCredentials = pidCredentials;
        this.teamElasticProperties = teamElasticProperties;
    }

    @Bean
    public JestClient openClient() {
        Gson gson = new GsonBuilder().registerTypeAdapter(LocalDateTime.class, new LocalDateTimeConverter()).create();
        JestClientFactory factory = new JestClientFactory();
        factory.setHttpClientConfig(new HttpClientConfig.Builder(teamElasticProperties.getUrl())
        		.readTimeout(teamElasticProperties.getJestReadTimeout())
                .connTimeout(teamElasticProperties.getJestReadTimeout())
                .defaultCredentials(pidCredentials.getUsername(), pidCredentials.getPassword()).multiThreaded(true)
                .defaultMaxTotalConnectionPerRoute(teamElasticProperties.getMaxTotalConnectionPerRoute())
                .maxTotalConnection(teamElasticProperties.getMaxTotalConnection())
                .gson(gson).build());

        return factory.getObject();
    }

}
