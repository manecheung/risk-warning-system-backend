-- V2__Add_Chain_Risk_Simulations_Table.sql

-- =================================================================
-- Table for Chain Risk Simulations
-- =================================================================
CREATE TABLE chain_risk_simulations
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(1024),
    creator     VARCHAR(255),
    create_time TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    nodes       TEXT         NOT NULL,
    edges       TEXT         NOT NULL,
    risk_path   TEXT
);

-- =================================================================
-- Mock Data for Chain Risk Simulations
-- =================================================================
INSERT INTO chain_risk_simulations (name, description, creator, create_time, nodes, edges, risk_path)
VALUES (
    '新能源汽车芯片断供风险模拟',
    '模拟芯片厂火灾，对下游“未来汽车”造成的产业链冲击影响。',
    '默认用户',
    '2025-08-15 10:00:00',
    '[{"id":"risk-source","label":"风险源：芯片厂火灾","isSource":true},{"id":"supplier-a","label":"A国芯片基板（二级）"},{"id":"supplier-b","label":"B国光刻胶（二级）"},{"id":"chip-fab","label":"凤凰芯片（一级）"},{"id":"car-company","label":"未来汽车（核心）"}]'::TEXT,
    '[{"source":"risk-source","target":"supplier-a"},{"source":"supplier-a","target":"chip-fab"},{"source":"supplier-b","target":"chip-fab"},{"source":"chip-fab","target":"car-company"}]'::TEXT,
    '[["supplier-a"], ["chip-fab"], ["car-company"]]'::TEXT
);
