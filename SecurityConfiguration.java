package com.jbhunt.personnel.team.configuration;

import static com.jbhunt.security.boot.autoconfig.enterprisesecurity.JBHAuthorizationAccessLevel.LIMITED;
import static com.jbhunt.security.boot.autoconfig.enterprisesecurity.JBHAuthorizationAccessLevel.POWER;
import static com.jbhunt.security.boot.autoconfig.enterprisesecurity.JBHAuthorizationAccessLevel.READ_ONLY;
import static com.jbhunt.security.boot.autoconfig.enterprisesecurity.JBHAuthorizationAccessLevel.SUPER;
import static com.jbhunt.security.boot.autoconfig.enterprisesecurity.JBHAuthorizationAccessLevel.UPDATE;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import com.jbhunt.infrastructure.exception.handler.JBHAccessDeniedExceptionHandler;
import com.jbhunt.security.boot.autoconfig.enterprisesecurity.AuthorizationConfiguration;
import com.jbhunt.security.boot.autoconfig.enterprisesecurity.filter.AuthorizationFilter;

/**
 * Security configuration for order service
 *
 */
@Configuration
@EnableWebSecurity
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final AuthorizationConfiguration authorizationConfiguration;

    public SecurityConfiguration(AuthorizationConfiguration authorizationConfiguration) {
        this.authorizationConfiguration = authorizationConfiguration;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
    	http.anonymous().disable();
		
		http.httpBasic().and().exceptionHandling().accessDeniedHandler(new JBHAccessDeniedExceptionHandler()).and().authorizeRequests()
			.antMatchers(HttpMethod.GET).hasAnyAuthority(READ_ONLY.getAuthority(), LIMITED.getAuthority(), UPDATE.getAuthority(), POWER.getAuthority(), SUPER.getAuthority())
			.antMatchers(HttpMethod.POST).hasAnyAuthority(LIMITED.getAuthority(), UPDATE.getAuthority(), POWER.getAuthority(), SUPER.getAuthority())
			.antMatchers(HttpMethod.PUT).hasAnyAuthority(UPDATE.getAuthority(), POWER.getAuthority(), SUPER.getAuthority())
			.antMatchers(HttpMethod.PATCH).hasAnyAuthority(UPDATE.getAuthority(), POWER.getAuthority(), SUPER.getAuthority())
			.antMatchers(HttpMethod.DELETE).hasAnyAuthority(UPDATE.getAuthority(), POWER.getAuthority(), SUPER.getAuthority())
			.anyRequest().denyAll().and().csrf().disable();

        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.addFilterAfter(authorizationConfiguration.getAuthorizationFilter(), BasicAuthenticationFilter.class);
        http.addFilterAfter(authorizationConfiguration.getSwitchUserFilter(), AuthorizationFilter.class);
    }

}
