package org.example.riskwarningsystembackend.repository;

import org.example.riskwarningsystembackend.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    List<Organization> findByParentId(Long parentId);
    long countByParentId(Long parentId);
}
