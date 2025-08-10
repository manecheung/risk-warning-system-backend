package org.example.riskwarningsystembackend.module_system.repository;

import org.example.riskwarningsystembackend.module_system.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 组织数据访问接口
 */
@Repository
public interface OrganizationRepository extends JpaRepository<Organization, Long> {
}
