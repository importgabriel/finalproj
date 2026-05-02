package com.uga.stayanalytics.service;

import com.uga.stayanalytics.model.User;
import com.uga.stayanalytics.repository.UserDao;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserDao userDao;
    private final PasswordEncoder encoder;

    public UserService(UserDao userDao, PasswordEncoder encoder) {
        this.userDao = userDao;
        this.encoder = encoder;
    }

    public Optional<User> findByUsername(String username) {
        return userDao.findByUsername(username);
    }

    public RegistrationResult register(String username, String email, String rawPassword) {
        if (username == null || username.isBlank()) {
            return RegistrationResult.fail("Username is required.");
        }
        if (email == null || email.isBlank() || !email.contains("@")) {
            return RegistrationResult.fail("A valid email is required.");
        }
        if (rawPassword == null || rawPassword.length() < 6) {
            return RegistrationResult.fail("Password must be at least 6 characters.");
        }
        if (userDao.usernameTaken(username)) {
            return RegistrationResult.fail("Username already taken.");
        }
        if (userDao.emailTaken(email)) {
            return RegistrationResult.fail("Email already in use.");
        }
        String hash = encoder.encode(rawPassword);
        long id = userDao.insert(username, email, hash, "USER");
        return RegistrationResult.ok(id);
    }

    public static class RegistrationResult {
        public final boolean success;
        public final String error;
        public final long userId;
        private RegistrationResult(boolean s, String e, long id) {
            this.success = s; this.error = e; this.userId = id;
        }
        static RegistrationResult ok(long id) { return new RegistrationResult(true, null, id); }
        static RegistrationResult fail(String e) { return new RegistrationResult(false, e, 0); }
    }
}
