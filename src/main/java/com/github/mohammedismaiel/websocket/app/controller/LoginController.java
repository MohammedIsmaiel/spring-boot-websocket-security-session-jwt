package com.github.mohammedismaiel.websocket.app.controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;

@AllArgsConstructor
@RestController
@Log4j2
public class LoginController {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder encoder;

    @PostMapping("/signin")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        log.info(loginRequest.toString());
        Authentication authenticationRequest = UsernamePasswordAuthenticationToken
                .unauthenticated(loginRequest.username(), loginRequest.password());
        Authentication authenticationResponse = this.authenticationManager.authenticate(authenticationRequest);

        return ResponseEntity.ok(generateToken(authenticationResponse));
    }

    @GetMapping("/test")
    public String test() {
        return "secured";
    }

    public record LoginRequest(String username, String password) {
    }

    private String generateToken(Authentication user) {

        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(5, ChronoUnit.HOURS))
                .subject(user.getName())
                .claim("authority", user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList())
                .claim("id", 2)
                .build();
        var encoderParameters = JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), claims);
        return encoder.encode(encoderParameters).getTokenValue();

    }
}