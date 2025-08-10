package org.example.riskwarningsystembackend.module_system.repository;

import org.example.riskwarningsystembackend.module_system.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 权限数据访问接口
 */
@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    Optional<Permission> findByPermissionKey(String permissionKey);

    Set<Permission> findByPermissionKeyIn(List<String> permissionKeys);
}