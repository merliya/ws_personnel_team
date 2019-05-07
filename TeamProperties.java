package com.jbhunt.personnel.team.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "team.activateTime")
public class TeamProperties {

    private int valueMinutes;

    private int valueHour;

    private int valueDay;

    private int valueYear;

    private int valueMonth;

    private int valueSeconds;

}
