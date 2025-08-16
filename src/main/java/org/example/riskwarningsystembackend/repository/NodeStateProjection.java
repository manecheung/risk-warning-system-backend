package org.example.riskwarningsystembackend.repository;

/**
 * NodeStateProjection接口用于定义节点状态数据的投影接口。
 * 该接口提供了获取公司ID、状态、KRI评分和内部因子的方法，
 * 通常用于从数据库查询中投影特定字段的数据。
 */
public interface NodeStateProjection {
    /**
     * 获取公司ID
     * @return 公司ID，返回Integer类型
     */
    Integer getCompanyId();

    /**
     * 获取状态值
     * @return 状态值，返回Integer类型
     */
    Integer getState();

    /**
     * 获取KRI评分
     * @return KRI评分，返回Double类型
     */
    Double getKriScore();

    /**
     * 获取内部因子值
     * @return 内部因子值，返回Double类型
     */
    Double getInnerFactor();
}

