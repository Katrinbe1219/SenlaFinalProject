package org.example.core.annotations;

import org.example.core.models.Role;
import org.example.core.models.User;
import org.example.core.models.types.RoleTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;


public class WithMockCustomSecurityContextFactory implements
        WithSecurityContextFactory<WithMockCustomUser> {



    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser annotation) {
        SecurityContext ctx = SecurityContextHolder.createEmptyContext();
        if (!annotation.username().equalsIgnoreCase("admin") &&
                !annotation.username().equalsIgnoreCase("an123") &&
        !annotation.username().equalsIgnoreCase("mod123") &&
        !annotation.username().equalsIgnoreCase("maxU") &&
                !annotation.username().equalsIgnoreCase("minU")
        ){
            throw new UsernameNotFoundException(annotation.username());

        }

        User principal = new User();
        principal.setUsername(annotation.username());
        principal.setPassword("password");
        principal.setLogin("login");
        principal.setUpdatedAt(
                Instant.now().minus(annotation.daysFromLastUpdate(), ChronoUnit.DAYS)
        );
        principal.setCreatedAt(Instant.now().minus(6, ChronoUnit.DAYS));
        principal.setEmail("sal@mail.ru");
        principal.setNonLocked(true);

        if (annotation.role() != null){
            Role role = new Role();
            role.setName(RoleTypes.fromString(annotation.role()));
            principal.setRole(role);
        }
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal,null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + annotation.role())));
        ctx.setAuthentication(auth);
        return ctx;

    }
}
