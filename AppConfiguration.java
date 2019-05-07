package com.jbhunt.personnel.team.configuration;

import javax.servlet.Filter;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.ziplet.filter.compression.CompressingFilter;

@Configuration
public class AppConfiguration {

    @Bean
    public FilterRegistrationBean filterRegistration() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(compressionFilter());
        registration.setName("compressionFilter");
        registration.setOrder(1);
        return registration;
    }

    /**
     * the compressing filter will gzip web service responses
     *
     * @return CompressingFilter
     */
    @Bean(name = "compressionFilter")
    public Filter compressionFilter() {
        return new CompressingFilter();
    }
}
