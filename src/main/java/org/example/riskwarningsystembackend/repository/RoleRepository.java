package org.example.riskwarningsystembackend.repository;

import org.example.riskwarningsystembackend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {
}
