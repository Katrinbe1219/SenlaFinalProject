package org.example.core.config;

import liquibase.integration.spring.SpringLiquibase;
import org.example.core.hibernate.TestDataHelper;
import org.example.core.hibernate.dictionaries.UnitHibImpl;
import org.example.core.hibernate.objects.GoodHibImpl;
import org.hibernate.SessionFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
@PropertySource("classpath:database.properties")

public class IntegrationTestConfig {
    // static - чтобы один на все тесты
    static final PostgreSQLContainer<?> postgres;
    static {
        postgres = new PostgreSQLContainer<>("postgres:18-alpine");
        postgres.start();
    }

    @Bean
    public TestDataHelper testDataHelper() {
        return new TestDataHelper();
    }

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource driver = new DriverManagerDataSource();
        driver.setDriverClassName("org.postgresql.Driver");
        // сами генерируются
        driver.setUsername(postgres.getUsername());
        driver.setPassword(postgres.getPassword());
        driver.setUrl(postgres.getJdbcUrl());
        return driver;
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
        LocalSessionFactoryBean sessionFactoryBean = new LocalSessionFactoryBean();
        sessionFactoryBean.setDataSource(dataSource);
        sessionFactoryBean.setConfigLocation(new ClassPathResource("hibernate.cfg.xml"));
        return sessionFactoryBean;
    }

    @Bean
    public PlatformTransactionManager transactionManager(SessionFactory sessionFactory) {
        return new HibernateTransactionManager(sessionFactory);
    }

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setShouldRun(true);
        liquibase.setChangeLog("classpath:db/changelog/changelog-test.xml");
        return liquibase;
    }

    @Bean
    public GoodHibImpl goodHib() {
        return new GoodHibImpl();
    }
    @Bean
    public UnitHibImpl unitHib() {
        return new UnitHibImpl();
    }
}

