package com.jbhunt.personnel.team.route;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.jbhunt.personnel.team.constants.TeamCommonConstants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TeamExceptionProcessor implements Processor {

    @Value("${spring.application.domain}")
    private String domain;
    @Value("${spring.application.subDomain}")
    private String subDomain;

    @Override
    public void process(Exchange exchange) {
        log.info("Entered class TeamExceptionProcessor");
        Exception cause = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Exception.class);
        exchange.getIn().setHeader(TeamCommonConstants.EXCEPTION_DOMAIN, domain);
        exchange.getIn().setHeader(TeamCommonConstants.EXCEPTION_SUB_DOMAIN, subDomain);
        exchange.getIn().setHeader(TeamCommonConstants.ORIGIN_IN_QUEUE,
                exchange.getProperty(TeamCommonConstants.ORIGIN_IN_QUEUE));
        exchange.getIn().setHeader(TeamCommonConstants.EXCEPTION_TYPE, cause.getClass().getCanonicalName());
        exchange.getIn().setHeader(TeamCommonConstants.EXCEPTION_DETAIL, ExceptionUtils.getStackTrace(cause));
        exchange.getIn().setBody(exchange.getProperty(TeamCommonConstants.TEAM_JSON_MESSAGE));
    }

}
