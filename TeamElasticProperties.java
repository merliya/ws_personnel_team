package com.jbhunt.personnel.team.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "elasticsearch")
public class TeamElasticProperties {

    private String url;

    private String employeeindex;

    private String employeetype;

    private String taskindex;

    private String tasktype;
    
    private int maxTotalConnectionPerRoute;
    
    private int maxTotalConnection;
    
    private int jestReadTimeout;
}