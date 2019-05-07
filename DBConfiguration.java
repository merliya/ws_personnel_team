package com.jbhunt.personnel.team.configuration;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * DB Configuration.
 *
 */
@Configuration
public class DBConfiguration {

	@Bean
	@ConfigurationProperties(prefix = "jbhunt.orderManagement.datasource.teamAndSubscription")
	public DataSource dataSource() {
		return DataSourceBuilder.create().type(BasicDataSource.class).build();
	}

}
