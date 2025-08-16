package org.example.riskwarningsystembackend.repository.product;

import org.example.riskwarningsystembackend.entity.ProductInfo;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 产品信息数据访问接口
 * /p
 * 该接口继承自JpaRepository，提供了对ProductInfo实体的基本CRUD操作
 * 包括保存、删除、查询单个实体和查询实体列表等方法
 */
public interface ProductInfoRepository extends JpaRepository<ProductInfo, Long> {
}

