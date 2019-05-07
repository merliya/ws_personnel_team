package com.jbhunt.personnel.team.configuration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.rest.core.config.RepositoryRestConfiguration;
import org.springframework.data.rest.webmvc.config.RepositoryRestConfigurerAdapter;

import com.jbhunt.personnel.team.Team;
import com.jbhunt.personnel.team.TeamMemberTeamAssignment;
@Configuration
public class RepositoryConfiguration extends RepositoryRestConfigurerAdapter {
	@Override
	public void configureRepositoryRestConfiguration(RepositoryRestConfiguration config) {
		config.exposeIdsFor(Team.class);
		config.exposeIdsFor(TeamMemberTeamAssignment.class);
	}
}
	