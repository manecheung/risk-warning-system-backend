package org.example.riskwarningsystembackend.repository;

import org.example.riskwarningsystembackend.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    List<Organization> findByParentId(Long parentId);
    long countByParentId(Long parentId);

    @Query("SELECT o FROM Organization o LEFT JOIN FETCH o.manager")
    List<Organization> findAllWithManager();
}
