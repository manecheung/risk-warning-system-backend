package org.example.riskwarningsystembackend.repository;

import org.example.riskwarningsystembackend.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Set<Permission> findByKeyIn(List<String> keys);
}
