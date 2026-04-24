package org.example.application.security;

import org.example.application.hibernate.objects.UserHibImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsCustomService implements UserDetailsService {
    private final UserHibImpl userHibImpl;

    public UserDetailsCustomService(UserHibImpl userHibImpl) {
        this.userHibImpl = userHibImpl;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            return userHibImpl.getUserByLoginFullVersion(username);
    }
}
