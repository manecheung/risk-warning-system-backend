package org.example.riskwarningsystembackend.repository;

import org.example.riskwarningsystembackend.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByKey(String key);
    List<Permission> findByKeyIn(List<String> keys);
}