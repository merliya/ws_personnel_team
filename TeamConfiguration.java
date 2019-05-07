package com.jbhunt.personnel.team.configuration;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.jbhunt.biz.securepid.PIDCredentials;
import com.jbhunt.hrms.EOIAPIUtil.apiutil.EOIAPIUtil;
import com.jbhunt.hrms.EOIAPIUtil.util.AuditInformation;
import com.jbhunt.hrms.EOIAPIUtil.util.UserCredentials;

@Configuration
public class TeamConfiguration {
    private static final int CONNECTION_MAX_TOTAL = 10;
    private final PIDCredentials pidCredentials;

    private final PIDCredentials eoipidCredentials;

    public TeamConfiguration(PIDCredentials pidCredentials, PIDCredentials eoipidCredentials) {
        this.pidCredentials = pidCredentials;
        this.eoipidCredentials = eoipidCredentials;
    }

    @Bean
    public RestTemplate teamRestTemplate() {
        MappingJackson2HttpMessageConverter mappingMessageconverter = new MappingJackson2HttpMessageConverter();
        Jaxb2RootElementHttpMessageConverter jaxbMessageConverter = new Jaxb2RootElementHttpMessageConverter();
        final PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(CONNECTION_MAX_TOTAL);
        final HttpClient httpClient = HttpClientBuilder.create().setConnectionManager(connManager).build();
        ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplateBuilder().messageConverters(mappingMessageconverter, jaxbMessageConverter)
                .requestFactory(requestFactory).build();
    }

    @Bean
    EOIAPIUtil eoiApiUtil() {
        return new EOIAPIUtil(new AuditInformation(pidCredentials.getUsername(), "ws_personnel_team"));
    }

    @Bean
    UserCredentials userCredentials() {
        return new UserCredentials(eoipidCredentials.getUsername(), eoipidCredentials.getPassword());
    }
}
