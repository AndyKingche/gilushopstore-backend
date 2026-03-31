package com.izenshy.gessainvoice.security.auth;

import com.izenshy.gessainvoice.common.exception.ResourceNotFoundException;
import com.izenshy.gessainvoice.modules.person.user.model.UserModel;
import com.izenshy.gessainvoice.modules.person.user.service.UserService;
import com.izenshy.gessainvoice.security.jwt.JwtRequest;
import com.izenshy.gessainvoice.security.jwt.JwtResponse;
import com.izenshy.gessainvoice.security.jwt.JwtResponseOnlineShop;
import com.izenshy.gessainvoice.security.jwt.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@CrossOrigin(origins = "*", methods= {RequestMethod.GET,RequestMethod.POST,RequestMethod.PUT,RequestMethod.DELETE})
public class JwtAuthenticationController {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final UserService userService;

    @Autowired
    public JwtAuthenticationController(final AuthenticationManager authenticationManager, JwtTokenUtil jwtTokenProvider, UserDetailsService userDetailsService, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
    }

    @PostMapping("/api/v1/authenticate")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody JwtRequest authenticationRequest) throws Exception {
        try {
            authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
            final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
            final String token = jwtTokenProvider.generateToken(userDetails);
            final UserModel user = userService.getUserByUserName(authenticationRequest.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            return ResponseEntity.ok(new JwtResponse(token, user.getUserRol(), user.getEnterpriseId().getId(), user.getUserFirstname() + " "+ user.getUserLastname(), user.getUserGender(), user.getId()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/api/login")
    public ResponseEntity<?> createAuthenticationTokenShop(@RequestBody JwtRequest authenticationRequest) throws Exception {
        try {
            authenticate(authenticationRequest.getUsername(), authenticationRequest.getPassword());
            final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
            final String token = jwtTokenProvider.generateToken(userDetails);
            final UserModel user = userService.getUserByUserName(authenticationRequest.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            return ResponseEntity.ok(new JwtResponseOnlineShop(token, user.getUserFirstname() + " "+ user.getUserLastname()));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/api/v1/invalidate")
    public ResponseEntity<?> invalidateToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        if (token != null) {
            jwtTokenProvider.revokeToken(token);
            return ResponseEntity.ok("Token invalidated.");
        } else {
            return ResponseEntity.badRequest().body("Token not provided.");
        }
    }

    private void authenticate(String username, String password) throws Exception {
        Objects.requireNonNull(username);
        Objects.requireNonNull(password);
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
        } catch (DisabledException e) {
            throw new Exception("USER_DISABLED", e);
        } catch (BadCredentialsException e) {
            throw new Exception("INVALID_CREDENTIALS", e);
        }
    }
}
