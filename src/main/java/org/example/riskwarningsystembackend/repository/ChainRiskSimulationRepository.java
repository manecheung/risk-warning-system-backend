package org.example.riskwarningsystembackend.repository;

import org.example.riskwarningsystembackend.entity.ChainRiskSimulation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ChainRiskSimulationRepository extends JpaRepository<ChainRiskSimulation, Long>, JpaSpecificationExecutor<ChainRiskSimulation> {
}
