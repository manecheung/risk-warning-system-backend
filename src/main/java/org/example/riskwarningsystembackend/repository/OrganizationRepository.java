package org.example.riskwarningsystembackend.repository;

import org.example.riskwarningsystembackend.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

/**
 * 组织机构数据访问接口
 * 提供组织机构的增删改查操作以及相关的查询功能
 */
public interface OrganizationRepository extends JpaRepository<Organization, Long> {

    /**
     * 根据父级组织ID查询子组织列表
     *
     * @param parentId 父级组织的ID
     * @return 子组织机构列表
     */
    List<Organization> findByParentId(Long parentId);

    /**
     * 统计指定父级组织下的子组织数量
     *
     * @param parentId 父级组织的ID
     * @return 子组织的数量
     */
    long countByParentId(Long parentId);

    /**
     * 查询所有组织机构并同时获取关联的管理员信息
     * 使用LEFT JOIN FETCH避免N+1查询问题
     *
     * @return 包含管理员信息的所有组织机构列表
     */
    @Query("SELECT o FROM Organization o LEFT JOIN FETCH o.manager")
    List<Organization> findAllWithManager();
}

