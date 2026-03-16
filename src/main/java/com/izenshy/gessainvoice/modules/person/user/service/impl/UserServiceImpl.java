package com.izenshy.gessainvoice.modules.person.user.service.impl;

import com.izenshy.gessainvoice.modules.enterprises.certificate.model.EnterpriseModel;
import com.izenshy.gessainvoice.modules.enterprises.certificate.repository.EnterpriseRepository;
import com.izenshy.gessainvoice.modules.person.user.dto.UserDTO;
import com.izenshy.gessainvoice.modules.person.user.model.UserModel;
import com.izenshy.gessainvoice.modules.person.user.repository.UserRepository;
import com.izenshy.gessainvoice.modules.person.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EnterpriseRepository enterpriseRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder, EnterpriseRepository enterpriseRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.enterpriseRepository = enterpriseRepository;
    }

    @Override
    public Optional<UserModel> getUserById(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public Optional<UserModel> getUserByRuc(String ruc) {
        return userRepository.findByUserRucAndUserStatusTrue(ruc);
    }

    @Override
    public Optional<UserModel> getUserByUserName(String email) {
        return userRepository.findByUserNameAndUserStatusTrue(email);
    }

    @Override
    public UserModel saveUser(UserDTO newUser) {
        if (newUser == null) {
            throw new IllegalArgumentException("UserDTO cannot be null");
        }
        if (newUser.getEnterpriseId() == null) {
            throw new IllegalArgumentException("Enterprise ID cannot be null");
        }

        Optional<UserModel> existingUser = userRepository.findByUserRucAndUserStatusTrue(newUser.getUserRuc());
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("El RUC ya está registrado: " + newUser.getUserRuc());
        }

        EnterpriseModel existingEnteprise = enterpriseRepository.findById(newUser.getEnterpriseId())
                .orElseThrow(() -> new RuntimeException("Empresa no encontrado con id: " + newUser.getEnterpriseId()));

        UserModel userEntity = new UserModel();
        userEntity.setUserName(newUser.getUserName());
        userEntity.setUserFirstname(newUser.getUserFirstname());
        userEntity.setUserLastname(newUser.getUserLastname());
        userEntity.setUserGender(newUser.getUserGender());
        userEntity.setUserPassword(passwordEncoder.encode(newUser.getUserPassword()));
        userEntity.setUserIdentification(newUser.getUserIdentification());
        userEntity.setUserRuc(newUser.getUserRuc());
        userEntity.setUserRol(newUser.getUserRol());
        userEntity.setUserStatus(newUser.getUserStatus() != null ? newUser.getUserStatus() : true);
        userEntity.setEnterpriseId(existingEnteprise);
        return userRepository.save(userEntity);
    }

    @Override
    public UserModel updateUser(Long userId, UserDTO updateUser) {
        if (updateUser == null) {
            throw new IllegalArgumentException("El objeto UserDTO no puede ser null.");
        }

        UserModel existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + userId));

        if (updateUser.getUserRuc() != null && !updateUser.getUserRuc().equals(existingUser.getUserRuc())) {
            boolean rucExists = userRepository.findByUserRucAndUserStatusTrue(updateUser.getUserRuc()).isPresent();
            if (rucExists) {
                throw new IllegalArgumentException("Ya existe un usuario activo con este RUC: " + updateUser.getUserRuc());
            }
            existingUser.setUserRuc(updateUser.getUserRuc());
        }

        Optional.ofNullable(updateUser.getUserName()).ifPresent(existingUser::setUserName);
        Optional.ofNullable(updateUser.getUserFirstname()).ifPresent(existingUser::setUserFirstname);
        Optional.ofNullable(updateUser.getUserLastname()).ifPresent(existingUser::setUserLastname);
        Optional.ofNullable(updateUser.getUserGender()).ifPresent(existingUser::setUserGender);
        Optional.ofNullable(updateUser.getUserIdentification()).ifPresent(existingUser::setUserIdentification);
        Optional.ofNullable(updateUser.getUserRol()).ifPresent(existingUser::setUserRol);
        Optional.ofNullable(updateUser.getUserStatus()).ifPresent(existingUser::setUserStatus);

        if (updateUser.getUserPassword() != null && !updateUser.getUserPassword().isEmpty()) {
            if (!updateUser.getUserPassword().startsWith("$2a$")) {
                existingUser.setUserPassword(passwordEncoder.encode(updateUser.getUserPassword()));
            } else {

                existingUser.setUserPassword(updateUser.getUserPassword());
            }
        }
        return userRepository.save(existingUser);
    }

    @Override
    public List<UserModel> getAllUser() {
        return userRepository.findAll();
    }

    @Override
    public Optional<UserModel> getUserByRol(String rol) {
        return userRepository.findByUserRolAndUserStatusTrue(rol);
    }

    @Override
    public List<UserModel> getUsersByEnterprise(Long enterpriseId) {
        return userRepository.findByEnterpriseId_Id(enterpriseId);
    }
}
