package org.example.core.configuration;


import jakarta.servlet.FilterRegistration;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import jakarta.servlet.ServletContext;

@Configuration
@EnableWebMvc
public class WebInitializer implements WebApplicationInitializer {

    @Override
    public void onStartup( ServletContext servletContext) throws ServletException {
        AnnotationConfigWebApplicationContext context=
                new AnnotationConfigWebApplicationContext();
        context.register(AppConfiguration.class);

        DispatcherServlet dispatcher =
                new DispatcherServlet(context);

        ServletRegistration.Dynamic servlet =
                servletContext.addServlet("dispatcher", dispatcher);
        servlet.setLoadOnStartup(1);
        servlet.addMapping("/");
        servlet.setAsyncSupported(true);

        FilterRegistration.Dynamic securityFilter =
                servletContext.addFilter(
                        "springSecurityFilterChain",
                        new DelegatingFilterProxy("springSecurityFilterChain")
                );

        securityFilter.addMappingForUrlPatterns(null, false, "/*");


    }
}
