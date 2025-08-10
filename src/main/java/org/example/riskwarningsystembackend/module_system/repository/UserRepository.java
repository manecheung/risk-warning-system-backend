package org.example.riskwarningsystembackend.module_system.repository;

import org.example.riskwarningsystembackend.module_system.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 用户数据访问接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 根据用户名查找用户
     * @param username 用户名
     * @return Optional<User>
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据用户名或姓名模糊查询用户（分页）
     * @param username 用户名关键词
     * @param name 姓名关键词
     * @param pageable 分页参数
     * @return 用户分页数据
     */
    Page<User> findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCase(String username, String name, Pageable pageable);

    /**
     * 使用JOIN FETCH解决N+1问题，高效地获取用户及其关联的角色和组织
     * @param pageable 分页参数
     * @return 带有完整关联信息的用户分页数据
     */
    @Query(value = "SELECT u FROM User u LEFT JOIN FETCH u.roles LEFT JOIN FETCH u.organization",
           countQuery = "SELECT count(u) FROM User u")
    Page<User> findAllWithDetails(Pageable pageable);

    @Query(value = "SELECT u FROM User u LEFT JOIN FETCH u.roles LEFT JOIN FETCH u.organization WHERE u.username LIKE %:keyword% OR u.name LIKE %:keyword%",
           countQuery = "SELECT count(u) FROM User u WHERE u.username LIKE %:keyword% OR u.name LIKE %:keyword%")
    Page<User> findByKeywordWithDetails(String keyword, Pageable pageable);

}