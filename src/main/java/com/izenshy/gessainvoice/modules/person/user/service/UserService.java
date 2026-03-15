package com.izenshy.gessainvoice.modules.person.user.service;

import com.izenshy.gessainvoice.modules.person.user.dto.UserDTO;
import com.izenshy.gessainvoice.modules.person.user.model.UserModel;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<UserModel> getUserById(Long userId);
    Optional<UserModel> getUserByRuc(String ruc);
    Optional<UserModel> getUserByUserName(String email);
    UserModel saveUser(UserDTO newuser);
    UserModel updateUser(Long userId, UserDTO updateUser);
    List<UserModel> getAllUser();
    Optional<UserModel> getUserByRol(String rol);
    List<UserModel> getUsersByEnterprise(Long enterpriseId);

}
