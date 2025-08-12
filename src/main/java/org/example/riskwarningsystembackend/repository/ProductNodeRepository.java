package org.example.riskwarningsystembackend.repository;

import org.example.riskwarningsystembackend.entity.ProductNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductNodeRepository extends JpaRepository<ProductNode, Long> {
    Optional<ProductNode> findByName(String name);
}
