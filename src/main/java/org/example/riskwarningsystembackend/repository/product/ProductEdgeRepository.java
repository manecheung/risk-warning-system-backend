package org.example.riskwarningsystembackend.repository.product;

import org.example.riskwarningsystembackend.entity.ProductEdge;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 产品边关系数据访问接口
 * /p
 * 该接口继承自JpaRepository，提供了对ProductEdge实体的基本CRUD操作
 * 包括保存、删除、查询等数据库操作方法
 */
public interface ProductEdgeRepository extends JpaRepository<ProductEdge, Long> {
}

