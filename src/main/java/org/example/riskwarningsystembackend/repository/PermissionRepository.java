package org.example.riskwarningsystembackend.repository;

import org.example.riskwarningsystembackend.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * 权限仓库接口，用于操作权限相关的数据访问
 * 继承自JpaRepository，提供基本的CRUD操作
 */
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    /**
     * 根据权限键查找权限信息
     *
     * @param key 权限键，用于唯一标识一个权限
     * @return 返回匹配的权限对象，如果不存在则返回空的Optional
     */
    Optional<Permission> findByKey(String key);

    /**
     * 根据权限键列表查找多个权限信息
     *
     * @param keys 权限键列表，用于批量查询多个权限
     * @return 返回匹配的权限列表
     */
    List<Permission> findByKeyIn(List<String> keys);
}
