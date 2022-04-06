package com.example.redditclone.service;

import com.example.redditclone.SpringRedditException;
import com.example.redditclone.dto.RegisterRequest;
import com.example.redditclone.model.NotificationEmail;
import com.example.redditclone.model.RedditUser;
import com.example.redditclone.model.VerificationToken;
import com.example.redditclone.repository.UserRepository;
import com.example.redditclone.repository.VerificationTokenRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthService {


    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final MailService mailService;

    @Transactional
    public void signup(RegisterRequest registerRequest) {
        RedditUser user = new RedditUser();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setCreated(Instant.now());
        user.setEnabled(false);

        userRepository.save(user);

        String token = generateVerificationToken(user);
        mailService.sendEmail(
                new NotificationEmail(
                        "Please Activate your Account",
                        user.getEmail(),
                        "http://localhost:8080/api/auth/accountVerification/" +
                                token));
    }

    private String generateVerificationToken(RedditUser user) {
        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setRedditUser(user);

        verificationTokenRepository.save(verificationToken);
        return token;
    }

    public void verifyAccount(String token) {
        Optional<VerificationToken> verificationToken = verificationTokenRepository.findByToken(token);
        verificationToken.orElseThrow(() -> new SpringRedditException("Invalid Token"));
        fetchUserAndEnable(verificationToken.get());
    }

    @Transactional
    private void fetchUserAndEnable(VerificationToken verificationToken) {
        String username = verificationToken.getRedditUser().getUsername();
        RedditUser redditUser = userRepository.findByUsername(username).orElseThrow(() -> new SpringRedditException("No such user:" + username));
        redditUser.setEnabled(true);
        userRepository.save(redditUser);
    }
}
