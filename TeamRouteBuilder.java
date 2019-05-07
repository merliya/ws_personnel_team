package com.jbhunt.personnel.team.route;

import org.apache.camel.ExchangePattern;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.Setter;

@Setter
@Component
public class TeamRouteBuilder extends ExceptionRouteBuilder {
	private static final String TEAM_DETAILS_TO_TOPIC = "direct:postTeamToTopic";
	private static final String TEAM_TOPIC = "activeMQProducer:topic:{{activemq.teamTopic}}";

	@Setter(value = AccessLevel.NONE)
	private TeamExceptionProcessor teamExceptionProcessor;

	private String postToErrorQueue = "activeMQProducer:queue:{{activemq.errorQueue}}";

	@Autowired
	public TeamRouteBuilder(TeamExceptionProcessor teamExceptionProcessor) {
		this.teamExceptionProcessor = teamExceptionProcessor;
	}

	@Override
	public void configure() throws Exception {

		from(TEAM_DETAILS_TO_TOPIC).log("Team Route Builder:post to topic").marshal().json(JsonLibrary.Jackson)
				.to(ExchangePattern.InOnly, TEAM_TOPIC).end();

		from("direct:postToErrorQueue").routeId("postToErrorQueueRoute")
				.log("Exception occured, posting to Error queue").process(teamExceptionProcessor)
				.to(ExchangePattern.InOnly, postToErrorQueue);
	}
}
