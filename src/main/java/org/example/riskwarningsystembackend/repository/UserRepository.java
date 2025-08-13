package org.example.riskwarningsystembackend.repository;

import org.example.riskwarningsystembackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    long countByOrganizationId(Long organizationId);
    Optional<User> findByUsername(String username);
}
