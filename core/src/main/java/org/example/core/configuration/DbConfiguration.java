package org.example.core.configuration;

import liquibase.integration.spring.SpringLiquibase;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
// proxyTargetClass - чтобы было прокси через CGLIB,
// или будет через интерфейс, и если его нет, то работать не будет
// jdk proxy - через интерфейсы
// CGLIB - через наследование
public class DbConfiguration {

    @Value("${db.password}")
    private String dbPassword;

    @Value("${db.user}")
    private String dbUser;

    @Value("${db.url}")
    private String dbUrl;

    @Value("${changelog}")
    private String changelog;

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl(this.dbUrl);
        dataSource.setUsername(this.dbUser);
        dataSource.setPassword(this.dbPassword);
        return dataSource;
    }

    @Bean
    public LocalSessionFactoryBean sessionFactory(DataSource dataSource) {
        // реализуется FactoryBean<SessionFactory> - сам будет инжектить SessionFactory
        LocalSessionFactoryBean factory = new LocalSessionFactoryBean();
        factory.setDataSource(dataSource);
        factory.setConfigLocation(new ClassPathResource("hibernate.cfg.xml"));

        return factory;
    }

    @Bean
    public SpringLiquibase liquibase(DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setShouldRun(true);
        liquibase.setDropFirst(false);
        liquibase.setChangeLog(changelog);
        return liquibase;
    }

    @Bean
    public PlatformTransactionManager transactionManager(SessionFactory sessionFactory){
        return new HibernateTransactionManager(sessionFactory);
    }
}
