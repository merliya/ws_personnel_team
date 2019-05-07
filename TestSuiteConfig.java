package com.jbhunt.personnel.team.config;

import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.jbhunt.biz.securepid.PIDCredentials;
import com.jbhunt.hrms.EOIAPIUtil.apiutil.EOIAPIUtil;
import com.jbhunt.hrms.EOIAPIUtil.util.AuditInformation;
import com.jbhunt.hrms.EOIAPIUtil.util.UserCredentials;

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = "com.jbhunt.personnel")
@EnableJpaRepositories(basePackages = "com.jbhunt.personnel.team.repository")
@EnableJpaAuditing
public class TestSuiteConfig {
    @Autowired
    private DataSource dataSource;

    @Bean
    public DataSource dataSource() {
        EmbeddedDatabaseBuilder builder = new EmbeddedDatabaseBuilder();
        return builder.setType(EmbeddedDatabaseType.DERBY).build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        HibernateJpaVendorAdapter hibernateJpa = new HibernateJpaVendorAdapter();
        LocalContainerEntityManagerFactoryBean emf = new LocalContainerEntityManagerFactoryBean();
        emf.setDataSource(dataSource);
        emf.setPackagesToScan("com.jbhunt.personnel.*", "com.jbhunt.infrastructure.*");
        Map<String, String> jpaProperties = new HashMap<String, String>();
        jpaProperties.put("hibernate.hbm2ddl.auto", "create");
        emf.setJpaPropertyMap(jpaProperties);
        hibernateJpa.setGenerateDdl(true);
        hibernateJpa.setDatabase(Database.DERBY);
        hibernateJpa.setShowSql(true);
        emf.setJpaVendorAdapter(hibernateJpa);
        return emf;
    }

    @Bean
    public JpaTransactionManager transactionManager() {
        JpaTransactionManager txnMgr = new JpaTransactionManager();
        txnMgr.setEntityManagerFactory(entityManagerFactory().getObject());
        return txnMgr;
    }

    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames("classpath:error/ws_personnel_team-messages_en_US");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }

    /**
     * IMPORTANT: This is used to mock the SecurePID Bean
     * 
     * @return
     */
    @Bean(name = "pidCredentials")
    public PIDCredentials pidCredentials() {
        return new PIDCredentials("username", "password");
    }

    @Bean
    public PIDCredentials eoipidCredentials() {
        return new PIDCredentials("username", "password");
    }

    @Bean
    UserCredentials userCredentials() {
        return new UserCredentials("username", "password");
    }

    @Bean
    EOIAPIUtil eoiApiUtil() {

        return new EOIAPIUtil(new AuditInformation("username", "ws_infrastructure_taskassignment"));
    }

    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
        return factory;
    }

    static {
        System.setProperty("runtime.environment", "TEST");
    }
}
