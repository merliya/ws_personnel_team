package com.jbhunt.personnel.team.configuration;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.jbhunt.biz.securepid.FusePIDReader;
import com.jbhunt.biz.securepid.PIDCredentials;

/**
 * PID Configuration
 *
 */
@Configuration
public class PIDConfiguration {
    @Bean(name = "pidCredentials")
    public PIDCredentials pidCredentials() {
        FusePIDReader fusePIDReader = new FusePIDReader("ws_personnel_team");
        return fusePIDReader.readPIDCredentials("personnelteamservices");
    }
    
    @Bean
    public PIDCredentials eoipidCredentials() {
        FusePIDReader fusePIDReader = new FusePIDReader("ws_personnel_team");
        return fusePIDReader.readPIDCredentials("personnelteamservices");
    }
    
    
}