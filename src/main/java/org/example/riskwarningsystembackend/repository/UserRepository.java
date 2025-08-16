package org.example.riskwarningsystembackend.repository;

import org.example.riskwarningsystembackend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 用户数据访问接口
 * 提供用户相关的数据库操作方法
 */
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     * 根据组织ID统计用户数量
     *
     * @param organizationId 组织ID
     * @return 指定组织下的用户数量
     */
    long countByOrganizationId(Long organizationId);

    /**
     * 根据用户名查找用户
     *
     * @param username 用户名
     * @return 包含用户信息的Optional对象，如果未找到则为空
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据用户名查找用户，并同时获取用户的角色和权限信息
     * 使用LEFT JOIN FETCH避免N+1查询问题，一次性加载用户关联的角色和权限数据
     *
     * @param username 用户名
     * @return 包含用户、角色和权限信息的Optional对象，如果未找到则为空
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.username = :username")
    Optional<User> findByUsernameWithRolesAndPermissions(@Param("username") String username);
}

