package com.izenshy.gessainvoice.modules.person.client.repository;

import com.izenshy.gessainvoice.modules.person.client.model.ClientModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<ClientModel, Long> {

    Optional<ClientModel> findByClientRuc(String ruc);
    Optional<ClientModel> findByClientIdentification(String ci);
    List<ClientModel> findByEnterpriseId_Id(Long enterpriseId);

}
