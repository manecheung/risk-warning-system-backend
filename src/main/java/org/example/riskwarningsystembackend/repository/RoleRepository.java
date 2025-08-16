package org.example.riskwarningsystembackend.repository;

import org.example.riskwarningsystembackend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * RoleRepository接口用于角色数据的持久化操作
 * 继承自JpaRepository，提供基本的CRUD操作功能
 */
public interface RoleRepository extends JpaRepository<Role, Long> {
    /**
     * 根据角色名称查找角色信息
     *
     * @param name 角色名称，用于精确匹配查找
     * @return Optional<Role> 返回匹配的角色对象，如果未找到则返回空的Optional
     */
    Optional<Role> findByName(String name);
}

