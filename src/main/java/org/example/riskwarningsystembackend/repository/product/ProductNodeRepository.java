package org.example.riskwarningsystembackend.repository.product;

import org.example.riskwarningsystembackend.entity.ProductNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 产品节点数据访问接口
 * /p
 * 该接口继承自JpaRepository，提供了对ProductNode实体的基本CRUD操作，
 * 并定义了根据名称查询产品节点的自定义方法。
 */
public interface ProductNodeRepository extends JpaRepository<ProductNode, Long> {

    /**
     * 根据产品节点名称查找产品节点
     *
     * @param name 产品节点名称，不能为空
     * @return 返回包含ProductNode的Optional对象，如果找不到对应名称的产品节点则返回空Optional
     */
    Optional<ProductNode> findByName(String name);
}

