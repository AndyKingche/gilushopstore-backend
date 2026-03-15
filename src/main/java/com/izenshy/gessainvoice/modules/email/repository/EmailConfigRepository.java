package com.izenshy.gessainvoice.modules.email.repository;


import com.izenshy.gessainvoice.modules.email.model.EmailConfigModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EmailConfigRepository extends JpaRepository<EmailConfigModel, Long>  {
    Optional<EmailConfigModel> findByUserEmail(String userEmail);
    Optional<EmailConfigModel> findByEnterpriseId_Id(Long enterpriseId);
}
