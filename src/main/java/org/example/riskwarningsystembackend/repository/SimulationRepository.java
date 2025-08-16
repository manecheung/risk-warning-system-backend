package org.example.riskwarningsystembackend.repository;

import org.example.riskwarningsystembackend.entity.Simulation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 仿真数据访问接口
 * <p>
 * 该接口继承自JpaRepository，提供了对Simulation实体的CRUD操作支持。
 * 通过Spring Data JPA的自动实现机制，无需编写具体实现代码即可获得完整的数据访问功能。
 * </p>
 */
@Repository
public interface SimulationRepository extends JpaRepository<Simulation, Long> {
}

