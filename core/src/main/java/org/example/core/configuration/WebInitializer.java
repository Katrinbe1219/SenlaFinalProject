package org.example.core.configuration;


import jakarta.servlet.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.WebApplicationInitializer;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

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
        servlet.setMultipartConfig(new MultipartConfigElement(
                "",
                10*1024*1024, // 10MB
                20*1024*1024, // 20MB
                0
        ));

        FilterRegistration.Dynamic securityFilter =
                servletContext.addFilter(
                        "springSecurityFilterChain",
                        new DelegatingFilterProxy("springSecurityFilterChain")
                );

        securityFilter.addMappingForUrlPatterns(null, false, "/*");


    }
}
