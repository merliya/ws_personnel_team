package com.jbhunt.personnel.team.configuration;

import javax.servlet.FilterRegistration;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.filter.CharacterEncodingFilter;

import lombok.extern.slf4j.Slf4j;

/**
 * 
 * This class initializes the application and add the servlets, filters and
 * listeners to the context.
 *
 */
@Slf4j
public class ApplicationInitializer implements WebApplicationInitializer {

	@Override
	public void onStartup(ServletContext context) throws ServletException {
		log.info("WebApplicationInitializer..onStartup");
		// Character encoding filter
		FilterRegistration characterEncodingFilter = context.addFilter("encodingFilter", CharacterEncodingFilter.class);
		characterEncodingFilter.setInitParameter("encoding", "UTF-8");
		characterEncodingFilter.setInitParameter("forceEncoding", "true");
		characterEncodingFilter.addMappingForUrlPatterns(null, false, "/*");
	}
}
