package org.example.riskwarningsystembackend.module_chain_risk.repository;

import org.example.riskwarningsystembackend.module_chain_risk.entity.CompanyRelationship;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRelationshipRepository extends JpaRepository<CompanyRelationship, Long> {
}
