package com.uga.stayanalytics.service;

import com.uga.stayanalytics.model.User;
import com.uga.stayanalytics.repository.UserDao;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/** Wires the {@code users} table into Spring Security. */
@Service
public class JdbcUserDetailsService implements UserDetailsService {

    private final UserDao userDao;

    public JdbcUserDetailsService(UserDao userDao) { this.userDao = userDao; }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = userDao.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new org.springframework.security.core.userdetails.User(
                u.getUsername(),
                u.getPasswordHash(),
                List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole()))
        );
    }
}
