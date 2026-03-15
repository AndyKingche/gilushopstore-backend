package com.izenshy.gessainvoice.modules.person.user.repository;

import com.izenshy.gessainvoice.modules.person.user.model.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserModel, Long> {
    Optional<UserModel> findByUserRucAndUserStatusTrue(String ruc);
    Optional<UserModel> findByUserNameAndUserStatusTrue(String userName);
    Optional<UserModel> findByUserRolAndUserStatusTrue(String rol);
    List<UserModel> findByEnterpriseId_Id(Long enterpriseId);
}
