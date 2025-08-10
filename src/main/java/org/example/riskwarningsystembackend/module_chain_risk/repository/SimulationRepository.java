package org.example.riskwarningsystembackend.module_chain_risk.repository;

import org.example.riskwarningsystembackend.module_chain_risk.entity.Simulation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SimulationRepository extends JpaRepository<Simulation, Long> {
    Page<Simulation> findByNameContainingIgnoreCase(String keyword, Pageable pageable);
}
