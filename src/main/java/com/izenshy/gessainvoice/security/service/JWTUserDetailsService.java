package com.izenshy.gessainvoice.security.service;

import com.izenshy.gessainvoice.modules.person.user.model.UserModel;
import com.izenshy.gessainvoice.modules.person.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class JWTUserDetailsService implements UserDetailsService {
    private final @Lazy UserService userService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        try {
            Optional<UserModel> userFound = userService.getUserByUserName(username);
            if (userFound.isPresent()) {
                return new User(userFound.get().getUserName(), userFound.get().getUserPassword()
                        , new
                        ArrayList<>());
            } else {
                throw new UsernameNotFoundException("User not found with email: " + username);
            }
        } catch (Exception e) {
            System.out.println("Error genereting token"+e);
            throw new UsernameNotFoundException("Error, can not charger User", e);

        }
    }
}
