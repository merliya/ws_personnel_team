package com.jbhunt.personnel.team.route;

import org.apache.camel.LoggingLevel;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.Setter;

@Component
public class ExceptionRouteBuilder extends RouteBuilder {

	@Setter(value = AccessLevel.NONE)
	static final String LOG_NAME = "com.jbhunt.personnel.team.route.TeamRouteBuilder";

	private String postToErrorQueue = "direct:postToErrorQueue";

	@Override
	public void configure() throws Exception {
		onException(java.lang.Exception.class).log(LoggingLevel.INFO, "Non-Recoverable Error occured")
				.log(LoggingLevel.ERROR, LOG_NAME, "${exception.stackTrace}").logStackTrace(true).to(postToErrorQueue)
				.end();
	}
}
